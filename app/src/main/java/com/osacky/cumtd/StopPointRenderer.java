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
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import com.osacky.cumtd.api.GetDeparturesByStopRequest;
import com.osacky.cumtd.models.Departure;
import com.osacky.cumtd.models.GetDeparturesResponse;
import com.osacky.cumtd.models.StopPoint;

import java.util.List;

public class StopPointRenderer extends DefaultClusterRenderer<StopPoint>
        implements ClusterManager.OnClusterClickListener<StopPoint>,
        ClusterManager.OnClusterItemClickListener<StopPoint> {

    private static final int MIN_CLUSTER_SIZE = 6;
    @SuppressWarnings("unused")
    private static final String TAG = "StopPointRenderer";
    private SpiceManager mSpiceManager;

    private Context mContext;

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
                new GetStopResponseListener(item.getStopId(), marker));
        marker.showInfoWindow();
        return true;
    }

    private class GetStopResponseListener implements PendingRequestListener<GetDeparturesResponse> {

        private final String mStopId;
        private final Marker mMarkerOptions;

        public GetStopResponseListener(String stopId, Marker markerOptions) {
            mStopId = stopId;
            mMarkerOptions = markerOptions;
        }

        @Override
        public void onRequestNotFound() {
            mSpiceManager.execute(GetDeparturesByStopRequest.getCachedSpiceRequest(mStopId), this);
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            spiceException.printStackTrace();
        }

        @Override
        public void onRequestSuccess(GetDeparturesResponse getDeparturesResponse) {
            final List<Departure> departures = getDeparturesResponse.getDepartures();
            StringBuilder snippet = new StringBuilder();
            if (!departures.isEmpty()) {
                String newline = "";
                for (Departure departure : departures) {
                    snippet.append(newline);
                    if (departure.getExpectedMins() == 0) {
                        snippet.append(String.format(mContext.getString(R.string.arriving_now),
                                departure.getHeadsign()));
                    } else {
                        snippet.append(mContext.getResources().getQuantityString(
                                R.plurals.bus_arrival_time,
                                departure.getExpectedMins(),
                                departure.getHeadsign(),
                                departure.getExpectedMins()
                        ));
                    }
                    newline = "\n";
                }
            } else {
                snippet.append(mContext.getString(R.string.no_departures));
            }
            mMarkerOptions.setSnippet(snippet.toString());
            if (mMarkerOptions.isInfoWindowShown()) {
                mMarkerOptions.showInfoWindow();
            }
        }
    }
}
