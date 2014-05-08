package com.osacky.cumtd.api;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.osacky.cumtd.Constants;
import com.osacky.cumtd.StopTable;
import com.osacky.cumtd.StopsProvider;
import com.osacky.cumtd.models.GetStopResponse;
import com.osacky.cumtd.models.Stop;
import com.osacky.cumtd.models.StopList;
import com.osacky.cumtd.models.StopPoint;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GetStopsSpiceRequest extends RetrofitSpiceRequest<StopList, CUMTDApi> {

    private static final long cacheDuration = DurationInMillis.ALWAYS_RETURNED;
    private final SharedPreferences mSharedPreferences;
    private final ContentResolver contentResolver;

    public GetStopsSpiceRequest(@NotNull Context context) {
        super(StopList.class, CUMTDApi.class);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Thread.currentThread().setContextClassLoader(GetStopsSpiceRequest.class.getClassLoader());
        contentResolver = context.getContentResolver();
    }

    @Override
    public StopList loadDataFromNetwork() throws Exception {
        final String changesetId = mSharedPreferences.getString(Constants.STOPS_CHANGESET_ID, "");
        final GetStopResponse stopResponse = getService().getStops(changesetId);
        if (stopResponse.isNewChangeset()) {
            contentResolver.delete(StopsProvider.CONTENT_URI, null, null);
            final SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(Constants.STOPS_CHANGESET_ID, stopResponse.getChangesetId());
            final StopList stopList = stopResponse.getStops();
            editor.putString(Constants.STOPS_SAVE_ID, new Gson().toJson(stopList));
            editor.commit();
            List<ContentValues> contentValues = new ArrayList<>();
            for (Stop stop : stopList) {
                for (StopPoint stopPoint : stop.getStopPoints()) {
                    ContentValues contentValue = new ContentValues();
                    contentValue.put(StopTable.COLUMN_ID, stopPoint.getStopId());
                    contentValue.put(StopTable.SEARCH_COL, stopPoint.getStopName() + " " +
                            stopPoint.getCode() + " " + stopPoint.getCode().substring(3));
                    contentValue.put(StopTable.CODE_COL, stopPoint.getCode());
                    contentValue.put(StopTable.NAME_COL, stopPoint.getStopName());
                    contentValue.put(StopTable.LAT_COL, stopPoint.getStopLat());
                    contentValue.put(StopTable.LON_COL, stopPoint.getStopLon());
                    contentValues.add(contentValue);
                }
            }
            contentResolver.bulkInsert(StopsProvider.CONTENT_URI, contentValues.toArray(new
                    ContentValues[contentValues.size()]));
            return stopList;
        } else {
            return new Gson().fromJson(
                    mSharedPreferences.getString(Constants.STOPS_SAVE_ID, ""),
                    StopList.class
            );
        }
    }

    public static CachedSpiceRequest<StopList> getCachedSpiceRequest(
            @NotNull Context context) {
        GetStopsSpiceRequest getStopsSpiceRequest = new GetStopsSpiceRequest(context);
        return new CachedSpiceRequest<>(getStopsSpiceRequest, "stopsList", cacheDuration);
    }
}
