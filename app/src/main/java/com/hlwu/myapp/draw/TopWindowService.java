package com.hlwu.myapp.draw;

import java.util.ArrayList;
import java.util.List;

import com.net.margaritov.preference.colorpicker.ColorPickerDialog;

import com.hlwu.myapp.ui.MainActivity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;

public class TopWindowService extends Service
{
    public static final String OPERATION = "operation";
    public static final int OPERATION_SHOW_OR_NOT = 100;
    public static final int OPERATION_NOT = 101;

    private static final int HANDLE_CHECK_ACTIVITY = 200;
    private static final int HANDLE_REMOVE_ACTIVITY = 201;
    public static final String PREFERENCES = "canvasTest";
    public static final String SP_URI = "com.example.canvasTest.";

    private boolean isAdded = false;
    private static WindowManager wm;
    private static WindowManager.LayoutParams params;
    private Button btn_floatView;

    private MainActivity mMainActivity = null;

    private boolean mAlphaSliderEnabled = false;
    private boolean mHexValueEnabled = false;

    private boolean hasMoved = false;

    final int SELECT_IMAGE = 1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createFloatView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d("flaggg", "onStart");

        int operation = intent != null ? intent.getIntExtra(OPERATION, OPERATION_SHOW_OR_NOT) : OPERATION_SHOW_OR_NOT;
        switch (operation)
        {
            case OPERATION_SHOW_OR_NOT:
                mHandler.removeMessages(HANDLE_CHECK_ACTIVITY);
                mHandler.sendEmptyMessage(HANDLE_CHECK_ACTIVITY);
                break;
            case OPERATION_NOT:
                mHandler.removeMessages(HANDLE_REMOVE_ACTIVITY);
                mHandler.sendEmptyMessage(HANDLE_REMOVE_ACTIVITY);
        }
    }

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case HANDLE_CHECK_ACTIVITY:
                    try {
                        if(wm != null) {
                            if (!isAdded) {
                                Log.d("flaggg", "add view");
                                wm.addView(btn_floatView, params);
                                isAdded = true;
                            } else {
                                Log.d("flaggg", "remove view");
                                wm.removeView(btn_floatView);
                                isAdded = false;
                            }
                        }
                    } catch (Exception e) {
                        Log.d("flaggg", "can't add float button, e: " + e.getStackTrace());
                    }
                    break;
                case HANDLE_REMOVE_ACTIVITY:
                    if(wm != null && isAdded) {
                        Log.d("flaggg", "HANDLE_REMOVE_ACTIVITY");
                        wm.removeView(btn_floatView);
                        isAdded = false;
                    }
            }
        }
    };

    private void createFloatView() {
        Log.d("flaggg", "createFloatView");
        btn_floatView = new Button(getApplicationContext());
        btn_floatView.setBackgroundResource(android.R.drawable.ic_menu_help);
        btn_floatView.setOnTouchListener(new OnTouchListener()
        {
            int lastX, lastY;
            int paramX, paramY;

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("flaggg", "topwindow action_down");
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        paramX = params.x;
                        paramY = params.y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("flaggg", "topwindow aciont_move");
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        params.x = paramX + dx;
                        params.y = paramY + dy;
                        wm.updateViewLayout(btn_floatView, params);
                        hasMoved = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d("flaggg", "topwindow action_up, hasMoved: " + hasMoved);
                        if(!hasMoved) {
                            sendBroadcastAfterClicked();
                        }
                        hasMoved = false;
                }
                return true;
            }
        });

        wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0+
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.width = 80;
        params.height = 80;
    }

    public void setMainActivityInstance(MainActivity ma) {
        if(ma != null)
            mMainActivity = ma;
    }

    public void sendBroadcastAfterClicked() {
        Log.d("flaggg", "sendBroadcast after clicked");
        Intent intent = new Intent();
        intent.setAction("com.example.canvastest.click");
        sendBroadcast(intent);
    }
}
