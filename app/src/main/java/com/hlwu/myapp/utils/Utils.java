package com.hlwu.myapp.utils;

import android.util.Log;

/**
 * Created by hlwu on 1/22/18.
 */

public class Utils {

    private static final String TAG = "flaggg_Utils";


    public static void printSimpleCallStack(String msg) {
        StackTraceElement[] elements = new Exception().getStackTrace();
        StringBuilder sb = new StringBuilder("printSimpleCallStack: "+msg);
        sb.append(" IN \n");
        int maxIndex = elements.length-1;
        for (int i = 1; i <= maxIndex; i++) {
            StackTraceElement e = elements[i];
            String className = e.getClassName();
            String simpleName = className.substring(className.lastIndexOf(".")+1, className.length()-1);
            sb.append(simpleName).append(".").append(e.getMethodName()).append("()").append(" line "+e.getLineNumber());
            if (maxIndex != i) {
                sb.append(" << ");
            }
        }
        Log.d(TAG, sb.toString());
    }
}
