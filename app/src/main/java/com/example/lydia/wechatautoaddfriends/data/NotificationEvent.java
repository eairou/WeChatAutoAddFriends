package com.example.lydia.wechatautoaddfriends.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by lydia on 2018/4/15.
 */
@Entity()
public class NotificationEvent {
    @PrimaryKey
    public int id;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] bytes;


    public int getId() {
        return id;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public void setId(int id) {
        this.id = id;
    }
}
