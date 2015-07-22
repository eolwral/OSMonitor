/**
 * @file ipsserver.h
 * @brief IPCServer Class Header file
 */

#ifndef IPCSERVER_H_
#define IPCSERVER_H_

#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stddef.h>
#include <errno.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <sys/ioctl.h>
#include <sys/select.h>
#include <sys/time.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/stat.h>

#include "ipcMessage_generated.h"

#define SOCKETBUF 1024
#define TRANSIZE 4096
#define TOKENSIZE 256

#include <android/log.h>

namespace com {
namespace eolwral {
namespace osmonitor {
namespace ipc {

  /**
   * @class ipcserver
   * @brief create a Unix domain socket server
   */
  class ipcserver
  {

  private:
    int serverFD;                       /**<< server file descriptor */

    // unix domain socket
    std::string uServerSocketName;      /**<< server socket name */
    struct sockaddr_un uServerAddr;     /**<< server socket address  */
    socklen_t uServerLen;               /**<< server socket length */

    int waitNumber;                     /**<< which client is wait */
    int clientFD[8];                    /**<< client file descriptor */
    bool verified[8];                   /**<< verified security token */
    std::string token;                  /**<< security token */
    unsigned int socketUid;             /**<< default UID */

    /**
     * close the specific socket
     * @param[in] number ==> client socket number
     */
    void closeSocket(int number);

    /**
     * check if the client connect exist
     * @return true == yes, false == no
     */
    bool hasClient();

    /**
     * check socket name, if the socket is abstract, return true
     * @return true == yes, false == no
     */
    bool isAbstractSocket();

  public:

    /**
     * constructor for ipcserver
     */
    ipcserver();

    /**
     * deconstructor for ipcserver
     */
    ~ipcserver();

    /**
     * initialize unix domain socket
     * @return success or fail
     */
    bool init();

    /**
     * bind socket
     */
    bool bind();

    /**
     * accept connections
     * @return success or fail
     */
    bool accept();

    /**
     * receive data from client
     * @param[in] data buffer
     * @param[in] size buffer size
     * @param[in] recvsize data size for received data
     * @return success or fail
     */
    bool receieve(char* data, int& size, int& recvsize);

    /**
     * send data to client
     * @param[in] data data
     * @param[in] size data size
     * @return success or fail
     */
    bool send(char* data, int size);

    /**
     * close socket (client)
     */
    void close();

    /**
     * cleanup socket
     */
    void clean();

    /**
     * result for process
     */
    enum EVENT {
      ERROR,            //!< ERROR
      WAIT,             //!< WAIT
      CONNECTION,       //!< CONNECTION
      COMMAND,          //!< COMMAND
    };

    /**
     * check new event for server
     * @return EVENT
     */
    EVENT poll();

    /**
     * return a id for client
     * @return id
     */
    int getClientId();

    /**
     * check verify status
     * @return verified or not yet
     */
    bool isVerified();

    /**
     * check token
     * @return success or fail
     */
    bool checkToken();

    /**
     * save security token
     * @param[in] token
     */
    void extractToken(char* fileName);

    /**
     * save unix domain socket name
     * @param[in] name
     */
    void extractSocketName(char* socketName);

    /**
     * save uid
     * @param[in] uid
     */
    void extractUid(char* uid);

  };

}
}
}
}


#endif /* IPCSERVER_H_ */
