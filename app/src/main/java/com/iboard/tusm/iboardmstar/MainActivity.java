package com.iboard.tusm.iboardmstar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,SlidingMenu.OnOpenedListener {
    private SlidingMenu menu;
    private int width;
    private TextView name,connect,netName,ip,set;
    private ImageView imageView;
    private EditText editText;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)findViewById(R.id.imageView_set);
        imageView.setOnClickListener(this);
        menu = new SlidingMenu(this);
        menu.setMode(1);
        menu.setTouchModeAbove(1);
        width = getWindowManager().getDefaultDisplay().getWidth();
        menu.setBehindOffset(width - 500);
        menu.setShadowWidth(2);
        menu.setFadeDegree(0.35F);
        menu.attachToActivity(this,1);
        menu.setMenu(R.layout.slienmen);
        menu.setOnOpenedListener(this);
         sharedPreferences = getSharedPreferences("imgic3", Context.MODE_PRIVATE);
        name = menu.getMenu().findViewById(R.id.textView4);
        connect = menu.getMenu().findViewById(R.id.textView6);
        set = menu.getMenu().findViewById(R.id.txSet);
        netName = menu.getMenu().findViewById(R.id.textView8);
        ip = menu.getMenu().findViewById(R.id.textView10);
        editText = menu.getMenu().findViewById(R.id.editText);
        name.setOnClickListener(this);
        set.setOnClickListener(this);
        editText.setOnKeyListener(onKeyListener);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b) {
                    sharedPreferences.edit().putString("name", editText.getText().toString()).commit();
                    editText.setVisibility(View.GONE);
                    name.setText(editText.getText().toString());
                    name.setVisibility(View.VISIBLE);
                }
            }
        });
        startService(new Intent(this,ForegroundService.class));
//        Intent intent = new Intent(MainActivity.this,MediaActivity.class);
//        String FtpUploadPath = Environment.getExternalStorageDirectory().getPath() + "/FtpFileTest/IMG_0001.MP4";
//        intent.putExtra("media",FtpUploadPath);
//        startActivity(intent);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
          case   R.id.textView4:
            editText.setVisibility(View.VISIBLE);
            name.setVisibility(View.INVISIBLE);
                break;
            case R.id.imageView_set:
                menu.showMenu();
                break;
            case R.id.txSet:
                menu.toggle();
                break;

        }
    }

    @Override
    public void onOpened() {

        String strname = sharedPreferences.getString("name", "iBoardHub");

        name.setText(strname);
        editText.setText(strname);
        String strIp  = ImagicUtill.GetIpAddress();;
        if(ImagicUtill.isIP(strIp)) {
            ip.setText(strIp);
        }else{
            ip.setText(ImagicUtill.getWifiIP(MainActivity.this));
        }


        int network  = ImagicUtill.CheckNetwork(this);
        switch (network){
            case 1: //wifi
                WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                int wifiState = wifiMgr.getWifiState();
                WifiInfo info = wifiMgr.getConnectionInfo();
                String wifiId = info != null ? info.getSSID() : "获取失败";
                netName.setText(wifiId.substring(1,wifiId.length()-1));
                break;
            case 2: //有线
                netName.setText("有线网络");
                break;
            case 0: //无网络
                netName.setText("未连接网络");
                if(ImagicUtill.GetIpAddress().equalsIgnoreCase("192.168.43.1")){
                    try {
                        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        //拿到getWifiApConfiguration()方法
                        Method method = manager.getClass().getDeclaredMethod("getWifiApConfiguration");
                        //调用getWifiApConfiguration()方法，获取到 热点的WifiConfiguration
                        WifiConfiguration configuration = (WifiConfiguration) method.invoke(manager);
                       String ssid = configuration.SSID;
                        netName.setText(ssid);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        if(UDPServer.udpLife){
            connect.setText("无设备连接");
        }else{
            connect.setText("设备已连接");
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==82){
            menu.showMenu();
        }

        return super.onKeyDown(keyCode, event);
    }


    private View.OnKeyListener onKeyListener = new View.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
                /*隐藏软键盘*/
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(inputMethodManager.isActive()){
                    inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                sharedPreferences.edit().putString("name", editText.getText().toString()).commit();
                editText.setVisibility(View.GONE);
                name.setText(editText.getText().toString());
                name.setVisibility(View.VISIBLE);

                return true;
            }
            return false;
        }
    };
}
