package com.osacky.cumtd;

import android.content.Context;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.octo.android.robospice.SpiceManager;
import com.osacky.cumtd.models.GetDeparturesResponse;
import com.osacky.cumtd.models.StopPoint;

import java.util.ArrayList;
import java.util.List;

public class StopPointRenderer extends DefaultClusterRenderer<StopPoint>
        implements ClusterManager.OnClusterClickListener<StopPoint>,
        ClusterManager.OnClusterItemClickListener<StopPoint> {

    private static final int MIN_CLUSTER_SIZE = 4;
    @SuppressWarnings("unused")
    private static final String TAG = StopPointRenderer.class.getName();
    private SpiceManager mSpiceManager;
    private final List<Marker> busMarkers = new ArrayList<>();
    private final Context mContext;


    public StopPointRenderer(Context context, GoogleMap map, ClusterManager<StopPoint>
            clusterManager, SpiceManager spiceManager) {
        super(context, map, clusterManager);
        mSpiceManager = spiceManager;
        mContext = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(StopPoint item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        markerOptions.title(item.getStopName());
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<StopPoint> cluster) {
        return cluster.getSize() > MIN_CLUSTER_SIZE;
    }

    @Override
    public boolean onClusterClick(Cluster<StopPoint> cluster) {
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(cluster.getPosition(),
                getMap().getCameraPosition().zoom + 1);
        getMap().animateCamera(cameraUpdate);
        return true;
    }

    @Override
    public boolean onClusterItemClick(StopPoint item) {
        final Marker marker = getMarker(item);
        mSpiceManager.addListenerIfPending(GetDeparturesResponse.class, item.getStopId(),
                new GetStopResponseListener(item.getStopId(), marker, mContext, getMap(),
                        mSpiceManager, busMarkers)
        );
        marker.showInfoWindow();
        return true;
    }


}
