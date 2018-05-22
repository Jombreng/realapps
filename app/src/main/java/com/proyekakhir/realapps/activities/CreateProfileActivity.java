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
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.google.vr.sdk.widgets.video.VrVideoView;
import com.proyekakhir.realapps.R;
import com.proyekakhir.realapps.managers.DatabaseHelper;
import com.proyekakhir.realapps.managers.ProfileManager;
import com.proyekakhir.realapps.managers.listeners.OnProfileCreatedListener;
import com.proyekakhir.realapps.model.Profile;
import com.proyekakhir.realapps.utils.PreferencesUtil;
import com.proyekakhir.realapps.utils.ValidationUtil;

public class CreateProfileActivity extends PickImageActivity implements OnProfileCreatedListener {
    private static final String TAG = CreateProfileActivity.class.getSimpleName();
    public static final String LARGE_IMAGE_URL_EXTRA_KEY = "CreateProfileActivity.LARGE_IMAGE_URL_EXTRA_KEY";

    // UI references.
    private EditText nameEditText;
    private ImageView imageView;
    private ProgressBar progressBar;

    private Profile profile;
    private String largeAvatarURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set up the login form.
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imageView = (ImageView) findViewById(R.id.imageView);
        nameEditText = (EditText) findViewById(R.id.nameEditText);

        largeAvatarURL = getIntent().getStringExtra(LARGE_IMAGE_URL_EXTRA_KEY);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        profile = ProfileManager.getInstance(this).buildProfile(firebaseUser, largeAvatarURL);

        nameEditText.setText(profile.getUsername());

        if (profile.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(profile.getPhotoUrl())
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE)
                            .error(R.drawable.ic_stub))
                    .transition(withCrossFade())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(imageView);
        } else {
            progressBar.setVisibility(View.GONE);
            imageView.setImageResource(R.drawable.ic_stub);
        }

        imageView.setOnClickListener(v -> onSelectImageClick(v,"profile"));
    }


    @Override
    protected LinearLayout getLinearSeekbar() {
        return null;
    }

    @Override
    protected TextView getTextViewSeekbar() {
        return null;
    }

    @Override
    protected VrVideoView getVideoView() {
        return null;
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
        return null;
    }

    @Override
    public void onImagePikedAction() {
        startCropImageActivity();
    }

    private void attemptCreateProfile() {

        // Reset errors.
        nameEditText.setError(null);

        // Store values at the time of the login attempt.
        String name = nameEditText.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError(getString(R.string.error_field_required));
            focusView = nameEditText;
            cancel = true;
        } else if (!ValidationUtil.isNameValid(name)) {
            nameEditText.setError(getString(R.string.error_profile_name_length));
            focusView = nameEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress();
            profile.setUsername(name);
            ProfileManager.getInstance(this).createOrUpdateProfile(profile, imageUri, this);
        }
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        super.onActivityResult(requestCode, resultCode, data);
        handleCropImageResult(requestCode, resultCode, data);
    }

    @Override
    public void onProfileCreated(boolean success) {
        hideProgress();

        if (success) {
            finish();
            PreferencesUtil.setProfileCreated(this, success);
            DatabaseHelper.getInstance(CreateProfileActivity.this.getApplicationContext())
                    .addRegistrationToken(FirebaseInstanceId.getInstance().getToken(), profile.getId());
        } else {
            showSnackBar(R.string.error_fail_create_profile);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.continueButton:
                if (hasInternetConnection()) {
                    attemptCreateProfile();
                } else {
                    showSnackBar(R.string.internet_connection_failed);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
