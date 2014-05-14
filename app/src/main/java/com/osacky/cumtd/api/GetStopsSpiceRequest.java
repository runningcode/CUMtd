package com.osacky.cumtd.api;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.osacky.cumtd.CUMtdApplication;
import com.osacky.cumtd.StopsProvider;
import com.osacky.cumtd.models.GetStopResponse;
import com.osacky.cumtd.models.Stop;
import com.osacky.cumtd.models.StopList;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.osacky.cumtd.Constants.STOPS_CHANGESET_ID;
import static com.osacky.cumtd.Constants.STOPS_SAVE_ID;
import static com.osacky.cumtd.StopTable.CODE_COL;
import static com.osacky.cumtd.StopTable.LAT_COL;
import static com.osacky.cumtd.StopTable.LON_COL;
import static com.osacky.cumtd.StopTable.NAME_COL;
import static com.osacky.cumtd.StopTable.SEARCH_COL;
import static com.osacky.cumtd.StopTable.STOP_ID;

public class GetStopsSpiceRequest extends RetrofitSpiceRequest<StopList, CUMTDApi> {

    private final SharedPreferences mSharedPreferences;
    private final ContentResolver contentResolver;
    Tracker t;

    public GetStopsSpiceRequest(@NotNull Application context) {
        super(StopList.class, CUMTDApi.class);
        t = ((CUMtdApplication) context).getTracker();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Thread.currentThread().setContextClassLoader(GetStopsSpiceRequest.class.getClassLoader());
        contentResolver = context.getContentResolver();
    }

    @Override
    public StopList loadDataFromNetwork() throws Exception {
        final long startTime = SystemClock.elapsedRealtime();
        final String changesetId = mSharedPreferences.getString(STOPS_CHANGESET_ID, "");
        GetStopResponse stopResponse;
        try {
            stopResponse = getService().getStops(changesetId);
        } catch (Exception e) {
            // if no internet connection, return cached data
            t.send(new HitBuilders.TimingBuilder()
                    .setCategory("LoadingTime")
                    .setLabel("NO INTERNET")
                    .setValue(SystemClock.elapsedRealtime() - startTime)
                    .build());
            return new Gson().fromJson(
                    mSharedPreferences.getString(STOPS_SAVE_ID, ""),
                    StopList.class);
        }
        if (stopResponse.isNewChangeset()) {
            contentResolver.delete(StopsProvider.CONTENT_URI, null, null);
            final SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(STOPS_CHANGESET_ID, stopResponse.getChangesetId());
            final StopList stopList = stopResponse.getStops();
            editor.putString(STOPS_SAVE_ID, new Gson().toJson(stopList));
            editor.commit();
            List<ContentValues> contentValues = new ArrayList<>();
            for (Stop stop : stopList) {
                ContentValues contentValue = new ContentValues();
                contentValue.put(STOP_ID, stop.getStopId());
                contentValue.put(SEARCH_COL, stop.getStopName() + " " +
                        stop.getCode() + " " + stop.getCode().substring(3));
                contentValue.put(CODE_COL, stop.getCode());
                contentValue.put(NAME_COL, stop.getStopName());
                contentValue.put(LAT_COL, stop.getStopPoints().get(0).getStopLat());
                contentValue.put(LON_COL, stop.getStopPoints().get(0).getStopLon());
                contentValues.add(contentValue);
            }
            contentResolver.bulkInsert(StopsProvider.CONTENT_URI, contentValues.toArray(new
                    ContentValues[contentValues.size()]));
            t.send(new HitBuilders.TimingBuilder()
                    .setCategory("LoadingTime")
                    .setLabel("FULL LOAD")
                    .setValue(SystemClock.elapsedRealtime() - startTime)
                    .build());
            return stopList;
        } else {
            t.send(new HitBuilders.TimingBuilder()
                    .setCategory("LoadingTime")
                    .setLabel("FROM CACHE")
                    .setValue(SystemClock.elapsedRealtime() - startTime)
                    .build());
            return new Gson().fromJson(
                    mSharedPreferences.getString(STOPS_SAVE_ID, ""),
                    StopList.class
            );
        }
    }
}
