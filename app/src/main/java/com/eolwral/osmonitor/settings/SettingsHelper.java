package com.eolwral.osmonitor.settings;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

import com.eolwral.osmonitor.provider.PreferenceContentProvider;

public class SettingsHelper {

  private ContentResolver contentResolver = null;
  private Map<String, Boolean> updateStatus = new HashMap<String, Boolean>();
  private Map<String, String> cachedSettings = new HashMap<String, String>();

  public SettingsHelper(Context context) {
    contentResolver = context.getContentResolver();
    contentResolver.registerContentObserver(
        PreferenceContentProvider.CONTENT_URI, true, new contentObserver(
            new Handler()));
  }

  private final class contentObserver extends ContentObserver {

    @Override
    public void onChange(boolean selfChange) {
      super.onChange(selfChange);
      clearCache();
    }

    public contentObserver(Handler handler) {
      super(handler);
    }

  }

  public void clearCache() {
    // set status of all fetched values to false
    for (Map.Entry<String, Boolean> entry : updateStatus.entrySet())
      entry.setValue(true);
  }

  public HashMap<String, String> getAllData() {

    HashMap<String, String> data = new HashMap<String, String>();

    // query
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(PreferenceContentProvider.CONTENT_URI,
          new String[] { PreferenceContentProvider.KEY,
              PreferenceContentProvider.VALUE }, null, null, null);
    } catch (Exception e) {
    }
    if (cursor == null)
      return data;

    cursor.moveToFirst();
    while (cursor.getPosition() < cursor.getCount()) {
      data.put(cursor.getString(0), cursor.getString(1));
      if (!cursor.moveToNext())
        break;
    }
    cursor.close();

    return data;
  }

  private boolean isExistKey(String key) {

    // query
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(PreferenceContentProvider.CONTENT_URI,
          new String[] { PreferenceContentProvider.KEY },
          PreferenceContentProvider.KEY + "=?", new String[] { key }, null);
    } catch (Exception e) {
    }
    if (cursor == null)
      return false;

    // get count
    int count = cursor.getCount();
    cursor.close();

    // check
    if (count == 0)
      return false;
    return true;
  }

  private boolean setKey(String key, String value) {

    // create data
    ContentValues data = new ContentValues();
    data.put(PreferenceContentProvider.KEY, key);
    data.put(PreferenceContentProvider.VALUE, value);

    // insert
    Uri uri = contentResolver.insert(PreferenceContentProvider.CONTENT_URI,
        data);

    // Fix: java.lang.NullPointerException
    if (uri == null)
      return false;

    if (uri.getLastPathSegment().equals(PreferenceContentProvider.NOEXIST))
      return false;
    return true;
  }

  private boolean updateKey(String key, String value) {

    // create data
    ContentValues data = new ContentValues();
    data.put(PreferenceContentProvider.KEY, key);
    data.put(PreferenceContentProvider.VALUE, value);

    // update
    if (contentResolver.update(PreferenceContentProvider.CONTENT_URI, data,
        PreferenceContentProvider.KEY + "=?", new String[] { key }) > 0)
      return true;
    return false;
  }

  private String getKey(String key) {
    String value = null;

    // check cached settings
    if (updateStatus.containsKey(key) && !updateStatus.get(key)) {
      return cachedSettings.get(key);
    }

    // query
    Cursor cursor = null;
    try {
      cursor = contentResolver.query(PreferenceContentProvider.CONTENT_URI,
          new String[] { PreferenceContentProvider.KEY,
              PreferenceContentProvider.VALUE }, PreferenceContentProvider.KEY
              + "=?", new String[] { key }, null);
    } catch (Exception e) {
    }
    if (cursor == null)
      return value;

    if (cursor.getCount() == 0) {
      cursor.close();
      return value;
    }

    // get string
    cursor.moveToFirst();
    value = cursor.getString(cursor
        .getColumnIndex(PreferenceContentProvider.VALUE));
    cursor.close();

    // update status
    cachedSettings.put(key, value);
    updateStatus.put(key, false);

    return value;
  }

  public boolean checkStatus(String key) {
    if (!updateStatus.containsKey(key))
      return false;
    return updateStatus.get(key);
  }

  public String getString(String key, String defaultValue) {
    String value = "";

    if (isExistKey(key))
      value = getKey(key);
    else
      value = defaultValue;

    return value;
  }

  public void setString(String key, String value) {

    if (isExistKey(key))
      updateKey(key, value);
    else
      setKey(key, value);

    return;
  }

  public int getInteger(String key, int defaultValue) {
    int value = 0;

    if (isExistKey(key)) {
      String stringValue = getKey(key);
      value = Integer.parseInt(stringValue);
    } else
      value = defaultValue;

    return value;
  }

  public void setInteger(String key, int value) {
    setString(key, Integer.toString(value));
    return;
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    boolean value = true;

    if (isExistKey(key)) {
      String stringValue = getKey(key);
      value = Boolean.parseBoolean(stringValue);
    } else
      value = defaultValue;

    return value;
  }

  public void setBoolean(String key, boolean value) {
    setString(key, Boolean.toString(value));
    return;
  }
}
