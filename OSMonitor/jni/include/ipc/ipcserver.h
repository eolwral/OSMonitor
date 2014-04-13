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

#include "ipcMessage.pb.h"

#define SOCKETNAME "osmipcV1"
#define SOCKETBUF 1024
#define TRANSIZE 4096

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
    struct sockaddr_un serverAddr;      /**<< server socket address  */
    socklen_t serverLen;                /**<< server socket length */

    int waitNumber;                     /**<< which client is wait */
    int clientFD[8];                    /**<< client file descriptor */
    bool verified[8];                   /**<< verified security token */
    std::string token;                  /**<< security token */

    /**
     * close the specific socket
     * @param number ==> client socket number
     */
    void closeSocket(int number);

    /**
     * check if the client connect exist
     * @return true == yes, false = false
     */
    bool hasClient();

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
     * initialize a Unix domain socket
     * @param socketName socket name
     * @return success or fail
     */
    bool init(char* socketName);

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
     * @param data buffer
     * @param size buffer size
     * @param recvsize data size for received data
     * @return success or fail
     */
    bool receieve(char* data, int& size, int& recvsize);

    /**
     * send data to client
     * @param data data
     * @param size data size
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
     * @param token
     */
    void extractToken(char* loc);

  };

}
}
}
}


#endif /* IPCSERVER_H_ */
