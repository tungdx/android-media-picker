package vn.tungdx.mediapickersample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.List;
import java.util.Random;

import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.MediaOptions;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;

public class DemoActivity extends FragmentActivity implements OnClickListener {
    private static final String TAG = "DemoActivity";

    private static final int REQUEST_MEDIA = 100;
    private TextView mMessage;
    private LinearLayout mLinearLayout;
    private List<MediaItem> mMediaSelectedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        mMessage = (TextView) findViewById(R.id.textView1);
        mLinearLayout = (LinearLayout) findViewById(R.id.list_image);
        findViewById(R.id.all_default).setOnClickListener(this);
        findViewById(R.id.select_crop).setOnClickListener(this);
        findViewById(R.id.select_crop_file).setOnClickListener(this);
        findViewById(R.id.select_crop_xy).setOnClickListener(this);
        findViewById(R.id.select_crop_not_fix_ratio).setOnClickListener(this);
        findViewById(R.id.select_pass_selected).setOnClickListener(this);
        findViewById(R.id.select_only_video).setOnClickListener(this);
        findViewById(R.id.select_only_video_max_duration).setOnClickListener(
                this);
        findViewById(R.id.select_only_video_min_duration).setOnClickListener(
                this);
        findViewById(R.id.select_only_video_max_duration_warning)
                .setOnClickListener(this);
        findViewById(R.id.select_both_video_photo).setOnClickListener(this);
        findViewById(R.id.showfragment).setOnClickListener(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA) {
            if (resultCode == RESULT_OK) {
                mMediaSelectedList = MediaPickerActivity
                        .getNMediaItemSelected(data);
                if (mMediaSelectedList != null) {

                    StringBuilder builder = new StringBuilder();
                    for (MediaItem mediaItem : mMediaSelectedList) {
                        Log.i(TAG, mediaItem.toString());
                        builder.append(mediaItem.toString());
                        builder.append(", PathOrigin=");
                        builder.append(mediaItem
                                .getPathOrigin(getApplicationContext()));
                        builder.append(", PathCropped=");
                        builder.append(mediaItem
                                .getPathCropped(getApplicationContext()));
                        builder.append("\n\n");

                        addImages(mediaItem);
                    }
                    mMessage.setText(builder.toString());
                } else {
                    Log.e(TAG, "Error to get media, NULL");
                }
            } else {
                Log.e(TAG, "Get media cancled.");
            }
        }
    }

    private void addImages(MediaItem mediaItem) {
        ImageView imageView = new ImageView(getApplicationContext());
        LayoutParams params = new LayoutParams(180, 180);
        imageView.setLayoutParams(params);
        mLinearLayout.addView(imageView);

        if (mediaItem.getUriCropped() == null) {
//            Picasso.with(getApplicationContext()).load(mediaItem.getUriOrigin()).into(imageView);
            ImageLoader.getInstance().displayImage(mediaItem.getUriOrigin().toString(),imageView);
        } else {
//            Picasso.with(getApplicationContext()).load(mediaItem.getUriCropped()).into(imageView);
            ImageLoader.getInstance().displayImage(mediaItem.getUriCropped().toString(), imageView);
        }
    }

    @Override
    public void onClick(View v) {
        MediaOptions.Builder builder = new MediaOptions.Builder();
        MediaOptions options = null;
        switch (v.getId()) {
            case R.id.all_default:
                options = MediaOptions.createDefault();
                break;
            case R.id.select_crop:
                options = builder.setIsCropped(true).setFixAspectRatio(true)
                        .build();
                break;
            case R.id.select_crop_file:
                File file = new File(
                        getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        new Random().nextLong() + ".jpg");
                options = builder.setIsCropped(true).setFixAspectRatio(true)
                        .setCroppedFile(file).build();
                break;
            case R.id.select_crop_xy:
                options = builder.setIsCropped(true).setFixAspectRatio(true)
                        .setAspectX(3).setAspectY(1).build();
                break;
            case R.id.select_crop_not_fix_ratio:
                options = builder.setIsCropped(true).setFixAspectRatio(false)
                        .build();
                break;

            case R.id.select_only_video:
                options = builder.selectVideo().canSelectMultiVideo(true).build();
                break;
            case R.id.select_only_video_max_duration:
                options = builder.selectVideo().setMaxVideoDuration(3 * 1000)
                        .build();
                break;
            case R.id.select_only_video_min_duration:
                options = builder.selectVideo().setMinVideoDuration(2 * 1000)
                        .build();
                break;
            case R.id.select_only_video_max_duration_warning:
                options = builder.selectVideo().setMaxVideoDuration(3 * 1000)
                        .setShowWarningBeforeRecordVideo(true).build();
                break;
            case R.id.select_both_video_photo:
                options = builder.canSelectBothPhotoVideo()
                        .canSelectMultiPhoto(true).canSelectMultiVideo(true)
                        .build();
                break;
            case R.id.select_pass_selected:
                options = builder.canSelectMultiPhoto(true)
                        .canSelectMultiVideo(true).canSelectBothPhotoVideo()
                        .setMediaListSelected(mMediaSelectedList).build();
                break;
            case R.id.showfragment:
                showFragment(new DemoFragment());
                break;
            default:
                break;
        }
        if (options != null) {
            clearImages();
            MediaPickerActivity.open(this, REQUEST_MEDIA, options);
        }
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void clearImages() {
        mLinearLayout.removeAllViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
