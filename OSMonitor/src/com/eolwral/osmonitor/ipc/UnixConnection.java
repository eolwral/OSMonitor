package com.eolwral.osmonitor.ipc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

/**
 * implement communicate mechanize between process with Unix socket
 */
public class UnixConnection extends IpcConnectionBase {

  /**
   * predefine socket name
   */
  private final static String socketName = "osmipcV3";

  /**
   * Unix domain socket
   */
  private LocalSocket clientSocket = null;

  /**
   * Local unix domain socket address
   */
  private LocalSocketAddress clientAddress = null;

  @Override
  public boolean connect(int timeOut) throws IOException {

    clientAddress = new LocalSocketAddress(socketName,
        LocalSocketAddress.Namespace.ABSTRACT);

    clientSocket = new LocalSocket();
    clientSocket.connect(clientAddress);
    clientSocket.setSendBufferSize(sendBufferSize);
    clientSocket.setReceiveBufferSize(recvBufferSize);

    // Notice: the value is milliseconds
    clientSocket.setSoTimeout(timeOut * 1000);

    return true;
  }

  @Override
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

  @Override
  public boolean isConnected() {
    if (clientSocket == null)
      return false;
    return clientSocket.isConnected();
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

}
