package vn.tungdx.mediapicker.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.TypedValue;

import java.io.File;
import java.io.IOException;

/**
 * Created by TungDX on 6/4/2015.
 */
public class Utils {
    // refers: http://stackoverflow.com/a/7167086/2128392
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    public static int getActionbarHeight(Activity activity) {
//        int attr;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            attr = android.R.attr.actionBarSize;
//        } else {
//            attr = android.support.v7.appcompat.R.attr.actionBarSize;
////            return 0;
//        }
//        final TypedArray styledAttributes = activity.getTheme()
//                .obtainStyledAttributes(new int[]{attr});
//        int actionbarSize = (int) styledAttributes.getDimension(0, 0);
//        styledAttributes.recycle();
//        return actionbarSize;
//    }
//    public static int getActionbarHeight(Activity activity) {
//        int height;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            height = activity.getActionBar().getHeight();
//        } else {
//            height = ((ActionBarActivity) activity).getSupportActionBar().getHeight();
//
//        }
//        return height;
//    }
    public static int getActionbarHeight(Activity activity) {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv,
                    true))
                actionBarHeight = TypedValue.complexToDimensionPixelSize(
                        tv.data, activity.getResources().getDisplayMetrics());
        } else {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                    activity.getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    /**
     * Create temp file. If has external storage create in external else create
     * in internal.
     *
     * @param context
     * @return
     * @throws IOException
     */
    public static File createTempFile(Context context) throws IOException {
        if (!hasExternalStorage()) {
            return createTempFile(context, context.getCacheDir());
        } else {
            return createTempFile(context,
                    context.getExternalFilesDir("caches"));
        }
    }

    /**
     * Check external exist or not.
     *
     * @return
     */
    public static boolean hasExternalStorage() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * Create temp file in folder
     *
     * @param context
     * @param folder  where place temp file
     * @return
     * @throws IOException
     */
    public static File createTempFile(Context context, File folder)
            throws IOException {
        String prefix = String.valueOf(System.currentTimeMillis());
        return File.createTempFile(prefix, null, folder);
    }
}
