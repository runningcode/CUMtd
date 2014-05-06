package com.osacky.cumtd.models;

import java.util.List;

public class Stop {
    String stopId;
    String stopName;
    String code;
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

    public List<StopPoint> getStopPoints() {
        return stopPoints;
    }
}
