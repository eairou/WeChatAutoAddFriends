package com.example.lydia.wechatautoaddfriends;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int FLOAT_WINDOW_REQUST_CODE = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissionToShowFloatWindow();
    }

    private void startFloatViewService(){
        Intent intent = new Intent(getApplicationContext(), AddFriendsFloatViewService.class);
        startService(intent);
        finish();
    }

    /**
     * Tpv loy.ouyang: request permission for showing float window
     */
    private void getPermissionToShowFloatWindow(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(intent, FLOAT_WINDOW_REQUST_CODE);
            } else {
                startFloatViewService();
            }
        } else {
            startFloatViewService();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FLOAT_WINDOW_REQUST_CODE) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(getApplicationContext(), getBaseContext().getResources().getString(R.string.aler_window_permission_failed), Toast.LENGTH_SHORT).show();
                } else {
                    startFloatViewService();
                }
            }
        }

    }

}
