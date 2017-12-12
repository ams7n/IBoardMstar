package com.iboard.tusm.iboardmstar;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Created by tusm on 17/12/4.
 */

public class IconServer {
    private static final int port = 45780;
    private ServerSocket serverSocket;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    public  boolean isRun = true;
    private  final byte REQUEST_ICON = (byte)0xF1;
    private  final byte REQUEST_END = (byte)0x2F;
    private List<AppsItemInfo> list;

    public IconServer(List<AppsItemInfo> list) {
        this.list = list;
        new IconThread().start();
    }

    class IconThread extends Thread {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port);
                socket = serverSocket.accept();
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                while(isRun){
                    int count = 0;

                    while (count == 0) {
                        count = inputStream.available();
                    }

                    byte[] bb = new byte[count];
                    int len;


                    if ((len = inputStream.read(bb)) != -1) {
                        Log.i("string", "-----  "+new String(bb));

                        ControlSwitch(bb);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void ControlSwitch (byte[] bb) throws IOException {
            switch (bb[1]){
                case REQUEST_ICON:
                    for(int i = 0;i<list.size();i++){
                        byte[] arrayOfByte1 = ImagicUtill.drawableTobyte(list.get(i).getIcon());
                        Log.i("size", "===icom size==== " + arrayOfByte1.length);
                        byte[] arrayOfByte2 = new byte[6];
                        byte[] arrayOfByte3 = ImagicUtill.intToBytes2(arrayOfByte1.length);
                        byte[] paramUri = new byte[arrayOfByte2.length + arrayOfByte1.length];
                        arrayOfByte2[0] = (byte) 0x92;
                        arrayOfByte2[1] = (byte) 0x1F;
                        System.arraycopy(arrayOfByte3, 0, arrayOfByte2, 2, arrayOfByte3.length);
                        System.arraycopy(arrayOfByte2, 0, paramUri, 0, arrayOfByte2.length);
                        System.arraycopy(arrayOfByte1, 0, paramUri, arrayOfByte2.length, arrayOfByte1.length);
                        outputStream.write(paramUri);
                        outputStream.flush();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(500);
                        sendMsg("END",(byte)0x92,(byte)0xF2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    break;
                case REQUEST_END:
                    serverSocket.close();
                    socket.close();
                    outputStream.close();
                    inputStream.close();
                    isRun = false;
                    break;
            }
        }
    }


    private  void sendMsg(String msg,byte bb0,byte bb1) throws IOException{
        byte[] strData = msg.getBytes();
        byte[] arrayOfByte1 = new byte[6];
        byte[] arrayOfByte2 = new byte[arrayOfByte1.length + strData.length];
        arrayOfByte1[0] = bb0;
        arrayOfByte1[1] = bb1;
        byte[] arrayOfByte3 = ImagicUtill.intToBytes2(strData.length);
        System.arraycopy(arrayOfByte3, 0, arrayOfByte1, 2, arrayOfByte3.length);
        System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 0, arrayOfByte1.length);
        System.arraycopy(strData, 0, arrayOfByte2, arrayOfByte1.length, strData.length);
        outputStream.write(arrayOfByte2);
        outputStream.flush();

    }

    public boolean isRun() {
        return isRun;
    };

   public void CloseServer(){
       try {
           if(serverSocket!=null&&!serverSocket.isClosed()){
               serverSocket.close();
           }
           if(socket!=null&&!socket.isClosed()){
               socket.close();
           }
          if(outputStream!=null){
              outputStream.close();
          }
           if(inputStream!=null){
               inputStream.close();
           }

           isRun = false;
       } catch (IOException e) {
           e.printStackTrace();
       }

   }
}
