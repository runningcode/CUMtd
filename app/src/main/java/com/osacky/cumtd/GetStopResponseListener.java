package com.osacky.cumtd;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.ui.IconGenerator;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import com.osacky.cumtd.api.GetDeparturesByStopRequest;
import com.osacky.cumtd.models.Departure;
import com.osacky.cumtd.models.GetDeparturesResponse;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import java.util.List;

@EBean
class GetStopResponseListener implements PendingRequestListener<GetDeparturesResponse> {

    @RootContext
    Context mContext;

    String mStopId;
    Marker mMarker;
    GoogleMap mMap;
    SpiceManager mSpiceManager;
    IconGenerator mIconGenerator;
    List<GroundOverlay> mBusMarkers;

    public GetStopResponseListener bind(String stopId, Marker marker,
                                   GoogleMap map, SpiceManager spiceManager,
                                   List<GroundOverlay> busMarkers) {
        mStopId = stopId;
        mMarker = marker;
        mMap = map;
        mSpiceManager = spiceManager;
        mIconGenerator = new IconGenerator(mContext);
        mBusMarkers = busMarkers;
        return this;
    }

    @Override
    public void onRequestNotFound() {
        mSpiceManager.execute(new GetDeparturesByStopRequest(mStopId), this);
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        spiceException.printStackTrace();
    }

    @Override
    public void onRequestSuccess(GetDeparturesResponse getDeparturesResponse) {
        for (GroundOverlay bus : mBusMarkers) {
            bus.remove();
        }
        processResponse(getDeparturesResponse);
    }

    @Background
    void processResponse(GetDeparturesResponse getDeparturesResponse) {
        mBusMarkers.clear();
        final List<Departure> departures = getDeparturesResponse.getDepartures();
        StringBuilder snippet = new StringBuilder();
        if (!departures.isEmpty()) {
            String newline = "";
            for (Departure departure : departures) {
                snippet.append(newline);
                if (departure.getExpectedMins() == 0) {
                    snippet.append(String.format(mContext.getString(R.string.arriving_now),
                            departure.getHeadsign()));
                } else {
                    snippet.append(mContext.getResources().getQuantityString(
                            R.plurals.bus_arrival_time,
                            departure.getExpectedMins(),
                            departure.getHeadsign(),
                            departure.getExpectedMins()
                    ));
                }
                newline = "\n";
                mIconGenerator.setBackground(new ColorDrawable(departure
                        .getRoute().getRouteColor()));
                mIconGenerator.setTextColor(departure.getRoute().getRouteTextColor());
                addIcon(departure.getHeadsign(), departure.getLocation());
            }
        } else {
            snippet.append(mContext.getString(R.string.no_departures));
        }
        updateSnippet(snippet.toString());
    }

    @UiThread
    protected void updateSnippet(String snippet) {
        mMarker.setSnippet(snippet);
        if (mMarker.isInfoWindowShown()) {
            mMarker.showInfoWindow();
        }
    }

    @UiThread
    protected void addIcon(String text, LatLng position) {
        GroundOverlayOptions groundOverlay = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromBitmap(mIconGenerator.makeIcon(text)))
                .position(position, 80);

        final GroundOverlay marker = mMap.addGroundOverlay(groundOverlay);
        mBusMarkers.add(marker);
    }
}
