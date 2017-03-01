package com.dominik.wlancrawl;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by dominik on 01.03.17.
 */
public class MyReceiver extends BroadcastReceiver
{
    private static boolean isConnected = false;

    public MyReceiver()
    {
        super();

        ConnectivityManager connManager = (ConnectivityManager) App.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        isConnected = mWifi.isConnectedOrConnecting();
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
        {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected())
            {
                // Wifi is connected
                Log.d("Inetify", "Wifi is connected: " + String.valueOf(networkInfo));
                isConnected = true;
            }
        }
        else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
        {
            NetworkInfo networkInfo =
                    intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI && !networkInfo.isConnected())
            {
                // Wifi is disconnected
                Log.d("Inetify", "Wifi is disconnected: " + String.valueOf(networkInfo));
            }
        }
    }

    public static boolean getIsConnected()
    {
        return isConnected;
    }
}
