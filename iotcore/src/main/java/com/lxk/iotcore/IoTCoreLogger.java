package com.lxk.iotcore;

import android.util.Log;

/**
 * @author https://github.com/103style
 * @date 2020/4/14 16:57
 * <p>
 * iotcore 相关的日志
 */
public class IoTCoreLogger {

    private static final String TAG = IoTCoreManager.class.getSimpleName();

    public static boolean OPEN_LOG = true;

    public static void i(String text) {
        if (OPEN_LOG) {
            Log.i(TAG, text);
        }
    }

    public static void i(Object... args) {
        if (OPEN_LOG) {
            StringBuilder builder = new StringBuilder();
            for (Object arg : args) {
                builder.append(arg);
            }
            Log.i(TAG, builder.toString());
        }
    }

    public static void e(String text) {
        if (OPEN_LOG) {
            Log.e(TAG, text);
        }
    }

    public static void e(Throwable throwable) {
        if (OPEN_LOG) {
            if (throwable != null) {
                Log.e(TAG, throwable.toString());
                throwable.printStackTrace();
            } else {
                Log.e(TAG, "throwable is null");
            }
        }
    }
}
