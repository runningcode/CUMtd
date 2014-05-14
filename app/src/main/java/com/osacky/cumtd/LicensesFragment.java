package com.osacky.cumtd;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

@EFragment
public class LicensesFragment extends DialogFragment {

    private TextView textView;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View root = getActivity().getLayoutInflater().inflate(R.layout.fragment_licenses, null);
        assert root != null;
        textView = (TextView) root.findViewById(R.id.license_text);
        fetchLicense();
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.license_title)
                .setNegativeButton(R.string.close, null)
                .setView(root)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Tracker t = ((CUMtdApplication) getActivity().getApplication()).getTracker();
        t.setScreenName(((Object) this).getClass().getSimpleName());
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    @Background
    void fetchLicense() {
        final InputStream inputStream = getResources().openRawResource(R.raw.third_party_licenses);
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(inputStream, writer);
            textView.setText(writer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
