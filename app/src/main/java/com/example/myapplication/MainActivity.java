package com.example.myapplication;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private EditText searchEditText;
    private Button searchButton;
    private FocusableVideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchEditText = findViewById(R.id.edtSearch);
        searchButton = findViewById(R.id.btnSearch);
        videoView = findViewById(R.id.videoView);

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

            videoView.start();
        });


        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString();
            try {
                query = URLEncoder.encode(query, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = "https://j3tsk1.pythonanywhere.com/api/?q=" + query;
            videoView.setVideoURI(Uri.parse(url));
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
            }
        }
        return super.dispatchKeyEvent(event);
    }

}


