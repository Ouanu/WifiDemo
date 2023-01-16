# WifiDemo
Android 12 Wifi Demo

## OldWifiUtils.java (额外补充，就在根目录下）

### 连接Wifi的代码仅在Android 4.0 和 Android 9.0中试过，如果出现连接不上，大概率是WifiConfiguration没配置对
### 连接Wifi的步骤：
1. 创建配置，需要Wifi的SSID（名称），password（密码），hidden（是否隐藏，非必选，用不上就不要添加这个属性），capabilities（加密的方法，有一堆，比如WPA2-PSK什么的，加密的会包含未加密的，所以要根据字符串来进行匹配）。

```
/**
     * 创建Wifi配置
     *
     * @param SSID         wifi名称
     * @param password     wifi密码
     * @param hidden       网络是否隐藏（该方法与添加隐藏网络通用）
     * @param capabilities 网络安全协议
     * @return 配置好的wifi
     */
    public static WifiConfiguration createWifiInfo(String SSID, String password, boolean hidden, String capabilities) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = "\"" + SSID + "\"";
        configuration.hiddenSSID = hidden;
        if (capabilities.contains("WPA-PSK") || capabilities.contains("WPA2-PSK")) {
            setWPA(configuration, password);
        } else if (capabilities.contains("WEP")) {
            setWEP(configuration, password);
        } else {
            setESS(configuration);
        }
        return configuration;
    }
```



2. 将配置传入连接的方法中即可完成连接。


    
```
    /**
     * 连接wifi
     * @param manager WifiManager
     * @param configuration Wifi配置
     * @return 是否连接成功
     */
    public static boolean connectWifi(WifiManager manager, WifiConfiguration configuration) {
        int id = manager.addNetwork(configuration);
        WifiInfo connectionInfo = manager.getConnectionInfo();
        manager.disableNetwork(connectionInfo.getNetworkId());
        boolean b = manager.enableNetwork(id, true);
        Log.d("WifiManagerUtils", "connectWifi: 连接状态=" + b);
        if (b) {
            manager.saveConfiguration();
        } else {
            Log.d("WifiManagerUtils", configuration.toString());
        }
        return b;
    }
```
