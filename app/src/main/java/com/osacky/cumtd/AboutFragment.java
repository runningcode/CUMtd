package com.osacky.cumtd;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_about)
public class AboutFragment extends DialogFragment {

    @ViewById(R.id.version_number)
    TextView versionText;

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
    @SuppressWarnings("ConstantConditions")
    void setUp() {
        getDialog().setTitle(getString(R.string.about_menu));
        try {
            versionText.setText(getActivity().getPackageManager().getPackageInfo(getActivity()
                    .getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
