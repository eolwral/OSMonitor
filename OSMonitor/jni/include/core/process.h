/**
 * @file process.h
 * @brief Process Class header file
 */

#ifndef PROCESS_H_
#define PROCESS_H_

#include <stdio.h>
#include <dirent.h>
#include <pwd.h>
#include <sys/types.h>
#include <ctype.h>
#include <unistd.h>

#include <sys/stat.h>
#include <sys/resource.h>

#include "base.h"
#include "cpu.h"
#include "processInfo_generated.h"

#define HZ 100
#define SYS_BOOT_TIME "/proc/uptime"
#define SYS_PROC_DIR "/proc"
#define SYS_PROC_LOC "/proc/%d"
#define SYS_PROC_STAT "/proc/%d/stat"
#define SYS_PROC_CMD "/proc/%d/cmdline"
#define SYS_PROC_BIN "%d (%255s)"
#define SYS_PROC_PATTERN "%*d %*s %c %d %*d %*d %*d %*d %*d %*d %*d %*d %*d %lu %lu %*d %*d %*d %*d %d %*lu %lu %lu %lu"

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  /**
   * @class process
   * @brief offer process information functions
   */
  class process : com::eolwral::osmonitor::core::base
  {
  private:

    const static unsigned int BUFFERSIZE = 256; /**< buffer size */

    FlatBufferBuilder *_prevFlatBuffer;               /**< internal previous flatbuffer  */
    FlatBufferBuilder *_curFlatBuffer;                /**< internal current flatbuffer */
    std::vector<Offset<processInfo>> _list;           /**< internal process list */

    cpu _cpuInfo;                               /**< internal CPU usage */

    unsigned long _bootTime;                    /**< boot time in milliseconds */

    /**
     * get boot time
     */
    void getBootTime();

    /**
     * gather process information for /proc directory
     * @return successes or fail
     */
    bool gatherProcesses();

    /**
     * get specific process information by PID
     * @param[in] pid target process id
     * @return success or fail
     */
    bool getProcessInfo(unsigned int pid);

    /**
     * calculate CPU usage
     * @param[in] pid target process id
     * @param[out] curProcessInfo process information object
     * @param[in] systemTime system time for specific process
     * @param[in] userTime user time for specific process
     * @return CPU Usage
     */
    float calculateCPUUsage(unsigned int pid, unsigned long systemTime, unsigned long userTime);

    /**
     * get process name from /proc/{pid}/cmdline
     * @param[in] pid target process id
     * @param[out] buffer buffer for process name
     * @param[in] size buffer size
     * @return true or fail
     */
    bool getProcessName(unsigned int pid, char *buffer, unsigned int size);

    /**
     * get process name from /proc/{pid}/stat
     * @param[in] pid target process id
     * @param[out] buffer buffer for process name
     * @param[in] size buffer size
     * @return true or fail
     */
    bool getProcessNamebyStat(unsigned int pid, char *buffer, unsigned int size);

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
    process();

    /**
     * destructor
     */
    ~process();

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

    /**
     * refresh process information
     * @return true == data is ready to be read, false == not ready
     */
    void refresh();

  };

}
}
}
}

#endif /* PROCESS_H_ */
