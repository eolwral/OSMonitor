/**
 * @file logcat.h
 * @brief Logcat Class header file
 */

#ifndef LOGCAT_H_
#define LOGCAT_H_

#define HAVE_IOCTL

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <cstdio>
#include <dlfcn.h>

#include <vector>
#include <string>
#include <android/log.h>

// Android Source
#include "logger_v3.h"
#include "android/event_tag_map_v3.h"

#include "base.h"
#include "logcatInfo_generated.h"

#define BUFFERSIZE 4096

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  /**
   * @brief EventTagMap
   */
  static EventTagMap* eventTagMap = NULL;

  /**
    * @enum LogcatLogger
    * @brief define a logger
    */
   enum logcatLogger
   {
     RADIO,
     EVENTS,
     SYSTEM,
     MAIN
   };

  /**
   * maximum log size
   */
  const static int MAXLOGSIZE = 30000;

  /**
   * @class logcat
   * @brief get logcat information
   */
  class logcat : com::eolwral::osmonitor::core::base
  {
  private:
    logcatLogger _sourceLogger;                          /**< data source */
    FlatBufferBuilder *_flatbuffer;                      /**< internal flatbuffer */
    std::vector<Offset<logcatInfo>> _list;               /**< internal logcat list */
    log_time _lastTime;                                  /**< last time */

    // liblog.so

    // handle
    static void* handle;
    static volatile int use_count;

    // prototype
    typedef struct logger_list* (*android_logger_list_open)(log_id_t id, int mode, unsigned int tail, pid_t pid);
    typedef void (*android_logger_list_free) (struct logger_list *logger_list);
    typedef int (*android_logger_list_read)(struct logger_list *logger_list, struct log_msg *log_msg);

    // entry
    static android_logger_list_open android_logger_open;  /**< dynamic load function */
    static android_logger_list_free android_logger_close; /**< dynamic load function */
    static android_logger_list_read android_logger_read;  /**< dynamic load function */

    /**
     * load liblog.so and get native function address
     * @return true == loaded, false == not found
     */
    bool prepareLogFunction();

    /**
     * get current log device id
     * @return logger_list object
     */
    struct logger_list* prepareLogDevice();

    /**
     * close logger device
     */
    void closeLogDevice(struct logger_list* logger);

    /**
     * read logcat from device
     * @param[in] logger device object
     */
    void fetchLogcat(struct logger_list* logger);

    /**
     * extract log from logger_entry and insert into list
     * @param[in] entry single log entry
     */
    void extractLogV3(struct logger_entry *entry);

    /**
     * extract log from logger_entry and insert into list
     * @param[in] entry single log entry
     */
    void extractBinaryLog(struct log_msg *entry);

    /**
     * convert binary log into readable log
     * @param[in] pMessage binary log
     * @param[in] pMessageLen length of binary log
     * @param[in] pBuffer output buffer
     * @param[in] pBufferLen length of output buffer
     * @return true == success, false == fail
     */
    bool processBinaryLog(const unsigned char** pMessage,
                          unsigned int* pMessageLen,
                          char** pBuffer,
                          unsigned int* pBufferLen);

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
     * @param[in] choose a source logger
     */
    logcat(logcatLogger logger);

    /**
     * destructor
     */
    ~logcat();

    /**
     * refresh logcat list
     */
    void refresh();

    /**
     * get logcat list
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

#endif /* LOGCAT_H_ */
