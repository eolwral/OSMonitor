/**
 * @file logcat.cc
 * @brief Logcat Class file
 */

#include "logcat.h"

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  logcat::logcat(logcatLogger source)
  {
    this->_logfd = 0;
    this->_sourceLogger = source;

    if (eventTagMap == NULL)
      eventTagMap = android_openEventTagMap(EVENT_TAG_MAP_FILE);
  }

  logcat::~logcat()
  {
    // close logger device
    if (this->_logfd != 0) {
        this->closeLogDevice(this->_logfd);
        this->_logfd = 0;
    }


    // clean up
    this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_curLogcatList);
  }


  void logcat::fetchLogcat(int logfd)
  {
    unsigned char buffer[LOGGER_ENTRY_MAX_LEN + 1] __attribute__((aligned(4)));

    struct logger_entry *entry = (struct logger_entry *) buffer;

    int readSize = 0;

    while (true) {

      // clean up memory
      memset(buffer, 0, LOGGER_ENTRY_MAX_LEN + 1);

      // read log from device
      readSize = read(logfd, entry, LOGGER_ENTRY_MAX_LEN);
      if (readSize < 0) {
        if (errno == EINTR)
          continue;
        if (errno == EAGAIN)
          break;
      }

      // nothing need to process
      if (readSize == 0)
        break;

      // extract log
      if( this->_sourceLogger == EVENTS)
        this->extractBinaryLog(entry);
      else
        this->extractLog(entry);
    }
  }

  void logcat::extractBinaryLog(struct logger_entry *entry)
  {
    unsigned int tagIndex = 0;
    const unsigned char* message = 0;
    unsigned int messageLen = 0;
    char outBuffer[BUFFERSIZE];
    unsigned int outBufferLen = BUFFERSIZE;

    /*
     * Pull the tag out.
     */
    message = (const unsigned char*) entry->msg;
    messageLen = entry->len;
    if (messageLen < 4)
      return;


    // get tagIndex
    tagIndex = get4LE(message);
    message += 4;
    messageLen -= 4;

    logcatInfo* curLogcatInfo = new logcatInfo();
    curLogcatInfo->set_seconds(entry->sec);
    curLogcatInfo->set_nanoseconds(entry->nsec);
    curLogcatInfo->set_pid(entry->pid);
    curLogcatInfo->set_tid(entry->tid);
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
      if (_curLogcatList.size() >= MAXLOGSIZE) {
        delete *this->_curLogcatList.begin();
        _curLogcatList.erase(_curLogcatList.begin());
      }
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


  void logcat::extractLog(struct logger_entry *entry)
  {
    char *offsetTag = 0;
    char *offsetMessage = 0;

    /* NOTE: driver guarantees we read exactly one full entry */
    entry->msg[entry->len] = '\0';
    offsetTag = entry->msg+1;
    offsetMessage = entry->msg+(strlen(offsetTag)+2);

    // skip it if no data
    if(strlen(offsetMessage) == 0)
      return;

    // add logcat information into list
    logcatInfo* curLogcatInfo = new logcatInfo();
    curLogcatInfo->set_seconds(entry->sec);
    curLogcatInfo->set_nanoseconds(entry->nsec);
    curLogcatInfo->set_pid(entry->pid);
    curLogcatInfo->set_tid(entry->tid);
    curLogcatInfo->set_tag(offsetTag);
    curLogcatInfo->set_message(offsetMessage);

    switch(entry->msg[0])
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

    if (_curLogcatList.size() >= MAXLOGSIZE) {
      delete *this->_curLogcatList.begin();
      _curLogcatList.erase(_curLogcatList.begin());
    }
    this->_curLogcatList.push_back(curLogcatInfo);

    return;
  }

  int logcat::getLogDeivce()
  {
    // set data source
     char *logDevice = 0;
     switch (this->_sourceLogger)
     {
     case RADIO:
       logDevice = strdup("/dev/"LOGGER_LOG_RADIO);
       break;
     case EVENTS:
       logDevice = strdup("/dev/"LOGGER_LOG_EVENTS);
       break;
     case SYSTEM:
       logDevice = strdup("/dev/"LOGGER_LOG_SYSTEM);
       break;
     case MAIN:
       logDevice = strdup("/dev/"LOGGER_LOG_MAIN);
       break;
     }

     // open logcat device
     this->_logfd = open(logDevice, O_NONBLOCK);
     if (this->_logfd < 0) {
       this->_logfd = 0;
     }

     return (this->_logfd);
  }

  void logcat::closeLogDevice(int logfd)
  {
    // close logcat device
    close(logfd);
  }

  bool logcat::checkLogDevice(int logfd)
  {
    struct stat fileStat;
    if(fstat(logfd,&fileStat) < 0)
      return (false);
    return (true);
  }

  void logcat::refresh()
  {
    // clean up
    this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_curLogcatList);

    if (this->_logfd == 0)
      this->_logfd = this->getLogDeivce();

    if (!this->checkLogDevice(this->_logfd)) {
      this->closeLogDevice(this->_logfd);
      this->_logfd = this->getLogDeivce();
    }

    // reload all logcat
    if (this->_logfd != 0)
      this->fetchLogcat(this->_logfd);

  }

  const std::vector<google::protobuf::Message*>& logcat::getData()
  {
    return ((const std::vector<google::protobuf::Message*>&) this->_curLogcatList);
  }
}
}
}
}
