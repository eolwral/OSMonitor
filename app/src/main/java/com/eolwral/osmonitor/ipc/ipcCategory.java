// automatically generated, do not modify

package com.eolwral.osmonitor.ipc;

public class ipcCategory {
  /// Non-Exist
  public static final byte NONEXIST = 0;
  /// OS 
  public static final byte OS = 1;
  /// CPU
  public static final byte CPU = 2;
  /// Processor 
  public static final byte PROCESSOR = 3;
  /// Process 
  public static final byte PROCESS = 4;
  /// Connection 
  public static final byte CONNECTION = 5;
  /// Network 
  public static final byte NETWORK = 6;
  /// DMesg 
  public static final byte DMESG = 7;
  /// LogCat Radio 
  public static final byte LOGCAT_RADIO = 8;
  /// LogCat Event 
  public static final byte LOGCAT_EVENT = 9;
  /// LogCat System 
  public static final byte LOGCAT_SYSTEM = 10;
  /// LogCat Main 
  public static final byte LOGCAT_MAIN = 11;
  /// LogCat Crash 
  public static final byte LOGCAT_CRASH = 12;
  /// LogCat Main for WatchLog 
  public static final byte LOGCAT_MAIN_R = 13;
  /// Set Priority
  public static final byte SETPRIORITY = 21;
  /// Kill Processes
  public static final byte KILLPROCESS = 22;
  /// Set CPU online
  public static final byte SETCPUSTATUS = 23;
  /// Set CPU Frequency
  public static final byte SETCPUMAXFREQ = 24;
  /// Set CPU Frequency
  public static final byte SETCPUMINFREQ = 25;
  /// Set CPU Governor
  public static final byte SETCPUGORV = 26;
  /// End
  public static final byte FINAL = 99;

  private static final String[] names = { "NONEXIST", "OS", "CPU", "PROCESSOR", "PROCESS", "CONNECTION", "NETWORK", "DMESG", "LOGCAT_RADIO", "LOGCAT_EVENT", "LOGCAT_SYSTEM", "LOGCAT_MAIN", "LOGCAT_CRASH", "LOGCAT_MAIN_R", "", "", "", "", "", "", "", "SETPRIORITY", "KILLPROCESS", "SETCPUSTATUS", "SETCPUMAXFREQ", "SETCPUMINFREQ", "SETCPUGORV", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "FINAL", };

  public static String name(int e) { return names[e]; }
};

