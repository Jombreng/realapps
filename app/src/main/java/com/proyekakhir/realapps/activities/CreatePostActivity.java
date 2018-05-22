/*
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.proyekakhir.realapps.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;
import com.proyekakhir.realapps.R;
import com.proyekakhir.realapps.managers.PostManager;
import com.proyekakhir.realapps.managers.listeners.OnPostCreatedListener;
import com.proyekakhir.realapps.model.Post;
import com.proyekakhir.realapps.utils.LogUtil;
import com.proyekakhir.realapps.utils.ValidationUtil;

public class CreatePostActivity extends PickImageActivity implements OnPostCreatedListener {
    private static final String TAG = CreatePostActivity.class.getSimpleName();
    public static final int CREATE_NEW_POST_REQUEST = 11;

    protected ImageView imageView;
    protected ProgressBar progressBar;
    protected EditText titleEditText;
    protected EditText descriptionEditText;
    protected VrPanoramaView imagePanoView;
    protected RadioButton radioButtonPublic;
    protected RadioButton radioButtonPrivate;
    protected VrVideoView videoPanoView;
    protected TextView statusText;
    protected SeekBar seekBar;
    protected ImageButton volumeToggle,playToggle;
    protected LinearLayout linearSeekbar;

    protected PostManager postManager;
    protected boolean creatingPost = false;
    protected boolean statePureTouch = true;
    protected boolean isPrivate = false;
    protected boolean isMuted;
    protected boolean isPaused = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_post_activity);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        postManager = PostManager.getInstance(CreatePostActivity.this);

        //video Widget
        videoPanoView = (VrVideoView) findViewById(R.id.panoVideoView);
        statusText =(TextView) findViewById(R.id.status_text);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        volumeToggle = (ImageButton) findViewById(R.id.volume_toggle);
        linearSeekbar =(LinearLayout) findViewById(R.id.linearSeekbar);
        playToggle = (ImageButton) findViewById(R.id.play_toggle);
        seekBar.setOnSeekBarChangeListener(new SeekBarListener());

        radioButtonPublic = (RadioButton) findViewById(R.id.radio_public);
        radioButtonPrivate = (RadioButton) findViewById(R.id.radio_private);
        radioButtonPublic.setChecked(true);
        titleEditText = (EditText) findViewById(R.id.titleEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        imageView = findViewById(R.id.imageView);
        imagePanoView = findViewById(R.id.panoImageView);

        imageView.setOnClickListener(v -> onSelectImageClick(v,"post"));

        videoPanoView.setEventListener(new VrVideoEventListener(){
            @Override
            public void onCompletion() {

                videoPanoView.seekTo(0);
            }

            @Override
            public void onNewFrame() {
                updateStatusText();
                seekBar.setProgress((int) videoPanoView.getCurrentPosition());
            }

            @Override
            public void onLoadSuccess() {
                seekBar.setMax((int) videoPanoView.getDuration());
                updateStatusText();
            }

            @Override
            public void onLoadError(String errorMessage) {
                super.onLoadError(errorMessage);
            }

            @Override
            public void onClick() {
                int display = videoPanoView.getDisplayMode();
                switch (display){
                    case 1:
                        onSelectImageClick(null,"post");
                        break;
                    case 2:
                        if(!statePureTouch){
                            videoPanoView.setPureTouchTracking(true);
                            statePureTouch = true;
                        }else {
                            videoPanoView.setPureTouchTracking(false);
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
                        videoPanoView.setPureTouchTracking(true);
                        break;
                    case 2:
                        videoPanoView.setPureTouchTracking(false);
                        break;
                    case 3:
                        videoPanoView.setPureTouchTracking(false);
                        break;
                    default:
                        return;
                }
            }
        });

        playToggle.setOnClickListener(v -> togglePause());

        volumeToggle.setOnClickListener(v -> setIsMuted(!isMuted));

        titleEditText.setOnTouchListener((v, event) -> {
            if (titleEditText.hasFocus() && titleEditText.getError() != null) {
                titleEditText.setError(null);
                return true;
            }
            return false;
        });
        imagePanoView.setEventListener(new VrPanoramaEventListener() {
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
                int display = imagePanoView.getDisplayMode();
                switch (display){
                    case 1:
                        onSelectImageClick(null,"post");
                        break;
                    case 2:
                        if(!statePureTouch){
                            imagePanoView.setPureTouchTracking(true);
                            statePureTouch = true;
                        }else {
                            imagePanoView.setPureTouchTracking(false);
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
                        imagePanoView.setPureTouchTracking(true);
                        imagePanoView.setStereoModeButtonEnabled(false);
                        break;
                    case 2:
                        imagePanoView.setPureTouchTracking(false);
                        imagePanoView.setStereoModeButtonEnabled(true);
                        break;
                    case 3:
                        imagePanoView.setStereoModeButtonEnabled(false);
                        imagePanoView.setPureTouchTracking(false);
                        break;
                        default:
                            return;
                }
                if(newDisplayMode==2||newDisplayMode==3)
                    imagePanoView.setPureTouchTracking(false);
                else
                    imagePanoView.setPureTouchTracking(true);
                super.onDisplayModeChanged(newDisplayMode);
            }
        });
    }

    private void updateStatusText() {
        StringBuilder status = new StringBuilder();
        status.append(isPaused ? "Paused: " : "Playing: ");
        status.append(String.format("%.2f", videoPanoView.getCurrentPosition() / 1000f));
        status.append(" / ");
        status.append(videoPanoView.getDuration() / 1000f);
        status.append(" seconds.");
        statusText.setText(status.toString());
    }

    private void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
        volumeToggle.setImageResource(isMuted ? R.drawable.volume_off : R.drawable.volume_on);
        videoPanoView.setVolume(isMuted ? 0.0f : 1.0f);
    }

    private void togglePause() {
        if (isPaused) {
            videoPanoView.playVideo();
        } else {
            videoPanoView.pauseVideo();
        }
        isPaused = !isPaused;
        playToggle.setImageResource(isPaused ? R.drawable.ic_play_arrow_white_24px : R.drawable.ic_pause_white_24px);
        updateStatusText();
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.radio_public:
                if (checked)
                    isPrivate = false;
                    break;
            case R.id.radio_private:
                if (checked)
                    isPrivate = true;
                    break;
        }
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoPanoView.seekTo(progress);
                updateStatusText();
            } // else this was from the ActivityEventHandler.onNewFrame()'s seekBar.setProgress update.
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }
    }

    @Override
    protected void onDestroy() {
        getPanoImageView().shutdown();
        videoPanoView.shutdown();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        imagePanoView.pauseRendering();
        super.onPause();
        // Prevent the view from rendering continuously when in the background.
        videoPanoView.pauseRendering();
        // If the video is playing when onPause() is called, the default behavior will be to pause
        // the video and keep it paused when onResume() is called.
        isPaused = true;
        playToggle.setImageResource(isPaused ? R.drawable.ic_play_arrow_white_24px : R.drawable.ic_pause_white_24px);
    }

    @Override
    protected void onResume() {
        super.onResume();
        imagePanoView.resumeRendering();
        videoPanoView.resumeRendering();
        // Update the text to account for the paused video in onPause().
        updateStatusText();
    }


    @Override
    protected LinearLayout getLinearSeekbar() {
        return linearSeekbar;
    }

    @Override
    protected TextView getTextViewSeekbar() {
        return statusText;
    }

    @Override
    protected VrVideoView getVideoView() {
        videoPanoView.setPureTouchTracking(true);
        videoPanoView.setInfoButtonEnabled(false);
        return videoPanoView;
    }

    @Override
    public ProgressBar getProgressView() {
        return progressBar;
    }

    @Override
    public ImageView getImageView() {
        return imageView;
    }

    @Override
    protected VrPanoramaView getPanoImageView() {
        imagePanoView.setInfoButtonEnabled(false);//隐藏信息按钮
        imagePanoView.setStereoModeButtonEnabled(false);//隐藏cardboard按钮
        imagePanoView.setFullscreenButtonEnabled(true);//隐藏全屏按钮
        imagePanoView.setPureTouchTracking(true);
        return imagePanoView;
    }

    @Override
    public void onImagePikedAction() {
        loadImageToImageView();
    }

    protected void attemptCreatePost() {
        // Reset errors.
        titleEditText.setError(null);
        descriptionEditText.setError(null);

        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        View focusView = null;
        boolean cancel = false;

        if (TextUtils.isEmpty(description)) {
            descriptionEditText.setError(getString(R.string.warning_empty_description));
            focusView = descriptionEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(title)) {
            titleEditText.setError(getString(R.string.warning_empty_title));
            focusView = titleEditText;
            cancel = true;
        } else if (!ValidationUtil.isPostTitleValid(title)) {
            titleEditText.setError(getString(R.string.error_post_title_length));
            focusView = titleEditText;
            cancel = true;
        }

        if (!(this instanceof EditPostActivity) && imageUri == null) {
            showWarningDialog(R.string.warning_empty_image);
            focusView = imageView;
            cancel = true;
        }

        if (!cancel) {
            creatingPost = true;
            hideKeyboard();
            savePost(title, description);
        } else if (focusView != null) {
            focusView.requestFocus();
        }
    }

    protected void savePost(String title, String description) {

        showProgress(R.string.message_creating_post);
        Post post = new Post();
        post.setTitle(title);
        post.setDescription(description);
        post.setState360(is360);
        post.setStatePrivate(isPrivate);
        post.setAuthorId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        post.setStateVideo360(isVideo);

        postManager.createOrUpdatePostWithImage(imageUri, CreatePostActivity.this, post,progressDialog);
    }

    @Override
    public void onPostSaved(boolean success) {
        hideProgress();

        if (success) {
            setResult(RESULT_OK);
            CreatePostActivity.this.finish();
            LogUtil.logDebug(TAG, "Post was created");
        } else {
            creatingPost = false;
            showSnackBar(R.string.error_fail_create_post);
            LogUtil.logDebug(TAG, "Failed to create a post");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.post:
                if (!creatingPost) {
                    if (hasInternetConnection()) {
                        attemptCreatePost();
                    } else {
                        showSnackBar(R.string.internet_connection_failed);
                    }
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
