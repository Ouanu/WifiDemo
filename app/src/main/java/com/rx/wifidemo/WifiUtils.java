package com.rx.wifidemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.MacAddress;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class WifiUtils {

    public static void searchWifiList(WifiManager manager) {
        manager.startScan();
    }

    /**
     * 获取附近的WiFi列表
     *
     * @param manager WifiManager
     * @param flag    是否保留重名但BSSID不同的wifi     true保留，false不保留
     * @return wifi列表
     */
    public static List<ScanResult> scanResults(WifiManager manager, boolean flag) {
        List<ScanResult> scanResults = new ArrayList<>();
        HashSet<String> hs = new HashSet<>();
        Log.d("WifiUtils", "scanResults: " + manager.getScanResults().size());
        if (flag) {
            scanResults = manager.getScanResults();
            return scanResults;
        }
        for (ScanResult scanResult : manager.getScanResults()) {
            if (hs.add(scanResult.SSID)) {
                scanResults.add(scanResult);
            }
        }
        return scanResults;
    }

    /**
     * 获取配置好的wifi（输入过密码的）
     *
     * @param context 上下文
     * @param manager WifiManager
     * @return 配置好的列表
     */
    public static List<WifiConfiguration> getConfiguredNetworks(Context context, WifiManager manager) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return new ArrayList<>();
        }
        return manager.getConfiguredNetworks();
    }

    /**
     * 创建连接
     * @param manager WifiManager
     * @param ssid  Wifi名称
     * @param bssid 唯一标识（可以为空）
     * @param passwd    密码  （当前网络是开放网络时，可以为空）
     * @param isHidden  是否是隐藏网络
     * @param capabilities  安全协议（根据协议选择连接方式）
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void connectWifiForQ(WifiManager manager, String ssid, String bssid, String passwd, boolean isHidden, String capabilities) {
        if (capabilities.contains("WPA-PSK") || capabilities.contains("WPA2-PSK")) {
            setWPA2ForQ(manager, ssid, bssid, passwd, isHidden);
        } else {
            setESSForQ(manager, ssid, isHidden);
        }
    }

    // WPA2-PSK
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static int setWPA2ForQ(WifiManager manager, String ssid, String bssid, String passwd, boolean isHidden) {
        WifiNetworkSuggestion suggestion;
        if (bssid == null) {
            suggestion= new WifiNetworkSuggestion.Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(passwd)
                    .setIsHiddenSsid(isHidden)
                    .build();
        } else {
            suggestion= new WifiNetworkSuggestion.Builder()
                    .setSsid(ssid)
                    .setBssid(MacAddress.fromString(bssid))
                    .setWpa2Passphrase(passwd)
                    .setIsHiddenSsid(isHidden)
                    .build();
        }
        List<WifiNetworkSuggestion> suggestions = new ArrayList<>();
        suggestions.add(suggestion);
        int status = manager.addNetworkSuggestions(suggestions);
        if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            // 连接失败
            Log.d("WifiUtils", "setWPA2ForQ: 添加失败");
        } else {
            Log.d("WifiUtils", "setWPA2ForQ: 添加成功");
        }
        return status;
    }

    // ESS
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static int setESSForQ(WifiManager manager, String ssid, boolean isHidden) {
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setIsHiddenSsid(isHidden)
                .build();
        List<WifiNetworkSuggestion> suggestions = new ArrayList<>();
        suggestions.add(suggestion);
        int status = manager.addNetworkSuggestions(suggestions);
        if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            // 连接失败
            Log.d("WifiUtils", "setWPA2ForQ: 添加失败");
        } else {
            Log.d("WifiUtils", "setWPA2ForQ: 添加成功");
        }
        return status;

    }
}
