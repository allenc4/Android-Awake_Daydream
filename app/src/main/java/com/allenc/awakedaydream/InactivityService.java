package com.allenc.awakedaydream;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

/**
 * Detects uer taps (interaction) on screen.
 * Service adds a zero sized view to the WindowManager which gets notified if there is
 * any interaction outside of the view (since the size is zero, ALL activity is notified).
 *
 * Created by allenc4 on 2/16/2016.
 */
public class InactivityService extends Service {

    protected static final String TAG = InactivityService.class.getSimpleName();
//    protected static final String PREFS_LAST_INPUT_TIME_KEY = "latestScreenInputTime";
    private static final long MAX_INACTIVITY_TIME = 30000;  // If a touch is not registered after 30 seconds

    private static long lastInputTime = -1;  // Stored in milli seconds

    private static final Handler mHandler = new Handler();

    Runnable inactivityMonitor = new Runnable() {

        @Override
        public void run() {

            boolean startDaydream = !Daydream.isInDaydreamActivity();

            if (!Settings.enabled)
                return;

            if (lastInputTime > 0) {
                long millis = System.currentTimeMillis();

                // Check if the last time a touch was registered exceeds the max inactivity time
                if (millis - lastInputTime >= MAX_INACTIVITY_TIME && !Daydream.isInForeground()) {

                    if (Settings.areWhitelistedApps()) {
                        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                        List<ActivityManager.RunningAppProcessInfo> tasks = manager.getRunningAppProcesses();

                        ActivityManager.RunningAppProcessInfo foregroundApp = tasks.get(0);
                        if (Settings.getWhitelistedApps().contains(foregroundApp.processName)) {
                            Log.v(TAG, "Whitelisted application found. Ignoring inactivity.");
                            startDaydream = false;
                        }
                    }

                    // Bring up the Daydream activity
                    if (startDaydream) {
                        Log.v(TAG, "Inactivity time maxed out. Calling daydream activity.");
                        Intent daydreamIntent = new Intent(InactivityService.this, Daydream.class);
                        daydreamIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(daydreamIntent);
                    }
                }
            }

            mHandler.postDelayed(inactivityMonitor, 15000); // Update once every 15 seconds

        }
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Create a zero sized window view
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                0, 0, 0, 0,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT);
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        View view = new View(this);
        view.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
            // Register the last input time
            lastInputTime = System.currentTimeMillis();

            return false;
            }
        });
        wm.addView(view, params);

        mHandler.post(inactivityMonitor);

        return START_STICKY;
    }

}