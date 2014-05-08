package com.osacky.cumtd;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.WindowFeature;

@EActivity(R.layout.activity_main_map)
@WindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        LoadingInterface {

    @SuppressWarnings("unused")
    private static final String TAG = MainActivity.class.getName();
    @FragmentById(R.id.map_fragment)
    BusMapFragment mapFragment;

//    @FragmentById(R.id.navigation_drawer)
//    NavigationDrawerFragment mNavigationDrawerFragment;

//    /**
//     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
//     */
//    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setProgressBarIndeterminateVisibility(false);
//        mTitle = getTitle();
        SystemBarTintManager systemBarTintManager = new SystemBarTintManager(this);
        systemBarTintManager.setStatusBarTintEnabled(true);
        systemBarTintManager.setStatusBarTintColor(getResources().getColor(R.color.actionBarColor));
        if (savedInstanceState == null) {
            handleIntent(getIntent());
        }
    }

//    @AfterViews
//    void setUpNavigationDrawer() {
//        mNavigationDrawerFragment.setUp(
//                R.id.navigation_drawer,
//                (DrawerLayout) findViewById(R.id.drawer_layout));
//    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(
                                R.id.container,
                                BusMapFragment_.builder().sectionNumber(position + 1).build())
                        .commit();
                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, NearbyListFragment_.builder().sectionNumber
                                (position + 1).build())
                        .commit();
                break;
            default:
                throw new IllegalArgumentException("Got a position of " + position);
        }

    }

//    public void onSectionAttached(int number) {
//        switch (number) {
//            case 1:
//                mTitle = getString(R.string.title_section1);
//                break;
//            case 2:
//                mTitle = getString(R.string.title_section2);
//                break;
//            case 3:
//                mTitle = getString(R.string.title_section3);
//                break;
//        }
//    }

//    public void restoreActionBar() {
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setTitle(mTitle);
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if (!mNavigationDrawerFragment.isDrawerOpen()) {
        // Only show items in the action bar relevant to this screen
        // if the drawer is not showing. Otherwise, let the drawer
        // decide what to show in the action bar.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//            restoreActionBar();
        return true;
//        }
//        return super.onCreateOptionsMenu(menu);
    }

    @OptionsItem(R.id.action_search_settings)
    void displaySearchSettings(MenuItem item) {
        Intent intent = new Intent(Settings.ACTION_SEARCH_SETTINGS);
        try {
            startActivityForResult(intent, 1);
        } catch (ActivityNotFoundException e) {
            item.setVisible(false);
        }
    }

    @OptionsItem(R.id.action_about)
    void displayLicenses() {
        AboutFragment_.builder().build().show(getSupportFragmentManager(), "ABOUT_FRAG");
    }

    @OptionsItem(R.id.action_clear_history)
    void clearHistory() {
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                StopSuggestionProvider.AUTHORITY, StopSuggestionProvider.MODE);
        suggestions.clearHistory();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    StopSuggestionProvider.AUTHORITY, StopSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            if (mapFragment != null) {
                mapFragment.passIntent(intent);
            }
        }
    }

    @Override
    public void onLoadingStarted() {
        setProgressBarIndeterminateVisibility(true);
    }

    @Override
    public void onLoadingFinished() {
        setProgressBarIndeterminateVisibility(false);
    }
}
