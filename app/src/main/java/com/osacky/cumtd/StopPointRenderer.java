package com.osacky.cumtd;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
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

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StopPointRenderer extends DefaultClusterRenderer<Stop>
        implements ClusterManager.OnClusterClickListener<Stop>,
        ClusterManager.OnClusterItemClickListener<Stop>,
        GoogleMap.InfoWindowAdapter,
        GoogleMap.OnInfoWindowClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = StopPointRenderer.class.getName();
    private static final int MIN_CLUSTER_SIZE = 4;

    private final List<GroundOverlay> mBusMarkers;
    private final Context mContext;
    private SpiceManager mSpiceManager;
    private Tracker t;

    public StopPointRenderer(@NotNull Context context, GoogleMap map, ClusterManager<Stop>
            clusterManager, SpiceManager spiceManager, List<GroundOverlay> busMarkers) {
        super(context, map, clusterManager);
        mSpiceManager = spiceManager;
        mContext = context;
        mBusMarkers = busMarkers;
        CUMtdApplication app = (CUMtdApplication) context.getApplicationContext();
        if (app != null) t = app.getTracker();
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
        final LatLng position = new LatLng(item.getLat() + Constants.ZOOM_OFFSET_LAT,
                item.getLon());
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(position);
        getMap().animateCamera(cameraUpdate);
        final Marker marker = getMarker(item);
        if (t != null) {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(Constants.STOP_EVENT)
                    .setAction(item.getStopId())
                    .setLabel(Constants.STOP_CLICKED)
                    .build());
        }
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
    public View getInfoContents(final Marker marker) {
        return MarkerInfoView_.build(mContext).bind(marker);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        new IsFavAsyncQuery(mContext.getContentResolver(), marker) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                cursor.moveToFirst();
                final boolean isFav = !(cursor.getInt(cursor.getColumnIndex(StopTable.IS_FAV)) ==
                        1);
                if (isFav) {
                    Toast.makeText(mContext, mContext.getString(R.string.fav_added), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.fav_removed), Toast.LENGTH_SHORT).show();
                }
                cursor.close();
                final ContentValues contentValues = new ContentValues(1);
                contentValues.put(StopTable.IS_FAV, isFav);
                new AsyncQueryHandler(mContext.getContentResolver()) {
                }
                        .startUpdate(0, null, StopsProvider.CONTENT_URI, contentValues,
                                StopTable.NAME_COL + "=?", new String[]{mMarker.getTitle()});
            }
        }.performQuery();
    }
}
