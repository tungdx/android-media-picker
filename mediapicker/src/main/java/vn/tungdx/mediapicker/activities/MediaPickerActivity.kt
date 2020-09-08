package vn.tungdx.mediapicker.activities

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.os.FileObserver
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import vn.tungdx.mediapicker.*
import vn.tungdx.mediapicker.imageloader.MediaImageLoader
import vn.tungdx.mediapicker.imageloader.MediaImageLoaderImpl
import vn.tungdx.mediapicker.utils.MediaUtils
import vn.tungdx.mediapicker.utils.MessageUtils
import vn.tungdx.mediapicker.utils.RecursiveFileObserver
import vn.tungdx.mediapicker.utils.Utils
import java.io.File
import java.io.IOException
import java.util.*


/**
 * @author TUNGDX
 */

/**
 * Use this activity for pickup photos or videos (media).
 *
 *
 * How to use:
 *
 *  *
 * Step1: Open media picker: <br></br>
 * - If using in activity use:
 * [MediaPickerActivity.open] or
 * [MediaPickerActivity.open]
 * - If using in fragment use:
 * [MediaPickerActivity.open] or
 * [MediaPickerActivity.open] <br></br>
 *
 *  *
 * Step2: Get out media that selected in
 * [Activity.onActivityResult] of activity or fragment
 * that open media picker. Use
 * [MediaPickerActivity.getMediaItemSelected] to get out media
 * list that selected.
 *
 *
 * *Note: Videos or photos return back depends on [MediaOptions] passed
 * to [.open] *
 *
 */
class MediaPickerActivity : AppCompatActivity(), MediaSelectedListener, CropListener, FragmentManager.OnBackStackChangedListener, FragmentHost {

    private var mMediaOptions: MediaOptions? = null
    private var mMediaSwitcher: MenuItem? = null
    private var mPhoto: MenuItem? = null
    private var mVideo: MenuItem? = null
    private var mDone: MenuItem? = null

    private var mPhotoFileCapture: File? = null
    private var mFilesCreatedWhileCapturePhoto: MutableList<File>? = null
    private var mFileObserver: RecursiveFileObserver? = null
    private var mFileObserverTask: FileObserverTask? = null
    private var takePhotoPending: Boolean = false
    private var takeVideoPending: Boolean = false

    override val imageLoader: MediaImageLoader
        get() = MediaImageLoaderImpl(applicationContext)

    private val mOnFileCreatedListener = object : RecursiveFileObserver.OnFileCreatedListener {

        override fun onFileCreate(file: File) {
            if (mFilesCreatedWhileCapturePhoto == null) {
                mFilesCreatedWhileCapturePhoto = ArrayList()
            }
            mFilesCreatedWhileCapturePhoto!!.add(file)
        }
    }

    private val activePage: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: not support change orientation right now (because out of
        // memory when crop image and change orientation, must check third party
        // to crop image again).
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_mediapicker)
        if (savedInstanceState != null) {
            mMediaOptions = savedInstanceState.getParcelable(EXTRA_MEDIA_OPTIONS)
            mPhotoFileCapture = savedInstanceState.getSerializable(KEY_PHOTOFILE_CAPTURE) as File
        } else {
            mMediaOptions = intent.getParcelableExtra(EXTRA_MEDIA_OPTIONS)
            if (mMediaOptions == null) {
                throw IllegalArgumentException(
                        "MediaOptions must be not null, you should use MediaPickerActivity.open"
                                + "(Activity activity, int requestCode,MediaOptions options) "
                                + "method instead.")
            }
        }
        if (activePage == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container,
                            MediaPickerFragment.newInstance(mMediaOptions!!))
                    .commit()
        }
        supportFragmentManager.addOnBackStackChangedListener(this)
        if (supportActionBar != null) {
            supportActionBar!!.setBackgroundDrawable(
                    resources.getDrawable(R.drawable.picker_actionbar_translucent))
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.mediapicker_main, menu)
        mPhoto = menu.findItem(R.id.take_photo)
        mVideo = menu.findItem(R.id.take_video)
        mMediaSwitcher = menu.findItem(R.id.media_switcher)
        mDone = menu.findItem(R.id.done)
        syncActionbar()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        supportFragmentManager.removeOnBackStackChangedListener(this)
        cancelFileObserverTask()
        stopWatchingFile()
        mFilesCreatedWhileCapturePhoto = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == android.R.id.home) {
            finish()

        } else if (i == R.id.take_photo) {
            performTakePhotoRequest()
            takePhotoPending = true
            return true
        } else if (i == R.id.take_video) {
            performTakeVideoRequest()
            takeVideoPending = true
            return true
        } else if (i == R.id.media_switcher) {
            val activePage = this@MediaPickerActivity.activePage
            if (mMediaOptions!!.canSelectPhotoAndVideo() && activePage is MediaPickerFragment) {
                activePage.switchMediaSelector()
                syncIconMenu(activePage.mediaType)
            }
            return true
        } else if (i == R.id.done) {
            val activePage = this@MediaPickerActivity.activePage
            val isPhoto = (activePage as MediaPickerFragment).mediaType == MediaItem.PHOTO
            if (isPhoto) {
                if (mMediaOptions!!.isCropped && !mMediaOptions!!.canSelectMultiPhoto()) {
                    // get first item in list (pos=0) because can only crop 1 image at same time.
                    val mediaItem = MediaItem(MediaItem.PHOTO, activePage.mediaSelectedList!![0]
                            .uriOrigin!!)
                    showCropFragment(mediaItem, mMediaOptions!!)
                } else {
                    returnBackData(activePage.mediaSelectedList)
                }
            } else {
                if (mMediaOptions!!.canSelectMultiVideo()) {
                    returnBackData(activePage
                            .mediaSelectedList)
                } else {
                    // only get 1st item regardless of have many.
                    returnVideo(activePage
                            .mediaSelectedList!![0].uriOrigin)
                }
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_MEDIA_OPTIONS, mMediaOptions)
        outState.putSerializable(KEY_PHOTOFILE_CAPTURE, mPhotoFileCapture)
    }

    override fun onHasNoSelected() {
        mDone!!.isVisible = false
        syncActionbar()
    }

    override fun onHasSelected(mediaSelectedList: List<MediaItem>) {
        showDone()
    }

    private fun showDone() {
        mDone!!.isVisible = true
        mPhoto!!.isVisible = false
        mVideo!!.isVisible = false
        mMediaSwitcher!!.isVisible = false
    }

    private fun syncMediaOptions() {
        // handle media options
        mMediaSwitcher!!.isVisible = mMediaOptions!!.canSelectPhotoAndVideo()
        mPhoto!!.isVisible = mMediaOptions!!.canSelectPhoto()
        mVideo!!.isVisible = mMediaOptions!!.canSelectVideo()
    }

    private fun syncIconMenu(mediaType: Int) {
        when (mediaType) {
            MediaItem.PHOTO -> mMediaSwitcher!!.setIcon(R.drawable.ab_picker_video_2)
            MediaItem.VIDEO -> mMediaSwitcher!!.setIcon(R.drawable.ab_picker_camera2)
            else -> {
            }
        }
    }

    private fun returnBackData(mediaSelectedList: List<MediaItem>?) {
        val data = Intent()
        data.putParcelableArrayListExtra(EXTRA_MEDIA_SELECTED,
                mediaSelectedList as ArrayList<MediaItem>?)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            var file: File? = mMediaOptions!!.photoFile
            if (file == null) {
                try {
                    file = MediaUtils.createDefaultImageFile(this)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (file != null) {
                mPhotoFileCapture = file
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Utils.getUriForFile(this, file))
                startActivityForResult(takePictureIntent, REQUEST_PHOTO_CAPTURE)
                mFileObserverTask = FileObserverTask()
                mFileObserverTask!!.execute()
            }
        }
    }

    private fun takeVideo() {
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (takeVideoIntent.resolveActivity(packageManager) != null) {
            var max = mMediaOptions!!.maxVideoDuration
            if (max != Integer.MAX_VALUE) {
                // /=1000 because it's must in seconds.
                max /= 1000
                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, max)
                if (mMediaOptions!!.isShowWarningVideoDuration) {
                    val dialog = MediaPickerErrorDialog
                            .newInstance(MessageUtils.getWarningMessageVideoDuration(
                                    applicationContext, max))
                    dialog.setOnOKClickListener(DialogInterface.OnClickListener { _, _ ->
                        startActivityForResult(takeVideoIntent,
                                REQUEST_VIDEO_CAPTURE)
                    })
                    dialog.show(supportFragmentManager, null)
                } else {
                    startActivityForResult(takeVideoIntent,
                            REQUEST_VIDEO_CAPTURE)
                }
            } else {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
            }
        }
    }

    /**
     * In some HTC devices (maybe others), duplicate image when captured with
     * extra_output. This method will try delete duplicate image. It's prefer
     * default image by camera than extra output.
     */
    private fun tryCorrectPhotoFileCaptured() {
        if (mPhotoFileCapture == null || mFilesCreatedWhileCapturePhoto == null
                || mFilesCreatedWhileCapturePhoto!!.size <= 0) {
            return
        }
        val captureSize = mPhotoFileCapture!!.length()
        for (file in mFilesCreatedWhileCapturePhoto!!) {
            if (MediaUtils
                            .isImageExtension(MediaUtils.getFileExtension(file))
                    && file.length() >= captureSize
                    && file != mPhotoFileCapture) {
                val value = mPhotoFileCapture!!.delete()
                mPhotoFileCapture = file
                Log.i(TAG,
                        String.format(
                                "Try correct photo file: Delete duplicate photos in [%s] [%s]",
                                mPhotoFileCapture, value))
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        cancelFileObserverTask()
        stopWatchingFile()
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_PHOTO_CAPTURE -> {
                    tryCorrectPhotoFileCaptured()
                    if (mPhotoFileCapture != null) {
                        MediaUtils.galleryAddPic(applicationContext, mPhotoFileCapture!!)
                        if (mMediaOptions!!.isCropped) {
                            val item = MediaItem(MediaItem.PHOTO, Uri.fromFile(mPhotoFileCapture))
                            showCropFragment(item, mMediaOptions!!)
                        } else {
                            val item = MediaItem(MediaItem.PHOTO, Uri.fromFile(mPhotoFileCapture))
                            val list = ArrayList<MediaItem>()
                            list.add(item)
                            returnBackData(list)
                        }
                    }
                }
                REQUEST_VIDEO_CAPTURE -> returnVideo(data!!.data)
                else -> {
                }
            }
        }
    }

    private fun showCropFragment(mediaItem: MediaItem, options: MediaOptions) {
        val fragment = PhotoCropFragment.newInstance(mediaItem, options)
        val transaction = supportFragmentManager
                .beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onSuccess(mediaItem: MediaItem) {
        val list = ArrayList<MediaItem>()
        list.add(mediaItem)
        returnBackData(list)
    }

    override fun onBackStackChanged() {
        syncActionbar()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        syncActionbar()
    }

    fun syncActionbar() {
        val fragment = activePage
        if (fragment is PhotoCropFragment) {
            hideAllOptionsMenu()
            supportActionBar!!.hide()
        } else if (fragment is MediaPickerFragment) {
            supportActionBar!!.show()
            syncMediaOptions()
            val pickerFragment = fragment as MediaPickerFragment?
            syncIconMenu(pickerFragment!!.mediaType)
            if (pickerFragment.hasMediaSelected()) {
                showDone()
            } else {
                mDone!!.isVisible = false
            }
        }
    }

    private fun hideAllOptionsMenu() {
        if (mPhoto != null) {
            mPhoto!!.isVisible = false
        }
        if (mVideo != null) {
            mVideo!!.isVisible = false
        }
        if (mMediaSwitcher != null) {
            mMediaSwitcher!!.isVisible = false
        }
        if (mDone != null) {
            mDone!!.isVisible = false
        }
    }

    /**
     * Check video duration valid or not with options.
     *
     * @return 1 if valid, otherwise is invalid. -2: not found, 0 larger than
     * accepted, -1 smaller than accepted.
     */
    private fun checkValidVideo(videoUri: Uri?): Int {
        if (videoUri == null) {
            return -2
        }
        // try get duration using MediaPlayer. (Should get duration using
        // MediaPlayer before use Uri because some devices can get duration by
        // Uri or not exactly. Ex: Asus Memo Pad8)
        var duration = MediaUtils.getDuration(applicationContext,
                MediaUtils.getRealVideoPathFromURI(contentResolver, videoUri)!!)
        if (duration == 0L) {
            // try get duration one more, by uri of video. Note: Some time can
            // not get duration by Uri after record video.(It's usually happen
            // in HTC
            // devices 2.3, maybe others)
            duration = MediaUtils.getDuration(applicationContext, videoUri)
        }
        // accept delta about < 1000 milliseconds. (ex: 10769 is still accepted
        // if limit is 10000)
        if (mMediaOptions!!.maxVideoDuration != Integer.MAX_VALUE && duration >= mMediaOptions!!.maxVideoDuration + 1000) {
            return 0
        } else if (duration == 0L || duration < mMediaOptions!!.minVideoDuration) {
            return -1
        }
        return 1
    }

    private fun returnVideo(videoUri: Uri?) {
        val code = checkValidVideo(videoUri)
        when (code) {
            // not found. should never happen. Do nothing when happen.
            -2 -> {
            }
            // smaller than min
            -1 -> {
                // in seconds
                val duration = mMediaOptions!!.minVideoDuration / 1000
                val msg = MessageUtils.getInvalidMessageMinVideoDuration(
                        applicationContext, duration)
                showVideoInvalid(msg)
            }

            // larger than max
            0 -> {
                // in seconds.
                val duration = mMediaOptions!!.maxVideoDuration / 1000
                val msg = MessageUtils.getInvalidMessageMaxVideoDuration(
                        applicationContext, duration)
                showVideoInvalid(msg)
            }
            // ok
            1 -> {
                val item = MediaItem(MediaItem.VIDEO, videoUri!!)
                val list = ArrayList<MediaItem>()
                list.add(item)
                returnBackData(list)
            }

            else -> {
            }
        }
    }

    private fun showVideoInvalid(msg: String) {
        val errorDialog = MediaPickerErrorDialog
                .newInstance(msg)
        errorDialog.show(supportFragmentManager, null)
    }

    private inner class FileObserverTask : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void): Void? {
            if (isCancelled) return null
            if (mFileObserver == null) {
                mFileObserver = RecursiveFileObserver(Environment
                        .getExternalStorageDirectory().absolutePath,
                        FileObserver.CREATE)
                mFileObserver!!
                        .setFileCreatedListener(mOnFileCreatedListener)
            }
            mFileObserver!!.startWatching()
            return null
        }
    }

    private fun cancelFileObserverTask() {
        if (mFileObserverTask != null) {
            mFileObserverTask!!.cancel(true)
            mFileObserver = null
        }
    }

    private fun stopWatchingFile() {
        if (mFileObserver != null) {
            mFileObserver!!.stopWatching()
            mFileObserver = null
        }
    }

    private fun performTakePhotoRequest() {
        if (hasCameraPermission()) {
            takePhoto()
        } else {
            requestCameraPermission()
        }
    }

    private fun performTakeVideoRequest() {
        if (hasCameraPermission()) {
            takeVideo()
        } else {
            requestCameraPermission()
        }
    }

    private fun hasCameraPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (takePhotoPending) {
                        takePhoto()
                    } else if (takeVideoPending) {
                        takeVideo()
                    }

                }
                return
            }
        }
        //pass permission result to fragments in this activity
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {
        private const val TAG = "MediaPickerActivity"

        const val EXTRA_MEDIA_OPTIONS = "extra_media_options"

        /**
         * Intent extra included when return back data in
         * [Activity.onActivityResult] of activity or fragment
         * that open media picker. Always return [ArrayList] of
         * [MediaItem]. You must always check null and size of this list
         * before handle your logic.
         */
        const val EXTRA_MEDIA_SELECTED = "extra_media_selected"
        private const val REQUEST_PHOTO_CAPTURE = 100
        private const val REQUEST_VIDEO_CAPTURE = 200

        private const val KEY_PHOTOFILE_CAPTURE = "key_photofile_capture"
        private const val REQUEST_CAMERA_PERMISSION = 300

        /**
         * Start [MediaPickerActivity] in [Activity] to pick photo or
         * video that depends on [MediaOptions] passed.
         */
        @JvmOverloads
        fun open(activity: Activity, requestCode: Int,
                 options: MediaOptions = MediaOptions.createDefault()) {
            val intent = Intent(activity, MediaPickerActivity::class.java)
            intent.putExtra(EXTRA_MEDIA_OPTIONS, options)
            activity.startActivityForResult(intent, requestCode)
        }

        /**
         * Start [MediaPickerActivity] in [Fragment] to pick photo or
         * video that depends on [MediaOptions] passed.
         */
        @JvmOverloads
        fun open(fragment: Fragment, requestCode: Int,
                 options: MediaOptions = MediaOptions.createDefault()) {
            val intent = Intent(fragment.activity,
                    MediaPickerActivity::class.java)
            intent.putExtra(EXTRA_MEDIA_OPTIONS, options)
            fragment.startActivityForResult(intent, requestCode)
        }

        /**
         * Get media item list selected from intent extra included in
         * [Activity.onActivityResult] of activity or fragment
         * that open media picker.
         *
         * @param intent In [Activity.onActivityResult] method of
         * activity or fragment that open media picker.
         * @return Always return [ArrayList] of [MediaItem]. You must
         * always check null and size of this list before handle your logic.
         */
        fun getMediaItemSelected(intent: Intent?): ArrayList<MediaItem>? {
            return intent?.getParcelableArrayListExtra(EXTRA_MEDIA_SELECTED)
        }
    }
}
/**
 * Start [MediaPickerActivity] in [Activity] with default media
 * option: [MediaOptions.createDefault]
 */
/**
 * Start [MediaPickerActivity] in [Fragment] with default media
 * option: [MediaOptions.createDefault]
 */