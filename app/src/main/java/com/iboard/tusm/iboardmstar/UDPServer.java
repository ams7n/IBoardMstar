package com.iboard.tusm.iboardmstar;

/**
 * Created by tusm on 17/11/21.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;



import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Created by Jason on 2016/5/18.
 */
 class UDPServer implements Runnable {
    private static   DatagramSocket ds = null;
    private static   DatagramSocket sendSocket = null;
    public static boolean udpLife = true;
    private DatagramPacket dpRcv = null;
    private DatagramPacket dpSend = null;
    private InetSocketAddress inetSocketAddress = null;
    private String ip = "255.255.255.255";
    private String json;
    private byte[] msgRcv = new byte[1024];
    private int port = 12457;
    private int sendport = 12458;
    private boolean udpLifeOver = true;
    private SharedPreferences sharedPreferences;
    private Context context;


    public UDPServer(Context context) {
        this.context = context;
        sharedPreferences  = context.getSharedPreferences("imgic3", Context.MODE_PRIVATE);

    }

    private void SetSoTime(int ms) throws SocketException {
        ds.setSoTimeout(ms);
    }

    //返回udp生命线程因子是否存活
    public boolean isUdpLife(){
        if (udpLife){
            return true;
        }

        return false;
    }

    //返回具体线程生命信息是否完结
    public boolean getLifeMsg(){
        return udpLifeOver;
    }

    //更改UDP生命线程因子
    public void setUdpLife(boolean b){
        udpLife = b;
    }

    public void Send(byte[] str) throws IOException {
        Log.i("SocketInfo", "客户端IP：" + this.dpRcv.getAddress().getHostAddress() + "客户端Port:" + this.dpRcv.getPort());
        InetAddress localInetAddress = InetAddress.getByName(this.ip);
        dpSend = new DatagramPacket(str, str.length, localInetAddress, this.sendport);
        sendSocket.send(dpSend);
        Log.i("SocketSendto", "UDP发送");
    }


    @Override
    public void run() {
        inetSocketAddress = new InetSocketAddress(ip, port);
        try {
            if(ds==null) {
                ds = new DatagramSocket(inetSocketAddress);
            }
            if(sendSocket==null) {
                sendSocket = new DatagramSocket(sendport);
            }
            Log.i("SocketInfo", "UDP服务器已经启动");
//            SetSoTime(3000);
            //设置超时，不需要可以删除
        } catch (SocketException e) {
            e.printStackTrace();
        }

        dpRcv = new DatagramPacket(msgRcv, msgRcv.length);
        while (udpLife) {
            try {
                Log.i("SocketInfo", "UDP监听中");
                 ds.receive(dpRcv);
                String string = new String(dpRcv.getData(), dpRcv.getOffset(), dpRcv.getLength());
                Log.i("SocketInfo", "收到信息：" + string);

                String name = sharedPreferences.getString("name", "iBoardHub");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("CMD","RECEIVE_CAST");
                    jsonObject.put("DeviceID",name);
                    String ip = ImagicUtill.GetIpAddress();
                    if(ImagicUtill.isIP(ip)) {
                        jsonObject.put("ServerIP", ip);
                    }else{
                        jsonObject.put("ServerIP", ImagicUtill.getWifiIP(context));
                    }
                    jsonObject.put("ServerPort","23568");
                    byte[] UDPjsonData = jsonObject.toString().getBytes();
                    byte[] headData  = new byte[6];
                    byte[] UDPsendData = new byte[UDPjsonData.length + headData.length];
                    headData[0] = (byte)0x91;
                    headData[1] = (byte)0xA1;
                    byte[] inttobyte = ImagicUtill.intToBytes2(UDPjsonData.length);
                    System.arraycopy(inttobyte, 0, headData, 2, inttobyte.length);
                    System.arraycopy(headData, 0, UDPsendData, 0, headData.length);
                    System.arraycopy(UDPjsonData, 0, UDPsendData, headData.length, UDPjsonData.length);
                    Send(UDPsendData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
//        ds.close();
//        sendSocket.close();
//        ds = null;
//        sendSocket = null;
        Log.i("SocketInfo","UDP监听关闭");
        //udp生命结束
        udpLifeOver = false;
    }
}