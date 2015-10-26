package com.eolwral.osmonitor.preference;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import com.eolwral.osmonitor.OSMonitorService;
import com.eolwral.osmonitor.R;
import com.eolwral.osmonitor.ipc.IpcService;
import com.eolwral.osmonitor.settings.Settings;
import com.eolwral.osmonitor.settings.SettingsHelper;
import com.eolwral.osmonitor.util.CoreUtil;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class OSMPreference extends PreferenceActivity {

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
    PreferenceScreen prefScreen = getPreferenceScreen();

    if (prefScreen != null) {

      // Disable start on boot feature if application installed on extra storage
      Preference prefAutoStart = prefScreen.findPreference(Settings.PREFERENCE_AUTOSTART);
      if ((helper.getBoolean(Settings.PREFERENCE_CPUUSAGE, false)
          || helper.getBoolean(Settings.PREFERENCE_SHORTCUT, false))
          && !CoreUtil.isExtraStroage(this)) {
        prefAutoStart.setEnabled(true);
      } else {
        prefAutoStart.setEnabled(false);
      }

      // notification color is disabled on Lollipop
      if (CoreUtil.isLollipop()) {
        Preference prefColor = prefScreen.findPreference(Settings.PREFERENCE_COLOR);
        if (getParent(prefColor) != null)
          getParent(prefColor).removePreference(prefColor);
      }

      preparePreferenceScreen(prefScreen);
    }


  }

  /*
     Thanks fo Stanislav Bokach
     http://stackoverflow.com/questions/6177244/how-do-i-get-the-category-of-an-android-preference
   */

  private PreferenceGroup getParent(Preference preference)
  {
    return getParent(getPreferenceScreen(), preference);
  }

  private PreferenceGroup getParent(PreferenceGroup root, Preference preference)
  {
    for (int i = 0; i < root.getPreferenceCount(); i++)
    {
      Preference p = root.getPreference(i);
      if (p == preference)
        return root;
      if (PreferenceGroup.class.isInstance(p))
      {
        PreferenceGroup parent = getParent((PreferenceGroup)p, preference);
        if (parent != null)
          return parent;
      }
    }
    return null;
  }

  private void preparePreferenceScreen(PreferenceScreen prefScreen) {
    int prefCategoryCount = prefScreen.getPreferenceCount();
    for (int checkCategoryItem = 0; checkCategoryItem < prefCategoryCount; checkCategoryItem++) {
      // lookup all subitems
      if (prefScreen.getPreference(checkCategoryItem) instanceof PreferenceCategory) {
        PreferenceCategory prefCategory = (PreferenceCategory) prefScreen
            .getPreference(checkCategoryItem);
        if (prefCategory == null)
          continue;
        preparePreferenceCategory(prefCategory);
      } else {
        preparePreferenceItem(prefScreen.getPreference(checkCategoryItem));
      }
    }
  }

  private void preparePreferenceCategory(PreferenceCategory prefCategory) {
    // lookup all preferences
    for (int checkItem = 0; checkItem < prefCategory.getPreferenceCount(); checkItem++) {
      android.preference.Preference pref = prefCategory
          .getPreference(checkItem);
      if (pref == null)
        continue;
      preparePreferenceItem(pref);
    }
    return;
  }

  private void preparePreferenceItem(android.preference.Preference pref) {
    // set value
    if (pref instanceof CheckBoxPreference) {
      ((CheckBoxPreference) pref).setChecked(helper.getBoolean(pref.getKey(),
          false));
    } else if (pref instanceof ListPreference) {
      ((ListPreference) pref).setValue(helper.getString(pref.getKey(), ""));
    } else if (pref instanceof ColorPickerPreference) {
      int defaultColor = helper.getInteger(pref.getKey(), 0x00000000);
      if (defaultColor != 0x00000000)
        ((ColorPickerPreference) pref).setColor(defaultColor);
    }

    // bind event
    if (pref instanceof PreferenceScreen)
      pref.setOnPreferenceClickListener(new preferencScreenChangeListener());
    else
      pref.setOnPreferenceChangeListener(new preferencChangeListener());
  }

  private class preferencScreenChangeListener implements
      OnPreferenceClickListener {
    @Override
    public boolean onPreferenceClick(android.preference.Preference prefSubScreen) {
      PreferenceScreen prefScreen = (PreferenceScreen) prefSubScreen;
      for (int checkItem = 0; checkItem < prefScreen.getPreferenceCount(); checkItem++) {
        android.preference.Preference pref = prefScreen
            .getPreference(checkItem);
        if (pref == null)
          continue;
        preparePreferenceItem(pref);
      }
      return false;
    }
  }

  private class preferencChangeListener implements OnPreferenceChangeListener {

    @Override
    public boolean onPreferenceChange(android.preference.Preference preference,
        Object newValue) {

      if (!onPrePreferenceCheck(preference.getKey()))
        return false;

      if (preference instanceof CheckBoxPreference) {
        helper.setBoolean(preference.getKey(), (Boolean) newValue);
      } else if (preference instanceof ListPreference) {
        helper.setString(preference.getKey(), (String) newValue);
      } else if (preference instanceof ColorPickerPreference) {
        helper.setInteger(preference.getKey(), (Integer) newValue);
      } else if (preference instanceof ProcessorPreference) {
        helper.setString(preference.getKey(), (String) newValue);
      }

      // force read value from content provider
      helper.clearCache();

      onPostPreferenceCheck(preference.getKey());

      return true;
    }

  }

  private boolean onPostPreferenceCheck(String key) {

    // Disable start on boot feature if application installed on extra storage
    if (key.equals(Settings.PREFERENCE_CPUUSAGE)
        || key.equals(Settings.PREFERENCE_SHORTCUT)) {

      CheckBoxPreference prefAutoStart = (CheckBoxPreference) getPreferenceScreen()
          .findPreference(Settings.PREFERENCE_AUTOSTART);

      if ((helper.getBoolean(Settings.PREFERENCE_CPUUSAGE, false)
          || helper.getBoolean(Settings.PREFERENCE_SHORTCUT, false))
          && !CoreUtil.isExtraStroage(this)) {
        prefAutoStart.setEnabled(true);
      } else {
        prefAutoStart.setEnabled(false);
      }

    }

    // check following option if changed
    if (key.equals(Settings.PREFERENCE_CPUUSAGE)
        || key.equals(Settings.PREFERENCE_COLOR)
        || key.equals(Settings.PREFERENCE_ROOT)
        || key.equals(Settings.PREFERENCE_TEMPVALUE)
        || key.equals(Settings.PREFERENCE_SHORTCUT)
        || key.equals(Settings.PREFERENCE_NOTIFICATION_COLOR)
        || key.equals(Settings.PREFERENCE_NOTIFICATION_TOP)
        || key.equals(Settings.PREFERENCE_NOTIFICATION_CUSTOMIZE)) {

      if (key.equals(Settings.PREFERENCE_ROOT)) {
        IpcService.getInstance().forceExit();
        IpcService.getInstance().createConnection();
      }

      // prevent exit
      if (helper.getBoolean(Settings.PREFERENCE_CPUUSAGE, false)
          || helper.getBoolean(Settings.PREFERENCE_SHORTCUT, false)) {
        helper.setString(Settings.SESSION_SECTION, "Non-Exit");
      }

      // restart background daemon
      getApplication().stopService(
          new Intent(getApplication(), OSMonitorService.class));

      // restart notification
      if (helper.getBoolean(Settings.PREFERENCE_CPUUSAGE, false)
          || helper.getBoolean(Settings.PREFERENCE_SHORTCUT, false)) {
        getApplication().startService(
            new Intent(getApplication(), OSMonitorService.class));
      }
    }
    return true;
  }

  public boolean onPrePreferenceCheck(String key) {
    if (key.equals(Settings.PREFERENCE_ROOT)
        && !helper.getBoolean(Settings.PREFERENCE_ROOT, false)) {
      if (CoreUtil.preCheckRoot() == false)
        return false;
    }
    return true;
  }
}
