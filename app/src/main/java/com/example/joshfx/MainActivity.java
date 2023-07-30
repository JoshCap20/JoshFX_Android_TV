package com.example.joshfx;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private EditText searchEditText;
    private Button searchButton;
    private TextView movieTitle;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private List<Movie> movies;
    private int currentMovieIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchEditText = findViewById(R.id.edtSearch);
        searchButton = findViewById(R.id.btnSearch);
        movieTitle = findViewById(R.id.movieTitle);
        playerView = findViewById(R.id.player_view);

        playerView.setKeepScreenOn(true);
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
                showError("Error", e.getMessage());
            }
        });

        player.addListener(new Player.Listener() {
            public void onPlayerError(ExoPlaybackException error) {
                if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                    IOException cause = error.getSourceException();
                    if (cause instanceof HttpDataSource.HttpDataSourceException) {
                        // A network error occurred when trying to load data through a network connection.
                        HttpDataSource.HttpDataSourceException httpError = (HttpDataSource.HttpDataSourceException) cause;
                        Throwable rootCause = httpError.getCause();
                        if (rootCause instanceof SocketTimeoutException) {
                            showError("Network Error", "The connection has timed out. Please check your network connection or try again later.");
                        } else if (rootCause instanceof SocketException && rootCause.getMessage().equals("socket is closed")) {
                            showError("Network Error", "Socket is closed. Please check your network connection or try again later.");
                        }
                    }
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    showNextMoviePrompt();
                }
            }
        });
    }
    @Override
    protected void onStop() {
        super.onStop();
        playerView.setKeepScreenOn(false);
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
                showSearchInterface();
                return true;
            }
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (searchButton.isFocused()) {
                new Handler().postDelayed(() -> {
                    hideSearchInterface();
                }, 500);
                return true;
            } else if (playerView.isFocused()) {
                hideSearchInterface();
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
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                showError("Network Error", "The connection has timed out. Please check your network connection or try again later.");
            } catch (SocketException e) {
                e.printStackTrace();
                showError("Network Error", "Socket is closed. Please check your network connection or try again later.");
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error", e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return movies;
        }

        protected void onPostExecute(List<Movie> movies) {
            MainActivity.this.movies = movies;
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Select a Movie");

            CharSequence[] sequence = new CharSequence[movies.size()];
            for (int i = 0; i < movies.size(); i++) {
                sequence[i] = movies.get(i).getTitle();
            }

            builder.setItems(sequence, (dialog, which) -> {
                try {
                    Movie selectedMovie = movies.get(which);
                    currentMovieIndex = which;
                    playMovie(selectedMovie);
                } catch (Exception e) {
                    showError("Error", e.getMessage());
                }

                hideSearchInterface();
            });

            builder.show();
        }
    }

    void showError(String title, String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(title);
                builder.setMessage(message);
                builder.setPositiveButton("OK", null);
                builder.show();
            }
        });
    }

    private void hideSearchInterface() {
        searchButton.setVisibility(View.GONE);
        searchEditText.setVisibility(View.GONE);
        movieTitle.setVisibility(View.GONE);
        searchButton.clearFocus();
        searchEditText.clearFocus();
    }

    private void showSearchInterface() {
        searchButton.setVisibility(View.VISIBLE);
        searchEditText.setVisibility(View.VISIBLE);
        movieTitle.setVisibility(View.VISIBLE);
        searchEditText.requestFocus();
    }

    private void playNextMovie() {
        currentMovieIndex++;
        if (currentMovieIndex < movies.size()) {
            playMovie(movies.get(currentMovieIndex));
        } else {
            showError("End of list", "You've reached the end of the movie list.");
        }
    }

    private void playMovie(Movie movie) {
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(movie.getStream()));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                player.setMediaItem(mediaItem);
                player.prepare();
                player.play();
            }
        });
        movieTitle.setText(movie.getTitle());
    }

    private void showNextMoviePrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Play next movie?");
        builder.setMessage("Would you like to play the next movie?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                playNextMovie();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }
}

