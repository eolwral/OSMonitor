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

#include <vector>
#include <string>

#include <android/log.h>

#include <android/event_tag_map.h>

#include "logger.h" // copy it from Android source code
#include "base.h"
#include "logcatInfo.pb.h"

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
    std::vector<logcatInfo*> _curLogcatList;    /**< internal logcat list */
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
     * @param logfd file handle
     * @return status
     */
    bool checkLogDevice(int logfd);

    /**
     * read logcat from device
     * @param logcat device handle
     */
    void fetchLogcat(int logfd);

    /**
     * extract log from logger_entry and insert into list
     * @param single log entry
     */
    void extractLog(struct logger_entry *entry);

    /**
     * extract log from logger_entry and insert into list
     * @param single log entry
     */
    void extractBinaryLog(struct logger_entry *entry);

    /**
     * convert binary log into readable log
     * @param pMessage binary log
     * @param pMessageLen length of binary log
     * @param pBuffer output buffer
     * @param pBufferLen length of output buffer
     * @return true == success, false == fail
     */
    bool processBinaryLog(const unsigned char** pMessage,
                          unsigned int* pMessageLen,
                          char** pBuffer,
                          unsigned int* pBufferLen);

  public:

    /**
     * constructor
     * @param choose a source logger
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
     * @return a vector contains logcat
     */
    const std::vector<google::protobuf::Message*>& getData();

  };

}
}
}
}


#endif /* LOGCAT_H_ */
