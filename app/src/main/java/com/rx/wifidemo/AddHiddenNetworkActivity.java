package com.rx.wifidemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.rx.wifidemo.databinding.ActivityAddHiddenNetworkBinding;

import java.util.List;

public class AddHiddenNetworkActivity extends AppCompatActivity {
    ActivityAddHiddenNetworkBinding networkBinding;
    private String mode = null;
    private WifiManager manager;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkBinding = ActivityAddHiddenNetworkBinding.inflate(getLayoutInflater());
        setContentView(networkBinding.getRoot());
        manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // 根据选择的安全协议来决定连接的网络是否需要输入密码（密码框显示与否）
        networkBinding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    networkBinding.passwd.setVisibility(View.VISIBLE);
                } else {
                    networkBinding.passwd.setVisibility(View.GONE);
                }
                mode = networkBinding.spinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        networkBinding.connect.setOnClickListener(v -> {
            String ssid = networkBinding.ssid.getText().toString();
            String passwd = networkBinding.passwd.getText().toString();
            //Android11及以上可以使用，清除建议列表，可以断开当前的网络
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                List<WifiNetworkSuggestion> networkSuggestions = manager.getNetworkSuggestions();
                manager.removeNetworkSuggestions(networkSuggestions);
            }
            WifiUtils.connectWifiForQ(manager, ssid, null, passwd,true, mode);
        });


    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}