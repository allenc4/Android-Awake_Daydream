package com.allenc.awakedaydream;

import android.app.Application;
import android.content.Context;

/**
 * Created by allenc4 on 2/27/2016.
 */
public class Base extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Base.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return Base.context;
    }
}
