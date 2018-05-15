package com.proyekakhir.realapps.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.proyekakhir.realapps.Constants;
import com.proyekakhir.realapps.FirebaseChatMainApp;
import com.proyekakhir.realapps.R;
import com.proyekakhir.realapps.fragment.ChatFragment;


public class ChatActivity extends BaseActivity {
    private Toolbar mToolbar;
    private static final String TAG = "ChatActivity";
    public static void startActivity(Context context,
                                     String receiver,
                                     String receiverUid,
                                     String firebaseToken) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Constants.Chat.ARG_RECEIVER, receiver);
        intent.putExtra(Constants.Chat.ARG_RECEIVER_UID, receiverUid);
        intent.putExtra(Constants.Chat.ARG_FIREBASE_TOKEN, firebaseToken);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        bindViews();
        init();
    }

    private void bindViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void init() {
        // set the toolbar
        setSupportActionBar(mToolbar);

        // set toolbar title
        mToolbar.setTitle(getIntent().getExtras().getString(Constants.Chat.ARG_RECEIVER));

        // set the register screen fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_content_chat,
                ChatFragment.newInstance(getIntent().getExtras().getString(Constants.Chat.ARG_RECEIVER),
                        getIntent().getExtras().getString(Constants.Chat.ARG_RECEIVER_UID),
                        getIntent().getExtras().getString(Constants.Chat.ARG_FIREBASE_TOKEN)),
                ChatFragment.class.getSimpleName());
        Log.e(TAG, "init isi pref token: "+getIntent().getExtras().getString(Constants.Chat.ARG_FIREBASE_TOKEN) );
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseChatMainApp.setChatActivityOpen(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseChatMainApp.setChatActivityOpen(false);
    }
}
