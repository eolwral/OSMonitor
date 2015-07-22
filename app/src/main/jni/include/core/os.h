/**
 * @file OS.h
 * @brief OS Class header file
 */

#ifndef OS_H_
#define OS_H_

#include <sys/cdefs.h>
#include <linux/kernel.h>

#include "base.h"
#include "osInfo_generated.h"

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
    FlatBufferBuilder *_flatbuffer; /**< current OS information */
    osInfoBuilder *_info;           /**< current OS information object */

    /**
     * get upTime
     */
    void getUpTime();

    /**
     * set memory information into osInfo object
     */
    void getMemoryFromFile();

    /**
     * move file to next line
     * @param[in] file handle
     * @return success and fail
     */;
    bool moveToNextLine(FILE *file);

    /**
     * prepare FlatBuffer
     */
    void prepareBuffer();

    /**
     * finish FlatBuffer
     */
    void finishBuffer();

  public:

    /**
     * constructor
     */
    os();

    /**
     * destructor
     */
    ~os();

    /**
     * refresh status
     */
    void refresh();

    /**
     * get system information
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

#endif /* OS_H_ */
