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
#include "processInfo.pb.h"

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

    std::vector<processInfo*> _PrevProcessList; /**< internal previous process list  */
    std::vector<processInfo*> _CurProcessList; /**< internal current process list */

    cpu _curCPUInfo; /**< internal CPU usage */

    unsigned long _bootTime; /**< boot time in milliseconds */

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
     * @param curProcessInfo process information object
     * @param pid target process
     * @return true == success, false == fail
     */
    bool getProcessInfo(processInfo& curProcessInfo, unsigned int pid);

    /**
     * calculate CPU usage for each process
     */
    void calcuateCPUUsage();

  public:

    /**
     * destructor for Process
     */
    ~process();

    /**
     * get process list
     * @return a ProcessInfo list for running process
     */
    const std::vector<google::protobuf::Message*>& getData();

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
