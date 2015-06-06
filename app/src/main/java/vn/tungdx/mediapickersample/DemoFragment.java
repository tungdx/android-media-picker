package vn.tungdx.mediapickersample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import vn.tungdx.mediapicker.MediaItem;
import vn.tungdx.mediapicker.activities.MediaPickerActivity;


public class DemoFragment extends Fragment {
    private static final int REQUEST_MEDIA = 100;
    private static final String TAG = "DemoMediaPickerFragment";
    private LinearLayout mLinearLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater
                .inflate(R.layout.fragment_demo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.all_default).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        MediaPickerActivity.open(DemoFragment.this, REQUEST_MEDIA);
                        clearImages();
                    }
                });
        mLinearLayout = (LinearLayout) view.findViewById(R.id.list_image);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<MediaItem> mMediaSelectedList;
        if (requestCode == REQUEST_MEDIA) {
            if (resultCode == Activity.RESULT_OK) {
                mMediaSelectedList = MediaPickerActivity.getMediaItemSelected(data);
                if (mMediaSelectedList != null) {
                    addImages(mMediaSelectedList.get(0));
                }
            }
        }
    }

    private void addImages(MediaItem mediaItem) {
        LinearLayout root = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.item, null);

        ImageView imageView = (ImageView) root.findViewById(R.id.image);
        TextView textView = (TextView) root.findViewById(R.id.textView);

        String info = String.format("Original Uri [%s]\nOriginal Path [%s] \n\nCropped Uri [%s] \nCropped Path[%s]", mediaItem.getUriOrigin(), mediaItem.getUriCropped(), mediaItem.getPathOrigin(getActivity()), mediaItem.getPathCropped(getActivity()));
        textView.setText(info);
        if (mediaItem.getUriCropped() == null) {
            ImageLoader.getInstance().displayImage(mediaItem.getUriOrigin().toString(), imageView);
        } else {
            ImageLoader.getInstance().displayImage(mediaItem.getUriCropped().toString(), imageView);
        }
        mLinearLayout.addView(root);
    }

    private void clearImages() {
        mLinearLayout.removeAllViews();
    }
}
