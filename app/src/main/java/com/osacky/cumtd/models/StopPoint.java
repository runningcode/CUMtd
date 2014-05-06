package com.osacky.cumtd.models;

import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterItem;

public class StopPoint implements ClusterItem, GoogleMap.InfoWindowAdapter {
    String code;
    String stopId;
    double stopLat;
    double stopLon;
    String stopName;
    LatLng position;

    public String getCode() {
        return code;
    }

    public String getStopId() {
        return stopId;
    }

    public double getStopLat() {
        return stopLat;
    }

    public double getStopLon() {
        return stopLon;
    }

    public String getStopName() {
        return stopName;
    }

    @Override
    public LatLng getPosition() {
        if (position == null) {
            position = new LatLng(stopLat, stopLon);
        }
        return position;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        marker.setTitle("Blah");
        return null;
    }
}
