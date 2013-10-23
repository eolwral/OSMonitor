/**
 * @file OS.h
 * @brief OS Class header file
 */

#ifndef OS_H_
#define OS_H_

#include <sys/cdefs.h>
#include <linux/kernel.h>

#include "base.h"
#include "osInfo.pb.h"

#define OS_BOOT_TIME "/proc/uptime"

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  /**
   * @class os
   * @brief get system information
   */
  class os : com::eolwral::osmonitor::core::base
  {
  private:
    std::vector<osInfo*>  _curOSInfo; /**< current OS information */

    void getUpTime(osInfo *curOSInfo);
    bool getMemoryFromFile(osInfo* curOsInfo);
    bool moveToNextLine(FILE *file);

  public:

    /**
     * refresh status
     */
    void refresh();

    /**
     * get system information
     * @return a object contains system information
     */
    const std::vector<google::protobuf::Message*>& getData();

  };

}
}
}
}

#endif /* OS_H_ */
