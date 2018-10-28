package vn.tungdx.mediapicker

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.AbsListView.RecyclerListener
import android.widget.RelativeLayout
import androidx.cursoradapter.widget.CursorAdapter
import vn.tungdx.mediapicker.imageloader.MediaImageLoader
import vn.tungdx.mediapicker.utils.MediaUtils
import vn.tungdx.mediapicker.widget.PickerImageView
import java.util.*

/**
 * @author TUNGDX
 */

/**
 * Adapter for display media item list.
 */
class MediaAdapter(context: Context, c: Cursor, flags: Int,
                   mediaListSelected: MutableList<MediaItem>?, private val mMediaImageLoader: MediaImageLoader,
                   private var mMediaType: Int, private val mMediaOptions: MediaOptions) : CursorAdapter(context, c, flags), RecyclerListener {
    private var mMediaListSelected: MutableList<MediaItem> = ArrayList()
    private var mItemHeight = 0
    // set numcols
    var numColumns = 0
    private val mImageViewLayoutParams: RelativeLayout.LayoutParams
    private val mPickerImageViewSelected = ArrayList<PickerImageView>()

    /**
     * @return List of [MediaItem] selected.
     */
    /**
     * Set list of [MediaItem] selected.
     *
     * @param list
     */
    var mediaSelectedList: MutableList<MediaItem>
        get() = mMediaListSelected
        set(list) {
            mMediaListSelected = list
        }

    constructor(context: Context, c: Cursor, flags: Int,
                mediaImageLoader: MediaImageLoader, mediaType: Int, mediaOptions: MediaOptions) : this(context, c, flags, null, mediaImageLoader, mediaType, mediaOptions) {
    }

    init {
        if (mediaListSelected != null)
            mMediaListSelected = mediaListSelected
        mImageViewLayoutParams = RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val holder = view.tag as ViewHolder
        val uri: Uri
        if (mMediaType == MediaItem.PHOTO) {
            uri = MediaUtils.getPhotoUri(cursor)
            holder.thumbnail!!.visibility = View.GONE
        } else {
            uri = MediaUtils.getVideoUri(cursor)
            holder.thumbnail!!.visibility = View.VISIBLE
        }
        val isSelected = isSelected(uri)
        holder.imageView!!.isSelected = isSelected
        if (isSelected) {
            mPickerImageViewSelected.add(holder.imageView!!)
        }
        mMediaImageLoader.displayImage(uri, holder.imageView!!)
    }

    override fun newView(context: Context, cursor: Cursor, viewGroup: ViewGroup): View {
        val holder = ViewHolder()
        val root = View
                .inflate(mContext, R.layout.list_item_mediapicker, null)
        holder.imageView = root.findViewById<View>(R.id.thumbnail) as PickerImageView
        holder.thumbnail = root.findViewById(R.id.overlay)

        holder.imageView!!.layoutParams = mImageViewLayoutParams
        // Check the height matches our calculated column width
        if (holder.imageView!!.layoutParams.height != mItemHeight) {
            holder.imageView!!.layoutParams = mImageViewLayoutParams
        }
        root.tag = holder
        return root
    }

    private inner class ViewHolder {
        internal var imageView: PickerImageView? = null
        internal var thumbnail: View? = null
    }

    fun hasSelected(): Boolean {
        return mMediaListSelected.size > 0
    }

    /**
     * Check media uri is selected or not.
     *
     * @param uri Uri of media item (photo, video)
     * @return true if selected, false otherwise.
     */
    fun isSelected(uri: Uri?): Boolean {
        if (uri == null)
            return false
        for (item in mMediaListSelected) {
            if (item.uriOrigin == uri)
                return true
        }
        return false
    }

    /**
     * Check [MediaItem] is selected or not.
     *
     * @param item [MediaItem] to check.
     * @return true if selected, false otherwise.
     */
    fun isSelected(item: MediaItem): Boolean {
        return mMediaListSelected.contains(item)
    }

    /**
     * Set [MediaItem] selected.
     *
     * @param item [MediaItem] to selected.
     */
    fun setMediaSelected(item: MediaItem) {
        syncMediaSelectedAsOptions()
        if (!mMediaListSelected.contains(item))
            mMediaListSelected.add(item)
    }

    /**
     * If item selected then change to unselected and unselected to selected.
     *
     * @param item Item to update.
     */
    fun updateMediaSelected(item: MediaItem,
                            pickerImageView: PickerImageView) {
        if (mMediaListSelected.contains(item)) {
            mMediaListSelected.remove(item)
            pickerImageView.isSelected = false
            this.mPickerImageViewSelected.remove(pickerImageView)
        } else {
            val value = syncMediaSelectedAsOptions()
            if (value) {
                for (picker in this.mPickerImageViewSelected) {
                    picker.isSelected = false
                }
                this.mPickerImageViewSelected.clear()
            }
            mMediaListSelected.add(item)
            pickerImageView.isSelected = true
            this.mPickerImageViewSelected.add(pickerImageView)
        }
    }

    /**
     * Whether clear or not media selected as options.
     *
     * @return true if clear, false otherwise.
     */
    private fun syncMediaSelectedAsOptions(): Boolean {
        when (mMediaType) {
            MediaItem.PHOTO -> if (!mMediaOptions.canSelectMultiPhoto()) {
                mMediaListSelected.clear()
                return true
            }
            MediaItem.VIDEO -> if (!mMediaOptions.canSelectMultiVideo()) {
                mMediaListSelected.clear()
                return true
            }
            else -> {
            }
        }
        return false
    }

    /**
     * [MediaItem.VIDEO] or [MediaItem.PHOTO]
     *
     * @param mediaType
     */
    fun setMediaType(mediaType: Int) {
        mMediaType = mediaType
    }

    // set photo item height
    fun setItemHeight(height: Int) {
        if (height == mItemHeight) {
            return
        }
        mItemHeight = height
        mImageViewLayoutParams.height = height
        mImageViewLayoutParams.width = height
        notifyDataSetChanged()
    }

    override fun onMovedToScrapHeap(view: View) {
        val imageView = view
                .findViewById<View>(R.id.thumbnail) as PickerImageView
        mPickerImageViewSelected.remove(imageView)
    }

    fun onDestroyView() {
        mPickerImageViewSelected.clear()
    }
}