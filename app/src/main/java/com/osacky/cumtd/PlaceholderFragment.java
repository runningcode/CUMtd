package com.osacky.cumtd;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import com.osacky.cumtd.api.GetStopsSpiceRequest;
import com.osacky.cumtd.models.StopList;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import static com.osacky.cumtd.Constants.*;

@EFragment(R.layout.fragment_main)
public class PlaceholderFragment extends BaseSpiceFragment implements
        PendingRequestListener<StopList> {
    private static final String TAG = "PlaceholderFragment";

    @FragmentArg(ARG_SECTION_NUMBER)
    int sectionNumber;

    public PlaceholderFragment() {
    }

    @AfterInject
    void setTitle() {
        ((MainActivity) getActivity()).onSectionAttached(sectionNumber);
    }

    @Override
    public void onStart() {
        super.onStart();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String cacheKey = sharedPreferences.getString(STOPS_CHANGESET_ID, "");

        getSpiceManager().addListenerIfPending(StopList.class, cacheKey, this);
    }

    @Override
    public void onRequestNotFound() {
        getSpiceManager().execute(GetStopsSpiceRequest.getCachedSpiceRequest(getActivity()), this);
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        spiceException.printStackTrace();
        Toast.makeText(getActivity(), spiceException.getCause().getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestSuccess(StopList stopList) {
        final String stop = stopList.get(0).toString();
        Log.i(TAG, stop);
    }
}
