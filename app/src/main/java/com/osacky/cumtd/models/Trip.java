package com.osacky.cumtd.models;

public class Trip {
    String tripId;
    String tripHeadsign;
    String routeId;
    String blockId;
    String direction;
    String serviceId;
    String shapedId;

    public String getTripId() {
        return tripId;
    }

    public String getTripHeadsign() {
        return tripHeadsign;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getBlockId() {
        return blockId;
    }

    public String getDirection() {
        return direction;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getShapedId() {
        return shapedId;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Trip && tripId.equals(((Trip) o).tripId);
    }

    @Override
    public int hashCode() {
        return tripId.hashCode();
    }
}
