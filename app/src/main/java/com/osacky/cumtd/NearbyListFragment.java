package com.osacky.cumtd;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
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

@EFragment
public class NearbyListFragment extends BaseSpiceListFragment
        implements PendingRequestListener<StopList>,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private LocationClient mLocationClient;
    private Location mLastLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getActivity(), this, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListShown(false);
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
        getSpiceManager().execute(GetStopsByLatLongRequest.getCachedSpiceRequest(mLastLocation
                .getLatitude(), +mLastLocation.getLongitude()), this);
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {

    }

    @Override
    public void onRequestSuccess(StopList stops) {
        setListAdapter(new ArrayAdapter<Stop>(getActivity(), android.R.layout.simple_list_item_1,
                android.R.id.text1,
                stops));
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }
}
