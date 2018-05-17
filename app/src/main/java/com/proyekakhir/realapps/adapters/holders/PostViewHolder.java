/*
 *  Copyright 2017 Rozdoum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.proyekakhir.realapps.adapters.holders;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.proyekakhir.realapps.Constants;
import com.proyekakhir.realapps.R;
import com.proyekakhir.realapps.controllers.LikeController;
import com.proyekakhir.realapps.managers.PostManager;
import com.proyekakhir.realapps.managers.ProfileManager;
import com.proyekakhir.realapps.managers.listeners.OnObjectChangedListener;
import com.proyekakhir.realapps.managers.listeners.OnObjectExistListener;
import com.proyekakhir.realapps.model.Like;
import com.proyekakhir.realapps.model.Post;
import com.proyekakhir.realapps.model.Profile;
import com.proyekakhir.realapps.utils.FormatterUtil;
import com.proyekakhir.realapps.utils.Utils;

import java.util.HashMap;

/**
 * Created by alexey on 27.12.16.
 */

public class PostViewHolder extends RecyclerView.ViewHolder {
    public static final String TAG = PostViewHolder.class.getSimpleName();

    private Context context;
    private ImageView postImageView;
    private TextView titleTextView;
    private TextView detailsTextView;
    private TextView likeCounterTextView;
    private ImageView likesImageView;
    private TextView commentsCountTextView;
    private TextView watcherCounterTextView;
    private TextView dateTextView;
    private ImageView authorImageView;
    private ViewGroup likeViewGroup;
    private RelativeLayout layoutPost;
    private Bitmap vidThumbnail;
    private ProfileManager profileManager;
    private PostManager postManager;
    private FrameLayout label360;
    private FrameLayout labelVideo;
    private LikeController likeController;

    private ThumbnailLoaderTask thumbnailLoaderTask;

    public PostViewHolder(View view, final OnClickListener onClickListener) {
        this(view, onClickListener, true);
    }

    public PostViewHolder(View view, final OnClickListener onClickListener, boolean isAuthorNeeded) {
        super(view);
        this.context = view.getContext();

        labelVideo = (FrameLayout) view.findViewById(R.id.label_layout_post_video);
        label360 = (FrameLayout) view.findViewById(R.id.label_layout_post);
        postImageView = (ImageView) view.findViewById(R.id.postImageView);
        likeCounterTextView = (TextView) view.findViewById(R.id.likeCounterTextView);
        likesImageView = (ImageView) view.findViewById(R.id.likesImageView);
        commentsCountTextView = (TextView) view.findViewById(R.id.commentsCountTextView);
        watcherCounterTextView = (TextView) view.findViewById(R.id.watcherCounterTextView);
        dateTextView = (TextView) view.findViewById(R.id.dateTextView);
        titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        detailsTextView = (TextView) view.findViewById(R.id.detailsTextView);
        authorImageView = (ImageView) view.findViewById(R.id.authorImageView);
        likeViewGroup = (ViewGroup) view.findViewById(R.id.likesContainer);
        layoutPost = (RelativeLayout) view.findViewById(R.id.layoutPost);
        layoutPost.setLayerType(View.LAYER_TYPE_SOFTWARE,null);

        authorImageView.setVisibility(isAuthorNeeded ? View.VISIBLE : View.GONE);

        profileManager = ProfileManager.getInstance(context.getApplicationContext());
        postManager = PostManager.getInstance(context.getApplicationContext());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onItemClick(getAdapterPosition(), v);
                }
            }
        });

        likeViewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onLikeClick(likeController, position);
                }
            }
        });

        authorImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onAuthorClick(getAdapterPosition(), v);
                }
            }
        });
    }

    public static Bitmap retriveVideoFrameFromVideo(String videoPath) throws Throwable
    {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try
        {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            else
                mediaMetadataRetriever.setDataSource(videoPath);
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.getMessage());

        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }

    public void bindData(Post post) {
        Log.e(TAG, "bindData: "+post.getId() );
        Log.e(TAG, "bindData: is360 "+post.isState360() );
        Log.e(TAG, "bindData: isPrivate "+post.isStatePrivate() );
        if(post.isState360() && !post.isStatePrivate()){
            if(post.isStateVideo360()){
                label360.setVisibility(View.VISIBLE);
                labelVideo.setVisibility(View.VISIBLE);
                thumbnailLoaderTask = new ThumbnailLoaderTask();
                thumbnailLoaderTask.execute(post.getImagePath());
            }else {
                label360.setVisibility(View.VISIBLE);
            }
        }else {
            label360.setVisibility(View.GONE);
            labelVideo.setVisibility(View.GONE);
        }

        likeController = new LikeController(context, post, likeCounterTextView, likesImageView, true);

        String title = removeNewLinesDividers(post.getTitle());
        titleTextView.setText(title);
        String description = removeNewLinesDividers(post.getDescription());
        detailsTextView.setText(description);
        likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
        commentsCountTextView.setText(String.valueOf(post.getCommentsCount()));
        watcherCounterTextView.setText(String.valueOf(post.getWatchersCount()));

        CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(context, post.getCreatedDate());
        dateTextView.setText(date);

        String imageUrl = post.getImagePath();
        int width = Utils.getDisplayWidth(context);
        int height = (int) context.getResources().getDimension(R.dimen.post_detail_image_height);

        // Displayed and saved to cache image, as needs for post detail.
        if(post.isStateVideo360()){

        }else {
            Glide.with(context)
                    .load(imageUrl)
                    .apply(RequestOptions.centerCropTransform()
                            .override(width,height)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.drawable.ic_stub))
                    .transition(withCrossFade())
                    .into(postImageView);
        }


        if (post.getAuthorId() != null) {
            profileManager.getProfileSingleValue(post.getAuthorId(), createProfileChangeListener(authorImageView));
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            postManager.hasCurrentUserLikeSingleValue(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
        }
    }

    private void loadThumbnailForVideo(Bitmap bm){
        int width = Utils.getDisplayWidth(context);
        int height = (int) context.getResources().getDimension(R.dimen.post_detail_image_height);
        if (isValidContextForGlide(context)) {
            Glide.with(context)
                    .load(bm)
                    .apply(RequestOptions.centerCropTransform()
                            .override(width,height)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.drawable.ic_stub))
                    .transition(withCrossFade())
                    .into(postImageView);
        }
    }

    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }

    private String removeNewLinesDividers(String text) {
        int decoratedTextLength = text.length() < Constants.Post.MAX_TEXT_LENGTH_IN_LIST ?
                text.length() : Constants.Post.MAX_TEXT_LENGTH_IN_LIST;
        return text.substring(0, decoratedTextLength).replaceAll("\n", " ").trim();
    }

    private OnObjectChangedListener<Profile> createProfileChangeListener(final ImageView authorImageView) {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(final Profile obj) {
                if (obj.getPhotoUrl() != null) {

                    Glide.with(context)
                            .asBitmap()
                            .load(obj.getPhotoUrl())
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)
                                    .override(100,100)
                            .centerCrop())
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    authorImageView.setImageBitmap(resource);
                                }
                            });

                }
            }
        };
    }

    private OnObjectExistListener<Like> createOnLikeObjectExistListener() {
        return new OnObjectExistListener<Like>() {
            @Override
            public void onDataChanged(boolean exist) {
                likeController.initLike(exist);
            }
        };
    }

    public interface OnClickListener {
        void onItemClick(int position, View view);

        void onLikeClick(LikeController likeController, int position);

        void onAuthorClick(int position, View view);
    }

    class ThumbnailLoaderTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            loadThumbnailForVideo(vidThumbnail);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                vidThumbnail = retriveVideoFrameFromVideo(strings[0]);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            return true;
        }
    }
}