package com.osacky.cumtd;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.marker_info)
public class MarkerInfoView extends LinearLayout {

    @ViewById(R.id.stop_name)
    TextView stopName;

    @ViewById(R.id.departures)
    TextView departures;

    @ViewById(R.id.progress_bar)
    ProgressBar progressBar;


    public MarkerInfoView(Context context) {
        super(context);
    }

    public MarkerInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public View bind(CharSequence title, CharSequence snippet) {
        stopName.setText(title);
        if (snippet != null && snippet.length() != 0) {
            departures.setText(snippet);
            progressBar.setVisibility(View.GONE);
        }
        return this;
    }
}
