package com.example.lydia.wechatautoaddfriends;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements TextWatcher{
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText editText = findViewById(R.id.message);
        editText.addTextChangedListener(this);
    }


    public void settingsClick(View view){
        try {
            //打开系统设置中辅助功能
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(MainActivity.this, "找到添加好友服务，然后开启服务即可", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendClick(View view){
        Button button = (Button) view;
        if (button.getText() == getResources().getText(R.string.start_add_friends_and_send_message)){
            button.setText(R.string.stop_add_friends);
            AutoAddFriendsService.startAddFriends(true);
        }else {
            button.setText(R.string.start_add_friends_and_send_message);
            AutoAddFriendsService.startAddFriends(false);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        AutoAddFriendsService.setMessage(s.toString());
    }
}
