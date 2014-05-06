package com.osacky.cumtd.api;

import com.osacky.cumtd.models.GetDeparturesResponse;
import com.osacky.cumtd.models.GetStopResponse;

import java.util.Map;

import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.http.QueryMap;

@SuppressWarnings("unused")
interface CUMTDApi {

    @GET("/GetStop")
    GetStopResponse getStop(
            @Query("stop_id") String stopId,
            @Query("changeset_id") String changesetId
    );

    @GET("/GetStops")
    GetStopResponse getStops(@Query("changeset_id") String changesetId);

    @GET("/GetDeparturesByStop")
    GetDeparturesResponse getDeparturesByStop(
            @Query("stop_id") String stopId,
            @QueryMap() Map optional
    );

    @GET("/GetStopsByLatLon?count=50")
    GetStopResponse getStopsByLatLon(@Query("lat") double lat, @Query("lon") double lon);

}
