package com.example.lydia.wechatautoaddfriends;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

/**
 * Created by lydia on 2018/4/14.
 */

public class NotifyHelper {
    //判断是否息屏
    public  static boolean isScreenLocked(Context c) {
        android.app.KeyguardManager mKeyguardManager = (KeyguardManager) c.getSystemService(Context.KEYGUARD_SERVICE);
        if (mKeyguardManager != null){
            return mKeyguardManager.inKeyguardRestrictedInputMode();
        }
        return false;
    }
}
