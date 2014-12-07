package com.eolwral.osmonitor.util;

import android.app.Activity;
import android.content.res.Resources;

import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.core.ProcessInfo.processInfo;
import com.eolwral.osmonitor.core.ProcessInfo.processInfo.processStatus;

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

}
