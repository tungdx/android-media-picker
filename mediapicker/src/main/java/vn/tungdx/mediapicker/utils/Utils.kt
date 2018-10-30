package vn.tungdx.mediapicker.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.os.Environment

import java.io.File
import java.io.IOException

import vn.tungdx.mediapicker.R

/**
 * Created by TungDX on 6/4/2015.
 */
object Utils {
    // refers: http://stackoverflow.com/a/7167086/2128392
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    fun getActionbarHeight(activity: Activity): Int {
        val attr: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            attr = android.R.attr.actionBarSize
        } else {
            attr = R.attr.actionBarSize
        }
        val styledAttributes = activity.theme
                .obtainStyledAttributes(intArrayOf(attr))
        val actionbarSize = styledAttributes.getDimension(0, 0f).toInt()
        styledAttributes.recycle()
        return actionbarSize
    }

    /**
     * Create temp file. If has external storage create in external else create
     * in internal.
     *
     * @param context
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createTempFile(context: Context): File {
        return if (!hasExternalStorage()) {
            createTempFile(context, context.cacheDir)
        } else {
            createTempFile(context,
                    context.getExternalFilesDir("caches"))
        }
    }

    /**
     * Check external exist or not.
     *
     * @return
     */
    fun hasExternalStorage(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * Create temp file in folder
     *
     * @param context
     * @param folder  where place temp file
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createTempFile(context: Context, folder: File?): File {
        val prefix = System.currentTimeMillis().toString()
        return File.createTempFile(prefix, null, folder)
    }
}
