package com.osacky.cumtd.models;

import java.util.List;

public class GetDeparturesResponse {
    String time;
    boolean newChangeset;
    Status stat;
    List<Departure> departures;

    public String getTime() {
        return time;
    }

    public boolean isNewChangeset() {
        return newChangeset;
    }

    public Status getStat() {
        return stat;
    }

    public List<Departure> getDepartures() {
        return departures;
    }
}
