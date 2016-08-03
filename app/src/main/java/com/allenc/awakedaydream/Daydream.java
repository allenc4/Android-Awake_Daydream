package com.allenc.awakedaydream;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A full-screen activity that shows a clock widget.
 * Activity starts a background service that monitors user input (touch gestures) and if a touch
 * is not detected within a specified interval, brings this activity to the top. Once a touch
 * event is registered in this activity, it will close and bring up the most recent application.
 */
public class Daydream extends AppCompatActivity {

    private static final String TAG = Daydream.class.getSimpleName();
    private TextView time;
    private TextView am_pm;
    private TextView currentDirection;
    private TextView destination;
    private RelativeLayout daydreamLayout;
    private RelativeLayout directionLayout;

    private final Handler mHandler;
    private static boolean mIsInForegroundMode;
    private static boolean mIsInDaydreamActivity;
    public static final String CLOSE_ACTIVITY_BROADCAST = "CLOSE_ACTIVITY_BROADCAST";
    private ScreenReceiver screenReceiver;

    private DateFormat sdf = new SimpleDateFormat("hh:mm");

    private float mDownX, mDownY;
    private boolean isOnClick;
    private static final float SCROLL_THRESHOLD = 25f;

    private static boolean notificationAccess = false;

    Runnable timeUpdate = new Runnable() {

        @Override
        public void run() {

            Calendar calendar = new GregorianCalendar();

            if (time != null) {
                time.setText(sdf.format(new Date()) + " ");
                if (calendar.get( Calendar.AM_PM ) == 0) {
                    am_pm.setText("AM");
                } else {
                    am_pm.setText("PM");
                }
            }

            mHandler.postDelayed(timeUpdate, 60000); // Update once a minute

        }
    };

    private final BroadcastReceiver closeActivityBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    public Daydream() {
        mHandler = new Handler();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //Remove title bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daydream);
        mIsInForegroundMode = true;
        mIsInDaydreamActivity = true;

        // Register the broadcast receiver to close the activity
        registerReceiver(closeActivityBroadcast, new IntentFilter(CLOSE_ACTIVITY_BROADCAST));

        // Set the current time
        time = (TextView) findViewById(R.id.daydream_time);
        am_pm = (TextView) findViewById(R.id.daydream_AM_PM);
        currentDirection = (TextView) findViewById(R.id.textview_current_direction);
        destination = (TextView) findViewById(R.id.textview_estimated_arrival);
        daydreamLayout = (RelativeLayout) findViewById(R.id.daydream_layout);
        directionLayout = (RelativeLayout) findViewById(R.id.included_direction);

        setMiscFields();
        // Initially hide the driving direction view
        directionLayout.setVisibility(View.GONE);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        View view = new View(this);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        mDownX = event.getX();
                        mDownY = event.getY();
                        isOnClick = true;
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        if (isOnClick) {
                            Log.d(TAG, "click registered in daydream application");
                            // If in activity_daydream, switch to preference activity
                            Intent intent = new Intent(Daydream.this, Settings.class);
                            startActivity(intent);
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isOnClick && (Math.abs(mDownX - event.getX()) > SCROLL_THRESHOLD ||
                                Math.abs(mDownY - event.getY()) > SCROLL_THRESHOLD)) {
                            Log.d(TAG, "movement detected in daydream application");
                            isOnClick = false;
                            sendBroadcast(new Intent(Daydream.CLOSE_ACTIVITY_BROADCAST));
                        }
                        break;
                }

                return false;
            }
        });
        wm.addView(view, params);

        mHandler.post(timeUpdate);

        startMonitoring();
    }

    public void startMonitoring() {
        // Start the service for monitoring user gesture if enabled
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Base.getAppContext());

        if (prefs.getBoolean(Settings.KEY_APPLICATION_ENABLE, true)) {
            startService(new Intent(this, InactivityService.class));

            // Start the receiver for screen on/off
            screenReceiver = new ScreenReceiver(this);
            screenReceiver.registerReciever();

        }
    }

    private BroadcastReceiver onNotice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String pack = intent.getStringExtra(NotificationListenerService.EXTRA_PACKAGE);
            // We only care about google maps
            if (!pack.equals("com.google.android.gms.maps") && !pack.equals("com.google.android.apps.maps"))
                return;

            String action = intent.getStringExtra(NotificationListenerService.EXTRA_ACTION);
            switch (action) {
                case NotificationListenerService.ACTION_NOTIFICATION_POST:
                    // If the layout is invisible, show it
                    if (directionLayout.getVisibility() != View.VISIBLE) {
                        directionLayout.setVisibility(View.VISIBLE);
                    }

                    // Set the various fields
                    try {
                        String curDirection = intent.getStringExtra(NotificationListenerService.EXTRA_BIG_TEXT).split("\n\n")[0];
                        String estimatedArrival = intent.getStringExtra(NotificationListenerService.EXTRA_BIG_TEXT).split("\n\n")[1];
                        currentDirection.setText(curDirection);
                        destination.setText(estimatedArrival);
                    } catch (Exception e) {

                    }
                    break;
                case NotificationListenerService.ACTION_NOTIFICATION_REMOVED:
                    // Maps notification removed, so hide the view
                    directionLayout.setVisibility(View.GONE);
                    break;
            }
        }
    };

    // Check if the application has notification access or not
    public boolean checkNotificationEnabled() {
        ContentResolver contentResolver = Base.getAppContext().getContentResolver();
        String enabledNotificationListeners = android.provider.Settings.Secure.getString(
                contentResolver, "enabled_notification_listeners");
        String packageName = Base.getAppContext().getPackageName();

        // Check to see if the enabledNotificationListeners String contains our package name
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName))
        {
            // User has not granted the app the requred Notification access permission
            // Show a dialog after a second has passed, to allow the context to be created
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    final AlertDialog.Builder allowNotificationsDialog = new AlertDialog.Builder(Daydream.this);
                    allowNotificationsDialog.setTitle("Notification access required.");
                    allowNotificationsDialog.setMessage("Press OK to open the Notification Access panel and " +
                            "allow access to Awake Daydream to use this application to the fullest extent.");
                    allowNotificationsDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            // Show the Notification Access activity
                            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                        }
                    });
                    allowNotificationsDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            // No action needed
                        }
                    });
                    allowNotificationsDialog.show();
                }
            }, 1000);

            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsInForegroundMode = false;
        mIsInDaydreamActivity = false;

        if (!notificationAccess) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
        }
        Log.v(TAG, "pausing. mIsInForegroundMode: " + mIsInForegroundMode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMiscFields();
        mIsInForegroundMode = true;
        mIsInDaydreamActivity = true;

        if (checkNotificationEnabled()) {
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    onNotice,
                    new IntentFilter(NotificationListenerService.NOTIFICATION_INTENT));
            notificationAccess = true;
        }

        Log.v(TAG, "resuming. mIsInForegroundMode: " + mIsInForegroundMode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't want to unregister the receivers because we want them to continue running
        // even after the onDestroy method is called
        // Unregister the broadcast receiver for touch inputs and screen on/off
//        unregisterReceiver(closeActivityBroadcast);
//        screenReceiver.unRegisterReciever();
    }

    // Set various fields based on user preferences
    private void setMiscFields() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int fontColor = prefs.getInt(Settings.KEY_FONT_COLOR, 0); // Initial color
        time.setTextColor(fontColor);
        am_pm.setTextColor(fontColor);

        if (directionLayout.getVisibility() == View.VISIBLE) {
            currentDirection.setTextColor(fontColor);
            destination.setTextColor(fontColor);
        }
    }

    public static boolean isInForeground() {
        return mIsInForegroundMode;
    }
    public static boolean isInDaydreamActivity() { return mIsInDaydreamActivity; };

    public static void setInForeground(boolean inForeground) {
        mIsInForegroundMode = inForeground;
    }

}
