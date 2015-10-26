package com.eolwral.osmonitor.util;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;

import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.core.connectionStatus;
import com.eolwral.osmonitor.core.connectionType;
import com.eolwral.osmonitor.core.dmesgLevel;
import com.eolwral.osmonitor.core.logPriority;
import com.eolwral.osmonitor.core.processStatus;
import com.eolwral.osmonitor.ipc.ipcCategory;
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
   * @param byte
   * @return status (by char)
   */
  public static String getSatusString(byte status) {
    if (resource == null)
      return "?";
      
    switch (status) {
    case processStatus.Running:
      return resource.getText(R.string.ui_process_status_running).toString();
    case processStatus.Sleep:
      return resource.getText(R.string.ui_process_status_sleep).toString();
    case processStatus.Stopped:
      return resource.getText(R.string.ui_process_status_stop).toString();
    case processStatus.Page:
    case processStatus.Disk:
      return resource.getText(R.string.ui_process_status_waitio).toString();
    case processStatus.Zombie:
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
    case logPriority.DEBUG:
      return 0;
    case logPriority.VERBOSE:
      return 1;
    case logPriority.INFO:
      return 2;
    case logPriority.WARN:
      return 3;
    case logPriority.ERROR:
      return 4;
    case logPriority.FATAL:
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
    case dmesgLevel.DEBUG:
      return 0;
    case dmesgLevel.INFORMATION:
      return 1;
    case dmesgLevel.NOTICE:
      return 2;
    case dmesgLevel.WARNING:
      return 3;
    case dmesgLevel.ALERT:
      return 4;
    case dmesgLevel.EMERGENCY:
      return 5;
    case dmesgLevel.ERROR:
      return 6;
    case dmesgLevel.CRITICAL:
      return 7;
    }
    return 0;
  }

  /**
   * convert Integer to IpcAction type
   * @param integer
   * @return Logcat
   */
  public static byte convertLocToType(int loc) {
    switch (loc) {
    case 0:
      return ipcCategory.LOGCAT_MAIN;
    case 1:
      return ipcCategory.LOGCAT_SYSTEM;
    case 2:
      return ipcCategory.LOGCAT_EVENT;
    case 3:
      return ipcCategory.LOGCAT_RADIO;
    case 4:
      return ipcCategory.DMESG;
    }

    return ipcCategory.LOGCAT_MAIN;
  }

  /**
   * convert IpcAction into integer
   * @param byte
   * @return integer
   */
  public static int convertTypeToLoc(byte type) {
    switch (type) {
    case ipcCategory.LOGCAT_MAIN:
      return 0;
    case ipcCategory.LOGCAT_SYSTEM:
      return 1;
    case ipcCategory.LOGCAT_EVENT:
      return 2;
    case ipcCategory.LOGCAT_RADIO:
      return 3;
    case ipcCategory.DMESG:
      return 4;
    }
    return 0;
  }

  /**
   * get priority string by logPriority
   * @param byte
   * @return String
   */
  public static String getLogpriority(byte priority) {
    switch (priority) {
    case logPriority.SILENT:
      return "SILENT";
    case logPriority.DEFAULT:
      return "DEFAULT";
    case logPriority.VERBOSE:
      return "VERBOSE";
    case logPriority.WARN:
      return "WARNING";
    case logPriority.INFO:
      return "INFORMATION";
    case logPriority.FATAL:
      return "FATAL";
    case logPriority.ERROR:
      return "ERROR";
    case logPriority.DEBUG:
      return "DEBUG";
    }
    return "UNKNOWN";
  }
  
  /**
   * get dmesg by dmesgLevel
   * @param byte
   * @return String
   */
  public static String getDmesgLevel(byte level) {
    switch (level) {
    case dmesgLevel.DEBUG:
      return "DEBUG";
    case dmesgLevel.INFORMATION:
      return "INFORMATION";
    case dmesgLevel.NOTICE:
      return "NOTICE";
    case dmesgLevel.WARNING:
      return "WARNING";
    case dmesgLevel.EMERGENCY:
      return "EMERGENCY";
    case dmesgLevel.ERROR:
      return "ERROR";
    case dmesgLevel.ALERT:
      return "ALERT";
    case dmesgLevel.CRITICAL:
      return "CRITICAL";
    }
    return "UNKNOWN";
  }

  /**
   * get color by Logcat type
   * @param byte
   * @return integer color
   */
  public static int getLogcatColor(byte priority) {
    switch (priority) {
    case logPriority.WARN:
      return settings.getLogcatWarningColor();
    case logPriority.INFO:
      return settings.getLogcatInfoColor();
    case logPriority.FATAL:
      return settings.getLogcatFatalColor();
    case logPriority.ERROR:
      return settings.getLogcatErrorColor();
    case logPriority.DEBUG:
      return settings.getLogcatDebugColor();
    case logPriority.SILENT:
    case logPriority.UNKNOWN:
    case logPriority.DEFAULT:
    case logPriority.VERBOSE:
      return settings.getLogcatVerboseColor();
    }
    return settings.getLogcatVerboseColor();
  }

  /**
   * get short tag for Logcat priority
   * @param byte
   * @return String short tag
   */
  public static String getLogcatTag(byte priority) {
    switch (priority) {
    case logPriority.SILENT:
    case logPriority.UNKNOWN:
    case logPriority.DEFAULT:
      return "S";
    case logPriority.VERBOSE:
      return "V";
    case logPriority.WARN:
      return "W";
    case logPriority.INFO:
      return "I";
    case logPriority.FATAL:
      return "F";
    case logPriority.ERROR:
      return "E";
    case logPriority.DEBUG:
      return "D";
    }
    return "S";
  }
  
  /**
   * get connection type by connectionType
   * @param byte
   * @return String connection type 
   */
  public static String getConnectionType(byte type) {
    switch (type) {
    case connectionType.TCPv4:
      return "TCP4";
    case connectionType.TCPv6:
      return "TCP6";
    case connectionType.UDPv4:
      return "UDP4";
    case connectionType.UDPv6:
      return "UDP6";
    case connectionType.RAWv4:
      return "RAW4";
    case connectionType.RAWv6:
      return "RAW6";
    }
    return "????";
  }
  
  /**
   * get connection status by connectionStatus
   * @param byte
   * @return connection status
   */
  public static String getConnectionStatus(byte status) {
    switch (status) {
    case connectionStatus.CLOSE:
      return "CLOSE";
    case connectionStatus.CLOSE_WAIT:
      return "CLOSE_WAIT";
    case connectionStatus.CLOSING:
      return "CLOSING";
    case connectionStatus.ESTABLISHED:
      return "ESTABLISHED";
    case connectionStatus.FIN_WAIT1:
      return "FIN_WAIT1";
    case connectionStatus.FIN_WAIT2:
      return "FIN_WAIT2";
    case connectionStatus.LAST_ACK:
      return "LAST_ACK";
    case connectionStatus.LISTEN:
      return "LISTEN";
    case connectionStatus.SYN_RECV:
      return "SYN_RECV";
    case connectionStatus.SYN_SENT:
      return "SYN_SENT";
    case connectionStatus.TIME_WAIT:
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


  /**
   * For custom purposes. Not used by ColorPickerPreference
   * 
   * @param color
   * @author Charles Rosaaen
   * @return A string representing the hex value of color, without the alpha
   *         value
   */
  public static String convertToRGB(int color) {
    String red = Integer.toHexString(Color.red(color));
    String green = Integer.toHexString(Color.green(color));
    String blue = Integer.toHexString(Color.blue(color));

    if (red.length() == 1) {
      red = "0" + red;
    }

    if (green.length() == 1) {
      green = "0" + green;
    }

    if (blue.length() == 1) {
      blue = "0" + blue;
    }

    return "#" + red + green + blue;
  }


  /**
   * convert data as Usage
   * 
   * @param data
   * @return a string of float value
   */
  @SuppressLint("DefaultLocale")
  public static String convertToUsage(float data) {
    return String.format("%.1f", data);
  }

  /**
   * convert string as Integer
   * 
   * @param string
   * @return int
   */
  public static int convertToInt(String value) {
    try {
      return Integer.parseInt(value);
    } catch (Exception e) {
    }
    return 0;
  }

  /**
   * remove string from array , if it can't be converted to int
   * 
   * @param string
   *          []
   * @return string []
   */
  public static String[] eraseNonIntegarString(String[] data) {
    ArrayList<String> checked = new ArrayList<String>();
    for (int index = 0; index < data.length; index++) {
      if (convertToInt(data[index]) != 0)
        checked.add(data[index]);
    }
    return checked.toArray(new String[checked.size()]);
  }

  /**
   * remove empty string from array
   * 
   * @param string
   *          []
   * @return string []
   */
  public static String[] eraseEmptyString(String[] data) {
    ArrayList<String> checked = new ArrayList<String>();
    for (int index = 0; index < data.length; index++) {
      if (!data[index].trim().isEmpty())
        checked.add(data[index]);
    }
    return checked.toArray(new String[checked.size()]);
  }


  /**
   * convert data as memory
   * 
   * @param data
   * @return a string with correct format
   *
   * Reference:
   * http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
   */
  @SuppressLint("DefaultLocale")
  public static String convertToSize(long data, boolean si) {
    int unit = si ? 1000 : 1024;
    if (data < unit)
      return data + " B";
    int exp = (int) (Math.log(data) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
    return String.format("%.1f %sB", data / Math.pow(unit, exp), pre);
  }

}
