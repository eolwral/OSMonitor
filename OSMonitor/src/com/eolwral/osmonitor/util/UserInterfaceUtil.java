package com.eolwral.osmonitor.util;

import android.app.Activity;
import android.content.res.Resources;

import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.core.ConnectionInfo.connectionInfo;
import com.eolwral.osmonitor.core.ConnectionInfo.connectionInfo.connectionStatus;
import com.eolwral.osmonitor.core.ConnectionInfo.connectionInfo.connectionType;
import com.eolwral.osmonitor.core.DmesgInfo.dmesgInfo;
import com.eolwral.osmonitor.core.DmesgInfo.dmesgInfo.dmesgLevel;
import com.eolwral.osmonitor.core.LogcatInfo.logcatInfo;
import com.eolwral.osmonitor.core.LogcatInfo.logcatInfo.logPriority;
import com.eolwral.osmonitor.core.ProcessInfo.processInfo;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcAction;
import com.eolwral.osmonitor.settings.Settings;

public class UserInterfaceUtil {

  // internal variable 
  private static Settings settings = null;
  private static Resources resource = null;

  /**
   * Initialize UserInterfaceUtil
   * @param activity
   */
  public static void Initialize(Activity activity) {
    if (settings == null)
      settings = Settings.getInstance(activity.getApplicationContext());
    if (resource == null)
      resource = activity.getResources();
  }
  
  /**
   * get status by processStatus
   * @param processStatus
   * @return status (by char)
   */
  public static String getSatusString(processInfo.processStatus status) {
    switch (status.getNumber()) {
    case processInfo.processStatus.Running_VALUE:
      return resource.getText(R.string.ui_process_status_running).toString();
    case processInfo.processStatus.Sleep_VALUE:
      return resource.getText(R.string.ui_process_status_sleep).toString();
    case processInfo.processStatus.Stopped_VALUE:
      return resource.getText(R.string.ui_process_status_stop).toString();
    case processInfo.processStatus.Page_VALUE:
    case processInfo.processStatus.Disk_VALUE:
      return resource.getText(R.string.ui_process_status_waitio).toString();
    case processInfo.processStatus.Zombie_VALUE:
      return resource.getText(R.string.ui_process_status_zombie).toString();
    }
    return resource.getText(R.string.ui_process_status_unknown).toString();
  }
  
  /**
   * convert Logcat to integer
   * @param type
   * @return integer 
   */
  public static int convertLogcatType(int type) {
    switch (type) {
    case logcatInfo.logPriority.DEBUG_VALUE:
      return 0;
    case logcatInfo.logPriority.VERBOSE_VALUE:
      return 1;
    case logcatInfo.logPriority.INFO_VALUE:
      return 2;
    case logcatInfo.logPriority.WARN_VALUE:
      return 3;
    case logcatInfo.logPriority.ERROR_VALUE:
      return 4;
    case logcatInfo.logPriority.FATAL_VALUE:
      return 5;
    }
    return 0;
  }

  /**
   * convert dmesg to integer
   * @param type
   * @return integer
   */
  public static int convertDmesgType(int type) {
    switch (type) {
    case dmesgInfo.dmesgLevel.DEBUG_VALUE:
      return 0;
    case dmesgInfo.dmesgLevel.INFORMATION_VALUE:
      return 1;
    case dmesgInfo.dmesgLevel.NOTICE_VALUE:
      return 2;
    case dmesgInfo.dmesgLevel.WARNING_VALUE:
      return 3;
    case dmesgInfo.dmesgLevel.ALERT_VALUE:
      return 4;
    case dmesgInfo.dmesgLevel.EMERGENCY_VALUE:
      return 5;
    case dmesgInfo.dmesgLevel.ERROR_VALUE:
      return 6;
    case dmesgInfo.dmesgLevel.CRITICAL_VALUE:
      return 7;
    }
    return 0;
  }

  /**
   * convert Integer to IpcAction type
   * @param integer
   * @return Logcat
   */
  public static ipcAction convertLocToType(int loc) {
    switch (loc) {
    case 0:
      return ipcAction.LOGCAT_MAIN;
    case 1:
      return ipcAction.LOGCAT_SYSTEM;
    case 2:
      return ipcAction.LOGCAT_EVENT;
    case 3:
      return ipcAction.LOGCAT_RADIO;
    case 4:
      return ipcAction.DMESG;
    }

    return ipcAction.LOGCAT_MAIN;
  }

  /**
   * convert IpcAction into integer
   * @param IpcAction
   * @return integer
   */
  public static int convertTypeToLoc(ipcAction type) {
    switch (type) {
    case LOGCAT_MAIN:
      return 0;
    case LOGCAT_SYSTEM:
      return 1;
    case LOGCAT_EVENT:
      return 2;
    case LOGCAT_RADIO:
      return 3;
    case DMESG:
      return 4;
    }
    return 0;
  }

  /**
   * get priority string by logPriority
   * @param logPriority
   * @return String
   */
  public static String getLogprority(logPriority priority) {
    switch (priority.getNumber()) {
    case logcatInfo.logPriority.SILENT_VALUE:
      return "SILENT";
    case logcatInfo.logPriority.DEFAULT_VALUE:
      return "DEFAULT";
    case logcatInfo.logPriority.VERBOSE_VALUE:
      return "VERBOSE";
    case logcatInfo.logPriority.WARN_VALUE:
      return "WARNING";
    case logcatInfo.logPriority.INFO_VALUE:
      return "INFORMATION";
    case logcatInfo.logPriority.FATAL_VALUE:
      return "FATAL";
    case logcatInfo.logPriority.ERROR_VALUE:
      return "ERROR";
    case logcatInfo.logPriority.DEBUG_VALUE:
      return "DEBUG";
    }
    return "UNKNOWN";
  }
  
  /**
   * get dmesg by dmesgLevel
   * @param dmesgLevel
   * @return String
   */
  public static String getDmesgLevel(dmesgLevel level) {
    switch (level.getNumber()) {
    case dmesgInfo.dmesgLevel.DEBUG_VALUE:
      return "DEBUG";
    case dmesgInfo.dmesgLevel.INFORMATION_VALUE:
      return "INFORMATION";
    case dmesgInfo.dmesgLevel.NOTICE_VALUE:
      return "NOTICE";
    case dmesgInfo.dmesgLevel.WARNING_VALUE:
      return "WARNING";
    case dmesgInfo.dmesgLevel.EMERGENCY_VALUE:
      return "EMERGENCY";
    case dmesgInfo.dmesgLevel.ERROR_VALUE:
      return "ERROR";
    case dmesgInfo.dmesgLevel.ALERT_VALUE:
      return "ALERT";
    case dmesgInfo.dmesgLevel.CRITICAL_VALUE:
      return "CRITICAL";
    }
    return "UNKNOWN";
  }

  /**
   * get color by Logcat type
   * @param logPriority
   * @return integer color
   */
  public static int getLogcatColor(logPriority priority) {
    switch (priority.getNumber()) {
    case logcatInfo.logPriority.WARN_VALUE:
      return settings.getLogcatWarningColor();
    case logcatInfo.logPriority.INFO_VALUE:
      return settings.getLogcatInfoColor();
    case logcatInfo.logPriority.FATAL_VALUE:
      return settings.getLogcatFatalColor();
    case logcatInfo.logPriority.ERROR_VALUE:
      return settings.getLogcatErrorColor();
    case logcatInfo.logPriority.DEBUG_VALUE:
      return settings.getLogcatDebugColor();
    case logcatInfo.logPriority.SILENT_VALUE:
    case logcatInfo.logPriority.UNKNOWN_VALUE:
    case logcatInfo.logPriority.DEFAULT_VALUE:
    case logcatInfo.logPriority.VERBOSE_VALUE:
      return settings.getLogcatVerboseColor();
    }
    return settings.getLogcatVerboseColor();
  }

  /**
   * get short tag for Logcat priority
   * @param logPriority
   * @return String short tag
   */
  public static String getLogcatTag(logPriority priority) {
    switch (priority.getNumber()) {
    case logcatInfo.logPriority.SILENT_VALUE:
    case logcatInfo.logPriority.UNKNOWN_VALUE:
    case logcatInfo.logPriority.DEFAULT_VALUE:
      return "S";
    case logcatInfo.logPriority.VERBOSE_VALUE:
      return "V";
    case logcatInfo.logPriority.WARN_VALUE:
      return "W";
    case logcatInfo.logPriority.INFO_VALUE:
      return "I";
    case logcatInfo.logPriority.FATAL_VALUE:
      return "F";
    case logcatInfo.logPriority.ERROR_VALUE:
      return "E";
    case logcatInfo.logPriority.DEBUG_VALUE:
      return "D";
    }
    return "S";
  }
  
  /**
   * get connection type by connectionType
   * @param connectionType
   * @return String connection type 
   */
  public static String getConnectionType(connectionType type) {
    switch (type.getNumber()) {
    case connectionInfo.connectionType.TCPv4_VALUE:
      return "TCP4";
    case connectionInfo.connectionType.TCPv6_VALUE:
      return "TCP6";
    case connectionInfo.connectionType.UDPv4_VALUE:
      return "UDP4";
    case connectionInfo.connectionType.UDPv6_VALUE:
      return "UDP6";
    case connectionInfo.connectionType.RAWv4_VALUE:
      return "RAW4";
    case connectionInfo.connectionType.RAWv6_VALUE:
      return "RAW6";
    }
    return "????";
  }
  
  /**
   * get connection status by connectionStatus
   * @param connectionStatus
   * @return connection status
   */
  public static String getConnectionStatus(connectionStatus status) {
    switch (status.getNumber()) {
    case connectionInfo.connectionStatus.CLOSE_VALUE:
      return "CLOSE";
    case connectionInfo.connectionStatus.CLOSE_WAIT_VALUE:
      return "CLOSE_WAIT";
    case connectionInfo.connectionStatus.CLOSING_VALUE:
      return "CLOSING";
    case connectionInfo.connectionStatus.ESTABLISHED_VALUE:
      return "ESTABLISHED";
    case connectionInfo.connectionStatus.FIN_WAIT1_VALUE:
      return "FIN_WAIT1";
    case connectionInfo.connectionStatus.FIN_WAIT2_VALUE:
      return "FIN_WAIT2";
    case connectionInfo.connectionStatus.LAST_ACK_VALUE:
      return "LAST_ACK";
    case connectionInfo.connectionStatus.LISTEN_VALUE:
      return "LISTEN";
    case connectionInfo.connectionStatus.SYN_RECV_VALUE:
      return "SYN_RECV";
    case connectionInfo.connectionStatus.SYN_SENT_VALUE:
      return "SYN_SENT";
    case connectionInfo.connectionStatus.TIME_WAIT_VALUE:
      return "TIME_WAIT";
    }
    return "UNKNOWN";
  }
  
  /**
   * combine IP and port as a string
   * @param String ip
   * @param integer port
   * @return String
   */
  public static String convertToIPv4(String ip, int port) {
    // replace IPv6 to IPv4
    ip = ip.replace("::ffff:", "");
    if (port == 0)
      return ip + ":*";
    return ip + ":" + port;
  }

}
