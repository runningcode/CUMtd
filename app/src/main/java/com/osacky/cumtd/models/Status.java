package com.osacky.cumtd.models;

public class Status {
    int code;
    String msg;

    @Override
    public String toString() {
        return code + " " + msg;
    }
}
