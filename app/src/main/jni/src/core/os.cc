/**
 * @file os.cc
 * @brief os Class file
 */

#include "os.h"
#include <sys/sysinfo.h>

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  os::os()
  {
    this->_flatbuffer = NULL;
    this->_info = NULL;
  }

  os::~os()
  {
    if (this->_flatbuffer != NULL)
      delete this->_flatbuffer;

    if (this->_info != NULL)
      delete this->_info;
  }

  void os::prepareBuffer()
  {
    if (this->_info != NULL)
      delete this->_info;

    if (this->_flatbuffer != NULL)
      delete this->_flatbuffer;

    this->_flatbuffer = new FlatBufferBuilder();
    this->_info = new osInfoBuilder(*this->_flatbuffer);

  }

  void os::finishBuffer( )
  {
    FinishosInfoBuffer(*this->_flatbuffer, this->_info->Finish().o);
  }

  void os::refresh()
  {
    // clean up data
    this->prepareBuffer();

    // get uptime
    this->getUpTime();

    // get current memory from meminfo
    this->getMemoryFromFile();

    // Finish
    this->finishBuffer();

    return;
  }

  void os::getUpTime()
  {
     // get uptime
     long uptime = 0;
     FILE *uptimeFile = fopen(OS_BOOT_TIME, "r");

     if(uptimeFile)
     {
       if ( 1 != fscanf(uptimeFile, "%lu.%*u", &uptime) )
         uptime = 0;
       fclose(uptimeFile);
     }

     time_t currentTime = time(0);

     this->_info->add_upTime(currentTime - uptime);

     return;
  }

  void os::getMemoryFromFile()
  {

    // set basic value
    this->_info->add_freeMemory(0);
    this->_info->add_totalMemory(0);
    this->_info->add_sharedMemory(0);
    this->_info->add_bufferedMemory(0);
    this->_info->add_freeSwap(0);
    this->_info->add_totalSwap(0);

    FILE *mif = 0;
    mif = fopen("/proc/meminfo", "r");
    if(mif == 0 )
      return;

    /* MemTotal:      2001372 kB
       MemFree:         96856 kB
       Buffers:         20316 kB
       Cached:        1350032 kB */

    unsigned long value = 0;
    if ( fscanf(mif, "MemTotal: %lu kB", &value) == 1 )
    {
      moveToNextLine(mif);
      if(value != 0)
        this->_info->add_totalMemory(value*1024);
    }

    if ( fscanf(mif, "MemFree: %lu kB", &value) == 1 )
    {
      moveToNextLine(mif);
      if(value != 0)
        this->_info->add_freeMemory(value*1024);
    }

    if ( fscanf(mif, "Buffers: %lu kB", &value) == 1 )
    {
      moveToNextLine(mif);
      if(value != 0)
        this->_info->add_bufferedMemory(value*1024);
    }

    if ( fscanf(mif, "Cached: %lu kB", &value) == 1 )
    {
      if(value != 0)
        this->_info->add_cachedMemory(value*1024);
    }

    int currentPos = ftell(mif);
    while (moveToNextLine(mif) == true)
    {
      if(fscanf(mif, "SwapTotal: %lu kB", &value) == 1) {
        this->_info->add_totalSwap(value*1024);
        break;
      }
    }

    fseek(mif, currentPos, SEEK_SET);
    while (moveToNextLine(mif) == true)
    {
      if(fscanf(mif, "SwapFree: %lu kB", &value) == 1) {
        this->_info->add_freeSwap(value*1024);
        break;
      }
    }

    fclose(mif);

    return;
  }

  bool os::moveToNextLine(FILE *file)
  {
    int ch = 0;
    do {
      ch = getc(file);
    } while ( ch != '\n' && ch != EOF );

    if (ch == '\n')
      return (true);
    return (false);
  }

  const uint8_t* os::getData()
  {
    return this->_flatbuffer->GetBufferPointer();
  }

  const uoffset_t os::getSize()
  {
    return this->_flatbuffer->GetSize();
  }

}
}
}
}
