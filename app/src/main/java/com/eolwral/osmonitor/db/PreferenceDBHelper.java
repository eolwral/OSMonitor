package com.eolwral.osmonitor.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PreferenceDBHelper extends SQLiteOpenHelper {
  public static final String SQLITEDB_NAME = "preference.db";
  public static final int VERSION = 1;
  private static final String PREFERENCE_DDL = "CREATE TABLE preference ( id TEXT PRIMARY KEY, value TEXT);";

  public PreferenceDBHelper(Context context) {
    super(context, SQLITEDB_NAME, null, VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(PREFERENCE_DDL);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // not support yet
  }

}
