package com.osacky.cumtd.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.osacky.cumtd.Constants;
import com.osacky.cumtd.models.GetStopResponse;
import com.osacky.cumtd.models.Stop;
import com.osacky.cumtd.models.StopList;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GetStopsSpiceRequest extends RetrofitSpiceRequest<StopList, CUMTDApi> {

    private static final long cacheDuration = DurationInMillis.ALWAYS_RETURNED;
    private final SharedPreferences mSharedPreferences;

    public GetStopsSpiceRequest(@NotNull Context context) {
        super(StopList.class, CUMTDApi.class);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public StopList loadDataFromNetwork() throws Exception {
        final String changesetId = mSharedPreferences.getString(Constants.STOPS_CHANGESET_ID, "");
        final GetStopResponse stopResponse = getService().getStops(changesetId);
        if (stopResponse.isNewChangeset()) {
            final SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(Constants.STOPS_CHANGESET_ID, stopResponse.getChangesetId());
            editor.putString(Constants.STOPS_SAVE_ID, new Gson().toJson(stopResponse.getStops()));
            editor.commit();
            return stopResponse.getStops();
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
