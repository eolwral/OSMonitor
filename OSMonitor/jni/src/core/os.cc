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

    this->_curOSInfo.push_back(curOSInfo);

    return;
  }

  const std::vector<google::protobuf::Message*>& os::getData()
  {
    return ((const std::vector<google::protobuf::Message*>&) this->_curOSInfo);
  }
}
}
}
}
