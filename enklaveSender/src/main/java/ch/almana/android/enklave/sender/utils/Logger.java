package ch.almana.android.enklave.sender.utils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;

public class Logger {
    private static final String TAG = "EnklaveSender";
    private static final String STACKTRACE_TAG = TAG + "StracktraceLog";

    public final static boolean DEBUG = false;

    public static void e(final String msg, final Throwable t) {
        try {
            Log.e(TAG, msg, t);
        } catch (Throwable t1) {
            // no nothing
        }
    }

    public static void w(final String msg, final Throwable t) {
        Log.w(TAG, msg, t);
    }

    public static void d(final String msg, final Throwable t) {
        Log.d(TAG, msg, t);
    }

    public static void v(final String msg, final Throwable t) {
        Log.v(TAG, msg, t);
    }

    public static void i(final String msg, final Throwable t) {
        Log.i(TAG, msg, t);
    }

    public static void e(final String msg) {
        Log.e(TAG, msg);
    }

    public static void w(final String msg) {
        Log.w(TAG, msg);
    }

    public static void d(final String msg) {
        Log.d(TAG, msg);
    }

    public static void v(final String msg) {
        Log.v(TAG, msg);
    }

    public static void i(final String msg) {
        Log.i(TAG, msg);
    }

    public static void logStacktrace(final String msg) {
        if (!Logger.DEBUG) {
            logToFile(msg, new Exception());
        }
    }

    public static void logStacktrace(final String msg, final Throwable e) {
        if (!Logger.DEBUG) {
            logToFile(msg, e);
        }
    }

    public static void logToFile(final String msg, final Throwable e) {
        if (Logger.DEBUG) {
            if (e != null) {
                Log.d(STACKTRACE_TAG, msg, e);
            } else {
                Log.d(TAG, msg, e);
            }
            try {
                Writer w = new FileWriter("/mnt/sdcard/cputuner.log", true);
                w.write("**************  Stacktrace ***********************\n");
                w.write((new Date()).toString());
                w.write("\n");
                w.write(msg);
                if (e != null) {
                    w.write("\n");
                    e.printStackTrace(new PrintWriter(w));
                }
                w.write("**************************************************\n");
                w.flush();
                w.close();
            } catch (IOException e1) {
                Logger.w("Cannot write stacktrage log", e1);
            }
        }
    }

    public static void logIntentExtras(final Intent intent) {
        if (DEBUG) {
            try {
                if (intent == null || intent.getExtras() == null) {
                    return;
                }
                Bundle extras = intent.getExtras();
                String action = intent.getAction();
                StringBuilder sb = new StringBuilder();
                sb.append("action: ").append(action);
                for (String key : extras.keySet()) {
                    sb.append(" extra: ").append(key).append(" -> ");
                    sb.append(extras.get(key)).append("\n");
                }
                logToFile(sb.toString(), null);

            } catch (Exception e) {
                //
            }
        }
    }
}
