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
public interface EventsDao {
    @Insert
    public void insertEventList(List<NotificationEvent> eventList);

    @Insert
    public void insertEvent(NotificationEvent... events);

    @Delete
    public void deleteNotificationEvents(NotificationEvent... events);

    @Query("SELECT  * FROM NotificationEvent")
    public List<NotificationEvent> searchAllEvents();

}
