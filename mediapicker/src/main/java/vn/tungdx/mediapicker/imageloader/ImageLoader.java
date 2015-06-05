package vn.tungdx.mediapicker.imageloader;

import android.net.Uri;
import android.widget.ImageView;

import java.io.File;

/**
 * @author TUNGDX
 */
public interface ImageLoader {
    void displayImage(Uri uri, ImageView imageView);
}
