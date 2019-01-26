package com.tananaev.logcat;

import android.app.Application;
import android.content.Context;

/**
 * MyApplication
 * Author: chenbin
 * Time: 2019-01-24
 */
public class MyApplication extends Application {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
