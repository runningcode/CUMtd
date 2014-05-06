package com.osacky.cumtd.api;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.osacky.cumtd.models.GetStopResponse;
import com.osacky.cumtd.models.StopList;

public class GetStopsByLatLongRequest extends RetrofitSpiceRequest<StopList, CUMTDApi> {

    private final double mLat;
    private final double mLon;

    private static final long cacheDuration = DurationInMillis.ONE_MINUTE;

    public GetStopsByLatLongRequest(double lat, double lon) {
        super(StopList.class, CUMTDApi.class);
        mLat = lat;
        mLon = lon;
    }

    @Override
    public StopList loadDataFromNetwork() throws Exception {
        final GetStopResponse stopResponse = getService().getStopsByLatLon(mLat, mLon);
        return stopResponse.getStops();
    }

    public static CachedSpiceRequest<StopList> getCachedSpiceRequest(
            double lat, double lon) {
        GetStopsByLatLongRequest departuresByStop = new GetStopsByLatLongRequest(lat, lon);
        return new CachedSpiceRequest<>(departuresByStop, lat + lon, cacheDuration);
    }
}
