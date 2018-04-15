package com.example.lydia.wechatautoaddfriends.data;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.RoomDatabase;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by lydia on 2018/4/15.
 */

@Database(entities = {NotificationEvent.class}, version = 1, exportSchema = false)
public abstract class NotificationEventsDataBase extends RoomDatabase {
    public abstract EventsDao eventsDao();
}
