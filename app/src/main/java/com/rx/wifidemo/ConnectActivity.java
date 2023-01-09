package com.rx.wifidemo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.rx.wifidemo.databinding.ActivityConnectBinding;
import java.util.List;

public class ConnectActivity extends AppCompatActivity {
    ActivityConnectBinding connectBinding;
    String ssid;
    String capability;
    String passwd;
    String bssid;
    ConnectivityManager manager;
    WifiManager wifiManager;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectBinding = ActivityConnectBinding.inflate(getLayoutInflater());
        setContentView(connectBinding.getRoot());
        manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (getIntent() != null) {
            ssid = getIntent().getStringExtra("SSID");
            capability = getIntent().getStringExtra("CAPABILITY");
            passwd = getIntent().getStringExtra("PASSWD");
            bssid = getIntent().getStringExtra("BSSID");
        }
        if (ssid == null || capability == null || bssid == null) {
            finish();
        }
        if (passwd == null) {
            connectBinding.passwd.setText("");
        } else {
            connectBinding.passwd.setText(passwd);
        }
        connectBinding.ssid.setText(ssid);
        connectBinding.connect.setOnClickListener(v -> {
            passwd = connectBinding.passwd.getText().toString();
            //Android11及以上可以使用，清除建议列表，可以断开当前的网络
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                List<WifiNetworkSuggestion> networkSuggestions = wifiManager.getNetworkSuggestions();
                wifiManager.removeNetworkSuggestions(networkSuggestions);
            }
            WifiUtils.connectWifiForQ(wifiManager, ssid, bssid, passwd, false, capability);
            finish();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
