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

package com.proyekakhir.realapps.activities;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Transition;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;
import com.proyekakhir.realapps.R;
import com.proyekakhir.realapps.adapters.CommentsAdapter;
import com.proyekakhir.realapps.controllers.LikeController;
import com.proyekakhir.realapps.dialogs.EditCommentDialog;
import com.proyekakhir.realapps.enums.PostStatus;
import com.proyekakhir.realapps.enums.ProfileStatus;
import com.proyekakhir.realapps.listeners.CustomTransitionListener;
import com.proyekakhir.realapps.managers.CommentManager;
import com.proyekakhir.realapps.managers.PostManager;
import com.proyekakhir.realapps.managers.ProfileManager;
import com.proyekakhir.realapps.managers.listeners.OnDataChangedListener;
import com.proyekakhir.realapps.managers.listeners.OnObjectChangedListener;
import com.proyekakhir.realapps.managers.listeners.OnObjectExistListener;
import com.proyekakhir.realapps.managers.listeners.OnPostChangedListener;
import com.proyekakhir.realapps.managers.listeners.OnTaskCompleteListener;
import com.proyekakhir.realapps.model.Comment;
import com.proyekakhir.realapps.model.Like;
import com.proyekakhir.realapps.model.Post;
import com.proyekakhir.realapps.model.Profile;
import com.proyekakhir.realapps.utils.FormatterUtil;
import com.proyekakhir.realapps.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PostDetailsActivity extends BaseActivity implements EditCommentDialog.CommentDialogCallback {

    public static final String POST_ID_EXTRA_KEY = "PostDetailsActivity.POST_ID_EXTRA_KEY";
    public static final String AUTHOR_ANIMATION_NEEDED_EXTRA_KEY = "PostDetailsActivity.AUTHOR_ANIMATION_NEEDED_EXTRA_KEY";
    private static final int TIME_OUT_LOADING_COMMENTS = 30000;
    public static final int UPDATE_POST_REQUEST = 1;
    public static final String POST_STATUS_EXTRA_KEY = "PostDetailsActivity.POST_STATUS_EXTRA_KEY";
    private static final String TAG = "PostDetailsActivity";

    private EditText commentEditText;
    @Nullable
    private Post post;
    private ScrollView scrollView;
    private ViewGroup likesContainer;
    private ImageView likesImageView;
    private TextView commentsLabel;
    private TextView likeCounterTextView;
    private TextView commentsCountTextView;
    private TextView watcherCounterTextView;
    private TextView authorTextView;
    private TextView dateTextView;
    private ImageView authorImageView;
    private ProgressBar progressBar;
    private ImageView postImageView;
    private TextView titleTextView;
    private TextView descriptionEditText;
    private ProgressBar commentsProgressBar;
    private RecyclerView commentsRecyclerView;
    private TextView warningCommentsTextView;
    private VrPanoramaView vrPanoramaView;
    private VrVideoView vrVideoView;
    private FrameLayout label360;
    private FrameLayout labelVideo;

    private TextView statusText;
    private SeekBar seekBar;
    private ImageButton volumeToggle,playToggle;
    private LinearLayout linearSeekbar;

    private RequestOptions cropOptions;

    private boolean attemptToLoadComments = false;

    private MenuItem complainActionMenuItem;
    private MenuItem editActionMenuItem;
    private MenuItem deleteActionMenuItem;
    private MenuItem downloadActionMenuItem;

    private String postId;

    private PostManager postManager;
    private CommentManager commentManager;
    private ProfileManager profileManager;
    private LikeController likeController;
    private boolean postRemovingProcess = false;
    private boolean isPostExist;
    private boolean authorAnimationInProgress = false;

    private boolean isAuthorAnimationRequired;
    private CommentsAdapter commentsAdapter;
    private ActionMode mActionMode;
    private boolean isEnterTransitionFinished = false;
    private boolean statePureTouch = false;
    private boolean isMuted;
    private boolean isPaused = false;

    private ImageLoaderTask imageLoaderTask;
    private VideoLoaderTask backgroundVideoLoaderTask;

    private StorageReference imageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        cropOptions = new RequestOptions().centerCrop();
        profileManager = ProfileManager.getInstance(this);
        postManager = PostManager.getInstance(this);
        commentManager = CommentManager.getInstance(this);
        imageRef = FirebaseStorage.getInstance().getReference().child("images");

        isAuthorAnimationRequired = getIntent().getBooleanExtra(AUTHOR_ANIMATION_NEEDED_EXTRA_KEY, false);
        postId = getIntent().getStringExtra(POST_ID_EXTRA_KEY);

        incrementWatchersCount();

        labelVideo = (FrameLayout) findViewById(R.id.label_layout_post_video);
        label360 = (FrameLayout) findViewById(R.id.label_layout);
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        descriptionEditText = (TextView) findViewById(R.id.descriptionEditText);
        postImageView = (ImageView) findViewById(R.id.postImageView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        commentsRecyclerView = (RecyclerView) findViewById(R.id.commentsRecyclerView);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        commentsLabel = (TextView) findViewById(R.id.commentsLabel);
        commentEditText = (EditText) findViewById(R.id.commentEditText);
        likesContainer = (ViewGroup) findViewById(R.id.likesContainer);
        likesImageView = (ImageView) findViewById(R.id.likesImageView);
        authorImageView = (ImageView) findViewById(R.id.authorImageView);
        authorTextView = (TextView) findViewById(R.id.authorTextView);
        likeCounterTextView = (TextView) findViewById(R.id.likeCounterTextView);
        commentsCountTextView = (TextView) findViewById(R.id.commentsCountTextView);
        watcherCounterTextView = (TextView) findViewById(R.id.watcherCounterTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        commentsProgressBar = (ProgressBar) findViewById(R.id.commentsProgressBar);
        warningCommentsTextView = (TextView) findViewById(R.id.warningCommentsTextView);
        vrPanoramaView = (VrPanoramaView) findViewById(R.id.panoImageView);

        //vrVideo widget
        statusText =(TextView) findViewById(R.id.status_text);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        volumeToggle = (ImageButton) findViewById(R.id.volume_toggle);
        linearSeekbar =(LinearLayout) findViewById(R.id.linearSeekbar);
        playToggle = (ImageButton) findViewById(R.id.play_toggle);
        seekBar.setOnSeekBarChangeListener(new PostDetailsActivity.SeekBarListener());
        vrVideoView = (VrVideoView) findViewById(R.id.panoVideoView);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isAuthorAnimationRequired) {
            authorImageView.setScaleX(0);
            authorImageView.setScaleY(0);

            // Add a listener to get noticed when the transition ends to animate the fab button
            getWindow().getSharedElementEnterTransition().addListener(new CustomTransitionListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);
                    //disable execution for exit transition
                    if (!isEnterTransitionFinished) {
                        isEnterTransitionFinished = true;
                        com.proyekakhir.realapps.utils.AnimationUtils.showViewByScale(authorImageView)
                                .setListener(authorAnimatorListener)
                                .start();
                    }
                }
            });
        }

        final Button sendButton = (Button) findViewById(R.id.sendButton);

        initRecyclerView();

        postManager.getPost(this, postId, createOnPostChangeListener());


        postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageDetailScreen();
            }
        });

        commentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                sendButton.setEnabled(charSequence.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasInternetConnection()) {
                    ProfileStatus profileStatus = ProfileManager.getInstance(PostDetailsActivity.this).checkProfile();

                    if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
                        sendComment();
                    } else {
                        doAuthorization(profileStatus);
                    }
                } else {
                    showSnackBar(R.string.internet_connection_failed);
                }
            }
        });

        commentsCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollToFirstComment();
            }
        });

        View.OnClickListener onAuthorClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post != null) {
                    openProfileActivity(post.getAuthorId(), v);
                }
            }
        };

        vrPanoramaView.setEventListener(new VrPanoramaEventListener(){
            @Override
            public void onLoadSuccess() {
                super.onLoadSuccess();
            }

            @Override
            public void onLoadError(String errorMessage) {
                super.onLoadError(errorMessage);
            }

            @Override
            public void onClick() {
                int display = vrPanoramaView.getDisplayMode();
                switch (display){
                    case 1:
                        vrPanoramaView.setDisplayMode(2);
                        break;
                    case 2:
                        if(!statePureTouch){
                            vrPanoramaView.setPureTouchTracking(true);
                            statePureTouch = true;
                        }else {
                            vrPanoramaView.setPureTouchTracking(false);
                            statePureTouch = false;
                        }
                        break;
                    default:
                }
            }

            @Override
            public void onDisplayModeChanged(int newDisplayMode) {
                switch (newDisplayMode){
                    case 1:
                        vrPanoramaView.setPureTouchTracking(true);
                        vrPanoramaView.setStereoModeButtonEnabled(false);
                        vrPanoramaView.setFullscreenButtonEnabled(false);
                        break;
                    case 2:
                        vrPanoramaView.setPureTouchTracking(false);
                        vrPanoramaView.setStereoModeButtonEnabled(true);
                        vrPanoramaView.setFullscreenButtonEnabled(false);
                        break;
                    case 3:
                        vrPanoramaView.setFullscreenButtonEnabled(true);
                        vrPanoramaView.setStereoModeButtonEnabled(false);
                        vrPanoramaView.setPureTouchTracking(false);
                        break;
                    default:
                        return;
                }
                super.onDisplayModeChanged(newDisplayMode);
            }
        });

        vrVideoView.setEventListener(new VrVideoEventListener(){
            @Override
            public void onCompletion() {

                vrVideoView.seekTo(0);
            }

            @Override
            public void onNewFrame() {
                updateStatusText();
                seekBar.setProgress((int) vrVideoView.getCurrentPosition());
            }

            @Override
            public void onLoadSuccess() {
                seekBar.setMax((int) vrVideoView.getDuration());
                updateStatusText();
            }

            @Override
            public void onLoadError(String errorMessage) {
                super.onLoadError(errorMessage);
            }

            @Override
            public void onClick() {
                int display = vrVideoView.getDisplayMode();
                switch (display){
                    case 1:
                        vrVideoView.setDisplayMode(2);
                        break;
                    case 2:
                        if(!statePureTouch){
                            vrVideoView.setPureTouchTracking(true);
                            statePureTouch = true;
                        }else {
                            vrVideoView.setPureTouchTracking(false);
                            statePureTouch = false;
                        }
                        break;
                    default:
                }
            }

            @Override
            public void onDisplayModeChanged(int newDisplayMode) {
                switch (newDisplayMode){
                    case 1:
                        vrVideoView.setPureTouchTracking(true);
                        vrVideoView.setStereoModeButtonEnabled(false);
                        break;
                    case 2:
                        vrVideoView.setPureTouchTracking(false);
                        vrVideoView.setStereoModeButtonEnabled(true);
                        break;
                    case 3:
                        vrVideoView.setPureTouchTracking(false);
                        vrVideoView.setStereoModeButtonEnabled(false);
                        break;
                    default:
                        return;
                }
            }
        });

        playToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePause();
            }
        });

        volumeToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIsMuted(!isMuted);
            }
        });

        authorImageView.setOnClickListener(onAuthorClickListener);

        authorTextView.setOnClickListener(onAuthorClickListener);

        supportPostponeEnterTransition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        postManager.closeListeners(this);
        vrPanoramaView.shutdown();
        vrVideoView.shutdown();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    hideKeyBoard();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isAuthorAnimationRequired) {
            if (!authorAnimationInProgress) {
                ViewPropertyAnimator hideAuthorAnimator = com.proyekakhir.realapps.utils.AnimationUtils.hideViewByScale(authorImageView);
                hideAuthorAnimator.setListener(authorAnimatorListener);
                hideAuthorAnimator.withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        PostDetailsActivity.super.onBackPressed();
                    }
                });
            }

        } else {
            super.onBackPressed();
        }
    }

    private void initRecyclerView() {
        commentsAdapter = new CommentsAdapter();
        commentsAdapter.setCallback(new CommentsAdapter.Callback() {
            @Override
            public void onLongItemClick(View view, int position) {
                Comment selectedComment = commentsAdapter.getItemByPosition(position);
                startActionMode(selectedComment);
            }

            @Override
            public void onAuthorClick(String authorId, View view) {
                openProfileActivity(authorId, view);
            }
        });
        commentsRecyclerView.setAdapter(commentsAdapter);
        commentsRecyclerView.setNestedScrollingEnabled(false);
        commentsRecyclerView.addItemDecoration(new DividerItemDecoration(commentsRecyclerView.getContext(),
                ((LinearLayoutManager) commentsRecyclerView.getLayoutManager()).getOrientation()));

        commentManager.getCommentsList(this, postId, createOnCommentsChangedDataListener());
    }

    private void startActionMode(Comment selectedComment) {
        if (mActionMode != null) {
            return;
        }

        //check access to modify or remove post
        if (hasAccessToEditComment(selectedComment.getAuthorId()) || hasAccessToModifyPost()) {
            mActionMode = startSupportActionMode(new ActionModeCallback(selectedComment));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        vrPanoramaView.resumeRendering();
        vrVideoView.resumeRendering();
        // Update the text to account for the paused video in onPause().
        updateStatusText();
    }

    @Override
    protected void onPause() {
        vrPanoramaView.pauseRendering();
        super.onPause();
        // Prevent the view from rendering continuously when in the background.
        vrVideoView.pauseRendering();
        // If the video is playing when onPause() is called, the default behavior will be to pause
        // the video and keep it paused when onResume() is called.
        isPaused = true;
        playToggle.setImageResource(isPaused ? R.drawable.ic_play_arrow_white_24px : R.drawable.ic_pause_white_24px);
    }

    private OnPostChangedListener createOnPostChangeListener() {
        return new OnPostChangedListener() {
            @Override
            public void onObjectChanged(Post obj) {
                if (obj != null) {
                    post = obj;
                    afterPostLoaded();
                } else if (!postRemovingProcess) {
                    isPostExist = false;
                    Intent intent = getIntent();
                    setResult(RESULT_OK, intent.putExtra(POST_STATUS_EXTRA_KEY, PostStatus.REMOVED));
                    showPostWasRemovedDialog();
                }
            }

            @Override
            public void onError(String errorText) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailsActivity.this);
                builder.setMessage(errorText);
                builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        };
    }

    private void afterPostLoaded() {
        isPostExist = true;
        initLikes();
        fillPostFields();
        updateCounters();
        initLikeButtonState();
        updateOptionMenuVisibility();
    }

    private void incrementWatchersCount() {
        postManager.incrementWatchersCount(postId);
        Intent intent = getIntent();
        setResult(RESULT_OK, intent.putExtra(POST_STATUS_EXTRA_KEY, PostStatus.UPDATED));
    }

    private void showPostWasRemovedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailsActivity.this);
        builder.setMessage(R.string.error_post_was_removed);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void scrollToFirstComment() {
        if (post != null && post.getCommentsCount() > 0) {
            scrollView.smoothScrollTo(0, commentsLabel.getTop());
        }
    }

    private void fillPostFields() {
        if (post != null) {
            titleTextView.setText(post.getTitle());
            descriptionEditText.setText(post.getDescription());

            loadPostDetailsImage();
            loadAuthorImage();
        }
    }

    @SuppressLint("CheckResult")
    private void loadPostDetailsImage() {
        if (post == null) {
            return;
        }

        if(post.isStateVideo360()){
            postImageView.setVisibility(View.GONE);
            vrVideoView.setVisibility(View.VISIBLE);
            labelVideo.setVisibility(View.VISIBLE);
            label360.setVisibility(View.VISIBLE);
            statusText.setVisibility(View.VISIBLE);
            linearSeekbar.setVisibility(View.VISIBLE);
            final VrVideoView.Options opt = new VrVideoView.Options();
            vrVideoView.setInfoButtonEnabled(false);
            vrVideoView.setStereoModeButtonEnabled(false);
            vrVideoView.setFullscreenButtonEnabled(false);
            vrVideoView.setPureTouchTracking(true);

            scheduleStartPostponedTransitionVrPanoramaVideo(vrVideoView);



            if (backgroundVideoLoaderTask != null) {
                // Cancel any task from a previous intent sent to this activity.
                backgroundVideoLoaderTask.cancel(true);
            }

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    backgroundVideoLoaderTask = new VideoLoaderTask();
                    backgroundVideoLoaderTask.execute(Pair.create(Uri.parse(post.getImagePath()),opt));
                }
            }, 500);
        }

        if(post.isState360()){
            postImageView.setVisibility(View.GONE);
            label360.setVisibility(View.VISIBLE);
            final VrPanoramaView.Options opt = new VrPanoramaView.Options();
            opt.inputType = VrPanoramaView.Options.TYPE_MONO;
            vrPanoramaView.setInfoButtonEnabled(false);
            vrPanoramaView.setStereoModeButtonEnabled(false);
            vrPanoramaView.setFullscreenButtonEnabled(false);
            vrPanoramaView.setPureTouchTracking(true);

            Glide.with(this)
                    .asBitmap()
                    .load(post.getImagePath())
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                            imageLoaderTask = new ImageLoaderTask();
                            imageLoaderTask.execute(Pair.create(resource,opt));
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            scheduleStartPostponedTransitionVrPanorama(vrPanoramaView);
                            super.onLoadFailed(errorDrawable);
                        }
                    });
        }else{
            String imageUrl = post.getImagePath();
            int width = Utils.getDisplayWidth(this);
            int height = (int) getResources().getDimension(R.dimen.post_detail_image_height);
            Glide.with(this)
                    .load(imageUrl)
                    .apply(cropOptions.override(width, height).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.ic_stub).centerCrop())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            scheduleStartPostponedTransition(postImageView);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            scheduleStartPostponedTransition(postImageView);
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    }).transition(withCrossFade())
                    .into(postImageView);
        }
    }

    class ImageLoaderTask extends AsyncTask<Pair<Bitmap, VrPanoramaView.Options>, Integer, Boolean> {

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }

        /**
         * Reads the bitmap from disk in the background and waits until it's loaded by pano widget.
         */
        @Override
        protected Boolean doInBackground(Pair<Bitmap, VrPanoramaView.Options>... fileInformation) {
            vrPanoramaView.loadImageFromBitmap(fileInformation[0].first, fileInformation[0].second);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            scheduleStartPostponedTransitionVrPanorama(vrPanoramaView);
            progressBar.setVisibility(View.GONE);
            vrPanoramaView.setVisibility(View.VISIBLE);
        }
    }

    private void scheduleStartPostponedTransition(final ImageView imageView) {
        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                supportStartPostponedEnterTransition();
                return true;
            }
        });
    }

    private void scheduleStartPostponedTransitionVrPanorama(final VrPanoramaView imageView) {
        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                supportStartPostponedEnterTransition();
                return true;
            }
        });
    }

    private void scheduleStartPostponedTransitionVrPanoramaVideo(final VrVideoView imageView) {
        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                supportStartPostponedEnterTransition();
                return true;
            }
        });
    }

    private void loadAuthorImage() {
        if (post != null && post.getAuthorId() != null) {
            profileManager.getProfileSingleValue(post.getAuthorId(), createProfileChangeListener());
        }
    }

    private void updateCounters() {
        if (post == null) {
            return;
        }

        long commentsCount = post.getCommentsCount();
        commentsCountTextView.setText(String.valueOf(commentsCount));
        commentsLabel.setText(String.format(getString(R.string.label_comments), commentsCount));
        likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
        likeController.setUpdatingLikeCounter(false);

        watcherCounterTextView.setText(String.valueOf(post.getWatchersCount()));

        CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(this, post.getCreatedDate());
        dateTextView.setText(date);

        if (commentsCount == 0) {
            commentsLabel.setVisibility(View.GONE);
            commentsProgressBar.setVisibility(View.GONE);
        } else if (commentsLabel.getVisibility() != View.VISIBLE) {
            commentsLabel.setVisibility(View.VISIBLE);
        }
    }

    private OnObjectChangedListener<Profile> createProfileChangeListener() {
        return obj -> {
            if (obj.getPhotoUrl() != null) {
                Glide.with(PostDetailsActivity.this)
                        .asBitmap()
                        .load(obj.getPhotoUrl())
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE)
                                .override(80, 80)
                                .error(R.drawable.ic_stub))
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                                authorImageView.setImageBitmap(resource);
                            }
                        });
            }

            authorTextView.setText(obj.getUsername());
        };
    }

    private OnDataChangedListener<Comment> createOnCommentsChangedDataListener() {
        attemptToLoadComments = true;

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (attemptToLoadComments) {
                commentsProgressBar.setVisibility(View.GONE);
                warningCommentsTextView.setVisibility(View.VISIBLE);
            }
        }, TIME_OUT_LOADING_COMMENTS);


        return new OnDataChangedListener<Comment>() {
            @Override
            public void onListChanged(List<Comment> list) {
                attemptToLoadComments = false;
                commentsProgressBar.setVisibility(View.GONE);
                commentsRecyclerView.setVisibility(View.VISIBLE);
                warningCommentsTextView.setVisibility(View.GONE);
                commentsAdapter.setList(list);
            }
        };
    }

    private void openImageDetailScreen() {
        if (post != null) {
            Intent intent = new Intent(this, ImageDetailActivity.class);
            intent.putExtra(ImageDetailActivity.IMAGE_URL_EXTRA_KEY, post.getImagePath());
            startActivity(intent);
        }
    }

    private void openProfileActivity(String userId, View view) {
        Intent intent = new Intent(PostDetailsActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(PostDetailsActivity.this,
                            new android.util.Pair<>(view, getString(R.string.post_author_image_transition_name)));
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    private OnObjectExistListener<Like> createOnLikeObjectExistListener() {
        return exist -> likeController.initLike(exist);
    }

    private void initLikeButtonState() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && post != null) {
            postManager.hasCurrentUserLike(this, post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
        }
    }

    private void initLikes() {
        likeController = new LikeController(this, post, likeCounterTextView, likesImageView, false);

        likesContainer.setOnClickListener(v -> {
            if (isPostExist) {
                likeController.handleLikeClickAction(PostDetailsActivity.this, post);
            }
        });
    }

    private void sendComment() {
        if (post == null) {
            return;
        }

        String commentText = commentEditText.getText().toString();

        if (commentText.length() > 0 && isPostExist) {
            commentManager.createOrUpdateComment(post.getAuthorId(),commentText, post.getId(), new OnTaskCompleteListener() {
                @Override
                public void onTaskComplete(boolean success) {
                    if (success) {
                        scrollToFirstComment();
                    }
                }
            });
            commentEditText.setText(null);
            commentEditText.clearFocus();
            hideKeyBoard();
        }
    }

    private void hideKeyBoard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private boolean hasAccessToEditComment(String commentAuthorId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null && commentAuthorId.equals(currentUser.getUid());
    }

    private boolean hasAccessToModifyPost() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null && post != null && post.getAuthorId().equals(currentUser.getUid());
    }

    private void updateOptionMenuVisibility() {
        if (editActionMenuItem != null && deleteActionMenuItem != null && hasAccessToModifyPost()) {
            editActionMenuItem.setVisible(true);
            deleteActionMenuItem.setVisible(true);
        }

        if (complainActionMenuItem != null && post != null && !post.isHasComplain()) {
            complainActionMenuItem.setVisible(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.post_details_menu, menu);
        complainActionMenuItem = menu.findItem(R.id.complain_action);
        editActionMenuItem = menu.findItem(R.id.edit_post_action);
        deleteActionMenuItem = menu.findItem(R.id.delete_post_action);
        downloadActionMenuItem = menu.findItem(R.id.download_post_action);

        if (post != null) {
            updateOptionMenuVisibility();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isPostExist) {
            return super.onOptionsItemSelected(item);
        }

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.complain_action:
                doComplainAction();
                return true;

            case R.id.edit_post_action:
                if (hasAccessToModifyPost()) {
                    openEditPostActivity();
                }
                return true;

            case R.id.delete_post_action:
                if (hasAccessToModifyPost()) {
                    attemptToRemovePost();
                }
                return true;
            case R.id.download_post_action:

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (!checkIfAlreadyhavePermission()) {
                        requestForSpecificPermission();
                    }else {
                        downloadAction();
                    }
                }


        }

        return super.onOptionsItemSelected(item);
    }

    private void downloadAction(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        if(post!=null){
            if(post.isStateVideo360()){
                alert.setTitle("File will be save in ../Videos");
                alert.setMessage("Enter file name");
            }else {
                alert.setTitle("File will be save in ../Pictures");
                alert.setMessage("Enter file name");
            }

            final EditText input = new EditText(PostDetailsActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);

            input.setHint("File name");

            alert.setView(input);

            alert.setPositiveButton("Ok", (dialog, whichButton) -> {
                //Your action here
                String name = input.getText().toString();
                downloadToLocalFile(imageRef.child(post.getImageTitle()),name);
            });

            alert.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });

            alert.show();
        }
    }

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
    }

    private void doComplainAction() {
        ProfileStatus profileStatus = profileManager.checkProfile();

        if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
            openComplainDialog();
        } else {
            doAuthorization(profileStatus);
        }
    }

    private void attemptToRemovePost() {
        if (hasInternetConnection()) {
            if (!postRemovingProcess) {
                openConfirmDeletingDialog();
            }
        } else {
            showSnackBar(R.string.internet_connection_failed);
        }
    }

    private void removePost() {
        postManager.removePost(post, new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(boolean success) {
                if (success) {
                    Intent intent = getIntent();
                    setResult(RESULT_OK, intent.putExtra(POST_STATUS_EXTRA_KEY, PostStatus.REMOVED));
                    finish();
                } else {
                    postRemovingProcess = false;
                    showSnackBar(R.string.error_fail_remove_post);
                }

                hideProgress();
            }
        });

        showProgress(R.string.removing);
        postRemovingProcess = true;
    }

    private void openEditPostActivity() {
        if (hasInternetConnection()) {
            Intent intent = new Intent(PostDetailsActivity.this, EditPostActivity.class);
            intent.putExtra(EditPostActivity.POST_EXTRA_KEY, post);
            startActivityForResult(intent, EditPostActivity.EDIT_POST_REQUEST);
        } else {
            showSnackBar(R.string.internet_connection_failed);
        }
    }

    private void openConfirmDeletingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_deletion_post)
                .setNegativeButton(R.string.button_title_cancel, null)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removePost();
                    }
                });

        builder.create().show();
    }

    private void openComplainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_complain)
                .setMessage(R.string.complain_text)
                .setNegativeButton(R.string.button_title_cancel, null)
                .setPositiveButton(R.string.add_complain, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        addComplain();
                    }
                });

        builder.create().show();
    }

    private void addComplain() {
        postManager.addComplain(post);
        complainActionMenuItem.setVisible(false);
        showSnackBar(R.string.complain_sent);
    }

    private void removeComment(String commentId, final ActionMode mode, final int position) {
        showProgress();
        commentManager.removeComment(commentId, postId, new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(boolean success) {
                hideProgress();
                mode.finish(); // Action picked, so close the CAB
                showSnackBar(R.string.message_comment_was_removed);
            }
        });
    }

    private void openEditCommentDialog(Comment comment) {
        EditCommentDialog editCommentDialog = new EditCommentDialog();
        Bundle args = new Bundle();
        args.putString(EditCommentDialog.COMMENT_TEXT_KEY, comment.getText());
        args.putString(EditCommentDialog.COMMENT_ID_KEY, comment.getId());
        editCommentDialog.setArguments(args);
        editCommentDialog.show(getFragmentManager(), EditCommentDialog.TAG);
    }

    private void updateComment(String newText, String commentId) {
        showProgress();
        commentManager.updateComment(commentId, newText, postId, new OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(boolean success) {
                hideProgress();
                showSnackBar(R.string.message_comment_was_edited);
            }
        });
    }

    @Override
    public void onCommentChanged(String newText, String commentId) {
        updateComment(newText, commentId);
    }

    private class ActionModeCallback implements ActionMode.Callback {
        Comment selectedComment;
        int position;

        ActionModeCallback(Comment selectedComment) {
            this.selectedComment = selectedComment;
        }

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.comment_context_menu, menu);

            menu.findItem(R.id.editMenuItem).setVisible(hasAccessToEditComment(selectedComment.getAuthorId()));

            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.editMenuItem:
                    openEditCommentDialog(selectedComment);
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.deleteMenuItem:
                    removeComment(selectedComment.getId(), mode, position);
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    }
    Animator.AnimatorListener authorAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            authorAnimationInProgress = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            authorAnimationInProgress = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            authorAnimationInProgress = false;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    private void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
        volumeToggle.setImageResource(isMuted ? R.drawable.volume_off : R.drawable.volume_on);
        vrVideoView.setVolume(isMuted ? 0.0f : 1.0f);
    }

    private void togglePause() {
        if (isPaused) {
            vrVideoView.playVideo();
        } else {
            vrVideoView.pauseVideo();
        }
        isPaused = !isPaused;
        playToggle.setImageResource(isPaused ? R.drawable.ic_play_arrow_white_24px : R.drawable.ic_pause_white_24px);
        updateStatusText();
    }

    private void updateStatusText() {
        StringBuilder status = new StringBuilder();
        status.append(isPaused ? "Paused: " : "Playing: ");
        status.append(String.format("%.2f", vrVideoView.getCurrentPosition() / 1000f));
        status.append(" / ");
        status.append(vrVideoView.getDuration() / 1000f);
        status.append(" seconds.");
        statusText.setText(status.toString());
    }

    class VideoLoaderTask extends AsyncTask<Pair<Uri, VrVideoView.Options>, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Pair<Uri, VrVideoView.Options>... fileInformation) {
            try {
                if (fileInformation == null || fileInformation.length < 1
                        || fileInformation[0] == null || fileInformation[0].first == null) {
                    // No intent was specified, so we default to playing the local stereo-over-under video.

                } else {

                    vrVideoView.loadVideo(fileInformation[0].first, fileInformation[0].second);
                }
            } catch (IOException e) {
                // An error here is normally due to being unable to locate the file.
                //loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
                // Since this is a background thread, we need to switch to the main thread to show a toast.
                vrVideoView.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast
                                .makeText(PostDetailsActivity.this, "Error opening file. ", Toast.LENGTH_LONG)
                                .show();
                    }
                });
                Log.e(TAG, "Could not open video: " + e);
            }

            return true;
        }
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                vrVideoView.seekTo(progress);
                updateStatusText();
            } // else this was from the ActivityEventHandler.onNewFrame()'s seekBar.setProgress update.
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }
    }

    private void downloadToLocalFile(StorageReference fileRef,String name) {
        if (fileRef != null) {
            showProgress(R.string.message_downloading_post);
            File rootPath;
            if(post.isStateVideo360()){
                rootPath = new File(Environment.getExternalStorageDirectory(), "Videos");
            }else {
                rootPath = new File(Environment.getExternalStorageDirectory(), "Pictures");
            }

            if(!rootPath.exists()) {
                rootPath.mkdirs();
            }
            final File localFile;
            if(!post.isStateVideo360())
                localFile = new File(rootPath, name+".jpg");
            else
                localFile = new File(rootPath,name+".mp4");

            fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "onSuccess: "+localFile.toString());
                    progressDialog.dismiss();
                    showSnackBar(R.string.download_successful);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    progressDialog.dismiss();
                    Toast.makeText(PostDetailsActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // progress percentage
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    // percentage in progress dialog
                    progressDialog.setMessage("Downloaded " + ((int) progress) + "%...");
                }
            });
        } else {
            Toast.makeText(PostDetailsActivity.this, "Upload file before downloading", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadAction();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(PostDetailsActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
