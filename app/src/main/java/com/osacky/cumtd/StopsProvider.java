package com.osacky.cumtd;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
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

public class StopsProvider extends ContentProvider {

    private static final String TAG = StopsProvider.class.getName();
    private StopDatabaseHelper database;

    private static final int SEARCH_SUGGEST = 0;
    private static final int SHORTCUT_REFRESH = 1;
    private static final int STOPS = 10;
    private static final int STOP_ID = 20;

    private static final String AUTHORITY = "com.osacky.cumtd.StopsProvider";
    private static final String BASE_PATH = "stops";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
            BASE_PATH;

    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
            "stop";

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, BASE_PATH, STOPS);
        sUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", STOP_ID);
        sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
        sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, SHORTCUT_REFRESH);
        sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SHORTCUT_REFRESH);
    }

    private static final Map<String, String> searchProjectionMap = new HashMap<>();

    static {
        searchProjectionMap.put(StopTable.COLUMN_ID, StopTable.COLUMN_ID + " as " + StopTable
                .COLUMN_ID);
        searchProjectionMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, StopTable.NAME_COL + " as "
                + SearchManager.SUGGEST_COLUMN_TEXT_1);
        searchProjectionMap.put(SearchManager.SUGGEST_COLUMN_TEXT_2, StopTable.CODE_COL + " as "
                + SearchManager.SUGGEST_COLUMN_TEXT_2);
        searchProjectionMap.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                StopTable.COLUMN_ID + " as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA);

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

        queryBuilder.setTables(StopTable.TABLE_STOP);
        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case STOP_ID:
                queryBuilder.appendWhere(StopTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            case STOPS:
                break;
            case SEARCH_SUGGEST:
                queryBuilder.setProjectionMap(searchProjectionMap);
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
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
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
                id = sqLiteDatabase.insert(StopTable.TABLE_STOP, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unkown URI:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
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
                rowsDeleted = sqLiteDatabase.delete(StopTable.TABLE_STOP, selection, selectionArgs);
                break;
            case STOP_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqLiteDatabase.delete(StopTable.TABLE_STOP,
                            StopTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqLiteDatabase.delete(StopTable.TABLE_STOP,
                            StopTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
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
                rowsUpdated = sqLiteDatabase.update(StopTable.TABLE_STOP, values, selection,
                        selectionArgs);
                break;
            case STOP_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqLiteDatabase.update(StopTable.TABLE_STOP, values,
                            StopTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = sqLiteDatabase.update(StopTable.TABLE_STOP, values,
                            StopTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unkown URI:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {StopTable.COLUMN_ID,
                StopTable.NAME_COL, StopTable.CODE_COL,
                StopTable.LAT_COL, StopTable.LON_COL};
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
