package com.iboard.tusm.iboardmstar;

import java.io.File;

import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;

public class ScreenshotObserver extends FileObserver {  
    private static final String TAG = "ScreenshotObserver";   
    private static final String PATH =  Environment.getExternalStorageDirectory()
			.getAbsolutePath() +"/Pictures/Screenshots/";
    		private OnScreenshotTakenListener listener;  
    private String lastTakenPath;  
  
    public ScreenshotObserver(OnScreenshotTakenListener listener) {  
        super(PATH, FileObserver.CLOSE_WRITE);  
        System.out.println(PATH);
        this.listener = listener;  
    }  
  
    @Override  
    public void onEvent(int event, String path) {  
        if (path==null || event!=FileObserver.CLOSE_WRITE){  
            //dont care  
        }  
        else if (lastTakenPath!=null && path.equalsIgnoreCase(lastTakenPath)){  
            //had observer,ignore this  
        }  
        else {  
            lastTakenPath = path;  
            File file = new File(PATH+path);  
            listener.onScreenshotTaken(Uri.fromFile(file));  
            System.out.println("sucess");
        }  
    }  
  
    public void start() {  
        super.startWatching();  
    }  
  
    public void stop() {  
        super.stopWatching();  
    }  
    public interface OnScreenshotTakenListener {  
        void onScreenshotTaken(Uri uri);  
    }
}  
