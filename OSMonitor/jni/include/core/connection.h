/**
 * @file Connection.h
 * @brief Connection Class header file
 */

#ifndef CONNECTION_H_
#define CONNECTION_H_

#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include <sys/types.h>
#include <unistd.h>

#include "base.h"
#include "ConnectionInfo.pb.h"

#define CONNECTION_TCP4 "/proc/%d/net/tcp"
#define CONNECTION_TCP6 "/proc/%d/net/tcp6"
#define CONNECTION_UDP4 "/proc/%d/net/udp"
#define CONNECTION_UDP6 "/proc/%d/net/udp6"
#define CONNECTION_RAW4 "/proc/%d/net/raw"
#define CONNECTION_RAW6 "/proc/%d/net/raw6"

#define IPv6PATTERN "%*d: %08X%08X%08X%08X:%x %08X%08X%08X%08X:%x %x %*x:%*x %*x:%*x %*x %d"
#define IPv4PATTERN " %*d: %x:%x %x:%x %x %*x:%*x %*x:%*x %*x %d"

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  /**
   * @class connection
   * @brief get connections information
   */
  class connection : com::eolwral::osmonitor::core::base
  {
  private:
    const static int BufferSize = 256;                  /**< internal buffer size */
    std::vector<connectionInfo*> _curConnectionList;    /**< current connections list */


    /**
     * gathering IPv4 connections
     */
    void gatheringIPv4Connection();

    /**
     * process IPv4 connections file
     * @param fileName filename
     * @param type process connection's type
     */
    void processIPv4Connection(char* fileName, connectionInfo::connectionType type);

    /**
     * add a IPv4 connection into list
     * @param type connection type
     * @param rawLocalAddr raw local address (in_addr)
     * @param localPort local port
     * @param rawRemoteAddr raw remote address (in_addr)
     * @param remotePort remote port
     * @param rawStatus raw status (unsigned int)
     * @param rawUID raw UID (unsigned int)
     */
    void addConnvectionIPv4(connectionInfo::connectionType type,
                            struct in_addr& rawLocalAddr, unsigned int localPort,
                            struct in_addr& rawRemoteAddr, unsigned int remotePort,
                            unsigned int rawStatus, unsigned int rawUID);

    /**
     * gathering IPv6 connections
     */
    void gatheringIPv6Connection();

    /**
     * process IPv6 connections file
     * @param fileName filename
     * @param type process connection's type
     */
    void processIPv6Connection(char* fileName, connectionInfo::connectionType type);

    /**
     * add a IPv6 connection into list
     * @param type connection type
     * @param rawLocalAddr raw local address (in_addr6)
     * @param localPort local port
     * @param rawRemoteAddr raw remote address (in_addr6)
     * @param remotePort remote port
     * @param rawStatus raw status (unsigned int)
     * @param rawUID raw UID (unsigned int)
     */
    void addConnvectionIPv6(connectionInfo::connectionType type,
                            struct in6_addr& rawLocalAddr, unsigned int localPort,
                            struct in6_addr& rawRemoteAddr, unsigned int remotePort,
                            unsigned int rawStatus, unsigned int rawUID);

    /**
     * convert status code from int to enum
     * @param rawStatus
     * @return enum
     */
    connectionInfo::connectionStatus convertStatus(unsigned int rawStatus);

  public:

    /**
     * destructor for Connection
     */
    ~connection();

    /**
     * refresh connections information
     */
    void refresh();

    /**
     * get connections information
     * @return a vector contains all connections
     */
    const std::vector<google::protobuf::Message*>& getData();

  };

}
}
}
}

#endif /* CONNECTION_H_ */
