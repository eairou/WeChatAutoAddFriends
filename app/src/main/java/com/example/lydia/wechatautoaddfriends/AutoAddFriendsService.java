package com.example.lydia.wechatautoaddfriends;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

/**
 * Created by lydia on 2018/4/13.
 */

@SuppressLint("Registered")
public class AutoAddFriendsService extends AccessibilityService {
    private static final String TAG = "AutoAddFriendsService";
    private static final String WECHAT_PACKAGENAME = "com.tencent.mm";//微信包名
    private static final String ACCEPT_ADD_FRIEND_TEXT_KEY = "接受"; //添加好友关键字
    private static final String FINISH_ADD_FRIEND_TEXT_KEY = "完成"; //完成添加好友关键字
    private static final String SEND_MESSAGE_TEXT_KEY = "发消息"; //发送消息关键字
    private static final String SEND_BUTTON_TEXT_KEY = "发送"; //发送按钮关键字

    private static String MESSAGE = "你好";
    private AccessibilityNodeInfo mFindView;

    AccessibilityNodeInfo nodeInfo;

    private static boolean ADD = false;


    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        int eventType = event.getEventType();
        Log.d(TAG, "==============Start==================== type: " + eventType);
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                Log.d(TAG, "onAccessibilityEvent: AutoAddFriendsService");
                nodeInfo = getRootInActiveWindow();
                if (nodeInfo == null || !ADD) {
                    return;
                }
                String windowClassName = event.getClassName().toString();
                Log.d(TAG, "window type changed: " + windowClassName);
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
                    pressSendMessageButton();
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
        Toast.makeText(this, "微信一键添加好友服务中断", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.loy.lydia.start");
        registerReceiver(receiver, filter);

        Toast.makeText(this, "微信一键添加好友服务连接成功", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
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
        if (nodeInfo == null){
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<AccessibilityNodeInfo> accessibilityNodeInfoList = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/iq");
//                List<AccessibilityNodeInfo> accessibilityNodeInfoList = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/j8");
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
        }, 300);
    }

    /**
     * 点击接受按钮
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void acceptAddFriends() {
        List<AccessibilityNodeInfo> acceptList = nodeInfo.findAccessibilityNodeInfosByText(ACCEPT_ADD_FRIEND_TEXT_KEY);
        if (!acceptList.isEmpty()) {
            acceptList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }else {
            List<AccessibilityNodeInfo> listView = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b8p");
//            List<AccessibilityNodeInfo> listView = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b8u");

            boolean isPerformedScroll = listView.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            if (isPerformedScroll) {
                nodeInfo = getRootInActiveWindow();
                acceptAddFriends();
            }
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
    private void pressSendMessageButton() {
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
}
