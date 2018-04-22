package com.example.lydia.wechatautoaddfriends;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";
    private static final int FLOAT_WINDOW_REQUST_CODE = 1001;
    private EditText mEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissionToShowFloatWindow();
        Button settingsBtn = findViewById(R.id.settings);
//        settingsBtn.setOnClickListener(this);

        Button sendBtn = findViewById(R.id.send);
//        sendBtn.setOnClickListener(this);

        mEditText = findViewById(R.id.message);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings:
                openSettings();
                break;
            case R.id.send:
                Button sendBtn = (Button) v;
                if (sendBtn.getText().toString().equals(getResources().getString(R.string.start_add_friends_and_send_message))) {
                    sendBroadcastToAddFriendsWithMessage();
                    sendBtn.setText(R.string.stop_add_friends);
                }else {
                    sendBroadcastToStopFriendsWithMessage();
                    sendBtn.setText(R.string.start_add_friends_and_send_message);
                }
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
        intent.putExtra(Utils.ADD_KEY, true);
        sendBroadcast(intent);
    }

    private void sendBroadcastToStopFriendsWithMessage(){
        Intent intent = new Intent();
        intent.setAction("android.loy.lydia.start");
        intent.putExtra(Utils.MESSAGE_KEY, mEditText.getText().toString());
        intent.putExtra(Utils.ADD_KEY, false);
        sendBroadcast(intent);
    }
}
