package com.eolwral.osmonitor.settings;

import android.content.Context;

public class Settings {

	// singleton
	private static Settings instance = null;
	
	private SettingsHelper helper = null;
	
	public final static String PREFRENCE_INTERVAL = "id_preference_interval";
	public final static String PREFERENCE_SHORTCUT = "id_preference_shortcut";
	public final static String PREFERENCE_CPUUSAGE = "id_preference_cpuusage";
	public final static String PREFERENCE_COLOR = "id_preference_color";
	public final static String PREFERENCE_TEMPVALUE = "id_preference_tempvalue";
	public final static String PREFERENCE_AUTOSTART = "id_preference_autostart";
	public final static String PREFERENCE_MAP = "id_preference_map";
	public final static String PREFERENCE_EXPERTMODE = "id_preference_expertmode";
	public final static String PREFERENCE_ROOT = "id_preference_root";
	public final static String PREFERENCE_SETCPU = "id_preference_setcpu";
	public final static String PREFERENCE_SETCPUDATA = "id_preference_setcpu_data";
	public final static String PREFERENCE_SORTTYPE = "id_preference_sorttype";
	public final static String PREFERENCE_NOTIFICATION_COLOR = "id_preference_notification_fontcolor";
	public final static String PREFERENCE_NOTIFICATION_TOP = "id_preference_notification_top";
	public final static String PREFERENCE_LOGCAT_FORMAT = "id_preference_logcat_format";
	
	/**
	 * get an instance for settings 
	 * @param context
	 * @return settings object
	 */
	public static Settings getInstance(Context context) {
		if (instance == null) {
			instance = new Settings(context);
		}
		return instance;
	}
	/**
	 * construct 
	 * @param context
	 */
	private  Settings(Context context) {
		helper = new SettingsHelper(context);
	}

	/**
	 * get update interval
	 * @return interval (seconds)
	 */
	public int getInterval() {
		String interval = helper.getString(PREFRENCE_INTERVAL, "2");
		return Integer.parseInt(interval);
	}
	
	/**
	 * enable CPU meter  
	 * @return true == enable, false == disable
	 */
	public boolean isEnableCPUMeter() {
		return helper.getBoolean(PREFERENCE_CPUUSAGE, false);
	}
	
	/**
	 * get color for CPU meter
	 * @return 1 == green, 2 == blue
	 */
	public int getCPUMeterColor() {
		String color = helper.getString(PREFERENCE_COLOR, "1");
		return Integer.parseInt(color);
	}
	
	/**
	 * start the CPU Meter when reboot
	 * @return true == yes, false == no
	 */
	public boolean isEnableAutoStart() {
		return helper.getBoolean(PREFERENCE_AUTOSTART, false);
	}    
	
	/**
	 * enable expert mode
	 * @return true == yes, false == no
	 */
	public boolean isUseExpertMode() {
		return helper.getBoolean(PREFERENCE_EXPERTMODE, false);
	} 
	
	/**
	 * grant root permission or not
	 * @return true == yes, false == no
	 */
	public boolean isRoot() {
		return helper.getBoolean(PREFERENCE_ROOT, false);
	}
	
	/**
	 * use Celsius
	 * @return true == yes, false == no
	 */
	public boolean isUseCelsius() {
		return helper.getBoolean(PREFERENCE_TEMPVALUE, false);
	}  

	/**
	 * set a security token
	 * @param token
	 */
	public void setToken(String token) {
		helper.setString("token", token);
	}
	
	/**
	 * get the security token
	 * @return token
	 */
	public String getToken() {
		if(helper.getString("token", "").length() == 0 )
			setToken(java.util.UUID.randomUUID().toString());
		return helper.getString("token", "");
	}
	
	/**
	 * get map type
	 * @return map
	 */
	public String getMapType() {
		return helper.getString(PREFERENCE_MAP, "GoogleMap");
	}
	
	/**
	 * set CPU on boot
	 * @return true == yes, false == no
	 */
	public boolean isSetCPU() {
		return helper.getBoolean(PREFERENCE_SETCPU, false);
	}
	
	/**
	 * get CPU settings
	 * @return settings
	 */
	public String getCPUSettings() {
		return helper.getString(PREFERENCE_SETCPUDATA, "");
	}
	
	/**
	 * show a shortcut on tje notification area
	 * @return true == yes, false == no
	 */
	public boolean isAddShortCut() {
		return helper.getBoolean(PREFERENCE_SHORTCUT, false);
	}
	
	/**
	 * save sort type
	 * @param type
	 */
	public void setSortType(String type) {
		helper.setString(PREFERENCE_SORTTYPE, type);
	}
	
	/**
	 * get sort type
	 * @return type
	 */
	public String getSortType() {
		return helper.getString(PREFERENCE_SORTTYPE, "");
	}

	/**
	 * set font color for notification
	 * @param color
	 */
	public void setNotificationFontColor(int color) {
	    helper.setInteger(PREFERENCE_NOTIFICATION_COLOR,  color);
	    return;
	}
	
	/**
	 * get font color for notification
	 * @return color
	 */
	public int getNotificationFontColor() {
	    return helper.getInteger(PREFERENCE_NOTIFICATION_COLOR,  -1);
	}
	
	/**
	 * keep notification on top
	 * @return true == yes, false == no
	 */
	public boolean isNotificationOnTop() {
		return helper.getBoolean(PREFERENCE_NOTIFICATION_TOP, false);
	}
	
	/**
	 * get logcat format 
	 * @return format
	 */
	public int getLogcatFormat() {
	    return helper.getInteger(PREFERENCE_LOGCAT_FORMAT,  0);
	}

}
