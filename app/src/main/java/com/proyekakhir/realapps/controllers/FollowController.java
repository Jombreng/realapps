package com.proyekakhir.realapps.controllers;

import android.content.Context;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.proyekakhir.realapps.ApplicationHelper;
import com.proyekakhir.realapps.R;
import com.proyekakhir.realapps.activities.BaseActivity;
import com.proyekakhir.realapps.activities.MainActivity;
import com.proyekakhir.realapps.enums.ProfileStatus;
import com.proyekakhir.realapps.managers.FollowManager;
import com.proyekakhir.realapps.managers.PostManager;
import com.proyekakhir.realapps.managers.ProfileManager;
import com.proyekakhir.realapps.managers.listeners.OnObjectExistListener;
import com.proyekakhir.realapps.model.Follow;
import com.proyekakhir.realapps.model.Post;
import com.proyekakhir.realapps.model.Profile;

public class FollowController {

    private boolean updatingFollowerCounter = true;
    private boolean isFollowed = false;

    private TextView followerCounterTextView;
    private Button followButton;

    private Context context;
    private String followerId;
    private String AuthorId;

    public FollowController(Context context, String follower,String authorId, TextView followerCounterTextView,
                            Button followButton) {
        this.context = context;
        this.followerId = follower;
        this.AuthorId = authorId;
        this.followerCounterTextView = followerCounterTextView;
        this.followButton = followButton;
    }

    private void addFollow(long prevValue) {
        updatingFollowerCounter = true;
        isFollowed = true;
        followerCounterTextView.setText(String.valueOf(prevValue + 1));
        ApplicationHelper.getDatabaseHelper().createOrUpdateFollow(followerId, AuthorId);
    }

    private void removeFollow(long prevValue) {
        updatingFollowerCounter = true;
        isFollowed = false;
        followerCounterTextView.setText(String.valueOf(prevValue - 1));
        ApplicationHelper.getDatabaseHelper().removeFollow(followerId, AuthorId);
    }

    public void followClickAction(long prevValue) {

            if (!isFollowed) {
                addFollow(prevValue);
            } else {
                removeFollow(prevValue);
            }

    }

    private void doHandleFollowClickAction(BaseActivity baseActivity, Profile profile) {
        ProfileStatus profileStatus = ProfileManager.getInstance(baseActivity).checkProfile();

        if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
            followClickAction(profile.getFollowersCount());
        } else {
            baseActivity.doAuthorization(profileStatus);
        }
    }

    public void handleFollowClickAction(final BaseActivity baseActivity, final Profile profile) {
        FollowManager.getInstance(baseActivity.getApplicationContext()).isPostExistSingleValue(profile.getId(), new OnObjectExistListener<Profile>() {
            @Override
            public void onDataChanged(boolean exist) {
                if (exist) {
                    if (baseActivity.hasInternetConnection()) {
                        doHandleFollowClickAction(baseActivity, profile);
                    } else {
                        showWarningMessage(baseActivity, R.string.internet_connection_failed);
                    }
                } else {
                    showWarningMessage(baseActivity, R.string.user_not_exist);
                }
            }
        });
    }

    private void showWarningMessage(BaseActivity baseActivity, int messageId) {
        if (baseActivity instanceof MainActivity) {
            ((MainActivity) baseActivity).showFloatButtonRelatedSnackBar(messageId);
        } else {
            baseActivity.showSnackBar(messageId);
        }
    }

    public void initFollow(boolean isFollowed) {
        followButton.setBackground(isFollowed ? context.getResources().getDrawable(R.drawable.btn_unfollowing) : context.getResources().getDrawable(R.drawable.btn_following));
        followButton.setText(isFollowed ? "UNFOLLOW" : "FOLLOW");
        this.isFollowed = isFollowed;
    }
}
