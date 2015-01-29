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

    // set to zero at beginning
    this->_totalCPUUtilization = 0;
    this->_totalCPUTime = 0;
    this->_totalCPUIdleTime = 0;

    this->_prevCPUIdleTime = 0;
    this->_prevCPUTime = 0;

    // init CPU Information
    this->_curFlatBuffer = NULL;
    this->_prevFlatBuffer = NULL;

  }

  cpu::~cpu()
  {
    // clean up _lastCPUStatus
    if (this->_prevFlatBuffer != NULL)
      delete this->_prevFlatBuffer;

    // clean up _currentCPUStatus
    if (this->_curFlatBuffer != NULL)
      delete this->_curFlatBuffer;
  }

  bool cpu::fillCPUInfo(cpuInfoBuilder& cpuInfo, int cpuNum)
  {
    // check CPU number
    if(this->_totalCPUCount == 0 || this->_totalCPUCount < cpuNum)
      return (false);

    // prepare scanf pattern
    char pattern[PATTERNSIZE];
    memset(pattern, 0, PATTERNSIZE);
    snprintf(pattern, PATTERNSIZE, CPU_SIGNLE_PATTERN, cpuNum);

    // open /proc/stat
    FILE *statFile = fopen(SYS_PROC_FILE, "r");
    if(!statFile)
      return (false);

    // extract information
    bool result = false;
    unsigned long usertime = 0;
    unsigned long nicetime = 0;
    unsigned long systemtime = 0;
    unsigned long idletime = 0;
    unsigned long iowaittime = 0;
    unsigned long irqtime = 0;
    unsigned long softirqtime = 0;

    // read data from file
    char buffer[PATTERNSIZE];
    memset(buffer, 0, PATTERNSIZE);
    while(fgets(buffer, PATTERNSIZE, statFile))
    {
      int flag = sscanf( buffer, pattern ,
                         &usertime, &nicetime,
                         &systemtime, &idletime,
                         &iowaittime, &irqtime,
                         &softirqtime);

      // found match record
      if(flag == 7)
      {
        result = true;
        break;
      }
    }

    // close file
    fclose(statFile);

    // processing if data is ready
    if (result == true)
    {
      // fill value
      cpuInfo.add_cpuNumber(cpuNum);
      cpuInfo.add_userTime(usertime);
      cpuInfo.add_niceTime(nicetime);
      cpuInfo.add_systemTime(systemtime);
      cpuInfo.add_idleTime(idletime);
      cpuInfo.add_ioWaitTime(iowaittime);
      cpuInfo.add_irqTime(idletime);
      cpuInfo.add_softIrqTime(softirqtime);
      cpuInfo.add_offLine(false);

      // calculate CPU utilize
      unsigned long cpuTime = usertime + nicetime + systemtime + idletime +
                              iowaittime + irqtime + softirqtime;
      this->calcuateCPUUtil(cpuInfo, cpuNum, cpuTime, idletime, iowaittime);
    }

    return result;
  }

  void cpu::gatherCPUsInfo()
  {
    // gather information for all CPUs
    for(int cpuNum = 0; cpuNum < this->_totalCPUCount; cpuNum++)
    {
      cpuInfoBuilder cpuInfo(*this->_curFlatBuffer);
      if(this->fillCPUInfo(cpuInfo, cpuNum) == false)
        this->fillEmptyCPUInfo(cpuInfo, cpuNum);
      this->_list.push_back(cpuInfo.Finish());
    }
  }

  void cpu::fillEmptyCPUInfo(cpuInfoBuilder& emptyCPUInfo, int cpuNum)
  {
    if (this->_prevFlatBuffer == NULL)
    {
      // give default value
      emptyCPUInfo.add_cpuNumber(cpuNum);
      emptyCPUInfo.add_offLine(true);
      emptyCPUInfo.add_userTime(0);
      emptyCPUInfo.add_niceTime(0);
      emptyCPUInfo.add_systemTime(0);
      emptyCPUInfo.add_idleTime(0);
      emptyCPUInfo.add_ioWaitTime(0);
      emptyCPUInfo.add_irqTime(0);
      emptyCPUInfo.add_softIrqTime(0);
      emptyCPUInfo.add_cpuUtilization(0);
      emptyCPUInfo.add_cpuTime(0);
    }
    else
    {
      const cpuInfoList *prevCPUList = GetcpuInfoList(this->_prevFlatBuffer->GetBufferPointer());
      for (int iterPrev = 0; iterPrev < prevCPUList->list()->size(); iterPrev++) {

        const cpuInfo *prevCPUInfo = prevCPUList->list()->Get(iterPrev);
        if (prevCPUInfo->cpuNumber() != cpuNum)
          continue;

        emptyCPUInfo.add_offLine(true);
        emptyCPUInfo.add_userTime(prevCPUInfo->userTime());
        emptyCPUInfo.add_niceTime(prevCPUInfo->niceTime());
        emptyCPUInfo.add_systemTime(prevCPUInfo->systemTime());
        emptyCPUInfo.add_idleTime(prevCPUInfo->idleTime());
        emptyCPUInfo.add_ioWaitTime(prevCPUInfo->ioWaitTime());
        emptyCPUInfo.add_irqTime(prevCPUInfo->irqTime());
        emptyCPUInfo.add_softIrqTime(prevCPUInfo->softIrqTime());
        break;

      }
    }

    // set rest of values
    emptyCPUInfo.add_cpuTime(0);
    emptyCPUInfo.add_ioUtilization(0);
    emptyCPUInfo.add_cpuUtilization(0);

  }

  void cpu::calcuateCPUUtil(cpuInfoBuilder& curCPUInfo, int cpuNum,
                            unsigned long cpuTime, unsigned long idleTime, unsigned long ioWaitTime)
  {
    if (this->_prevFlatBuffer == NULL)
      return;

    const cpuInfoList *prevCPUList = GetcpuInfoList(this->_prevFlatBuffer->GetBufferPointer());
    for (int iterPrev = 0; iterPrev < prevCPUList->list()->size(); iterPrev++) {

      const cpuInfo *prevCPUInfo = prevCPUList->list()->Get(iterPrev);
      if (prevCPUInfo->cpuNumber() != cpuNum)
        continue;

      unsigned long prevCPUTime = prevCPUInfo->cpuTime() + prevCPUInfo->niceTime() +
                                  prevCPUInfo->systemTime() + prevCPUInfo->idleTime() +
                                  prevCPUInfo->ioWaitTime() + prevCPUInfo->irqTime() +
                                  prevCPUInfo->softIrqTime() ;

      unsigned long totalDeltaTime = cpuTime - prevCPUTime;
      unsigned long totalIdleTime = idleTime - prevCPUInfo->idleTime();
      unsigned long totalIoWaitTime = ioWaitTime - prevCPUInfo->ioWaitTime();

      curCPUInfo.add_cpuTime(totalDeltaTime);

      if(totalIoWaitTime != 0)
        curCPUInfo.add_ioUtilization((float) totalIoWaitTime*100/totalDeltaTime);

      if(totalIdleTime != 0)
        curCPUInfo.add_cpuUtilization(100 - ((float) totalIdleTime*100/ totalDeltaTime));
      else
        curCPUInfo.add_cpuUtilization(100);

      break;
    }

    return;
  }

  void cpu::prepareBuffer()
  {
    // clean up old data
    if (this->_prevFlatBuffer != NULL)
      delete this->_prevFlatBuffer;

    // prepare new buffer
    this->_prevFlatBuffer = this->_curFlatBuffer;
    this->_curFlatBuffer = new FlatBufferBuilder ();
    this->_list.clear ();
  }

  void cpu::finishBuffer()
  {
    // finish the buffer
    auto mloc = CreatecpuInfoList(*this->_curFlatBuffer, this->_curFlatBuffer->CreateVector(this->_list));
    FinishcpuInfoListBuffer(*this->_curFlatBuffer, mloc);
  }

  void cpu::refresh()
  {
    // check CPU number
    if(this->_totalCPUCount <= 0)
      return;

    // clean up old data
    this->prepareBuffer();

    // gathering every CPU
    this->gatherCPUsInfo();

    // finish the buffer
    this->finishBuffer ();

    return;
  }

  const uint8_t* cpu::getData()
  {
    return this->_curFlatBuffer->GetBufferPointer();
  }

  const uoffset_t cpu::getSize()
  {
    return this->_curFlatBuffer->GetSize();
  }

  void cpu::refreshGlobal()
  {
    // prepare scanf pattern
    char curPattern[PATTERNSIZE];
    memset(curPattern, 0, PATTERNSIZE);
    strncpy(curPattern, CPU_GLOBAL_PATTERN, PATTERNSIZE);

    // open /proc/stat
    FILE *statFile = fopen(SYS_PROC_FILE, "r");
    if(!statFile) return;

    // extract information
    unsigned long usertime = 0;
    unsigned long nicetime = 0;
    unsigned long systemtime = 0;
    unsigned long idletime = 0;
    unsigned long iowaittime = 0;
    unsigned long irqtime = 0;
    unsigned long softirqtime = 0;

    // get total CPU usage
    int flag = fscanf( statFile, curPattern ,
                       &usertime, &nicetime,
                       &systemtime, &idletime,
                       &iowaittime, &irqtime,
                       &softirqtime);

    // close file
    fclose(statFile);

    // validate
    if (flag != 7) return;

    // get CPU time
    unsigned long cputime = usertime + nicetime + systemtime + idletime +
                            iowaittime + irqtime + softirqtime;

    // calculate utilization
    unsigned long totalDeltaTime = cputime - this->_prevCPUTime;
    unsigned long totalIdleTime = idletime - this->_prevCPUIdleTime;

    if(totalIdleTime != 0)
      this->_totalCPUUtilization = 100 - ((float) totalIdleTime*100/ totalDeltaTime);
    else
      this->_totalCPUUtilization = 100;

    // save data
    this->_prevCPUTime = cputime;
    this->_prevCPUIdleTime = idletime;

    this->_totalCPUTime = totalDeltaTime;
    this->_totalCPUIdleTime = totalIdleTime;

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
