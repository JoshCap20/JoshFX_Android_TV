package com.example.myapplication;

import android.app.AlertDialog;
import android.media.MediaPlayer;
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
    private FocusableVideoView videoView;
    private Button pauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchEditText = findViewById(R.id.edtSearch);
        searchButton = findViewById(R.id.btnSearch);
        videoView = findViewById(R.id.videoView);
        pauseButton = findViewById(R.id.btnPause);

        pauseButton.setVisibility(View.GONE);
        videoView.setSearchButton(searchButton);

        videoView.setOnErrorListener((mp, what, extra) -> {
            Log.e("MainActivity", "Video player error: " + what);
            return true; // True if the method handled the error
        });

        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);

            mp.setOnBufferingUpdateListener((mp1, percent) -> {
                // percent is the buffering progress from 0 to 100
                Log.d("Buffering", "Buffering progress: " + percent + "%");
            });

            videoView.start();
        });

        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);

            mp.setOnBufferingUpdateListener((mp1, percent) -> {
                // percent is the buffering progress from 0 to 100
                Log.d("Buffering", "Buffering progress: " + percent + "%");
            });

            // Hide the search bar views
            searchButton.setVisibility(View.GONE);
            searchEditText.setVisibility(View.GONE);
            pauseButton.setVisibility(View.GONE);

            videoView.start();
        });


        searchButton.setOnClickListener(v -> {
                try {
                    String query = URLEncoder.encode(searchEditText.getText().toString(), "UTF-8");
                    String url = "https://j3tsk1.pythonanywhere.com/results/?q=" + query;
                    new RetrieveFeedTask().execute(url);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            });

        pauseButton.setOnClickListener(v -> {
            if(videoView.isPlaying()){
                videoView.pause();
                pauseButton.setText("Play");
            }else{
                videoView.start();
                pauseButton.setText("Pause");
            }
        });
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        View searchButton = findViewById(R.id.btnSearch);
        View searchEditText = findViewById(R.id.edtSearch);
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (searchButton.getVisibility() != View.VISIBLE || searchEditText.getVisibility() != View.VISIBLE) {
                searchButton.setVisibility(View.VISIBLE);
                searchEditText.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
                return true;
            } else if (pauseButton.isFocused()) {
                // If the VideoView is focused, show the Pause button
                pauseButton.setVisibility(View.GONE);
                videoView.requestFocus();
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
            } else if (videoView.isFocused()) {
                // If the VideoView is focused, show the Pause button
                searchButton.setVisibility(View.GONE);
                searchEditText.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                pauseButton.requestFocus();
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
                in.close();
                conn.disconnect();

                // Parse JSON
                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Movie movie = new Movie(
                            jsonObject.getInt("id"),
                            jsonObject.getString("title"),
                            jsonObject.getString("link"));
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
                // Get the selected movie
                Movie selectedMovie = movies.get(which);

                // Play the video
                videoView.setVideoURI(Uri.parse(selectedMovie.getLink()));
                videoView.start();
            });

            builder.show();
        }
    }



}

