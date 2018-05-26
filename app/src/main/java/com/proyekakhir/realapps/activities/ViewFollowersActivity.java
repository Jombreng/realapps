package com.proyekakhir.realapps.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.proyekakhir.realapps.R;
import com.proyekakhir.realapps.adapters.holders.UsersViewHolder;
import com.proyekakhir.realapps.model.Profile;

import java.util.ArrayList;
import java.util.Map;

import static com.proyekakhir.realapps.activities.ProfileActivity.FOLLOW_EXTRA_KEY;
import static com.proyekakhir.realapps.activities.ProfileActivity.USER_ID_EXTRA_KEY;

public class ViewFollowersActivity extends BaseActivity {
    private static final String TAG = "ViewFollowersActivity";

    private ArrayList<String> ids = new ArrayList<>();

    private String userID;
    private RecyclerView recyclerView;
    private DatabaseReference mUserDatabase;
    private String key;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_follower);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        userID = getIntent().getStringExtra(USER_ID_EXTRA_KEY);
        key = getIntent().getStringExtra(FOLLOW_EXTRA_KEY);

        recyclerView = findViewById(R.id.recycler_view_recentchat);
        mUserDatabase = FirebaseDatabase.getInstance().getReference("profiles");
        if(key.equals("following")){
            getFollowingIds();
        }else {
            getFollowersIds();
        }
    }

    private void getFollowersIds() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("follower").child(userID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    collectFollowersIds((Map<String,Object>) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowingIds() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("following").child(userID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    collectFollowersIds((Map<String,Object>) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void collectFollowersIds(Map<String,Object> users) {
        //iterate through each user, ignoring their UID
        ids.clear();
        for (Map.Entry<String, Object> entry : users.entrySet()){

            //Get user map
            Map singleUser = (Map) entry.getValue();
            //Get phone field and append to list
            ids.add((String) singleUser.get("id"));
        }

        RecyclerView.Adapter adapter = newAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Log.e(TAG, "collectFollowingIds: "+ids.toString() );
    }

    private RecyclerView.Adapter newAdapter(){

        Query firebaseQuery = mUserDatabase.orderByChild("username");
        FirebaseRecyclerOptions<Profile> options =
                new FirebaseRecyclerOptions.Builder<Profile>()
                        .setQuery(firebaseQuery, Profile.class)
                        .setLifecycleOwner(this)
                        .build();



        return new FirebaseRecyclerAdapter<Profile, UsersViewHolder>(options) {
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new UsersViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_list_layout, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Profile model) {
                holder.setDetailsFollowers(ViewFollowersActivity.this, model.getUsername(), model.getEmail(), model.getId(),model.getPhotoUrl(),ids);
                holder.setOnClickListener(new UsersViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        String userId = getRef(position).getKey();
                        openProfileActivity(userId);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                    }
                });
            }

            @Override
            public void onDataChanged() {
                // If there are no chat messages, show a view that invites the user to add a message.
                //mEmptyListMessage.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        };
    }

    private void openProfileActivity(String userId) {
        Intent intent = new Intent(ViewFollowersActivity.this, ProfileActivity.class);
        intent.putExtra(USER_ID_EXTRA_KEY, userId);
        startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST);
    }
}
