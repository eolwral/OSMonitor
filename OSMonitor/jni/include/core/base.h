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

// google protocol buffer (include declarations for base unit)
#include <src/google/protobuf/message.h>

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
  protected:
    /**
     * clean up target set
     * @param currentSet
     */
    void clearDataSet(std::vector<google::protobuf::Message*>& currentSet);

    /**
     * move data from source set to destination set
     * @param srcDataSet source set
     * @param dstDataSet destination set
     */
    void moveDataSet(std::vector<google::protobuf::Message*>& srcDataSet,
                     std::vector<google::protobuf::Message*>& dstDataSet);

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
     * get data list
     * @return data list
     */
    virtual const std::vector<google::protobuf::Message*>& getData() = 0;

  };

}
}
}
}

#endif /* BASE_H_ */
