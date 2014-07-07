package com.eolwral.osmonitor;

import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.settings.Settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver{

	private Settings setting = null;
	
 	@Override 
	public void onReceive(Context context, Intent intent) {

 	  setting = Settings.getInstance(context);

 	  if(setting.isEnableAutoStart() && ( setting.isEnableCPUMeter() || setting.isAddShortCut()))
 	    context.startService(new Intent(context, OSMonitorService.class));

 	  if(setting.isSetCPU() && setting.isRoot()) {
 	    IpcService.Initialize(context);
 	    prepareSetCPU(); // fix ANR when booting
 	  }

 	  return;
	}

	private void prepareSetCPU() {
		new Thread() {
			@Override
			public void run() { setCPU(); }
		}.start();
	}

	private void setCPU() {
		IpcService.getInstance().forceConnect();
		
		String cpudata = setting.getCPUSettings();
		String [] cpu = cpudata.split(";");
		for(int index = 0; index < cpu.length; index++) {
			
			String [] value = cpu[index].split(",");
			if(value.length < 4)
				continue;
			
			int processorNum = Integer.parseInt(value[0]);
			long maxFreq = Long.parseLong(value[1]);
			long minFreq = Long.parseLong(value[2]);
			String gov = value[3];
			int enable = Integer.parseInt(value[4]);
			
			IpcService.getInstance().setCPUStatus(processorNum, 1);
			IpcService.getInstance().setCPUMaxFreq(processorNum, maxFreq);
			IpcService.getInstance().setCPUMinFreq(processorNum, minFreq);
			IpcService.getInstance().setCPUGov(processorNum, gov);
			IpcService.getInstance().setCPUStatus(processorNum, enable);
		}
		IpcService.getInstance().disconnect();
	}
}
