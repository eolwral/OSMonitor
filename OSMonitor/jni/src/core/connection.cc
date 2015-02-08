/**
 * @file Connection.cpp
 * @brief Connection Class file
 */

#include <connection.h>

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  connection::connection()
  {
    this->_flatbuffer = NULL;
  }

  connection::~connection()
  {
    // clean up _current
    if (this->_flatbuffer != NULL) {
      delete this->_flatbuffer;
      this->_flatbuffer = NULL;
    }
  }

  void connection::prepareBuffer()
  {
    // clean up current connections list
    if (this->_flatbuffer != NULL)
      delete this->_flatbuffer;

    // create a new FlatBufferBuilder and clear up connection list
    this->_flatbuffer = new FlatBufferBuilder ();
    this->_list.clear ();
  }

  void connection::finishBuffer()
  {
    // finish the buffer
    auto mloc = CreateconnectionInfoList (*this->_flatbuffer, this->_flatbuffer->CreateVector (_list));
    FinishconnectionInfoListBuffer (*this->_flatbuffer, mloc);
  }

  void connection::refresh()
  {
    // clean up current connections list
    this->prepareBuffer ();

    // gathering IPv4 Connection
    this->gatheringIPv4Connection();

    // gathering IPv6 Connection
    this->gatheringIPv6Connection();

    // finish the buffer
    this->finishBuffer();

    return;
  }

  void connection::gatheringIPv6Connection()
  {
    char filename[BufferSize];

    // gathering TCP connections
    snprintf(filename, BufferSize, CONNECTION_TCP6, getpid());
    this->processIPv6Connection(filename, connectionType_TCPv6);

    // gathering UDP connections
    snprintf(filename, BufferSize, CONNECTION_UDP6, getpid());
    this->processIPv6Connection(filename, connectionType_UDPv6);

    // gathering RAW connections
    snprintf(filename, BufferSize, CONNECTION_RAW6, getpid());
    this->processIPv6Connection(filename, connectionType_RAWv6);

    return;
  }

  void connection::processIPv6Connection(char* fileName,
                                         connectionType type)
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
                         &remotePort,
			 &connectionStatus, &connectionUid);


        if(matchCount == 12)
          this->addConnvectionIPv6(type, localAddrv6, localPort, remoteAddrv6, remotePort,
                                    connectionStatus, connectionUid);
      }
      fclose(connectFile);
    }
    return;
  }

  void connection::addConnvectionIPv6(connectionType type,
                                      struct in6_addr& rawLocalAddr,
                                      unsigned int localPort,
                                      struct in6_addr& rawRemoteAddr,
                                      unsigned int remotePort,
                                      unsigned int rawStatus,
                                      unsigned int rawUID)
  {
    char addrV6[INET6_ADDRSTRLEN];

    memset(addrV6, 0, INET6_ADDRSTRLEN);
    inet_ntop(AF_INET6, &rawLocalAddr, addrV6, INET6_ADDRSTRLEN);

    Offset<String> localIP = this->_flatbuffer->CreateString(addrV6);

    memset(addrV6, 0, INET6_ADDRSTRLEN);
    inet_ntop(AF_INET6, &rawRemoteAddr, addrV6, INET6_ADDRSTRLEN);

    Offset<String> remoteIP = this->_flatbuffer->CreateString(addrV6);

    // save type and uid
    connectionInfoBuilder connectionInfo(*this->_flatbuffer);
    connectionInfo.add_type(type);
    connectionInfo.add_uid(rawUID);

    // save local information
    connectionInfo.add_localIP(localIP);
    connectionInfo.add_localPort(localPort);

    // save remote information
    connectionInfo.add_remoteIP(remoteIP);
    connectionInfo.add_remotePort(remotePort);

    // although status code is 0x7, but UDP is not "CLOSED"
    if (type == connectionType_UDPv6 && rawStatus == connectionStatus_CLOSE)
      connectionInfo.add_status(connectionStatus_LISTEN);
    else
      connectionInfo.add_status(convertStatus(rawStatus));

    this->_list.push_back(connectionInfo.Finish());

    return;
  }

  void connection::gatheringIPv4Connection()
  {
    char filename[BufferSize];

    // gathering TCP connections
    snprintf(filename, BufferSize, CONNECTION_TCP4, getpid());
    this->processIPv4Connection(filename, connectionType_TCPv4);

    // gathering UDP connections
    snprintf(filename, BufferSize, CONNECTION_UDP4, getpid());
    this->processIPv4Connection(filename, connectionType_UDPv4);

    // gathering RAW connections
    snprintf(filename, BufferSize, CONNECTION_RAW4, getpid());
    this->processIPv4Connection(filename, connectionType_RAWv4);

    return;
  }

  void connection::processIPv4Connection(char* fileName,
                                         connectionType type)
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

  void connection::addConnvectionIPv4(connectionType type,
                                      struct in_addr& rawLocalAddr,
                                      unsigned int localPort,
                                      struct in_addr& rawRemoteAddr,
                                      unsigned int remotePort,
                                      unsigned int rawStatus,
                                      unsigned int rawUID)
  {

    char addrV4[INET_ADDRSTRLEN];
    memset(addrV4, 0, INET_ADDRSTRLEN);
    inet_ntop(AF_INET, &rawLocalAddr, addrV4, INET_ADDRSTRLEN);

    Offset<String> localIP = this->_flatbuffer->CreateString(addrV4);

    memset(addrV4, 0, INET_ADDRSTRLEN);
    inet_ntop(AF_INET, &rawRemoteAddr, addrV4 ,INET_ADDRSTRLEN);

    Offset<String> remoteIP = this->_flatbuffer->CreateString(addrV4);

    // save type and uid
    connectionInfoBuilder connectionInfo(*this->_flatbuffer);
    connectionInfo.add_type(type);
    connectionInfo.add_uid(rawUID);

    // save local information
    connectionInfo.add_localIP(localIP);
    connectionInfo.add_localPort(localPort);

    // save remote information
    memset(addrV4, 0, INET_ADDRSTRLEN);
    inet_ntop(AF_INET, &rawRemoteAddr, addrV4 ,INET_ADDRSTRLEN);

    connectionInfo.add_remoteIP(remoteIP);
    connectionInfo.add_remotePort(remotePort);

    // although status code is 0x7, but UDP is not "CLOSED"
    if (type == connectionType_UDPv4 && rawStatus == connectionStatus_CLOSE)
      connectionInfo.add_status(connectionStatus_LISTEN);
    else
      connectionInfo.add_status(convertStatus(rawStatus));

    this->_list.push_back(connectionInfo.Finish());

    return;
  }

  connectionStatus connection::convertStatus(unsigned int rawStatus)
  {
    switch(rawStatus)
    {
    case 1:
      return (connectionStatus_ESTABLISHED);
    case 2:
      return (connectionStatus_SYN_SENT);
    case 3:
      return (connectionStatus_SYN_RECV);
    case 4:
      return (connectionStatus_FIN_WAIT1);
    case 5:
      return (connectionStatus_FIN_WAIT2);
    case 6:
      return (connectionStatus_TIME_WAIT);
    case 7:
      return (connectionStatus_CLOSE);
    case 8:
      return (connectionStatus_CLOSE_WAIT);
    case 9:
      return (connectionStatus_LAST_ACK);
    case 10:
      return (connectionStatus_LISTEN);
    case 11:
      return (connectionStatus_CLOSING);
    }
    return (connectionStatus_UNKNOWN);
  }

  const uint8_t* connection::getData()
  {
    return this->_flatbuffer->GetBufferPointer();
  }

  const uoffset_t connection::getSize()
  {
    return this->_flatbuffer->GetSize();
  }

}
}
}
}
