package com.example.h_dj.adbwifidemo.utils;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created by H_DJ on 2017/5/1.
 * <p>
 * <p>
 * 问题： 使用Runtime.getRuntime().exec(new String[]{"/system/xbin/su","-c","setprop service.adb.tcp.port 5555"}); 没有反应
 */

public class ADButils {

    private Context mContext;
    private static ADButils mADButils = null;
    private boolean mHaveRoot = false;
    public static boolean isConn = false;
    private Process mProcess;

    private ADButils() {
    }

    /**
     * 单例模式
     *
     * @return
     */
    public static ADButils getInstance() {
        if (mADButils == null) {
            synchronized (ADButils.class) {
                if (mADButils == null) {
                    mADButils = new ADButils();
                }
            }
        }
        return mADButils;
    }


    public ADButils with(Context context) {
        mContext = context;
        return this;
    }

    /**
     * 获取ip地址
     */
    public String getIpAddress() {
        Enumeration<NetworkInterface> en = null;
        try {
            for (en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                LogUtil.e("en:" + en.toString());
                NetworkInterface intf = en.nextElement();
                LogUtil.e("intf:" + intf.toString());

                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    LogUtil.e(enumIpAddr.toString());
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    LogUtil.e(inetAddress.toString());

                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 把32整型ip地址
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

    /**
     * 获取判断root权限
     *
     * @return
     */
    public boolean havaRoot() {
        if (!mHaveRoot) {
            int ret = execRootCmd("echo test");
            if (ret != -1) {
                LogUtil.e("已经获得root");
                mHaveRoot = true;
            } else {
                LogUtil.e("还没有root");
            }
        } else {
            LogUtil.e("已经获得root");
        }
        return mHaveRoot;
    }

    /**
     * 执行命令但不关注结果输出
     *
     * @param cmd
     * @return
     */
    private int execRootCmd(String cmd) {
        int result = -1;
        DataOutputStream dos = null;
        try {
            Process su = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(su.getOutputStream());

            LogUtil.e(cmd);

            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();

            su.waitFor();
            result = su.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return result;
    }

    /**
     * // 执行命令并且输出结果
     *
     * @param cmd
     * @return
     */
    private String execRootCmdSlient(String cmd) {
        DataOutputStream dos = null;
        DataInputStream dis = null;
        String result = null;
        try {
            Process su = Runtime.getRuntime().exec("su");// 经过Root处理的android系统即有su命令
            dos = new DataOutputStream(su.getOutputStream());
            dis = new DataInputStream(su.getInputStream());
            LogUtil.e(cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line = null;
            while ((line = dis.readLine()) != null) {
                LogUtil.e("result:" + line);
                result += line;
            }
            su.waitFor(); //暂停当前线程

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }

                if (dis != null) {
                    dis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return result;
    }


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


    /**
     * 获取执行的进程
     *
     * @return
     */
    public Process getProcess() {
        return mProcess;
    }

    /**
     * 关闭adb
     */
    public void closeAdb() {
        LogUtil.e("ip:" + getLocalIpAddress());
        execShell(new String[]{"setprop service.adb.tcp.port -1", "stop adbd"});
        isConn = false;
    }

    /**
     * 重启adb
     */
    public void restartAdb() {
        Process process = execShell(new String[]{"adb kill-server", "adb start-server"}).getProcess();
        if (process == null) {
            Toast.makeText(mContext, "重启adb失败", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "重启adb成功", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 根据包卸载apk
     *
     * @param packageName
     */
    public void uninstallApk(String packageName) {
        try {
            Process exec = execShell("pm uninstall " + packageName).getProcess();
            if (exec == null) {
                Toast.makeText(mContext, "app 卸载失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "app 卸载成功", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 杀死app
     *
     * @param packageName
     */
    public void killApp(String packageName) {
        Process exec = execShell("am force-stop " + packageName).getProcess();
        if (exec == null) {
            Toast.makeText(mContext, "app 关闭失败", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "app 关闭成功", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 通过Android底层实现关闭当前进程
     */
    public void killProcess() {
        int myPid = android.os.Process.myPid();
        if (myPid != 0) {
            System.exit(0);//推出程序
            android.os.Process.killProcess(myPid); //关闭进程
        }
    }

    /**
     * 启动程序
     *
     * @param packageName
     */
    public void startApp(String packageName) {
        try {
            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
            mContext.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(mContext, "没有此程序", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 清楚缓存
     *
     * @param packageName
     */
    public void clearAppCache(String packageName) {
        Process exec = execShell("pm clear" + packageName + " HERE").getProcess();
        if (exec == null) {
            Toast.makeText(mContext, "app 清楚缓存失败", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "app 清楚缓存成功", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 重启手机
     */
    public void rebootPhone() {
        Process exec = execShell("reboot").getProcess();
        if (exec == null) {
            Toast.makeText(mContext, "重启失败", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "重启成功", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 关闭手机
     */
    public void closePhone() {
        Process exec = execShell("reboot -p").getProcess();
        if (exec == null) {
            Toast.makeText(mContext, "重启失败", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "重启成功", Toast.LENGTH_SHORT).show();
        }
    }

}
