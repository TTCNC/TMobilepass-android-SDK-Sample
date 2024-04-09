package kr.co.ttcnc.ucsdk.dccctrllib.util;

import android.util.Log;

/**
 * Created by parkjeongho on 2023-12-06 오전 10:18
 */
public class TLog {
    public static boolean LOG_DISPLAY = false;

    public static void d(String tag, String msg) {
        if (LOG_DISPLAY) Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (LOG_DISPLAY) Log.e(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (LOG_DISPLAY) Log.i(tag, msg);
    }

    public static void d(String tag, String msg, boolean isDisplay) {
        if (LOG_DISPLAY) {
            Log.d(tag, msg);
        } else {
            if(isDisplay) Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg, boolean isDisplay) {
        if (LOG_DISPLAY) {
            Log.e(tag, msg);
        } else {
            if(isDisplay) Log.e(tag, msg);
        }
    }

    public static void i(String tag, String msg, boolean isDisplay) {
        if (LOG_DISPLAY) {
            Log.i(tag, msg);
        } else {
            if(isDisplay) Log.i(tag, msg);
        }
    }
}
