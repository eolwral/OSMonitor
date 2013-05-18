/**
 * @file network.h
 * @brief Network Class file
 */

#ifndef NETWORK_H_
#define NETWORK_H_

#include <linux/if.h>
#include <linux/sockios.h>
#include <linux/param.h>
#include <linux/wireless.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "base.h"
#include "networkInfo.pb.h"

#define INT_IPV4_FILE "/proc/%d/net/dev"
#define INT_IPV4_PATTERN " %[^:]:%u %u %u %u %u %u %u %u %u %u %u %u %u %u %u"

#define INT_MAC_FILE "/sys/class/net/%s/address"

#define INT_IPV6_FILE "/proc/%d/net/if_inet6"
#define INT_IPV6_PATTERN "%8X%8X%8X%8X %*x %x %*x %*x %16s"

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  /**
   * @class network
   * @brief get network interface information
   */
  class network : com::eolwral::osmonitor::core::base
  {
  private:
    const static int BufferSize = 256;          /**< internal buffer size */
    std::vector<networkInfo*> _curNetworkList;  /**< internal network list */

    /**
     * get interface information
     */
    void getInterfaceStatistic();

    /**
     * get IPv6 information
     */
    void getIPv6Information();

    /**
     * get IPv4 information
     * @param _curNetworkInfo target interface
     */
    void getIPv4Information(networkInfo* curNetworkInfo);

    /**
     * get MAC information
     * @param curNetworkInfo target interface
     */
    void getMACInformation(networkInfo* curNetworkInfo);

  public:

    /**
     * destructor for Network
     */
    ~network();

    /**
     * refresh network information
     */
    void refresh();

    /**
     * get network interface list
     * @return a vector contains all network information
     */
    const std::vector<google::protobuf::Message*>& getData();

  };
}
}
}
}


#endif /* NETWORK_H_ */
