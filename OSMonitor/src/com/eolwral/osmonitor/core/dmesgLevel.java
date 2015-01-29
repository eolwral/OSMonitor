// automatically generated, do not modify

package com.eolwral.osmonitor.core;

public class dmesgLevel {
  /// emergency 
  public static final byte EMERGENCY = 0;
  /// alert 
  public static final byte ALERT = 1;
  /// critical 
  public static final byte CRITICAL = 2;
  /// error 
  public static final byte ERROR = 3;
  /// warning 
  public static final byte WARNING = 4;
  /// notice 
  public static final byte NOTICE = 5;
  /// information 
  public static final byte INFORMATION = 6;
  /// debug 
  public static final byte DEBUG = 7;

  private static final String[] names = { "EMERGENCY", "ALERT", "CRITICAL", "ERROR", "WARNING", "NOTICE", "INFORMATION", "DEBUG", };

  public static String name(int e) { return names[e]; }
};

