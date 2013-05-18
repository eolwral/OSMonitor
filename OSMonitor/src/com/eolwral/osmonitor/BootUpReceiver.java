package com.eolwral.osmonitor;

import com.eolwral.osmonitor.util.Settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver{

 	@Override
	public void onReceive(Context context, Intent intent) {
 		Settings setting = new Settings(context);
        if(setting.enableAutoStart() && setting.enableCPUMeter())
        	context.startService(new Intent(context, OSMonitorService.class));
	}
}
