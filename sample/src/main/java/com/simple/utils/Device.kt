package com.simple.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import android.text.TextUtils

/**
 * 获取手机基础信息，如IMEI等
 *
 * @author user
 * @since 1.0
 */
object Device {

    private var imei: String? = null

    /**
     * 获取屏幕宽度
     */
    fun getScreenWidth(context: Context): Int {
        return context.applicationContext.resources.displayMetrics.widthPixels
    }

    /**
     * 获取屏幕高度
     */
    fun getScreenHeight(context: Context): Int {
        return context.applicationContext.resources.displayMetrics.heightPixels
    }

    val screenPattern: String
        get() = "1"

    /**
     * 获取手机IMEI号 需要权限
     */
    fun getICCID(context: Context): String {
        var iccid: String? = ""
        val manager =
            context.applicationContext.getSystemService(Activity.TELEPHONY_SERVICE) as TelephonyManager
        // check if has the permission
        if (PackageManager.PERMISSION_GRANTED == context.applicationContext.packageManager.checkPermission(
                Manifest.permission.READ_PHONE_STATE, context.applicationContext.packageName
            )
        ) {
            iccid = manager.simSerialNumber
        }

        return if (iccid == null || "" == iccid) "isFake" else iccid
    }

    /**
     * 获取手机Mac地址
     *
     * @param context
     * @return
     * @author user
     * @since 1.0
     */
    fun getLocalMacAddress(context: Context): String {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifi.connectionInfo ?: return ""
        return info.macAddress

    }

    /**
     * 是否已联网
     *
     * @param context
     * @return
     * @author user
     * @since 1.0
     */
    fun isOnline(context: Context): Boolean {
        val manager = context.getSystemService(Activity.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = manager.activeNetworkInfo
        return info != null && info.isConnected
    }

    /**
     * 获取版本名称
     */
    fun getVersionName(context: Context): String {
        try {
            val pi = context.applicationContext.packageManager
                .getPackageInfo(context.applicationContext.packageName, 0)
            return pi.versionName
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return "2.1"
    }

    /**
     * 获取版本号
     */
    fun getVersionCode(context: Context): Int {
        return try {
            val pi = context.applicationContext.packageManager
                .getPackageInfo(context.applicationContext.packageName, 0)
            pi.versionCode
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
            0
        }

    }

    /**
     * 返回一个供公共上行所需要的字符串
     *
     * @param context
     * @return
     */
    fun getDeviceID(context: Context): String {
        val mDeviceId: String
        val sharedpreferences = context.getSharedPreferences("bids", 0)
        var s = sharedpreferences.getString("i", null)

        if (s == null) {
            s = getICCID(context)
            val editor = sharedpreferences.edit()
            editor.putString("i", s)
            editor.apply()
        }
        var s1 = sharedpreferences.getString("a", null)
        if (s1 == null) {
            s1 = getAndroidId(context)
            val editor1 = sharedpreferences.edit()
            editor1.putString("a", s1)
            editor1.apply()
        }

        mDeviceId =
            Md5Tools.toMd5(
                StringBuilder().append("com.okay").append(s).append(s1).toString().toByteArray(),
                true
            )

        return mDeviceId
    }

    /**
     * 返回Android_ID
     *
     * @param context
     * @return
     */
    private fun getAndroidId(context: Context): String {
        var s = ""
        s = android.provider.Settings.Secure.getString(context.contentResolver, "android_id")
        if (TextUtils.isEmpty(s)) {
            s = ""
        }
        return s
    }

}
