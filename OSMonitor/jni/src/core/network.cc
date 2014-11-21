/**
 * @file network.cc
 * @brief Network Class file
 */

#include "network.h"

namespace com {
namespace eolwral {
namespace osmonitor {
namespace core {

  network::~network()
  {
    // clean up
    this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_curNetworkList);
    this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_prevNetworkList);
  }

  void network::refresh()
  {
	// clean up
	this->clearDataSet((std::vector<google::protobuf::Message*>&) this->_prevNetworkList);

    // move current to previous
    this->moveDataSet((std::vector<google::protobuf::Message*>&) this->_curNetworkList,
                   			  (std::vector<google::protobuf::Message*>&) this->_prevNetworkList);

    // get base information
    this->getInterfaceStatistic();

    std::vector<networkInfo*>::iterator curIter = this->_curNetworkList.begin();
    while(curIter != this->_curNetworkList.end())
    {
      // get MAC address
      this->getMACInformation(*curIter);

      // get IPv4 address
      this->getIPv4Information(*curIter);

      // get traffic statistics
      this->getTrafficInformation(*curIter);

      curIter++;
    }

    // get IPv6 address (for performance, we do it once)
    this->getIPv6Information();

    // calculate IO utilization
    this->calculateNetworkIO();

    return;
  }

  void network::calculateNetworkIO()
  {
    // check 2 lists is ready to calculate
     if(this->_curNetworkList.size() == 0  || this->_prevNetworkList.size() == 0)
       return;

     // search for match PID and summary all CPUTime (Remove it for reducing CPU consume)
     for(int curItem=0; curItem < this->_curNetworkList.size(); curItem++)
     {
	 for(int prevItem=0; prevItem < this->_prevNetworkList.size(); prevItem++)
	 {
	   if (strcmp(this->_prevNetworkList[prevItem]->name().c_str(), this->_curNetworkList[curItem]->name().c_str()) != 0)
	     continue;

	   this->_curNetworkList[curItem]->set_recvusage(this->_curNetworkList[curItem]->recvbytes() - this->_prevNetworkList[prevItem]->recvbytes() );
	   this->_curNetworkList[curItem]->set_transusage(this->_curNetworkList[curItem]->transbytes() - this->_prevNetworkList[prevItem]->transbytes() );
	 }
     }
    return;
  }

  void network::getInterfaceStatistic()
  {
    char buffer[BufferSize];

    memset(buffer, 0 , BufferSize);
    snprintf(buffer, BufferSize, INT_IPV4_FILE, getpid());
    FILE *ifFile = fopen(buffer, "r");

    if(ifFile ==0)
      return;

    // skip 2 lines
    fgets(buffer, BufferSize, ifFile);
    fgets(buffer, BufferSize, ifFile);

    while(fgets(buffer, BufferSize, ifFile) != NULL)
    {
      char curName[BufferSize];
      unsigned long recvBytes = 0;
      unsigned long recvPackages = 0;
      unsigned long recvErrorBytes = 0;
      unsigned long recvDropBytes = 0;
      unsigned long recvFIFOBytes = 0;
      unsigned long recvFrames = 0;
      unsigned long recvCompressedBytes = 0;
      unsigned long recvMultiCastBytes = 0;
      unsigned long transBytes = 0;
      unsigned long transPackages = 0;
      unsigned long transErrorBytes = 0;
      unsigned long transDropBytes = 0;
      unsigned long transFIFOBytes = 0;
      unsigned int collisionTimes = 0;
      unsigned int carrierErrors = 0;
      unsigned long transCompressedBytes = 0;

      networkInfo* curNetworkInfo = new networkInfo();

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

      if(matchCounts >= 16)
      {
        curNetworkInfo->set_name(curName);
        curNetworkInfo->set_recvbytes(recvBytes);
        curNetworkInfo->set_recvpackages(recvPackages);
        curNetworkInfo->set_recverrorbytes(recvErrorBytes);
        curNetworkInfo->set_recvdropbytes(recvDropBytes);
        curNetworkInfo->set_recvfifobytes(recvFIFOBytes);
        curNetworkInfo->set_recvframes(recvFrames);
        curNetworkInfo->set_recvcompressedbytes(recvCompressedBytes);
        curNetworkInfo->set_recvmulticastbytes(recvMultiCastBytes);
        curNetworkInfo->set_transbytes(transBytes);
        curNetworkInfo->set_transpackages(transPackages);
        curNetworkInfo->set_transerrorbytes(transErrorBytes);
        curNetworkInfo->set_transdropbytes(transDropBytes);
        curNetworkInfo->set_transfifobytes(transFIFOBytes);
        curNetworkInfo->set_collisiontimes(collisionTimes);
        curNetworkInfo->set_carriererrors(carrierErrors);
        curNetworkInfo->set_transcompressedbytes(transCompressedBytes);
        curNetworkInfo->set_transusage(0);
        curNetworkInfo->set_recvusage(0);

        this->_curNetworkList.push_back(curNetworkInfo);
      }
      else
        delete curNetworkInfo;
    }
    fclose(ifFile);
  }

  void network::getMACInformation(networkInfo* curNetworkInfo)
  {
    char buffer[BufferSize];
    char curMACAddr[BufferSize];
    int curMAC = 0;

    memset(buffer, 0, BufferSize);
    memset(curMACAddr, 0, BufferSize);

    snprintf(buffer, BufferSize, INT_MAC_FILE, curNetworkInfo->name().c_str());
    curMAC = open(buffer, O_RDONLY);
    if(curMAC != -1)
    {
      read(curMAC, curMACAddr, 17);
      close(curMAC);
    }

    // keep curMacAddr is null-terminate as possible
    curMACAddr[17] = 0;

    if(strlen(curMACAddr) < 17)
      curMACAddr[0] = 0;

    curNetworkInfo->set_mac(curMACAddr);
    return;
  }

  void network::getIPv6Information()
  {
    char buffer[BufferSize];
    memset(buffer, 0, BufferSize);
    snprintf(buffer, BufferSize, INT_IPV6_FILE, getpid());

    FILE *ifFile = fopen(buffer, "r");
    if(ifFile ==0)
      return;

    //00000000000000000000000000000001 01 80 10 80       lo
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

      if(matchCounts == 18)
      {
        // search matched interface
        std::vector<networkInfo*>::iterator curIter = this->_curNetworkList.begin();
        while(curIter != this->_curNetworkList.end())
        {
          char addrV6[INET6_ADDRSTRLEN];

          if((*curIter)->name().compare(curName) != 0)
          {
            curIter++;
            continue;
          }

          memset(addrV6, 0, INET6_ADDRSTRLEN);
          inet_ntop(AF_INET6, &curIPv6, addrV6, INET6_ADDRSTRLEN);

          (*curIter)->set_ipv6addr(addrV6);
          (*curIter)->set_netmaskv6(curNetmaskV6);

          curIter++;
        }
      }
    }
    fclose(ifFile);
  }

  void network::getIPv4Information(networkInfo* curNetworkInfo)
  {
    char curIPv4[INET_ADDRSTRLEN];
    char curNetMaskv4[INET_ADDRSTRLEN];
    struct ifreq curIFREQ;
    int curSocket = 0;

    memset(curIPv4, 0, INET_ADDRSTRLEN);
    memset(curNetMaskv4, 0, INET_ADDRSTRLEN);
    memset(&curIFREQ, 0, sizeof(struct ifreq));
    strncpy(curIFREQ.ifr_name, curNetworkInfo->name().c_str(), IFNAMSIZ);

    if((curSocket = socket(AF_INET, SOCK_DGRAM, 0)) >= 0)
    {
      if (ioctl(curSocket, SIOCGIFADDR, &curIFREQ) >= 0)
      {
        inet_ntop(AF_INET, &((struct sockaddr_in *) &curIFREQ.ifr_addr)->sin_addr.s_addr,
                    curIPv4, INET_ADDRSTRLEN);
        curNetworkInfo->set_ipv4addr(curIPv4);
      }

      if (ioctl(curSocket, SIOCGIFNETMASK, &curIFREQ) >= 0)
      {
        inet_ntop(AF_INET, &((struct sockaddr_in *) &curIFREQ.ifr_addr)->sin_addr.s_addr,
                              curNetMaskv4, INET_ADDRSTRLEN);
        curNetworkInfo->set_netmaskv4(curNetMaskv4);
      }

      if (ioctl(curSocket, SIOCGIFFLAGS, &curIFREQ) >= 0)
        curNetworkInfo->set_flags(curIFREQ.ifr_flags);

      close(curSocket);
    }
  }

  void network::getTrafficInformation(networkInfo* curNetworkInfo)
  {
    char buffer[BufferSize];
    char curTrafficData[BufferSize];
    int curTraffic = 0;

    if( curNetworkInfo->recvbytes() == 0)
    {
      memset(buffer, 0, BufferSize);
      memset(curTrafficData, 0, BufferSize);

      snprintf(buffer, BufferSize, INT_RX_FILE, curNetworkInfo->name().c_str());
      if((curTraffic = open(buffer, O_RDONLY)) != -1)
      {
        read(curTraffic, curTrafficData, BufferSize);
        close(curTraffic);
      }
      curNetworkInfo->set_recvbytes(strtoul(curTrafficData, NULL, 0));
    }

    if( curNetworkInfo->transbytes() == 0)
    {
      memset(buffer, 0, BufferSize);
      memset(curTrafficData, 0, BufferSize);

      snprintf(buffer, BufferSize, INT_TX_FILE, curNetworkInfo->name().c_str());
      if( (curTraffic = open(buffer, O_RDONLY)) != -1 )
      {
        read(curTraffic, curTrafficData, BufferSize);
        close(curTraffic);
      }
      curNetworkInfo->set_transbytes(strtoul(curTrafficData, NULL, 0));
    }
  }

  const std::vector<google::protobuf::Message*>& network::getData()
  {
    return ((const std::vector<google::protobuf::Message*>&) this->_curNetworkList);
  }

}
}
}
}
