/**
 * @file logcat.cc
 * @brief Logcat Class file
 */

#include "logcat_v3.h"

// Android
#include <android/log.h>
#define APPNAME "OSMCore"

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

    this->_flatbuffer = NULL;
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
    if (this->use_count == 0) {
      this->android_logger_open = NULL;
      this->android_logger_close = NULL;
      this->android_logger_read = NULL;
      dlclose(this->handle);
    }

    if (this->_flatbuffer != NULL)
    {
      delete this->_flatbuffer;
      this->_flatbuffer = NULL;
    }
  }


  void logcat::fetchLogcat(struct logger_list* logger)
  {
    while (true)
    {

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
        this->extractLogV3(&log_msg.entry_v1);

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
    Offset<String> tag = 0;
    Offset<String> logMessage = 0;

    /*
     * Pull the tag out.
     */
    message = (const unsigned char*) entry->entry_v2.msg;
    if (entry->entry_v2.hdr_size)
      message = ((unsigned char*) entry) + entry->entry_v2.hdr_size;
    messageLen = entry->entry_v2.len;
    if (messageLen < 4)
      return;

    // get tagIndex
    tagIndex = get4LE(message);
    message += 4;
    messageLen -= 4;

    // clean up
    memset(outBuffer, 0, BUFFERSIZE);
    char *charBuffer = outBuffer;
    if(!this->processBinaryLog(&message, &messageLen, &charBuffer, &outBufferLen))
      return;
    logMessage =  this->_flatbuffer->CreateString(outBuffer);

    // map tagIndex to tag
    if (eventTagMap != NULL)
    {
      const char* tagBuf = android_lookupEventTag(eventTagMap, tagIndex);
      if(tagBuf == NULL)
      {
        char buffer[64];
        memset(buffer, 0, 64);
        snprintf(buffer, 64, "%d", tagIndex );
        tag = this->_flatbuffer->CreateString(buffer);
      }
      else
        tag = this->_flatbuffer->CreateString(tagBuf);
    }

    logcatInfoBuilder logcatInfo(*this->_flatbuffer);
    logcatInfo.add_seconds(entry->entry_v2.sec);
    logcatInfo.add_nanoSeconds(entry->entry_v2.nsec);
    logcatInfo.add_pid(entry->entry_v2.pid);
    logcatInfo.add_tid(entry->entry_v2.tid);
    logcatInfo.add_priority(logPriority_INFO);
    logcatInfo.add_tag(tag);
    logcatInfo.add_message(logMessage);
    this->_list.push_back(logcatInfo.Finish());

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
    type = *message++;
    messageLen--;

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
      else if (bufferLen > 0)
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

  void logcat::extractLogV3(struct logger_entry *entry)
  {

    // copy following codes from liblog.c
    if (entry->len < 3) {
      //LOG: entry too small
      return;
    }

    int msgStart = -1;
    int msgEnd = -1;

    int i;
    char *msg = entry->msg;
    struct logger_entry_v2 *buf2 = (struct logger_entry_v2 *)entry;

    if (buf2->hdr_size) {
      msg = ((char *)buf2) + buf2->hdr_size;
    }

    for (i = 1; i < entry->len; i++) {
      if (msg[i] == '\0') {
        if (msgStart == -1) {
          msgStart = i + 1;
        } else {
          msgEnd = i;
          break;
        }
      }
    }

    if (msgStart == -1) {
        // "LOG: malformed log message\n";
        return;
    }
    if (msgEnd == -1) {
        // incoming message not null-terminated; force it
        msgEnd = entry->len - 1;
        msg[msgEnd] = '\0';
    }

    Offset<String> tag = this->_flatbuffer->CreateString(msg+1);
    Offset<String> message = this->_flatbuffer->CreateString(msg+msgStart);

    // add logcat information into list
    logcatInfoBuilder logcatInfo(*this->_flatbuffer);
    logcatInfo.add_seconds(entry->sec);
    logcatInfo.add_nanoSeconds(entry->nsec);
    logcatInfo.add_pid(entry->pid);
    logcatInfo.add_tid(entry->tid);
    logcatInfo.add_tag(tag);
    logcatInfo.add_message(message);

    switch(msg[0])
    {
    case ANDROID_LOG_DEFAULT:
      logcatInfo.add_priority(logPriority_DEFAULT);
      break;
    case ANDROID_LOG_VERBOSE:
      logcatInfo.add_priority(logPriority_VERBOSE);
      break;
    case ANDROID_LOG_DEBUG:
      logcatInfo.add_priority(logPriority_DEBUG);
      break;
    case ANDROID_LOG_INFO:
      logcatInfo.add_priority(logPriority_INFO);
      break;
    case ANDROID_LOG_WARN:
      logcatInfo.add_priority(logPriority_WARN);
      break;
    case ANDROID_LOG_ERROR:
      logcatInfo.add_priority(logPriority_ERROR);
      break;
    case ANDROID_LOG_FATAL:
      logcatInfo.add_priority(logPriority_FATAL);
      break;
    case ANDROID_LOG_SILENT:
      logcatInfo.add_priority(logPriority_SILENT);
      break;
    default:
      logcatInfo.add_priority(logPriority_UNKNOWN);
      break;
    }

    this->_list.push_back(logcatInfo.Finish());

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

  void logcat::prepareBuffer ()
  {
    // clean up
    if (this->_flatbuffer != NULL)
      delete this->_flatbuffer;

    this->_flatbuffer = new FlatBufferBuilder ();
    this->_list.clear ();
  }

	void
	logcat::finishBuffer ()
	{
	  // logcatInfo
	  Offset<Vector<Offset<logcatInfo> > > list =
	      this->_flatbuffer->CreateVector (this->_list);
	  logcatInfoListBuilder logcatInfoList (*this->_flatbuffer);
	  logcatInfoList.add_list (list);
	  FinishlogcatInfoListBuffer (*this->_flatbuffer,
				      logcatInfoList.Finish ().o);
	}

  void logcat::refresh()
  {
    // clean up
    this->prepareBuffer ();

    // check library function
    if (!this->android_logger_open  || !this->android_logger_close || !this->android_logger_read)
      return;

    // prepare load log device
    struct logger_list *logger = this->prepareLogDevice();
    if (!logger) return;

    this->fetchLogcat(logger);
    this->closeLogDevice(logger);

    // logcatInfo
    this->finishBuffer();

  }

  const uint8_t* logcat::getData()
  {
    return this->_flatbuffer->GetBufferPointer();
  }

  const uoffset_t logcat::getSize()
  {
    return this->_flatbuffer->GetSize();
  }

}
}
}
}
