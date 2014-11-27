package com.eolwral.osmonitor.ipc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * implement connection between process with TCP socket
 */
public class TCPConnection extends IpcConnectionBase {

  /**
   * predefine TCP port number
   */
  private final static int portNumber = 15075;

  /**
   * TCP socket
   */
  private Socket clientSocket = null;

  /**
   * TCP socket address
   */
  private SocketAddress clientAddress = null;

  @Override
  public boolean connect(int timeOut) throws IOException {

    clientAddress = new InetSocketAddress("127.0.0.1", portNumber);

    clientSocket = new Socket();
    clientSocket.connect(clientAddress);
    clientSocket.setSendBufferSize(sendBufferSize);
    clientSocket.setReceiveBufferSize(recvBufferSize);
    clientSocket.setKeepAlive(true);

    // Notice: the value is milliseconds
    clientSocket.setSoTimeout(timeOut * 1000);

    return false;
  }

  @Override
  public void close() throws IOException {

    if (clientSocket == null)
      return;

    clientSocket.close();
    clientSocket = null;
    clientAddress = null;

    return;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    if (clientSocket == null)
      return null;
    return clientSocket.getOutputStream();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if (clientSocket == null)
      return null;
    return clientSocket.getInputStream();
  }

  @Override
  public boolean isConnected() {
    if (clientSocket == null)
      return false;
    return clientSocket.isConnected();
  }
}
