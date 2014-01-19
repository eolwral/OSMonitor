/**
 * @file dmesg.h
 * @brief Dmesg Class Header file
 */

#ifndef DMESG_H_
#define DMESG_H_

#include <sys/klog.h>

#include "base.h"
#include "dmesgInfo.pb.h"

#define SYS_BOOT_TIME "/proc/uptime"

#define KLOG_BUF_SHIFT  17      /* CONFIG_LOG_BUF_SHIFT from our kernel */
#define KLOG_BUF_LEN    (1 << KLOG_BUF_SHIFT)

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  /**
   * @class dmesg
   * @brief get dmesg information
   */
  class dmesg : com::eolwral::osmonitor::core::base
  {
    const static int BufferSize = 512;                  /**< internal buffer size */

  private:
    std::vector<dmesgInfo*> _curDmesgList;              /**< internal list for dmesg */
    unsigned long _bootTime;                            /**< boot time in milliseconds */

  public:

    /**
     * constructor for Dmesg
     */
    dmesg();

    /**
     * destructor for Dmesg
     */
    ~dmesg();

    /**
     * refresh dmesg information
     */
    void refresh();

    /**
     * get boot time
     */
    void getBootTime();

    /**
     * get current dmesg information
     * @return a vector contains dmesg information
     */
    const std::vector<google::protobuf::Message*>& getData();

  };

}
}
}
}

#endif /* DMESG_H_ */
