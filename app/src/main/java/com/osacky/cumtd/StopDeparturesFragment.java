package com.osacky.cumtd;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import com.osacky.cumtd.api.GetDeparturesByStopRequest;
import com.osacky.cumtd.models.Departure;
import com.osacky.cumtd.models.GetDeparturesResponse;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

@EFragment
public class StopDeparturesFragment extends BaseSpiceListFragment
        implements PendingRequestListener<GetDeparturesResponse> {

    @FragmentArg
    String stopId;

    View headerView;
    ArrayAdapter<Departure> mListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        headerView = inflater.inflate(R.layout.departures_header, container, false);
        return null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListAdapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_list_item_1, android.R.id.text1);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(getString(R.string.no_departures));
        getListView().addHeaderView(headerView, null, false);
        setListAdapter(mListAdapter);
        if (!mListAdapter.isEmpty()) {
            setListShown(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getSpiceManager().addListenerIfPending(GetDeparturesResponse.class, stopId, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListAdapter = null;
    }

    @Override
    public void onRequestNotFound() {
        getSpiceManager().execute(GetDeparturesByStopRequest.getCachedSpiceRequest(stopId), this);
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        setEmptyText(spiceException.getCause().getMessage());
        setListShown(true);
    }

    @Override
    public void onRequestSuccess(GetDeparturesResponse response) {
        mListAdapter.addAll(response.getDepartures());
        if (isVisible()) {
            setListShown(true);
        } else {
            throw new RuntimeException("This should be visible, wtf?");
        }
    }
}
