package com.eolwral.osmonitor.util;

import android.app.Activity;
import android.content.res.Resources;

import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.core.DmesgInfo.dmesgInfo;
import com.eolwral.osmonitor.core.LogcatInfo.logcatInfo;
import com.eolwral.osmonitor.core.ProcessInfo.processInfo;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcAction;

public class UserInterfaceUtil {

  /**
   * get status by processStatus
   * @param processStatus
   * @return status (by char)
   */
  public static String getSatusString(Activity activity, processInfo.processStatus status) {
    
    final Resources res = activity.getResources();
    String result = res.getText(R.string.ui_process_status_unknown).toString();
    
    switch (status.getNumber()) {
    case processInfo.processStatus.Running_VALUE:
      result = res.getText(R.string.ui_process_status_running).toString();
      break;
    case processInfo.processStatus.Sleep_VALUE:
      result = res.getText(R.string.ui_process_status_sleep).toString();
      break;
    case processInfo.processStatus.Stopped_VALUE:
      result = res.getText(R.string.ui_process_status_stop).toString();
      break;
    case processInfo.processStatus.Page_VALUE:
    case processInfo.processStatus.Disk_VALUE:
      result = res.getText(R.string.ui_process_status_waitio).toString();
      break;
    case processInfo.processStatus.Zombie_VALUE:
      result = res.getText(R.string.ui_process_status_zombie).toString();
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



}
