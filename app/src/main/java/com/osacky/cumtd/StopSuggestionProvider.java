package com.osacky.cumtd;

import android.content.SearchRecentSuggestionsProvider;

public class StopSuggestionProvider extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = "com.osacky.cumtd.StopSuggestionProvider";
    public static final int MODE = DATABASE_MODE_QUERIES;

    public StopSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
