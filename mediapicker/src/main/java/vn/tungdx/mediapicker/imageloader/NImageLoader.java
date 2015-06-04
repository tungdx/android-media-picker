package vn.tungdx.mediapicker.imageloader;

import android.net.Uri;
import android.widget.ImageView;

import java.io.File;

/**
 * @author TUNGDX
 */
public interface NImageLoader{
    void displayImage(String url, ImageView imageView);

    void displayImage(File file, ImageView imageView);

    void displayImage(Uri uri, ImageView imageView);
}
