package com.osacky.cumtd.api;

import com.google.common.collect.ImmutableMap;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.osacky.cumtd.models.GetDeparturesResponse;

public class GetDeparturesByStopRequest extends RetrofitSpiceRequest<GetDeparturesResponse, CUMTDApi> {

    private final String mStopId;
    private static final long cacheDuration = DurationInMillis.ALWAYS_EXPIRED;
    private static final int mMaxDepartures = 10;
    private static final int mTime = 60;
    private static final ImmutableMap<String, Integer> params = ImmutableMap.of(
            "pt", mTime,
            "count", mMaxDepartures
    );

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
