package vn.tungdx.mediapicker.imageloader;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import vn.tungdx.mediapicker.R;

/**
 * @author TUNGDX
 */

public class MediaImageLoaderImpl implements MediaImageLoader {

    public MediaImageLoaderImpl(Context context) {
    }

    @Override
    public void displayImage(Uri uri, ImageView imageView) {
        Glide.with(imageView.getContext()).load(uri).placeholder(R.color.picker_imageloading).into(imageView);
    }
}