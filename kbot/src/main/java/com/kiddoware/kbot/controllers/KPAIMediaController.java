package com.kiddoware.kbot.controllers;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;

import com.kiddoware.kbot.R;

/**
 * Created by Shardul on 04/07/17.
 */

public class KPAIMediaController extends FrameLayout implements View.OnClickListener {

    private ImageButton pauseButton;
    private ImageButton nextButton;
    private ImageButton previousButton;

    private MediaController.MediaPlayerControl mediaPlayer;

    public KPAIMediaController(Context context) {
        super(context);
        init();
    }

    public KPAIMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KPAIMediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public KPAIMediaController(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.media_controller, this);
        pauseButton = (ImageButton) findViewById(R.id.media_controller_pause);
        previousButton = (ImageButton) findViewById(R.id.media_controller_prev);
        nextButton = (ImageButton) findViewById(R.id.media_controller_next);

        for (View view : new View[]{pauseButton, previousButton, nextButton}) {
            view.setOnClickListener(this);
        }

    }

    public void setMediaPlayer(MediaController.MediaPlayerControl mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        update();
    }

    private void update() {

        if (mediaPlayer.isPlaying()) {
            pauseButton.setImageResource(R.drawable.ic_pause_black_36dp);
        } else {
            pauseButton.setImageResource(R.drawable.ic_play_arrow_black_36dp);
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.media_controller_pause:

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }

                update();

                break;
            case R.id.media_controller_prev:
                break;
            case R.id.media_controller_next:
                break;
        }
    }
}
