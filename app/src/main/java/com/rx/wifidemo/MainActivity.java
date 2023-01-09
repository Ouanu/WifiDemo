package com.rx.wifidemo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rx.wifidemo.databinding.ActivityMainBinding;
import com.rx.wifidemo.databinding.ItemWifiBinding;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding mainBinding;
    List<ScanResult> results = new ArrayList<>();
    List<WifiConfiguration> configurations = new ArrayList<>();
    WifiManager manager;
    ConnectivityManager connectivityManager;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                Log.d("WifiFragment", "onReceive: 刷新数据");
                results = WifiUtils.scanResults(manager, true);
                mainBinding.scanResult.getAdapter().notifyDataSetChanged();
            } else if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        Log.d("WifiFragment", "onReceive: wifi 关闭");
                        mainBinding.switchWifi.setText("已停用");
                        results = new ArrayList<>();
                        mainBinding.scanResult.getAdapter().notifyDataSetChanged();
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                    case WifiManager.WIFI_STATE_ENABLING:
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        Log.d("WifiFragment", "onReceive: wifi 打开");
                        mainBinding.switchWifi.setText("已启用");
                        break;
                }
            }
        }
    };
    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_COARSE_LOCATION,false);
                        if (fineLocationGranted != null && fineLocationGranted) {
                            // Precise location access granted.
                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            // Only approximate location access granted.
                        } else {
                            // No location access granted.
                        }
                    }
            );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //监听网络连接状态
        connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback(){
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                Log.d("MainActivity", "onAvailable: 网络已连接");
                Toast.makeText(MainActivity.this, "已连接网络", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Log.d("MainActivity", "onUnavailable: 网络已断开");
                Toast.makeText(MainActivity.this, "已断开网络", Toast.LENGTH_SHORT).show();
            }
        });

        // 监听广播（wifi开启与关闭）
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(receiver, filter);


        results = WifiUtils.scanResults(manager, true);
        configurations = WifiUtils.getConfiguredNetworks(this, manager);
        mainBinding.scanResult.setLayoutManager(new LinearLayoutManager(this));
        mainBinding.scanResult.setAdapter(new WifiScanResultAdapter());
        mainBinding.scanResult.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = 5;
                outRect.bottom = 5;
            }
        });
        mainBinding.switchWifi.setOnClickListener(v -> {
            if (!manager.isWifiEnabled()) {
                manager.setWifiEnabled(true);
            } else {
                manager.setWifiEnabled(false);
            }

            Log.d("MainActivity", "onCreate: " + manager.getWifiState());
        });
        mainBinding.refresh.setOnClickListener(v -> WifiUtils.searchWifiList(manager));
        mainBinding.addHideNetwork.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddHiddenNetworkActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });



        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    class WifiScanResultItem extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemWifiBinding wifiBinding;
        ScanResult scanResult;
        public WifiScanResultItem(@NonNull View itemView) {
            super(itemView);
            wifiBinding = ItemWifiBinding.bind(itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(int position) {
            String ssid = results.get(position).SSID + results.get(position).capabilities;
            scanResult = results.get(position);
            wifiBinding.ssid.setText(ssid);
            wifiBinding.dBm.setText(String.valueOf(results.get(position).level));
        }


        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
            intent.putExtra("SSID", scanResult.SSID);
            intent.putExtra("BSSID", scanResult.BSSID);
            intent.putExtra("CAPABILITY", scanResult.capabilities);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    class WifiScanResultAdapter extends RecyclerView.Adapter<WifiScanResultItem> {

        @NonNull
        @Override
        public WifiScanResultItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemWifiBinding wifiBinding = ItemWifiBinding.inflate(getLayoutInflater(), parent, false);
            return new WifiScanResultItem(wifiBinding.getRoot());
        }

        @Override
        public void onBindViewHolder(@NonNull WifiScanResultItem holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return results.size();
        }
    }
}