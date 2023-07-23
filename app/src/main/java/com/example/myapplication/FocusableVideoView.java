package com.example.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.VideoView;

public class FocusableVideoView extends VideoView {
    public FocusableVideoView(Context context) {
        super(context);
    }

    public FocusableVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FocusableVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private EditText searchEditText;

    public void setSearchEditText(EditText editText) {
        this.searchEditText = editText;
    }

    @Override
    public View focusSearch(int direction) {
        if (direction == View.FOCUS_UP) {
            // return the EditText to get focus when the UP key is pressed
            return searchEditText;
        }
        return super.focusSearch(direction);
    }
}