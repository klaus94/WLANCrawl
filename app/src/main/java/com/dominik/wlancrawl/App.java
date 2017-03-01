package com.dominik.wlancrawl;

import android.app.Application;
import android.content.Context;

/**
 * Created by dominik on 01.03.17.
 */
public class App extends Application
{
    private static Context context;

    public void onCreate() {
        super.onCreate();
        App.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return App.context;
    }

}
