/**
 * @file CPU.h
 * @brief CPU Class file
 */

#ifndef CPU_H_
#define CPU_H_

#include "base.h"
#include "cpuInfo.pb.h"

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

    int _totalCPUCount;                 /**< total CPUs count */
    unsigned long _totalCPUTime;        /**< passed time since last time */
    float _totalCPUUtilization;         /**< total CPUs utilization */

    cpuInfo *_prevTotalCPU;             /**< previous total CPUs information */
    cpuInfo *_currentTotalCPU;          /**< previous total CPUs information */
    std::vector<cpuInfo*> _prevCPUStatus;    /**< previous CPUs information */
    std::vector<cpuInfo*> _currentCPUStatus; /**< current CPUs information */

    static const unsigned PATTERNSIZE = 64;        /**< size of pattern string */

    /**
     * dump information for activity CPU
     * @param curCPUInfo CPUInfo object want to be filled
     * @param curCPUNum which CPU number want to read\n
     *        -1 == whole system's CPU usage\n
     *        0~n == specific CPU number
     * @return success or fail
     */
    bool fillCPUInfo(cpuInfo& curCPUInfo, int curCPUNum);

    /**
     * gather all information into a CPUStatus vector
     * @param curCPUStatus
     */
    void gatherCPUsInfo(std::vector<cpuInfo*>& curCPUStatus);

    /**
     * calculate CPUs utilization
     * @param prevCPUInfo previous status
     * @param curCPUInfo current status
     */
    void calcuateCPUUtil(cpuInfo& prevCPUInfo, cpuInfo& curCPUInfo);

    /**
     * processing Off-line CPUs
     */
    void processOfflineCPUs();

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
     * @return a vector contains all CPUs information
     */
    const std::vector<google::protobuf::Message*>& getData();

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
          void
          refreshGlobal();
        };

}
}
}
}

#endif /* CPU_H_ */
