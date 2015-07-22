/**
 * @file logcat.h
 * @brief Logcat Class header file
 */

#ifndef LOGCAT_H_
#define LOGCAT_H_

#define OFFSETOF(type, field)    ((unsigned long) &(((type *) 0)->field))

#define HAVE_IOCTL

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <cstdio>

#include <vector>
#include <string>

#include <android/log.h>

#include <android/event_tag_map.h>

#include "logger.h" // copy it from Android source code
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
    logcatLogger _sourceLogger;                 /**< data source */
    FlatBufferBuilder *_flatbuffer;             /**< internal flatbuffer */
    std::vector<Offset<logcatInfo>> _list;      /**< internal logcat list */
    int _logfd;                                 /**< device handle */

    /**
     * get logger device
     */
    int getLogDeivce();

    /**
     * close logger device
     */
    void closeLogDevice(int logfd);

    /**
     * check logger device status
     * @param[in] logfd file handle
     * @return status
     */
    bool checkLogDevice(int logfd);

    /**
     * read logcat from device
     * @param[in] logcat device handle
     */
    void fetchLogcat(int logfd);

    /**
     * extract log from logger_entry and insert into list
     * @param[in] single log entry
     */
    void extractLog(struct logger_entry *entry);

    /**
     * extract log from logger_entry and insert into list
     * @param[in] single log entry
     */
    void extractBinaryLog(struct logger_entry *entry);

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
