package com.eolwral.osmonitor.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Settings {
	
	private SharedPreferences preferenceMgr = null;
	final public static String PREFRENCE_INTERVAL = "id_preference_interval";
	final public static String PREFERENCE_SHORTCUT = "id_preference_shortcut";
	final public static String PREFERENCE_CPUUSAGE = "id_preference_cpuusage";
	final public static String PREFERENCE_COLOR = "id_preference_color";
	final public static String PREFERENCE_TEMPVALUE = "id_preference_tempvalue";
	final public static String PREFERENCE_AUTOSTART = "id_preference_autostart";
	final public static String PREFERENCE_MAP = "id_preference_map";
	final public static String PREFERENCE_EXPERTMODE = "id_preference_expertmode";
	final public static String PREFERENCE_ROOT = "id_preference_root";
	final public static String PREFERENCE_SETCPU = "id_preference_setcpu";
	final public static String PREFERENCE_SETCPUDATA = "id_preference_setcpu_data";
	final public static String PREFERENCE_SORTTYPE = "id_preference_sorttype";
	final public static String PREFERENCE_NOTIFICATION_COLOR = "id_preference_notification_fontcolor";
	final private static int MODE_MULTI_PROCESS = 4;
	 
	/**
	 * construct 
	 * @param context
	 */
	public Settings(Context context) {
		preferenceMgr =  context.getSharedPreferences(context.getPackageName() + "_preferences",
							MODE_MULTI_PROCESS);
	}

	/**
	 * get update interval
	 * @return interval (seconds)
	 */
	public int getInterval() {
		String interval = preferenceMgr.getString(PREFRENCE_INTERVAL, "2");
		return Integer.parseInt(interval);
	}
	
	/**
	 * enable CPU meter  
	 * @return true == enable, false == disable
	 */
	public boolean enableCPUMeter() {
		return preferenceMgr.getBoolean(PREFERENCE_CPUUSAGE, false);
	}
	
	/**
	 * get color for CPU meter
	 * @return 1 == green, 2 == blue
	 */
	public int chooseColor() {
		String color = preferenceMgr.getString(PREFERENCE_COLOR, "1");
		return Integer.parseInt(color);
	}
	
	/**
	 * start the CPU Meter when reboot
	 * @return true == yes, false == no
	 */
	public boolean enableAutoStart() {
		return preferenceMgr.getBoolean(PREFERENCE_AUTOSTART, false);
	}    
	
	/**
	 * enable expert mode
	 * @return true == yes, false == no
	 */
	public boolean useExpertMode() {
		return preferenceMgr.getBoolean(PREFERENCE_EXPERTMODE, false);
	} 
	
	/**
	 * grant root permission or not
	 * @return true == yes, false == no
	 */
	public boolean isRoot() {
		return preferenceMgr.getBoolean(PREFERENCE_ROOT, false);
	}
	
	/**
	 * use Celsius
	 * @return true == yes, false == no
	 */
	public boolean useCelsius() {
		return preferenceMgr.getBoolean(PREFERENCE_TEMPVALUE, false);
	}  

	/**
	 * set a security token
	 * @param token
	 */
	public void setToken(String token) {
		Editor edit = preferenceMgr.edit();
		edit.putString("token", token);
		edit.commit();
	}
	
	/**
	 * get the security token
	 * @return token
	 */
	public String getToken() {
		if(preferenceMgr.getString("token", "").length() == 0 )
			setToken(java.util.UUID.randomUUID().toString());
		return preferenceMgr.getString("token", "");
	}
	
	/**
	 * get map type
	 * @return map
	 */
	public String getMapType() {
		return preferenceMgr.getString(PREFERENCE_MAP, "GoogleMap");
	}
	
	/**
	 * set CPU on boot
	 * @return true == yes, false == no
	 */
	public boolean setCPU() {
		return preferenceMgr.getBoolean(PREFERENCE_SETCPU, false);
	}
	
	/**
	 * get CPU settings
	 * @return settings
	 */
	public String getCPUSettings() {
		return preferenceMgr.getString(PREFERENCE_SETCPUDATA, "");
	}
	
	/**
	 * show a shortcut on tje notification area
	 * @return true == yes, false == no
	 */
	public boolean addShortCut() {
		return preferenceMgr.getBoolean(PREFERENCE_SHORTCUT, false);
	}
	
	/**
	 * save sort type
	 * @param type
	 */
	public void setSortType(String type) {
		Editor edit = preferenceMgr.edit();
		edit.putString(PREFERENCE_SORTTYPE, type);
		edit.commit();
	}
	
	/**
	 * get sort type
	 * @return type
	 */
	public String getSortType() {
		return preferenceMgr.getString(PREFERENCE_SORTTYPE, "");
	}
	
	/**
	 * get font color for notification
	 * @return color
	 */
	public int chooseNotificationFontColor() {
	    return preferenceMgr.getInt(PREFERENCE_NOTIFICATION_COLOR,  -1);
	}
	
}
