package com.eolwral.osmonitor.settings;

import android.content.Context;

public class Settings {

	// notification type
	public class NotificationType {
		public final static int MEMORY_BATTERY = 1;
		public final static int MEMORY_DISKIO  = 2;
		public final static int BATTERY_DISKIO = 3; 
		public final static int NETWORKIO = 4;
	}
	
	// statusbar color
	public class StatusBarColor {
		public final static int GREEN = 1;
		public final static int BLUE = 2;
	}
	
	// singleton
	private static Settings instance = null;
	
	private SettingsHelper helper = null;
	
	public final static String PREFRENCE_INTERVAL = "id_preference_interval";
	public final static String PREFERENCE_TEMPVALUE = "id_preference_tempvalue";
	public final static String PREFERENCE_AUTOSTART = "id_preference_autostart";
	public final static String PREFERENCE_ROOT = "id_preference_root";
	
	public final static String PREFERENCE_MAP = "id_preference_map";
	
	public final static String PREFERENCE_EXPERTMODE = "id_preference_expertmode";

	public final static String PREFERENCE_SETCPU = "id_preference_setcpu";
	public final static String PREFERENCE_SETCPUDATA = "id_preference_setcpu_data";

	public final static String PREFERENCE_SORTTYPE = "id_preference_sorttype";

	public final static String PREFERENCE_SHORTCUT = "id_preference_shortcut";
	public final static String PREFERENCE_CPUUSAGE = "id_preference_cpuusage";
	public final static String PREFERENCE_COLOR = "id_preference_color";
	public final static String PREFERENCE_NOTIFICATION_COLOR = "id_preference_notification_fontcolor";
	public final static String PREFERENCE_NOTIFICATION_TOP = "id_preference_notification_top";
	public final static String PREFERENCE_NOTIFICATION_CUSTOMIZE = "id_preference_notification_customize";
	
	public final static String PREFERENCE_LOGCAT_FORMAT = "id_preference_logcat_format";
	public final static String PREFERENCE_LOGCAT_VERBOSE = "id_preference_logcat_verbose_color";
	public final static String PREFERENCE_LOGCAT_DEBUG = "id_preference_logcat_debug_color";
	public final static String PREFERENCE_LOGCAT_INFO = "id_preference_logcat_info_color";
	public final static String PREFERENCE_LOGCAT_WARNING = "id_preference_logcat_warning_color";
	public final static String PREFERENCE_LOGCAT_ERROR = "id_preference_logcat_error_color";
	public final static String PREFERENCE_LOGCAT_FATAL = "id_preference_logcat_fatal_color";
	
	public final static String PREFERENCE_DMESG_FORMAT = "id_preference_dmesg_format";
	public final static String PREFERENCE_DMESG_EMERGENCY = "id_preference_dmesg_emergency_color";
	public final static String PREFERENCE_DMESG_ALERT  = "id_preference_dmesg_alert_color";
	public final static String PREFERENCE_DMESG_CRITICAL = "id_preference_dmesg_critical_color";
	public final static String PREFERENCE_DMESG_ERROR  = "id_preference_dmesg_error_color";
	public final static String PREFERENCE_DMESG_WARNING = "id_preference_dmesg_warning_color";
	public final static String PREFERENCE_DMESG_NOTICE = "id_preference_dmesg_notice_color";
	public final static String PREFERENCE_DMESG_INFO = "id_preference_dmesg_info_color";
	public final static String PREFERENCE_DMESG_DEBUG = "id_preference_dmesg_debug_color";
	
	public final static String SESSION_SECTION = "session_storage";
	
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
	
	/**
	 * get color of verbose level 
	 * @return color
	 */
	public int getLogcatVerboseColor() {
		return helper.getInteger(PREFERENCE_LOGCAT_VERBOSE,  0xff888888);
	}

	/**
	 * get color of debug level 
	 * @return color
	 */
	public int getLogcatDebugColor() {
		return helper.getInteger(PREFERENCE_LOGCAT_DEBUG,  0xff3399ff);
	}

	/**
	 * get color of information level 
	 * @return color
	 */
	public int getLogcatInfoColor() {
		return helper.getInteger(PREFERENCE_LOGCAT_INFO,  0xff00ff00);
	}

	/**
	 * get color of warning level 
	 * @return color
	 */
	public int getLogcatWarningColor() {
		return helper.getInteger(PREFERENCE_LOGCAT_WARNING,  0xffff00ff);
	}

	/**
	 * get color of error level 
	 * @return color
	 */
	public int getLogcatErrorColor() {
		return helper.getInteger(PREFERENCE_LOGCAT_ERROR, 0xffff0000);
	}

	/**
	 * get color of fatal level 
	 * @return color
	 */
	public int getLogcatFatalColor() {
		return helper.getInteger(PREFERENCE_LOGCAT_FATAL, 0xffff0000);
	}

	
	/**
	 * get dmesg format 
	 * @return format
	 */
	public int getDmesgFormat() {
	    return helper.getInteger(PREFERENCE_DMESG_FORMAT,  0);
	}
	
	/**
	 * get color of emergency level 
	 * @return color
	 */
	public int getDmesgEmergencyColor() {
		return helper.getInteger(PREFERENCE_DMESG_EMERGENCY,  0xffff0000);
	}

	/**
	 * get color of alert level 
	 * @return color
	 */
	public int getDmesgAlertColor() {
		return helper.getInteger(PREFERENCE_DMESG_ALERT,  0xffcccc00);
	}

	/**
	 * get color of critical level 
	 * @return color
	 */
	public int getDmesgCriticalColor() {
		return helper.getInteger(PREFERENCE_DMESG_CRITICAL,  0xff66ff99);
	}
	
	/**
	 * get color of error level 
	 * @return color
	 */
	public int getDmesgErrorColor() {
		return helper.getInteger(PREFERENCE_DMESG_ERROR,  0xff33cc33);
	}
	
	/**
	 * get color of warning level 
	 * @return color
	 */
	public int getDmesgWarningColor() {
		return helper.getInteger(PREFERENCE_DMESG_WARNING,  0xff339933);
	}

	/**
	 * get color of notice level 
	 * @return color
	 */
	public int getDmesgNoticeColor() {
		return helper.getInteger(PREFERENCE_DMESG_NOTICE,  0xff3399ff);
	}

	/**
	 * get color of information level 
	 * @return color
	 */
	public int getDmesgInfoColor() {
		return helper.getInteger(PREFERENCE_DMESG_INFO,  0xff0000ff);
	}

	/**
	 * get color of debug level 
	 * @return color
	 */
	public int getDmesgDebugColor() {
		return helper.getInteger(PREFERENCE_DMESG_DEBUG,  0xff9933ff);
	}
	
	/**
	 * get customize type of notification
	 * @return type
	 */
	public int getNotificationType() {
		return helper.getInteger(PREFERENCE_NOTIFICATION_CUSTOMIZE, NotificationType.MEMORY_BATTERY);
	}
	
	/**
	 * set session value 
	 * @param value
	 */
	public void setSessionValue(String value) {
		helper.setString(SESSION_SECTION, value);
	}
	
	/**
	 * get session value
	 * @return value
	 */
	public String getSessionValue() {
		String value = helper.getString(SESSION_SECTION, "");
		helper.setString(SESSION_SECTION, "");
		return value;
	}
}
