package com.example.lydia.wechatautoaddfriends.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;

/**
 * Created by lydia on 2018/4/15.
 */

@Dao
public abstract class EventsDao {

    @Insert
    public abstract void insertEvent(NotificationEvent... events);

    @Delete
    public abstract void deleteNotificationEvents(NotificationEvent... events);

    @Query("SELECT  * FROM NotificationEvent")
    public abstract List<NotificationEvent> searchAllEvents();

}
