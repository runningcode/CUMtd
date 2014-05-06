package com.osacky.cumtd;

import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import com.osacky.cumtd.api.CUMTDApiService;
import com.osacky.cumtd.api.GetStopsSpiceRequest;
import com.osacky.cumtd.models.Stop;
import com.osacky.cumtd.models.StopList;
import com.osacky.cumtd.models.StopPoint;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;

import static com.osacky.cumtd.Constants.ARG_SECTION_NUMBER;
import static com.osacky.cumtd.Constants.CU_LON;
import static com.osacky.cumtd.Constants.STOPS_CHANGESET_ID;

@EFragment
public class BusMapFragment extends SupportMapFragment
        implements PendingRequestListener<StopList>,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    @SuppressWarnings("unused")
    private static final String TAG = "BusMapFragment";
    private SpiceManager spiceManager = new SpiceManager(CUMTDApiService.class);
    private ClusterManager<StopPoint> mClusterManager;
    private LocationClient mLocationClient;

    @FragmentArg(ARG_SECTION_NUMBER)
    int sectionNumber;

    @AfterInject
    void setTitle() {
        ((MainActivity) getActivity()).onSectionAttached(sectionNumber);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getActivity(), this, this);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SystemBarTintManager tintManager = new SystemBarTintManager(getActivity());
        final SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
        final GoogleMap map = getMap();
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(Constants.CU_LAT,
                CU_LON), 13);

        mClusterManager = new ClusterManager<>(getActivity(), map);
        final StopPointRenderer stopPointRenderer = new StopPointRenderer(getActivity().getApplicationContext(), map,
                mClusterManager, getSpiceManager());
        mClusterManager.setRenderer(stopPointRenderer);
        mClusterManager.setOnClusterClickListener(stopPointRenderer);
        mClusterManager.setOnClusterItemClickListener(stopPointRenderer);

        map.moveCamera(cameraUpdate);
        map.setOnCameraChangeListener(mClusterManager);
        map.setOnMarkerClickListener(mClusterManager);
        map.setMyLocationEnabled(true);
        map.setPadding(0, config.getPixelInsetTop(true), config.getPixelInsetRight(),
                config.getPixelInsetBottom());
    }

    @Override
    public void onStart() {
        spiceManager.start(getActivity());
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String cacheKey = sharedPreferences.getString(STOPS_CHANGESET_ID, "");
        getSpiceManager().addListenerIfPending(StopList.class, cacheKey, this);
        mLocationClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        if (spiceManager.isStarted()) {
            spiceManager.shouldStop();
        }
        mLocationClient.disconnect();
        super.onStop();
    }

    protected SpiceManager getSpiceManager() {
        return spiceManager;
    }

    @Override
    public void onRequestNotFound() {
        getSpiceManager().execute(GetStopsSpiceRequest.getCachedSpiceRequest(getActivity()), this);
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        spiceException.printStackTrace();
    }

    @Override
    public void onRequestSuccess(StopList stops) {
        addStops(stops);
    }

    @Background
    void addStops(StopList stops) {
        for (Stop stop : stops) {
            mClusterManager.addItems(stop.getStopPoints());
        }
        updateCluster();
    }

    @UiThread
    void updateCluster() {
        mClusterManager.cluster();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location lastLocation = mLocationClient.getLastLocation();
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation
                .getLatitude(), lastLocation.getLongitude()), 16);
        getMap().moveCamera(cameraUpdate);
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        try {
            connectionResult.startResolutionForResult(getActivity(), 1);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }


}
