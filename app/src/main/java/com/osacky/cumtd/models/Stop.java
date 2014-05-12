package com.osacky.cumtd.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.List;

public class Stop implements ClusterItem {
    String stopId;
    String stopName;
    String code;
    double distance;
    List<StopPoint> stopPoints;

    @Override
    public String toString() {
        return stopName;
    }

    public String getStopId() {
        return stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public String getCode() {
        return code;
    }

    public double getDistance() {
        return distance;
    }

    public List<StopPoint> getStopPoints() {
        return stopPoints;
    }

    @Override
    public LatLng getPosition() {
        return getStopPoints().get(0).getPosition();
    }

    public double getLat() {
        return getStopPoints().get(0).getStopLat();
    }

    public double getLon() {
        return getStopPoints().get(0).getStopLon();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Stop && stopId.equals(((Stop) o).getStopId());
    }

    @Override
    public int hashCode() {
        return stopId.hashCode();
    }
}
