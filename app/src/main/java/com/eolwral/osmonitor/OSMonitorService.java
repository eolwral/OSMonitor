package com.eolwral.osmonitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.eolwral.osmonitor.core.cpuInfo;
import com.eolwral.osmonitor.core.cpuInfoList;
import com.eolwral.osmonitor.core.networkInfo;
import com.eolwral.osmonitor.core.networkInfoList;
import com.eolwral.osmonitor.core.osInfo;
import com.eolwral.osmonitor.core.processInfo;
import com.eolwral.osmonitor.core.processInfoList;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.ipc.IpcService.ipcClientListener;
import com.eolwral.osmonitor.ipc.ipcCategory;
import com.eolwral.osmonitor.ipc.ipcData;
import com.eolwral.osmonitor.ipc.ipcMessage;
import com.eolwral.osmonitor.settings.Settings;
import com.eolwral.osmonitor.settings.Settings.NotificationType;
import com.eolwral.osmonitor.settings.Settings.StatusBarColor;
import com.eolwral.osmonitor.util.CoreUtil;
import com.eolwral.osmonitor.util.ProcessUtil;
import com.eolwral.osmonitor.util.UserInterfaceUtil;

import java.nio.ByteBuffer;
import java.util.Locale;

public class OSMonitorService extends Service implements ipcClientListener {
  private static final int NOTIFYID = 20100811;
  private IpcService ipcService = null;
  private int UpdateInterval = 2;

  private boolean isRegistered = false;
  private NotificationManager nManager = null;
  private NotificationCompat.Builder nBuilder = null;

  // process
  private int iconColor = 0;
  private int fontColor = 0;
  private boolean isSetTop = false;
  private float cpuUsage = 0;
  private float ioWaitUsage = 0;
  private float[] topUsage = new float[3];
  private String[] topProcess = new String[3];

  // memory
  private long memoryTotal = 0;
  private long memoryFree = 0;

  // battery
  private boolean useCelsius = false;
  private int battLevel = 0; // percentage value or -1 for unknown
  private int temperature = 0;

  // network
  private long trafficOut = 0;
  private long trafficIn = 0;

  // private
  private ProcessUtil infoHelper = null;
  private Settings settings = null;
  private int notificationType = NotificationType.MEMORY_BATTERY;

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {

    super.onCreate();

    settings = Settings.getInstance(this);

    IpcService.Initialize(this);
    ipcService = IpcService.getInstance();

    refreshSettings();
    initializeNotification();

    if (settings.isEnableCPUMeter()) {
      infoHelper = ProcessUtil.getInstance(this, false);
      initService();
    }
  }

  private void refreshSettings() {

    // use notification for meta color
    if (CoreUtil.isLollipop()) {
      iconColor = R.drawable.ic_cpu_graph_meta;
    }
    else {
      switch (settings.getCPUMeterColor()) {
        case StatusBarColor.GREEN:
          iconColor = R.drawable.ic_cpu_graph_green;
          break;
        case StatusBarColor.BLUE:
          iconColor = R.drawable.ic_cpu_graph_blue;
          break;
        }
    }


    notificationType = settings.getNotificationType();
    fontColor = settings.getNotificationFontColor();
    isSetTop = settings.isNotificationOnTop();
    useCelsius = settings.isUseCelsius();
    
    // recreate connection type when refreshing settings
    ipcService.createConnection();
  }

  @Override
  public void onDestroy() {
    endNotification();
    endService();
    if (!settings.getSessionValue().equals("Non-Exit"))
      android.os.Process.killProcess(android.os.Process.myPid());
    super.onDestroy();
  }

  private void endNotification() {
    nManager.cancel(NOTIFYID);
    stopForeground(true);
  }

  private void initializeNotification() {

    Intent notificationIntent = new Intent(this, OSMonitor.class);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent contentIntent = PendingIntent.getActivity(
        this.getBaseContext(), 0, notificationIntent, 0);

    nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    nBuilder = new NotificationCompat.Builder(this);
    nBuilder.setContentTitle(getResources().getText(R.string.ui_appname));
    nBuilder.setContentText(getResources().getText(R.string.ui_shortcut_detail));
    nBuilder.setOnlyAlertOnce(true);
    nBuilder.setOngoing(true);
    nBuilder.setContentIntent(contentIntent);
    nBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);

    // Use new style icon for Lollipop
    if (CoreUtil.isLollipop())
      nBuilder.setSmallIcon(R.drawable.ic_stat_notify);
    else
      nBuilder.setSmallIcon(R.drawable.ic_launcher);

    if (isSetTop)
      nBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

    Notification osNotification = nBuilder.build();
    nManager.notify(NOTIFYID, osNotification);

    // set foreground to avoid recycling
    startForeground(NOTIFYID, osNotification);

  }

  private BroadcastReceiver mReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {

      if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        goSleep();
      else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
        wakeUp();
    }
  };

  private void registerScreenEvent() {
    IntentFilter filterScreenON = new IntentFilter(Intent.ACTION_SCREEN_ON);
    registerReceiver(mReceiver, filterScreenON);

    IntentFilter filterScreenOFF = new IntentFilter(Intent.ACTION_SCREEN_OFF);
    registerReceiver(mReceiver, filterScreenOFF);
  }

  private void initService() {
    if (!isRegistered) {
      registerScreenEvent();
      isRegistered = true;
    }

    wakeUp();
  }

  private void endService() {
    if (isRegistered) {
      unregisterReceiver(mReceiver);
      isRegistered = false;
    }

    goSleep();
  }

  private byte[] getReceiveDataType() {
    byte newCommand[] = { ipcCategory.PROCESS, ipcCategory.CPU, ipcCategory.OS };
    switch (notificationType) {
    case NotificationType.MEMORY_BATTERY:
      newCommand = new byte[] { ipcCategory.PROCESS, ipcCategory.OS };
      break;
    case NotificationType.MEMORY_DISKIO:
      newCommand = new byte[] { ipcCategory.PROCESS, ipcCategory.CPU, ipcCategory.OS };
      break;
    case NotificationType.BATTERY_DISKIO:
      newCommand = new byte[] { ipcCategory.PROCESS, ipcCategory.CPU };
      break;
    case NotificationType.NETWORKIO:
      newCommand = new byte[] { ipcCategory.PROCESS, ipcCategory.NETWORK };
      break;
    }
    return newCommand;
  }

  private void wakeUp() {
    UpdateInterval = settings.getInterval();
    byte newCommand[] = getReceiveDataType();
    ipcService.removeRequest(this);
    ipcService.addRequest(newCommand, 0, this);
    startBatteryMonitor();
  }

  private void goSleep() {
    ipcService.removeRequest(this);
    stopBatteryMonitor();
  }

  private void startBatteryMonitor() {
    if (notificationType != NotificationType.MEMORY_BATTERY
        && notificationType != NotificationType.BATTERY_DISKIO)
      return;

    if (!isRegisterBattery) {
      IntentFilter battFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
      registerReceiver(battReceiver, battFilter);
      isRegisterBattery = true;
    }
  }

  private void stopBatteryMonitor() {
    if (isRegisterBattery) {
      unregisterReceiver(battReceiver);
      isRegisterBattery = false;
    }
  }

  private boolean isRegisterBattery = false;

  private BroadcastReceiver battReceiver = new BroadcastReceiver() {
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
  public void onRecvData(byte [] result) {

    if (result == null) {
      byte newCommand[] = getReceiveDataType();
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

    try {
      ipcMessage ipcMessageResult = ipcMessage.getRootAsipcMessage(ByteBuffer.wrap(result));

      for (int index = 0; index < ipcMessageResult.dataLength(); index++) {
        ipcData rawData = ipcMessageResult.data(index);

        if (rawData.category() == ipcCategory.OS)
          extractOSInfo(rawData);
        else if (rawData.category() == ipcCategory.CPU)
          extractCPUInfo(rawData);
        else if (rawData.category() == ipcCategory.NETWORK)
          extractNetworkInfo(rawData);
        else if (rawData.category() == ipcCategory.PROCESS)
          extractProcessInfo(rawData);
      }

    } catch (Exception e) {}

    refreshNotification();

    // send command again
    byte newCommand[] = getReceiveDataType();
    ipcService.addRequest(newCommand, UpdateInterval, this);
  }

  private void extractProcessInfo(ipcData rawData)
  {

    processInfoList infoList = processInfoList.getRootAsprocessInfoList(rawData.payloadAsByteBuffer().asReadOnlyBuffer());
    for (int count = 0; count < infoList.listLength(); count++) {
      processInfo item = infoList.list(count);
      cpuUsage += item.cpuUsage();

      for (int check = 0; check < 3; check++) {

        if (topUsage[check] >= item.cpuUsage())
          continue;

        // keep top 3 usage
        for (int push = 2; push > check; push--) {
          topUsage[push] = topUsage[push - 1];
          topProcess[push] = topProcess[push - 1];
        }

        topUsage[check] = item.cpuUsage();
        topProcess[check] = infoHelper.getPackageName(item.name());

        // if name is not ready, use process name
        if (topProcess[check] == null)
          topProcess[check] = item.name();

        // check cached status
        if (infoHelper.checkPackageInformation(item.name()))
          break;

        // prepare to do cache
        if (item.name().toLowerCase(Locale.getDefault()).contains("osmcore"))
          infoHelper.doCacheInfo(android.os.Process.myUid(), item.owner(), item.name());
        else
          infoHelper.doCacheInfo(item.uid(), item.owner(), item.name());
        break;
      }

    }
  }

  private void extractNetworkInfo(ipcData rawData)
  {
    // process processInfo
    trafficOut = 0;
    trafficIn = 0;
    networkInfoList nwInfoList = networkInfoList.getRootAsnetworkInfoList(rawData.payloadAsByteBuffer().asReadOnlyBuffer());
    for (int count = 0; count < nwInfoList.listLength(); count++) {
      networkInfo nwInfo = nwInfoList.list(count);
      trafficOut += nwInfo.transUsage();
      trafficIn += nwInfo.recvUsage();
    }
  }

  private void extractCPUInfo(ipcData rawData)
  {
    cpuInfoList infoList = cpuInfoList.getRootAscpuInfoList(rawData.payloadAsByteBuffer().asReadOnlyBuffer());
    for(int count = 0; count < infoList.listLength(); count++) {
      cpuInfo info = infoList.list(count);
      ioWaitUsage = info.ioUtilization();
      break;
    }
  }

  private void extractOSInfo(ipcData rawData)
  {
    osInfo info = osInfo.getRootAsosInfo(rawData.payloadAsByteBuffer().asReadOnlyBuffer());
    memoryFree = info.freeMemory() + info.bufferedMemory() + info.cachedMemory();
    memoryTotal = info.totalMemory();
  }

  private String getBatteryInfo() {
    String info = "";
    if (useCelsius)
      info = battLevel + "% (" + temperature / 10 + "\u2103)";
    else
      info = battLevel + "% (" + ((int) temperature / 10 * 9 / 5 + 32) + "\u2109)";
    return info;
  }

  private void refreshNotification() {

    Notification osNotification = nBuilder.build();
    osNotification.contentView = new RemoteViews(getPackageName(),
        R.layout.ui_notification);

    // set contentIntent to fix
    // "android.app.RemoteServiceException: Bad notification posted from package"
    Intent notificationIntent = new Intent(this, OSMonitor.class);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
    osNotification.contentIntent = PendingIntent.getActivity( this.getBaseContext(), 0, notificationIntent, 0);

    osNotification.contentView.setTextViewText(R.id.notification_cpu, "CPU: "
                                               + UserInterfaceUtil.convertToUsage(cpuUsage) + "%");
    osNotification.contentView.setProgressBar(R.id.notification_cpu_bar, 100, (int) cpuUsage, false);

    switch (notificationType) {

    case NotificationType.MEMORY_BATTERY:
      osNotification.contentView.setTextViewText(R.id.notification_1nd, "MEM: "
                                                 + UserInterfaceUtil.convertToSize(memoryFree, true));
      osNotification.contentView.setTextViewText(R.id.notification_2nd, "BAT: " + getBatteryInfo());
      osNotification.contentView.setProgressBar(R.id.notification_1nd_bar, (int) memoryTotal,
                                                 (int) (memoryTotal - memoryFree), false);
      osNotification.contentView.setProgressBar(R.id.notification_2nd_bar, 100, (int) battLevel, false);
      break;

    case NotificationType.MEMORY_DISKIO:
      osNotification.contentView.setTextViewText(R.id.notification_1nd, "MEM: "
                                                 + UserInterfaceUtil.convertToSize(memoryFree, true));
      osNotification.contentView.setTextViewText(R.id.notification_2nd, "IO: "
                                                 + UserInterfaceUtil.convertToUsage(ioWaitUsage) + "%");
      osNotification.contentView.setProgressBar(R.id.notification_1nd_bar, (int) memoryTotal,
                                                 (int) (memoryTotal - memoryFree), false);
      osNotification.contentView.setProgressBar(R.id.notification_2nd_bar, 100, (int) ioWaitUsage, false);
      break;

    case NotificationType.BATTERY_DISKIO:
      osNotification.contentView.setTextViewText(R.id.notification_1nd, "BAT: " + getBatteryInfo());
      osNotification.contentView.setTextViewText(R.id.notification_2nd, "IO: "
                                                 + UserInterfaceUtil.convertToUsage(ioWaitUsage) + "%");
      osNotification.contentView.setProgressBar(R.id.notification_1nd_bar, 100, (int) battLevel, false);
      osNotification.contentView.setProgressBar(R.id.notification_2nd_bar, 100, (int) ioWaitUsage, false);
      break;

    case NotificationType.NETWORKIO:
      osNotification.contentView.setTextViewText(R.id.notification_1nd, "OUT: "
                                                + UserInterfaceUtil.convertToSize(trafficOut, true));
      osNotification.contentView.setTextViewText(R.id.notification_2nd, "IN: "
                                                + UserInterfaceUtil.convertToSize(trafficIn, true));
      osNotification.contentView.setProgressBar(R.id.notification_1nd_bar,
                                                (int) (trafficOut + trafficIn), (int) trafficOut, false);
      osNotification.contentView.setProgressBar(R.id.notification_2nd_bar,
                                                (int) (trafficOut + trafficIn), (int) trafficIn, false);
      break;
    }

    osNotification.contentView.setTextViewText(R.id.notification_top1st,
        UserInterfaceUtil.convertToUsage(topUsage[0]) + "% " + topProcess[0]);
    osNotification.contentView.setTextViewText(R.id.notification_top2nd,
        UserInterfaceUtil.convertToUsage(topUsage[1]) + "% " + topProcess[1]);
    osNotification.contentView.setTextViewText(R.id.notification_top3nd,
        UserInterfaceUtil.convertToUsage(topUsage[2]) + "% " + topProcess[2]);

    // use custom color
    if (fontColor != -1) {
      osNotification.contentView.setTextColor(R.id.notification_2nd, fontColor);
      osNotification.contentView.setTextColor(R.id.notification_1nd, fontColor);
      osNotification.contentView.setTextColor(R.id.notification_cpu, fontColor);
      osNotification.contentView.setTextColor(R.id.notification_top1st, fontColor);
      osNotification.contentView.setTextColor(R.id.notification_top2nd, fontColor);
      osNotification.contentView.setTextColor(R.id.notification_top3nd, fontColor);
    }

    osNotification.icon = iconColor;
    if (cpuUsage < 20)
      osNotification.iconLevel = 1;
    else if (cpuUsage < 40)
      osNotification.iconLevel = 2;
    else if (cpuUsage < 60)
      osNotification.iconLevel = 3;
    else if (cpuUsage < 80)
      osNotification.iconLevel = 4;
    else if (cpuUsage < 100)
      osNotification.iconLevel = 5;
    else
      osNotification.iconLevel = 6;

    nManager.notify(NOTIFYID, osNotification);
  }

}
