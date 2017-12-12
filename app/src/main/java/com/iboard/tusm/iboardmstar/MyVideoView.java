package com.iboard.tusm.iboardmstar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

/**
 * Created by tusm on 17/12/9.
 */

public class MyVideoView extends VideoView {
    private PlayPauseListener mListener;
    public MyVideoView(Context context) {
        super(context);
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public void setPlayPauseListener(PlayPauseListener listener) {
        mListener = listener;
    }
    @Override
    public void pause() {
        super.pause();
        if (mListener != null) {
            mListener.onPause();
        }
    }

    @Override
    public void start() {
        super.start();
        if (mListener != null) {
            mListener.onPlay();
        }
    }

    interface PlayPauseListener {
        void onPlay();
        void onPause();
        void onSeekTo(int msec);
    }



    @Override
    public void seekTo(int msec) {
        super.seekTo(msec);
        if (mListener != null) {
            mListener.onSeekTo(msec);
        }
    }
}
