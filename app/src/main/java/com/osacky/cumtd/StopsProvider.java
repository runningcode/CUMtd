package com.osacky.cumtd;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static android.app.SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID;
import static android.app.SearchManager.SUGGEST_COLUMN_TEXT_1;
import static android.app.SearchManager.SUGGEST_COLUMN_TEXT_2;
import static android.app.SearchManager.SUGGEST_URI_PATH_QUERY;
import static android.app.SearchManager.SUGGEST_URI_PATH_SHORTCUT;
import static android.content.ContentResolver.CURSOR_DIR_BASE_TYPE;
import static android.content.ContentResolver.CURSOR_ITEM_BASE_TYPE;
import static android.provider.BaseColumns._ID;
import static com.osacky.cumtd.StopTable.CODE_COL;
import static com.osacky.cumtd.StopTable.LAT_COL;
import static com.osacky.cumtd.StopTable.LON_COL;
import static com.osacky.cumtd.StopTable.NAME_COL;
import static com.osacky.cumtd.StopTable.ROW_ID;
import static com.osacky.cumtd.StopTable.SEARCH_COL;
import static com.osacky.cumtd.StopTable.TABLE_STOP;

public class StopsProvider extends ContentProvider {

    @SuppressWarnings("unused")
    private static final String TAG = StopsProvider.class.getName();
    private StopDatabaseHelper database;

    private static final int SEARCH_SUGGEST = 0;
    private static final int SHORTCUT_REFRESH = 1;
    private static final int STOPS = 10;
    private static final int STOP_ID = 20;

    private static final String AUTHORITY = "com.osacky.cumtd.StopsProvider";
    private static final String BASE_PATH = "stops";
    public static final String CONTENT_TYPE = CURSOR_DIR_BASE_TYPE + "/stops";
    public static final String CONTENT_ITEM_TYPE = CURSOR_ITEM_BASE_TYPE + "/stop";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, BASE_PATH, STOPS);
        sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", STOP_ID);
        sUriMatcher.addURI(AUTHORITY, SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        sUriMatcher.addURI(AUTHORITY, SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
        sUriMatcher.addURI(AUTHORITY, SUGGEST_URI_PATH_SHORTCUT, SHORTCUT_REFRESH);
        sUriMatcher.addURI(AUTHORITY, SUGGEST_URI_PATH_SHORTCUT + "/*", SHORTCUT_REFRESH);
    }

    private static final Map<String, String> SEARCH_PROJECTION_MAP = new HashMap<>();
    static {
        SEARCH_PROJECTION_MAP.put(_ID, ROW_ID + " as " + _ID);
        SEARCH_PROJECTION_MAP.put(SUGGEST_COLUMN_TEXT_1, NAME_COL + " as " + SUGGEST_COLUMN_TEXT_1);
        SEARCH_PROJECTION_MAP.put(SUGGEST_COLUMN_TEXT_2, CODE_COL + " as " + SUGGEST_COLUMN_TEXT_2);
        SEARCH_PROJECTION_MAP.put(SUGGEST_COLUMN_INTENT_DATA_ID,
                ROW_ID + " as " + SUGGEST_COLUMN_INTENT_DATA_ID);
    }

    @Override
    public boolean onCreate() {
        database = new StopDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        checkColumns(projection);

        queryBuilder.setTables(TABLE_STOP);
        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case STOP_ID:
                queryBuilder.appendWhere(ROW_ID + "=" + uri.getLastPathSegment());
                break;
            case STOPS:
                break;
            case SEARCH_SUGGEST:
                queryBuilder.setProjectionMap(SEARCH_PROJECTION_MAP);
                if (selectionArgs[0] != null) {
                    selectionArgs[0] = "*" + selectionArgs[0] + "*";
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = database.getReadableDatabase();
        assert db != null;
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null,
                sortOrder);
        assert cursor != null;
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public int bulkInsert(Uri uri, @NotNull ContentValues[] values) {
        int numRows = 0;
        SQLiteDatabase sqLiteDatabase = database.getWritableDatabase();
        assert sqLiteDatabase != null;
        sqLiteDatabase.beginTransaction();
        try {
            for (ContentValues contentValues : values) {
                insert(uri, contentValues);
                numRows++;
            }
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
        return numRows;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqLiteDatabase = database.getWritableDatabase();
        assert sqLiteDatabase != null;
        long id;
        switch (uriType) {
            case STOPS:
                id = sqLiteDatabase.insert(TABLE_STOP, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unkown URI:" + uri);
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqLiteDatabase = database.getWritableDatabase();
        assert sqLiteDatabase != null;
        int rowsDeleted;
        switch (uriType) {
            case STOPS:
                rowsDeleted = sqLiteDatabase.delete(TABLE_STOP, selection, selectionArgs);
                break;
            case STOP_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqLiteDatabase.delete(TABLE_STOP,
                            ROW_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqLiteDatabase.delete(TABLE_STOP,
                            ROW_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase sqLiteDatabase = database.getWritableDatabase();
        assert sqLiteDatabase != null;
        int rowsUpdated;
        switch (uriType) {
            case STOPS:
                rowsUpdated = sqLiteDatabase.update(TABLE_STOP, values, selection,
                        selectionArgs);
                break;
            case STOP_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqLiteDatabase.update(TABLE_STOP, values,
                            ROW_ID + "=" + id, null);
                } else {
                    rowsUpdated = sqLiteDatabase.update(TABLE_STOP, values,
                            ROW_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unkown URI:" + uri);
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {ROW_ID, StopTable.STOP_ID, SEARCH_COL, NAME_COL, CODE_COL, LAT_COL,
                LON_COL};
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

}
