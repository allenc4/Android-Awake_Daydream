package com.allenc.awakedaydream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Created by allenc4 on 2/19/2016.
 */
public class ScreenReceiver extends BroadcastReceiver {

    public static boolean wasScreenOn = true;
    public static final String TAG = ScreenReceiver.class.getSimpleName();
    Context context;
    IntentFilter screenStateFilter;


    public ScreenReceiver(Context context) {
        this.context = context;
        screenStateFilter  = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
    }

    public void registerReciever(){
        context.registerReceiver(this, screenStateFilter);
    }
    public void unRegisterReciever(){
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // Screen turned off
            Log.d(TAG, "Screen off");
            wasScreenOn = false;

            // Stop InactivityService service
            context.stopService(new Intent(context, InactivityService.class));

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // Screen turned on
            Log.d(TAG, "Screen on");
            wasScreenOn = true;

            // Start InactivityService service
            context.startService(new Intent(context, InactivityService.class));

        }
    }

}
