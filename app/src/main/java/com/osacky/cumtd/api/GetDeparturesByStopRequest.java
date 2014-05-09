package com.osacky.cumtd.api;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.osacky.cumtd.models.GetDeparturesResponse;

import java.util.HashMap;

public class GetDeparturesByStopRequest extends RetrofitSpiceRequest<GetDeparturesResponse, CUMTDApi> {

    private final String mStopId;
    private static final long cacheDuration = DurationInMillis.ALWAYS_EXPIRED;
    private static final int mMaxDepartures = 10;
    private static final int mTime = 60;
    private static final HashMap<String, Integer> params = new HashMap<>();

    static {
        params.put("pt", mTime);
        params.put("count", mMaxDepartures);
    }

    public GetDeparturesByStopRequest(String stopId) {
        super(GetDeparturesResponse.class, CUMTDApi.class);
        mStopId = stopId;
    }

    @Override
    public int getPriority() {
        return Thread.MIN_PRIORITY + 1;
    }

    @Override
    public GetDeparturesResponse loadDataFromNetwork() throws Exception {
        return getService().getDeparturesByStop(mStopId, params);
    }

    public static CachedSpiceRequest<GetDeparturesResponse> getCachedSpiceRequest(String stopId) {
        GetDeparturesByStopRequest departuresByStop = new GetDeparturesByStopRequest(stopId);
        return new CachedSpiceRequest<>(departuresByStop, stopId, cacheDuration);
    }
}
