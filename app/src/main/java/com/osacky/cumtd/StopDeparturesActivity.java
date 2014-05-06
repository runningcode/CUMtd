package com.osacky.cumtd;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_blank)
public class StopDeparturesActivity extends ActionBarActivity {

    @Extra
    String stopId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(
                    R.id.container,
                    StopDeparturesFragment_.builder().stopId(stopId).build()
            ).commit();
        }
    }
}
