package com.osacky.cumtd;

import android.support.v4.app.Fragment;

import com.octo.android.robospice.SpiceManager;
import com.osacky.cumtd.api.CUMTDApiService;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import static com.osacky.cumtd.Constants.ARG_SECTION_NUMBER;

@EFragment
public class BaseSpiceFragment extends Fragment {
    private SpiceManager spiceManager = new SpiceManager(CUMTDApiService.class);

    @FragmentArg(ARG_SECTION_NUMBER)
    int sectionNumber;

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
