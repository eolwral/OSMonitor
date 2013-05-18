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
		
		if(key.equals(Settings.PREFERENCE_CPUUSAGE) || key.equals(Settings.PREFERENCE_COLOR) ||
		   key.equals(Settings.PREFERENCE_ROOT)) {
			
			// restart background daemon
			getApplication().stopService(new Intent(getApplication(), OSMonitorService.class));
			IpcService.getInstance().forceExit();
			IpcService.getInstance().disconnect();
			
			// restart notification 
			if(sharedPreferences.getBoolean(Settings.PREFERENCE_CPUUSAGE, false)) {
				getApplication().startService(new Intent(getApplication(), OSMonitorService.class));
			}
				
		}
	}
	
	
}
