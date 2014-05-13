package com.osacky.cumtd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.octo.android.robospice.SpiceManager;
import com.osacky.cumtd.models.GetDeparturesResponse;
import com.osacky.cumtd.models.Stop;

import java.util.List;

public class StopPointRenderer extends DefaultClusterRenderer<Stop>
        implements ClusterManager.OnClusterClickListener<Stop>,
        ClusterManager.OnClusterItemClickListener<Stop>,
        GoogleMap.InfoWindowAdapter,
        GoogleMap.OnInfoWindowClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = StopPointRenderer.class.getName();
    private static final String[] IS_FAV_PROJECTION = new String[]{StopTable.IS_FAV};
    private static final int MIN_CLUSTER_SIZE = 4;
    private SpiceManager mSpiceManager;
    private final List<GroundOverlay> mBusMarkers;
    private final Context mContext;

    public StopPointRenderer(Context context, GoogleMap map, ClusterManager<Stop>
            clusterManager, SpiceManager spiceManager, List<GroundOverlay> busMarkers) {
        super(context, map, clusterManager);
        mSpiceManager = spiceManager;
        mContext = context;
        mBusMarkers = busMarkers;
    }

    @Override
    protected void onBeforeClusterItemRendered(Stop item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        markerOptions.title(item.getStopName());
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<Stop> cluster) {
        return cluster.getSize() > MIN_CLUSTER_SIZE;
    }

    @Override
    public boolean onClusterClick(Cluster<Stop> cluster) {
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(cluster.getPosition(),
                getMap().getCameraPosition().zoom + 1.5f);
        getMap().animateCamera(cameraUpdate);
        return true;
    }

    @Override
    public boolean onClusterItemClick(Stop item) {
        LatLng position = new LatLng(item.getLat() + Constants.ZOOM_OFFSET_LAT, item.getLon());
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(position);
        getMap().animateCamera(cameraUpdate);
        final Marker marker = getMarker(item);
        GetStopResponseListener listener = GetStopResponseListener_.getInstance_(mContext)
                .bind(item.getStopId(), getMarker(item), getMap(), mSpiceManager, mBusMarkers);
        mSpiceManager.addListenerIfPending(GetDeparturesResponse.class, item.getStopId(), listener);
        marker.showInfoWindow();
        return true;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        boolean isFav = isMarkerFav(marker);
        return MarkerInfoView_.build(mContext).bind(marker, isFav);
    }

    private boolean isMarkerFav(Marker marker) {
        final Cursor cursor = mContext.getContentResolver().query(StopsProvider.CONTENT_URI,
                IS_FAV_PROJECTION,
                StopTable.NAME_COL + "=?",
                new String[]{marker.getTitle()},
                null);
        boolean isFav;
        assert cursor != null;
        cursor.moveToFirst();
        isFav = cursor.getInt(cursor.getColumnIndex(StopTable.IS_FAV)) == 1;
        cursor.close();
        return isFav;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        boolean isFav = isMarkerFav(marker);
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(StopTable.IS_FAV, !isFav);
        mContext.getContentResolver().update(StopsProvider.CONTENT_URI, contentValues,
                StopTable.NAME_COL + "=?", new String[]{marker.getTitle()});
        marker.showInfoWindow();
    }
}
