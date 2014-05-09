package com.osacky.cumtd;

import android.content.Context;
import android.content.res.Resources;
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

import java.util.List;

class GetStopResponseListener implements PendingRequestListener<GetDeparturesResponse> {

    private final String mStopId;
    private final Marker mMarker;
    private final GoogleMap mMap;
    private final SpiceManager mSpiceManager;
    private final IconGenerator mIconGenerator;
    private final Resources mResources;
    private final List<GroundOverlay> mBusMarkers;

    public GetStopResponseListener(String stopId, Marker marker, Context context,
                                   GoogleMap map, SpiceManager spiceManager,
                                   List<GroundOverlay> busMarkers) {
        mStopId = stopId;
        mMarker = marker;
        mMap = map;
        mSpiceManager = spiceManager;
        mIconGenerator = new IconGenerator(context);
        mResources = context.getResources();
        mBusMarkers = busMarkers;
    }

    @Override
    public void onRequestNotFound() {
        mSpiceManager.execute(GetDeparturesByStopRequest.getCachedSpiceRequest(mStopId), this);
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
        mBusMarkers.clear();
        final List<Departure> departures = getDeparturesResponse.getDepartures();
        StringBuilder snippet = new StringBuilder();
        if (!departures.isEmpty()) {
            String newline = "";
            for (Departure departure : departures) {
                snippet.append(newline);
                if (departure.getExpectedMins() == 0) {
                    snippet.append(String.format(mResources.getString(R.string.arriving_now),
                            departure.getHeadsign()));
                } else {
                    snippet.append(mResources.getQuantityString(
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
            snippet.append(mResources.getString(R.string.no_departures));
        }
        mMarker.setSnippet(snippet.toString());
        if (mMarker.isInfoWindowShown()) {
            mMarker.showInfoWindow();
        }
    }

    private void addIcon(String text, LatLng position) {
        GroundOverlayOptions groundOverlay = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromBitmap(mIconGenerator.makeIcon(text)))
                .position(position, 80);

        final GroundOverlay marker = mMap.addGroundOverlay(groundOverlay);
        mBusMarkers.add(marker);
    }
}
