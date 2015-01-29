// automatically generated, do not modify

package com.eolwral.osmonitor.core;

public class connectionStatus {
  public static final byte UNKNOWN = 0;
  public static final byte ESTABLISHED = 1;
  public static final byte SYN_SENT = 2;
  public static final byte SYN_RECV = 3;
  public static final byte FIN_WAIT1 = 4;
  public static final byte FIN_WAIT2 = 5;
  public static final byte TIME_WAIT = 6;
  public static final byte CLOSE = 7;
  public static final byte CLOSE_WAIT = 8;
  public static final byte LAST_ACK = 9;
  public static final byte LISTEN = 10;
  public static final byte CLOSING = 11;

  private static final String[] names = { "UNKNOWN", "ESTABLISHED", "SYN_SENT", "SYN_RECV", "FIN_WAIT1", "FIN_WAIT2", "TIME_WAIT", "CLOSE", "CLOSE_WAIT", "LAST_ACK", "LISTEN", "CLOSING", };

  public static String name(int e) { return names[e]; }
};

