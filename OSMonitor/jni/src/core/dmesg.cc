/**
 * @file dmesg.cc
 * @brief Dmesg Class file
 */

#include "dmesg.h"

#include <android/log.h>

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  dmesg::dmesg()
  {
    this->_bootTime = 0;
  }

  dmesg::~dmesg()
  {
    // clean up _curDemsgList
    this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_curDmesgList);
  }

  void dmesg::getBootTime()
  {
     // get uptime
     long uptime = 0;
     FILE *uptimeFile = fopen(SYS_BOOT_TIME, "r");

     if(!uptimeFile)
       uptime = 0;
     else
     {
       fscanf(uptimeFile, "%lu.%*lu", &uptime);
       fclose(uptimeFile);
     }

     time_t currentTime = time(0);

     _bootTime = currentTime - uptime;
  }

  void dmesg::refresh()
  {
    // clean up
    this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_curDmesgList);

    // refresh boot time
    if (this->_bootTime == 0)
      this->getBootTime();

    char buffer[KLOG_BUF_LEN+1];
    char procLine[BufferSize];
    char* procLineEnd = 0;
    unsigned int procLineLen = 0;
    unsigned int offsetStart = 0;

    int readSize = klogctl(KLOG_READ_ALL, buffer, KLOG_BUF_LEN);
    if(readSize < 0)
      return;

    // set C style end
    buffer[readSize] = 0;

    while((procLineEnd = strstr( buffer + offsetStart, "\n" )) != 0)
    {
      procLineLen = procLineEnd - (buffer + offsetStart);

      // every line must less than BufferSize
      if(procLineLen > BufferSize)
        procLineLen = BufferSize;

      // copy message into line buffer
      strncpy(procLine, buffer + offsetStart, procLineLen);
      procLine[procLineLen] = '\0';

      // move to next block
      offsetStart = offsetStart + procLineLen + 1;

      // prepare a Dmesginfo object
      dmesgInfo* curDmesgInfo = new dmesgInfo();

      char message[BufferSize];
      char level = '\x0';
      unsigned long seconds = 0;
      int itemCounts= 0;
      memset(message, 0, BufferSize);

      // detect log message format and parse it
      if(procLine[3] == '[')
      {
        itemCounts = sscanf(procLine, "<%c>[%lu.%*06lu] %[^\n]",
                            &level,
                            &seconds,
                            message);
        curDmesgInfo->set_seconds(_bootTime+seconds);
      }
      else
      {
        curDmesgInfo->set_seconds(0);
        itemCounts = sscanf(procLine, "<%c>%[^\n]", &level, message);
      }

      switch(level)
      {
      case '0':
        curDmesgInfo->set_level(dmesgInfo_dmesgLevel_EMERGENCY);
        break;
      case '1':
        curDmesgInfo->set_level(dmesgInfo_dmesgLevel_ALERT);
        break;
      case '2':
        curDmesgInfo->set_level(dmesgInfo_dmesgLevel_CRITICAL);
        break;
      case '3':
        curDmesgInfo->set_level(dmesgInfo_dmesgLevel_ERROR);
        break;
      case '4':
        curDmesgInfo->set_level(dmesgInfo_dmesgLevel_WARNING);
        break;
      case '5':
        curDmesgInfo->set_level(dmesgInfo_dmesgLevel_NOTICE);
        break;
      case '6':
        curDmesgInfo->set_level(dmesgInfo_dmesgLevel_INFORMATION);
        break;
      case '7':
        curDmesgInfo->set_level(dmesgInfo_dmesgLevel_DEBUG);
        break;
      default:
        curDmesgInfo->set_level(dmesgInfo_dmesgLevel_INFORMATION);
        break;
      }

      curDmesgInfo->set_message(message);

      // push log into list

      if (itemCounts == 3)
        this->_curDmesgList.push_back(curDmesgInfo);
      else
        delete curDmesgInfo;

      // EOF ?
      if(offsetStart >= readSize)
        break;
    }

  }

  const std::vector<google::protobuf::Message*>& dmesg::getData()
  {
    return ((const std::vector<google::protobuf::Message*>&) this->_curDmesgList);
  }

}
}
}
}

