package vn.tungdx.mediapicker

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils

import vn.tungdx.mediapicker.activities.MediaPickerActivity
import vn.tungdx.mediapicker.utils.MediaUtils

/**
 * @author TUNGDX
 */

/**
 * Contains information of photo or video that selected and return back in
 * [MediaPickerActivity]
 */
class MediaItem : Parcelable {
    /**
     * @return type of media item. Whether [.PHOTO] or [.VIDEO]
     */
    /**
     * Set type of media.
     *
     * @param type is [.PHOTO] or [.VIDEO]
     */
    var type: Int = 0
    var uriCropped: Uri? = null
    var uriOrigin: Uri? = null

    val isVideo: Boolean
        get() = type == VIDEO

    val isPhoto: Boolean
        get() = type == PHOTO

    /**
     * @param mediaType Whether [.PHOTO] or [.VIDEO]
     * @param uriOrigin [Uri] of media item.
     */
    constructor(mediaType: Int, uriOrigin: Uri) {
        this.type = mediaType
        this.uriOrigin = uriOrigin
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(this.type)
        if (this.uriCropped == null) {
            dest.writeString(null)
        } else {
            dest.writeString(this.uriCropped!!.toString())
        }
        if (this.uriOrigin == null) {
            dest.writeString(null)
        } else {
            dest.writeString(this.uriOrigin!!.toString())
        }
    }

    constructor(`in`: Parcel) {
        this.type = `in`.readInt()
        val crop = `in`.readString()
        if (!TextUtils.isEmpty(crop))
            this.uriCropped = Uri.parse(crop)
        val origin = `in`.readString()
        if (!TextUtils.isEmpty(origin))
            this.uriOrigin = Uri.parse(origin)
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (uriCropped == null) 0 else uriCropped!!.hashCode()
        result = prime * result + if (uriOrigin == null) 0 else uriOrigin!!.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (javaClass != other.javaClass)
            return false
        val temp = other as MediaItem?
        if (uriCropped == null) {
            if (temp!!.uriCropped != null)
                return false
        } else if (uriCropped != temp!!.uriCropped)
            return false
        if (uriOrigin == null) {
            if (temp.uriOrigin != null)
                return false
        } else if (uriOrigin != temp.uriOrigin)
            return false
        return true
    }

    override fun toString(): String {
        return ("MediaItem [type=" + type + ", uriCropped=" + uriCropped
                + ", uriOrigin=" + uriOrigin + "]")
    }

    /**
     * @param context
     * @return Path of origin file.
     */
    fun getPathOrigin(context: Context): String? {
        return getPathFromUri(context, uriOrigin)
    }

    /**
     * @param context
     * @return Path of cropped file.
     */
    fun getPathCropped(context: Context): String? {
        return getPathFromUri(context, uriCropped)
    }

    private fun getPathFromUri(context: Context, uri: Uri?): String? {
        if (uri == null)
            return null
        val scheme = uri.scheme
        if (scheme == ContentResolver.SCHEME_FILE) {
            return uri.path
        } else if (scheme == ContentResolver.SCHEME_CONTENT) {
            return if (isPhoto) {
                MediaUtils.getRealImagePathFromURI(context.contentResolver, uri)
            } else {
                MediaUtils.getRealVideoPathFromURI(context.contentResolver, uri)
            }
        }

        return uri.toString()
    }

    companion object {

        const val PHOTO = 1

        const val VIDEO = 2

        @JvmField
        val CREATOR: Parcelable.Creator<MediaItem> = object : Parcelable.Creator<MediaItem> {

            override fun newArray(size: Int): Array<MediaItem?> {
                return arrayOfNulls(size)
            }

            override fun createFromParcel(source: Parcel): MediaItem {
                return MediaItem(source)
            }
        }
    }
}