package vn.tungdx.mediapicker.activities

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.MediaStore.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView.LayoutParams
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import vn.tungdx.mediapicker.*
import vn.tungdx.mediapicker.utils.MediaUtils
import vn.tungdx.mediapicker.utils.Utils
import vn.tungdx.mediapicker.widget.HeaderGridView
import vn.tungdx.mediapicker.widget.PickerImageView
import java.util.*


/**
 * @author TUNGDX
 */

/**
 * Display list of videos, photos from [MediaStore] and select one or many
 * item from list depends on [MediaOptions] that passed when open media
 * picker.
 */
class MediaPickerFragment : BaseFragment(), LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

    private var mGridView: HeaderGridView? = null
    private var mNoItemView: TextView? = null
    private var mMediaAdapter: MediaAdapter? = null
    private var mMediaOptions: MediaOptions? = null
    private var mMediaSelectedListener: MediaSelectedListener? = null
    private var mSavedInstanceState: Bundle? = null
    var mediaSelectedList: List<MediaItem>? = null
        private set

    var mediaType: Int = 0
        private set
    private var mPhotoSize: Int = 0
    private var mPhotoSpacing: Int = 0

    init {
        mSavedInstanceState = Bundle()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        mMediaSelectedListener = activity as MediaSelectedListener?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mMediaOptions = savedInstanceState
                    .getParcelable(MediaPickerActivity.EXTRA_MEDIA_OPTIONS)
            mediaType = savedInstanceState.getInt(KEY_MEDIA_TYPE)
            mediaSelectedList = savedInstanceState
                    .getParcelableArrayList(KEY_MEDIA_SELECTED_LIST)
            mSavedInstanceState = savedInstanceState
        } else {
            mMediaOptions = arguments!!.getParcelable(
                    MediaPickerActivity.EXTRA_MEDIA_OPTIONS)
            if (mMediaOptions!!.canSelectPhotoAndVideo() || mMediaOptions!!.canSelectPhoto()) {
                mediaType = MediaItem.PHOTO
            } else {
                mediaType = MediaItem.VIDEO
            }
            mediaSelectedList = mMediaOptions!!.mediaListSelected
            // Override mediaType by 1st item media if has media selected.
            if (mediaSelectedList != null && mediaSelectedList!!.size > 0) {
                mediaType = mediaSelectedList!![0].type
            }
        }
        // get the photo size and spacing
        mPhotoSize = resources.getDimensionPixelSize(
                R.dimen.picker_photo_size)
        mPhotoSpacing = resources.getDimensionPixelSize(
                R.dimen.picker_photo_spacing)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_mediapicker, container,
                false)
        initView(root)
        return root
    }

    private fun requestPhotos(isRestart: Boolean) {
        requestMedia(Images.Media.EXTERNAL_CONTENT_URI,
                MediaUtils.PROJECT_PHOTO, isRestart)
    }

    private fun requestVideos(isRestart: Boolean) {
        requestMedia(Video.Media.EXTERNAL_CONTENT_URI,
                MediaUtils.PROJECT_VIDEO, isRestart)
    }

    private fun requestMedia(uri: Uri, projects: Array<String>, isRestart: Boolean) {
        val bundle = Bundle()
        bundle.putStringArray(LOADER_EXTRA_PROJECT, projects)
        bundle.putString(LOADER_EXTRA_URI, uri.toString())
        if (isRestart)
            loaderManager.restartLoader(0, bundle, this)
        else
            loaderManager.initLoader(0, bundle, this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mGridView != null) {
            mSavedInstanceState!!.putParcelable(KEY_GRID_STATE,
                    mGridView!!.onSaveInstanceState())
        }
        mSavedInstanceState!!.putParcelable(
                MediaPickerActivity.EXTRA_MEDIA_OPTIONS, mMediaOptions)
        mSavedInstanceState!!.putInt(KEY_MEDIA_TYPE, mediaType)
        mSavedInstanceState!!.putParcelableArrayList(KEY_MEDIA_SELECTED_LIST,
                mediaSelectedList as ArrayList<MediaItem>?)
        outState.putAll(mSavedInstanceState)
    }

    override fun onCreateLoader(id: Int, bundle: Bundle?): Loader<Cursor> {
        val uri = Uri.parse(bundle!!.getString(LOADER_EXTRA_URI))
        val projects = bundle.getStringArray(LOADER_EXTRA_PROJECT)
        val order = MediaColumns.DATE_ADDED + " DESC"
        return CursorLoader(mContext!!, uri, projects, null, null, order)
    }

    private fun bindData(cursor: Cursor?) {
        if (cursor == null || cursor.count <= 0) {
            switchToError()
            return
        }
        switchToData()
        if (mMediaAdapter == null) {
            mMediaAdapter = MediaAdapter(mContext!!, cursor, 0,
                    mMediaImageLoader, mediaType, mMediaOptions!!)
        } else {
            mMediaAdapter!!.setMediaType(mediaType)
            mMediaAdapter!!.swapCursor(cursor)
        }
        if (mGridView!!.adapter == null) {
            mGridView!!.adapter = mMediaAdapter
            mGridView!!.setRecyclerListener(mMediaAdapter)
        }
        val state = mSavedInstanceState!!.getParcelable<Parcelable>(KEY_GRID_STATE)
        if (state != null) {
            mGridView!!.onRestoreInstanceState(state)
        }
        if (mediaSelectedList != null) {
            mMediaAdapter!!.mediaSelectedList = mediaSelectedList!!.toMutableList()
        }
        mMediaAdapter!!.notifyDataSetChanged()
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        bindData(cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // Preference:http://developer.android.com/guide/components/loaders.html#callback
        if (mMediaAdapter != null)
            mMediaAdapter!!.swapCursor(null)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int,
                             id: Long) {
        val `object` = parent.adapter.getItem(position)
        if (`object` is Cursor) {
            val uri: Uri
            if (mediaType == MediaItem.PHOTO) {
                uri = MediaUtils.getPhotoUri(`object`)
            } else {
                uri = MediaUtils.getVideoUri(`object`)
            }
            val pickerImageView = view
                    .findViewById<View>(R.id.thumbnail) as PickerImageView
            val mediaItem = MediaItem(mediaType, uri)
            mMediaAdapter!!.updateMediaSelected(mediaItem, pickerImageView)
            mediaSelectedList = mMediaAdapter!!.mediaSelectedList

            if (mMediaAdapter!!.hasSelected()) {
                mMediaSelectedListener!!.onHasSelected(mMediaAdapter!!
                        .mediaSelectedList)
            } else {
                mMediaSelectedListener!!.onHasNoSelected()
            }
        }
    }

    fun switchMediaSelector() {
        if (!mMediaOptions!!.canSelectPhotoAndVideo())
            return
        mediaType = if (mediaType == MediaItem.PHOTO) {
            MediaItem.VIDEO
        } else {
            MediaItem.PHOTO
        }
        when (mediaType) {
            MediaItem.PHOTO -> requestPhotos(true)
            MediaItem.VIDEO -> requestVideos(true)
            else -> {
            }
        }
    }

    fun hasMediaSelected(): Boolean {
        return mediaSelectedList != null && mediaSelectedList!!.size > 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mGridView != null) {
            mSavedInstanceState!!.putParcelable(KEY_GRID_STATE,
                    mGridView!!.onSaveInstanceState())
            mGridView = null
        }
        if (mMediaAdapter != null) {
            mMediaAdapter!!.onDestroyView()
        }
    }

    private fun switchToData() {
        mNoItemView!!.visibility = View.GONE
        mNoItemView!!.text = null
        mGridView!!.visibility = View.VISIBLE
    }

    private fun switchToError() {
        mNoItemView!!.visibility = View.VISIBLE
        mNoItemView!!.setText(R.string.picker_no_items)
        mGridView!!.visibility = View.GONE
    }

    private fun initView(view: View) {
        mGridView = view.findViewById<View>(R.id.grid) as HeaderGridView
        val header = View(activity)
        val params = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                Utils.getActionbarHeight(activity!!))
        header.layoutParams = params
        mGridView!!.addHeaderView(header)

        mGridView!!.onItemClickListener = this
        mNoItemView = view.findViewById<View>(R.id.no_data) as TextView

        // get the view tree observer of the grid and set the height and numcols
        // dynamically
        mGridView!!.viewTreeObserver.addOnGlobalLayoutListener {
            if (mMediaAdapter != null && mMediaAdapter!!.numColumns == 0) {
                val numColumns = Math.floor((mGridView!!
                        .width / (mPhotoSize + mPhotoSpacing)).toDouble()).toInt()
                if (numColumns > 0) {
                    val columnWidth = mGridView!!.width / numColumns - mPhotoSpacing
                    mMediaAdapter!!.numColumns = numColumns
                    mMediaAdapter!!.setItemHeight(columnWidth)
                }
            }
        }
    }

    private fun requestMedia() {
        if (mediaType == MediaItem.PHOTO) {
            requestPhotos(false)
        } else {
            requestVideos(false)
        }
    }

    private fun performRequestMedia() {
        if (hasPermission()) {
            requestMedia()
        } else {
            requestReadingExternalStoragePermission()
        }
    }

    private fun requestReadingExternalStoragePermission() {
        requestPermissions(arrayOf("android.permission.READ_EXTERNAL_STORAGE"),
                REQUEST_READ_EXTERNAL_STORAGE)
    }

    private fun hasPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return permission == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestMedia()
                }
                return
            }
        }
        //handle permissions that passed from the host activity.
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestMedia()
        }
    }

    override fun onStart() {
        super.onStart()
        performRequestMedia()
    }

    companion object {
        private val LOADER_EXTRA_URI = "loader_extra_uri"
        private val LOADER_EXTRA_PROJECT = "loader_extra_project"
        private val KEY_MEDIA_TYPE = "media_type"
        private val KEY_GRID_STATE = "grid_state"
        private val KEY_MEDIA_SELECTED_LIST = "media_selected_list"
        private val REQUEST_READ_EXTERNAL_STORAGE = 100

        fun newInstance(options: MediaOptions): MediaPickerFragment {
            val bundle = Bundle()
            bundle.putParcelable(MediaPickerActivity.EXTRA_MEDIA_OPTIONS, options)
            val fragment = MediaPickerFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}