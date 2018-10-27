package vn.tungdx.mediapicker.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.exifinterface.media.ExifInterface
import com.theartofdev.edmodo.cropper.CropImageView
import vn.tungdx.mediapicker.CropListener
import vn.tungdx.mediapicker.MediaItem
import vn.tungdx.mediapicker.MediaOptions
import vn.tungdx.mediapicker.R
import vn.tungdx.mediapicker.utils.MediaUtils
import vn.tungdx.mediapicker.utils.Utils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * @author TUNGDX
 */

/**
 * For crop photo. Only crop one item at same time.
 */
class PhotoCropFragment : BaseFragment(), OnClickListener {

    private var mCropListener: CropListener? = null
    private var mMediaOptions: MediaOptions? = null
    private var mMediaItemSelected: MediaItem? = null
    private var mCropImageView: CropImageView? = null
    private var mRotateLeft: View? = null
    private var mRotateRight: View? = null
    private var mCancel: View? = null
    private var mSave: View? = null
    private var mDialog: ProgressDialog? = null
    private var mSaveFileCroppedTask: SaveFileCroppedTask? = null

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        mCropListener = activity as CropListener?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mMediaItemSelected = savedInstanceState
                    .getParcelable(EXTRA_MEDIA_SELECTED)
            mMediaOptions = savedInstanceState
                    .getParcelable(EXTRA_MEDIA_OPTIONS)
        } else {
            val bundle = arguments
            mMediaItemSelected = bundle!!.getParcelable(EXTRA_MEDIA_SELECTED)
            mMediaOptions = bundle.getParcelable(EXTRA_MEDIA_OPTIONS)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_MEDIA_OPTIONS, mMediaOptions)
        outState.putParcelable(EXTRA_MEDIA_SELECTED, mMediaItemSelected)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_mediapicker_crop, container, false)
        init(root)
        return root
    }

    private fun init(view: View) {
        mCropImageView = view.findViewById(R.id.crop)
        mRotateLeft = view.findViewById(R.id.rotate_left)
        mRotateRight = view.findViewById(R.id.rotate_right)
        mCancel = view.findViewById(R.id.cancel)
        mSave = view.findViewById(R.id.save)

        mRotateLeft!!.setOnClickListener(this)
        mRotateRight!!.setOnClickListener(this)
        mSave!!.setOnClickListener(this)
        mCancel!!.setOnClickListener(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mCropImageView!!.setFixedAspectRatio(mMediaOptions!!.isFixAspectRatio)
        mCropImageView!!.setAspectRatio(mMediaOptions!!.aspectX,
                mMediaOptions!!.aspectY)
        var filePath: String? = null
        val scheme = mMediaItemSelected!!.uriOrigin?.scheme
        if (scheme == ContentResolver.SCHEME_CONTENT) {
            filePath = MediaUtils.getRealImagePathFromURI(activity!!
                    .contentResolver, mMediaItemSelected!!.uriOrigin)
        } else if (scheme == ContentResolver.SCHEME_FILE) {
            filePath = mMediaItemSelected!!.uriOrigin?.path
        }
        if (TextUtils.isEmpty(filePath)) {
            Log.e("PhotoCrop", "not found file path")
            fragmentManager!!.popBackStack()
            return
        }
        val width = resources.displayMetrics.widthPixels / 3 * 2
        val bitmap = MediaUtils.decodeSampledBitmapFromFile(filePath, width,
                width)
        try {
            val exif = ExifInterface(filePath!!)
            mCropImageView!!.setImageBitmap(bitmap, exif)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.rotate_left) {// must catch exception, maybe bitmap in CropImage null
            try {
                mCropImageView!!.rotateImage(-90)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else if (i == R.id.rotate_right) {
            try {
                mCropImageView!!.rotateImage(90)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else if (i == R.id.cancel) {
            fragmentManager!!.popBackStack()

        } else if (i == R.id.save) {
            mSaveFileCroppedTask = SaveFileCroppedTask(activity!!)
            mSaveFileCroppedTask!!.execute()

        } else {
        }
    }

    private fun saveBitmapCropped(bitmap: Bitmap?): Uri? {
        if (bitmap == null) {
            return null
        }
        try {
            val file: File
            if (mMediaOptions!!.croppedFile != null) {
                file = mMediaOptions!!.croppedFile!!
            } else {
                file = Utils.createTempFile(mContext)
            }
            val success = bitmap.compress(CompressFormat.JPEG, 100,
                    FileOutputStream(file))
            if (success) {
                return Uri.fromFile(file)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    private inner class SaveFileCroppedTask(activity: Activity) : AsyncTask<Void, Void, Uri>() {
        private val reference: WeakReference<Activity>

        init {
            reference = WeakReference(activity)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            if (reference.get() != null && mDialog == null || !mDialog!!.isShowing) {
                mDialog = ProgressDialog.show(reference.get(), null, reference
                        .get()!!.getString(R.string.waiting), false, false)
            }
        }

        override fun doInBackground(vararg params: Void): Uri? {
            var uri: Uri? = null
            // must try-catch, maybe getCroppedImage() method crash because not
            // set bitmap in mCropImageView
            try {
                var bitmap: Bitmap? = mCropImageView!!.croppedImage
                uri = saveBitmapCropped(bitmap)
                if (bitmap != null) {
                    bitmap.recycle()
                    bitmap = null
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return uri
        }

        override fun onPostExecute(result: Uri) {
            super.onPostExecute(result)
            if (mDialog != null) {
                mDialog!!.dismiss()
                mDialog = null
            }
            mMediaItemSelected!!.uriCropped = result
            mCropListener!!.onSuccess(mMediaItemSelected)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mSaveFileCroppedTask != null) {
            mSaveFileCroppedTask!!.cancel(true)
            mSaveFileCroppedTask = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCropImageView = null
        mDialog = null
        mSave = null
        mCancel = null
        mRotateLeft = null
        mRotateRight = null
    }

    companion object {
        private val EXTRA_MEDIA_SELECTED = "extra_media_selected"
        private val EXTRA_MEDIA_OPTIONS = "extra_media_options"

        fun newInstance(item: MediaItem,
                        options: MediaOptions): PhotoCropFragment {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_MEDIA_SELECTED, item)
            bundle.putParcelable(EXTRA_MEDIA_OPTIONS, options)
            val cropFragment = PhotoCropFragment()
            cropFragment.arguments = bundle
            return cropFragment
        }
    }
}