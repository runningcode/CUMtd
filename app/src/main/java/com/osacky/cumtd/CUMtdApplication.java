package com.osacky.cumtd;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

public class CUMtdApplication extends Application {

    private Tracker tracker;

    public synchronized Tracker getTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.enableAutoActivityReports(this);
            if (BuildConfig.DEBUG) {
                analytics.setDryRun(true);
            } else {
                analytics.getLogger().setLogLevel(Logger.LogLevel.ERROR);
            }
            tracker = analytics.newTracker(R.xml.global_tracker);
        }
        return tracker;
    }
}
