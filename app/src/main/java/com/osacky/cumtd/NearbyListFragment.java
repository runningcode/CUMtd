package com.osacky.cumtd;

import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import com.osacky.cumtd.api.GetStopsByLatLongRequest;
import com.osacky.cumtd.models.Stop;
import com.osacky.cumtd.models.StopList;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;

@EFragment
public class NearbyListFragment extends BaseSpiceListFragment
        implements PendingRequestListener<StopList>,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private LocationClient mLocationClient;
    private Location mLastLocation;
    private ArrayAdapter<Stop> mListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getActivity(), this, this);
        mListAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                android.R.id.text1);
        setListAdapter(mListAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mLocationClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = mLocationClient.getLastLocation();
        getSpiceManager().addListenerIfPending(
                StopList.class,
                mLastLocation.getLatitude() + mLastLocation.getLongitude(),
                this
        );
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onRequestNotFound() {
        getSpiceManager().execute(
                GetStopsByLatLongRequest.getCachedSpiceRequest(
                        mLastLocation.getLatitude(), +mLastLocation.getLongitude()),
                this
        );
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        setEmptyText(spiceException.getCause().getMessage());
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onRequestSuccess(StopList stops) {
//        mListAdapter.addAll(stops);
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @ItemClick
    void listItemClicked(Stop stop) {
//        StopDeparturesActivity_.intent(getActivity()).stopId(stop.getStopId()).start();
    }
}
