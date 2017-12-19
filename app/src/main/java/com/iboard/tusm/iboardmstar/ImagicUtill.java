package com.iboard.tusm.iboardmstar;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;

import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tusm on 17/11/21.
 */

public class ImagicUtill {
    private static final int DEFAULT_HEIGHT = 720;
    private static final int DEFAULT_WIDTH = 1080;
    private static String LOG_TAG = "cleaner";
    private static AudioManager mAudioManager;
    private String RECEIVE_SOURCE;
    public String RECEIVE_STATUS = "POWER_ON";
    private Bitmap bitmap;
    private Context context;
    private static int mTouchSelA = 88;
    private static int mTouchSelB = 89;



    public ImagicUtill(Context paramContext)
    {
        this.context = paramContext;
        mAudioManager = (AudioManager)paramContext.getSystemService(Service.AUDIO_SERVICE);
    }

    public static String GetIpAddress()  {
        String ipaddress = "";
        Enumeration<NetworkInterface> netInterfaces = null;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface intf = netInterfaces.nextElement();
                if (intf.getName().toLowerCase().equals("eth0")
                        || intf.getName().toLowerCase().equals("wlan0")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            ipaddress = inetAddress.getHostAddress().toString();
                            if (!ipaddress.contains("::")) {// ipV6的地址
                                ipaddress = ipaddress;
                            }
                        }
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ipaddress;
    }
    public static byte[] intToBytes2(int paramInt)
    {
        return new byte[] { (byte)(paramInt >> 24 & 0xFF), (byte)(paramInt >> 16 & 0xFF), (byte)(paramInt >> 8 & 0xFF), (byte)(paramInt & 0xFF) };
    }
    public static int bytesToInt2(byte[] paramArrayOfByte, int paramInt)
    {
        return (paramArrayOfByte[paramInt] & 0xFF) << 24 | (paramArrayOfByte[(paramInt + 1)] & 0xFF) << 16 | (paramArrayOfByte[(paramInt + 2)] & 0xFF) << 8 | paramArrayOfByte[(paramInt + 3)] & 0xFF;
    }
    private   String longToString(long currentTime, String formatType)
            throws java.text.ParseException {
        Date date = longToDate(currentTime, formatType); // long类型转成Date类型
        String strTime = dateToString(date, formatType); // date类型转成String
        return strTime;
    }
    public static Date longToDate(long currentTime, String formatType)
            throws java.text.ParseException {
        Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
        Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
        return date;
    }

    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType).format(data);
    }

    public static Date stringToDate(String strTime, String formatType)
            throws  java.text.ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }
    public String getUptimeMillis() throws java.text.ParseException {

        return longToString( SystemClock.uptimeMillis(),"hh:mm:ss");
    }

    public String getAvailMemory()
    {
        ActivityManager localActivityManager = (ActivityManager)this.context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo localMemoryInfo = new ActivityManager.MemoryInfo();
        localActivityManager.getMemoryInfo(localMemoryInfo);
        Log.d(LOG_TAG, "可用内存---->>>" + localMemoryInfo.availMem / (1024 * 1024));
        Log.d(LOG_TAG, "总内存---->>>" + localMemoryInfo.totalMem / (1024 * 1024));
        return localMemoryInfo.availMem / (1024 * 1024) + "/" + localMemoryInfo.totalMem / (1024 * 1024);
    }

    public void SetVol(int paramInt)
    {
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, paramInt, AudioManager.FLAG_SHOW_UI);
    }

    public int getVoi()
    {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
    }

    public void SendKeyDown(final int paramInt)
    {
        Log.i("SOURCE", paramInt + "====keyCode");
        new Thread(new Runnable()
        {
            public void run()
            {
                Instrumentation instrumentation = new Instrumentation();
                instrumentation.sendKeyDownUpSync(paramInt);
            }
        }).start();
    }

    public void StartSource(int paramInt)
    {
        Intent intent;
        ComponentName componentName;
        switch (paramInt)
        {
            case 3:
                componentName = new ComponentName("com.iboard.tusm.newluanch", "com.example.tusm.newluanch.activity.MainActivity");
                intent = new Intent();
                intent.setComponent(componentName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 context.startActivity(intent);
                break;
            case 327:
                intent = new Intent("com.mstar.android.intent.action.OPS_BUTTON");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                break;
            case 328:
                intent = new Intent("com.mstar.android.intent.action.HDMI1_BUTTON");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                break;
            case 329:
                intent = new Intent("com.mstar.android.intent.action.HDMI2_BUTTON");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                break;
            case 330:
                intent = new Intent("com.mstar.android.intent.action.HDMI3_BUTTON");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                break;
            case 331:
                intent = new Intent("com.mstar.android.intent.action.VGA1_BUTTON");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                break;
            case 332:
                intent = new Intent("com.mstar.android.intent.action.VGA2_BUTTON");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                break;
            case 333:
                intent = new Intent("com.mstar.android.intent.action.VGA3_BUTTON");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                break;
            case 302:
                intent = new Intent("com.mstar.android.intent.action.VGA4_BUTTON");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            case 324:
                intent = new Intent("com.mstar.android.intent.action.TV_BUTTON");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                break;
            case 325:
                intent = new Intent("com.mstar.android.intent.action.AV_BUTTON");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                break;
            case 303:
                intent = new Intent("com.mstar.android.intent.action.DTV_BUTTON");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                break;
            case 334:
                componentName = new ComponentName("com.jrm.localmm", "com.jrm.localmm.ui.main.FileBrowserActivity");
                intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(componentName);
                context.startActivity(intent);
                break;
            case 326:
                  intent = new Intent("com.mstar.android.intent.action.COMPONENT_BUTTON");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 context.startActivity(intent);
                break;
        }
       
    }

    private void setSourceState(int getcode)
    {
        Log.i("Ext_HDMI", "======paramInt=====" + getcode);

        switch (getcode)
        {

            case 1:
                RECEIVE_SOURCE = "324";
                break;
            case 5:
                RECEIVE_SOURCE = "331";
                break;
            case 6:
                RECEIVE_SOURCE = "332";
                break;
            case 7:
                RECEIVE_SOURCE = "333";
                break;
            case 8:
                RECEIVE_SOURCE = "302";
                break;
            case 28:
                RECEIVE_SOURCE = "303";
                break;
            case 24:
                RECEIVE_SOURCE = "328";
                break;
            case 23:
                RECEIVE_SOURCE = "329";
                break;
            case 26:
                RECEIVE_SOURCE = "330";
                break;
            case 27:
                RECEIVE_SOURCE = "327";
                break;
            case 16:
                RECEIVE_SOURCE = "326";
                break;
            case 2:
                RECEIVE_SOURCE = "325";
                break;
            case 34:
                RECEIVE_SOURCE = "3";
                break;
            case 35:
                RECEIVE_SOURCE = "334";
                break;
        }

    }
    public String getInputSource()
    {
        int i;
        if (TvManager.getInstance() != null)
        {
            i = TvCommonManager.getInstance().getCurrentTvInputSource();
            switch (i){
                case 34:
                    setSourceState(34);
                    break;
                case 1:
                    setSourceState(1);
                    break;
                case 28:
                    setSourceState(28);
                    break;
                case 24:
                    setSourceState(24);
                    break;
                case 23:
                    setSourceState(23);
                    break;
                case 25:
                    try
                    {
                        if (TvManager.getInstance().getEnvironment("Ext_HDMI").equalsIgnoreCase("Ext_HDMI3")) {
                            Log.i("Ext_HDMI", "======3333333==");

                            setSourceState(26);
                            break;
                        } else {
                            Log.i("Ext_HDMI", "======44444444==");
                            setSourceState(27);
                            break;
                        }

                    }
                    catch (TvCommonException localTvCommonException1)
                    {
                        localTvCommonException1.printStackTrace();
                    }
                    break;
                case 0:
                    try
                    {
                        Log.i("Ext_HDMI", "====VGA====" + TvManager.getInstance().getEnvironment("Ext_HDMI"));
                        if (TvManager.getInstance().getEnvironment("Ext_HDMI").equals("Ext_VGA1")) {
                            setSourceState(5);
                            break;
                        }

                        if (TvManager.getInstance().getEnvironment("Ext_HDMI").equals("Ext_VGA2")) {
                            setSourceState(6);
                            break;
                        }

                        if (TvManager.getInstance().getEnvironment("Ext_HDMI").equals("Ext_VGA3")) {
                            setSourceState(7);
                            break;
                        }
                        if (TvManager.getInstance().getEnvironment("Ext_HDMI").equals("Ext_VGA4")) {
                            setSourceState(8);
                            break;
                        }

                    }
                    catch (TvCommonException localTvCommonException2)
                    {
                        localTvCommonException2.printStackTrace();
                    }
                    break;
                case 16:
                    setSourceState(16);
                    break;
                case 2:
                    setSourceState(2);
                    break;
            }
        }
        Log.i("Ext_HDMI", "======RECEIVE_SOURCE=====" + RECEIVE_SOURCE);

        return   RECEIVE_SOURCE;
    }
    public boolean isAvilible( String packageName )
    {
          PackageManager packageManager = context.getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for ( int i = 0; i < pinfo.size(); i++ )
        {
            if(pinfo.get(i).packageName.equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }
    public List<AppsItemInfo> queryAppInfo() throws PackageManager.NameNotFoundException {
        List<AppsItemInfo> list = new ArrayList();
        PackageManager pm = context.getPackageManager(); // 获得PackageManager对象
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 通过查询，获得所有ResolveInfo对象.
        List<ResolveInfo> resolveInfos = pm
                .queryIntentActivities(mainIntent,0);
        // 调用系统排序 ， 根据name排序
        // 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序
        Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(pm));
        if (list != null) {
            list.clear();
            for (ResolveInfo reInfo : resolveInfos) {
                String activityName = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
                String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
                String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
                Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标
                PackageInfo mPackageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
                // 创建一个AppInfo对象，并赋值
                AppsItemInfo appInfo = new AppsItemInfo();
                appInfo.setLabel(appLabel);
                appInfo.setActivityName(activityName);
                appInfo.setPackageName(pkgName);
                appInfo.setIcon(icon);
                appInfo.setAppsize("");
                if ((mPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                    appInfo.setAppflags("FLAG_USER");
                }else{
                    appInfo.setAppflags("FLAG_SYSTEM");
                }
                if(!appLabel.equals("NewLuanch")&&!appLabel.equals("Source_CN")) {
                    list.add(appInfo); // 添加至列表中
                }
            }
        }
        return list;
    }
    public static String getPrintSize(long size) {
        //如果字节数少于1024，则直接以B为单位，否则先除于1024，后3位因太少无意义
        if (size < 1024) {
            return String.valueOf(size) + "B";
        } else {
            size = size / 1024;
        }
        //如果原字节数除于1024之后，少于1024，则可以直接以KB作为单位
        //因为还没有到达要使用另一个单位的时候
        //接下去以此类推
        if (size < 1024) {
            return String.valueOf(size) + "KB";
        } else {
            size = size / 1024;
        }
        if (size < 1024) {
            //因为如果以MB为单位的话，要保留最后1位小数，
            //因此，把此数乘以100之后再取余
            size = size * 100;
            return String.valueOf((size / 100)) + "."
                    + String.valueOf((size % 100)) + "MB";
        } else {
            //否则如果要以GB为单位的，先除于1024再作同样的处理
            size = size * 100 / 1024;
            return String.valueOf((size / 100)) + "."
                    + String.valueOf((size % 100)) + "GB";
        }
    }
    public static   byte[] drawableTobyte(Drawable drawable) {

        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        System.out.println("Drawable转Bitmap");
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        //注意，下面三行代码要用到，否则在View或者SurfaceView里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 10;
        bitmap.compress(Bitmap.CompressFormat.PNG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
        return baos.toByteArray();

    }

    public String bytetoString(byte[] paramBitmap)
    {
         return Base64.encodeToString(paramBitmap, 0);
    }
    public String clearMem(){

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infoList = am.getRunningAppProcesses();
        List<ActivityManager.RunningServiceInfo> serviceInfos = am.getRunningServices(100);


        int count = 0;
        if (infoList != null) {
            for (int i = 0; i < infoList.size(); ++i) {
                ActivityManager.RunningAppProcessInfo appProcessInfo = infoList.get(i);
                 //importance 该进程的重要程度  分为几个级别，数值越低就越重要。

                // 一般数值大于RunningAppProcessInfo.IMPORTANCE_SERVICE的进程都长时间没用或者空进程了
                // 一般数值大于RunningAppProcessInfo.IMPORTANCE_VISIBLE的进程都是非可见进程，也就是在后台运行着
                if (appProcessInfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    String[] pkgList = appProcessInfo.pkgList;
                    for (int j = 0; j < pkgList.length; ++j) {//pkgList 得到该进程下运行的包名
                        am.killBackgroundProcesses(pkgList[j]);
                        count++;
                    }
                }

            }
        }


        return getAvailMemory();
    }
    /*
          ops检测
       */
    public int  getGpioDeviceStatus(int pinId){
        int ret = 0;
        try
        {
            if (TvManager.getInstance() != null )
            {
                ret = TvManager.getInstance().
                        getGpioDeviceStatus(pinId);
            }
        }
        catch (TvCommonException e)
        {
            e.printStackTrace();
        }
        return ret;
    }
    public String getState()
    {
        int state85 = getGpioDeviceStatus(85);
        int state86 = getGpioDeviceStatus(86);
        if (state86 == 1) {
            RECEIVE_STATUS = "UNLOADED";
        } else {
            if (state85 == 0) {
                RECEIVE_STATUS = "POWER_OFF";
            }
        }
        return  RECEIVE_STATUS;
    }
    public byte[] bittobyte(Bitmap bmp){
        int bytes = bmp.getByteCount();

        ByteBuffer buf = ByteBuffer.allocate(bytes);
        bmp.copyPixelsToBuffer(buf);

        byte[] byteArray = buf.array();
        return byteArray;
    }
    public  Bitmap getBitmapFormUri(Uri uri) throws FileNotFoundException, IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        //图片分辨率以480x800为标准
        float hh = 1920f;//这里设置高度为800f
        float ww = 1080f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (originalHeight / hh);
        }
        if (be <= 0)
            be = 1;
        //比例压缩
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//设置缩放比例
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return bitmap;//再进行质量压缩
    }
    public static byte[] WeChatBitmapToByteArray(Bitmap bmp, boolean needRecycle) {

        // 首先进行一次大范围的压缩

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
        float zoom = (float)Math.sqrt(300 * 1024 / (float)output.toByteArray().length); //获取缩放比例

        // 设置矩阵数据
        Matrix matrix = new Matrix();
        matrix.setScale(zoom, zoom);

        // 根据矩阵数据进行新bitmap的创建
        Bitmap resultBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        output.reset();

        resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);

        // 如果进行了上面的压缩后，依旧大于32K，就进行小范围的微调压缩
        while(output.toByteArray().length > 300 * 1024){
            matrix.setScale(0.9f, 0.9f);//每次缩小 1/10

            resultBitmap = Bitmap.createBitmap(
                    resultBitmap, 0, 0,
                    resultBitmap.getWidth(), resultBitmap.getHeight(), matrix,true);

            output.reset();
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        }

        return output.toByteArray();
    }
    public static int CheckNetwork(Context context){
        int isConnect = 0;
        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.isConnected()) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    Log.e("network", "当前WiFi连接可用 ");
                    isConnect=  1;
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_ETHERNET) {
                    Log.e("network", "当前有线网络连接可用 ");
                    isConnect =  2;
                }
            } else {
                Log.e("network", "当前没有网络连接，请确保你已经打开网络 ");
                isConnect =  0;
            }
        }else{
            isConnect =  0;
        }
        return isConnect;
    }
    public static Bitmap byteToBitmap(byte[] imgByte) {
        InputStream input = null;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        input = new ByteArrayInputStream(imgByte);
        SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
                input, null, options));
        bitmap = (Bitmap) softRef.get();
        if (imgByte != null) {
            imgByte = null;
        }

        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bitmap;
    }
    /**
     * 链接两个byte[]
     *
     * @param buf1
     * @param buf2
     * @return
     */
    public static byte[] arraycat(byte[] buf1, byte[] buf2) {
        byte[] bufret = null;
        int len1 = 0;
        int len2 = 0;
        if (buf1 != null)
            len1 = buf1.length;
        if (buf2 != null)
            len2 = buf2.length;
        if (len1 + len2 > 0)
            bufret = new byte[len1 + len2];
        if (len1 > 0)
            System.arraycopy(buf1, 0, bufret, 0, len1);
        if (len2 > 0)
            System.arraycopy(buf2, 0, bufret, len1, len2);
        return bufret;
    }
    public static int getLen(byte[] bytes) {
        byte[] buf = new byte[4];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = bytes[i + 2];
        }
        return bytesToInt2(buf, 0);
    }


    public static  boolean isIP(String addr)
    {
        if(addr.length() < 7 || addr.length() > 15 || "".equals(addr))
        {
            return false;
        }
        /**
         * 判断IP格式和范围
         */
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(addr);
        boolean ipAddress = mat.find();
        //============对之前的ip判断的bug在进行判断
        if (ipAddress==true){
            String ips[] = addr.split("\\.");
            if(ips.length==4){
                try{
                    for(String ip : ips){
                        if(Integer.parseInt(ip)<0||Integer.parseInt(ip)>255){
                            return false;
                        }
                    }
                }catch (Exception e){
                    return false;
                }
                return true;
            }else{
                return false;
            }
        }
        return ipAddress;
    }
    public static String getWifiIP(Context context) {
        String ip = null;
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            ip = (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                    + "." + (i >> 24 & 0xFF);
        }
        return ip;
    }
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }
    public static String getRandom(){
        String random  = "";
        for(int i = 0;i<3;i++){
            Random rand = new Random();
            int a = rand.nextInt(10);
            random = random+a;
        }
       Log.i("random",random);
        return random;
    }

    public static void SetTouchToAndroid() {
        setGpioDeviceStatus(mTouchSelA, false);
        setGpioDeviceStatus(mTouchSelB, false);

    }

    public static boolean setGpioDeviceStatus(int mGpio, boolean bEnable) {
        try {
            if (TvManager.getInstance() != null) {
                return TvManager.getInstance().setGpioDeviceStatus(mGpio, bEnable);
            }
        } catch (TvCommonException e) {
            e.printStackTrace();
        }
        return false;
    }
}

