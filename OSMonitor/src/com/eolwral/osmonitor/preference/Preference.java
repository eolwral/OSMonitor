package com.eolwral.osmonitor.preference;

import com.eolwral.osmonitor.OSMonitorService;
import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.util.CommonUtil;
import com.eolwral.osmonitor.util.Settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;

public class Preference extends PreferenceActivity 
						implements OnSharedPreferenceChangeListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesMode(MODE_MULTI_PROCESS);		
		addPreferencesFromResource(R.xml.ui_preference_main);
	    
		// not ready to release
		// resetStatus();
	}

	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	} 
	
	private void resetStatus() {
		
		CheckBoxPreference cpuUsage = 
				(CheckBoxPreference) getPreferenceScreen().findPreference(Settings.PREFERENCE_CPUUSAGE);

		CheckBoxPreference shortCut = 
				(CheckBoxPreference) getPreferenceScreen().findPreference(Settings.PREFERENCE_SHORTCUT);

		CheckBoxPreference autoStart = 
				(CheckBoxPreference) getPreferenceScreen().findPreference(Settings.PREFERENCE_AUTOSTART);

		SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
		
		if(sharedPreferences.getBoolean(Settings.PREFERENCE_CPUUSAGE, false)) 
			shortCut.setEnabled(false);
		else 
			shortCut.setEnabled(true);			

		if(sharedPreferences.getBoolean(Settings.PREFERENCE_SHORTCUT, false)) 
			cpuUsage.setEnabled(false);
		else 
			cpuUsage.setEnabled(true);			

		if(sharedPreferences.getBoolean(Settings.PREFERENCE_CPUUSAGE, false) || 
		   sharedPreferences.getBoolean(Settings.PREFERENCE_SHORTCUT, false)) {
			autoStart.setEnabled(true);
		}
		else {
			autoStart.setEnabled(false);
		}		
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		
		if(key.equals(Settings.PREFERENCE_ROOT) && 
				sharedPreferences.getBoolean(Settings.PREFERENCE_ROOT, false)) {
			if(CommonUtil.preCheckRoot() == false ) {
				
				final SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putBoolean(Settings.PREFERENCE_ROOT, false);
				editor.commit();
				
				CheckBoxPreference checkRoot = 
						(CheckBoxPreference) getPreferenceScreen().findPreference(Settings.PREFERENCE_ROOT);
				checkRoot.setChecked(false);
			}
		}
		
		
		if(key.equals(Settings.PREFERENCE_CPUUSAGE) || key.equals(Settings.PREFERENCE_SHORTCUT)) {

			//CheckBoxPreference cpuUsage = 
			//		(CheckBoxPreference) getPreferenceScreen().findPreference(Settings.PREFERENCE_CPUUSAGE);

			//CheckBoxPreference shortCut = 
			//		(CheckBoxPreference) getPreferenceScreen().findPreference(Settings.PREFERENCE_SHORTCUT);

			CheckBoxPreference autoStart = 
					(CheckBoxPreference) getPreferenceScreen().findPreference(Settings.PREFERENCE_AUTOSTART);

			
			//if(sharedPreferences.getBoolean(Settings.PREFERENCE_CPUUSAGE, false)) 
			//	shortCut.setEnabled(false);
			//else 
			//	shortCut.setEnabled(true);			

			//if(sharedPreferences.getBoolean(Settings.PREFERENCE_SHORTCUT, false)) 
			//	cpuUsage.setEnabled(false);
			//else 
			//	cpuUsage.setEnabled(true);			

			if(sharedPreferences.getBoolean(Settings.PREFERENCE_CPUUSAGE, false) || 
			   sharedPreferences.getBoolean(Settings.PREFERENCE_SHORTCUT, false)) {
				autoStart.setEnabled(true);
			}
			else {
				autoStart.setEnabled(false);
			}
				
		}
		
		if(key.equals(Settings.PREFERENCE_CPUUSAGE) || key.equals(Settings.PREFERENCE_COLOR) ||
		   key.equals(Settings.PREFERENCE_ROOT) || key.equals(Settings.PREFERENCE_TEMPVALUE) ||
		   key.equals(Settings.PREFERENCE_SHORTCUT) || key.equals(Settings.PREFERENCE_NOTIFICATION_COLOR) ||
		   key.equals(Settings.PREFERENCE_NOTIFICATION_TOP)) {
			
			// restart background daemon
			getApplication().stopService(new Intent(getApplication(), OSMonitorService.class));

			// check it before use
			IpcService ipc = IpcService.getInstance();
			if(ipc != null) {
				ipc.forceExit();
				ipc.disconnect();
			}
			
			// restart notification 
			if(sharedPreferences.getBoolean(Settings.PREFERENCE_CPUUSAGE, false) ||
			    sharedPreferences.getBoolean(Settings.PREFERENCE_SHORTCUT, false)  ) {
				getApplication().startService(new Intent(getApplication(), OSMonitorService.class));
			}
				
		}
	}
	
	
}
