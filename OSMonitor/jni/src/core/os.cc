/**
 * @file os.cc
 * @brief os Class file
 */

#include "os.h"

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  void os::refresh()
  {
    struct sysinfo curSysInfo;

    memset(&curSysInfo, 0, sizeof(struct sysinfo));

    // call sysinfo
    if(sysinfo(&curSysInfo) != 0)
      return;

    // clean up data
    this->clearDataSet((std::vector<google::protobuf::Message*>&)this->_curOSInfo);

    // prepare time
    time_t currentTime = time(0);

    // save value
    osInfo* curOSInfo = new osInfo();
    curOSInfo->set_uptime(currentTime-curSysInfo.uptime);
    curOSInfo->set_freememory(curSysInfo.freeram);
    curOSInfo->set_totalmemory(curSysInfo.totalram);
    curOSInfo->set_sharedmemory(curSysInfo.sharedram);
    curOSInfo->set_bufferedmemory(curSysInfo.bufferram);
    curOSInfo->set_freeswap(curSysInfo.freeswap);
    curOSInfo->set_totalswap(curSysInfo.totalswap);

    // get current memory from meminfo
    getMemoryFromFile(curOSInfo);

    this->_curOSInfo.push_back(curOSInfo);

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

        fscanf(mif, "SwapTotal: %lu kB", &value);
        if(value != 0)
          curOSInfo->set_totalswap(value*1024);

        fscanf(mif, "SwapFree: %lu kB", &value);
        if(value != 0)
          curOSInfo->set_freeswap(value*1024);
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
