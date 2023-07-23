package com.example.myapplication;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button searchButton = findViewById(R.id.btnSearch);
        EditText searchEditText = findViewById(R.id.edtSearch);
        VideoView videoView = findViewById(R.id.videoView);

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

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);

                mp.setOnBufferingUpdateListener((mp1, percent) -> {
                    // percent is the buffering progress from 0 to 100
                    Log.d("Buffering", "Buffering progress: " + percent + "%");
                });

                // Hide the search bar views
                searchButton.setVisibility(View.GONE);
                searchEditText.setVisibility(View.GONE);

                videoView.start();
            }
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
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            if (searchButton.getVisibility() != View.VISIBLE || searchEditText.getVisibility() != View.VISIBLE) {
                searchButton.setVisibility(View.VISIBLE);
                searchEditText.setVisibility(View.VISIBLE);
                return true;
            }
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (searchButton.getVisibility() == View.VISIBLE || searchEditText.getVisibility() == View.VISIBLE) {
                searchButton.setVisibility(View.GONE);
                searchEditText.setVisibility(View.GONE);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

}


