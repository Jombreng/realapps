package com.proyekakhir.realapps.managers;

import android.content.Context;

import com.google.firebase.database.ValueEventListener;
import com.proyekakhir.realapps.ApplicationHelper;
import com.proyekakhir.realapps.managers.listeners.OnObjectExistListener;
import com.proyekakhir.realapps.model.Follow;
import com.proyekakhir.realapps.model.Like;
import com.proyekakhir.realapps.model.Profile;

public class FollowManager extends FirebaseListenersManager{

    private static final String TAG = FollowManager.class.getSimpleName();
    private static FollowManager instance;
    private Context context;

    public static FollowManager getInstance(Context context) {
        if (instance == null) {
            instance = new FollowManager(context);
        }

        return instance;
    }

    private FollowManager(Context context) {
        this.context = context;
    }

    public void hasCurrentUserFollow(Context activityContext, String followerId, String authorId, final OnObjectExistListener<Follow> onObjectExistListener) {
        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        ValueEventListener valueEventListener = databaseHelper.hasCurrentUserFollow(followerId, authorId, onObjectExistListener);
        addListenerToMap(activityContext, valueEventListener);
    }

    public void isPostExistSingleValue(String userId, final OnObjectExistListener<Profile> onObjectExistListener) {
        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        databaseHelper.isProfileExistSingleValue(userId, onObjectExistListener);
    }
}
