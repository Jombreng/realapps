package com.proyekakhir.realapps.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.proyekakhir.realapps.R;
import com.proyekakhir.realapps.activities.ChatActivity;
import com.proyekakhir.realapps.adapters.holders.UsersViewHolder;
import com.proyekakhir.realapps.managers.listeners.IDataCallback;
import com.proyekakhir.realapps.managers.listeners.IFragmentListener;
import com.proyekakhir.realapps.managers.listeners.ISearch;
import com.proyekakhir.realapps.model.Profile;

import java.util.ArrayList;

public class SearchChatFragment extends Fragment implements ISearch, IDataCallback{
    private RecyclerView recyclerView;
    private DatabaseReference mUserDatabase;
    private static final String ARG_SEARCHTERM = "search_term";
    private String mSearchTerm = null;

    ArrayList<String> strings = null;
    private IFragmentListener mIFragmentListener = null;
    ArrayAdapter<String> arrayAdapter = null;

    private View view;

    //Overriden method onCreateView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Returning the layout file after inflating
        //Change R.layout.tab1 in you classes
        view = inflater.inflate(R.layout.fragment_searchchat, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_searchchat);

        mUserDatabase = FirebaseDatabase.getInstance().getReference("profiles");
        firebaseUserSearch("");

        if (getArguments() != null) {
            mSearchTerm = (String) getArguments().get(ARG_SEARCHTERM);
        }
        return view;
    }

    @Override
    public void onTextQuery(String text) {
        firebaseUserSearch(text);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mSearchTerm) {
            onTextQuery(mSearchTerm);
        }
    }

    public static SearchChatFragment newInstance(String searchTerm) {
        SearchChatFragment fragment = new SearchChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_SEARCHTERM, searchTerm);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIFragmentListener = (IFragmentListener) context;
        mIFragmentListener.addiSearch(SearchChatFragment.this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (null != mIFragmentListener)
            mIFragmentListener.removeISearch(SearchChatFragment.this);
    }

    @Override
    public void onFragmentCreated(ArrayList<String> listData) {
        strings = listData;
    }

    private void firebaseUserSearch(String searchText) {
        final RecyclerView.Adapter adapter = newAdapter(searchText);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
//        //Toast.makeText(SearchActivity.this, "Started Search", Toast.LENGTH_SHORT).show();
//
//        Query firebaseSearchQuery = mUserDatabase.orderByChild("username").startAt(searchText).endAt(searchText + "\uf8ff");
//        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_searchchat);
//
//        final FirebaseRecyclerAdapter<Profile, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Profile, UsersViewHolder>(
//
//                Profile.class,
//                R.layout.user_list_layout,
//                UsersViewHolder.class,
//                firebaseSearchQuery
//
//        ) {
//            @Override
//            protected void populateViewHolder(UsersViewHolder viewHolder, Profile model, int position) {
//
//                    viewHolder.setDetails(getContext(), model.getUsername(), model.getEmail(), model.getId(),model.getPhotoUrl());
//            }
//
//            @Override
//            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//                final UsersViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
//
//                viewHolder.setOnClickListener(new UsersViewHolder.ClickListener() {
//                    @Override
//                    public void onItemClick(View view, int position) {
//                        String userId = getRef(position).getKey();
//                        Profile p = getItem(position);
//                        ChatActivity.startActivity(getActivity(),p.getEmail(),p.getId(),p.getFirebaseToken());
//                        //openProfileActivity(userId);
//                        //Toast.makeText(getContext(), "Key ["+p.getId()+"] clicked at " + position, Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onItemLongClick(View view, int position) {
//                        Toast.makeText(getContext(), "Item long clicked at " + position, Toast.LENGTH_SHORT).show();
//                    }
//                });
//                return viewHolder;
//            }
//        };
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    protected RecyclerView.Adapter newAdapter(String searchText) {

        Query firebaseSearchQuery = mUserDatabase.orderByChild("username").startAt(searchText).endAt(searchText + "\uf8ff");

        FirebaseRecyclerOptions<Profile> options =
                new FirebaseRecyclerOptions.Builder<Profile>()
                        .setQuery(firebaseSearchQuery, Profile.class)
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
                holder.setDetails(getContext(), model.getUsername(), model.getEmail(), model.getId(),model.getPhotoUrl());
                holder.setOnClickListener(new UsersViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Profile p = getItem(position);
                        ChatActivity.startActivity(getActivity(),p.getUsername(),p.getId(),p.getFirebaseToken());
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

}
