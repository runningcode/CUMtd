package com.osacky.cumtd;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.osacky.cumtd.models.Stop;

public class NearbyListAdapter extends ArrayAdapter<Stop> {

    public NearbyListAdapter(Context context, int resource) {
        super(context, resource);
    }
}
