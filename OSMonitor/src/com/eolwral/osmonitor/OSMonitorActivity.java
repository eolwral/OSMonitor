package com.eolwral.osmonitor;

import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;

/**
 * Fix Crash when pressing the options menu hardware button on Android 4.1 device
 * https://code.google.com/p/android/issues/detail?id=78154
 */

public class OSMonitorActivity extends ActionBarActivity {

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {

    boolean isLGE = false;
    if (keyCode == KeyEvent.KEYCODE_MENU ) { 
      if ("LGE".equalsIgnoreCase(Build.BRAND))
        isLGE = true;
      if (Build.VERSION.SDK_INT <= 16 && "LGE".compareTo(Build.MANUFACTURER) == 0)
        isLGE = true;
    }

    if (isLGE)
      return true;
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {

    boolean isLGE = false;
    if (keyCode == KeyEvent.KEYCODE_MENU) {
      if ("LGE".equalsIgnoreCase(Build.BRAND))
        isLGE = true;
      if (Build.VERSION.SDK_INT <= 16 && "LGE".compareTo(Build.MANUFACTURER) == 0)
        isLGE = true;
    }

    if (isLGE) {
      openOptionsMenu();
      return true;
    }

    return super.onKeyUp(keyCode, event);
  }

}
