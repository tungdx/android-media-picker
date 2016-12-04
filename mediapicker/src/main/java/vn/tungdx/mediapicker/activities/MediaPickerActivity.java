package vn.tungdx.mediapicker.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vn.tungdx.mediapicker.CropListener;
import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.MediaOptions;
import vn.tungdx.mediapicker.MediaSelectedListener;
import vn.tungdx.mediapicker.R;
import vn.tungdx.mediapicker.imageloader.MediaImageLoader;
import vn.tungdx.mediapicker.imageloader.MediaImageLoaderImpl;
import vn.tungdx.mediapicker.utils.MediaUtils;
import vn.tungdx.mediapicker.utils.MessageUtils;
import vn.tungdx.mediapicker.utils.RecursiveFileObserver;


/**
 * @author TUNGDX
 */

/**
 * Use this activity for pickup photos or videos (media).
 * <p/>
 * How to use:
 * <ul>
 * <li>
 * Step1: Open media picker: <br/>
 * - If using in activity use:
 * {@link MediaPickerActivity#open(Activity, int, MediaOptions)} or
 * {@link MediaPickerActivity#open(Activity, int)}</li>
 * - If using in fragment use:
 * {@link MediaPickerActivity#open(Fragment, int, MediaOptions)} or
 * {@link MediaPickerActivity#open(Fragment, int)} <br/>
 * </li>
 * <li>
 * Step2: Get out media that selected in
 * {@link Activity#onActivityResult(int, int, Intent)} of activity or fragment
 * that open media picker. Use
 * {@link MediaPickerActivity#getMediaItemSelected(Intent)} to get out media
 * list that selected.</li>
 * <p/>
 * <i>Note: Videos or photos return back depends on {@link MediaOptions} passed
 * to {@link #open(Activity, int, MediaOptions)} </i></li>
 * </ul>
 */
public class MediaPickerActivity extends AppCompatActivity implements
        MediaSelectedListener, CropListener, FragmentManager.OnBackStackChangedListener, FragmentHost {
    private static final String TAG = "MediaPickerActivity";

    public static final String EXTRA_MEDIA_OPTIONS = "extra_media_options";
    /**
     * Intent extra included when return back data in
     * {@link Activity#onActivityResult(int, int, Intent)} of activity or fragment
     * that open media picker. Always return {@link ArrayList} of
     * {@link MediaItem}. You must always check null and size of this list
     * before handle your logic.
     */
    public static final String EXTRA_MEDIA_SELECTED = "extra_media_selected";
    private static final int REQUEST_PHOTO_CAPTURE = 100;
    private static final int REQUEST_VIDEO_CAPTURE = 200;

    private static final String KEY_PHOTOFILE_CAPTURE = "key_photofile_capture";
    private static final int REQUEST_CAMERA_PERMISSION = 300;

    private MediaOptions mMediaOptions;
    private MenuItem mMediaSwitcher;
    private MenuItem mPhoto;
    private MenuItem mVideo;
    private MenuItem mDone;

    private File mPhotoFileCapture;
    private List<File> mFilesCreatedWhileCapturePhoto;
    private RecursiveFileObserver mFileObserver;
    private FileObserverTask mFileObserverTask;
    private boolean takePhotoPending;
    private boolean takeVideoPending;

    /**
     * Start {@link MediaPickerActivity} in {@link Activity} to pick photo or
     * video that depends on {@link MediaOptions} passed.
     *
     * @param activity
     * @param requestCode
     * @param options
     */
    public static void open(Activity activity, int requestCode,
                            MediaOptions options) {
        Intent intent = new Intent(activity, MediaPickerActivity.class);
        intent.putExtra(EXTRA_MEDIA_OPTIONS, options);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Start {@link MediaPickerActivity} in {@link Activity} with default media
     * option: {@link MediaOptions#createDefault()}
     *
     * @param activity
     * @param requestCode
     */
    public static void open(Activity activity, int requestCode) {
        open(activity, requestCode, MediaOptions.createDefault());
    }

    /**
     * Start {@link MediaPickerActivity} in {@link Fragment} to pick photo or
     * video that depends on {@link MediaOptions} passed.
     *
     * @param fragment
     * @param requestCode
     * @param options
     */
    public static void open(Fragment fragment, int requestCode,
                            MediaOptions options) {
        Intent intent = new Intent(fragment.getActivity(),
                MediaPickerActivity.class);
        intent.putExtra(EXTRA_MEDIA_OPTIONS, options);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * Start {@link MediaPickerActivity} in {@link Fragment} with default media
     * option: {@link MediaOptions#createDefault()}
     *
     * @param fragment
     * @param requestCode
     */
    public static void open(Fragment fragment, int requestCode) {
        open(fragment, requestCode, MediaOptions.createDefault());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: not support change orientation right now (because out of
        // memory when crop image and change orientation, must check third party
        // to crop image again).
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_mediapicker);
        if (savedInstanceState != null) {
            mMediaOptions = savedInstanceState
                    .getParcelable(EXTRA_MEDIA_OPTIONS);
            mPhotoFileCapture = (File) savedInstanceState
                    .getSerializable(KEY_PHOTOFILE_CAPTURE);
        } else {
            mMediaOptions = getIntent().getParcelableExtra(EXTRA_MEDIA_OPTIONS);
            if (mMediaOptions == null) {
                throw new IllegalArgumentException(
                        "MediaOptions must be not null, you should use MediaPickerActivity.open(Activity activity, int requestCode,MediaOptions options) method instead.");
            }
        }
        if (getActivePage() == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container,
                            MediaPickerFragment.newInstance(mMediaOptions))
                    .commit();
        }
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.picker_actionbar_translucent));
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mediapicker_main, menu);
        mPhoto = menu.findItem(R.id.take_photo);
        mVideo = menu.findItem(R.id.take_video);
        mMediaSwitcher = menu.findItem(R.id.media_switcher);
        mDone = menu.findItem(R.id.done);
        syncActionbar();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        cancelFileObserverTask();
        stopWatchingFile();
        mFilesCreatedWhileCapturePhoto = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();

        } else if (i == R.id.take_photo) {
            performTakePhotoRequest();
            takePhotoPending = true;
            return true;
        } else if (i == R.id.take_video) {
            performTakeVideoRequest();
            takeVideoPending = true;
            return true;
        } else if (i == R.id.media_switcher) {
            Fragment activePage = getActivePage();
            if (mMediaOptions.canSelectPhotoAndVideo()
                    && activePage instanceof MediaPickerFragment) {
                MediaPickerFragment mediaPickerFragment = ((MediaPickerFragment) activePage);
                mediaPickerFragment.switchMediaSelector();
                syncIconMenu(mediaPickerFragment.getMediaType());
            }
            return true;
        } else if (i == R.id.done) {
            Fragment activePage;
            activePage = getActivePage();
            boolean isPhoto = ((MediaPickerFragment) activePage)
                    .getMediaType() == MediaItem.PHOTO;
            if (isPhoto) {
                if (mMediaOptions.isCropped()
                        && !mMediaOptions.canSelectMultiPhoto()) {
                    // get first item in list (pos=0) because can only crop 1 image at same time.
                    MediaItem mediaItem = new MediaItem(MediaItem.PHOTO,
                            ((MediaPickerFragment) activePage)
                                    .getMediaSelectedList().get(0)
                                    .getUriOrigin());
                    showCropFragment(mediaItem, mMediaOptions);
                } else {
                    returnBackData(((MediaPickerFragment) activePage)
                            .getMediaSelectedList());
                }
            } else {
                if (mMediaOptions.canSelectMultiVideo()) {
                    returnBackData(((MediaPickerFragment) activePage)
                            .getMediaSelectedList());
                } else {
                    // only get 1st item regardless of have many.
                    returnVideo(((MediaPickerFragment) activePage)
                            .getMediaSelectedList().get(0).getUriOrigin());
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_MEDIA_OPTIONS, mMediaOptions);
        outState.putSerializable(KEY_PHOTOFILE_CAPTURE, mPhotoFileCapture);
    }

    @Override
    public MediaImageLoader getImageLoader() {
        return new MediaImageLoaderImpl(getApplicationContext());
    }

    @Override
    public void onHasNoSelected() {
        mDone.setVisible(false);
        syncActionbar();
    }

    @Override
    public void onHasSelected(List<MediaItem> mediaSelectedList) {
        showDone();
    }

    private void showDone() {
        mDone.setVisible(true);
        mPhoto.setVisible(false);
        mVideo.setVisible(false);
        mMediaSwitcher.setVisible(false);
    }

    private void syncMediaOptions() {
        // handle media options
        if (mMediaOptions.canSelectPhotoAndVideo()) {
            mMediaSwitcher.setVisible(true);
        } else {
            mMediaSwitcher.setVisible(false);
        }
        if (mMediaOptions.canSelectPhoto()) {
            mPhoto.setVisible(true);
        } else {
            mPhoto.setVisible(false);
        }
        if (mMediaOptions.canSelectVideo()) {
            mVideo.setVisible(true);
        } else {
            mVideo.setVisible(false);
        }
    }

    private void syncIconMenu(int mediaType) {
        switch (mediaType) {
            case MediaItem.PHOTO:
                mMediaSwitcher.setIcon(R.drawable.ab_picker_video_2);
                break;
            case MediaItem.VIDEO:
                mMediaSwitcher.setIcon(R.drawable.ab_picker_camera2);
                break;
            default:
                break;
        }
    }

    private void returnBackData(List<MediaItem> mediaSelectedList) {
        Intent data = new Intent();
        data.putParcelableArrayListExtra(EXTRA_MEDIA_SELECTED,
                (ArrayList<MediaItem>) mediaSelectedList);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File file = mMediaOptions.getPhotoFile();
            if (file == null) {
                try {
                    file = MediaUtils.createDefaultImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (file != null) {
                mPhotoFileCapture = file;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(file));
                startActivityForResult(takePictureIntent, REQUEST_PHOTO_CAPTURE);
                mFileObserverTask = new FileObserverTask();
                mFileObserverTask.execute();
            }
        }
    }

    private RecursiveFileObserver.OnFileCreatedListener mOnFileCreatedListener = new RecursiveFileObserver.OnFileCreatedListener() {

        @Override
        public void onFileCreate(File file) {
            if (mFilesCreatedWhileCapturePhoto == null)
                mFilesCreatedWhileCapturePhoto = new ArrayList<File>();
            mFilesCreatedWhileCapturePhoto.add(file);
        }
    };

    private void takeVideo() {
        final Intent takeVideoIntent = new Intent(
                MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            int max = mMediaOptions.getMaxVideoDuration();
            if (max != Integer.MAX_VALUE) {
                // /=1000 because it's must in seconds.
                max /= 1000;
                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, max);
                if (mMediaOptions.isShowWarningVideoDuration()) {
                    MediaPickerErrorDialog dialog = MediaPickerErrorDialog
                            .newInstance(MessageUtils
                                    .getWarningMessageVideoDuration(
                                            getApplicationContext(), max));
                    dialog.setOnOKClickListener(new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(takeVideoIntent,
                                    REQUEST_VIDEO_CAPTURE);
                        }
                    });
                    dialog.show(getSupportFragmentManager(), null);
                } else {
                    startActivityForResult(takeVideoIntent,
                            REQUEST_VIDEO_CAPTURE);
                }
            } else {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            }
        }
    }

    /**
     * In some HTC devices (maybe others), duplicate image when captured with
     * extra_output. This method will try delete duplicate image. It's prefer
     * default image by camera than extra output.
     */
    private void tryCorrectPhotoFileCaptured() {
        if (mPhotoFileCapture == null || mFilesCreatedWhileCapturePhoto == null
                || mFilesCreatedWhileCapturePhoto.size() <= 0)
            return;
        long captureSize = mPhotoFileCapture.length();
        for (File file : mFilesCreatedWhileCapturePhoto) {
            if (MediaUtils
                    .isImageExtension(MediaUtils.getFileExtension(file))
                    && file.length() >= captureSize
                    && !file.equals(mPhotoFileCapture)) {
                boolean value = mPhotoFileCapture.delete();
                mPhotoFileCapture = file;
                Log.i(TAG,
                        String.format(
                                "Try correct photo file: Delete duplicate photos in [%s] [%s]",
                                mPhotoFileCapture, value));
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        cancelFileObserverTask();
        stopWatchingFile();
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PHOTO_CAPTURE:
                    tryCorrectPhotoFileCaptured();
                    if (mPhotoFileCapture != null) {
                        MediaUtils.galleryAddPic(getApplicationContext(),
                                mPhotoFileCapture);
                        if (mMediaOptions.isCropped()) {
                            MediaItem item = new MediaItem(MediaItem.PHOTO,
                                    Uri.fromFile(mPhotoFileCapture));
                            showCropFragment(item, mMediaOptions);
                        } else {
                            MediaItem item = new MediaItem(MediaItem.PHOTO,
                                    Uri.fromFile(mPhotoFileCapture));
                            ArrayList<MediaItem> list = new ArrayList<MediaItem>();
                            list.add(item);
                            returnBackData(list);
                        }
                    }
                    break;
                case REQUEST_VIDEO_CAPTURE:
                    returnVideo(data.getData());
                    break;
                default:
                    break;
            }
        }
    }

    private void showCropFragment(MediaItem mediaItem, MediaOptions options) {
        Fragment fragment = PhotoCropFragment.newInstance(mediaItem, options);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onSuccess(MediaItem mediaItem) {
        List<MediaItem> list = new ArrayList<MediaItem>();
        list.add(mediaItem);
        returnBackData(list);
    }

    @Override
    public void onBackStackChanged() {
        syncActionbar();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        syncActionbar();
    }

    public void syncActionbar() {
        Fragment fragment = getActivePage();
        if (fragment instanceof PhotoCropFragment) {
            hideAllOptionsMenu();
            getSupportActionBar().hide();
        } else if (fragment instanceof MediaPickerFragment) {
            getSupportActionBar().show();
            syncMediaOptions();
            MediaPickerFragment pickerFragment = (MediaPickerFragment) fragment;
            syncIconMenu(pickerFragment.getMediaType());
            if (pickerFragment.hasMediaSelected()) {
                showDone();
            } else {
                mDone.setVisible(false);
            }
        }
    }

    private Fragment getActivePage() {
        return getSupportFragmentManager().findFragmentById(R.id.container);
    }

    private void hideAllOptionsMenu() {
        if (mPhoto != null)
            mPhoto.setVisible(false);
        if (mVideo != null)
            mVideo.setVisible(false);
        if (mMediaSwitcher != null)
            mMediaSwitcher.setVisible(false);
        if (mDone != null)
            mDone.setVisible(false);
    }

    /**
     * Check video duration valid or not with options.
     *
     * @param videoUri
     * @return 1 if valid, otherwise is invalid. -2: not found, 0 larger than
     * accepted, -1 smaller than accepted.
     */
    private int checkValidVideo(Uri videoUri) {
        if (videoUri == null)
            return -2;
        // try get duration using MediaPlayer. (Should get duration using
        // MediaPlayer before use Uri because some devices can get duration by
        // Uri or not exactly. Ex: Asus Memo Pad8)
        long duration = MediaUtils.getDuration(getApplicationContext(),
                MediaUtils.getRealVideoPathFromURI(getContentResolver(), videoUri));
        if (duration == 0) {
            // try get duration one more, by uri of video. Note: Some time can
            // not get duration by Uri after record video.(It's usually happen
            // in HTC
            // devices 2.3, maybe others)
            duration = MediaUtils
                    .getDuration(getApplicationContext(), videoUri);
        }
        // accept delta about < 1000 milliseconds. (ex: 10769 is still accepted
        // if limit is 10000)
        if (mMediaOptions.getMaxVideoDuration() != Integer.MAX_VALUE
                && duration >= mMediaOptions.getMaxVideoDuration() + 1000) {
            return 0;
        } else if (duration == 0
                || duration < mMediaOptions.getMinVideoDuration()) {
            return -1;
        }
        return 1;
    }

    private void returnVideo(Uri videoUri) {
        final int code = checkValidVideo(videoUri);
        switch (code) {
            // not found. should never happen. Do nothing when happen.
            case -2:

                break;
            // smaller than min
            case -1:
                // in seconds
                int duration = mMediaOptions.getMinVideoDuration() / 1000;
                String msg = MessageUtils.getInvalidMessageMinVideoDuration(
                        getApplicationContext(), duration);
                showVideoInvalid(msg);
                break;

            // larger than max
            case 0:
                // in seconds.
                duration = mMediaOptions.getMaxVideoDuration() / 1000;
                msg = MessageUtils.getInvalidMessageMaxVideoDuration(
                        getApplicationContext(), duration);
                showVideoInvalid(msg);
                break;
            // ok
            case 1:
                MediaItem item = new MediaItem(MediaItem.VIDEO, videoUri);
                ArrayList<MediaItem> list = new ArrayList<MediaItem>();
                list.add(item);
                returnBackData(list);
                break;

            default:
                break;
        }
    }

    private void showVideoInvalid(String msg) {
        MediaPickerErrorDialog errorDialog = MediaPickerErrorDialog
                .newInstance(msg);
        errorDialog.show(getSupportFragmentManager(), null);
    }

    /**
     * Get media item list selected from intent extra included in
     * {@link Activity#onActivityResult(int, int, Intent)} of activity or fragment
     * that open media picker.
     *
     * @param intent In {@link Activity#onActivityResult(int, int, Intent)} method of
     *               activity or fragment that open media picker.
     * @return Always return {@link ArrayList} of {@link MediaItem}. You must
     * always check null and size of this list before handle your logic.
     */
    public static ArrayList<MediaItem> getMediaItemSelected(Intent intent) {
        if (intent == null)
            return null;
        ArrayList<MediaItem> mediaItemList = intent
                .getParcelableArrayListExtra(MediaPickerActivity.EXTRA_MEDIA_SELECTED);
        return mediaItemList;
    }

    private class FileObserverTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (isCancelled()) return null;
            if (mFileObserver == null) {
                mFileObserver = new RecursiveFileObserver(Environment
                        .getExternalStorageDirectory().getAbsolutePath(),
                        FileObserver.CREATE);
                mFileObserver
                        .setFileCreatedListener(mOnFileCreatedListener);
            }
            mFileObserver.startWatching();
            return null;
        }
    }

    private void cancelFileObserverTask() {
        if (mFileObserverTask != null) {
            mFileObserverTask.cancel(true);
            mFileObserver = null;
        }
    }

    private void stopWatchingFile() {
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
            mFileObserver = null;
        }
    }

    private void performTakePhotoRequest() {
        if (hasCameraPermission()) {
            takePhoto();
        } else {
            requestCameraPermission();
        }
    }

    private void performTakeVideoRequest() {
        if (hasCameraPermission()) {
            takeVideo();
        } else {
            requestCameraPermission();
        }
    }

    private boolean hasCameraPermission() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (takePhotoPending) {
                        takePhoto();
                    } else if (takeVideoPending) {
                        takeVideo();
                    }

                }
                return;
        }
        //pass permission result to fragments in this activity
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
}