/**
 * @file process.cc
 * @brief  Process Class file
 */

#include "process.h"

#include <android/log.h>

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  process::~process()
  {

    // clean up _lastCPUStatus
    this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_PrevProcessList);

    // clean up _lastCPUStatus
    this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_CurProcessList);
  }

  void process::getBootTime()
  {
    // get uptime
    long uptime = 0;
    FILE *uptimeFile = fopen(SYS_BOOT_TIME, "r");

    if(!uptimeFile)
      uptime = 0;
    else
    {
      fscanf(uptimeFile, "%lu.%*lu", &uptime);
      fclose(uptimeFile);
    }

    time_t currentTime = time(0);

    _bootTime = currentTime - uptime;
  }

  bool process::gatherProcesses()
  {
    // search /proc
    DIR *curDirectory = 0;
    curDirectory = opendir(SYS_PROC_DIR);
    if(curDirectory == 0)
      return (false);

    // enter every process directory
    struct dirent *curDirecotryEntry = 0;
    while((curDirecotryEntry = readdir(curDirectory)) != 0)
    {
      if(isdigit(curDirecotryEntry->d_name[0]) == false)
        continue;

      int curPID = atoi(curDirecotryEntry->d_name);
      if(curPID == 0)
        continue;

      processInfo* curProcessInfo = new processInfo();
      if(curProcessInfo == 0)
        continue;

      if(this->getProcessInfo(*curProcessInfo, curPID) == true)
        this->_CurProcessList.push_back(curProcessInfo);
      else
        delete curProcessInfo;

    }

    closedir(curDirectory);

    // refresh CPU usage
    this->_curCPUInfo.refreshGlobal();

    return (true);
  }

  bool process::getProcessInfo(processInfo& curProcessInfo, unsigned int pid)
  {
    // initialize
    curProcessInfo.set_pid(0);
    curProcessInfo.set_uid(0);
    curProcessInfo.set_ppid(0);
    curProcessInfo.set_owner("");
    curProcessInfo.set_usedusertime(0);
    curProcessInfo.set_usedsystemtime(0);
    curProcessInfo.set_threadcount(0);
    curProcessInfo.set_starttime(0);
    curProcessInfo.set_vsz(0);
    curProcessInfo.set_rss(0);
    curProcessInfo.set_status(processInfo_processStatus_Unknown);
    curProcessInfo.set_prioritylevel(0);
    curProcessInfo.set_cpuusage(0);
    curProcessInfo.set_cputime(0);

    // save pid
    curProcessInfo.set_pid(pid);

    // get UID
    char statProc[BUFFERSIZE];
    struct stat statInfo;
    memset(statProc, 0, BUFFERSIZE);
    snprintf(statProc, BUFFERSIZE, SYS_PROC_LOC, pid);
    if(stat(statProc, &statInfo) == -1)
      return (false);

    curProcessInfo.set_uid(statInfo.st_uid);

    // get Owner
    struct passwd *curPW = 0;
    curPW = getpwuid(statInfo.st_uid);
    if(curPW == 0)
    {
      char uidToStr[BUFFERSIZE];
      memset(uidToStr, 0, BUFFERSIZE);
      snprintf(uidToStr, BUFFERSIZE, "%lu", statInfo.st_uid);
      curProcessInfo.set_owner(uidToStr);
    }
    else
    {
      curProcessInfo.set_owner(curPW->pw_name);
    }

    // get other process information
    snprintf(statProc, BUFFERSIZE, SYS_PROC_STAT, pid);
    FILE* psFile = fopen(statProc, "r");
    if(psFile != 0)
    {
      /* Scan rest of string. */
      char curProcessStats = '\0';
      unsigned long parentPid = 0;
      unsigned long usedUserTime = 0;
      unsigned long usedSystemTime = 0;
      unsigned long threadCount = 0;
      unsigned long startTime = 0;
      unsigned long vsz = 0;
      unsigned long rss = 0;
      fscanf(psFile, SYS_PROC_PATTERN,
                     &curProcessStats,
                     &parentPid,
                     &usedUserTime,
                     &usedSystemTime,
                     &threadCount,
                     &startTime,
                     &vsz,
                     &rss);

      curProcessInfo.set_ppid(parentPid);
      curProcessInfo.set_usedusertime(usedUserTime);
      curProcessInfo.set_usedsystemtime(usedSystemTime);
      curProcessInfo.set_threadcount(threadCount);

      if( (_bootTime+startTime) >0 )
        curProcessInfo.set_starttime(_bootTime+startTime/HZ);

      if(vsz > 0)
        curProcessInfo.set_vsz(vsz/1024);

      if(rss > 0)
        curProcessInfo.set_rss(rss*4);

      fclose(psFile);
      psFile = 0;

      // mapping process status
      switch(curProcessStats)
      {
      case 'R':
        curProcessInfo.set_status(processInfo_processStatus_Running);
        break;
      case 'S':
        curProcessInfo.set_status(processInfo_processStatus_Sleep);
        break;
      case 'Z':
        curProcessInfo.set_status(processInfo_processStatus_Zombie);
        break;
      case 'D':
        curProcessInfo.set_status(processInfo_processStatus_Disk);
        break;
      case 'T':
        curProcessInfo.set_status(processInfo_processStatus_Stopped);
        break;
      case 'W':
        curProcessInfo.set_status(processInfo_processStatus_Page);
        break;
      default:
        curProcessInfo.set_status(processInfo_processStatus_Unknown);
        break;
      }

    }

    // get command line
    snprintf(statProc, BUFFERSIZE, SYS_PROC_CMD, pid);
    psFile = fopen(statProc, "r");
    if(psFile != 0)
    {
      char cmdLine[BUFFERSIZE];
      int readSize = 0;
      memset(cmdLine, 0, BUFFERSIZE);
      readSize = fread(cmdLine, 1, BUFFERSIZE, psFile);
      fclose(psFile);

      cmdLine[BUFFERSIZE-1] = '\0';

      if(readSize != 0)
        curProcessInfo.set_name(cmdLine);
    }

    // if we couldn't get data from cmdline, try to get from stat
    if(curProcessInfo.name().size() == 0)
    {
      snprintf(statProc, BUFFERSIZE, SYS_PROC_STAT, pid);
      psFile = fopen(statProc, "r");
      if(psFile != 0)
      {
        char cmdLine[BUFFERSIZE];
        int matchItem = 0;
        memset(cmdLine, 0, BUFFERSIZE);

        // restrict maximum chars is 255, it could prevent security warning
        matchItem = fscanf(psFile, SYS_PROC_BIN, &pid, cmdLine);
        fclose(psFile);

        if(matchItem == 2)
        {
          cmdLine[BUFFERSIZE-1] = '\0';

          // remove ')'
          if(cmdLine[strlen(cmdLine)-1] == ')')
            cmdLine[strlen(cmdLine)-1] = '\0';

          curProcessInfo.set_name(cmdLine);
        }
      }
    }

    // get priority
    curProcessInfo.set_prioritylevel(getpriority(PRIO_PROCESS, pid));

    // get CPU time
    unsigned long CPUTimeJiffies = (curProcessInfo.usedsystemtime() +
                                    curProcessInfo.usedusertime());
    if(CPUTimeJiffies > 0)
      curProcessInfo.set_cputime( CPUTimeJiffies / HZ);

    return (true);
  }

  void process::calcuateCPUUsage()
  {
    // check 2 lists is ready to calculate
    if(this->_CurProcessList.size() == 0  || this->_PrevProcessList.size() == 0)
      return;

    // search for match PID and summary all CPUTime (Remove it for reducing CPU consume)
    unsigned long curCPUTime = 0;
    for(int curItem=0; curItem < this->_CurProcessList.size(); curItem++)
    {
      for(int prevItem=0; prevItem < this->_PrevProcessList.size(); prevItem++)
      {
        if(this->_CurProcessList[curItem]->pid() == this->_PrevProcessList[prevItem]->pid())
        {
          curCPUTime += this->_CurProcessList[curItem]->usedsystemtime() -
                              this->_PrevProcessList[prevItem]->usedsystemtime();
          curCPUTime += this->_CurProcessList[curItem]->usedusertime() -
                              this->_PrevProcessList[prevItem]->usedusertime();

          prevItem = this->_PrevProcessList.size();
        }
      }
    }

    if(curCPUTime < _curCPUInfo.getCPUTime())
      curCPUTime = (float) _curCPUInfo.getCPUTime();
    if(curCPUTime == 0)
      return;

    // calculate load for each process
    for(int curItem=0; curItem < this->_CurProcessList.size(); curItem++)
    {
      for(int prevItem=0; prevItem < this->_PrevProcessList.size(); prevItem++)
      {
        if(this->_CurProcessList[curItem]->pid() == this->_PrevProcessList[prevItem]->pid())
        {
          unsigned long procCPUTime = 0;
          procCPUTime += this->_CurProcessList[curItem]->usedsystemtime() -
                              this->_PrevProcessList[prevItem]->usedsystemtime();
          procCPUTime += this->_CurProcessList[curItem]->usedusertime() -
                              this->_PrevProcessList[prevItem]->usedusertime();

          if(procCPUTime != 0)
            this->_CurProcessList[curItem]->set_cpuusage((float) procCPUTime * 100/curCPUTime);

          // check upper and bottom limit
          //if(this->_CurProcessList[curItem]->cpuusage() > 100 ||
          //   this->_CurProcessList[curItem]->cpuusage() < 0 )
          //  this->_CurProcessList[curItem]->set_cpuusage(0);
        }
      }
    }
    return ;
  }

  void process::refresh()
  {
    // refresh uptime
    this->getBootTime();

    // clean up
    this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_PrevProcessList);

    // move current to previous
    this->moveDataSet((std::vector<google::protobuf::Message*>&) this->_CurProcessList,
                      (std::vector<google::protobuf::Message*>&) this->_PrevProcessList);

    // gathering information
    if(this->gatherProcesses() == false)
      return;

    // calculate CPU usage
   this->calcuateCPUUsage();

    return;
  }

  const std::vector<google::protobuf::Message*>& process::getData()
  {
    return ((const std::vector<google::protobuf::Message*>&) this->_CurProcessList);
  }
}
}
}
}
