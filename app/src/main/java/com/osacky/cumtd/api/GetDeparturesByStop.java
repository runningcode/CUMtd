package com.osacky.cumtd.api;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.osacky.cumtd.models.GetDeparturesResponse;

import java.util.HashMap;

public class GetDeparturesByStop extends RetrofitSpiceRequest<GetDeparturesResponse, CUMTDApi> {

    private final String mStopId;
    private static final long cacheDuration = DurationInMillis.ONE_MINUTE;

    public GetDeparturesByStop(String stopId) {
        super(GetDeparturesResponse.class, CUMTDApi.class);
        mStopId = stopId;
    }

    @Override
    public GetDeparturesResponse loadDataFromNetwork() throws Exception {
        return getService().getDeparturesByStop(mStopId, new HashMap());
    }

    public static CachedSpiceRequest<GetDeparturesResponse> getCachedSpiceRequest(String stopId) {
        GetDeparturesByStop departuresByStop = new GetDeparturesByStop(stopId);
        return new CachedSpiceRequest<>(departuresByStop, stopId, cacheDuration);
    }
}
