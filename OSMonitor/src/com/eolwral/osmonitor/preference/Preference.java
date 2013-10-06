package com.eolwral.osmonitor.preference;

import com.eolwral.osmonitor.OSMonitorService;
import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.util.CommonUtil;
import com.eolwral.osmonitor.settings.Settings;
import com.eolwral.osmonitor.settings.SettingsHelper;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class Preference extends PreferenceActivity  {
	
	private SettingsHelper helper = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// reload settings
		helper = new SettingsHelper(this);

		// add layout
		addPreferencesFromResource(R.xml.ui_preference_main);
		
		// initial preferences
		initPreferences();
	}

	private void initPreferences() {
		int prefCategoryCount =  getPreferenceScreen().getPreferenceCount();
		for(int checkCategoryItem = 0; checkCategoryItem < prefCategoryCount; checkCategoryItem++) {

			// lookup all categories
			android.preference.PreferenceCategory prefCategory =  
						(android.preference.PreferenceCategory) getPreferenceScreen().getPreference(checkCategoryItem);
			if(prefCategory == null) continue;

			// lookup all preferences
		   int prefCount  = prefCategory.getPreferenceCount();
		   for(int checkItem = 0; checkItem < prefCount; checkItem++) {
				android.preference.Preference pref =  prefCategory.getPreference(checkItem);
				if (pref == null) continue;
			   pref.setOnPreferenceChangeListener(new preferencChangeListener());

			   // set value 
			   if(pref instanceof CheckBoxPreference) {
					((CheckBoxPreference) pref).setChecked(helper.getBoolean(pref.getKey(), false));
				}
				else if (pref instanceof ListPreference) {
					((ListPreference) pref).setValue(helper.getString(pref.getKey(), ""));
				}
			   
		   }
		}
	}

	private class preferencChangeListener implements  OnPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(
			android.preference.Preference preference, Object newValue) {

			if (!onPrePreferenceCheck(preference.getKey()))
				return false;
			
			if(preference instanceof CheckBoxPreference) {
				helper.setBoolean(preference.getKey(),  (Boolean) newValue);
			}
			else if (preference instanceof ListPreference) {
				helper.setString(preference.getKey(),  (String) newValue);
			}
			else if (preference instanceof ColorPickerPreference) {
				helper.setInteger(preference.getKey(),  (Integer) newValue);
			}
			else if (preference instanceof ProcessorPreference) {
				helper.setString(preference.getKey(),  (String) newValue);
			}
			
			// force read value from content provider
			helper.clearCache();
			
			onPostPreferenceCheck(preference.getKey());
			
			return true;
		}
	  
	}
	
	private boolean onPostPreferenceCheck(String key) {
		if(key.equals(Settings.PREFERENCE_CPUUSAGE) || key.equals(Settings.PREFERENCE_SHORTCUT)) {

			CheckBoxPreference autoStart = 
					(CheckBoxPreference) getPreferenceScreen().findPreference(Settings.PREFERENCE_AUTOSTART);

			if(helper.getBoolean(Settings.PREFERENCE_CPUUSAGE, false) || 
				helper.getBoolean(Settings.PREFERENCE_SHORTCUT, false)) {
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
			if(helper.getBoolean(Settings.PREFERENCE_CPUUSAGE, false) ||
			    helper.getBoolean(Settings.PREFERENCE_SHORTCUT, false)  ) {
				getApplication().startService(new Intent(getApplication(), OSMonitorService.class));
			}
		}	
		return true;
	}
	  
	public boolean onPrePreferenceCheck(String key) {
		
		if(key.equals(Settings.PREFERENCE_ROOT) && !helper.getBoolean(Settings.PREFERENCE_ROOT, false)) {
			if(CommonUtil.preCheckRoot() == false ) 
				return false;
		}
		return true;
	}
}
