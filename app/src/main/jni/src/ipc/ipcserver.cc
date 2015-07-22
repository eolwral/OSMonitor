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
    this->socketUid = 0;

    this->uServerLen = 0;
    memset(&this->uServerAddr, 0, sizeof(this->uServerAddr));

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

  bool ipcserver::init()
  {
    // check socket name to avoid overflow
    if (this->uServerSocketName.length() > UNIX_PATH_MAX)
      return (false);

    if (!this->isAbstractSocket())
    {
      __android_log_print(ANDROID_LOG_VERBOSE, "OSMCore","use file system Unix domain socket");
      strncpy(this->uServerAddr.sun_path, this->uServerSocketName.c_str(), UNIX_PATH_MAX-1);
      this->uServerLen = this->uServerSocketName.length() + offsetof(struct sockaddr_un, sun_path);

      // unlink file
      if (unlink(this->uServerSocketName.c_str()) != 0)
        if (errno != ENOENT) return (false);
    }
    else {
        __android_log_print(ANDROID_LOG_VERBOSE, "OSMCore","use abstract Unix domain socket");
      this->uServerAddr.sun_path[0] = '\0';
      strcpy(this->uServerAddr.sun_path+1, this->uServerSocketName.c_str());
      this->uServerLen = 1 + this->uServerSocketName.length() + offsetof(struct sockaddr_un, sun_path);
    }

    this->uServerAddr.sun_family = AF_UNIX;

    return (true);
  }

  bool ipcserver::isAbstractSocket()
  {
    if (this->uServerSocketName[0] == '/')
      return (false);
    return (true);
  }

  bool ipcserver::bind()
  {
    // listen
    this->serverFD = socket(AF_UNIX, SOCK_STREAM, 0);

    if(this->serverFD < 0) {
      __android_log_print(ANDROID_LOG_VERBOSE, "OSMCore","Failed to open socket: %d\n", errno);
      return (false);
    }

    // set socket is reusable
    int option = true;
    if(::setsockopt(this->serverFD, SOL_SOCKET, SO_REUSEADDR, (char *)&option, sizeof(option)) < 0)
    {
      __android_log_print(ANDROID_LOG_VERBOSE, "OSMCore","Failed to set socket failed: %d\n", errno);
      ::close(this->serverFD);
      return (false);
    }

    int result = 0;
    result = ::bind(this->serverFD, (const sockaddr*) &this->uServerAddr, this->uServerLen);

    if (result < 0)
    {
      __android_log_print(ANDROID_LOG_VERBOSE, "OSMCore","Failed to bind socket: %d\n", errno);
      ::close(this->serverFD);
      return (false);
    }

    if (listen(this->serverFD, 8) < 0)
    {
      __android_log_print(ANDROID_LOG_VERBOSE, "OSMCore","Failed to listen: %d\n", errno);
      ::close(this->serverFD);
      return (false);
    }

    if (!this->isAbstractSocket())
    {
      if (chown(this->uServerSocketName.c_str(), this->socketUid, this->socketUid) != 0)
      {
        __android_log_print(ANDROID_LOG_VERBOSE, "OSMCore","Failed to change owner: %d\n", errno);
        ::close(this->serverFD);
        return (false);
      }
    }

    return (true);
  }

  bool ipcserver::accept()
  {

    // accept new connection
    int newSocket = 0;
    newSocket = ::accept(this->serverFD, NULL, NULL);

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
    int tokenlen = 0;
    memset(tokenloc, 0, TOKENSIZE);
    FILE* tokenFile = fopen(fileName, "r");

    if (tokenFile == NULL)
      return;

    tokenlen = fread(tokenloc, sizeof(char), TOKENSIZE, tokenFile);
    fclose(tokenFile);

    if (tokenlen >=  TOKENSIZE-1)
      tokenloc[TOKENSIZE-1] = '\x0';
    else
      tokenloc[tokenlen] = '\x0';

    // save token
    token.assign(tokenloc);

    // erase token filename (for secure communication)
    int size = strlen(fileName);
    memset(fileName, 'x', size);

    return;
  }

  void ipcserver::extractSocketName(char* socketName)
  {

    // save socket name
    this->uServerSocketName.assign(socketName);

    // erase socket name (for secure communication)
    int size = strlen(socketName);
    memset(socketName, 'x', size);

    return;
  }

  void ipcserver::extractUid(char* uid)
  {
    this->socketUid = atoi(uid);
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
