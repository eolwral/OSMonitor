/**
 * @file command.h
 * @brief Command Class header file
 */

#ifndef COMMAND_H_
#define COMMAND_H_

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <time.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/stat.h>
#include <sys/resource.h>

#include "base.h"
#include "ipcMessage_generated.h"
#include "commandInfo_generated.h"

#define BufferSize 256

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  /**
   * this is helper class for executing command from UI
   */
  class command
  {
  private:
    ipc::ipcCategory category;        /**< internal category */
    core::commandInfo* info;          /**< internal commandInfo */

    /**
     * set process priority
     */
    void setPriority();

    /**
     * kill process by pid
     */
    void killProcess();

    /**
     * set CPU status (online/offline)
     */
    void setCPUStatus();

    /**
     * set CPU Maximum frequency
     */
    void setCPUMaxFrequency();

    /**
     * set CPU Minimum frequency
     */
    void setCPUMinFrequency();

    /**
     * set CPU's governor
     */
    void setCPUGovernor();

  public:
    /**
     * initialize command object
     */
    command(ipc::ipcCategory category, const commandInfo *info);

    /**
     * execute command
     */
    void execute();
  };

}
}
}
}

#endif /* COMMAND_H */
