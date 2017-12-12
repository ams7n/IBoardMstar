package com.iboard.tusm.iboardmstar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;

import org.apache.ftpserver.ftplet.FtpException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by tusm on 17/11/21.
 */

public class ImgicServer implements ScreenshotObserver.OnScreenshotTakenListener {
    private static final int Port = 23568;
    private final byte CHECK_SOURCE = (byte)0x10;
    private final byte CONNECTE = (byte)0x02;
    private final byte DISCONNECTE = (byte)0x03;
    private final byte HEARTBEAT = (byte)0xFF;
    private final byte OPS_CHECK_STATUS = (byte)0x12;
    private final byte OPS_SHUTDOWN = (byte)0x14;
    private final byte OPS_START = (byte)0x13;
    private final byte OPS_TOUCHPAD = (byte)0x15;
    private final byte REMOTE_APP = (byte)0x07;
    private final byte REMOTE_CLEAN = (byte)0x09;
    private final byte REMOTE_KEYBOARD = (byte)0x04;
    private final byte REMOTE_SNAPSHOT = (byte)0x08;
    private final byte REMOTE_SOURCE = (byte)0x05;
    private final byte REQUEST_APP_LIST = (byte)0x06;
    private final byte REQUEST_SEND_IMAGE = (byte)0x16;
    private static final byte SEND_VIDEO =  (byte)0x18 ;
    private  final  byte START_VIDEO = (byte)0x19;
    private  final  byte STOP_VIDEO = (byte)0x20;
    private  final  byte SEEKTO_VIDEO = (byte)0x21;
    private  final  byte TRANSFER_CONTROL_IMAGE = (byte)0x23;
    private boolean SocketRun = false;
    private boolean StartSnapshot = false;
    private int TIME = 5000;
    private ApplicationManager am;
    private IconServer iconServer;
    private long cachesize;
    private long codesize;
    private Context context;
    private boolean isLast = false ;
    private long datasize;
    private ImagicUtill imagicUitll;
    private InputStream inputStream;
    private JSONObject jsonObject;
    private List<AppsItemInfo> list;
    public static DataOutputStream outputStream;
    private JSONObject reciveJson;
    private ScreenshotObserver screenShotObserver;
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private TimerTask task;
    private Timer timer;
    private long totalsize;
    private String user;
    private FileServer fileServer;
    private int RECONNECT = 0;

    public ImgicServer(Context paramContext)
    {
        this.context = paramContext;
        imagicUitll = new ImagicUtill(paramContext);
         screenShotObserver = new ScreenshotObserver(this);
         this.screenShotObserver.start();
         StartDeleteObserver();

    }

    private void  ControlSwitch(byte[] bb)
            throws Exception{
//        timer.cancel();
//        task.cancel();
        switch (bb[1]){
            case CONNECTE:
                byte[] mConnectUser = new byte[4];
                System.arraycopy(bb, 2, mConnectUser, 0, 4);
                int Userleng = ImagicUtill.bytesToInt2(mConnectUser, 0);
                byte[] UserDataKey = new byte[Userleng];
                System.arraycopy(bb, 6, UserDataKey, 0, Userleng);
                user = new String(UserDataKey);
                mHandler.sendEmptyMessage(0);
                Log.i("reconnect", "----- 请求连接");
                SendMsg("RECEIVE_CONNECTE",(byte)0x91,(byte)0xA2);
              //  startHeart(TIME);
                handler.postDelayed(runnable,TIME);
                break;

            case DISCONNECTE:
                Log.i("reconnect", "----- 请求断开");

                SendMsg("RECEIVE_DISCONNECTE",(byte)0x91,(byte)0xA3);
//                timer.cancel();
//                task.cancel();
                handler.removeCallbacks(runnable);
                outputStream.close();
                inputStream.close();
                socket.close();
                serverSocket.close();
                mHandler.sendEmptyMessage(1);
                Log.i("socket", "=======2=====close");
                break;
            case REMOTE_KEYBOARD:
                Log.i("keyboard", "----- 收到遥控器消息");
                byte[] DataKeyLen = new byte[4];
                System.arraycopy(bb, 2, DataKeyLen, 0, 4);
                int leng = ImagicUtill.bytesToInt2(DataKeyLen, 0);
                byte[] DataKey = new byte[leng];
                System.arraycopy(bb, 6, DataKey, 0, leng);
                String DataJson = new String(DataKey);
                Log.i("keyboard", "----- json===" + DataJson);
                this.jsonObject = new JSONObject(DataJson);
                 String func = this.jsonObject.getString("FUNC");
                if(func.equals("KEY_VOL")){
                    int vol = Integer.parseInt(jsonObject.getString("VOL"));
                    if (vol != 999){
                        imagicUitll.SetVol(vol);
                        reciveJson = new JSONObject();
                        reciveJson.put("CMD", "RECIVE_REMOTE_KEYBOARD");
                        reciveJson.put("FUNC", "KEY_VOL");
                        reciveJson.put("VOL", this.imagicUitll.getVoi() + "");
                        SendMsg(reciveJson.toString(),(byte)0x92,(byte)0xA4);
                        Log.i("keyboard", "----- 回复json===" + this.reciveJson.toString());
                        Log.i("keyboard", "----- 回复遥控器消息");
                    }
                }else{
                    int keydown = Integer.parseInt(func);
                    this.imagicUitll.SendKeyDown(keydown);
                }
                break;
            case REMOTE_SOURCE:
                byte[] source = new byte[4];
                System.arraycopy(bb, 2, source, 0, 4);
                int SoureLen = ImagicUtill.bytesToInt2(source, 0);
                byte[] sourcedata = new byte[SoureLen];
                System.arraycopy(bb, 6, sourcedata, 0, SoureLen);
                int SourceCode = Integer.parseInt(new String(sourcedata));
                this.imagicUitll.StartSource(SourceCode);
                break;
            case CHECK_SOURCE:
                 reciveJson = new JSONObject();
                 reciveJson.put("CUR_SOURCE", imagicUitll.getInputSource());
                 String NATION = "";
                if (imagicUitll.isAvilible("Source_CN")){
                    NATION = "FOREIGN";
                }else{
                    NATION = "CHINA";
                }
                 reciveJson.put("NATION", NATION);
                Log.i("SOURCE",  reciveJson.toString());
                SendMsg(reciveJson.toString(),(byte)0x92,(byte)0xB1);
                break;

            case REQUEST_APP_LIST:
                 list =  imagicUitll.queryAppInfo();
               for(int i =0;i<list.size();i++){
                   queryPacakgeSize(list.get(i).getPackageName());
               }
                break;
            case REMOTE_APP:
                byte[] app = new byte[4];
                System.arraycopy(bb, 2, app, 0, 4);
                int len = ImagicUtill.bytesToInt2(app, 0);
                byte[] app_data = new byte[len];
                System.arraycopy(bb, 6, app_data, 0, len);
                JSONObject jsonObject = new JSONObject(new String(app_data));
                String Func = jsonObject.getString("FUNC");
                String packname = jsonObject.getString("BUNDLE_ID");
                if(Func.equalsIgnoreCase("OPEN_APP")) {
                    Intent intent = context.getPackageManager().getLaunchIntentForPackage(packname);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }else{
                    am.uninstallPackage(packname);
                }
                break;
            case REMOTE_CLEAN:
                byte[] remote_app = new byte[4];
                System.arraycopy(bb, 2, remote_app, 0, 4);
                int remote_app_len = ImagicUtill.bytesToInt2(remote_app, 0);
                byte[] remote_app_json = new byte[remote_app_len];
                System.arraycopy(bb, 6, remote_app_json, 0, remote_app_len);
                jsonObject = new JSONObject(new String(remote_app_json));
                String func2 = jsonObject.getString("FUNC");
                JSONObject jsonObject1 =new JSONObject();
                jsonObject1.put("CMD", "REMOTE_TV");
                if (func2.equals("REMOTE_CLEAN")){
                    String men = imagicUitll.clearMem();
                    jsonObject1.put("FUNC", "REMOTE_CLEAN");
                    jsonObject1.put("STATE", men);
                }else if(func2.equals("REMOTE_SHUTDOWN")){
                    jsonObject1.put("FUNC", "REMOTE_SHUTDOWN");
                    jsonObject1.put("STATE", "REMOTE_SUCCEED");
                }else if(func2.equals("REMOTE_SETTINGS")){
                    ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.Settings");
                    Intent setting = new Intent("android.intent.action.MAIN");
                    setting.addCategory("android.intent.category.LAUNCHER");
                    setting.setComponent(componentName);
                    setting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.context.startActivity(setting);
                    jsonObject1.put("FUNC", "REMOTE_SETTINGS");
                    jsonObject1.put("STATE", "REMOTE_SUCCEED");
                }else if(func2.equals("REMOTE_ENOTE")){
                    ComponentName componentName = new ComponentName("com.iboard.whiteboard", "com.iboard.whiteboard.MainActivity");
                    Intent setting = new Intent("android.intent.action.MAIN");
                    setting.addCategory("android.intent.category.LAUNCHER");
                    setting.setComponent(componentName);
                    setting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.context.startActivity(setting);
                    jsonObject1.put("FUNC", "REMOTE_ENOTE");
                    jsonObject1.put("STATE", "REMOTE_SUCCEED");

                }
                SendMsg(jsonObject1.toString(),(byte)0x92,(byte)0xA9);

                if(func2.equals("REMOTE_SHUTDOWN")){
                    imagicUitll.SendKeyDown(26);
                }
                break;
            case REMOTE_SNAPSHOT:
                StartSnapshot =true;
                imagicUitll.SendKeyDown(279);
                break;
            case OPS_CHECK_STATUS:
                SendMsg(imagicUitll.getState(),(byte)0x93,(byte)0xB2);

                break;
            case OPS_START:
                try
                {
                    TvManager.getInstance().setTvosInterfaceCommand("574650510000000000");
                    Log.i("touch", "OPS_open");
                }
                catch (TvCommonException e)
                {

                        e.printStackTrace();
                }
                break;
            case OPS_SHUTDOWN:
                try
                {
                    TvManager.getInstance().setTvosInterfaceCommand("574650520000000000");
                    Log.i("touch", "OPS_SHUTDOWN");
                }
                catch (TvCommonException e)
                {

                    e.printStackTrace();
                }
                break;
            case OPS_TOUCHPAD:
                for(int i=0;i<bb.length;i++){
                    if ((bb[i] == OPS_TOUCHPAD) && (bb.length - i > 20)){
                        byte[] touch = new byte[4];
                        System.arraycopy(bb, i + 1, touch, 0, 4);
                        int j = ImagicUtill.bytesToInt2(touch, 0);
                        byte[] touchdata = new byte[j];
                        System.arraycopy(bb, i + 5, touchdata, 0, j);
                        String strtouch = new String(touchdata);
                        Log.i("touch", strtouch);
                        try
                        {
                            TvManager.getInstance().setTvosInterfaceCommand(strtouch);

                        }
                        catch (TvCommonException e)
                        {

                            e.printStackTrace();
                        }
                    }
                }
                break;
            case HEARTBEAT:
                SocketRun = true;
                RECONNECT = 0;
                Log.i("heart","===== 收到心跳");
                break;
            case REQUEST_SEND_IMAGE:
                    SendMsg("REQUEST_SUCCEED",(byte)0x94,(byte)0xB6);
                    Intent intent = new Intent(context, PictureActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                break;
            case SEND_VIDEO:
                JSONObject ftpJson =new JSONObject();
                String USERNAME = ImagicUtill.getRandom();
                String PASSWORD = ImagicUtill.getRandom();
                ftpJson.put("CMD","REMOTE_FILE");
                ftpJson.put("USERNAME","iboard123");
                ftpJson.put("PASSWORD","iboard123");
                String ip  = ImagicUtill.GetIpAddress();;
                if(ImagicUtill.isIP(ip)) {
                    ftpJson.put("IP", ip);
                }else{
                    ftpJson.put("IP", ImagicUtill.getWifiIP(context));
                }
                SendMsg(ftpJson.toString(),(byte)0x94,(byte)0xB8);
                Log.i("media","ftpJson======"+ftpJson.toString());

                FtpServerlet ftpServerlet =FtpServerlet.getInstance();
                ftpServerlet.SetUser("iboard123","iboard123",context);
                try {
                    ftpServerlet.start();
                } catch (FtpException e) {
                    e.printStackTrace();
                }
                break;
            case START_VIDEO:
                if(MediaActivity.videoView!=null){
                    MediaActivity.videoView.start();
                   SendMsg("START_SUCCEED",(byte)0x94,(byte)0xB9);
                }else{

                    SendMsg("START_FAILED",(byte)0x94,(byte)0xB9);
                }
                break;
            case STOP_VIDEO:
                Log.i("media","canPause======"+MediaActivity.videoView.canPause());

                if(MediaActivity.videoView.canPause()){
                    MediaActivity.videoView.pause();
                    SendMsg("PAUSE_SUCCEED",(byte)0x94,(byte)0xC0);
                }else{
                    SendMsg("PAUSE_FAILED",(byte)0x94,(byte)0xC0);
                }
                break;
            case SEEKTO_VIDEO:
                byte[] VideoLen =new byte[4];
                System.arraycopy(bb, 2, VideoLen, 0, 4);
                int mlen = ImagicUtill.bytesToInt2(VideoLen, 0);
                byte[] VideoData = new byte[mlen];
                System.arraycopy(bb, 6, VideoData, 0, mlen);
                String sMses = new String(VideoData);
                int iMses = Integer.parseInt(sMses);
                Log.i("media","sMses======"+sMses);
                Log.i("media","getDuration======"+MediaActivity.videoView.getDuration());

                if(iMses>MediaActivity.videoView.getDuration()){
                    MediaActivity.videoView.seekTo(MediaActivity.videoView.getDuration());
                }else{
                    MediaActivity.videoView.seekTo(iMses);
                }
                SendMsg(MediaActivity.videoView.getCurrentPosition()+"",(byte)0x94,(byte)0xC1);
                Log.i("media","getCurrentPosition======"+MediaActivity.videoView.getCurrentPosition());
                break;
            case TRANSFER_CONTROL_IMAGE:
                byte[] IMAGELen =new byte[4];
                System.arraycopy(bb, 2, IMAGELen, 0, 4);
                int ilen = ImagicUtill.bytesToInt2(IMAGELen, 0);
                byte[] IMAGEData = new byte[ilen];
                System.arraycopy(bb, 6, IMAGEData, 0, ilen);
                String TRANSFER = new String(IMAGEData);
                fileServer =FileServer.getInstance();
                if(TRANSFER.equalsIgnoreCase("IMAGE_TURNRIGHT")){
                    fileServer.setImageRotation(fileServer.getImageRotation()+90);
                    SendMsg("IMAGE_TURNRIGHT_SUCCEED",(byte)0x94,(byte)0xC3);
                }else{
                    fileServer.setImageRotation(fileServer.getImageRotation()-90);
                    SendMsg("IMAGE_TURNLEFT_SUCCEED",(byte)0x94,(byte)0xC3);
                }

                break;

        }

    }

    @Override
    public void onScreenshotTaken(Uri uri) {
        if(StartSnapshot) {
//            timer.cancel();
//            task.cancel();
            try {
                Bitmap snapshot = imagicUitll.getBitmapFormUri(uri);
                if (snapshot != null) {
                    byte[] arrayOfByte1 = imagicUitll.WeChatBitmapToByteArray(snapshot,true);
                    Log.i("size", "===data_snapshot==== " + arrayOfByte1.length);
                    byte[] arrayOfByte2 = new byte[6];
                    byte[] arrayOfByte3 = ImagicUtill.intToBytes2(arrayOfByte1.length);
                    byte[] paramUri = new byte[arrayOfByte2.length + arrayOfByte1.length];
                    arrayOfByte2[0] = (byte) 0x92;
                    arrayOfByte2[1] = (byte) 0xA8;
                    System.arraycopy(arrayOfByte3, 0, arrayOfByte2, 2, arrayOfByte3.length);
                    System.arraycopy(arrayOfByte2, 0, paramUri, 0, arrayOfByte2.length);
                    System.arraycopy(arrayOfByte1, 0, paramUri, arrayOfByte2.length, arrayOfByte1.length);
                    outputStream.write(paramUri);
                    outputStream.flush();
                    StartSnapshot = false;
//                    SocketRun = true;
//                    startHeart(30000);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void  queryPacakgeSize(String pkgName) throws Exception{
        Log.i("pack",pkgName);
        if ( pkgName != null){
            //使用放射机制得到PackageManager类的隐藏函数getPackageSizeInfo
            PackageManager pm = context.getPackageManager();  //得到pm对象
            try {
                String methodName = "getPackageSizeInfo";// 想通过反射机制调用的方法名
                Class<?> parameterType1 = String.class;// 被反射的方法的第一个参数的类型
                Class<?> parameterType2 = IPackageStatsObserver.class;// 被反射的方法的第二个参数的类型
                Method getPackageSizeInfo = pm.getClass().getMethod(
                        methodName, parameterType1, parameterType2);
                getPackageSizeInfo.invoke(pm,// 方法所在的类
                        pkgName, new PkgSizeObserver());// 方法使用的参数
             }
            catch(Exception ex){
                ex.printStackTrace() ;
                throw ex ;  // 抛出异常
            }
        }
    }

    private void appDate(List<AppsItemInfo> paramList)
    {
        iconServer = new IconServer(paramList);
        for(int i =0;i<paramList.size();i++){
            try {
                Log.i("appsize", "=======jsonObject=======" + paramList.get(i).getPackageName()+"====="+paramList.get(i).getAppsize());
                JSONObject jsonObject = new JSONObject();
            jsonObject.put("CMD", "RECEIVE_APP_LIST");
            if (paramList.size() - 1 == i){
                jsonObject.put("APP_LAST", true);
            }else{
                jsonObject.put("APP_LAST", false);
            }
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("APP_NAME",paramList.get(i).getLabel());
            jsonObject1.put("APP_FLAGS",paramList.get(i).getAppflags());
            jsonObject1.put("CLASS_NAME",paramList.get(i).getActivityName());
            jsonObject1.put("BUNDLE_ID",paramList.get(i).getPackageName());
            jsonObject1.put("APP_STORAGE",paramList.get(i).getAppsize());
         //   jsonObject1.put("APP_ICON",imagicUitll.bytetoString(imagicUitll.drawableToBitmap(paramList.get(i).getIcon())));
            jsonObject.put("APP_LIST",jsonObject1);
            byte[] AppJson = jsonObject.toString().getBytes();
            byte[] AppHead = new byte[6];
            byte[] AppSend = new byte[AppHead.length + AppJson.length];
            AppHead[0] = (byte)0x92;
            AppHead[1] = (byte)0xA6;
            byte[] len = ImagicUtill.intToBytes2(AppJson.length);
            System.arraycopy(len, 0, AppHead, 2, len.length);
            System.arraycopy(AppHead, 0, AppSend, 0, AppHead.length);
            System.arraycopy(AppJson, 0, AppSend, AppHead.length, AppJson.length);
            Log.i("appsize", "=======allsize=======" + AppSend.length);
                outputStream.write(AppSend);
                outputStream.flush();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("IOException",e.getMessage());
            } catch (JSONException e) {
                Log.i("JSONException",e.getMessage());
                e.printStackTrace();
            }

        }

    }

    private void StartDeleteObserver()
    {
        try
        {
            am = new ApplicationManager(this.context);
              am.setOnPackagedObserver(new OnPackagedObserver()
            {
                public void packageDeleted(String paramAnonymousString, int paramAnonymousInt)
                {
                    if (paramAnonymousInt == 1)
                    {
                         SendMsg("UNINSTALL_SUCCEED",(byte)0x92,(byte)0xA9);
                        Log.i("ApplicationManager", "=======删除成功=====" + paramAnonymousString);
                        return;
                    }
                    SendMsg("UNINSTALL_FAILED",(byte)0x92,(byte)0xA9);
                    Log.i("ApplicationManager", "=======删除失败=====" + paramAnonymousString);
                }
            });
            return;
        }
        catch (NoSuchMethodException localNoSuchMethodException)
        {
            localNoSuchMethodException.printStackTrace();
        }
    }

    private void SendMsg (String paramString,byte bb1 , byte bb2)
    {
        byte[] strData = paramString.getBytes();
        byte[] arrayOfByte1 = new byte[6];
        byte[] arrayOfByte2 = new byte[arrayOfByte1.length + strData.length];
        arrayOfByte1[0] = bb1;
        arrayOfByte1[1] = bb2;
        byte[] arrayOfByte3 = ImagicUtill.intToBytes2(strData.length);
        System.arraycopy(arrayOfByte3, 0, arrayOfByte1, 2, arrayOfByte3.length);
        System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, arrayOfByte1.length);
        System.arraycopy(strData, 0, arrayOfByte2, arrayOfByte1.length, strData.length);
        try {
            outputStream.write(arrayOfByte2);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("APP", "SendMsg");
    }

//    private void startHeart(int paramInt){
//        timer = new Timer();
//        task = new TimerTask()
//        {
//            public void run()
//            {
//                Log.i("socket", "=====SocketRun====" + SocketRun);
//                if (!SocketRun)
//                {
//                       Log.i("socket", "=====isClosed====" + socket.isClosed());
//                        if(socket.isClosed()){
//                            timer.cancel();
//                            task.cancel();
//
//                        }else {
//                            byte[] strData = "SEND_HEARTBEAT".getBytes();
//                            byte[] arrayOfByte1 = new byte[6];
//                            byte[] arrayOfByte2 = new byte[arrayOfByte1.length + strData.length];
//                            arrayOfByte1[0] = (byte) 0x91;
//                            arrayOfByte1[1] = (byte) 0xFF;
//                            byte[] arrayOfByte3 = ImagicUtill.intToBytes2(strData.length);
//                            System.arraycopy(arrayOfByte3, 0, arrayOfByte1, 2, arrayOfByte3.length);
//                            System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, arrayOfByte1.length);
//                            System.arraycopy(strData, 0, arrayOfByte2, arrayOfByte1.length, strData.length);
//                            try {
//                                outputStream.write(arrayOfByte2);
//                                outputStream.flush();
//                                Log.i("socket", "=====write====");
//                                RECONNECT++;
//                                Log.i("APP", "RECONNECT=====" + RECONNECT);
//                                if (RECONNECT == 10) {
//                                    ReStart();
//                                    RECONNECT = 0;
//                                    return;
//                                }
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                                Log.i("socket", "IOException");
//                                timer.cancel();
//                                task.cancel();
//                                ReStart();
//                                RECONNECT = 0;
//                                return;
//
//                            }
//
//                        }
//                }else {
//                    SocketRun =false;
//                }
//
//            }
//        };
//        this.timer.schedule( task, paramInt, TIME);
//    }
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // handler自带方法实现定时器
            try {
                handler.postDelayed(this, TIME);
                if (!SocketRun)
                {
                    Log.i("socket", "=====isClosed====" + socket.isClosed());
                    if(socket.isClosed()){
//                        timer.cancel();
//                        task.cancel();
                        Log.i("socket", "=====removeCallbacks===="  );

                        handler.removeCallbacks(this);
                    }else {
                        byte[] strData = "SEND_HEARTBEAT".getBytes();
                        byte[] arrayOfByte1 = new byte[6];
                        byte[] arrayOfByte2 = new byte[arrayOfByte1.length + strData.length];
                        arrayOfByte1[0] = (byte) 0x91;
                        arrayOfByte1[1] = (byte) 0xFF;
                        byte[] arrayOfByte3 = ImagicUtill.intToBytes2(strData.length);
                        System.arraycopy(arrayOfByte3, 0, arrayOfByte1, 2, arrayOfByte3.length);
                        System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, arrayOfByte1.length);
                        System.arraycopy(strData, 0, arrayOfByte2, arrayOfByte1.length, strData.length);
                        try {
                            outputStream.write(arrayOfByte2);
                            outputStream.flush();
                            Log.i("socket", "=====write====");
                            RECONNECT++;
                            Log.i("APP", "RECONNECT=====" + RECONNECT);
                            if (RECONNECT == 10) {
                                ReStart();
                                RECONNECT = 0;
                                handler.removeCallbacks(runnable);
                                return;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i("socket", "IOException");
//                            timer.cancel();
//                            task.cancel();
                            handler.removeCallbacks(this);
                            ReStart();
                            RECONNECT = 0;
                            return;

                        }

                    }
                }else {
                    SocketRun =false;
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("exception...");
            }
        }
    };

    public void StartThread()
    {
        new Server().start();
    }

    class Server extends Thread{
        @Override
        public void run() {
            try {
                 serverSocket = new ServerSocket(Port);
                 socket = serverSocket.accept();
                 outputStream = new DataOutputStream(socket.getOutputStream());
                 inputStream = socket.getInputStream();
                 UDPServer.udpLife = false;
              //   SocketRun = true;
              //   startHeart(5000);
                 while (true) {
                    int count = 0;

                    while (count == 0) {
                        count = inputStream.available();
                    }

                    byte[] bb = new byte[count];
                    int len;


                    if ((len = inputStream.read(bb)) != -1) {
                        Log.i("string", "-----  "+new String(bb));
                        RECONNECT = 0;
                        ControlSwitch(bb);
                    }

                }
                } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if (!UDPServer.udpLife){
                    UDPServer udpServer =new UDPServer(context);
                    UDPServer.udpLife = true;
                    new Thread(udpServer).start();
//                    timer.cancel();
//                    task.cancel();
                    handler.removeCallbacks(runnable);
                    if(iconServer!=null){
                        if(iconServer.isRun){
                            iconServer.CloseServer();
                        }
                    }
                    try {
                        outputStream.close();
                        inputStream.close();
                        socket.close();
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        StartThread();
                        Log.i("socket", "=======2=====restart");
                    }


                }
            }
        }
    }

    public class PkgSizeObserver extends IPackageStatsObserver.Stub{

        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                throws RemoteException {
            // TODO Auto-generated method stub
            cachesize = pStats.cacheSize  ; //缓存大小
            datasize = pStats.codeSize  ;  //数据大小
            codesize =    pStats.codeSize  ;  //应用程序大小
            totalsize = cachesize + datasize + codesize ;
            isLast = false;
            for(int i =0;i<list.size();i++){
               if( list.get(i).getPackageName().equalsIgnoreCase(pStats.packageName)){
                   list.get(i).setAppsize(ImagicUtill.getPrintSize(totalsize));
               }
            }
            for(int i =0;i<list.size();i++){
                if(list.get(i).getAppsize().trim().length()==0){
                   isLast =true;
                    break;
                }
            }
          if(!isLast){
              try {
                  appDate(list);
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
        }
    }
    private void ReStart() {
            if (!UDPServer.udpLife) {
            SocketRun = true;
            UDPServer localUdpServer = new UDPServer(context);
            UDPServer.udpLife = true;
            new Thread(localUdpServer).start();
            try {
                if (!socket.isClosed()) {
                    outputStream.close();
                    inputStream.close();
                    socket.close();
                    serverSocket.close();
                    mHandler.sendEmptyMessage(1);
                    if(iconServer!=null){
                        if(iconServer.isRun){
                            iconServer.CloseServer();
                        }
                    }
                    Log.i("socket", "=======3=====close");
                }
                return;
            } catch (IOException localIOException) {
                localIOException.printStackTrace();
                return;
            } finally {
                //重启服务
                StartThread();
                Log.i("socket", "restart");
            }
        }
    }
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Log.i("connect",user);
                    Toast.makeText(context,user+"已连接智能一体机",Toast.LENGTH_LONG).show();
                    break;
                case 1:
                    Log.i("connect",user);
                    Toast.makeText(context,user+"已断开智能一体机",Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };
}
