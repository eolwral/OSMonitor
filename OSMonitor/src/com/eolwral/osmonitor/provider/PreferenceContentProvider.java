package com.eolwral.osmonitor.provider;

import java.util.Map;

import com.eolwral.osmonitor.db.PreferenceDBHelper;
import com.eolwral.osmonitor.util.CommonUtil;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class PreferenceContentProvider  extends ContentProvider {
  private PreferenceDBHelper dbHelper =  null;  
  private SQLiteDatabase database = null;
  
  public final static String TABLE="preference";  
  public final static String KEY="id"; 
  public final static String VALUE="value";
  public static final String NOEXIST = "noExist";
  
  // base 
  private static final String AUTHORITY = "com.eolwral.osmonitor.provider";
  private static final String SETTINGS_PATH = "settings";

  private static final int SETTINGS_METHOD = 10;
  private static final int MODE_MULTI_PROCESS = 4;
  
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + SETTINGS_PATH);
  
  // prepare UriMatcher
  private static final UriMatcher osmURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  static {
    osmURIMatcher.addURI(AUTHORITY, SETTINGS_PATH, SETTINGS_METHOD);
  }
  
  @Override
  public boolean onCreate() {

    // TODO: this code should be removed soon
    boolean initial = true;
    if(!CommonUtil.doesDatabaseExist(getContext(), PreferenceDBHelper.SQLITEDB_NAME)) 
      initial = false;
    
    initSQLiteDB();

    if (!initial) mergeSettings();
    
    return false;
  }
  
  private void mergeSettings() {
    SharedPreferences preferenceMgr =  getContext().getSharedPreferences( getContext().getPackageName() + "_preferences",   
                                                                               MODE_MULTI_PROCESS);
    // dump into SQLite
    Map<String, ?> oldData = preferenceMgr.getAll();
    for (Map.Entry<String, ?> entry : oldData.entrySet()) {
      ContentValues data = new ContentValues();
      data.put(KEY, entry.getKey());
      data.put(VALUE, entry.getValue().toString());
      insert(CONTENT_URI, data);
    }
  }

  private void initSQLiteDB() {
    dbHelper = new PreferenceDBHelper(getContext());
    database = dbHelper.getWritableDatabase();
    database.setLockingEnabled(true);
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
      
    int uriType = osmURIMatcher.match(uri);
    Cursor cursor = null;

    switch (uriType) {
    case SETTINGS_METHOD:
      cursor = database.query(TABLE, projection, selection, selectionArgs, null, null, sortOrder);
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    return cursor;
  }

  @Override
  public String getType(Uri uri) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {

    int uriType = osmURIMatcher.match(uri);
    long recordId = 0;

    switch (uriType) {
    case SETTINGS_METHOD:
      recordId = database.insert(TABLE, null, values);
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    // notify
    getContext().getContentResolver().notifyChange(uri, null);
    
    if(recordId == -1)
      return  Uri.parse(SETTINGS_PATH + "/" + NOEXIST);
    return Uri.parse(SETTINGS_PATH + "/" + values.get(KEY));
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    int uriType = osmURIMatcher.match(uri);

    int rowsUpdated = 0;
    switch (uriType) {
    case SETTINGS_METHOD:
      rowsUpdated = database.update(TABLE, values, selection, selectionArgs);
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    
    // notify
    getContext().getContentResolver().notifyChange(uri, null);
    
    return rowsUpdated;
  } 
  
  

}
