package com.simple.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;

public class PackageUtils {

    private static String packageName;

    public static String getPackageName(Context context) {
        if (packageName == null) {
            if (null!=context) {
                packageName = context.getPackageName();
            } else {
                try {
                    final Class<?> activityThreadClass = PackageUtils.class.getClassLoader().loadClass("android.app.ActivityThread");
                    final Method currentPackageName = activityThreadClass.getDeclaredMethod("currentPackageName");
                    packageName = (String) currentPackageName.invoke(null);
                } catch (final Exception e) {
                   e.printStackTrace();
                }
            }
        }
        return packageName;
    }

    public static ApplicationInfo getAppInfo(Context context) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appInfo;
    }


    /**
     * 判断应用是否已经启动
     *
     * @param context     一个context
     * @param packageName 要判断应用的包名
     * @return boolean
     */
    public static boolean isAppAlive(Context context, String packageName) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos
                = activityManager.getRunningAppProcesses();
        for (int i = 0; i < processInfos.size(); i++) {
            if (processInfos.get(i).processName.equals(packageName)) {
                Log.i("NotificationLaunch",
                        String.format("the %s is notRunning, isAppAlive return true", packageName));
                return true;
            }
        }
        Log.i("NotificationLaunch",
                String.format("the %s is not notRunning, isAppAlive return false", packageName));
        return false;
    }


    /**
     * 获得string mataData数据
     *
     * @param key
     * @return
     */
    public static String getStringMataData(Context context, String key) {
        ApplicationInfo appInfo = getAppInfo(context);
        String value = null;
        if (null != appInfo) {
            value = appInfo.metaData.getString(key);
        }
        return value;
    }

    /**
     * 获得boolean mataData数据
     *
     * @param key
     * @return
     */
    public static boolean getBooleanMataData(Context context, String key) {
        ApplicationInfo appInfo = getAppInfo(context);
        boolean value = false;
        if (null != appInfo) {
            try {
                value = appInfo.metaData.getBoolean(key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    /**
     * 获得软件版本
     *
     * @return
     */
    public static String getAppVersion(Context context) {
        String appVersion = "";
        try {
            appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appVersion;
    }

    /**
     * 获得软件版本
     *
     * @return
     */
    public static int getAppVersionCode(Context context) {
        int appVersionCode = -1;
        try {
            appVersionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appVersionCode;
    }

    /**
     * 获得软件名称
     *
     * @return
     */
    public static String getApplicationName(Context context) {
        String appName = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(context), 0);
            CharSequence label = packageManager.getApplicationLabel(applicationInfo);
            if (!TextUtils.isEmpty(label)) {
                appName = label.toString();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }




    /**
     * 检查某个应用是否安装
     *
     * @param packageName
     */
    public static boolean appIsInstall(Context context, String packageName) {
        boolean install = false;
        if (!TextUtils.isEmpty(packageName) && null != context) {
            try {
                install = (null != context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES));
            } catch (PackageManager.NameNotFoundException e) {
                install = false;
            }
        }
        return install;
    }

    /**
     * 获得启动activity对象
     *
     * @return
     */
    public static String getLancherActivity(Context context) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(getPackageName(context));
        ComponentName component = launchIntent.getComponent();
        return component.getClassName();
    }


    /**
     * 启用设置界面
     *
     * @param context
     */
    public static void startSetting(Context context) {
        if (null != context) {
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                context.startActivity(intent);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 判断是否主进程
     *
     * @return
     */
    public static boolean isMainProcess(Context context) {
        boolean result=false;
        String mainProcessName = context.getPackageName();
        if (mainProcessName.equals(getProcessName(context, android.os.Process.myPid()))) {
            result= true;
        }
        return result;
    }

    /**
     * 判断是否是Launcher进程
     *
     * @return
     */
    public static boolean isLauncherProcess(Context context) {
        boolean result=false;
        String launcherProcessName = context.getPackageName()+":launcher";
        if (launcherProcessName.equals(getProcessName(context, android.os.Process.myPid()))) {
            result= true;
        }
        return result;
    }

    public static String getProcessName(Context cxt, int pid) {
        String result=null;
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (!runningApps.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
                if (procInfo.pid == pid) {
                    result=procInfo.processName;
                    break;
                }
            }
        }
        return result;
    }
}
