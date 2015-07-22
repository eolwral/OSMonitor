/**
 * @file BaseDataSet.h
 * @brief DataSet Class header file
 */


#ifndef BASE_H_
#define BASE_H_

#include <sys/sysconf.h>
#include <sys/time.h>
#include <sys/stat.h>
#include <time.h>
#include <string.h>
#include <stdio.h>
#include <vector>

#include "flatbuffers/flatbuffers.h"

using namespace flatbuffers;

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  /**
   * @class base
   * @brief base data set object
   */
  class base
  {
  public:

    /**
     * destruct
     */
    virtual ~base() {};

    /**
     * refresh function need to be overwrote by subclass
     */
    virtual void refresh() = 0;

    /**
     * get data
     * @return a buffer pointer
     */
    virtual const uint8_t* getData() = 0;

    /**
     * get data size
     * @return buffer size
     */
    virtual const uoffset_t getSize() = 0;

  };

}
}
}
}

#endif /* BASE_H_ */
