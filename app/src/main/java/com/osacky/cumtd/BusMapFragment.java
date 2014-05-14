package com.osacky.cumtd;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import com.osacky.cumtd.api.CUMTDApiService;
import com.osacky.cumtd.api.GetStopsSpiceRequest;
import com.osacky.cumtd.models.GetDeparturesResponse;
import com.osacky.cumtd.models.Stop;
import com.osacky.cumtd.models.StopList;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.List;

import roboguice.util.temp.Ln;

import static com.osacky.cumtd.Constants.PREF_GPS;
import static com.osacky.cumtd.Constants.STOPS_CHANGESET_ID;
import static com.osacky.cumtd.Constants.ZOOM_OFFSET_LAT;

@EFragment
@OptionsMenu(R.menu.map)
public class BusMapFragment extends SupportMapFragment
        implements PendingRequestListener<StopList>,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    @SuppressWarnings("unused")
    private static final String LOG_TAG = BusMapFragment.class.getSimpleName();

    @SuppressWarnings("unused")
    private static final String TAG = BusMapFragment.class.getName();
    private static final String[] QUERY_PROJECTION = new String[]{StopTable.NAME_COL,
            StopTable.STOP_ID, StopTable.LAT_COL, StopTable.LON_COL};
    private boolean gpsOn = true;
    private SpiceManager spiceManager = new SpiceManager(CUMTDApiService.class);
    private ClusterManager<Stop> mClusterManager;
    private LocationClient mLocationClient;
    private LoadingInterface mLoadingInterface;
    private List<GroundOverlay> busMarkers = new ArrayList<>();
    private Tracker t;

    static {
        if (!BuildConfig.DEBUG) {
            Ln.getConfig().setLoggingLevel(Log.ERROR);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        gpsOn = sharedPreferences.getBoolean(PREF_GPS, true);
        setRetainInstance(true);
        t = ((CUMtdApplication) getActivity().getApplication()).getTracker();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mLocationClient = new LocationClient(getActivity(), this, this);
        try {
            mLoadingInterface = (LoadingInterface) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement LoadingInterface");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SystemBarTintManager tintManager = new SystemBarTintManager(getActivity());
        final SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
        if (getMap() == null) {
            Toast.makeText(getActivity(), "An error occurred", Toast.LENGTH_SHORT).show();
            view.setPadding(0, config.getPixelInsetTop(true), config.getPixelInsetRight(),
                    config.getPixelInsetBottom());
            return;
        }

        if (mClusterManager == null) {
            mClusterManager = new ClusterManager<>(getActivity().getApplicationContext(), getMap());
            final StopPointRenderer stopPointRenderer = new StopPointRenderer(getActivity()
                    .getApplication(), getMap(),
                    mClusterManager, getSpiceManager(), busMarkers
            );
            mClusterManager.setRenderer(stopPointRenderer);
            mClusterManager.setOnClusterClickListener(stopPointRenderer);
            mClusterManager.setOnClusterItemClickListener(stopPointRenderer);
            getMap().setInfoWindowAdapter(stopPointRenderer);
            getMap().setOnInfoWindowClickListener(stopPointRenderer);
            getMap().setOnCameraChangeListener(mClusterManager);
            getMap().setOnMarkerClickListener(mClusterManager);
            getMap().setMyLocationEnabled(gpsOn);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // this is a stupid hack since actionBarSize was returning zero
            getMap().setPadding(0, 60, 0, 0);
        } else {
            getMap().setPadding(0, config.getPixelInsetTop(true), config.getPixelInsetRight(),
                    config.getPixelInsetBottom());
        }
        if (mClusterManager.isEmpty()) {
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final String cacheKey = sharedPreferences.getString(STOPS_CHANGESET_ID, "");
            getSpiceManager().addListenerIfPending(StopList.class, cacheKey, this);
        }
    }

    @Override
    public void onStart() {
        Tracker t = ((CUMtdApplication) getActivity().getApplication()).getTracker();
        t.setScreenName(((Object) this).getClass().getSimpleName());
        t.send(new HitBuilders.AppViewBuilder().build());
        spiceManager.start(getActivity());
        if (mLocationClient != null) {
            mLocationClient.connect();
        }
        super.onStart();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mLocationClient = null;
        mLoadingInterface = null;
    }

    @Override
    public void onStop() {
        if (spiceManager.isStarted()) {
            spiceManager.shouldStop();
        }
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
        mLoadingInterface.onLoadingFinished();
        super.onStop();
    }

    protected SpiceManager getSpiceManager() {
        return spiceManager;
    }

    @Override
    public void onRequestNotFound() {
        mLoadingInterface.onLoadingStarted();
        getSpiceManager().execute(new GetStopsSpiceRequest(getActivity().getApplication()), this);
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        mLoadingInterface.onLoadingFinished();
        spiceException.printStackTrace();
    }

    @Override
    public void onRequestSuccess(StopList stops) {
        mClusterManager.clearItems();
        addStops(stops);
    }

    @Background
    void addStops(StopList stops) {
        mClusterManager.addItems(stops);
        updateCluster();
    }

    @UiThread
    void updateCluster() {
        if (mClusterManager != null) {
            mClusterManager.cluster();
        }
        if (mLoadingInterface != null) {
            mLoadingInterface.onLoadingFinished();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location lastLocation = mLocationClient.getLastLocation();
        try {
            final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation
                    .getLatitude(), lastLocation.getLongitude()), 16);
            getMap().moveCamera(cameraUpdate);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnected() {
        mLocationClient = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mLocationClient = null;
        try {
            connectionResult.startResolutionForResult(getActivity(), 1);
        } catch (IntentSender.SendIntentException | ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @OptionsItem(R.id.action_location)
    void toggleLocation(MenuItem item) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        gpsOn = !sharedPreferences.getBoolean(PREF_GPS, true);
        item.setChecked(gpsOn);
        getMap().setMyLocationEnabled(gpsOn);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_GPS, gpsOn);
        editor.commit();
        t.send(new HitBuilders.EventBuilder()
                .setCategory(Constants.GPS_EVENT)
                .setAction(String.valueOf(gpsOn))
                .build());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final MenuItem item = menu.findItem(R.id.action_location);
        assert item != null;
        item.setChecked(gpsOn);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Background
    public void passIntent(Intent intent) {
        assert intent != null;
        assert intent.getData() != null;
        final Cursor cursor = getActivity().getContentResolver().query(intent.getData(),
                QUERY_PROJECTION,
                null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        String title = cursor.getString(cursor.getColumnIndex(StopTable.NAME_COL));
        String stopId = cursor.getString(cursor.getColumnIndex(StopTable.STOP_ID));
        double lat = cursor.getDouble(cursor.getColumnIndex(StopTable.LAT_COL));
        double lon = cursor.getDouble(cursor.getColumnIndex(StopTable.LON_COL));
        cursor.close();
        final boolean favorite = intent.getBooleanExtra("favorite", false);
        final String label = favorite ? Constants.STOP_FAV_CLICK : Constants.STOP_SEARCHED;
        t.send(new HitBuilders.EventBuilder()
                .setCategory(Constants.STOP_EVENT)
                .setAction(stopId)
                .setLabel(label)
                .build());
        MarkerOptions markerOptions = new MarkerOptions().title(title).position(new LatLng(lat,
                lon));
        addMarker(markerOptions, lat, lon, stopId);
    }

    @UiThread
    void addMarker(MarkerOptions markerOptions, double lat, double lon, String stopId) {
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                new LatLng(lat + ZOOM_OFFSET_LAT, lon), 16);
        final Marker marker = getMap().addMarker(markerOptions);
        marker.showInfoWindow();
        getMap().animateCamera(cameraUpdate);
        GetStopResponseListener listener = GetStopResponseListener_.getInstance_(getActivity())
                .bind(stopId, marker, getMap(), getSpiceManager(), busMarkers);
        getSpiceManager().addListenerIfPending(GetDeparturesResponse.class, stopId, listener);
    }
}
