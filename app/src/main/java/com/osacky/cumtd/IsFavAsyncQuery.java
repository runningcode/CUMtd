package com.osacky.cumtd;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;

import com.google.android.gms.maps.model.Marker;

class IsFavAsyncQuery extends AsyncQueryHandler {

    private static final String[] IS_FAV_PROJECTION = new String[]{StopTable.IS_FAV};
    protected final Marker mMarker;

    public IsFavAsyncQuery(ContentResolver cr, Marker marker) {
        super(cr);
        mMarker = marker;
    }

    public void performQuery() {
        startQuery(
                0,
                null,
                StopsProvider.CONTENT_URI,
                IS_FAV_PROJECTION,
                StopTable.NAME_COL + "=?",
                new String[]{mMarker.getTitle()},
                null
        );
    }
}
