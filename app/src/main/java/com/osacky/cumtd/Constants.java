package com.osacky.cumtd;

public class Constants {

    private Constants() {
        throw new AssertionError();
    }

    public static final String STOPS_CHANGESET_ID = "stops_changeset_id";
    public static final String STOPS_SAVE_ID = "stops_save_id";
    public static final String PREF_GPS = "pref_gps";
    public static final double ZOOM_OFFSET_LAT = .0023;


    // analytics constants
    public static final String STOP_EVENT = "Stop event";
    public static final String STOP_SEARCHED = "Searched";
    public static final String STOP_FAV_CLICK = "Favorite clicked";
    public static final String STOP_CLICKED = "Stop marker clicked";
    public static final String GPS_EVENT = "GPS TOGGLED";

}
