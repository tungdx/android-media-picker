package vn.tungdx.mediapicker.imageloader;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * @author TUNGDX
 */

public class NImageLoaderImpl implements NImageLoader {
    private Context mContext;

    public NImageLoaderImpl(Context context) {
        mContext = context;
    }

    @Override
    public void displayImage(String url, ImageView imageView) {
        Picasso.with(mContext).load(url).into(imageView);
    }

    @Override
    public void displayImage(File file, ImageView imageView) {
        Picasso.with(mContext).load(file).into(imageView);
    }

    @Override
    public void displayImage(Uri uri, ImageView imageView) {
        Picasso.with(mContext).load(uri).into(imageView);
    }
}
