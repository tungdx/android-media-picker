package vn.tungdx.mediapicker.utils

import android.Manifest.permission
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Build.VERSION
import androidx.core.content.ContextCompat

val MediaPermissions: Array<String> = if (VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    arrayOf(
        permission.READ_MEDIA_IMAGES, permission.READ_MEDIA_VISUAL_USER_SELECTED,
        permission.READ_MEDIA_VIDEO
    )
else if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    arrayOf(permission.READ_MEDIA_IMAGES, permission.READ_MEDIA_VIDEO)
else
    arrayOf(permission.READ_EXTERNAL_STORAGE)

fun Context.checkMediaPermission(): Boolean {
    if (
        VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        (
            ContextCompat.checkSelfPermission(
                this,
                permission.READ_MEDIA_IMAGES
            ) == PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    permission.READ_MEDIA_VIDEO
                ) == PERMISSION_GRANTED
            )
    ) {
        // Full access on Android 13 (API level 33) or higher
        return true
    } else if (
        VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
        ContextCompat.checkSelfPermission(
            this,
            permission.READ_MEDIA_VISUAL_USER_SELECTED
        ) == PERMISSION_GRANTED
    ) {
        // Partial access on Android 14 (API level 34) or higher
        return true
    } else if (ContextCompat.checkSelfPermission(
            this,
            permission.READ_EXTERNAL_STORAGE
        ) == PERMISSION_GRANTED
    ) {
        // Full access up to Android 12 (API level 32)
        return true
    } else {
        // Access denied
        return false
    }
}
