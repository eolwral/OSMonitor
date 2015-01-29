// automatically generated, do not modify

package com.eolwral.osmonitor.core;

public class connectionType {
  /// TCP version 4
  public static final byte TCPv4 = 0;
  /// TCP version 6
  public static final byte TCPv6 = 1;
  /// UDP version 4
  public static final byte UDPv4 = 2;
  /// UDP version 6
  public static final byte UDPv6 = 3;
  /// RAW version 4
  public static final byte RAWv4 = 4;
  /// RAW version 6
  public static final byte RAWv6 = 5;

  private static final String[] names = { "TCPv4", "TCPv6", "UDPv4", "UDPv6", "RAWv4", "RAWv6", };

  public static String name(int e) { return names[e]; }
};

