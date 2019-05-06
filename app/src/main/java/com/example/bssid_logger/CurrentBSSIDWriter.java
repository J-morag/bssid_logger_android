package com.example.bssid_logger;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.TextView;

public class CurrentBSSIDWriter extends Thread {

    private static final int sleepTimeMs = 10*1000;
    private TextView bsssid_view;
    private Context baseContext;

    public CurrentBSSIDWriter(TextView bssid_view, Context baseContext) {
        super();
        this.bsssid_view = bssid_view;
        this.baseContext = baseContext;
    }

    @Override
    public void run() {
//        super.run();
        while(true){
            String bssid = getCurrentBssid(baseContext);
            String ssid = getCurrentSsid(baseContext);
            bsssid_view.setText(bssid);
            try {
                Thread.sleep(sleepTimeMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getCurrentBssid(Context context) {
//        String ssid = null;
//        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        if (networkInfo.isConnected()) {
//            final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
//            return connectionInfo.getBSSID();
//        }
//        return null;
        if (context == null) {
            return "";
        }
        final Intent intent = context.registerReceiver(
                null, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        if (intent != null) {
            final WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
            if (wifiInfo != null) {
                final String ssid = wifiInfo.getBSSID();
                if (ssid != null) {
                    return ssid;
                }
            }
        }
        return "";
    }

    public static String getCurrentSsid(Context context) {
//        String ssid = null;
//        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        if (networkInfo.isConnected()) {
//            final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
//            return connectionInfo.getSSID();
//        }
//        return null;
        return getWifiSSID(context);
    }

    public static String getWifiSSID(Context context) {
        if (context == null) {
            return "";
        }
        final Intent intent = context.registerReceiver(
                null, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        if (intent != null) {
            final WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
            if (wifiInfo != null) {
                final String ssid = wifiInfo.getSSID();
                if (ssid != null) {
                    return ssid;
                }
            }
        }
        return "";
    }
}
