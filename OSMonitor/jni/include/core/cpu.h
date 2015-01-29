/**
 * @file CPU.h
 * @brief CPU Class file
 */

#ifndef CPU_H_
#define CPU_H_

#include "base.h"
#include "cpuInfo_generated.h"

#define SYS_PROC_FILE "/proc/stat"

#define CPU_GLOBAL_PATTERN "cpu  %lu %lu %lu %lu %lu %lu %lu "
#define CPU_SIGNLE_PATTERN "cpu%d %%lu %%lu %%lu %%lu %%lu %%lu %%lu"


namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  /**
   * @class cpu
   * @brief get information from CPUs
   */
  class cpu : com::eolwral::osmonitor::core::base
  {
  private:

    float _totalCPUUtilization;              /**< total CPUs utilization since last time*/
    unsigned long _totalCPUTime;             /**< passed time since last time */
    unsigned long _totalCPUIdleTime;         /**< total CPU Idle Time since last time*/

    unsigned long _prevCPUTime;              /**< passed time since last time */
    unsigned long _prevCPUIdleTime;          /**< total CPU Idle Time */

    int _totalCPUCount;                      /**< total CPUs count */
    FlatBufferBuilder *_prevFlatBuffer;      /**< previous CPUs information */
    FlatBufferBuilder *_curFlatBuffer;       /**< current CPUs information */
    std::vector<Offset<cpuInfo>> _list;      /**< current CPUs list */

    static const unsigned PATTERNSIZE = 64;    /**< size of pattern string */

    /**
     * dump information for activity CPU
     * @param[in] cpuInfo    cpuInfo object want to be filled
     * @param[in] cpuNum     which CPU number want to read\n
     *                         0~n == specific CPU number
     * @return success or fail
     */
    bool fillCPUInfo(cpuInfoBuilder& cpuInfo, int cpuNum);

    /**
     * dump information for inactivity CPU
     * @param[in] cpuInfo    cpuInfo object want to be filled
     * @param[in] cpuNum     which CPU number want to read\n
     *                         -1 == whole system's CPU usage\n
     *                         0~n == specific CPU number
     */
    void fillEmptyCPUInfo(cpuInfoBuilder& cpuInfo, int cpuNum);

    /**
     * calculate CPUs utilization
     * @param[in] cpuInfo    cpuInfo object want to be filled
     * @param[in] cpuNum     which CPU number want to read\n
     *                         0~n == specific CPU number
     * @param[in] cpuTime    usage Time
     * @param[in] idleTime   idle Time
     * @param[in] ioWaitTime iowait Time
     */
    void calcuateCPUUtil(cpuInfoBuilder& cpuInfo, int cpuNum,
                         unsigned long cpuTime, unsigned long idleTime, unsigned long ioWaitTime);

    /**
     * gather all CPU information
     */
    void gatherCPUsInfo();

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
     * construct for CPU class
     */
    cpu();

    /**
     * destructor for CPU Class
     */
    ~cpu();

    /**
     * refresh all information
     */
    void refresh();

    /**
     * get current CPUs information
     * @return buffer pointer
     */
    const uint8_t* getData();

    /**
     * get current data size
     * @return size
     */
    const uoffset_t getSize();

    /**
     * get total CPUs utilization
     * @return total CPUs utilization
     */
    float getCPUUtilization();

    /**
     * get total CPUs Time
     * @return total CPUs Time
     */
    unsigned long getCPUTime();

    /**
     * refresh total CPUs information
     */
    void refreshGlobal();

  };

}
}
}
}

#endif /* CPU_H_ */
