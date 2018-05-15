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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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
import com.proyekakhir.realapps.utils.SquareImageView;
import com.proyekakhir.realapps.utils.Utils;

/**
 * Created by alexey on 27.12.16.
 */

public class PrivatePostViewHolder extends RecyclerView.ViewHolder {
    public static final String TAG = PrivatePostViewHolder.class.getSimpleName();

    private Context context;
    private SquareImageView postImageView;


    public PrivatePostViewHolder(View view, final OnClickListener onClickListener) {
        this(view, onClickListener, true);
    }

    public PrivatePostViewHolder(View view, final OnClickListener onClickListener, boolean isAuthorNeeded) {
        super(view);
        this.context = view.getContext();

        postImageView = (SquareImageView) view.findViewById(R.id.postImageView);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                    onClickListener.onItemClick(getAdapterPosition(), v);
                }
            }
        });
    }

    public void bindData(Post post) {

        String imageUrl = post.getImagePath();

        Glide.with(context)
                .load(imageUrl)
                .apply(RequestOptions.centerCropTransform()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_stub))
                .transition(withCrossFade())
                .into(postImageView);

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
                            .load(obj.getPhotoUrl())
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)
                            .centerCrop())
                            .transition(withCrossFade())
                            .into(authorImageView);
                }
            }
        };
    }


    public interface OnClickListener {
        void onItemClick(int position, View view);

        void onLikeClick(LikeController likeController, int position);

        void onAuthorClick(int position, View view);
    }
}