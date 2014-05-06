package com.osacky.cumtd.models;

public class Departure {
    String stopId;
    String headsign;
    Route route;
    Trip trip;
    String vehicleId;
    DestOrig origin;
    DestOrig destination;
    boolean isMonitored;
    boolean isScheduled;
    boolean isIsstop;
    String scheduled;
    String expected;
    int expectedMins;
    CULocation location;

    class DestOrig {
        public String stopId;
    }

    public String getStopId() {
        return stopId;
    }

    public String getHeadsign() {
        return headsign;
    }

    public Route getRoute() {
        return route;
    }

    public Trip getTrip() {
        return trip;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public DestOrig getOrigin() {
        return origin;
    }

    public DestOrig getDestination() {
        return destination;
    }

    public boolean isMonitored() {
        return isMonitored;
    }

    public boolean isScheduled() {
        return isScheduled;
    }

    public boolean isIsstop() {
        return isIsstop;
    }

    public String getScheduled() {
        return scheduled;
    }

    public String getExpected() {
        return expected;
    }

    public int getExpectedMins() {
        return expectedMins;
    }

    public CULocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return headsign + " arrives in " + expectedMins + " mins";
    }
}
