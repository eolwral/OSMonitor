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

  process::process()
  {
    this->_bootTime = 0;
    this->_prevFlatBuffer = NULL;
    this->_curFlatBuffer = NULL;
  }

  process::~process()
  {
    if (this->_prevFlatBuffer != NULL)
      delete this->_prevFlatBuffer;

    if (this->_curFlatBuffer != NULL)
      delete this->_curFlatBuffer;
  }

  void process::getBootTime()
  {
    // get uptime
    long uptime = 0;
    FILE *uptimeFile = fopen(SYS_BOOT_TIME, "r");

    if(uptimeFile)
    {
      if ( 1 != fscanf(uptimeFile, "%lu.%*u", &uptime) )
        uptime = 0;
      fclose(uptimeFile);
    }

    time_t currentTime = time(0);

    _bootTime = currentTime - uptime;
  }

  bool process::gatherProcesses()
  {
    // refresh CPU usage
    this->_cpuInfo.refreshGlobal();

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

      if(this->getProcessInfo(curPID) != true)
        continue;
    }

    closedir(curDirectory);

    return (true);
  }

  bool process::getProcessName(unsigned int pid, char *buffer, const unsigned int size)
  {
    // get command line
    char statProc[BUFFERSIZE];
    FILE *psFile = 0;
    bool done = false;

    snprintf(statProc, BUFFERSIZE, SYS_PROC_CMD, pid);
    psFile = fopen(statProc, "r");
    if (psFile != 0) {
      char cmdLine[BUFFERSIZE];
      int readSize = 0;
      memset(cmdLine, 0, BUFFERSIZE);
      readSize = fread(cmdLine, 1, BUFFERSIZE, psFile);
      fclose(psFile);
      cmdLine[BUFFERSIZE - 1] = '\0';
      if (readSize != 0)
      {
        memcpy(buffer, cmdLine, size);
        done = true;
      }
    }
    return done;
  }

  bool process::getProcessNamebyStat(unsigned int pid, char *buffer, const unsigned int size)
  {
    char statProc[BUFFERSIZE];
    FILE *psFile = 0;
    bool done = false;

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

        memcpy(buffer, cmdLine, size);
        done = true;
      }
    }
    return done;
  }

  bool process::getProcessInfo(unsigned int pid)
  {

    /* Scan rest of string. */
    char curProcessStats = '\0';
    int parentPid = 0;
    unsigned long usedUserTime = 0;
    unsigned long usedSystemTime = 0;
    int threadCount = 0;
    unsigned long startTime = 0;
    unsigned long vsz = 0;
    unsigned long rss = 0;
    Offset<String> owner = 0;
    Offset<String> processName = 0;

    // get UID
    char statUid[BUFFERSIZE];
    struct stat statInfo;
    memset(statUid, 0, BUFFERSIZE);
    snprintf(statUid, BUFFERSIZE, SYS_PROC_LOC, pid);
    if(stat(statUid, &statInfo) == -1)
      return (false);

    // get other process information
    char statProc[BUFFERSIZE];
    snprintf(statProc, BUFFERSIZE, SYS_PROC_STAT, pid);
    FILE* psFile = fopen(statProc, "r");
    if(psFile != 0)
    {
      // all value must available
      // %*d %*s %c %d %*d %*d %*d %*d %*d %*d %*d %*d %*d %lu %lu %*d %*d %*d %*d %d %*u %lu %lu %lu
      if ( 8 != fscanf(psFile, SYS_PROC_PATTERN,
                       &curProcessStats,
                       &parentPid,
                       &usedUserTime,
                       &usedSystemTime,
                       &threadCount,
                       &startTime,
                       &vsz,
                       &rss))
      {
        fclose(psFile);
        return (false);
      }

      fclose(psFile);
      psFile = 0;
    }

    // get Owner
    struct passwd *curPW = 0;
    curPW = getpwuid(statInfo.st_uid);
    if(curPW == 0)
    {
      char uidToStr[BUFFERSIZE];
      memset(uidToStr, 0, BUFFERSIZE);
      snprintf(uidToStr, BUFFERSIZE, "%lu", statInfo.st_uid);
      owner = this->_curFlatBuffer->CreateString(uidToStr);
    }
    else
      owner = this->_curFlatBuffer->CreateString(curPW->pw_name);

    // get process name
    char processNameBuf[BUFFERSIZE];
    memset(processNameBuf, 0, BUFFERSIZE);
    if (this->getProcessName(pid, processNameBuf, BUFFERSIZE) == false)
      this->getProcessNamebyStat(pid, processNameBuf, BUFFERSIZE);
    processName = this->_curFlatBuffer->CreateString(processNameBuf);

    processInfoBuilder curProcessInfo(*this->_curFlatBuffer);

    curProcessInfo.add_pid(pid);
    curProcessInfo.add_uid(statInfo.st_uid);
    curProcessInfo.add_owner(owner);
    curProcessInfo.add_ppid(parentPid);
    curProcessInfo.add_usedUserTime(usedUserTime);
    curProcessInfo.add_usedSystemTime(usedSystemTime);
    curProcessInfo.add_threadCount(threadCount);
    curProcessInfo.add_name(processName);

    if( (_bootTime+startTime) >0 )
      curProcessInfo.add_startTime(_bootTime+startTime/HZ);
    else
      curProcessInfo.add_startTime(0);

    if(vsz > 0)
      curProcessInfo.add_vsz(vsz/1024);
    else
      curProcessInfo.add_vsz(0);

    if(rss > 0)
      curProcessInfo.add_rss(rss*4);
    else
      curProcessInfo.add_rss(0);

    // get CPU time
    unsigned long CPUTimeJiffies = (usedSystemTime + usedUserTime);
    if(CPUTimeJiffies > 0)
      curProcessInfo.add_cpuTime( CPUTimeJiffies / HZ);

    // mapping process status
    switch(curProcessStats)
    {
    case 'R':
      curProcessInfo.add_status(processStatus_Running);
      break;
    case 'S':
      curProcessInfo.add_status(processStatus_Sleep);
      break;
    case 'Z':
      curProcessInfo.add_status(processStatus_Zombie);
      break;
    case 'D':
      curProcessInfo.add_status(processStatus_Disk);
      break;
    case 'T':
      curProcessInfo.add_status(processStatus_Stopped);
      break;
    case 'W':
      curProcessInfo.add_status(processStatus_Page);
      break;
    default:
      curProcessInfo.add_status(processStatus_Unknown);
      break;
    }

    // get priority
    curProcessInfo.add_priorityLevel(getpriority(PRIO_PROCESS, pid));

    // calculate CPU usage
    curProcessInfo.add_cpuUsage(this->calculateCPUUsage(pid, usedSystemTime, usedUserTime));

    // push into list
    this->_list.push_back(curProcessInfo.Finish());

    return (true);
  }

  float process::calculateCPUUsage(unsigned int pid, unsigned long systemTime, unsigned long userTime)
  {
    float cpuUsage = 0;

    if (this->_prevFlatBuffer == NULL)
      return cpuUsage;

    float curCPUTime = (float) _cpuInfo.getCPUTime();
    if(curCPUTime == 0)
      return cpuUsage;

    // calculate load for each process
    const processInfoList *infoList = GetprocessInfoList(this->_prevFlatBuffer->GetBufferPointer());
    for (int curItem = 0; curItem < infoList->list()->size(); curItem++)
    {
      const processInfo *prevProcessItem = infoList->list()->Get(curItem);

      if (prevProcessItem->pid() != pid)
        continue;

      unsigned long procCPUTime = 0;
      procCPUTime += systemTime - prevProcessItem->usedSystemTime();
      procCPUTime += userTime - prevProcessItem->usedUserTime();

      if(procCPUTime != 0)
        cpuUsage = ((float) (procCPUTime * 100)/curCPUTime);
      break;
    }
    return cpuUsage;
  }

  void process::prepareBuffer()
  {
    // clean up
    if (this->_prevFlatBuffer != NULL)
      delete this->_prevFlatBuffer;

    // move current to previous
    this->_prevFlatBuffer = this->_curFlatBuffer;
    this->_curFlatBuffer = new FlatBufferBuilder ();
    this->_list.clear ();
  }

  void process::finishBuffer ()
  {
    // finish flatbuffer
    auto mloc = CreateprocessInfoList(*this->_curFlatBuffer, this->_curFlatBuffer->CreateVector(this->_list));
    FinishprocessInfoListBuffer(*this->_curFlatBuffer, mloc);
  }

  void process::refresh()
  {
    // refresh uptime
    this->getBootTime();

    // clean up
    this->prepareBuffer ();

    // gathering information
    this->gatherProcesses();

    // finish flatbuffer
    this->finishBuffer ();
  }

  const uint8_t* process::getData()
  {
    return this->_curFlatBuffer->GetBufferPointer();
  }

  const uoffset_t process::getSize()
  {
    return this->_curFlatBuffer->GetSize();
  }
}
}
}
}
