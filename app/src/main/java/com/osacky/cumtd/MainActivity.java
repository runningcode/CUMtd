package com.osacky.cumtd;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
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
        handleIntent(getIntent());
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
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment_.builder().sectionNumber
                                (position + 1).build())
                        .commit();
                break;
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
        getMenuInflater().inflate(R.menu.license_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        searchView.setQueryRefinementEnabled(true);

//            restoreActionBar();
        return true;
//        }
//        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case (R.id.action_clear_history):
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                        StopSuggestionProvider.AUTHORITY, StopSuggestionProvider.MODE);
                suggestions.clearHistory();
                return true;
            case (R.id.action_license):
                LicensesFragment_.builder().build().show(getSupportFragmentManager(), "LICENSES_FRAG");
                return true;
//            case (R.id.action_search):
//                onSearchRequested();
        }
        return super.onOptionsItemSelected(item);
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
            mapFragment.passIntent(intent);
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
