// automatically generated, do not modify

package com.eolwral.osmonitor.core;

public class processStatus {
  /// process status is unknown 
  public static final byte Unknown = 0;
  /// process is running 
  public static final byte Running = 1;
  /// process is s sleeping in an interruptible wait 
  public static final byte Sleep = 2;
  /// process is traced or stopped 
  public static final byte Stopped = 3;
  /// process is is waiting in uninterruptible disk sleep 
  public static final byte Disk = 4;
  /// process is zombie that couldn't be killed 
  public static final byte Zombie = 5;
  /// process is paging 
  public static final byte Page = 6;

  private static final String[] names = { "Unknown", "Running", "Sleep", "Stopped", "Disk", "Zombie", "Page", };

  public static String name(int e) { return names[e]; }
};

