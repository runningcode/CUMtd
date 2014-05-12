package com.osacky.cumtd;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@EViewGroup(R.layout.marker_info)
public class MarkerInfoView extends LinearLayout {

    @ViewById(R.id.stop_name)
    TextView stopName;

    @ViewById(R.id.departures)
    TextView departures;

    @ViewById(R.id.progress_bar)
    ProgressBar progressBar;

    @ViewById(R.id.timestamp)
    TextView timeStampText;

    public MarkerInfoView(Context context) {
        super(context);
    }

    public MarkerInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public View bind(Marker marker, boolean isFav) {
        setStar(isFav);
        stopName.setText(marker.getTitle());
        if (!TextUtils.isEmpty(marker.getSnippet())) {
            progressBar.setVisibility(View.GONE);
            if (!marker.getSnippet().equals(departures.getText())) {
                // we're changing the text so update timestamp
                DateFormat dateFormat = new SimpleDateFormat("h:mm:ss a", Locale.getDefault());
                timeStampText.setText(dateFormat.format(new Date()));
            }
            departures.setText(marker.getSnippet());
        }
        return this;
    }

    void setStar(boolean isFav) {
        assert getResources() != null;
        if (isFav) {
            stopName.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.star_big_on,
                    0);
        } else {
            stopName.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.star_big_off, 0);
        }
    }
}
