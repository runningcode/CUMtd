package com.osacky.cumtd;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;

import com.octo.android.robospice.SpiceManager;
import com.osacky.cumtd.api.CUMTDApiService;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import static com.osacky.cumtd.Constants.ARG_SECTION_NUMBER;

@EFragment
public class BaseSpiceListFragment extends ListFragment {
    private SpiceManager spiceManager = new SpiceManager(CUMTDApiService.class);

    @FragmentArg(ARG_SECTION_NUMBER)
    int sectionNumber;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListShown(false);
        getListView().setClipToPadding(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getListView().setFitsSystemWindows(true);
        }
    }

    @Override
    public void onStart() {
        spiceManager.start(getActivity());
        super.onStart();
    }

    @Override
    public void onStop() {
        if (spiceManager.isStarted()) {
            spiceManager.shouldStop();
        }
        super.onStop();
    }

    protected SpiceManager getSpiceManager() {
        return spiceManager;
    }
}
