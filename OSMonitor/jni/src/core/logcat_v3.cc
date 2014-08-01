/**
 * @file logcat.cc
 * @brief Logcat Class file
 */

#include "logcat_v3.h"

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  void *logcat::handle = NULL;
  volatile int logcat::use_count = 0;
  logcat::android_logger_list_open logcat::android_logger_open = NULL;
  logcat::android_logger_list_free logcat::android_logger_close = NULL;
  logcat::android_logger_list_read logcat::android_logger_read = NULL;

  logcat::logcat(logcatLogger source)
  {
    this->_sourceLogger = source;
    this->_lastTime.tv_nsec = 0;
    this->_lastTime.tv_sec = 0;
    this->android_logger_open = NULL;
    this->android_logger_close = NULL;
    this->android_logger_read = NULL;
    this->use_count++;

    if (eventTagMap == NULL)
      eventTagMap = android_openEventTagMap(EVENT_TAG_MAP_FILE);

    if (!this->prepareLogFunction())
    {
      this->android_logger_open = NULL;
      this->android_logger_close = NULL;
      this->android_logger_read = NULL;
    }
  }

  bool logcat::prepareLogFunction()
  {
    if (!this->handle) {
      this->handle = dlopen("liblog.so", RTLD_LAZY);
      if (!this->handle)
        return false;
    }

    if (android_logger_open == NULL) {
      this->android_logger_open = (android_logger_list_open) dlsym(this->handle, "android_logger_list_open");
      if (!this->android_logger_open)
        return false;
    }

    if (android_logger_close == NULL) {
      this->android_logger_close = (android_logger_list_free) dlsym(this->handle, "android_logger_list_free");
      if (!this->android_logger_close)
        return false;
    }

    if (android_logger_read == NULL) {
      this->android_logger_read = (android_logger_list_read) dlsym(this->handle, "android_logger_list_read");
      if (!this->android_logger_read)
        return false;
    }

    return true;
  }

  logcat::~logcat()
  {
    this->use_count--;
    if (this->use_count == 0)
      dlclose(this->handle);

    this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_curLogcatList);
  }


  void logcat::fetchLogcat(struct logger_list* logger)
  {
    while (true) {

      struct log_msg log_msg;

      int ret = this->android_logger_read(logger, &log_msg);

      // nothing need to process or occur error
      if (ret <= 0)
        break;

      // check time
      if ((log_msg.entry.sec < this->_lastTime.tv_sec ) ||
          (log_msg.entry.sec == this->_lastTime.tv_sec  && log_msg.entry.nsec <= this->_lastTime.tv_nsec ))
        continue;

      // extract log
      if (this->_sourceLogger == EVENTS)
        this->extractBinaryLog(&log_msg);
      else
        this->extractLog(&log_msg);

      // save time
      this->_lastTime.tv_nsec = log_msg.entry.nsec;
      this->_lastTime.tv_sec = log_msg.entry.sec;

    }

    return;
  }

  void logcat::extractBinaryLog(struct log_msg *entry)
  {
    unsigned int tagIndex = 0;
    const unsigned char* message = 0;
    unsigned int messageLen = 0;
    char outBuffer[BUFFERSIZE];
    unsigned int outBufferLen = BUFFERSIZE;

    /*
     * Pull the tag out.
     */
    message = (const unsigned char*) entry->entry_v3.msg;
    messageLen = entry->entry_v3.len;
    if (messageLen < 4)
      return;


    // get tagIndex
    tagIndex = get4LE(message);
    message += 4;
    messageLen -= 4;

    logcatInfo* curLogcatInfo = new logcatInfo();
    curLogcatInfo->set_seconds(entry->entry_v3.sec);
    curLogcatInfo->set_nanoseconds(entry->entry_v3.nsec);
    curLogcatInfo->set_pid(entry->entry_v3.pid);
    curLogcatInfo->set_tid(entry->entry_v3.tid);
    curLogcatInfo->set_priority(logcatInfo_logPriority_INFO);

    // map tagIndex to tag
    if (eventTagMap != NULL)
    {
      const char* tag = android_lookupEventTag(eventTagMap, tagIndex);
      if(tag != NULL)
        curLogcatInfo->set_tag(tag);
    }

    if (!curLogcatInfo->has_tag())
    {
      char buffer[64];
      memset(buffer, 0, 64);
      snprintf(buffer, 64, "%d", tagIndex );
      curLogcatInfo->set_tag(buffer);
    }

    // clean up
    memset(outBuffer, 0, BUFFERSIZE);

    char *charBuffer = outBuffer;
    if(this->processBinaryLog(&message, &messageLen, &charBuffer, &outBufferLen))
    {
      curLogcatInfo->set_message(outBuffer);
      if (_curLogcatList.size() >= MAXLOGSIZE)
        _curLogcatList.erase(_curLogcatList.begin());
      _curLogcatList.push_back(curLogcatInfo);
    }
    else
      delete curLogcatInfo;

    return;
  }

  bool logcat::processBinaryLog(const unsigned char** pMessage,
                                unsigned int * pMessageLen,
                                char** pBuffer,
                                unsigned int* pBufferLen)
  {
    const unsigned char* message = *pMessage;
    unsigned int messageLen = *pMessageLen;
    char* buffer = *pBuffer;
    unsigned int bufferLen = *pBufferLen;
    unsigned int usedBufferLen = 0;
    bool isProcessLog = false;
    unsigned char type;

    // convert message
    if (messageLen < 1)
      return (isProcessLog);

    // get type
    type = *message;
    message += 1;
    messageLen -= 1;

    switch (type)
    {
    case EVENT_TYPE_INT:
         /* 32-bit signed int */
    {
      if (messageLen < 4)
        break;

      int ival = get4LE(message);
      message += 4;
      messageLen -= 4;

      usedBufferLen = snprintf(buffer, bufferLen, "%d", ival);
      if (usedBufferLen < bufferLen)
      {
        buffer += usedBufferLen;
        bufferLen -= usedBufferLen;
      }
      else
        break;

      isProcessLog = true;
    }
    break;

    case EVENT_TYPE_LONG:
         /* 64-bit signed long */
    {
      if (messageLen < 8)
        break;

      long long lval = get8LE(message);
      message += 8;
      messageLen -= 8;

      usedBufferLen = snprintf(buffer, bufferLen, "%lld", lval);
      if (usedBufferLen < bufferLen)
      {
        buffer += usedBufferLen;
        bufferLen -= usedBufferLen;
      }
      else
        break;

      isProcessLog = true;
    }
    break;

    case EVENT_TYPE_STRING:
         /* UTF-8 chars, not NULL-terminated */
    {
      if (messageLen < 4)
        break;

      unsigned int strLen = get4LE(message);
      message += 4;
      messageLen -= 4;

      if (messageLen < strLen)
        break;

      if (strLen < bufferLen)
      {
        memcpy(buffer, message, strLen);
        buffer += strLen;
        bufferLen -= strLen;
      }
      else
      {
        memcpy(buffer, message, bufferLen);
        buffer += bufferLen;
        bufferLen -= bufferLen;
        break;
      }

      message += strLen;
      messageLen -= strLen;

      isProcessLog = true;
      break;
    }

    case EVENT_TYPE_LIST:
        /* N items, all different types */
    {
      unsigned char count = 0;

      if (messageLen < 1)
        break;

      count = *message;
      message += 1;
      messageLen -= 1;

      if (bufferLen > 0)
      {
        *buffer = '[';
        buffer += 1;
        bufferLen -= 1;
      }
      else
        break;

      for (int index = 0; index < count; index++)
      {
        // unable to process log
        if(!this->processBinaryLog(&message, &messageLen,
                                   &buffer, &bufferLen))
          break;

        //
        if (index < count-1)
        {
          if (bufferLen > 0)
          {
            *buffer = ',';
            buffer += 1;
            bufferLen -= 1;
          }
          else
            break;
        }
      }

      if (bufferLen > 0)
      {
        *buffer = ']';
        buffer += 1;
        bufferLen -= 1;
      }
      else
        break;

      isProcessLog = true;
    }
    break;

    default:
      break;
    }

    *pMessage = message;
    *pMessageLen = messageLen;
    *pBuffer = buffer;
    *pBufferLen = bufferLen ;

    return (isProcessLog);
  }


  void logcat::extractLog(struct log_msg *entry)
  {
    char *offsetTag = 0;
    char *offsetMessage = 0;

    /* NOTE: driver guarantees we read exactly one full entry */
    entry->entry.msg[entry->entry.len-1] = '\0';
    offsetTag = entry->entry.msg+1;
    offsetMessage = entry->entry.msg+(strlen(offsetTag)+2);

    // skip it if no data
    if(strlen(offsetMessage) == 0)
      return;

    // add logcat information into list
    logcatInfo* curLogcatInfo = new logcatInfo();
    curLogcatInfo->set_seconds(entry->entry.sec);
    curLogcatInfo->set_nanoseconds(entry->entry.nsec);
    curLogcatInfo->set_pid(entry->entry.pid);
    curLogcatInfo->set_tid(entry->entry.tid);
    curLogcatInfo->set_tag(offsetTag);
    curLogcatInfo->set_message(offsetMessage);

    switch(entry->entry_v3.msg[0])
    {
    case ANDROID_LOG_DEFAULT:
      curLogcatInfo->set_priority(logcatInfo_logPriority_DEFAULT);
      break;
    case ANDROID_LOG_VERBOSE:
      curLogcatInfo->set_priority(logcatInfo_logPriority_VERBOSE);
      break;
    case ANDROID_LOG_DEBUG:
      curLogcatInfo->set_priority(logcatInfo_logPriority_DEBUG);
      break;
    case ANDROID_LOG_INFO:
      curLogcatInfo->set_priority(logcatInfo_logPriority_INFO);
      break;
    case ANDROID_LOG_WARN:
      curLogcatInfo->set_priority(logcatInfo_logPriority_WARN);
      break;
    case ANDROID_LOG_ERROR:
      curLogcatInfo->set_priority(logcatInfo_logPriority_ERROR);
      break;
    case ANDROID_LOG_FATAL:
      curLogcatInfo->set_priority(logcatInfo_logPriority_FATAL);
      break;
    case ANDROID_LOG_SILENT:
      curLogcatInfo->set_priority(logcatInfo_logPriority_SILENT);
      break;
    default:
      curLogcatInfo->set_priority(logcatInfo_logPriority_UNKNOWN);
      break;
    }

    if (this->_curLogcatList.size() >= MAXLOGSIZE)
      this->_curLogcatList.erase(this->_curLogcatList.begin());
    this->_curLogcatList.push_back(curLogcatInfo);

    return;
  }

  struct logger_list* logcat::prepareLogDevice()
  {

    log_id_t logDeviceId = LOG_ID_MAIN;
    if (this->_sourceLogger == EVENTS)
      logDeviceId = LOG_ID_MAIN;
    else if (this->_sourceLogger == RADIO)
      logDeviceId = LOG_ID_RADIO;
    else if (this->_sourceLogger == SYSTEM)
      logDeviceId = LOG_ID_SYSTEM;

    struct logger_list *logger = android_logger_open(logDeviceId, O_NONBLOCK | O_RDONLY, 0, 0);
    return (logger);
  }

  void logcat::closeLogDevice(struct logger_list* logger)
  {
    // close logcat device
    if (!logger)
      this->android_logger_close(logger);
  }

  void logcat::refresh()
  {
    // clean up
    this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_curLogcatList);

    // check library function
    if (!this->android_logger_open  && !this->android_logger_close && !this->android_logger_read)
      return;

    // prepare load log device
    struct logger_list *logger = this->prepareLogDevice();
    if (!logger)
      return;

    this->fetchLogcat(logger);
    this->closeLogDevice(logger);
  }

  const std::vector<google::protobuf::Message*>& logcat::getData()
  {
    return ((const std::vector<google::protobuf::Message*>&) this->_curLogcatList);
  }
}
}
}
}
