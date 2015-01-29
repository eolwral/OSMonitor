/**
 * @file dmesg.h
 * @brief Dmesg Class Header file
 */

#ifndef DMESG_H_
#define DMESG_H_

#include <sys/klog.h>

#include "base.h"
#include "dmesgInfo_generated.h"

#define SYS_BOOT_TIME "/proc/uptime"

#define KLOG_SIZE_BUFFER  10
#define KLOG_BUF_SHIFT    17      /* CONFIG_LOG_BUF_SHIFT from our kernel */
#define KLOG_BUF_LEN      (1 << KLOG_BUF_SHIFT)

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

  private:
    const static int BufferSize = 1024;                  /**< internal buffer size */

    unsigned long _bootTime;                            /**< boot time in milliseconds */

    FlatBufferBuilder* _flatbuffer;                     /**< internal FlatBuffer */
    std::vector<Offset<dmesgInfo>> _list;               /**< internal dmesg list */

    /**
     * prepare FlatBuffer
     */
    void prepareBuffer();

    /**
     * finish FlatBuffer
     */
    void finishBuffer();

    /**
     * collecting dmesg
     */
    void gatheringDmesg();

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
     * @return a buffer pointer
     */
    const uint8_t* getData();

    /**
     * get buffer size
     * @return buffer size
     */
    const uoffset_t getSize();

  };

}
}
}
}

#endif /* DMESG_H_ */
