package com.example.lydia.wechatautoaddfriends;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lydia on 2018/4/13.
 */

@SuppressLint("Registered")
public class AutoAddFriendsService extends AccessibilityService {
    private static final String TAG = "AutoAddFriendsService";
    private static final String WECHAT_PACKAGENAME = "com.tencent.mm";//微信包名
    private static final String REQUST_ADD_FRIEND_TEXT_KEY = "请求添加你为朋友"; //添加好友消息关键字
    private static final String CONMUNICATION_TEXT_KEY = "通讯录"; //添加好友关键字
    private static final String NEW_FRIENDS_TEXT_KEY = "新的朋友"; //添加好友关键字
    private static final String ACCEPT_ADD_FRIEND_TEXT_KEY = "接受"; //添加好友关键字
    private static final String FINISH_ADD_FRIEND_TEXT_KEY = "完成"; //完成添加好友关键字
    private static final String SEND_MESSAGE_TEXT_KEY = "发消息"; //发送消息关键字
    private static final String SEND_BUTTON_TEXT_KEY = "发送"; //发送按钮关键字

//    private static String MESSAGE = "你好，我是丫丫鞋业，欢迎加入。如果有需要的话，可以去我朋友圈看看，各式高仿名牌，质量保证，价格实惠。";
    private static String MESSAGE = "";
    private AccessibilityNodeInfo editText;

    //锁屏、唤醒相关
    private KeyguardManager km;
    private KeyguardManager.KeyguardLock kl;
    private PowerManager pm;
    private PowerManager.WakeLock wl = null;
    private boolean enableKeyguard = false;
    private boolean enablePower = false;



    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        int eventType = event.getEventType();
        Log.e(TAG, "==============Start==================== type: " + eventType);
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                Log.e("ouyang", "window type changed");
//                addFriends();
                break;
            default:
                break;
        }
    }

    @Override
    public void onInterrupt() {
        unregisterReceiver(receiver);
        Toast.makeText(this, "添加好友服务中断", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.loy.lydia.start");
        registerReceiver(receiver, filter);
        //获取电源管理器对象
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //得到键盘锁管理器对象
        km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        kl = km.newKeyguardLock("unLock");

        Toast.makeText(this, "添加好友服务连接成功", Toast.LENGTH_SHORT).show();

    }

    public static void setMessage(String message) {
        MESSAGE = message;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                addFriends();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    int i = 0;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void addFriends() throws InterruptedException {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> comunicatetList = nodeInfo.findAccessibilityNodeInfosByText(CONMUNICATION_TEXT_KEY);
        if (!comunicatetList.isEmpty()) {
            comunicatetList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            Thread.sleep(300);
        }
        List<AccessibilityNodeInfo> newFriendsList = nodeInfo.findAccessibilityNodeInfosByText(NEW_FRIENDS_TEXT_KEY);
        if (!newFriendsList.isEmpty()) {
            newFriendsList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            Thread.sleep(300);
        }
        List<AccessibilityNodeInfo> acceptList = nodeInfo.findAccessibilityNodeInfosByText(ACCEPT_ADD_FRIEND_TEXT_KEY);
        if (!acceptList.isEmpty()) {
            if (i > 5) {
                return;
            }
            acceptList.get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            Thread.sleep(300);
            i++;
        }
        List<AccessibilityNodeInfo> sendMessageList = nodeInfo.findAccessibilityNodeInfosByText(SEND_MESSAGE_TEXT_KEY);
        if (!sendMessageList.isEmpty()) {
            sendMessageList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            Thread.sleep(300);
        }
        findEditTextByClassName(nodeInfo, "android.widget.EditText");
        if (editText != null) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null) {
                return;
            }
            ClipData clip = ClipData.newPlainText("message", MESSAGE);
            clipboard.setPrimaryClip(clip);
            editText.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            editText.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            List<AccessibilityNodeInfo> sendButtonList = nodeInfo.findAccessibilityNodeInfosByText(SEND_BUTTON_TEXT_KEY);
            if (!sendButtonList.isEmpty()) {
                sendButtonList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Thread.sleep(300);
                pressBackButton();
            }
        }


//        List<AccessibilityNodeInfo> finishList = nodeInfo.findAccessibilityNodeInfosByText(FINISH_ADD_FRIEND_TEXT_KEY);
//        if (!finishList.isEmpty()) {
//            finishList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//        }

    }


    //通过组件名递归查找编辑框
    private void findEditTextByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if (className.equals(nodeInfo.getClassName().toString())) {
            editText = nodeInfo;
            return;
        }
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            findEditTextByClassName(nodeInfo.getChild(i), className);
        }
    }

    /**
     * 唤醒和解锁相关
     */
    private void wakeAndUnlock() {

        if (enablePower) {
            wl.release();
        }

        if (enableKeyguard) {
            kl.reenableKeyguard();
        }

        if (!pm.isScreenOn()) {
            //获取电源管理器对象
            enablePower = true;
            wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "bright");
            //点亮屏幕
            wl.acquire();
        }
        if (km.inKeyguardRestrictedInputMode()) {
            //解锁
            enableKeyguard = true;
            kl.disableKeyguard();
            Log.i("demo", "解锁");
        }
    }

    /**
     * 模拟back按键
     */
    private void pressBackButton(){
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
