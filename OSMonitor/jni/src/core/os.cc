/**
 * @file os.cc
 * @brief os Class file
 */

#include "os.h"
#include <sys/sysinfo.h>

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  void os::refresh()
  {
    // clean up data
    this->clearDataSet((std::vector<google::protobuf::Message*>&)this->_curOSInfo);

    // save value
    osInfo* curOSInfo = new osInfo();
    curOSInfo->set_uptime(0);
    curOSInfo->set_freememory(0);
    curOSInfo->set_totalmemory(0);
    curOSInfo->set_sharedmemory(0);
    curOSInfo->set_bufferedmemory(0);
    curOSInfo->set_freeswap(0);
    curOSInfo->set_totalswap(0);

    // get uptime
    getUpTime(curOSInfo);

    // get current memory from meminfo
    getMemoryFromFile(curOSInfo);

    this->_curOSInfo.push_back(curOSInfo);

    return;
  }

  void os::getUpTime(osInfo *curOSInfo)
  {
     // get uptime
     long uptime = 0;
     FILE *uptimeFile = fopen(OS_BOOT_TIME, "r");

     if(uptimeFile)
     {
       if ( fscanf(uptimeFile, "%lu.%*lu", &uptime) != 1 )
         uptime = 0;
       fclose(uptimeFile);
     }

     time_t currentTime = time(0);

     curOSInfo->set_uptime(currentTime - uptime);

     return;
  }

  bool os::getMemoryFromFile(osInfo *curOSInfo)
  {
    FILE *mif = 0;
    mif = fopen("/proc/meminfo", "r");
    if(mif == 0 )
      return (false);

    /* MemTotal:      2001372 kB
       MemFree:         96856 kB
       Buffers:         20316 kB
       Cached:        1350032 kB */

    unsigned long value = 0;
    fscanf(mif, "MemTotal: %lu kB", &value);
    moveToNextLine(mif);
    if(value != 0)
      curOSInfo->set_totalmemory(value*1024);

    value = 0;
    fscanf(mif, "MemFree: %lu kB", &value);
    moveToNextLine(mif);
    if(value != 0)
      curOSInfo->set_freememory(value*1024);

    value = 0;
    fscanf(mif, "Buffers: %lu kB", &value);
    moveToNextLine(mif);
    if(value != 0)
      curOSInfo->set_bufferedmemory(value*1024);

    value = 0;
    fscanf(mif, "Cached: %lu kB", &value);
    if(value != 0)
      curOSInfo->set_cachedmemory(value*1024);

    while (moveToNextLine(mif) == true)
    {
        value = 0;

        if(fscanf(mif, "SwapTotal: %lu kB", &value) == 1) {
          curOSInfo->set_totalswap(value*1024);
          continue;
        }

        if(fscanf(mif, "SwapFree: %lu kB", &value) == 1) {
          curOSInfo->set_freeswap(value*1024);
          continue;
        }
    }

    fclose(mif);

    return (true);
  }

  bool os::moveToNextLine(FILE *file)
  {
    int ch = 0;
    do {
      ch = getc(file);
    } while ( ch != '\n' && ch != EOF );

    if (ch == '\n')
      return (true);
    return (false);
  }

  const std::vector<google::protobuf::Message*>& os::getData()
  {
    return ((const std::vector<google::protobuf::Message*>&) this->_curOSInfo);
  }
}
}
}
}
