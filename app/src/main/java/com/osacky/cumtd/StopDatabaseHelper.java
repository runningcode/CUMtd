package com.osacky.cumtd;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

public class StopDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "stoptable.db";
    private static final int DATABASE_VERSION = 2;
    private final Context mContext;

    public StopDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StopTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        StopTable.onUpgrade(db, oldVersion, newVersion);
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(Constants.STOPS_CHANGESET_ID, "");
        editor.commit();
    }
}
