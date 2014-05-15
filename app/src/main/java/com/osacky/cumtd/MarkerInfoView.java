package com.osacky.cumtd;

import android.content.Context;
import android.database.Cursor;
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

    @SuppressWarnings("unused")
    private static final String LOG_TAG = MarkerInfoView.class.getSimpleName();
    private static final DateFormat dateFormat = new SimpleDateFormat("h:mm:ss a",
            Locale.getDefault());

    @ViewById(R.id.stop_name)
    protected TextView stopName;

    @ViewById(R.id.departures)
    protected TextView departures;

    @ViewById(R.id.progress_bar)
    protected ProgressBar progressBar;

    @ViewById(R.id.timestamp)
    protected TextView timeStampText;

    public MarkerInfoView(Context context) {
        super(context);
    }

    public MarkerInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarkerInfoView bind(final Marker marker) {
        stopName.setText(marker.getTitle());
        if (!TextUtils.isEmpty(marker.getSnippet())) {
            progressBar.setVisibility(View.GONE);
            if (!marker.getSnippet().equals(departures.getText())) {
                // we're changing the text so update timestamp
                timeStampText.setText(dateFormat.format(new Date()));
            }
            departures.setText(marker.getSnippet());
        }
        if (BuildConfig.DEBUG) {
            if (getContext() == null) {
                return this;
            }
            new IsFavAsyncQuery(getContext().getContentResolver(), marker) {
                @Override
                protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                    cursor.moveToFirst();
                    final boolean isFav = cursor.getInt(cursor.getColumnIndex(StopTable.IS_FAV)) == 1;
                    cursor.close();
                    setFav(isFav);
                    marker.showInfoWindow();
                }
            }.performQuery();
        }
        return this;
    }

    public void setFav(boolean isFav) {
        if (isFav) {
            stopName.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, android.R.drawable.star_big_on, 0);
        } else {
            stopName.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, android.R.drawable.star_big_off, 0);
        }
    }

}
