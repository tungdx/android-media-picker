package vn.tungdx.mediapicker.utils;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author TUNGDX
 */

/**
 * Utility for Media Picker module.
 */
public class MediaUtils {
    public static final String[] PROJECT_PHOTO = {MediaColumns._ID};
    public static final String[] PROJECT_VIDEO = {MediaColumns._ID};

    public static Uri getPhotoUri(Cursor cursor) {
        return getMediaUri(cursor, Images.Media.EXTERNAL_CONTENT_URI);
    }

    public static Uri getVideoUri(Cursor cursor) {
        return getMediaUri(cursor, Video.Media.EXTERNAL_CONTENT_URI);
    }

    public static Uri getMediaUri(Cursor cursor, Uri uri) {
        String id = cursor.getString(cursor.getColumnIndex(MediaColumns._ID));
        return Uri.withAppendedPath(uri, id);
    }

    /**
     * Create an default file for save image from camera.
     *
     * @return
     * @throws IOException
     */
    public static File createDefaultImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        return new File(storageDir, imageFileName);
    }

    public static int getLastImageId(Context context) {
        final String[] imageColumns = {Images.Media._ID};
        final String imageOrderBy = Images.Media._ID + " DESC";
        Cursor cursor = context.getContentResolver().query(
                Images.Media.EXTERNAL_CONTENT_URI, imageColumns,
                null, null, imageOrderBy);
        if (cursor == null)
            return 0;
        int id = 0;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor
                    .getColumnIndex(Images.Media._ID));
        }
        cursor.close();
        return id;
    }

    // Should use
    // this: (anwser 2)
    // http://stackoverflow.com/questions/6390163/deleting-a-gallery-image-after-camera-intent-photo-taken
    public static String checkNull(Context context, int lastImageId,
                                   File fileCapture) {
        final String[] imageColumns = {Images.Media._ID,
                Images.Media.DATA};
        final String imageOrderBy = Images.Media._ID + " DESC";
        final String imageWhere = Images.Media._ID + ">?";
        final String[] imageArguments = {Integer.toString(lastImageId)};

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                Images.Media.EXTERNAL_CONTENT_URI, imageColumns,
                imageWhere, imageArguments, imageOrderBy);
        if (cursor == null)
            return null;

        String newpath = null;
        if (cursor.getCount() >= 2) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                    .moveToNext()) {
                int id = cursor.getInt(cursor
                        .getColumnIndex(Images.Media._ID));
                String data = cursor.getString(cursor
                        .getColumnIndex(Images.Media.DATA));
                if (data.equals(fileCapture.getPath())) {
                    int rows = contentResolver.delete(
                            Images.Media.EXTERNAL_CONTENT_URI,
                            Images.Media._ID + "=?",
                            new String[]{Long.toString(id)});
                    boolean ok = fileCapture.delete();

                } else {
                    newpath = data;
                }
            }
        } else {
            newpath = fileCapture.getPath();
            Log.e("MediaUtils", "Not found duplicate.");
        }

        cursor.close();
        return newpath;
    }

    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }

    public static boolean isImageExtension(String extension) {
        String[] valid = {".jpg", ".jpeg"};
        for (String ex : valid) {
            if (extension.equalsIgnoreCase(ex)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get path of image from uri
     *
     * @param contentResolver
     * @param contentURI
     * @return path of image. Null if not found.
     */
    public static String getRealImagePathFromURI(ContentResolver contentResolver,
                                                 Uri contentURI) {
        Cursor cursor = contentResolver.query(contentURI, null, null, null,
                null);
        if (cursor == null)
            return contentURI.getPath();
        else {
            cursor.moveToFirst();
            int idx = cursor
                    .getColumnIndex(Images.ImageColumns.DATA);
            try {
                return cursor.getString(idx);
            } catch (Exception exception) {
                return null;
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
    public static String getRealVideoPathFromURI(ContentResolver contentResolver,
                                                 Uri contentURI) {
        Cursor cursor = contentResolver.query(contentURI, null, null, null,
                null);
        if (cursor == null)
            return contentURI.getPath();
        else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(Video.VideoColumns.DATA);
            try {
                return cursor.getString(idx);
            } catch (Exception exception) {
                return null;
            }
        }
    }

    /**
     * Add file photo to gallery after capture from camera or downloaded.
     *
     * @param context
     * @param file
     */
    public static void galleryAddPic(Context context, File file) {
        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * Get video's duration without {@link ContentProvider}. Because not know
     * {@link Uri} of video.
     *
     * @param context
     * @param path    Path of video file.
     * @return Duration of video, in milliseconds. Return 0 if path is null.
     */
    public static long getDuration(Context context, String path) {
        MediaPlayer mMediaPlayer = null;
        long duration = 0;
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(context, Uri.parse(path));
            mMediaPlayer.prepare();
            duration = mMediaPlayer.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
        return duration;
    }

    /**
     * Get video's duration from {@link ContentProvider}
     *
     * @param context
     * @param uri     must has {@link Uri#getScheme()} equals
     *                {@link ContentResolver#SCHEME_CONTENT}
     * @return Duration of video, in milliseconds.
     */
    public static long getDuration(Context context, Uri uri) {
        long duration = 0L;
        Cursor cursor = MediaStore.Video.query(context.getContentResolver(),
                uri, new String[]{MediaStore.Video.VideoColumns.DURATION});
        if (cursor != null) {
            cursor.moveToFirst();
            duration = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Video.VideoColumns.DURATION));
            cursor.close();
        }
        return duration;
    }

    public static Bitmap decodeSampledBitmapFromFile(String pathFile,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathFile, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathFile, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}