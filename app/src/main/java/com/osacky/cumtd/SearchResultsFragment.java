package com.osacky.cumtd;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_search_results)
public class SearchResultsFragment extends DialogFragment implements AdapterView
        .OnItemClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = SearchResultsFragment.class.getName();
    @ViewById(android.R.id.list)
    ListView listView;

    @FragmentArg
    String query;

    String[] from = {SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2};
    int[] to = {android.R.id.text1, android.R.id.text2};

    @AfterViews
    void setUp() {
        String selection = StopTable.SEARCH_COL + " MATCH ?";
        final Cursor cursor = getActivity().getContentResolver().query(StopsProvider.SEARCH_URI,
                null, selection, new String[]{query},
                null);
        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(getActivity(), String.format(getString(R.string.no_results), query),
                    Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 10);
            try {
                cursor.close();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return;
        } else if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
            Intent i = new Intent(getActivity(), MainActivity_.class);
            i.setAction(Intent.ACTION_VIEW);
            i.setData(Uri.withAppendedPath(StopsProvider.CONTENT_URI, String.valueOf(id)));
            startActivity(i);
            cursor.close();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 10);
            return;
        }
        getDialog().setTitle(getResources().getQuantityString(R.plurals.search_results,
                cursor.getCount(), cursor.getCount()));
        listView.setAdapter(new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_2, cursor, from, to, 0));
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(getActivity(), MainActivity_.class);
        i.setAction(Intent.ACTION_VIEW);
        i.setData(Uri.withAppendedPath(StopsProvider.CONTENT_URI, String.valueOf(id)));
        startActivity(i);
        dismiss();
    }
}
