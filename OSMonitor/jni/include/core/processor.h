/**
 * @file processor.h
 * @brief Processor Class Header file
 */

#ifndef PROCESSOR_H_
#define PROCESSOR_H_

#include "base.h"
#include "processorInfo.pb.h"

#define PROCESSOR_FREQ_MAX "/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_max_freq"
#define PROCESSOR_FREQ_MIN "/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_min_freq"
#define PROCESSOR_SCALING_CUR "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_cur_freq"
#define PROCESSOR_SCALING_MAX "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_max_freq"
#define PROCESSOR_SCALING_MIN "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_min_freq"
#define PROCESSOR_SCALING_GOR "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_governor"
#define PROCESSOR_AVAILABLE_FREQ "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_available_frequencies"
#define PROCESSOR_AVAILABLE_GOR "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_available_governors"
#define PROCESSOR_STATUS "/sys/devices/system/cpu/cpu%d/online"

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  /**
   * @class processor
   * @brief get processor information
   */
  class processor : com::eolwral::osmonitor::core::base
  {
  private:
    const static int BufferSize = 256;                    /**< internal buffer size */

    int MaximumCPUs;                                      /**< maximum cpu number */
    std::vector<processorInfo*> _curProcessorList;        /**< get current processors list */
    std::vector<processorInfo*> _prevProcessorList;       /**< get previous processors list */


    mode_t getPermission(const char* fileName);

    void resetPermission();

    void gatherProcessor();

    void processOfflineProcessor();

  public:

    /**
     * constructor for Processor
     */
    processor();

    /**
     * destructor for Processor
     */
    ~processor();

    /**
     * refresh processors list
     */
    void refresh();

    /**
     * get processors list
     * @return processors list
     */
    const std::vector<google::protobuf::Message*>& getData();

  };

}
}
}
}


#endif /* PROCESSOR_H_ */
