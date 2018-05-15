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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.google.vr.sdk.widgets.video.VrVideoView;
import com.proyekakhir.realapps.Constants;
import com.proyekakhir.realapps.R;
import com.proyekakhir.realapps.utils.ImageFileFilter;
import com.proyekakhir.realapps.utils.LogUtil;
import com.proyekakhir.realapps.utils.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;

import iam.thevoid.mediapicker.cropper.CropArea;
import iam.thevoid.mediapicker.cropper.CropImage;
import iam.thevoid.mediapicker.rxmediapicker.Purpose;
import iam.thevoid.mediapicker.rxmediapicker.RxMediaPicker;


public abstract class PickImageActivity extends BaseActivity {
    private static final String TAG = PickImageActivity.class.getSimpleName();
    protected static final int MAX_FILE_SIZE_IN_BYTES = 10485760;   //10 Mb
    private static final String SAVED_STATE_IMAGE_URI = "RegistrationActivity.SAVED_STATE_IMAGE_URI";

    /**
     * Arbitrary constants and variable to track load status. In this example, this variable should
     * only be accessed on the UI thread. In a real app, this variable would be code that performs
     * some UI actions when the video is fully loaded.
     */
    public static final int LOAD_VIDEO_STATUS_UNKNOWN = 0;
    public static final int LOAD_VIDEO_STATUS_SUCCESS = 1;
    public static final int LOAD_VIDEO_STATUS_ERROR = 2;

    private int loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

    private VrVideoView.Options videoOptions = new VrVideoView.Options();
    private ImageLoaderTask imageLoaderTask;
    private VideoLoaderTask videoLoaderTask;
    protected Uri imageUri;
    protected Bitmap bitmap;
    protected boolean is360 = false;
    protected boolean isVideo = false;

    protected abstract LinearLayout getLinearSeekbar();

    protected abstract TextView getTextViewSeekbar();

    protected abstract VrVideoView getVideoView();

    protected abstract ProgressBar getProgressView();

    protected abstract ImageView getImageView();

    protected abstract VrPanoramaView getPanoImageView();

    protected abstract void onImagePikedAction();

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(SAVED_STATE_IMAGE_URI, imageUri);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SAVED_STATE_IMAGE_URI)) {
                imageUri = savedInstanceState.getParcelable(SAVED_STATE_IMAGE_URI);
                loadImageToImageView();
            }
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    @SuppressLint("NewApi")
    public void onSelectImageClick(View view,String from) {
        if(from=="post"){
            if (CropImage.isExplicitCameraPermissionRequired(this)) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
            } else {
                RxMediaPicker.builder(this)
                        .pick(Purpose.Pick.IMAGE, Purpose.Pick.VIDEO)
                        .take(Purpose.Take.PHOTO)
                        .build()
                        .subscribe(uri -> {
                            this.imageUri = uri;
                            try {
                                this.bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            loadImageToImageView();
                        });
            }
        }else {
            if (CropImage.isExplicitCameraPermissionRequired(this)) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
            } else {
                RxMediaPicker.builder(this)
                        .crop(CropArea.circle())
                        .pick(Purpose.Pick.IMAGE)
                        .take(Purpose.Take.PHOTO)
                        .build()
                        .subscribe(uri -> {
                            this.imageUri = uri;
                        });
            }
        }
    }

    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    protected void loadImageToImageView() {
        if (imageUri == null) {
            return;
        }

        if(bitmap==null){

            isVideo = true;
            is360 = true;
            videoOptions.inputType = VrVideoView.Options.TYPE_MONO;
            videoOptions.inputFormat = VrVideoView.Options.FORMAT_DEFAULT;

            videoLoaderTask = new VideoLoaderTask();
            videoLoaderTask.execute(Pair.create(imageUri,videoOptions));

            getImageView().setVisibility(View.GONE);
            getPanoImageView().setVisibility(View.GONE);
            getVideoView().setVisibility(View.VISIBLE);
            getLinearSeekbar().setVisibility(View.VISIBLE);
            getTextViewSeekbar().setVisibility(View.VISIBLE);

        }else {
            if(getDropboxIMGSize(bitmap)){

                is360 = true;
                VrPanoramaView.Options opt = new VrPanoramaView.Options();
                opt.inputType = VrPanoramaView.Options.TYPE_MONO;

                imageLoaderTask = new ImageLoaderTask();
                imageLoaderTask.execute(Pair.create(bitmap,opt));

                getVideoView().setVisibility(View.GONE);
                getPanoImageView().setVisibility(View.VISIBLE);
                getImageView().setVisibility(View.GONE);

            }else {
                Glide.with(this)
                        .load(imageUri).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .fitCenter())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                getProgressView().setVisibility(View.GONE);
                                LogUtil.logDebug(TAG, "Glide Success Loading image from uri : " + imageUri.getPath());
                                return false;
                            }
                        })
                        .into(getImageView());
            }
        }
    }

    private boolean getDropboxIMGSize(Bitmap bitmap){

        int imageHeight = bitmap.getHeight();
        int imageWidth = bitmap.getWidth();

        float RATIO = 2 / 1;
        float EPSILON = 0.00001f;
        float ratio = (float)imageWidth / imageHeight;
        if (Math.abs(ratio - RATIO) < EPSILON) {
            Log.e(TAG, "getDropboxIMGSize return true: "+bitmap.toString() );
            return true;
        }else {
            Log.e(TAG, "getDropboxIMGSize return false: "+bitmap.toString() );
            return false;
        }
    }

    protected boolean isImageFileValid(Uri imageUri) {
        int message = R.string.error_general;
        boolean result = false;

        if (imageUri != null) {
            if (ValidationUtil.isImage(imageUri, this)) {
                File imageFile = new File(imageUri.getPath());
                if (imageFile.length() > MAX_FILE_SIZE_IN_BYTES) {
                    message = R.string.error_bigger_file;
                } else {
                    result = true;
                }
            } else {
                message = R.string.error_incorrect_file_type;
            }
        }

        if (!result) {
            showSnackBar(message);
            getProgressView().setVisibility(View.GONE);
        }

        return result;
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            if (isImageFileValid(imageUri)) {
                this.imageUri = imageUri;
                try {
                    this.bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    Log.e(TAG, "onActivityResult: "+bitmap.toString() );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted
                RxMediaPicker.builder(this)
                        .crop(CropArea.circle())
                        .pick(Purpose.Pick.IMAGE)
                        .take(Purpose.Take.PHOTO)
                        .build()
                        .subscribe(uri -> {
                            this.imageUri = uri;
                        });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LogUtil.logDebug(TAG, "CAMERA_CAPTURE_PERMISSIONS granted");
                //CropImage.startPickImageActivity(this);
            } else {
                showSnackBar(R.string.permissions_not_granted);
                LogUtil.logDebug(TAG, "CAMERA_CAPTURE_PERMISSIONS not granted");
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (imageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                LogUtil.logDebug(TAG, "PICK_IMAGE_PERMISSIONS granted");

                RxMediaPicker.builder(this)
                        .crop(CropArea.circle())
                        .pick(Purpose.Pick.IMAGE)
                        .take(Purpose.Take.PHOTO)
                        .build()
                        .subscribe(uri -> {
                            this.imageUri = uri;
                        });

            } else {
                showSnackBar(R.string.permissions_not_granted);
                LogUtil.logDebug(TAG, "PICK_IMAGE_PERMISSIONS not granted");
            }
        }
    }

    protected void handleCropImageResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            if (resultCode == RESULT_OK) {
//                if (ValidationUtil.checkImageMinSize(result.getCropRect())) {
//                    imageUri = result.getUri();
//                    loadImageToImageView();
//                } else {
//                    showSnackBar(R.string.error_smaller_image);
//                }
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                LogUtil.logError(TAG, "crop image error", result.getError());
//                showSnackBar(R.string.error_fail_crop_image);
//            }
//        }
    }

    protected void startCropImageActivity() {
        if (imageUri == null) {
            return;
        }

        RxMediaPicker.builder(this)
                .crop(CropArea.circle())
                .pick(Purpose.Pick.IMAGE)
                .take(Purpose.Take.PHOTO)
                .build()
                .subscribe(uri -> {
                    this.imageUri = uri;
                });
    }

    /**
     * Helper class to manage threading.
     */
    @SuppressLint("StaticFieldLeak")
    class ImageLoaderTask extends AsyncTask<Pair<Bitmap, VrPanoramaView.Options>, Void, Boolean> {

        /**
         * Reads the bitmap from disk in the background and waits until it's loaded by pano widget.
         */
        @Override
        protected Boolean doInBackground(Pair<Bitmap, VrPanoramaView.Options>... fileInformation) {
            getPanoImageView().loadImageFromBitmap(fileInformation[0].first, fileInformation[0].second);
            return true;
        }
    }

    /**
     * Helper class to manage threading.
     */
    class VideoLoaderTask extends AsyncTask<Pair<Uri, VrVideoView.Options>, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Pair<Uri, VrVideoView.Options>... fileInformation) {
            try {
                getVideoView().loadVideo(fileInformation[0].first, fileInformation[0].second);
            } catch (IOException e) {
                // An error here is normally due to being unable to locate the file.
                loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
                // Since this is a background thread, we need to switch to the main thread to show a toast.
                getVideoView().post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
                Log.e(TAG, "Could not open video: " + e);
            }
            return true;
        }
    }
}

