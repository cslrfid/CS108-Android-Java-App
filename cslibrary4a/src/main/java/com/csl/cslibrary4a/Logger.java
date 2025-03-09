package com.csl.cslibrary4a;

import static com.csl.cslibrary4a.Utility.getReferencedCurrentTimeMs;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;

/**
 * Lazy and configurable logger for the library
 */
public class Logger {
    private static Handler mHandler = new Handler();

    public static boolean LOG_VIEW_ENABLED = false;
    public static boolean LOG_APDATA = false;
    public static boolean LOG_PKDATA = false;
    public static boolean LOG_BTDATA = false;
    public static boolean LOG_FMDATA = false;
    public static boolean LOG_BTOP = false;
    public static boolean LOG_INVCFG = false;
    public static boolean LOG_CONNECT = false;
    public static boolean LOG_SCAN = false;
    public static boolean LOG_SELECT = false;
    public static boolean LOG_COMPACT = false;

    static TextView mLogView;

    public final static LogLevel LEVEL = LogLevel.WARN;

    public static void trace(String pattern, Object... args) {
        if (LEVEL.ordinal() <= LogLevel.TRACE.ordinal()) {
            Log.v(getTag(), fillPattern(pattern, args));
        }
    }

    public static void debug(String pattern, Object... args) {
        if (LEVEL.ordinal() <= LogLevel.DEBUG.ordinal()) {
            Log.d(getTag(), fillPattern(pattern, args));
        }
    }

    public static void info(String pattern, Object... args) {
        if (LEVEL.ordinal() <= LogLevel.INFO.ordinal()) {
            Log.i(getTag(), fillPattern(pattern, args));
        }
    }

    public static void warn(String pattern, Object... args) {
        if (LEVEL.ordinal() <= LogLevel.WARN.ordinal()) {
            Log.w(getTag(), fillPattern(pattern, args));
        }
    }

    public static void error(String pattern, Object... args) {
        if (LEVEL.ordinal() <= LogLevel.WARN.ordinal()) {
            Log.e(getTag(), fillPattern(pattern, args));
        }
    }

    public static void apData(String pattern, Object... args) {
        if (LOG_APDATA) {
            Log.d(getTag(), fillPattern(pattern, args));
        }
    }

    public static void pkData(String pattern, Object... args) {
        if (LOG_PKDATA) {
            Log.d(getTag(), fillPattern(pattern, args));
        }
    }

    public static void btdData(String pattern, Object... args) {
        if (LOG_BTDATA) {
            Log.d(getTag(), fillPattern(pattern, args));
        }
    }

    public static void fmdData(String pattern, Object... args) {
        if (LOG_FMDATA) {
            Log.d(getTag(), fillPattern(pattern, args));
        }
    }

    public static void connect(String pattern, Object... args) {
        if (LOG_CONNECT) {
            Log.d(getTag(), fillPattern(pattern, args));
        }
    }

    public static void scan(String pattern, Object... args) {
        if (LOG_SCAN) {
            Log.d(getTag(), fillPattern(pattern, args));
        }
    }

    public static void select(String pattern, Object... args) {
        if (LOG_SELECT) {
            Log.d(getTag(), fillPattern(pattern, args));
        }
    }

    public static void compact(String pattern, Object... args) {
        if (LOG_COMPACT) {
            Log.d(getTag(), fillPattern(pattern, args));
        }
    }

    public static void btop(String pattern, Object... args) {
        if (LOG_BTOP) {
            Log.d(getTag(), fillPattern(pattern, args));
        }
    }

    public static void invCfg(String pattern, Object... args) {
        if (LOG_INVCFG) {
            Log.d(getTag(), fillPattern(pattern, args));
        }
    }

    public static void runDebug(String pattern, Object... args) {
        mHandler.post(() -> debug(pattern, args));
    }

    /**
     * Tries to extract the caller method name from the stack trace to
     * determine customized log tag.
     * @return the log tag from the stacktrace or the default tag
     */
    private static String getTag() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        boolean foundMe = false;
        for (StackTraceElement e : stacktrace) {
            String methodName = e.getMethodName();
            if (methodName.contains("appendToLog")) {
                foundMe = true;
            } else if (foundMe && !methodName.startsWith("access$")) {
                return String.format(Locale.US, "%s", methodName);
            }
        }
        return "CSLibrary4a";
    }

    /**
     * Replaces all {} from the pattern with string representation of
     * the parameters.
     * @param pattern the string to replace {} by args in
     * @param args the objects to replace placeholders with
     * @return the filled pattern
     */
    private static String fillPattern(String pattern, Object... args) {
        int i = 0;
        int pos = pattern.indexOf("{}");
        while (pos != -1 && i < args.length) {
            pattern = pattern.substring(0, pos) + args[i] + pattern.substring(pos + 2);
            i++;
            pos = pattern.indexOf("{}");
        }
        return pattern;
    }

    public static LoggerMessage toLogView(String pattern, Object... args) {
        if (LOG_VIEW_ENABLED && Looper.myLooper() == Looper.getMainLooper() && mLogView != null) {
            String message = fillPattern(pattern, args);
            mLogView.append("\n" + getReferencedCurrentTimeMs() + "." + message);
            return new LoggerMessage(message);
        }
        return LoggerMessage.EMPTY;
    }

    static class LoggerMessage {
        static final LoggerMessage EMPTY = new LoggerMessage(null);
        private final String value;
        LoggerMessage(String value) { this.value = value; }

        public void trace() { if (value != null) Logger.trace(value); }
        public void debug() { if (value != null) Logger.debug(value); }
        public void info() { if (value != null) Logger.info(value); }
        public void warn() { if (value != null) Logger.warn(value); }
        public void error() { if (value != null) Logger.error(value); }
    }

    /** Different level of logs to filter messages */
    public enum LogLevel { TRACE, DEBUG, INFO, WARN, ERROR }
}
