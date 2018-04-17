package com.example.lydia.wechatautoaddfriends;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * service to add float view to window
 * Created by loy.ouyang on 2017/11/15.
 */

@SuppressLint("Registered")
public class AddFriendsFloatViewService extends Service implements View.OnClickListener {
    private final static String TAG = "AddFriendsFloatViewService";
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams wmParams;
    ///TPV loy.ouyang: float view
    private View mFloatView;

    private EditText mEditText;

    ///TPV loy.ouyang: touch point in float view
    private float mTouchStartX;
    private float mTouchStartY;

    ///TPV loy.ouyang: real position of float view
    private float mX;
    private float mY;


    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        addFloatView();
        return START_NOT_STICKY;
    }

    /**
     * Tpv loy.ouyang: init float view
     */
    private void init() {

        ///TPV loy.ouyang: init float view
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        if (inflater == null) {
            return;
        }
        mFloatView = inflater.inflate(R.layout.float_view, null);
        Button sendBtn = mFloatView.findViewById(R.id.send);
        sendBtn.setOnClickListener(this);

        Button settingsBtn = mFloatView.findViewById(R.id.settings);
        settingsBtn.setOnClickListener(this);

        mEditText = mFloatView.findViewById(R.id.message);

        ///TPV loy.ouyang: init window params
        if (Build.VERSION.SDK_INT >= 26) {
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        wmParams.gravity = Gravity.CENTER;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        DisplayMetrics metrics = new DisplayMetrics();
        ///TPV loy.ouyang: set default position
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
    }

    /**
     * Tpv loy.ouyang: add float view to window
     */
    private void addFloatView() {
        ///TPV loy.ouyang: avoid add float view twice
        if (mFloatView.getParent() != null)
            mWindowManager.removeView(mFloatView);
        mWindowManager.addView(mFloatView, wmParams);
        initWindowSize();
        mFloatView.setOnTouchListener(mOnTouch);
    }


    public void removeFloatView() {
        if (mFloatView != null)
            mWindowManager.removeView(mFloatView);
    }

    /**
     * init the window size when show float view
     */
    private void initWindowSize() {
        WindowManager wm = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        if (wm != null){
            wm.getDefaultDisplay().getMetrics(outMetrics);
        }
        int screenW = outMetrics.widthPixels;
        int screenH = outMetrics.heightPixels;
        if (getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {//landscape
            screenW = outMetrics.heightPixels;
            screenH = outMetrics.widthPixels;
        }
        double zoom = 0.75;
        wmParams.width = (int) (screenW * zoom);
        wmParams.height = (int) (screenH * zoom);

        mWindowManager.updateViewLayout(mFloatView, wmParams);
    }

    private View.OnTouchListener mOnTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mX = wmParams.x;
                    mY = wmParams.y;
                    mTouchStartX = event.getRawX();
                    mTouchStartY = event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    wmParams.x = (int) (event.getRawX() - mTouchStartX + mX);
                    wmParams.y = (int) (event.getRawY() - mTouchStartY + mY);

                    ///TPV loy.ouyang: limit the max vertical and horizon move distance
                    DisplayMetrics metrics = new DisplayMetrics();
                    mWindowManager.getDefaultDisplay().getMetrics(metrics);
                    int maxX = metrics.widthPixels;
                    int minX = -mFloatView.getWidth();
                    int maxY = metrics.heightPixels - 3 * getNavigationBarHeight();///TPV loy.ouyang: solve problem of can not move float view when under navigation bar
                    int minY = -mFloatView.getHeight();
                    if (wmParams.x < minX) {
                        wmParams.x = minX;
                    }
                    if (wmParams.x > maxX) {
                        wmParams.x = maxX;
                    }
                    if (wmParams.y < minY) {
                        wmParams.y = minY;
                    }
                    if (wmParams.y > maxY) {
                        wmParams.y = maxY;
                    }
                    ///TPV---------------------------------------
                    mWindowManager.updateViewLayout(mFloatView, wmParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    /**
     * Tpv loy.ouyang: get virtual navigation height
     */
    private int getNavigationBarHeight() {
        Resources resources = this.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.settings:
                openSettings();
                break;
            case R.id.send:
                sendBroadcastToAddFriendsWithMessage();
                break;
            default:
                break;
        }
    }

    private void openSettings(){
        try {
            //打开系统设置中辅助功能
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(getApplicationContext(), "找到添加好友服务，然后开启服务即可", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendBroadcastToAddFriendsWithMessage(){
        Intent intent = new Intent();
        intent.setAction("android.loy.lydia.start");
        intent.putExtra(Utils.MESSAGE_KEY, mEditText.getText().toString());
        sendBroadcast(intent);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
