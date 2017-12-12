package com.iboard.tusm.iboardmstar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by tusm on 17/12/9.
 */

public class StartBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {     // boot
            Intent service = new Intent(context,ForegroundService.class);
            context.startService(service);
        }
    }
}
