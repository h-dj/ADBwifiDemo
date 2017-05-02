#### 一、前言：
1. 为了没有数据就不能调试的尴尬，需要学习一下adb wifi调试
2. 不过前提是手机必须root。
3. 同时学习一下adb命令

### 二、app工具效果图
![image](https://github.com/naughty-pig/ADBwifiDemo/blob/master/shootcut/1.png)

![image](https://github.com/naughty-pig/ADBwifiDemo/blob/master/shootcut/2.png)

### 三、实现步骤:
1. 首先获取局域网的ip地址
```
    /**
     * 获取32整型ip地址
     */
    private String getLocalIpAddress() {
        //1. 获取wifi服务
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        //判断是否开启wifi
        if (!wifiManager.isWifiEnabled()) {
            //没有则开启
            wifiManager.setWifiEnabled(true);
        }
        //获取当前连接wifi的动态信息
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        //获取32位ip地址
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);

    }

    /**
     * 把32位ip转化为转换为四位X.X.X.X的本地ip地址
     *
     * @param ipAddress
     * @return
     */
    private String intToIp(int ipAddress) {
        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }


```

2. 设置adb服务连接的ip和端口，并且开启adb
```
/**
     * 设置adb服务连接的ip和端口，并且开启adb
     *
     * @return
     */
    public String openAdb() {
        LogUtil.e("openAdb" + isConn);
        String result = null;
        if (!isConn) {
            result = "adb connect: " + getLocalIpAddress(); //获取ip地址;
            execShell(new String[]{"setprop service.adb.tcp.port 5555", "stop adbd", "start adbd"});
            /**
             * 上述是自定义方法：用于往android shell中写入命令
             * 也可以使用一下语句；但我没有执行成功
             * Runtime.getRuntime().exec("/system/xbin/su/,"-c","setprop service.adb.tcp.port 5555");
             *Runtime.getRuntime().exec("/system/xbin/su/,"-c","stop adbd");
             * Runtime.getRuntime().exec("/system/xbin/su/,"-c","start adbd");
             */
            isConn = true; //判断adb是否已开启
        }
        LogUtil.e("return :" + result + " :isConn" + isConn);
        return result;
    }
    
    
    
//往android shell写入命令的方法
/**
     * 在su文件中写入命令
     * 这里一定要注意s[i]后面的“\n”是不可缺少的，
     * 由于DataOutputStream这个接口并不能直接操作底层shell，
     * 所以需要"\n"来标志一条命令的结束。
     *
     * @param s
     */
    public ADButils execShell(String... s) {
        //权限设置
        try {
            Process su = Runtime.getRuntime().exec("su");
            mProcess = su;
            //获取输出流
            DataOutputStream dos = new DataOutputStream(su.getOutputStream());
            //写入命令
            for (int i = 0; i < s.length; i++) {
                dos.writeBytes(s[i] + "\n");
            }
            dos.writeBytes("exit\n");
            //提交命令
            dos.flush();
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
```

3.  在pc端的执行连接命令
```
// 来到adb.exe 的目录下（在sdk\platform-tools; sdk为android开发环境sdk包）
cd D:\Prigram_Files\Android\sdk1\platform-tools

// 开始连接adb； 断开连接adb disconnect ip地址
adb connect ip地址
```