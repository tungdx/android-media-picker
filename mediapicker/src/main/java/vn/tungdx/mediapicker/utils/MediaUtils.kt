package vn.tungdx.mediapicker.utils

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.*
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author TUNGDX
 */

/**
 * Utility for Media Picker module.
 */
object MediaUtils {

    val PROJECT_PHOTO = arrayOf(MediaColumns._ID)

    val PROJECT_VIDEO = arrayOf(MediaColumns._ID)

    fun getPhotoUri(cursor: Cursor): Uri {
        return getMediaUri(cursor, Images.Media.EXTERNAL_CONTENT_URI)
    }

    fun getVideoUri(cursor: Cursor): Uri {
        return getMediaUri(cursor, Video.Media.EXTERNAL_CONTENT_URI)
    }

    fun getMediaUri(cursor: Cursor, uri: Uri): Uri {
        val id = cursor.getString(cursor.getColumnIndex(MediaColumns._ID))
        return Uri.withAppendedPath(uri, id)
    }

    /**
     * Create an default file for save image from camera.
     *
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createDefaultImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_$timeStamp.jpg"
        val storageDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File(storageDir, imageFileName)
    }

    fun getLastImageId(context: Context): Int {
        val imageColumns = arrayOf(Images.Media._ID)
        val imageOrderBy = Images.Media._ID + " DESC"
        val cursor = context.contentResolver.query(
                Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy)
                ?: return 0
        var id = 0
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor
                    .getColumnIndex(Images.Media._ID))
        }
        cursor.close()
        return id
    }

    // Should use
    // this: (anwser 2)
    // http://stackoverflow.com/questions/6390163/deleting-a-gallery-image-after-camera-intent-photo-taken
    fun checkNull(context: Context, lastImageId: Int,
                  fileCapture: File): String? {
        val imageColumns = arrayOf(Images.Media._ID, Images.Media.DATA)
        val imageOrderBy = Images.Media._ID + " DESC"
        val imageWhere = Images.Media._ID + ">?"
        val imageArguments = arrayOf(Integer.toString(lastImageId))

        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
                Images.Media.EXTERNAL_CONTENT_URI, imageColumns,
                imageWhere, imageArguments, imageOrderBy) ?: return null

        var newpath: String? = null
        if (cursor.count >= 2) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val id = cursor.getInt(cursor
                        .getColumnIndex(Images.Media._ID))
                val data = cursor.getString(cursor
                        .getColumnIndex(Images.Media.DATA))
                if (data == fileCapture.path) {
                    val rows = contentResolver.delete(
                            Images.Media.EXTERNAL_CONTENT_URI,
                            Images.Media._ID + "=?",
                            arrayOf(java.lang.Long.toString(id.toLong())))
                    val ok = fileCapture.delete()

                } else {
                    newpath = data
                }
                cursor
                        .moveToNext()
            }
        } else {
            newpath = fileCapture.path
            Log.e("MediaUtils", "Not found duplicate.")
        }

        cursor.close()
        return newpath
    }

    fun getFileExtension(file: File): String {
        val name = file.name
        val lastIndexOf = name.lastIndexOf(".")
        return if (lastIndexOf == -1) {
            "" // empty extension
        } else name.substring(lastIndexOf)
    }

    fun isImageExtension(extension: String): Boolean {
        val valid = arrayOf(".jpg", ".jpeg")
        for (ex in valid) {
            if (extension.equals(ex, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    /**
     * Get path of image from uri
     *
     * @param contentResolver
     * @param contentURI
     * @return path of image. Null if not found.
     */
    fun getRealImagePathFromURI(contentResolver: ContentResolver,
                                contentURI: Uri): String? {
        val cursor = contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null)
            return contentURI.path
        else {
            cursor.moveToFirst()
            val idx = cursor
                    .getColumnIndex(Images.ImageColumns.DATA)
            try {
                return cursor.getString(idx)
            } catch (exception: Exception) {
                return null
            }

        }
    }

    /**
     * Get path of video from uri
     *
     * @param contentResolver
     * @param contentURI
     * @return path of video. Null if not found.
     */
    fun getRealVideoPathFromURI(contentResolver: ContentResolver,
                                contentURI: Uri): String? {
        val cursor = contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null)
            return contentURI.path
        else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(Video.VideoColumns.DATA)
            try {
                return cursor.getString(idx)
            } catch (exception: Exception) {
                return null
            }

        }
    }

    /**
     * Add file photo to gallery after capture from camera or downloaded.
     *
     * @param context
     * @param file
     */
    fun galleryAddPic(context: Context, file: File) {
        val mediaScanIntent = Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(file)
        mediaScanIntent.data = contentUri
        context.sendBroadcast(mediaScanIntent)
    }

    /**
     * Get video's duration without [ContentProvider]. Because not know
     * [Uri] of video.
     *
     * @param context
     * @param path    Path of video file.
     * @return Duration of video, in milliseconds. Return 0 if path is null.
     */
    fun getDuration(context: Context, path: String): Long {
        var mMediaPlayer: MediaPlayer? = null
        var duration: Long = 0
        try {
            mMediaPlayer = MediaPlayer()
            mMediaPlayer.setDataSource(context, Uri.parse(path))
            mMediaPlayer.prepare()
            duration = mMediaPlayer.duration.toLong()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset()
                mMediaPlayer.release()
                mMediaPlayer = null
            }
        }
        return duration
    }

    /**
     * Get video's duration from [ContentProvider]
     *
     * @param context
     * @param uri     must has [Uri.getScheme] equals
     * [ContentResolver.SCHEME_CONTENT]
     * @return Duration of video, in milliseconds.
     */
    fun getDuration(context: Context, uri: Uri): Long {
        var duration = 0L
        val cursor = MediaStore.Video.query(context.contentResolver,
                uri, arrayOf(MediaStore.Video.VideoColumns.DURATION))
        if (cursor != null) {
            cursor.moveToFirst()
            duration = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Video.VideoColumns.DURATION))
            cursor.close()
        }
        return duration
    }

    fun decodeSampledBitmapFromFile(pathFile: String,
                                    reqWidth: Int, reqHeight: Int): Bitmap {

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(pathFile, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(pathFile, options)
    }

    fun calculateInSampleSize(options: BitmapFactory.Options,
                              reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}