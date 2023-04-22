package com.example.newsappca;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private ProgressBar mProgressBar; // Progress bar to show while waiting for API response
    private RecyclerView mRecyclerView; // RecyclerView to show news articles
    private ArrayList<NewsArticle> mArticlesList; // List of news articles
    private ViewAdapter mViewAdapter; // Adapter to connect news articles to RecyclerView
    private String searchQuery=""; // Query to search for specific news articles

    private String categoryQuery=""; // Query to filter news articles by category
    private String nextPage=""; // Token for next page of news articles

    private SharedPreferences sharedPreferences; // SharedPreferences object to store favorite query
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mRecyclerView = findViewById(R.id.recyclerViewMain);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mArticlesList = new ArrayList<>();
        mViewAdapter = new ViewAdapter(getApplicationContext(), mArticlesList);
        mRecyclerView.setAdapter(mViewAdapter);

        mProgressBar=findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        newsAPI_call(); // Make API call to get news articles
    }

    // Create options menu for toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);

        // Find search item in menu and set query hint
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Enter keyword to search for: ");

        // Set listener for searchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchQuery=s;
                categoryQuery=""; // Resets category query as now filtering by user query not category
                newsAPI_call(); // Make API call with new search query
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        // Set listener for searchView focus change
        searchView.setOnQueryTextFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                searchItem.collapseActionView(); // Collapse searchView when focus is lost
                searchQuery="";
                newsAPI_call();
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.searchFavQuery:
                // Get the shared preferences and retrieve the saved favorite query
                sharedPreferences = getSharedPreferences("MYPREFS", MODE_PRIVATE);
                String query = sharedPreferences.getString("favQuery", "");

                // Make API call if a query exists
                if (!query.equals("")){
                    searchQuery = query;
                    newsAPI_call();
                }
                // Otherwise, show a toast to prompt the user to set a favorite query first
                else {
                    Toast.makeText(MainActivity.this, "Please set a favourite query first!", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.changeFavQuery:
                // Create an alert dialog to prompt the user to enter a new favorite query
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Change Favorite Query");
                builder.setMessage("Please enter a new favorite query");

                // Inflate the layout containing the edit text and buttons for the dialog
                View view = LayoutInflater.from(this).inflate(R.layout.edit_query, null);
                builder.setView(view);

                // Find the edit text and buttons from the layout
                Button btnSave = view.findViewById(R.id.btnSave);
                Button btnCancel = view.findViewById(R.id.btnCancel);
                EditText editText = view.findViewById(R.id.edit_query);

                // Create the dialog and set click listeners for the buttons
                AlertDialog dialog = builder.create();
                btnSave.setOnClickListener(v -> {
                    // When the save button is clicked, save the new favorite query to shared preferences
                    sharedPreferences = getSharedPreferences("MYPREFS", MODE_PRIVATE);
                    editor = sharedPreferences.edit();
                    editor.putString("favQuery", editText.getText().toString());
                    editor.apply(); // Commit the changes to shared preferences
                    dialog.dismiss(); // Dismiss the dialog
                    Toast.makeText(MainActivity.this, "Updated favourite query!", Toast.LENGTH_SHORT).show();
                });
                btnCancel.setOnClickListener(v -> dialog.dismiss()); // If the cancel button is clicked, dismiss the dialog
                dialog.show();
                return true;
            case R.id.restartApp:
                recreate(); // Restart the activity
                return true;
            case R.id.closeApp:
                finishAffinity(); // Close the app
                return true;
            case R.id.visitWebsite:
                // Opens the webpage in the default browser when clicked
                Uri webpage = Uri.parse("https://danielmoulton.me");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item); // Handle all other options by calling the superclass method
    }


    @Override
    public void onClick(View view) {

        // Gets clicked button and it's text
        Button clickedButton = (Button) view;
        String buttonText = clickedButton.getText().toString();

        // If category search query already equals that button, reset the category query
        if (Objects.equals(categoryQuery, buttonText)) {
            categoryQuery = "";
        }
        // Otherwise, category query set to that button's text
        else{
                categoryQuery = buttonText;
            }
        nextPage="";
        newsAPI_call(); // Call API with updated categories

    }

    // Method to call API to retrieve news articles
    public void newsAPI_call() {

        // Get the current size of the articles list and clear it
        int currentSize=mArticlesList.size();
        mArticlesList.clear();

        // Notify the adapter that the items have been removed from the list
        mViewAdapter.notifyItemRangeRemoved(0, currentSize);

        // Get the API key and create an OkHttpClient object
        String apiKey = getString(R.string.api_key);
        OkHttpClient client = new OkHttpClient();

        // Build the API URL based on the search and category queries
        String apiUrl;

        // Category cannot be empty so if empty, use url without categoryQuery
        if (categoryQuery.isEmpty()){
            apiUrl = "https://newsdata.io/api/1/news?apikey=" + apiKey + "&country=gb&qInTitle=" + searchQuery + "&page=" + nextPage;
        }
        else {
            apiUrl = "https://newsdata.io/api/1/news?apikey="+apiKey+"&country=gb&category="+categoryQuery+"&page="+nextPage;
        }

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        // Make the API call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "onFailure: error=", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Get the JSON response as a string
                assert response.body() != null;
                String jsonData = response.body().string();

                try {
                    // Parse the JSON data into a JSONObject and get the results array
                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONArray jsonArray = jsonObject.getJSONArray("results");

                    // Create NewsArticle object for each JSONObject
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject current = jsonArray.getJSONObject(i);
                        NewsArticle currentArticle = new NewsArticle();

                        // Get the article properties from the JSONObject
                        String author = current.optString("source_id");
                        String title = current.optString("title");
                        String description = current.optString("description");
                        String urlToImage = current.optString("image_url");
                        String pubDate = current.optString("pubDate");
                        String content = current.optString("content");

                        // Set the properties of the NewsArticle object
                        currentArticle.setAuthor(author);
                        currentArticle.setTitle(title);
                        currentArticle.setDescription(description);
                        currentArticle.setUrlToImage(urlToImage);
                        currentArticle.setPubDate(pubDate);
                        currentArticle.setContent(content);

                        // Add the NewsArticle object to the articles list
                        mArticlesList.add(currentArticle);
                    }

                    // Update the UI with the new articles list and hide the progress bar
                    runOnUiThread(() -> {
                        mViewAdapter = new ViewAdapter(getApplicationContext(), mArticlesList);
                        mRecyclerView.setAdapter(mViewAdapter);
                        mProgressBar.setVisibility(View.GONE);
                    });

                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: error=", e);
                }
            }
        });
    }

    // Method to load the next page of news articles
    public void loadNextPage(View view) {
        newsAPI_call();
    }
}
