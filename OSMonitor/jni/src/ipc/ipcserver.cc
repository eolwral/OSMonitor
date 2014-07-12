/**
 * @file ipsserver.cc
 * @brief IPCServer Class file
 */

#include "ipcserver.h"

namespace com {
namespace eolwral {
namespace osmonitor {
namespace ipc {

  ipcserver::ipcserver()
  {
    // Initialize
    this->serverFD = 0;
    this->waitNumber = 0;

    bzero((char *)&this->serverAddr, sizeof(this->serverAddr));

    // initialize clean socket
    for ( int index = 0 ; index < 8 ; index++)
    {
        this->clientFD[index] = 0;
        this->verified[index] = false;
    }
  }

  ipcserver::~ipcserver()
  {
    this->clean();
  }

  void ipcserver::closeSocket(int number)
  {
    if(this->clientFD[number] != 0)
      ::close(this->clientFD[number]);
    this->clientFD[number] = 0;
    this->verified[number] = false;
  }

  bool ipcserver::hasClient()
  {
    int hasClient = false;
    for ( int index = 0 ; index < 8 ; index++)
      if(this->clientFD[index] != 0)
        hasClient = true;
    return (hasClient);
  }

  bool ipcserver::init(int portNumber)
  {
    // check socket name to avoid overflow
    this->serverAddr.sin_family = AF_INET;
    this->serverAddr.sin_addr.s_addr = INADDR_ANY;//inet_addr("127.0.0.1");
    this->serverAddr.sin_port = htons(portNumber);

    return (true);
  }

  bool ipcserver::bind()
  {
    // listen
    this->serverFD = socket(AF_INET, SOCK_STREAM, 0);
    if(this->serverFD < 0)
      return (false);

    // set socket is reusable
    int option = true;
    if(::setsockopt(this->serverFD, SOL_SOCKET, SO_REUSEADDR, (char *)&option, sizeof(option)) < 0)
    {
      ::close(this->serverFD);
      return (false);
    }

    if(::bind(this->serverFD, (const sockaddr*) &this->serverAddr, sizeof(this->serverAddr)) < 0)
    {
      ::close(this->serverFD);
      return (false);
    }

    if (listen(this->serverFD, 8) < 0)
    {
      ::close(this->serverFD);
      return (false);
    }

    return (true);
  }

  bool ipcserver::accept()
  {
    // client address
    struct sockaddr_in clientAddr;
    int clientAddrLen = sizeof(clientAddr);;

    // accept new connection
    int newSocket = ::accept(this->serverFD, (struct sockaddr *) &clientAddr, &clientAddrLen);
    if (newSocket < 0)
      return (false);

    // save new connection
    bool saved = false;
    for ( int index = 0 ; index < 8 ; index++)
    {
      if(this->clientFD[index] == 0)
      {
        this->clientFD[index] = newSocket;
        this->verified[index] = false;
        saved = true;
        break;
      }
    }

    // if can't save, close it
    if(saved == false)
    {
      ::close(newSocket);
      return (false);
    }

    return (true);
  }

  bool ipcserver::receieve(char* data, int& size, int& recvsize)
  {
     // receive data
    memset(data, 0, size);
    recvsize = read(this->clientFD[waitNumber], data, size);
    if (recvsize == 0)
    {
      this->closeSocket(waitNumber);
      return (false);
    }
    return (true);
  }

  bool ipcserver::send(char* data, int size)
  {
    // check size
    if(size == 0)
    {
      this->closeSocket(waitNumber);
      return (false);
    }

    // send size
    if(write(this->clientFD[waitNumber], &size, sizeof(int)) != sizeof(int))
    {
      this->closeSocket(waitNumber);
      return (false);
    }

    // send payload
    int sendSize = write(this->clientFD[waitNumber], data, size);
    if(size != sendSize)
    {
      this->closeSocket(waitNumber);
      return (false);
    }
    return (true);
  }

  void ipcserver::clean()
  {
    for (int index = 0; index < 8 ; index++)
      this->closeSocket(index);

    if(this->serverFD != 0)
    {
      ::close(this->serverFD);
      this->serverFD = 0;
    }
  }

  void ipcserver::close()
  {
    this->closeSocket(waitNumber);
    return;
  }

  ipcserver::EVENT ipcserver::poll()
  {
    struct timeval timeout;
    fd_set socketset;
    int maxsocket;

    // prepare FD
    FD_ZERO(&socketset);

    // add server socket to set
    maxsocket = this->serverFD;
    FD_SET(this->serverFD, &socketset);

    //add client sockets to set
    for ( int index = 0 ; index < 8 ; index++)
    {
      if(this->clientFD[index] != 0)
      {
        FD_SET(this->clientFD[index] , &socketset);
        if(maxsocket < this->clientFD[index])
          maxsocket = this->clientFD[index];
      }
    }

    // set timeout
    timeout.tv_sec  = 5;
    timeout.tv_usec = 0;

    // wait for connection for 5 seconds (on blocking mode)
    int result = 0;
    if(this->hasClient())
      result = select(maxsocket + 1, &socketset, NULL, NULL, NULL);
    else
      result = select(maxsocket + 1, &socketset, NULL, NULL, &timeout);

    if (result < 0 && errno != EINTR )
      return (ERROR);

    // a new connection
    if (FD_ISSET(this->serverFD, &socketset))
      return (CONNECTION);

    // a new command
    waitNumber = -1;
    for ( int index = 0 ; index < 8 ; index++)
    {
      if(this->clientFD[index] == 0)
        continue;

      if(FD_ISSET(this->clientFD[index], &socketset))
        waitNumber = index;
    }

    // receive commands from client
    if(waitNumber != -1)
      return (COMMAND);

    // client is still connected
    if(this->hasClient())
      return (WAIT);

    // if no one connected, return error
    return (ERROR);
  }

  int ipcserver::getClientId()
  {
    return (waitNumber);
  }

  void ipcserver::extractToken(char* fileName)
  {
    // load token from file
    char tokenloc[TOKENSIZE];
    memset(tokenloc, 0, TOKENSIZE);
    FILE* tokenFile = fopen(fileName, "r");
    if (tokenFile == NULL) return;
    fread(tokenloc, sizeof(char), TOKENSIZE, tokenFile);
    fclose(tokenFile);
    tokenloc[TOKENSIZE-1] = '\0';

    // save token
    token.assign(tokenloc);

    // erase token filename (for secure communication)
    int size = strlen(fileName);
    memset(fileName, 'x', size);

    return;
  }

  bool ipcserver::isVerified()
  {
    if(this->verified[waitNumber] == true)
      return (true);
    return (false);
  }

  bool ipcserver::checkToken()
  {
    // check security token
    int tokenSize = 0;
    char buffer[256];
    int bufferSize = 256;

    // receive data
    memset(buffer, 0, bufferSize);
    tokenSize = read(this->clientFD[waitNumber], buffer, bufferSize);
    if (tokenSize == -1 || token.compare(buffer) != 0)
    {
      this->closeSocket(waitNumber);
      return (false);
    }

    this->verified[waitNumber] = true;
    return (true);
  }


}
}
}
}
