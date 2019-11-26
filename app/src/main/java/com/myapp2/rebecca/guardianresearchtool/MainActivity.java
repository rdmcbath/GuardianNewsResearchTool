package com.myapp2.rebecca.guardianresearchtool;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements LoaderCallbacks<List<News>>,
        SwipeRefreshLayout.OnRefreshListener {
    private static final String LOG_TAG = MainActivity.class.getName();
    /**
     * URL for Guardian data from their dataset
     */
    private static final String GUARDIAN_REQUEST_URL = "http://content.guardianapis.com/search?";
    /**
     * Constant value for the news loader ID. We can choose any integer. This only comes into play
     * if you're using multiple loaders.
     */
    private static final int NEWS_LOADER_ID = 1;
    private String urlQuery = GUARDIAN_REQUEST_URL;
    private String mUserInput = "";
    private NewsAdapter mAdapter;
    private List<News> newsItems = new ArrayList<>();
    private boolean isConnected;
    SharedPreferences sharedPrefs;
    private String STATE_KEY = "keywords";
    @BindView(R.id.search_button)
    FloatingActionButton searchButton;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.news_list_view)
    RecyclerView listRecyclerView;
    @BindView(R.id.keyword_search)
    EditText keywordSearch;
    @BindView(R.id.empty_view)
    TextView emptyStateTextView;
    @BindView(R.id.loading_spinner)
    ProgressBar spinnerProgress;
    @BindView(R.id.activity_main)
    RelativeLayout relativeLayout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //set default preferences when the app starts (order by newest)
            PreferenceManager.setDefaultValues(this, R.xml.settings_main, true);
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "onCreate started");
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        spinnerProgress.setVisibility(View.VISIBLE);

        //set default preferences when the app starts (order by relevance)
        PreferenceManager.setDefaultValues(this, R.xml.settings_main, true);

        // Retrieve the keywords if there were any
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String restoredText = prefs.getString("text", null);
        if (!TextUtils.isEmpty(restoredText)) {
            keywordSearch.setText(restoredText);
        }

        //Search Button action
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG, "TEST: Search Button Clicked");

                spinnerProgress.setVisibility(View.VISIBLE);

                // Hide the soft keyboard when search button is clicked
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null :
                        getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String query = keywordSearch.getText().toString();
                // Store the keywords in shared preferences so that if the user changes menu options,
                // the keywords will be remembered when they return to the activity
                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                editor.putString("text", keywordSearch.getText().toString());
                editor.commit();

                LoaderManager loaderManager = getLoaderManager();

                if (query.isEmpty()) {
                    CharSequence text = "Nothing Entered in Search";
                    Snackbar snackbar = Snackbar
                            .make(relativeLayout, text, Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    loaderManager.restartLoader(NEWS_LOADER_ID, null, MainActivity.this);

                } else {
                    mUserInput = query;
                    urlQuery = GUARDIAN_REQUEST_URL + query;
                    loaderManager.restartLoader(NEWS_LOADER_ID, null, MainActivity.this);
                }
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        listRecyclerView.setLayoutManager(linearLayoutManager);
        // Create a new adapter that takes the list of headlines as input & initialize adapter
        mAdapter = new NewsAdapter(newsItems, this);
        // Set the adapter on the {@link ListView}so the list can be populated in the user interface
        listRecyclerView.setAdapter(mAdapter);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {

            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(NEWS_LOADER_ID, null, MainActivity.this);

        } else {
            // Update empty state with no connection error message
            Snackbar snackbar = Snackbar
                    .make(relativeLayout, R.string.no_connection, Snackbar.LENGTH_SHORT);
            snackbar.show();
            //hide loading indicator
            spinnerProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {
        Log.i(LOG_TAG, "TEST: OnCREATE LOADER CALLBACK");

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String restoredText = prefs.getString("text", null);

        if (!TextUtils.isEmpty(restoredText)) {
            Uri baseUri = Uri.parse(GUARDIAN_REQUEST_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            uriBuilder.appendQueryParameter("q", restoredText);
            uriBuilder.appendQueryParameter("api-key", getString(R.string.api_key));
            uriBuilder.appendQueryParameter("order-by", orderBy);
            uriBuilder.appendQueryParameter("show-fields", "thumbnail");
            uriBuilder.appendQueryParameter("page-size", "50");
            Log.v("TEST", "TEST" + uriBuilder.toString());

            return new NewsLoader(this, uriBuilder.toString());

        } else {
            Uri baseUri = Uri.parse(GUARDIAN_REQUEST_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            uriBuilder.appendQueryParameter("q", mUserInput);
            uriBuilder.appendQueryParameter("api-key", getString(R.string.api_key));
            uriBuilder.appendQueryParameter("order-by", orderBy);
            uriBuilder.appendQueryParameter("show-fields", "thumbnail");
            uriBuilder.appendQueryParameter("page-size", "50");
            Log.v("TEST", "TEST" + uriBuilder.toString());

            return new NewsLoader(this, uriBuilder.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "TEST: ONRESUME");
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        getLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> data) {
        Log.i(LOG_TAG, "TEST: OnLOADFINISH");

        mSwipeRefreshLayout.setRefreshing(false);

        // Hide loading indicator because the data has been loaded
        spinnerProgress.setVisibility(View.GONE);
        // Clear previous news data
        newsItems.clear();

        // If there is a valid list of {@link news}items, then add them to the adapter's
        // data set. This will trigger the RecyclerView to update.
        if (data != null && !data.isEmpty()) {
            newsItems.addAll(data);
            // Notify the Adapter of changes
            mAdapter.notifyDataSetChanged();

        } else if (!isConnected) {
            listRecyclerView.setVisibility(View.GONE);
            spinnerProgress.setVisibility(View.GONE);
            emptyStateTextView.setText(R.string.no_connection);

        } else {

            emptyStateTextView.setText(R.string.no_news);
            listRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        Log.i(LOG_TAG, "TEST:OnLOAD RESET");
        // Clear previous news data
        newsItems.clear();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getLoaderManager().destroyLoader(NEWS_LOADER_ID);
    }

    @Override
    public void onRefresh() {
        getLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        Log.i(LOG_TAG, "TEST: onSaveInstanceState");
        super.onSaveInstanceState(state);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        Log.i(LOG_TAG, "TEST: onRestoreInstanceState");
        super.onRestoreInstanceState(state);
    }
}
