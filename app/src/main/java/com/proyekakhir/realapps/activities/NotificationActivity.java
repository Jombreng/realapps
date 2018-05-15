package com.proyekakhir.realapps.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.proyekakhir.realapps.R;
import com.proyekakhir.realapps.adapters.NotificationsAdapter;
import com.proyekakhir.realapps.managers.NotificationsManager;
import com.proyekakhir.realapps.managers.PostManager;
import com.proyekakhir.realapps.managers.ProfileManager;
import com.proyekakhir.realapps.managers.listeners.OnDataChangedListener;
import com.proyekakhir.realapps.model.Notifications;

import java.util.List;

public class NotificationActivity extends BaseActivity {

    private static final String TAG = "NotificationActivity";

    private boolean attemptToLoadNotifications = false;
    private NotificationsAdapter notificationsAdapter;
    private RecyclerView notificationsRecyclerView;
    private ProgressBar notificationsProgressBar;
    private static final int TIME_OUT_LOADING_NOTIFICATIONS = 32340;
    private NotificationsManager notificationsManager;
    private TextView warningNotificationsTextView;
    private ProfileManager profileManager;
    private PostManager postManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        profileManager = ProfileManager.getInstance(this);
        postManager = PostManager.getInstance(this);
        notificationsManager = NotificationsManager.getInstance(this);

        warningNotificationsTextView = (TextView) findViewById(R.id.warningNotificationsTextView);
        notificationsRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        notificationsProgressBar = (ProgressBar) findViewById(R.id.notificationsProgressBar);

        initRecyclerView();
    }


    private void initRecyclerView() {
        notificationsAdapter = new NotificationsAdapter();
        notificationsAdapter.setCallback(new NotificationsAdapter.Callback() {
            @Override
            public void onItemClick(Notifications notifications, final View view) {
                if(notifications.getType().equals("follow")){
                    openProfileActivityWithObject(notifications,view);
                }else if(notifications.getType().equals("likes")||notifications.getType().equals("comments")){
                    openPostDetailsActivity(notifications,view);
                }
            }

            @Override
            public void onAuthorClick(String authorId, View view) {
                openProfileActivity(authorId, view);
            }
        });
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ((SimpleItemAnimator) notificationsRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        notificationsRecyclerView.setAdapter(notificationsAdapter);
        notificationsRecyclerView.setNestedScrollingEnabled(false);
        notificationsRecyclerView.addItemDecoration(new DividerItemDecoration(notificationsRecyclerView.getContext(),
                ((LinearLayoutManager) notificationsRecyclerView.getLayoutManager()).getOrientation()));

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        notificationsManager.getNotificationsList(this, userId, createOnChangedDataListener());
    }


    private OnDataChangedListener<Notifications> createOnChangedDataListener() {
        attemptToLoadNotifications = true;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (attemptToLoadNotifications) {
                    notificationsProgressBar.setVisibility(View.GONE);
                    warningNotificationsTextView.setVisibility(View.VISIBLE);
                }
            }
        }, TIME_OUT_LOADING_NOTIFICATIONS);


        return new OnDataChangedListener<Notifications>() {
            @Override
            public void onListChanged(List<Notifications> list) {
                attemptToLoadNotifications = false;
                notificationsProgressBar.setVisibility(View.GONE);
                notificationsRecyclerView.setVisibility(View.VISIBLE);
                warningNotificationsTextView.setVisibility(View.GONE);
                notificationsAdapter.setList(list);
            }
        };
    }


    private void openPostDetailsActivity(Notifications notifications, View v) {
        Intent intent = new Intent(NotificationActivity.this, PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, notifications.getPostId());

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
        } else {
            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
        }
    }

    private void openProfileActivity(String userId, View view) {
        Intent intent = new Intent(NotificationActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(NotificationActivity.this,
                            new android.util.Pair<>(view, getString(R.string.post_author_image_transition_name)));
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    private void openProfileActivityWithObject(Notifications notifications, View view) {
        Intent intent = new Intent(NotificationActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, notifications.getAuthorId());

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(NotificationActivity.this,
                            new android.util.Pair<>(view, getString(R.string.post_author_image_transition_name)));
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }
}
