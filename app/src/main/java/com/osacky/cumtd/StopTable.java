package com.osacky.cumtd;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class StopTable {

    public static final String TABLE_STOP = "stop";
    public static final String ROW_ID = "rowid";
    public static final String STOP_ID = "stopid";
    public static final String SEARCH_COL = "search";
    public static final String NAME_COL = "name";
    public static final String CODE_COL = "code";
    public static final String LAT_COL = "lat";
    public static final String LON_COL = "lon";
    public static final String IS_FAV = "fav";

    private static final String DATABASE_CREATE = "create virtual table "
            + TABLE_STOP
            + " using fts3 ("
            + STOP_ID + " text unique, "
            + SEARCH_COL + " text not null, "
            + NAME_COL + " text not null, "
            + CODE_COL + " text not null, "
            + LAT_COL + " real not null, "
            + LON_COL + " real not null, "
            + IS_FAV + " integer default 0"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(StopTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_STOP);
        onCreate(database);
    }
}
