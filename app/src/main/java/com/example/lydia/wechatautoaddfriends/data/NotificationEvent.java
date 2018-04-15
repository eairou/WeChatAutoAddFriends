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

    @Ignore
    public AccessibilityEvent event;

    public NotificationEvent(int id){
        this.id = id;
    }

    public void setEvent(AccessibilityEvent event){
        this.event = event;
    }

    public AccessibilityEvent getEvent(){
        return this.event;
    }
}
