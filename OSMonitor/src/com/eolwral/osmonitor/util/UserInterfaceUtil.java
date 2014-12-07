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
    
    String result = resource.getText(R.string.ui_process_status_unknown).toString();
    
    switch (status.getNumber()) {
    case processInfo.processStatus.Running_VALUE:
      result = resource.getText(R.string.ui_process_status_running).toString();
      break;
    case processInfo.processStatus.Sleep_VALUE:
      result = resource.getText(R.string.ui_process_status_sleep).toString();
      break;
    case processInfo.processStatus.Stopped_VALUE:
      result = resource.getText(R.string.ui_process_status_stop).toString();
      break;
    case processInfo.processStatus.Page_VALUE:
    case processInfo.processStatus.Disk_VALUE:
      result = resource.getText(R.string.ui_process_status_waitio).toString();
      break;
    case processInfo.processStatus.Zombie_VALUE:
      result = resource.getText(R.string.ui_process_status_zombie).toString();
      break;
    }
    return result;
  }
  
  /**
   * convert Logcat to integer
   * @param type
   * @return integer 
   */
  public static int convertLogcatType(int type) {
    int result = 0;

    switch (type) {
    case logcatInfo.logPriority.DEBUG_VALUE:
      result = 0;
      break;
    case logcatInfo.logPriority.VERBOSE_VALUE:
      result = 1;
      break;
    case logcatInfo.logPriority.INFO_VALUE:
      result = 2;
      break;
    case logcatInfo.logPriority.WARN_VALUE:
      result = 3;
      break;
    case logcatInfo.logPriority.ERROR_VALUE:
      result = 4;
      break;
    case logcatInfo.logPriority.FATAL_VALUE:
      result = 5;
      break;
    }
    return result;
  }

  /**
   * convert dmesg to integer
   * @param type
   * @return integer
   */
  public static int convertDmesgType(int type) {
    int result = 0;

    switch (type) {
    case dmesgInfo.dmesgLevel.DEBUG_VALUE:
      result = 0;
      break;
    case dmesgInfo.dmesgLevel.INFORMATION_VALUE:
      result = 1;
      break;
    case dmesgInfo.dmesgLevel.NOTICE_VALUE:
      result = 2;
      break;
    case dmesgInfo.dmesgLevel.WARNING_VALUE:
      result = 3;
      break;
    case dmesgInfo.dmesgLevel.ALERT_VALUE:
      result = 4;
      break;
    case dmesgInfo.dmesgLevel.EMERGENCY_VALUE:
      result = 5;
      break;
    case dmesgInfo.dmesgLevel.ERROR_VALUE:
      result = 6;
      break;
    case dmesgInfo.dmesgLevel.CRITICAL_VALUE:
      result = 7;
      break;
    }
    return result;
  }

  /**
   * convert Integer to IpcAction type
   * @param integer
   * @return Logcat
   */
  public static ipcAction convertLocToType(int loc) {
    ipcAction type = ipcAction.LOGCAT_MAIN;
    switch (loc) {
    case 0:
      type = ipcAction.LOGCAT_MAIN;
      break;
    case 1:
      type = ipcAction.LOGCAT_SYSTEM;
      break;
    case 2:
      type = ipcAction.LOGCAT_EVENT;
      break;
    case 3:
      type = ipcAction.LOGCAT_RADIO;
      break;
    case 4:
      type = ipcAction.DMESG;
      break;
    default:
      break;
    }

    return type;
  }

  /**
   * convert IpcAction into integer
   * @param IpcAction
   * @return integer
   */
  public static int convertTypeToLoc(ipcAction type) {
    int loc = 0;
    switch (type) {
    case LOGCAT_MAIN:
      loc = 0;
      break;
    case LOGCAT_SYSTEM:
      loc = 1;
      break;
    case LOGCAT_EVENT:
      loc = 2;
      break;
    case LOGCAT_RADIO:
      loc = 3;
      break;
    case DMESG:
      loc = 4;
      break;
    default:
      break;
    }
    return loc;
  }

  /**
   * get priority string by logPriority
   * @param logPriority
   * @return String
   */
  public static String getLogprority(logPriority priority) {
    String result = "UNKNOWN";
    switch (priority.getNumber()) {
    case logcatInfo.logPriority.SILENT_VALUE:
      result = "SILENT";
      break;
    case logcatInfo.logPriority.DEFAULT_VALUE:
      result = "DEFAULT";
      break;
    case logcatInfo.logPriority.VERBOSE_VALUE:
      result = "VERBOSE";
      break;
    case logcatInfo.logPriority.WARN_VALUE:
      result = "WARNING";
      break;
    case logcatInfo.logPriority.INFO_VALUE:
      result = "INFORMATION";
      break;
    case logcatInfo.logPriority.FATAL_VALUE:
      result = "FATAL";
      break;
    case logcatInfo.logPriority.ERROR_VALUE:
      result = "ERROR";
      break;
    case logcatInfo.logPriority.DEBUG_VALUE:
      result = "DEBUG";
      break;
    }
    return result;
  }
  
  /**
   * get dmesg by dmesgLevel
   * @param dmesgLevel
   * @return String
   */
  public static String getDmesgLevel(dmesgLevel level) {
    String result = "UNKNOWN";
    switch (level.getNumber()) {
    case dmesgInfo.dmesgLevel.DEBUG_VALUE:
      result = "DEBUG";
      break;
    case dmesgInfo.dmesgLevel.INFORMATION_VALUE:
      result = "INFORMATION";
      break;
    case dmesgInfo.dmesgLevel.NOTICE_VALUE:
      result = "NOTICE";
      break;
    case dmesgInfo.dmesgLevel.WARNING_VALUE:
      result = "WARNING";
      break;
    case dmesgInfo.dmesgLevel.EMERGENCY_VALUE:
      result = "EMERGENCY";
      break;
    case dmesgInfo.dmesgLevel.ERROR_VALUE:
      result = "ERROR";
      break;
    case dmesgInfo.dmesgLevel.ALERT_VALUE:
      result = "ALERT";
      break;
    case dmesgInfo.dmesgLevel.CRITICAL_VALUE:
      result = "CRITICAL";
      break;
    }
    return result;
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
