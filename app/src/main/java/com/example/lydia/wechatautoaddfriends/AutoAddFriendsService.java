package com.example.lydia.wechatautoaddfriends;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.example.lydia.wechatautoaddfriends.data.NotificationEvent;
import com.example.lydia.wechatautoaddfriends.data.NotificationEventsDataBase;

import java.util.List;

/**
 * Created by lydia on 2018/4/13.
 */

@SuppressLint("Registered")
public class AutoAddFriendsService extends AccessibilityService {
    private static final String TAG = "AutoAddFriendsService";
    private static final String WECHAT_PACKAGENAME = "com.tencent.mm";//微信包名
    private static final String REQUST_ADD_FRIEND_TEXT_KEY = "请求添加你为朋友"; //添加好友消息关键字
    private static final String ACCEPT_ADD_FRIEND_TEXT_KEY = "接受"; //添加好友关键字
    private static final String FINISH_ADD_FRIEND_TEXT_KEY = "完成"; //完成添加好友关键字
    private static final String SEND_MESSAGE_TEXT_KEY = "发消息"; //发送消息关键字
    private static final String SEND_BUTTON_TEXT_KEY = "发送"; //发送按钮关键字

    private static String MESSAGE = "你好，我是丫丫鞋业，欢迎加入。如果有需要的话，可以去我朋友圈看看，各式高仿名牌，质量保证，价格实惠。";

    private List<NotificationEvent> mEventsList;
    private static final String EVENTS_FILE_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/events_list.txt";

    private boolean isFirstOpenNotification = true;
    private AccessibilityNodeInfo editText;

    //锁屏、唤醒相关
    private KeyguardManager km;
    private KeyguardManager.KeyguardLock kl;
    private PowerManager pm;
    private PowerManager.WakeLock wl = null;
    private boolean enableKeyguard = false;
    private boolean enablePower = false;

    private NotificationEventsDataBase dataBase;

    private NotificationEvent mCurrentEvent;

    private boolean mAddFriendsServiceConnected = false;

    private static boolean ADD_FRIENDS = false;


    public AutoAddFriendsService(){

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!event.getPackageName().equals(WECHAT_PACKAGENAME)) {
            return;
        }
        int eventType = event.getEventType();
        Log.e(TAG, "==============Start==================== type: " + eventType);
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();
                for (CharSequence text : texts) {
                    String str = text.toString();
                    if (!str.isEmpty())
                        if (str.contains(REQUST_ADD_FRIEND_TEXT_KEY)) {
                            // 模拟打开通知消息
                            final NotificationEvent notificationEvent = new NotificationEvent(event.hashCode());
                            notificationEvent.setEvent(event);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    dataBase.eventsDao().insertEvent(notificationEvent);
                                }
                            }).start();
                        }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                // 添加好友流程开始
                if (!mEventsList.isEmpty()) {
                    addFriends();
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:

                break;
            default:
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void addFriends() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> acceptList = nodeInfo.findAccessibilityNodeInfosByText(ACCEPT_ADD_FRIEND_TEXT_KEY);
        if (!acceptList.isEmpty()) {
            acceptList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        List<AccessibilityNodeInfo> finishList = nodeInfo.findAccessibilityNodeInfosByText(FINISH_ADD_FRIEND_TEXT_KEY);
        if (!finishList.isEmpty()) {
            finishList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

        List<AccessibilityNodeInfo> sendMessageList = nodeInfo.findAccessibilityNodeInfosByText(SEND_MESSAGE_TEXT_KEY);
        if (!sendMessageList.isEmpty()) {
            sendMessageList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
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
                dataBase.eventsDao().deleteNotificationEvents(mCurrentEvent);
                openNotification();
            }
        }

    }

    private Thread mOpenOperationThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "thread run!");
            mEventsList = dataBase.eventsDao().searchAllEvents();
            while (true){
                if (ADD_FRIENDS){
                    Log.e(TAG, "thread run: add friends!");
                    openNotification();
                }
            }
        }
    });


    //通过组件名递归查找编辑框
    private void findEditTextByClassName(AccessibilityNodeInfo nodeInfo, String ClassName) {
        if (ClassName.equals(nodeInfo.getClassName())) {
            editText = nodeInfo;
            return;
        }
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            findEditTextByClassName(nodeInfo.getChild(i), ClassName);
        }
    }

    /**
     * 模拟打开加好友通知消息栏
     */
    private void openNotification() {
        mEventsList = dataBase.eventsDao().searchAllEvents();
        kl = km.newKeyguardLock("unLock");
        if (mEventsList.isEmpty()) {
            return;
        }
        mCurrentEvent = mEventsList.get(mEventsList.size() - 1);
        AccessibilityEvent event = mCurrentEvent.event;
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onInterrupt() {
        if(mOpenOperationThread.isAlive()) {
            mOpenOperationThread.interrupt();
        }
        Toast.makeText(this, "添加好友服务中断", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        //获取电源管理器对象
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //得到键盘锁管理器对象
        km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        kl = km.newKeyguardLock("unLock");

        dataBase = Room.databaseBuilder(getApplicationContext(), NotificationEventsDataBase.class, "notification_events.db").build();
        mOpenOperationThread.start();
        Toast.makeText(this, "添加好友服务连接成功", Toast.LENGTH_SHORT).show();
    }

    public static void setMessage(String message) {
        MESSAGE = message;
    }

    public static void startAddFriends(boolean add) {
        ADD_FRIENDS = add;
    }

    /**
     * 唤醒和解锁相关
     */
    private void wakeAndUnlock() {
        if (mEventsList.isEmpty()){
            if (enablePower) {
                wl.release();
            }
            if (enableKeyguard) {
                kl.reenableKeyguard();
            }
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
            enableKeyguard  = true;
            kl.disableKeyguard();
            Log.i("demo", "解锁");
        }
    }
}
