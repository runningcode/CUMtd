package com.osacky.cumtd;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_about)
public class AboutFragment extends DialogFragment {

    @Click(R.id.show_source)
    void showSource() {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github" +
                ".com/runningcode/cumtd"));
        startActivity(i);
    }

    @Click(R.id.show_licenses)
    void viewLicenses() {
        LicensesFragment_.builder().build().show(getActivity().getSupportFragmentManager(),
                "LICENSE_TAG");
    }

    @AfterViews
    void setUp() {
        getDialog().setTitle(getString(R.string.about_menu));
    }
}
