package com.allenc.awakedaydream;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by allenc4 on 2/28/2016.
 */
public class NotificationListenerService extends android.service.notification.NotificationListenerService{

    private static final String TAG = NotificationListenerService.class.getSimpleName();

    public static final String NOTIFICATION_INTENT = "notificationIntent";
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_PACKAGE = "package";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_TEXT = "text";
    public static final String EXTRA_BIG_TEXT ="bigText";

    public static final String ACTION_NOTIFICATION_POST = "notificationPosted";
    public static final String ACTION_NOTIFICATION_REMOVED = "notificationRemoved";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        // There is a new notification posted. Send to Daydream main activity to parse
        Intent notificationIntent = new Intent(NOTIFICATION_INTENT);
        Bundle extras = statusBarNotification.getNotification().extras;
        String title = extras.getString(Notification.EXTRA_TITLE);
        String text = null, bigText = null;
        if (extras.getCharSequence(Notification.EXTRA_TEXT) != null) {
            text = extras.getCharSequence(Notification.EXTRA_TEXT).toString();
        }
        if (extras.getCharSequence(Notification.EXTRA_BIG_TEXT) != null) {
            bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString();
        }

        notificationIntent.putExtra(EXTRA_PACKAGE, statusBarNotification.getPackageName());
        notificationIntent.putExtra(EXTRA_TITLE, title);
        notificationIntent.putExtra(EXTRA_TEXT, text);
        notificationIntent.putExtra(EXTRA_BIG_TEXT, bigText);
        notificationIntent.putExtra(EXTRA_ACTION, ACTION_NOTIFICATION_POST);

        LocalBroadcastManager.getInstance(this).sendBroadcast(notificationIntent);

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
        Intent notificationIntent = new Intent(NOTIFICATION_INTENT);
        Bundle extras = statusBarNotification.getNotification().extras;
        String title = null, text = null, bigText = null;
        if (extras != null) {
            title = extras.getString(Notification.EXTRA_TITLE);
            if (extras.getCharSequence(Notification.EXTRA_TEXT) != null) {
                text = extras.getCharSequence(Notification.EXTRA_TEXT).toString();
            }
            if (extras.getCharSequence(Notification.EXTRA_BIG_TEXT) != null) {
                bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString();
            }
        }

        notificationIntent.putExtra(EXTRA_PACKAGE, statusBarNotification.getPackageName());
        notificationIntent.putExtra(EXTRA_TITLE, title);
        notificationIntent.putExtra(EXTRA_TEXT, text);
        notificationIntent.putExtra(EXTRA_BIG_TEXT, bigText);
        notificationIntent.putExtra(EXTRA_ACTION, ACTION_NOTIFICATION_REMOVED);

        LocalBroadcastManager.getInstance(this).sendBroadcast(notificationIntent);
    }

}
