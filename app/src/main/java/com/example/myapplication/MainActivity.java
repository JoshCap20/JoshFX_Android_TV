package com.example.myapplication;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private EditText searchEditText;
    private Button searchButton;
    private PlayerView playerView;
    private SimpleExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchEditText = findViewById(R.id.edtSearch);
        searchButton = findViewById(R.id.btnSearch);

        playerView = findViewById(R.id.player_view);
        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "JoshFX"));

        searchButton.setOnClickListener(v -> {
            try {
                String query = URLEncoder.encode(searchEditText.getText().toString(), "UTF-8");
                String url = "https://j3tsk1.pythonanywhere.com/results/?q=" + query;
                new RetrieveFeedTask().execute(url);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });

    }


    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        View searchButton = findViewById(R.id.btnSearch);
        View searchEditText = findViewById(R.id.edtSearch);
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (searchButton.getVisibility() != View.VISIBLE || searchEditText.getVisibility() != View.VISIBLE) {
                searchButton.setVisibility(View.VISIBLE);
                searchEditText.setVisibility(View.VISIBLE);
                return true;
            }
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (searchButton.isFocused()) {
                // Add a delay before hiding the search bar
                new Handler().postDelayed(() -> {
                    searchButton.setVisibility(View.GONE);
                    searchEditText.setVisibility(View.GONE);
                }, 500);  // delay of 1 second
                return true;
            } else if (playerView.isFocused()) {
                // If the VideoView is focused, show the Pause button
                searchButton.setVisibility(View.GONE);
                searchEditText.setVisibility(View.GONE);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    class RetrieveFeedTask extends AsyncTask<String, Void, List<Movie>> {
        protected List<Movie> doInBackground(String... urls) {
            List<Movie> movies = new ArrayList<>();
            HttpURLConnection conn = null;
            try {
                URL apiURL = new URL(urls[0]);
                conn = (HttpURLConnection) apiURL.openConnection();
                conn.setRequestMethod("GET");

                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                Log.d("API Response", response.toString());
                in.close();

                // Parse JSON
                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Movie movie = new Movie(
                            jsonObject.getInt("id"),
                            jsonObject.getString("title"),
                            jsonObject.getString("link"),
                            jsonObject.getString("stream"));
                    movies.add(movie);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
            }

            return movies;
        }

        protected void onPostExecute(List<Movie> movies) {
            // Use the movies to populate an AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Select a Movie");

            CharSequence[] sequence = new CharSequence[movies.size()];
            for (int i = 0; i < movies.size(); i++) {
                sequence[i] = movies.get(i).getTitle();
            }

            builder.setItems(sequence, (dialog, which) -> {
                Movie selectedMovie = movies.get(which);
                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(selectedMovie.getStream()));
                player.setMediaItem(mediaItem);
                player.prepare();
                player.play();

                searchButton.setVisibility(View.GONE);
                searchEditText.setVisibility(View.GONE);
            });

            builder.show();
        }
    }


}

