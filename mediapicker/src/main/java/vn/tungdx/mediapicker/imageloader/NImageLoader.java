package vn.tungdx.mediapicker.imageloader;

import android.net.Uri;
import android.widget.ImageView;

import java.io.File;

/**
 * @author TUNGDX
 */
public interface NImageLoader{
    void displayImage(Uri uri, ImageView imageView);
}
