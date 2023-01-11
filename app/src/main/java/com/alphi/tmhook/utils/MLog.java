package com.alphi.tmhook.utils;

/*
    author: alphi
    createDate: 2022/11/8
*/

import static android.util.Log.getStackTraceString;

import android.util.Log;

import de.robv.android.xposed.XposedBridge;

public final class MLog {
    /**
     * Send a DEBUG log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int v(String tag, String msg) {
        printXposedLog(tag, msg, null);
        return Log.v(tag, msg);
    }

    /**
     * Send a DEBUG log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int v(String tag, String msg, Throwable tr) {
        printXposedLog(tag, msg, tr);
        return Log.v(tag, msg, tr);
    }


    /**
     * Send a DEBUG log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int d(String tag, String msg) {
        printXposedLog(tag, msg, null);
        return Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        printXposedLog(tag, msg, tr);
        return Log.d(tag, msg, tr);
    }

    /**
     * Send an INFO log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int i(String tag, String msg) {
        printXposedLog(tag, msg, null);
        return Log.i(tag, msg);
    }

    /**
     * Send a INFO log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int i(String tag, String msg, Throwable tr) {
        printXposedLog(tag, msg, tr);
        return Log.i(tag, msg, tr);
    }

    /**
     * Send a WARN log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int w(String tag, String msg) {
        printXposedLog(tag, msg, null);
        return Log.w(tag, msg);
    }

    /**
     * Send a WARN log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int w(String tag, String msg, Throwable tr) {
        printXposedLog(tag, msg, tr);
        return Log.w(tag, msg, tr);
    }

    /*
     * Send a WARN log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param tr An exception to log
     */
    public static int w(String tag, Throwable tr) {
        printXposedLog(tag + "!", null, tr);
        return Log.w(tag, getStackTraceString(tr));
    }

    /**
     * Send an ERROR log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int e(String tag, String msg) {
        printXposedLog(tag, msg, null);
        return Log.e(tag, msg);
    }

    /**
     * Send a ERROR log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int e(String tag, String msg, Throwable tr) {
        printXposedLog(tag + "!", msg, tr);
        return Log.e(tag, msg, tr);
    }

    private static void printXposedLog(String tag, String msg, Throwable tr) {
        XposedBridge.log(tag + ": " + msg + '\n' + getStackTraceString(tr));
    }
}
