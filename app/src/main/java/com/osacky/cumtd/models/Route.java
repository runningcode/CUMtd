package com.osacky.cumtd.models;

import android.graphics.Color;

public class Route {
    String routeColor;
    String routeId;
    String routeLongName;
    String routeShortName;
    String routeTextColor;

    public int getRouteColor() {
        return Color.parseColor("#" + routeColor);
    }

    public String getRouteId() {
        return routeId;
    }

    public String getRouteLongName() {
        return routeLongName;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public int getRouteTextColor() {
        return Color.parseColor("#" + routeTextColor);
    }

    @Override
    public String toString() {
        return routeLongName;
    }
}
