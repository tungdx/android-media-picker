package vn.tungdx.mediapicker.utils

import android.content.Context

import vn.tungdx.mediapicker.MediaOptions
import vn.tungdx.mediapicker.R

/**
 * @author TUNGDX
 */

/**
 * Get warning, error message for media picker module.
 */
object MessageUtils {
    /**
     * @param context
     * @param maxDuration in seconds.
     * @return message before record video.
     */
    fun getWarningMessageVideoDuration(context: Context,
                                       maxDuration: Int): String {
        return context.resources.getQuantityString(
                R.plurals.picker_video_duration_warning, maxDuration, maxDuration)
    }

    /**
     * @param context
     * @param maxDuration
     * @return message when record and select video that has duration larger
     * than max options.
     * [MediaOptions.Builder.setMaxVideoDuration]
     */
    fun getInvalidMessageMaxVideoDuration(context: Context,
                                          maxDuration: Int): String {
        return context.resources.getQuantityString(
                R.plurals.picker_video_duration_max, maxDuration, maxDuration)
    }

    /**
     * @param context
     * @param minDuration
     * @return message when record and select video that has duration smaller
     * than min options.
     * [MediaOptions.Builder.setMinVideoDuration]
     */
    fun getInvalidMessageMinVideoDuration(context: Context,
                                          minDuration: Int): String {
        return context.resources.getQuantityString(
                R.plurals.picker_video_duration_min, minDuration, minDuration)
    }
}