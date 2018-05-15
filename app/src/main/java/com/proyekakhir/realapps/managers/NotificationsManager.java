package com.proyekakhir.realapps.managers;

import android.content.Context;

import com.google.firebase.database.ValueEventListener;
import com.proyekakhir.realapps.ApplicationHelper;
import com.proyekakhir.realapps.managers.listeners.OnDataChangedListener;
import com.proyekakhir.realapps.model.Notifications;

public class NotificationsManager extends FirebaseListenersManager {
    private static final String TAG = NotificationsManager.class.getSimpleName();
    private static NotificationsManager instance;

    private Context context;

    public static NotificationsManager getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationsManager(context);
        }

        return instance;
    }

    private NotificationsManager(Context context) {
        this.context = context;
    }

    public void getNotificationsList(Context activityContext, String userId, OnDataChangedListener<Notifications> onDataChangedListener) {
        ValueEventListener valueEventListener = ApplicationHelper.getDatabaseHelper().getNotificationsList(userId, onDataChangedListener);
        addListenerToMap(activityContext, valueEventListener);
    }
}
