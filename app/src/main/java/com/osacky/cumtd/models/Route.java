package com.osacky.cumtd.models;

public class Route {
    String routeColor;
    String routeId;
    String routeLongName;
    String routeShortName;
    String routeTextColor;

    @Override
    public String toString() {
        return routeLongName;
    }
}
