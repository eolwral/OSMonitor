package com.eolwral.osmonitor.ipc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

/**
 * implement communication mechanize between process with Unix socket
 */
public class IpcConnection {

  /**
   * predefine buffer size
   */
  public final static int sendBufferSize = 131072; /* 128K */
  public final static int recvBufferSize = 10485760; /* 10M */

  /**
   * 
   */
  private String socketName = "";

  /**
   * Unix domain socket
   */
  private LocalSocket clientSocket = null;

  /**
   * Local unix domain socket address
   */
  private LocalSocketAddress clientAddress = null;

  public IpcConnection(String socketName) {
    this.socketName = socketName;
  }

  public boolean connect(int timeOut) throws IOException {

    clientAddress = new LocalSocketAddress(socketName,
                                           LocalSocketAddress.Namespace.FILESYSTEM);

    clientSocket = new LocalSocket();
    clientSocket.connect(clientAddress);
    clientSocket.setSendBufferSize(sendBufferSize);
    clientSocket.setReceiveBufferSize(recvBufferSize);

    // Notice: the value is milliseconds
    clientSocket.setSoTimeout(timeOut * 1000);

    return true;
  }

  public void close() throws IOException {

    if (clientSocket == null)
      return;

    clientSocket.shutdownInput();
    clientSocket.shutdownOutput();
    clientSocket.close();
    clientSocket = null;
    clientAddress = null;

    return;
  }

  public boolean isConnected() {
    if (clientSocket == null)
      return false;
    return clientSocket.isConnected();
  }

  public OutputStream getOutputStream() throws IOException {
    if (clientSocket == null)
      return null;
    return clientSocket.getOutputStream();
  }

  public InputStream getInputStream() throws IOException {
    if (clientSocket == null)
      return null;
    return clientSocket.getInputStream();
  }

}