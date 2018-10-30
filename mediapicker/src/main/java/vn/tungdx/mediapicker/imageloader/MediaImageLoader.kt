package vn.tungdx.mediapicker.imageloader

import android.net.Uri
import android.widget.ImageView

/**
 * @author TUNGDX
 */
interface MediaImageLoader {
    fun displayImage(uri: Uri, imageView: ImageView)
}
