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

#include <tuple>

#include "base.h"
#include "networkInfo_generated.h"

#define INT_IPV4_FILE "/proc/%d/net/dev"
#define INT_IPV4_PATTERN " %[^:]: %lu %lu %lu %lu %lu %lu %lu %lu %lu %lu %lu %lu %lu %u %u %lu"

#define INT_MAC_FILE "/sys/class/net/%s/address"
#define INT_RX_FILE "sys/class/net/%s/statistics/rx_bytes"
#define INT_TX_FILE "sys/class/net/%s/statistics/tx_bytes"

#define INT_IPV6_FILE "/proc/%d/net/if_inet6"
#define INT_IPV6_PATTERN "%2X%2X%2X%2X%2X%2X%2X%2X%2X%2X%2X%2X%2X%2X%2X%2X %*x %x %*x %*x %16s"

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
    const static int BufferSize = 256;           /**< internal buffer size */
    FlatBufferBuilder* _curFlatBuffer;           /**< current flatbuffer */
    FlatBufferBuilder* _preFlatBuffer;           /**< previous flatbuffer */

    std::vector<Offset<networkInfo>> _list;      /**< current network list */

    /**
     * get interface information
     */
    void getInterfaceStatistic();

    /**
     * get IPv6 information
     * @param[in] ifName interface name
     * @param[in] networkInfo target interface
     */
    std::tuple<Offset<String>, int> getIPv6Information(char* ifName);

    /**
     * calculate Network IO information
     * @param[in] ifName interface name
     * @param[in] targetNetworkInfo target interface
     * @param[in] recvBytes receive bytes
     * @param[in] transBytes transmit bytes
     */
    std::tuple<unsigned long, unsigned long> calculateNetworkIO( char* ifName,
                              unsigned long recvBytes, unsigned long transBytes);

    /**
     * get IPv4 information
     * @param[in] ifName interface name
     * @param[in] networkInfo target interface
     */
    std::tuple<Offset<String>, Offset<String>, short> getIPv4Information(char* ifName);

    /**
     * get MAC information
     * @param[in] ifName interface name
     * @return flatbuffer string
     */
    Offset<String> getMACInformation(char* ifName);

    /**
     * get receive traffic information
     * @param[in] ifName interface name
     * @param[in] networkInfo target interface
     * @return receive bytes
     */
    unsigned long getTrafficRecvInformation(char* ifName);

    /**
     * get transmit traffic information
     * @param[in] ifName interface name
     * @param[in] networkInfo target interface
     * @return transmit bytes
     */
    unsigned long getTrafficTransInformation(char* ifName);

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
     * constructor for Network
     */
    network();

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


#endif /* NETWORK_H_ */
