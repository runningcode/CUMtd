package com.osacky.cumtd;

import android.support.v4.app.DialogFragment;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

@EFragment(R.layout.fragment_licenses)
public class LicensesFragment extends DialogFragment {

    @ViewById(R.id.license_text)
    TextView textView;

    @AfterViews
    void setUp() {
        getDialog().setTitle(getString(R.string.license_title));
        fetchLicense();
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
