package com.myapp2.rebecca.guardianresearchtool;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rebecca on 1/3/2017.
 */

public final class Utils {
    public static final String LOG_TAG = Utils.class.getSimpleName();

    /**
     * This class is meant to hold static variables and methods, which can be accessed
     * directly from the class named Utils
     */
    private Utils() {
    }

    /**
     * Query the Guardian dataset and return a list of {@link News} objects.
     */
    public static List<News> fetchNewsData(String requestUrl) {
        Log.i(LOG_TAG, requestUrl);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "TEST: Problem making the HTTP request", e);
        }

        // Extract relevant fields from the JSON response and create an {@link News} object
        List<News> newsItems = extractFeatureFromJson(jsonResponse);
        // Return the list of {@link newsItems}
        return newsItems;
    }

    /**
     * Return a list of {@link News} objects that has been built from parsing a JSON response.
     */

    public static List<News> extractFeatureFromJson(String GuardianJSON) {

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(GuardianJSON)) {
            return null;
        }
        // Create an empty ArrayList that we can start adding news items to
        List<News> newsItems = new ArrayList<>();

        // Try to parse the JSON Response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown. Catch the
        // exception so the app doesn't crash, and print the error message to the logs.
        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(GuardianJSON);
            JSONObject response = baseJsonResponse.getJSONObject("response");
            JSONArray resultsArray = response.getJSONArray("results");

            // Iterate through our JSONArray structure, working with one news item at a time
            for (int i = 0; i < resultsArray.length(); ++i) {
                // Get a single object at position i within the list
                JSONObject newsItem = resultsArray.getJSONObject(i);
                // Grab the title
                String webTitle = "No Title Found";
                if (newsItem.has("webTitle")) {
                    // get article title
                    webTitle = newsItem.getString("webTitle");
                }
                // Grab the section
                String webSectionName = "No Section Name Found";
                if (newsItem.has("sectionName")) {
                    // get article section name
                    webSectionName = newsItem.getString("sectionName");
                }
                // Grab the date published
                String webPublicationDate = "No Date Found";
                if (newsItem.has("webPublicationDate")) {
                    // get article publish date
                    webPublicationDate = newsItem.getString("webPublicationDate");
                }
                StringBuilder formattedDate = new StringBuilder(webPublicationDate);
                for (int j = 0; j < webPublicationDate.length(); j++) {
                    if (webPublicationDate.charAt(j) == 'T' || webPublicationDate.charAt(j) == 'Z')
                        formattedDate.setCharAt(j, ' ');
                }
                webPublicationDate = formattedDate.toString();
                // Grab the website address for the article
                String webUrl = "No URL Found";
                if (newsItem.has("webUrl")) {
                    // get article URL
                    webUrl = newsItem.getString("webUrl");
                }

                // Grab the thumbnail (in a nested Json object)
                String webThumbnail = "http://www.thedailymash.co.uk/images/stories/guardian1.jpg";
                if (newsItem.has("fields")) {
                    JSONObject fields = newsItem.getJSONObject("fields");
                    if (fields.has("thumbnail")) {
                        webThumbnail = fields.getString("thumbnail");
                    }
                }

                // Create a new {@link newsItem} object
                News currentNewsItem = new News
                        (webTitle, webSectionName, webPublicationDate, webUrl, webThumbnail);

                newsItems.add(currentNewsItem);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "UTILS: Problem parsing Guardian JSON results", e);
        }
        return newsItems;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "UTILS: Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If request was successful (response code 200), read input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the Guardian JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies that an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the whole
     * JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}


