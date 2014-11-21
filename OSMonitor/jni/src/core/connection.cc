/**
 * @file Connection.cpp
 * @brief Connection Class file
 */

#include <connection.h>

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  connection::~connection()
  {
    // clean up _lastCPUStatus
    this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_curConnectionList);
  }

  void connection::refresh()
  {
    // clean up current connections list
    this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_curConnectionList);

    // gathering IPv4 Connection
    this->gatheringIPv4Connection();

    // gathering IPv6 Connection
    this->gatheringIPv6Connection();

    return;
  }

  void connection::gatheringIPv6Connection()
  {
    char filename[BufferSize];

    // gathering TCP connections
    snprintf(filename, BufferSize, CONNECTION_TCP6, getpid());
    this->processIPv6Connection(filename, connectionInfo::TCPv6);

    // gathering UDP connections
    snprintf(filename, BufferSize, CONNECTION_UDP6, getpid());
    this->processIPv6Connection(filename, connectionInfo::UDPv6);

    // gathering RAW connections
    snprintf(filename, BufferSize, CONNECTION_RAW6, getpid());
    this->processIPv6Connection(filename, connectionInfo::RAWv6);

    return;
  }

  void connection::processIPv6Connection(char* fileName,
                                         connectionInfo::connectionType type)
  {
    struct in6_addr localAddrv6;
    struct in6_addr remoteAddrv6;
    unsigned int localPort = 0;
    unsigned int remotePort = 0;
    unsigned int connectionStatus = 0;
    unsigned int connectionUid = 0;

    FILE *connectFile = 0;
    char buffer[BufferSize];

    connectFile = fopen(fileName, "r");
    if(connectFile != 0)
    {
      memset(buffer, 0, BufferSize);
      fgets(buffer, BufferSize, connectFile);
      while(fgets(buffer, BufferSize, connectFile))
      {
        int matchCount = sscanf(buffer, IPv6PATTERN,
                         &localAddrv6.in6_u.u6_addr32[0], &localAddrv6.in6_u.u6_addr32[1],
                         &localAddrv6.in6_u.u6_addr32[2], &localAddrv6.in6_u.u6_addr32[3],
                         &localPort,
                         &remoteAddrv6.in6_u.u6_addr32[0], &remoteAddrv6.in6_u.u6_addr32[1],
                         &remoteAddrv6.in6_u.u6_addr32[2], &remoteAddrv6.in6_u.u6_addr32[3],
                         &remotePort, &connectionStatus, &connectionUid);


        if(matchCount == 12)
          this->addConnvectionIPv6(type, localAddrv6, localPort, remoteAddrv6, remotePort,
                                    connectionStatus, connectionUid);
      }
      fclose(connectFile);
    }
    return;
  }

  void connection::addConnvectionIPv6(connectionInfo::connectionType type,
                                      struct in6_addr& rawLocalAddr,
                                      unsigned int localPort,
                                      struct in6_addr& rawRemoteAddr,
                                      unsigned int remotePort,
                                      unsigned int rawStatus,
                                      unsigned int rawUID)
  {
    char addrV6[INET6_ADDRSTRLEN];

    // save type and uid
    connectionInfo* curConnectionInfo = new connectionInfo();
    curConnectionInfo->set_type(type);
    curConnectionInfo->set_uid(rawUID);

    // save local information
    memset(addrV6, 0, INET6_ADDRSTRLEN);
    inet_ntop(AF_INET6, &rawLocalAddr, addrV6, INET6_ADDRSTRLEN);

    curConnectionInfo->set_localip(addrV6);
    curConnectionInfo->set_localport(localPort);

    // save remote information
    memset(addrV6, 0, INET6_ADDRSTRLEN);
    inet_ntop(AF_INET6, &rawRemoteAddr, addrV6, INET6_ADDRSTRLEN);

    curConnectionInfo->set_remoteip(addrV6);
    curConnectionInfo->set_remoteport(remotePort);

    // we could directly assign it, because enumerate value equals status code
    curConnectionInfo->set_status(convertStatus(rawStatus));

    this->_curConnectionList.push_back(curConnectionInfo);

    return;
  }

  void connection::gatheringIPv4Connection()
  {
    char filename[BufferSize];

    // gathering TCP connections
    snprintf(filename, BufferSize, CONNECTION_TCP4, getpid());
    this->processIPv4Connection(filename, connectionInfo::TCPv4);

    // gathering UDP connections
    snprintf(filename, BufferSize, CONNECTION_UDP4, getpid());
    this->processIPv4Connection(filename, connectionInfo::UDPv4);

    // gathering RAW connections
    snprintf(filename, BufferSize, CONNECTION_RAW4, getpid());
    this->processIPv4Connection(filename, connectionInfo::RAWv4);

    return;
  }

  void connection::processIPv4Connection(char* fileName,
                                         connectionInfo::connectionType type)
  {
    struct in_addr localAddrv4;
    struct in_addr remoteAddrv4;
    unsigned int localPort = 0;
    unsigned int remotePort = 0;
    unsigned int connectionStatus = 0;
    unsigned int connectionUid = 0;

    FILE *connectFile = 0;
    char buffer[BufferSize];

    connectFile = fopen(fileName, "r");
    if(connectFile != 0)
    {
      memset(buffer, 0, BufferSize);
      fgets(buffer, BufferSize, connectFile);
      while(fgets(buffer, BufferSize, connectFile))
      {
        int matchCount = sscanf(buffer, IPv4PATTERN,
                                &localAddrv4.s_addr, &localPort,
                                &remoteAddrv4.s_addr, &remotePort,
                                &connectionStatus, &connectionUid);

        if(matchCount == 6)
          this->addConnvectionIPv4(type, localAddrv4, localPort, remoteAddrv4, remotePort,
                                    connectionStatus, connectionUid);
      }
      fclose(connectFile);
    }
    return;
  }

  void connection::addConnvectionIPv4(connectionInfo::connectionType type,
                                      struct in_addr& rawLocalAddr,
                                      unsigned int localPort,
                                      struct in_addr& rawRemoteAddr,
                                      unsigned int remotePort,
                                      unsigned int rawStatus,
                                      unsigned int rawUID)
  {
    char addrV4[INET_ADDRSTRLEN];

    // save type and uid
    connectionInfo* curConnectionInfo = new connectionInfo();
    curConnectionInfo->set_type(type);
    curConnectionInfo->set_uid(rawUID);

    // save local information
    memset(addrV4, 0, INET_ADDRSTRLEN);
    inet_ntop(AF_INET, &rawLocalAddr, addrV4, INET_ADDRSTRLEN);

    curConnectionInfo->set_localip(addrV4);
    curConnectionInfo->set_localport(localPort);

    // save remote information
    memset(addrV4, 0, INET_ADDRSTRLEN);
    inet_ntop(AF_INET, &rawRemoteAddr, addrV4 ,INET_ADDRSTRLEN);

    curConnectionInfo->set_remoteip(addrV4);
    curConnectionInfo->set_remoteport(remotePort);

    // we could directly assign it, because enumerate value equals status code
    curConnectionInfo->set_status(convertStatus(rawStatus));

    this->_curConnectionList.push_back(curConnectionInfo);

    return;
  }

  connectionInfo::connectionStatus connection::convertStatus(unsigned int rawStatus)
  {
    switch(rawStatus)
    {
    case 1:
      return (connectionInfo_connectionStatus_ESTABLISHED);
    case 2:
      return (connectionInfo_connectionStatus_SYN_SENT);
    case 3:
      return (connectionInfo_connectionStatus_SYN_RECV);
    case 4:
      return (connectionInfo_connectionStatus_FIN_WAIT1);
    case 5:
      return (connectionInfo_connectionStatus_FIN_WAIT2);
    case 6:
      return (connectionInfo_connectionStatus_TIME_WAIT);
    case 7:
      return (connectionInfo_connectionStatus_CLOSE);
    case 8:
      return (connectionInfo_connectionStatus_CLOSE_WAIT);
    case 9:
      return (connectionInfo_connectionStatus_LAST_ACK);
    case 10:
      return (connectionInfo_connectionStatus_LISTEN);
    case 11:
      return (connectionInfo_connectionStatus_CLOSING);
    }
    return (connectionInfo_connectionStatus_UNKNOWN);
  }

  const std::vector<google::protobuf::Message*>& connection::getData()
  {
    return ((const std::vector<google::protobuf::Message*>&) this->_curConnectionList);
  }
}
}
}
}
