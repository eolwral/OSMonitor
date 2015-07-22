/**
 * @file processor.h
 * @brief Processor Class Header file
 */

#ifndef PROCESSOR_H_
#define PROCESSOR_H_

// Android
#include <android/log.h>
#define APPNAME "OSMCore"

#include <limits.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>

#include "base.h"
#include "processorInfo_generated.h"

#define PROCESSOR_FREQ_MAX "/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_max_freq"
#define PROCESSOR_FREQ_MIN "/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_min_freq"
#define PROCESSOR_SCALING_CUR "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_cur_freq"
#define PROCESSOR_SCALING_MAX "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_max_freq"
#define PROCESSOR_SCALING_MIN "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_min_freq"
#define PROCESSOR_SCALING_GOR "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_governor"
#define PROCESSOR_AVAILABLE_FREQ "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_available_frequencies"
#define PROCESSOR_AVAILABLE_GOR "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_available_governors"
#define PROCESSOR_STATUS "/sys/devices/system/cpu/cpu%d/online"
#define PROCESSOR_PRESENT "/sys/devices/system/cpu/present"

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
    const static int BufferSize = 256;        /**< internal buffer size */

    int _maximumCPUs;                         /**< maximum CPU number */
    FlatBufferBuilder* _curFlatBuffer;        /**< current flatbuffer */
    FlatBufferBuilder* _preFlatBuffer;        /**< previous flatbuffer */
    std::vector<Offset<processorInfo>> _list; /**< current processor list

    /**
     * reset all relative files permission
     */
    void resetAllPermissions();

    /**
     * gathering all processors information
     */
    void gatherProcessor();

    /**
     * get processor number
     */
    int getProcessorNumber();


    /**
     * check and set permission
     * @param[in] number CPU Number
     * @prarm[in] pattern check file pattern
     * @param[in] mode desired permission
     */
    void resetPermission(int number, const char* pattern, unsigned short mode);

    /**
     * get previous processor inforamtion
     * @param[in] number number
     * @return a point of processorInfo
     */
    const processorInfo* getPrevProcessor(int number);

    /**
     * get string value from file
     * @param[in] number CPU Number
     * @param[in] fileName check file pattern
     * @paran[out] extractString string buffer
     * @param[in] extractLen buffer length
     * @return boolean successful or fail
     */
    bool getProcessorString(int number, const char* fileName, char* extractString, int extractLen);

    /**
     * get integer from file
     * @param[in] number CPU Number
     * @param[in] fileName check file pattern
     * @return integer value
     */
    int getProcessorValue(int number, const char* fileName);

    /**
     * prepare FlatBuffer
     */
    void prepareBuffer();

    /**
     * finish FlatBuffer
     */
    void finishBuffer();

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
     * @return a buffer pointer
     */
    const uint8_t* getData();

    /**
     * get buffer size
     * @return buffer size
     */
    const uoffset_t getSize();

  };

}
}
}
}


#endif /* PROCESSOR_H_ */
