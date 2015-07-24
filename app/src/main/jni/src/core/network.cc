/**
 * @file network.cc
 * @brief Network Class file
 */

#include "network.h"

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  network::network()
  {
    this->_curFlatBuffer = NULL;
    this->_preFlatBuffer = NULL;
  }

  network::~network()
  {
    // clean up
    if (this->_curFlatBuffer != NULL)
      delete this->_curFlatBuffer;

    if (this->_preFlatBuffer != NULL)
      delete this->_preFlatBuffer;
  }

  void network::prepareBuffer ()
  {
    // clean up
    if (this->_preFlatBuffer != NULL)
      delete this->_preFlatBuffer;

    // move current to previous
    this->_preFlatBuffer = this->_curFlatBuffer;
    this->_curFlatBuffer = new FlatBufferBuilder ();
    this->_list.clear ();
  }

  void network::finishBuffer()
  {
    // create a networkInfoList
    auto mloc = CreatenetworkInfoList(*this->_curFlatBuffer, this->_curFlatBuffer->CreateVector (this->_list));
    FinishnetworkInfoListBuffer (*this->_curFlatBuffer, mloc);
  }

  void network::refresh()
  {
    // clean up
    this->prepareBuffer();

    // get base information
    this->getInterfaceStatistic();

    // create a networkInfoList
    this->finishBuffer();

    return;
  }

  std::tuple<unsigned long, unsigned long> network::calculateNetworkIO( char* ifName,
                                                 unsigned long recvBytes, unsigned long transBytes)
  {
    // ready to calculate
    if(this->_preFlatBuffer == NULL)
      return std::make_tuple(0, 0);

    unsigned long recvUsage = 0;
    unsigned long transUsage = 0;
    const networkInfoList *prevNetworkList = GetnetworkInfoList(this->_preFlatBuffer->GetBufferPointer());
    for(int prevItem = 0; prevItem < prevNetworkList->list()->Length(); prevItem++)
    {

      const networkInfo *prevInterface = prevNetworkList->list()->Get(prevItem);
      if (strcmp(prevInterface->name()->c_str(), ifName) != 0)
        continue;

      recvUsage = recvBytes - prevInterface->recvBytes();
      transUsage = transBytes - prevInterface->transBytes();
      break;
    }
    return std::make_tuple(recvUsage, transUsage);
  }

  void network::getInterfaceStatistic()
  {
    char buffer[BufferSize];

    memset(buffer, 0 , BufferSize);
    snprintf(buffer, BufferSize, INT_IPV4_FILE, getpid());
    FILE *ifFile = fopen(buffer, "r");

    if(ifFile == 0) return;

    // skip 2 lines
    fgets(buffer, BufferSize, ifFile);
    fgets(buffer, BufferSize, ifFile);

    while(fgets(buffer, BufferSize, ifFile) != NULL)
    {
      char curName[BufferSize];
      unsigned long recvBytes = 0;
      unsigned long recvUsage = 0;
      unsigned long recvPackages = 0;
      unsigned long recvErrorBytes = 0;
      unsigned long recvDropBytes = 0;
      unsigned long recvFIFOBytes = 0;
      unsigned long recvFrames = 0;
      unsigned long recvCompressedBytes = 0;
      unsigned long recvMultiCastBytes = 0;
      unsigned long transBytes = 0;
      unsigned long transUsage = 0;
      unsigned long transPackages = 0;
      unsigned long transErrorBytes = 0;
      unsigned long transDropBytes = 0;
      unsigned long transFIFOBytes = 0;
      unsigned int collisionTimes = 0;
      unsigned int carrierErrors = 0;
      unsigned long transCompressedBytes = 0;
      Offset<String> macAddress = 0;
      Offset<String> ipV4 = 0;
      Offset<String> netMaskV4 = 0;
      Offset<String> ipV6 = 0;
      int netMaskV6 = 0;
      short flags = 0;

      memset(curName, 0, BufferSize);

      int matchCounts = sscanf(buffer,
                               INT_IPV4_PATTERN,
                               curName,
                               &recvBytes,
                               &recvPackages,
                               &recvErrorBytes,
                               &recvDropBytes,
                               &recvFIFOBytes,
                               &recvFrames,
                               &recvCompressedBytes,
                               &recvMultiCastBytes,
                               &transBytes,
                               &transPackages,
                               &transErrorBytes,
                               &transDropBytes,
                               &transFIFOBytes,
                               &collisionTimes,
                               &carrierErrors,
                               &transCompressedBytes);

      if(matchCounts < 16) continue;

       // get receive traffic information
      if (recvBytes == 0)
        recvBytes = this->getTrafficRecvInformation(curName);

      // get transmit traffic information
      if (transBytes == 0)
        transBytes = this->getTrafficTransInformation(curName);

      // get MAC address
      macAddress = this->getMACInformation(curName);

      // get IPV4 address
      std::tie(ipV4, netMaskV4, flags) = this->getIPv4Information(curName);

      // get IPv6 address
      std::tie(ipV6, netMaskV6) = this->getIPv6Information(curName);

      // calculate IO utilization
      std::tie(recvUsage, transUsage) = this->calculateNetworkIO(curName, recvBytes, transBytes);

      // gathering basic information
       networkInfoBuilder networkInfo(*this->_curFlatBuffer);
       networkInfo.add_name(networkInfo.fbb_.CreateString(curName));
       networkInfo.add_recvPackages(recvPackages);
       networkInfo.add_recvErrorBytes(recvErrorBytes);
       networkInfo.add_recvDropBytes(recvDropBytes);
       networkInfo.add_recvFIFOBytes(recvFIFOBytes);
       networkInfo.add_recvFrames(recvFrames);
       networkInfo.add_recvCompressedBytes(recvCompressedBytes);
       networkInfo.add_recvMultiCastBytes(recvMultiCastBytes);
       networkInfo.add_transPackages(transPackages);
       networkInfo.add_transErrorBytes(transErrorBytes);
       networkInfo.add_transDropBytes(transDropBytes);
       networkInfo.add_transFIFOBytes(transFIFOBytes);
       networkInfo.add_collisionTimes(collisionTimes);
       networkInfo.add_carrierErros(carrierErrors);
       networkInfo.add_transCompressedBytes(transCompressedBytes);
       networkInfo.add_transUsage(0);
       networkInfo.add_recvUsage(0);

       networkInfo.add_recvBytes(recvBytes);
       networkInfo.add_transBytes(transBytes);

       networkInfo.add_mac(macAddress);

       networkInfo.add_ipv4Addr(ipV4);
       networkInfo.add_netMaskv4(netMaskV4);

       networkInfo.add_ipv6Addr(ipV6);
       networkInfo.add_netMaskv6(netMaskV6);

       networkInfo.add_flags(flags);

       networkInfo.add_recvUsage(recvUsage);
       networkInfo.add_transUsage(transUsage);

       this->_list.push_back(networkInfo.Finish());
    }

    fclose(ifFile);

  }

  Offset<String> network::getMACInformation(char* ifName)
  {
    char buffer[BufferSize];
    char curMACAddr[BufferSize];
    int curMACLen = 0;
    int curMAC = 0;

    memset(buffer, 0, BufferSize);
    memset(curMACAddr, 0, BufferSize);

    snprintf(buffer, BufferSize, INT_MAC_FILE, ifName);
    curMAC = open(buffer, O_RDONLY);
    if(curMAC != -1)
    {
      char readBuffer[BufferSize];
      memset(readBuffer, 0, BufferSize);
      curMACLen = read(curMAC, readBuffer, 17);
      strncpy(curMACAddr, readBuffer, 17);
      close(curMAC);
    }

    // if MAC is invalidated, set it as null
    if (curMACLen <= 16)
      memset(curMACAddr, 0, BufferSize);

    return this->_curFlatBuffer->CreateString(curMACAddr);
  }

  std::tuple<Offset<String>, int> network::getIPv6Information(char* ifName)
  {
    char buffer[BufferSize];
    memset(buffer, 0, BufferSize);
    snprintf(buffer, BufferSize, INT_IPV6_FILE, getpid());

    FILE *ifFile = fopen(buffer, "r");
    if(ifFile == 0)
      return std::make_tuple(0, 0);

    //00000000000000000000000000000001 01 80 10 80       lo
    Offset<String> ipV6Addr = 0;
    int netmaskV6 =0;
    while(fgets(buffer, BufferSize, ifFile) != NULL)
    {
      int curNetmaskV6;
      struct in6_addr curIPv6;
      char curName[BufferSize];

      memset(&curIPv6, 0, sizeof(in6_addr));
      memset(curName, 0, BufferSize);

      int matchCounts = sscanf(buffer, INT_IPV6_PATTERN,
                               (unsigned int*) &curIPv6.in6_u.u6_addr8[0], (unsigned int*) &curIPv6.in6_u.u6_addr8[1],
                               (unsigned int*) &curIPv6.in6_u.u6_addr8[2], (unsigned int*) &curIPv6.in6_u.u6_addr8[3],
                               (unsigned int*) &curIPv6.in6_u.u6_addr8[4], (unsigned int*) &curIPv6.in6_u.u6_addr8[5],
                               (unsigned int*) &curIPv6.in6_u.u6_addr8[6], (unsigned int*) &curIPv6.in6_u.u6_addr8[7],
                               (unsigned int*) &curIPv6.in6_u.u6_addr8[8], (unsigned int*) &curIPv6.in6_u.u6_addr8[9],
                               (unsigned int*) &curIPv6.in6_u.u6_addr8[10], (unsigned int*) &curIPv6.in6_u.u6_addr8[11],
                               (unsigned int*) &curIPv6.in6_u.u6_addr8[12], (unsigned int*) &curIPv6.in6_u.u6_addr8[13],
                               (unsigned int*) &curIPv6.in6_u.u6_addr8[14], (unsigned int*) &curIPv6.in6_u.u6_addr8[15],
                               &curNetmaskV6, curName );

      if(matchCounts == 18 && strcmp(ifName, curName) == 0)
      {
        char addrV6[INET6_ADDRSTRLEN];
        memset(addrV6, 0, INET6_ADDRSTRLEN);
        inet_ntop(AF_INET6, &curIPv6, addrV6, INET6_ADDRSTRLEN);
        ipV6Addr = this->_curFlatBuffer->CreateString(addrV6);
        netmaskV6 = curNetmaskV6;
        break;
      }

    }
    fclose(ifFile);

    return std::make_tuple(ipV6Addr, netmaskV6);
  }

  std::tuple<Offset<String>, Offset<String>, short> network::getIPv4Information(char* ifName)
  {
    char curIPv4[INET_ADDRSTRLEN];
    char curNetMaskv4[INET_ADDRSTRLEN];
    struct ifreq curIFREQ;
    int curSocket = 0;

    Offset<String> ipV4Addr = 0;
    Offset<String> netmaskV4 = 0;
    short flags = 0;

    memset(curIPv4, 0, INET_ADDRSTRLEN);
    memset(curNetMaskv4, 0, INET_ADDRSTRLEN);
    memset(&curIFREQ, 0, sizeof(struct ifreq));
    strncpy(curIFREQ.ifr_name, ifName, IFNAMSIZ-1);

    if((curSocket = socket(AF_INET, SOCK_DGRAM, 0)) >= 0)
    {
      if (ioctl(curSocket, SIOCGIFADDR, &curIFREQ) >= 0)
      {
        inet_ntop(AF_INET, &((struct sockaddr_in *) &curIFREQ.ifr_addr)->sin_addr.s_addr,
                    curIPv4, INET_ADDRSTRLEN);
        ipV4Addr = this->_curFlatBuffer->CreateString(curIPv4);
      }

      if (ioctl(curSocket, SIOCGIFNETMASK, &curIFREQ) >= 0)
      {
        inet_ntop(AF_INET, &((struct sockaddr_in *) &curIFREQ.ifr_addr)->sin_addr.s_addr,
                              curNetMaskv4, INET_ADDRSTRLEN);
        netmaskV4 = this->_curFlatBuffer->CreateString(curNetMaskv4);
      }

      if (ioctl(curSocket, SIOCGIFFLAGS, &curIFREQ) >= 0)
        flags = curIFREQ.ifr_flags;

      close(curSocket);
    }

    return std::make_tuple(ipV4Addr, netmaskV4, flags);
  }

  unsigned long network::getTrafficRecvInformation(char* ifName)
  {
    char buffer[BufferSize];
    char curTrafficData[BufferSize];
    int curTafficDataLen = 0;
    int curTraffic = 0;

    memset(buffer, 0, BufferSize);
    memset(curTrafficData, 0, BufferSize);

    snprintf(buffer, BufferSize, INT_RX_FILE, ifName);
    if((curTraffic = open(buffer, O_RDONLY)) != -1)
    {
      curTafficDataLen = read(curTraffic, curTrafficData, BufferSize);
      close(curTraffic);
    }
    if (curTafficDataLen > 0)
      return strtoul(curTrafficData, NULL, 0);
    return 0;
  }

  unsigned long network::getTrafficTransInformation(char* ifName)
  {
    char buffer[BufferSize];
    char curTrafficData[BufferSize];
    int curTafficDataLen = 0;
    int curTraffic = 0;

    memset(buffer, 0, BufferSize);
    memset(curTrafficData, 0, BufferSize);

    snprintf(buffer, BufferSize, INT_TX_FILE, ifName);
    if( (curTraffic = open(buffer, O_RDONLY)) != -1 )
    {
      curTafficDataLen = read(curTraffic, curTrafficData, BufferSize);
      close(curTraffic);
    }
    if (curTafficDataLen > 0)
      return strtoul(curTrafficData, NULL, 0);
    return 0;
  }

  const uint8_t* network::getData()
  {
    return this->_curFlatBuffer->GetBufferPointer();
  }

  const uoffset_t network::getSize()
  {
    return this->_curFlatBuffer->GetSize();
  }

}
}
}
}
