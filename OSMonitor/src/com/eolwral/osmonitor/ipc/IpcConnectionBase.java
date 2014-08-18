package com.eolwral.osmonitor.ipc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * implement communicate mechanize between process with Unix socket
 */
abstract public class IpcConnectionBase {
  
  /**
   * predefine buffer size
   */
  public final static int sendBufferSize = 131072; /* 128K */
  public final static int recvBufferSize = 1048576; /* 1M */

  abstract public boolean connect(int timeOut) throws IOException;
  abstract public void close() throws IOException;
  abstract public boolean isConnected();
  
  abstract public OutputStream getOutputStream() throws IOException;
  abstract public InputStream getInputStream() throws IOException;

}
