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
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * Created by lydia on 2018/4/13.
 */

@SuppressLint("Registered")
public class AutoAddFriendsService extends AccessibilityService {
    private static final String TAG = "ouyang";
    private static final String WECHAT_PACKAGENAME = "com.tencent.mm";//微信包名
    private static final String ACCEPT_ADD_FRIEND_TEXT_KEY = "接受"; //添加好友关键字
    private static final String FINISH_ADD_FRIEND_TEXT_KEY = "完成"; //完成添加好友关键字
    private static final String SEND_MESSAGE_TEXT_KEY = "发消息"; //发送消息关键字
    private static final String SEND_BUTTON_TEXT_KEY = "发送"; //发送按钮关键字

    private static String MESSAGE = "你好，我是丫丫鞋业，欢迎加入。如果有需要的话，可以去我朋友圈看看，各式高仿名牌，质量保证，价格实惠。";
    private AccessibilityNodeInfo mFindView;

    //锁屏、唤醒相关
    private KeyguardManager km;
    private KeyguardManager.KeyguardLock kl;
    private PowerManager pm;
    private PowerManager.WakeLock wl = null;
    private boolean enableKeyguard = false;
    private boolean enablePower = false;

    AccessibilityNodeInfo nodeInfo;

    private static boolean ADD = false;


    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        int eventType = event.getEventType();
        Log.e(TAG, "==============Start==================== type: " + eventType);
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                nodeInfo = getRootInActiveWindow();
                if (nodeInfo == null || !ADD) {
                    return;
                }
                String windowClassName = event.getClassName().toString();
                Log.e(TAG, "window type changed: " + windowClassName);
                if ("com.tencent.mm.ui.LauncherUI".equals(windowClassName)) {
                    jumpToAddNewFriends();
                }
                if ("com.tencent.mm.plugin.subapp.ui.friend.FMessageConversationUI".equals(windowClassName)) {
                    acceptAddFriends();
                }

                if ("com.tencent.mm.plugin.profile.ui.SayHiWithSnsPermissionUI".equals(windowClassName)){
                    finishAddFriends();
                }

                if ("com.tencent.mm.plugin.profile.ui.ContactInfoUI".equals(windowClassName)) {
                    preessSendMessageButton();
                }

                if ("com.tencent.mm.ui.chatting.ChattingUI".equals(windowClassName)) {
                    sendMessageToFriends();
                }
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

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.loy.lydia.start".equals(action)){
                MESSAGE = (intent.getStringExtra(Utils.MESSAGE_KEY).isEmpty()) ? MESSAGE : intent.getStringExtra(Utils.MESSAGE_KEY);
                ADD = intent.getBooleanExtra(Utils.ADD_KEY, false);
                jumpToAddNewFriends();
            }
        }
    };

    /**
     * 进入添加朋友界面
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void jumpToAddNewFriends() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<AccessibilityNodeInfo> accessibilityNodeInfoList = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/j8");
                if (accessibilityNodeInfoList != null && accessibilityNodeInfoList.size() != 0) {
                    AccessibilityNodeInfo accessibilityNodeInfo = accessibilityNodeInfoList.get(0);
                    if (accessibilityNodeInfo != null) {
                        accessibilityNodeInfo = accessibilityNodeInfo.getChild(0);
                        if (accessibilityNodeInfo != null) {
                            accessibilityNodeInfo = accessibilityNodeInfo.getChild(0);
                            if (accessibilityNodeInfo != null) {
                                accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }
                    }
                }
            }
        }, 500);
    }

    /**
     * 点击接受按钮
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void acceptAddFriends() {
        List<AccessibilityNodeInfo> acceptList = nodeInfo.findAccessibilityNodeInfosByText(ACCEPT_ADD_FRIEND_TEXT_KEY);
        if (!acceptList.isEmpty()) {
            acceptList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    /**
     * 点击完成按钮，完成添加好友
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void finishAddFriends() {
        List<AccessibilityNodeInfo> finishList = nodeInfo.findAccessibilityNodeInfosByText(FINISH_ADD_FRIEND_TEXT_KEY);
        if (!finishList.isEmpty()) {
            finishList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    /**
     * 点击发消息按钮，进入聊天界面
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void preessSendMessageButton() {
        List<AccessibilityNodeInfo> sendMessageList = nodeInfo.findAccessibilityNodeInfosByText(SEND_MESSAGE_TEXT_KEY);
        if (!sendMessageList.isEmpty()) {
            sendMessageList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    /**
     * 发送消息给新朋友
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendMessageToFriends() {
        findEditTextByClassName(nodeInfo, "android.widget.EditText");
        if (mFindView != null) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null) {
                return;
            }
            ClipData clip = ClipData.newPlainText("message", MESSAGE);
            clipboard.setPrimaryClip(clip);
            mFindView.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            mFindView.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            List<AccessibilityNodeInfo> sendButtonList = nodeInfo.findAccessibilityNodeInfosByText(SEND_BUTTON_TEXT_KEY);
            if (!sendButtonList.isEmpty()) {
                sendButtonList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                List<AccessibilityNodeInfo> nodeInfos = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/hl");
                if (nodeInfos != null && nodeInfos.size() != 0) {
                    AccessibilityNodeInfo nodeInfo = nodeInfos.get(0);
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }


    //通过组件名递归查找编辑框
    private void findEditTextByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if (className.equals(nodeInfo.getClassName().toString())) {
            mFindView = nodeInfo;
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
}
