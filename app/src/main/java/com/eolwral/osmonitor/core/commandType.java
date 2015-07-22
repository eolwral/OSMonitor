// automatically generated, do not modify

package com.eolwral.osmonitor.core;

public class commandType {
  /// NONE
  public static final byte NOEXIST = 0;
  /// Set Priority
  public static final byte SETPRIORITY = 1;
  /// Kill Processes
  public static final byte KILLPROCESS = 2;
  /// Set CPU online
  public static final byte SETCPUSTATUS = 13;
  /// Set CPU Frequency
  public static final byte SETCPUMAXFREQ = 14;
  /// Set CPU Frequency
  public static final byte SETCPUMINFREQ = 15;
  /// Set CPU Governor
  public static final byte SETCPUGORV = 16;

  private static final String[] names = { "NOEXIST", "SETPRIORITY", "KILLPROCESS", "", "", "", "", "", "", "", "", "", "", "SETCPUSTATUS", "SETCPUMAXFREQ", "SETCPUMINFREQ", "SETCPUGORV", };

  public static String name(int e) { return names[e]; }
};

