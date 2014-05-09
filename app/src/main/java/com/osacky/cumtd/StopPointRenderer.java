package com.osacky.cumtd;

import android.content.ContentValues;
import android.content.Context;
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

    private static final int MIN_CLUSTER_SIZE = 6;
    @SuppressWarnings("unused")
    private static final String TAG = StopPointRenderer.class.getName();
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
        final float zoom = getMap().getCameraPosition().zoom;
        LatLng position = new LatLng(item.getLat() + 150.0 / Math.pow(2, zoom), item.getLon());
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(position);
        getMap().animateCamera(cameraUpdate);
        final Marker marker = getMarker(item);
        mSpiceManager.addListenerIfPending(GetDeparturesResponse.class, item.getStopId(),
                new GetStopResponseListener(item.getStopId(), marker, mContext, getMap(),
                        mSpiceManager, mBusMarkers)
        );
        marker.showInfoWindow();
        return true;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return MarkerInfoView_.build(mContext).bind(marker, false);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Stop stop = getClusterItem(marker);
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(StopTable.IS_FAV, 1);
        mContext.getContentResolver().update(StopsProvider.CONTENT_URI, contentValues,
                null, null);
    }
}
