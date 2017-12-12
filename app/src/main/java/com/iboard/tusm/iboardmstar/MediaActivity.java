package com.iboard.tusm.iboardmstar;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Toast;


import java.io.IOException;

/**
 * Created by tusm on 17/12/6.
 */

public class MediaActivity extends Activity   {
    public static MyVideoView videoView;
    private MediaController mediaController;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.media_layout);
        videoView = (MyVideoView) findViewById(R.id.video);
        Intent intent = getIntent();
        String url = intent.getStringExtra("media");
        mediaController = new MediaController(this);
        Uri uri = Uri.parse(url);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(uri);
        videoView.setZOrderOnTop(true);
        videoView.start();
        videoView.setOnCompletionListener(new MyPlayerOnCompletionListener());
        videoView.setPlayPauseListener(new MyVideoView.PlayPauseListener() {
            @Override
            public void onPlay() {
                try {
                    sendMsg("START_VIDEO",(byte)0x94,(byte)0xB9);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPause() {
                try {
                    sendMsg("PAUSE_VIDEO",(byte)0x94,(byte)0xC0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSeekTo(int msec) {
                try {
                    sendMsg(msec+"",(byte)0x94,(byte)0xC1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                try {
                    sendMsg("PLAYER_FINISH",(byte)0x94,(byte)0x22);
                    videoView.resume();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

    }





    class MyPlayerOnCompletionListener
            implements MediaPlayer.OnCompletionListener{

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            try {


                     Log.i("media","=============MyPlayerOnCompletionListener");
                     sendMsg("PLAYER_FINISH",(byte)0x94,(byte)0x22);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();


            try {

                    Log.i("media","=============onStop   PLAYER_FINISH" );
                    sendMsg("PLAYER_FINISH",(byte)0x94,(byte)0x22);



            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        intent = getIntent();
        String url = intent.getStringExtra("media");
        Log.i("media","=============url" +url);
        mediaController = new MediaController(this);
        Uri uri = Uri.parse(url);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(uri);
        videoView.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("media","=============onDestroy" );
    }

    private  void sendMsg(String msg, byte bb0, byte bb1) throws IOException{
        byte[] strData = msg.getBytes();
        byte[] arrayOfByte1 = new byte[6];
        byte[] arrayOfByte2 = new byte[arrayOfByte1.length + strData.length];
        arrayOfByte1[0] = bb0;
        arrayOfByte1[1] = bb1;
        byte[] arrayOfByte3 = ImagicUtill.intToBytes2(strData.length);
        System.arraycopy(arrayOfByte3, 0, arrayOfByte1, 2, arrayOfByte3.length);
        System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, arrayOfByte1.length);
        System.arraycopy(strData, 0, arrayOfByte2, arrayOfByte1.length, strData.length);
        if(ImgicServer.outputStream!=null) {
            ImgicServer.outputStream.write(arrayOfByte2);
            ImgicServer.outputStream.flush();
        }
    }


}
