package com.osacky.cumtd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StopDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "stoptable.db";
    private static final int DATABASE_VERSION = 1;

    public StopDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StopTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        StopTable.onUpgrade(db, oldVersion, newVersion);
    }
}
