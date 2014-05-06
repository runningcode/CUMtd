package com.osacky.cumtd.models;

public class GetStopResponse {
    String changesetId;
    boolean newChangeset;
    Status stat;
    String time;
    StopList stops;

    public String getChangesetId() {
        return changesetId;
    }

    public boolean isNewChangeset() {
        return newChangeset;
    }

    public Status getStat() {
        return stat;
    }

    public String getTime() {
        return time;
    }

    public StopList getStops() {
        return stops;
    }
}
