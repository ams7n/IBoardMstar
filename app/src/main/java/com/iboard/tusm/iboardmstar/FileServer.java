package com.iboard.tusm.iboardmstar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by tusm on 17/11/25.
 */

public class FileServer {
    private static final String TAG = "file";
    private  ServerSocket mFileServer;
    private static final int port = 34679;
    private  Socket mFileSocket;
    private  OutputStream output;
    private  InputStream input;
    private  final  byte SEND_IMAGE = (byte)0x17;
    private  final  byte LOAD_IMAGE = (byte)0xF3;
    private   Handler handler ;
    private   Runnable runnable ;
    private   ImageView mVp;
    private  ProgressBar mLoad;
    private  Context mContext;
    private static volatile FileServer instance=null;


    public static FileServer getInstance(){
        synchronized(FileServer.class){
            if(instance==null){
                instance=new FileServer();
            }
        }
        return instance;
    }

    private FileServer(){

    }


    public void initFileServer(final ImageView vp, ProgressBar loading, Context context) {
        mVp = vp;
        mLoad = loading;
        mContext = context;
        new ServerThread().start();
        handler =new Handler();
        runnable = new Runnable() {

            @Override
            public void run() {
                // handler自带方法实现定时器
                try {
                    mLoad.post(new Runnable() {
                        @Override
                        public void run() {
                            mLoad.setVisibility(View.GONE);
                            mVp.setImageDrawable(mContext.getDrawable(R.drawable.ic_launcher_app));
                            mVp.setVisibility(View.VISIBLE);
                        }
                    });

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    System.out.println("exception...");
                }
            }
        };
        handler.postDelayed(runnable, 50000);

    }

    public  ServerSocket getmFileServer() {
        return mFileServer;
    }

    public void RESTART(){
        try {


            if(mFileSocket!=null){
                mFileSocket.close();
            }
         if(output!=null){
             output.close();
         }
            if(input!=null) {
                input.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            Log.i(TAG,"FileServer ------ restart");
            new ServerThread().start();
        }


    }
    public void  Close(){
        try {
            if(mFileSocket!=null) {
                if(!mFileSocket.isClosed()){
                    mFileSocket.close();
                }

            }
            if(output!=null){
                output.close();
            }
            if(input!=null){
                input.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            Log.i(TAG,"FileServer ------ close");

        }
    }
     class   ServerThread extends Thread{
        @Override
        public void run() {
            try {
                if(mFileServer==null) {
                    mFileServer = new ServerSocket(port);
                }
                mFileSocket = mFileServer.accept();
                output = mFileSocket.getOutputStream();
                input  = mFileSocket.getInputStream();
                Log.i(TAG,"FileServer ------ goin");
                receive(input);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public  void setmFileServer(ServerSocket mFileServer) {
           mFileServer = mFileServer;
    }

    private   void file(byte[] bb) throws IOException {
        switch (bb[1]){
            case SEND_IMAGE:
                byte[] imagLen =new byte[4];
                System.arraycopy(bb, 2, imagLen, 0, 4);
                int len = ImagicUtill.bytesToInt2(imagLen, 0);
                byte[] picData = new byte[len];
                System.arraycopy(bb, 6, picData, 0, len);
                final Bitmap bitmap = ImagicUtill.byteToBitmap(picData);
                 mVp.post(new Runnable() {
                     @Override
                     public void run() {
                         Log.i("Rotation","Rotation======="+mVp.getRotation());
                        // float rot = 360-mVp.getRotation();
                         setImageRotation(360);
                         mVp.setVisibility(View.VISIBLE);
                         mLoad.setVisibility(View.GONE);
                         mVp.setImageBitmap(bitmap);
                     }
                 });
                handler.removeCallbacks(runnable);
                sendMsg("RECEIVE_SEND_IMAGE",(byte)0x94,(byte)0xB7);
                break;
            case  LOAD_IMAGE:
                Log.i(TAG,"FileServer ------ goin LOAD_IMAGE");
                handler.postDelayed(runnable, 30000);

                mLoad.post(new Runnable() {
                   @Override
                   public void run() {
                       mLoad.setVisibility(View.VISIBLE);
                       mVp.setVisibility(View.GONE);

                   }
               });
                break;


        }

    }

    private  void receive(InputStream is) throws IOException {
        boolean flag = true;
        int len;
        int dataLen = 0;
        byte[] buf = new byte[0];
        byte[] bytes = new byte[1024 * 1024];
        byte buff[];

        while ((len = is.read(bytes)) != -1) {
            byte[] pbuf = new byte[len];// 临时读取数据
            System.arraycopy(bytes, 0, pbuf, 0, len);
            buf = ImagicUtill.arraycat(buf, pbuf);

            //对读取的数据进行分包，直到不够一个数据包的长度
            while (true) {
                if (flag) {//是否取数据长度
                    if (buf.length >= 6) {//可以取到数据长度
                        dataLen = ImagicUtill.getLen(buf);
//                Log.i(TAG, "--------------dataLen-----" + dataLen);
                    } else {//取不到，再读取一段数据
                        break;
                    }
                }
                if ((dataLen + 6) <= buf.length) {//可以取到数据包

                    flag = true;

                    //截取数据包
                    byte newBuf[] = new byte[dataLen + 6];
                    System.arraycopy(buf, 0, newBuf, 0, dataLen + 6);
                    //同时将多出的部分截取出来
                    buff = new byte[buf.length - newBuf.length];// 多出的数据
                    System.arraycopy(buf, newBuf.length, buff, 0, buff.length);
                    file(newBuf);
                    dataLen = 0;
                    buf = buff;
                } else {//不够一个数据包的长度，需再次读数据（数据包长度已经获取过，不用再次获取）
                    flag = false;
                    break;
                }
            }
        }
    }
    public  void sendMsg(String msg,byte bb0,byte bb1) throws IOException{
        byte[] strData = msg.getBytes();
        byte[] arrayOfByte1 = new byte[6];
        byte[] arrayOfByte2 = new byte[arrayOfByte1.length + strData.length];
        arrayOfByte1[0] = bb0;
        arrayOfByte1[1] = bb1;
        byte[] arrayOfByte3 = ImagicUtill.intToBytes2(strData.length);
        System.arraycopy(arrayOfByte3, 0, arrayOfByte1, 2, arrayOfByte3.length);
        System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, arrayOfByte1.length);
        System.arraycopy(strData, 0, arrayOfByte2, arrayOfByte1.length, strData.length);
        output.write(arrayOfByte2);
        output.flush();
    }

    public void setImageRotation(float rotation){
        mVp.setPivotX(mVp.getWidth()/2);
        mVp.setPivotY(mVp.getHeight()/2);//支点在图片中心
        mVp.setRotation(rotation);

    }
    public float getImageRotation( ){

        return mVp.getRotation();
    }
}
