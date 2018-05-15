package com.proyekakhir.realapps.adapters.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.proyekakhir.realapps.R;
import com.proyekakhir.realapps.views.CircularImageView;

import java.util.List;

public class UsersViewHolder extends RecyclerView.ViewHolder {

    public ClickListener getmClickListener() {
        return mClickListener;
    }

    public View getmView() {

        return mView;
    }

    View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onItemClick(v, getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mClickListener.onItemLongClick(v, getAdapterPosition());
                    return true;
                }
            });
        }

        private UsersViewHolder.ClickListener mClickListener;

        //Interface to send callbacks...
        public interface ClickListener{
            public void onItemClick(View view, int position);
            public void onItemLongClick(View view, int position);
        }

        public void setOnClickListener(UsersViewHolder.ClickListener clickListener){
            mClickListener = clickListener;
        }

        public void setDetails(Context ctx, String userName, String email,String id, String userImage){

            TextView tvUsername = (TextView) mView.findViewById(R.id.username_text);
            TextView tvName = (TextView) mView.findViewById(R.id.name_text);
            TextView tvId = (TextView) mView.findViewById(R.id.userId);
            CircularImageView ivUserimage = (CircularImageView) mView.findViewById(R.id.profile_image);
            RelativeLayout rel = (RelativeLayout) mView.findViewById(R.id.relLayoutUser);

            ViewGroup.LayoutParams params = rel.getLayoutParams();

            if(!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(id)){

                rel.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                tvUsername.setText(userName);
                tvName.setText(email);
                tvId.setText(id);

                Glide.with(ctx).load(userImage).into(ivUserimage);
            }else {
                params.height = 0;
                rel.setLayoutParams(params);
            }
        }

        public void setDetailsRecent(Context ctx, String userName, String email, String id, String userImage, List<String> recentId){

            TextView tvUsername = (TextView) mView.findViewById(R.id.username_text);
            TextView tvName = (TextView) mView.findViewById(R.id.name_text);
            TextView tvId = (TextView) mView.findViewById(R.id.userId);
            CircularImageView ivUserimage = (CircularImageView) mView.findViewById(R.id.profile_image);
            RelativeLayout rel = (RelativeLayout) mView.findViewById(R.id.relLayoutUser);

            ViewGroup.LayoutParams params = rel.getLayoutParams();

            if(!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(id) && recentId.contains(id)){
                rel.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                tvUsername.setText(userName);
                tvName.setText(email);
                tvId.setText(id);

                Glide.with(ctx).load(userImage).into(ivUserimage);
            }else {
                params.height = 0;
                rel.setLayoutParams(params);
            }
        }
    }
