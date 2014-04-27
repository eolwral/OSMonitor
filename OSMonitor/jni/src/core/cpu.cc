/**
 * @file cpu.cc
 * @brief CPU Class file
 */

#include "cpu.h"

#include <android/log.h>

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  cpu::cpu()
  {
    // get total CPUs
    this->_totalCPUCount = sysconf(_SC_NPROCESSORS_CONF);

    // set to zero in the beginning
    this->_totalCPUUtilization = 0;
    this->_totalCPUTime = 0;

    // init CPU Information
    this->_prevTotalCPU = 0;
    this->_currentTotalCPU = 0;

  }

  cpu::~cpu()
  {
    // clean up _lastCPUStatus
    this->clearDataSet((std::vector<google::protobuf::Message*>&) _prevCPUStatus);

    // clean up _currentCPUStatus
    this->clearDataSet((std::vector<google::protobuf::Message*>&) _currentCPUStatus);
  }

  bool cpu::fillCPUInfo(cpuInfo& curCPUInfo, int curCPUNum)
  {
    // check CPU number
    if(this->_totalCPUCount == 0 || this->_totalCPUCount < curCPUNum)
      return (false);

    // prepare scanf pattern
    char curPattern[PATTERNSIZE];
    memset(curPattern, 0, PATTERNSIZE);
    if(curCPUNum == -1)
      strncpy(curPattern, CPU_GLOBAL_PATTERN, PATTERNSIZE);
    else
      snprintf(curPattern, PATTERNSIZE, CPU_SIGNLE_PATTERN, curCPUNum);

    // open /proc/stat
    FILE *statFile = fopen(SYS_PROC_FILE, "r");
    if(!statFile)
      return (false);

    // extract information
    int flag = 0;
    unsigned long usertime = 0;
    unsigned long nicetime = 0;
    unsigned long systemtime = 0;
    unsigned long idletime = 0;
    unsigned long iowaittime = 0;
    unsigned long irqtime = 0;
    unsigned long softirqtime = 0;

    if(curCPUNum == -1)
    {
      // get total CPU usage
      flag = fscanf( statFile, curPattern ,
                         &usertime, &nicetime,
                         &systemtime, &idletime,
                         &iowaittime, &irqtime,
                         &softirqtime);
    }
    else
    {
      // fscanf couldn't work well with cpu0, so use sscanf
      char buffer[PATTERNSIZE];
      memset(buffer, 0, PATTERNSIZE);
      while(fgets(buffer, PATTERNSIZE, statFile))
      {
          flag = sscanf( buffer, curPattern ,
                         &usertime, &nicetime,
                         &systemtime, &idletime,
                         &iowaittime, &irqtime,
                         &softirqtime);

          // found match record
          if(flag == 7)
           break;
          else
            flag = EOF;
      }

      curCPUInfo.set_cpunumber(curCPUNum);
    }

    // fill value
    curCPUInfo.set_usertime(usertime);
    curCPUInfo.set_nicetime(nicetime);
    curCPUInfo.set_systemtime(systemtime);
    curCPUInfo.set_idletime(idletime);
    curCPUInfo.set_iowaittime(iowaittime);
    curCPUInfo.set_irqtime(idletime);
    curCPUInfo.set_softirqtime(softirqtime);
    curCPUInfo.set_offline(false);
    curCPUInfo.set_ioutilization(0);
    curCPUInfo.set_cpuutilization(0);
    curCPUInfo.set_cputime(0);

    // close file
    fclose(statFile);

    // if EOF, no data couldn't read
    if(flag == EOF)
      return (false);
    return (true);
  }

  void cpu::gatherCPUsInfo(std::vector<cpuInfo*>& curCPUStatus)
  {
    // gather information for all CPUs
    for(int curCPUNum = 0; curCPUNum < this->_totalCPUCount; curCPUNum++)
    {
      cpuInfo* curCPUInfo = new cpuInfo();
      if(curCPUInfo == 0)
        break;

      if(this->fillCPUInfo(*curCPUInfo, curCPUNum) == false)
      {
          curCPUInfo->set_cpunumber(curCPUNum);
          curCPUInfo->set_offline(true);
          curCPUInfo->set_usertime(0);
          curCPUInfo->set_nicetime(0);
          curCPUInfo->set_systemtime(0);
          curCPUInfo->set_idletime(0);
          curCPUInfo->set_iowaittime(0);
          curCPUInfo->set_irqtime(0);
          curCPUInfo->set_softirqtime(0);
          curCPUInfo->set_cpuutilization(0);
          curCPUInfo->set_cputime(0);
      }

      curCPUStatus.push_back(curCPUInfo);
    }
  }

  void cpu::calcuateCPUUtil(cpuInfo& prevCPUInfo, cpuInfo& curCPUInfo)
  {
    unsigned long prevCPUTime = prevCPUInfo.usertime() + prevCPUInfo.nicetime() +
                                prevCPUInfo.systemtime() + prevCPUInfo.idletime() +
                                prevCPUInfo.iowaittime() + prevCPUInfo.irqtime() +
                                prevCPUInfo.softirqtime() ;

    unsigned long curCPUTime = curCPUInfo.usertime() + curCPUInfo.nicetime() +
                               curCPUInfo.systemtime() + curCPUInfo.idletime() +
                               curCPUInfo.iowaittime() + curCPUInfo.irqtime() +
                               curCPUInfo.softirqtime();


    unsigned long totalDeltaTime = curCPUTime - prevCPUTime;
    unsigned long totalIdleTime = curCPUInfo.idletime() - prevCPUInfo.idletime();
    unsigned long totalIoWaitTime = curCPUInfo.iowaittime() - prevCPUInfo.iowaittime();

    curCPUInfo.set_cputime(totalDeltaTime);

    if(totalIoWaitTime != 0)
      curCPUInfo.set_ioutilization((float) totalIoWaitTime*100/totalDeltaTime);

    if(totalIdleTime != 0)
      curCPUInfo.set_cpuutilization(100 - ((float) totalIdleTime*100/ totalDeltaTime));
    else
      curCPUInfo.set_cpuutilization(100);

    return;
  }

  void cpu::processOfflineCPUs()
  {
    // lookup CPUs
    bool findCPU = false;
    std::vector<cpuInfo*>::iterator iterPrevCPUInfo = this->_prevCPUStatus.begin();
    while (iterPrevCPUInfo != this->_prevCPUStatus.end())
    {
      findCPU = false;
      std::vector<cpuInfo*>::iterator iterCurCPUInfo = this->_currentCPUStatus.begin();
      while (iterCurCPUInfo != this->_currentCPUStatus.end())
      {
        if ((*iterPrevCPUInfo)->cpunumber() == (*iterCurCPUInfo)->cpunumber())
        {
          findCPU = true;
          break;
        }

        iterCurCPUInfo++;
      }

      // if CPU is off-line, just copy old data
      if (findCPU == true && (*iterCurCPUInfo)->offline())
      {
          (*iterCurCPUInfo)->set_usertime((*iterPrevCPUInfo)->usertime());
          (*iterCurCPUInfo)->set_nicetime((*iterPrevCPUInfo)->nicetime());
          (*iterCurCPUInfo)->set_systemtime((*iterPrevCPUInfo)->systemtime());
          (*iterCurCPUInfo)->set_idletime((*iterPrevCPUInfo)->idletime());
          (*iterCurCPUInfo)->set_iowaittime((*iterPrevCPUInfo)->iowaittime());
          (*iterCurCPUInfo)->set_irqtime((*iterPrevCPUInfo)->irqtime());
          (*iterCurCPUInfo)->set_softirqtime((*iterPrevCPUInfo)->softirqtime());
          (*iterCurCPUInfo)->set_cpuutilization(0);
          (*iterCurCPUInfo)->set_cputime(0);
      }

      iterPrevCPUInfo++;
    }
  }

  void cpu::refresh()
  {
    // check CPU number
    if(this->_totalCPUCount <= 0)
      return;

    // clean up old data
    this->clearDataSet((std::vector<google::protobuf::Message*>&)this->_prevCPUStatus);

    // prepare data
    this->moveDataSet((std::vector<google::protobuf::Message*>&)this->_currentCPUStatus,
                      (std::vector<google::protobuf::Message*>&)this->_prevCPUStatus);

    // gathering every CPU
    this->gatherCPUsInfo(this->_currentCPUStatus);

    // processing off-line CPUS
    if(this->_currentCPUStatus.size() != this->_prevCPUStatus.size())
      this->processOfflineCPUs();

    // calculate every CPU Utilization
    std::vector<cpuInfo*>::iterator iterPrevCPUInfo = this->_prevCPUStatus.begin();
    while(iterPrevCPUInfo != this->_prevCPUStatus.end())
    {

      std::vector<cpuInfo*>::iterator iterCurCPUInfo = this->_currentCPUStatus.begin();
      while(iterCurCPUInfo != this->_currentCPUStatus.end())
      {
        if((*iterCurCPUInfo)->offline() == true) {
          iterCurCPUInfo++;
          continue;
        }

        if ((*iterCurCPUInfo)->cpunumber() == (*iterPrevCPUInfo)->cpunumber())
        {
          if((*iterCurCPUInfo)->offline() == false)
            this->calcuateCPUUtil(*(*iterPrevCPUInfo), *(*iterCurCPUInfo));

          break;
        }
        iterCurCPUInfo++;
      }
      iterPrevCPUInfo++;
    }

    return;
  }

  const std::vector<google::protobuf::Message*>& cpu::getData()
  {
    return ((const std::vector<google::protobuf::Message*>&) this->_currentCPUStatus);
  }

  void cpu::refreshGlobal()
  {
    // gathering total CPU
    if (this->_prevTotalCPU != 0)
      delete this->_prevTotalCPU;

    this->_prevTotalCPU = this->_currentTotalCPU;
    this->_currentTotalCPU = new cpuInfo();
    this->fillCPUInfo(*this->_currentTotalCPU, -1);

    // calculate total CPU Utilization
    if (this->_prevTotalCPU != 0 && this->_currentTotalCPU != 0)
      this->calcuateCPUUtil(*this->_prevTotalCPU, *this->_currentTotalCPU);

    this->_totalCPUUtilization = this->_currentTotalCPU->cpuutilization();
    this->_totalCPUTime = this->_currentTotalCPU->cputime();
  }

  float cpu::getCPUUtilization()
  {
    return (this->_totalCPUUtilization);
  }

  unsigned long cpu::getCPUTime()
  {
    return (this->_totalCPUTime);
  }

}
}
}
}
