package com.osacky.cumtd;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class AboutFragment extends DialogFragment {

    @SuppressWarnings("ConstantConditions")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View layout = getActivity().getLayoutInflater().inflate(R.layout.fragment_about,
                null);
        assert layout != null;

        final TextView versionText = (TextView) layout.findViewById(R.id.version_number);
        final Button source = (Button) layout.findViewById(R.id.show_source);
        final Button licenses = (Button) layout.findViewById(R.id.show_licenses);

        try {
            versionText.setText(getActivity().getPackageManager().getPackageInfo(getActivity()
                    .getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
            e.printStackTrace();
        }

        source.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tracker t = ((CUMtdApplication) getActivity().getApplication()).getTracker();
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("View licenses")
                        .setAction("View")
                        .build());
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github" +
                        ".com/runningcode/cumtd"));
                startActivity(i);
            }
        });

        licenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LicensesFragment_.builder().build().show(getActivity().getSupportFragmentManager(),
                        "LICENSE_TAG");
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.about_menu)
                .setView(layout)
                .setPositiveButton(getString(R.string.close), null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Tracker t = ((CUMtdApplication) getActivity().getApplication()).getTracker();
        t.setScreenName(((Object) this).getClass().getSimpleName());
        t.send(new HitBuilders.AppViewBuilder().build());
    }
}
