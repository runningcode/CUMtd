package com.osacky.cumtd;

import android.support.v4.app.Fragment;

import com.octo.android.robospice.SpiceManager;
import com.osacky.cumtd.api.CUMTDApiService;

import org.androidannotations.annotations.EFragment;

@EFragment
public class BaseSpiceFragment extends Fragment {
    private SpiceManager spiceManager = new SpiceManager(CUMTDApiService.class);

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
