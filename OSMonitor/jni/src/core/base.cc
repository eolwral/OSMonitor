/**
 * @file base.cc
 * @brief DataSet Class file
 */

#include "base.h"

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  void base::clearDataSet(std::vector<google::protobuf::Message*>& curDataSet)
  {
    // remove all data from vector and delete it carefully
    while(curDataSet.empty() != true)
    {
      google::protobuf::Message* curObject = curDataSet.back();
      delete curObject;
      curObject = 0;
      curDataSet.pop_back();
    }
    return;
  }

  void base::moveDataSet(std::vector<google::protobuf::Message*>& srcDataSet,
                         std::vector<google::protobuf::Message*>& dstDataSet)
  {
    while(srcDataSet.empty() != true)
    {
      dstDataSet.push_back(srcDataSet.back());
      srcDataSet.pop_back();
    }
    return;
  }


}
}
}
}
