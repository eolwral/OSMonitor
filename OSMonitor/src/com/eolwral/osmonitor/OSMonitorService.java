package com.eolwral.osmonitor;

import com.eolwral.osmonitor.core.OsInfo.osInfo;
import com.eolwral.osmonitor.core.ProcessInfo.processInfo;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ipc.IpcService.ipcClientListener;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcAction;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcData;
import com.eolwral.osmonitor.ipc.IpcMessage.ipcMessage;
import com.eolwral.osmonitor.util.CommonUtil;
import com.eolwral.osmonitor.util.ProcessUtil;
import com.eolwral.osmonitor.util.Settings;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class OSMonitorService extends Service 
                              implements ipcClientListener 
{
	private static final int NOTIFYID = 20100811;
	private IpcService ipcService = null;
	private int UpdateInterval = 2; 
	 
	private boolean isRegistered = false;
	private NotificationManager nManager = null;
	private NotificationCompat.Builder nBuilder = null;
	
	// process   
	private int iconColor = 0;
	private float cpuUsage = 0;
	private float [] topUsage = new float[3];
	private String [] topProcess = new String[3];
	
	// memory
	private long memoryFree = 0;

	// battery
	private boolean useCelsius = false;
	private int battLevel = 0;  // percentage value or -1 for unknown
	private int temperature = 0;

	//private  
	private OSMonitorService self = null;
	private ProcessUtil infoHelper = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null; 
    }

    @Override
    public void onCreate() {
    	
    	super.onCreate();

		IpcService.Initialize(this);

   		Settings setting = new Settings(this);
   		if(!setting.enableCPUMeter()) {
   			IpcService.getInstance().disconnect();
			android.os.Process.killProcess(android.os.Process.myPid());
			return;
   		}
    	
    	self = this;
    	refreshSettings();
    	initializeNotification();

    	ipcService = IpcService.getInstance();
    	infoHelper = ProcessUtil.getInstance(self, false);

    	initService();
    }

	private void refreshSettings() {

   		Settings setting = new Settings(self);
    	switch(setting.chooseColor()) {
    	case 1:
    		iconColor = R.drawable.ic_cpu_graph_green;
    		break;
    	case 2:
    		iconColor = R.drawable.ic_cpu_graph_blue;
    		break;
    	}

    	useCelsius = setting.useCelsius();
	}
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	Disable();
    } 

	private void initializeNotification() { 
		
		Intent notificationIntent = new Intent(this, OSMonitor.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		nBuilder = new NotificationCompat.Builder(this);
    	nBuilder.setContentTitle(getResources().getText(R.string.ui_appname));
		nBuilder.setOnlyAlertOnce(true);
		nBuilder.setOngoing(true);
		nBuilder.setContentIntent(contentIntent);

		nManager.notify(NOTIFYID, nBuilder.build()); 
	}

    private BroadcastReceiver mReceiver = new BroadcastReceiver() 
    {
    	public void onReceive(Context context, Intent intent) {
    		
    		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) 
    			ipcService.removeRequest(self);
    		
    		
    		if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) 
    			wakeUp();
    	}
    };
    
    private void registerScreenEvent() {
		IntentFilter filterScreenON = new IntentFilter(Intent.ACTION_SCREEN_ON);
		registerReceiver(mReceiver, filterScreenON);

		IntentFilter filterScreenOFF = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mReceiver, filterScreenOFF);
	}
    
    private void initService()
    {
    	if(!isRegistered)
    	{
			registerScreenEvent();
    		isRegistered = true;
    	}

    	wakeUp();

    	startBatteryMonitor();
    }

	private void wakeUp() {
		ipcService.removeRequest(self);
		Settings settings = new Settings(this);
		UpdateInterval = settings.getInterval();
       	ipcAction newCommand[] = { ipcAction.PROCESS, ipcAction.OS };
    	ipcService.addRequest(newCommand, 0, self);
	} 
     
    private void Disable()
    {
    	if(isRegistered)
    	{
    		nManager.cancel(NOTIFYID);
    		unregisterReceiver(mReceiver);
    		isRegistered = false;
    	}
    	
    	ipcService.removeRequest(self);
    	ipcService.disconnect();
    	
    	stopBatteryMonitor();
    }
    
    private void startBatteryMonitor()
    {
    	IntentFilter battFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    	registerReceiver(battReceiver, battFilter);		        		
    }
    
    private void stopBatteryMonitor()
    {
    	unregisterReceiver(battReceiver);
    }
    
    private BroadcastReceiver battReceiver = new BroadcastReceiver() 
	{
		public void onReceive(Context context, Intent intent) {
			
			int rawlevel = intent.getIntExtra("level", -1);
			int scale = intent.getIntExtra("scale", -1);
			
			temperature = intent.getIntExtra("temperature", -1);

			if (rawlevel >= 0 && scale > 0) {
				battLevel = (rawlevel * 100) / scale;
			}
		}
	};

	@Override
	public void onRecvData(ipcMessage result) {
		
		if(result == null) {
			ipcAction newCommand[] = { ipcAction.PROCESS, ipcAction.OS };
			ipcService.addRequest(newCommand, UpdateInterval, this);
			return;
		}
		
		// gather data
		cpuUsage = 0;
		
		// empty
		for (int index = 0; index < 3; index++) {
			topUsage[index] = 0;
			topProcess[index] = "";
		}
		
		for (int index = 0; index < result.getDataCount(); index++) {
			try {
				ipcData rawData = result.getData(index);
				
				if (rawData.getAction() == ipcAction.OS){
					osInfo info = osInfo.parseFrom(rawData.getPayload(0));
					memoryFree = info.getFreeMemory()+info.getBufferedMemory()+info.getCachedMemory();
				}
				
				if (rawData.getAction() != ipcAction.PROCESS)
					continue;
				
				for (int count = 0; count < rawData.getPayloadCount(); count++) {
					processInfo item = processInfo.parseFrom(rawData.getPayload(count));
					cpuUsage += item.getCpuUsage();
					for(int check = 0; check < 3; check++) {
						if(topUsage[check] < item.getCpuUsage()) {
							
							for(int push = 2; push > check; push--) {
								topUsage[push] = topUsage[push-1];
								topProcess[push] = topProcess[push-1];
							}
							
							// check cached status
							if (!infoHelper.checkPackageInformation(item.getName()))
								infoHelper.doCacheInfo(item.getUid(), item.getOwner(), item.getName());
							topUsage[check] = item.getCpuUsage();
							topProcess[check] = infoHelper.getPackageName(item.getName());
							break;
						}
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		refreshNotification();
		
		// send command again
		ipcAction newCommand[] = { ipcAction.PROCESS, ipcAction.OS };
		ipcService.addRequest(newCommand, UpdateInterval, this);
	}

	private void refreshNotification() {
		
		if (useCelsius)
			nBuilder.setContentTitle("Mem: "+CommonUtil.convertLong(memoryFree)+", Bat:"+battLevel+"% ("+temperature/10+"¢XC)" );
		else
			nBuilder.setContentTitle("Mem: "+CommonUtil.convertLong(memoryFree)+", Bat:"+battLevel+"% ("+((int)temperature/10*9/5+32)+"¢XF)");
		
		nBuilder.setContentText("CPU: "+CommonUtil.convertFloat(cpuUsage) + "% [ " +
								CommonUtil.convertFloat(topUsage[0]) + "% "  + topProcess[0] + " ]");

		nBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(
								getResources().getString(R.string.ui_process_cpuusage) + " " + 
								CommonUtil.convertFloat(cpuUsage) + "%\n" +
							    " > " + CommonUtil.convertFloat(topUsage[0]) + "% "  + topProcess[0] + "\n" +
							    " > " + CommonUtil.convertFloat(topUsage[1]) + "% "  + topProcess[1] + "\n" +
							    " > " + CommonUtil.convertFloat(topUsage[2]) + "% "  + topProcess[2]));

		if (cpuUsage < 20)
			nBuilder.setSmallIcon(iconColor, 1);
		else if (cpuUsage < 40)
			nBuilder.setSmallIcon(iconColor, 2);
		else if (cpuUsage < 60)
			nBuilder.setSmallIcon(iconColor, 3);
		else if (cpuUsage < 80)
			nBuilder.setSmallIcon(iconColor, 4);
		else if (cpuUsage < 100)
			nBuilder.setSmallIcon(iconColor, 5);
		else
			nBuilder.setSmallIcon(iconColor, 6);
		
		nManager.notify(NOTIFYID, nBuilder.build());
	}
	
}
