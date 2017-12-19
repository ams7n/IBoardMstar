package com.iboard.tusm.iboardmstar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by tusm on 17/11/25.
 */

public class PictureActivity extends Activity{
    private ProgressBar bar;
  //  private ViewPager vp;
    private ImageView vp;
    private FileServer fileServer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pictury_layout);
        vp = (ImageView) findViewById(R.id.vp);
        bar = (ProgressBar) findViewById(R.id.progressBar);
          fileServer = FileServer.getInstance();
          fileServer.initFileServer(vp,bar,this);

        // fileServer.RESTART();

        //startHeart();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        fileServer.RESTART();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("PictureActivity","=========onDestroy");
        if(fileServer.getmFileServer()!=null) {
            fileServer.Close();
            fileServer.setmFileServer(null);

        }
    }
//    private void startHeart(){
//        timer = new Timer();
//        task = new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    fileServer.SendHeart();
//                } catch (IOException e) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(PictureActivity.this,"连接超时，已退出浏览",Toast.LENGTH_LONG).show();
//                        }
//                    });
//                    timer.cancel();
//                    task.cancel();
//                    finish();
//                    e.printStackTrace();
//                }
//            }
//        };
//        this.timer.schedule( task, 5000, 15000);
//
//    }

}
