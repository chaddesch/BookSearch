package com.example.chad.booksearch;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import static android.R.attr.value;

public class BookSearchActivity extends AppCompatActivity {

    private static final String LOG_TAG = BookSearchActivity.class.getName();

    private static final String GOOGLE_BOOKS_API_BASE_QUERY =
            "https://www.googleapis.com/books/v1/volumes?maxResults=20&q=";

    private Button mButton;
    private EditText mEditText;
    private BookAdapter mBookAdapter;
    private ListView mListView;
    private ArrayList<Book> mBookList;
    private TextView mEmptyStateTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_book_search);

        mButton = (Button) findViewById(R.id.search_button);
        mEditText = (EditText) findViewById(R.id.search_edit_text);

        mBookList = new ArrayList<>();

        // Create the adapter to convert the array to views
        mBookAdapter = new BookAdapter(this, mBookList);

        // Attach the adapter to a ListView
        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(mBookAdapter);


        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();


        mButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (networkInfo != null && networkInfo.isConnected()) {

                    new BookAsyncTask().execute();
                } else {
                    // Update empty state with no connection error message
                    mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
                    mListView.setEmptyView(mEmptyStateTextView);
                    View loadingIndicator = findViewById(R.id.loading_indicator);
                    loadingIndicator.setVisibility(View.GONE);
                    mEmptyStateTextView.setText("No Internet Connection");
                }
            }
        });

        if (savedInstanceState != null) {
            mBookList = (ArrayList<Book>) savedInstanceState.getSerializable("key");
            mBookAdapter.clear();
            mBookAdapter.addAll(mBookList);
            mBookAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        savedState.putSerializable("key", mBookList);

    }


    /**
     * update the screen to display information from the given {@link Book}
     *
     * @param bookList
     */
    private void updateUi(ArrayList<Book> bookList) {
        mBookList.clear();

        if (bookList == null) {
            mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
            mListView.setEmptyView(mEmptyStateTextView);
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText("No Results found or invalid query entered. Please enter valid search terms.");
        } else {


            mBookList.clear();
            mBookList.addAll(bookList);

            mBookAdapter.notifyDataSetChanged();
            mListView.setAdapter(new BookAdapter(BookSearchActivity.this, mBookList));
        }
    }

    /**
     * Returns new URL object from the given String URL.
     */
    private URL createUrl(String stringUrl) {

        URL url = null;

        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private String makeHTTPRequest(URL url) throws IOException {
        String jsonResponse = "";

        // if url is null, then return early
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();
            // if the request was successful (response code 200);
            // then read the input stream and parse the response
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);

            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem receiving book JSON results", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                //function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private String readFromStream(InputStream inputStream) throws IOException {
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

    private ArrayList<Book> extractFeatureFromJson(String booksJSON) {

        // if JSON string is empty or null, return early
        if (TextUtils.isEmpty(booksJSON)) {
            return null;
        }

        ArrayList booksList = new ArrayList();

        try {
            JSONObject baseJsonResponse = new JSONObject(booksJSON);
            JSONArray itemsArray = baseJsonResponse.getJSONArray("items");


            // If there are results in the features array
            for (int i = 0; i < itemsArray.length(); i++) {
                //Extract out the first feature
                JSONObject firstFeature = itemsArray.getJSONObject(i);
                JSONObject items = firstFeature.getJSONObject("volumeInfo");

                //Extract out the title and author(s)
                String title = items.getString("title");
                String authors = "";
                JSONArray authorJsonArray = items.optJSONArray("authors");

                if (items.has("authors")) {
                    if (authorJsonArray.length() > 0) {
                        for (int j = 0; j < authorJsonArray.length(); j++) {
                            authors = authorJsonArray.optString(j) + "";
                        }
                    }
                }

                //Create a new {@link Event} object, add the book to the array
                booksList.add(new Book(title, authors));
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the book JSON results", e);
            return null;
        }
        return booksList;
    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first book in the response.
     */
    private class BookAsyncTask extends AsyncTask<URL, Void, ArrayList<Book>> {

        /**
         * Check connectivity
         *
         * @return
         */
        public boolean isOnline() {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        }

        String userInput = mEditText.getText().toString().replace(" ", "+");

        @Override
        protected ArrayList<Book> doInBackground(URL... urls) {


            if (userInput.length() > 1) {
            } else if (userInput == null || userInput.equals("")) {
                Log.e(LOG_TAG, "No search terms entered.");
                return null;
            }

            URL url = createUrl(GOOGLE_BOOKS_API_BASE_QUERY + userInput);
            Log.v("EditText", url.toString());

            //Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHTTPRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem making the HTTP request.", e);
            }

            //Extract relevant fields from the JSON response and create an {@link Event} object

            // Return the {@link Book} object as the result of the {@link BookAsyncTask}

            return extractFeatureFromJson(jsonResponse);
        }


        /**
         * Update the screen with the given book (which was the result of the
         * {@link BookAsyncTask}
         */
        @Override
        protected void onPostExecute(ArrayList<Book> bookList) {

            boolean network = isOnline();
            Log.v(LOG_TAG, "Network is online = " + network);

            if (!network) {
                Toast.makeText(BookSearchActivity.this, "Please connect to the Internet.", Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, "Not connected to the Internet");
            }


            updateUi(bookList);
        }
    }
}