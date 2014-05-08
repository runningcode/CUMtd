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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
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
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.List;

import roboguice.util.temp.Ln;

import static com.osacky.cumtd.Constants.ARG_SECTION_NUMBER;
import static com.osacky.cumtd.Constants.PREF_GPS;
import static com.osacky.cumtd.Constants.STOPS_CHANGESET_ID;

@EFragment
@OptionsMenu(R.menu.map)
public class BusMapFragment extends SupportMapFragment
        implements PendingRequestListener<StopList>,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        GoogleMap.InfoWindowAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = "BusMapFragment";
    private static boolean GPS_ON = true;
    private boolean restore = true;
    private SpiceManager spiceManager = new SpiceManager(CUMTDApiService.class);
    private ClusterManager<Stop> mClusterManager;
    private LocationClient mLocationClient;
    private LoadingInterface mLoadingInterface;
    private List<Marker> busMarkers = new ArrayList<>();

    static {
        Ln.getConfig().setLoggingLevel(Log.ERROR);
    }

    @FragmentArg(ARG_SECTION_NUMBER)
    int sectionNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        GPS_ON = sharedPreferences.getBoolean(PREF_GPS, true);
        if (savedInstanceState == null) {
            restore = false;
        }
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String cacheKey = sharedPreferences.getString(STOPS_CHANGESET_ID, "");
        getSpiceManager().addListenerIfPending(StopList.class, cacheKey, this);
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
        getMap().clear();
        mClusterManager = new ClusterManager<>(getActivity().getApplicationContext(), getMap());
        final StopPointRenderer stopPointRenderer = new StopPointRenderer(getActivity()
                .getApplicationContext(), getMap(),
                mClusterManager, getSpiceManager(), busMarkers
        );
        mClusterManager.setRenderer(stopPointRenderer);
        mClusterManager.setOnClusterClickListener(stopPointRenderer);
        mClusterManager.setOnClusterItemClickListener(stopPointRenderer);
        getMap().setOnCameraChangeListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setInfoWindowAdapter(this);
        getMap().setMyLocationEnabled(GPS_ON);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // this is a stupid hack since actionbarsize was returning zero
            getMap().setPadding(0, 60, 0, 0);
        } else {
            getMap().setPadding(0, config.getPixelInsetTop(true), config.getPixelInsetRight(),
                    config.getPixelInsetBottom());
        }
    }

    @Override
    public void onStart() {
        spiceManager.start(getActivity());
        if (mLocationClient != null) {
            mLocationClient.connect();
        }
        super.onStart();
//        ((MainActivity) getActivity()).onSectionAttached(sectionNumber);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mClusterManager.clearItems();
        mClusterManager = null;
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
        getSpiceManager().execute(new GetStopsSpiceRequest(getActivity()), this);
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        mLoadingInterface.onLoadingFinished();
        spiceException.printStackTrace();
    }

    @Override
    public void onRequestSuccess(StopList stops) {
        addStops(stops);
    }

    @Background
    void addStops(StopList stops) {
        if (mClusterManager != null) {
            mClusterManager.addItems(stops);
            updateCluster();
        }
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
        if (!restore) {
            try {
                final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation
                        .getLatitude(), lastLocation.getLongitude()), 16);
                getMap().moveCamera(cameraUpdate);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        try {
            connectionResult.startResolutionForResult(getActivity(), 1);
        } catch (IntentSender.SendIntentException | ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        if (!TextUtils.isEmpty(marker.getTitle())) {
            return MarkerInfoView_.build(getActivity()).bind(marker.getTitle(), marker.getSnippet());
        } else {
            return null;
        }
    }

    @OptionsItem(R.id.action_location)
    void toggleLocation(MenuItem item) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        GPS_ON = !sharedPreferences.getBoolean(PREF_GPS, true);
        item.setChecked(GPS_ON);
        getMap().setMyLocationEnabled(GPS_ON);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_GPS, GPS_ON);
        editor.commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final MenuItem item = menu.findItem(R.id.action_location);
        assert item != null;
        item.setChecked(GPS_ON);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Background
    public void passIntent(Intent intent) {
        assert intent != null;
        assert intent.getData() != null;
        final Cursor cursor = getActivity().getContentResolver().query(intent.getData(), null,
                null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        String title = cursor.getString(cursor.getColumnIndex(StopTable.NAME_COL));
        String stopId = cursor.getString(cursor.getColumnIndex(StopTable.STOP_ID));
        double lat = cursor.getDouble(cursor.getColumnIndex(StopTable.LAT_COL));
        double lon = cursor.getDouble(cursor.getColumnIndex(StopTable.LON_COL));
        cursor.close();
        LatLng position = new LatLng(lat, lon);
        MarkerOptions markerOptions = new MarkerOptions().title(title).position(position);
        addMarker(markerOptions, position, stopId);
    }

    @UiThread
    void addMarker(MarkerOptions markerOptions, LatLng position, String stopId) {
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 16);
        final Marker marker = getMap().addMarker(markerOptions);
        marker.showInfoWindow();
        getMap().animateCamera(cameraUpdate);
        getSpiceManager().addListenerIfPending(GetDeparturesResponse.class, stopId,
                new GetStopResponseListener(stopId, marker, getActivity(), getMap(),
                        getSpiceManager(), busMarkers)
        );
    }
}
