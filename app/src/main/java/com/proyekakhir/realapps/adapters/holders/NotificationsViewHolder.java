package com.proyekakhir.realapps.adapters.holders;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.proyekakhir.realapps.R;
import com.proyekakhir.realapps.adapters.NotificationsAdapter;
import com.proyekakhir.realapps.managers.ProfileManager;
import com.proyekakhir.realapps.managers.listeners.OnObjectChangedListener;
import com.proyekakhir.realapps.model.Notifications;
import com.proyekakhir.realapps.model.Profile;
import com.proyekakhir.realapps.utils.FormatterUtil;
import com.proyekakhir.realapps.views.ExpandableTextView;

public class NotificationsViewHolder extends RecyclerView.ViewHolder{

    private static final String TAG = "NotificationsViewHolder";
    private final ImageView avatarImageView;
    //private final ImageView postImageView;
    private final ExpandableTextView commentTextView;
    private final TextView dateTextView;
    private final ProfileManager profileManager;

    private NotificationsAdapter.Callback callback;
    private Context context;

    public NotificationsViewHolder(View itemView, final NotificationsAdapter.Callback callback) {
        super(itemView);

        this.callback = callback;
        this.context = itemView.getContext();

        profileManager = ProfileManager.getInstance(itemView.getContext().getApplicationContext());

        avatarImageView = (ImageView) itemView.findViewById(R.id.avatarImageView);
        //postImageView = (ImageView) itemView.findViewById(R.id.post_imageView);
        commentTextView = (ExpandableTextView) itemView.findViewById(R.id.commentText);
        dateTextView = (TextView) itemView.findViewById(R.id.dateTextView);

        if (callback != null) {
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    int position = getAdapterPosition();
//                    if (position != RecyclerView.NO_POSITION) {
//                        callback.onItemClick(v, position);
//                        return true;
//                    }
//
//                    return false;
//                }
//            });
        }
    }

    public void bindData(final Notifications comment) {
        final String id = comment.getAuthorId();
        if (id != null)
            profileManager.getProfileSingleValue(id, createOnProfileChangeListener(commentTextView,
                    avatarImageView,comment));

        commentTextView.setText("");
        Log.e(TAG, "bindData: Notif.getid()"+comment.getNotificationsId() );

        CharSequence date = FormatterUtil.getRelativeTimeSpanString(context, comment.getCreatedDate());
        dateTextView.setText(date);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onItemClick(comment, v);
            }
        });
        avatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onAuthorClick(id, v);
            }
        });
    }

    private OnObjectChangedListener<Profile> createOnProfileChangeListener(final ExpandableTextView expandableTextView, final ImageView avatarImageView, final Notifications notifications) {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                String userName = obj.getUsername();
                fillComment(userName, notifications,  expandableTextView);

                if (obj.getPhotoUrl() != null) {
                    Glide.with(context)
                            .load(obj.getPhotoUrl())
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)
                            .error(R.drawable.ic_stub))
                            .transition(withCrossFade())
                            .into(avatarImageView);
                }
            }
        };
    }

    private void fillComment(String userName,Notifications notifications, ExpandableTextView commentTextView) {
        String text="";
        if(notifications.getType().equals("likes")){
            text = "like your photo";
        }else if(notifications.getType().equals("follow")){
            text = "start following you";
        }else if(notifications.getType().equals("comments")){
            text = "commented: ";
        }
        Spannable contentString = new SpannableStringBuilder(userName + "   "+text+notifications.getText() );


        contentString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.highlight_text)),
                0, userName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        commentTextView.setText(contentString);
    }
}
