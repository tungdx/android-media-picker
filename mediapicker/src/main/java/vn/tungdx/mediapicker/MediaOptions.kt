package vn.tungdx.mediapicker

import android.os.Environment
import android.os.Parcel
import android.os.Parcelable
import vn.tungdx.mediapicker.MediaOptions.Builder
import vn.tungdx.mediapicker.utils.MediaUtils
import java.io.File

/**
 * @author TUNGDX
 */

/**
 *
 * Contains all options for pick one or many photos, videos. Defines:
 *
 *  * Can select many photos or videos
 *  * Crop photo or not. Can set file to save photo cropped. Can set ratio to
 * crop.
 *  * Whether choose photo or video or both in picker.
 *  * Set max, min duration of video.
 *  * Set show warning before record video.
 *
 *
 *
 * Two methods to create instance :
 *
 *  *
 * `new [MediaOptions.Builder].
 * [canSelectMultiPhoto(boolean)][Builder.canSelectMultiPhoto].
 * [setFixAspectRatio(boolean)][Builder.setFixAspectRatio].
 * [setMaxVideoDuration(int)][Builder.setMaxVideoDuration].[build()][Builder.build]`
`` *
 *  * or create default [.createDefault]
 *
 *
 */
class MediaOptions : Parcelable {
    private var canSelectMultiPhoto: Boolean = false
    private var canSelectMultiVideo: Boolean = false
    var isCropped: Boolean = false
        private set

    /**
     *
     * @return in milliseconds.
     */
    var maxVideoDuration: Int = 0
        private set

    /**
     *
     * @return in milliseconds.
     */
    var minVideoDuration: Int = 0
        private set

    private var canSelectPhoto: Boolean = false

    private var canSelectVideo: Boolean = false

    var photoFile: File? = null
        private set

    var aspectX: Int = 0
        private set

    var aspectY: Int = 0
        private set

    var isFixAspectRatio: Boolean = false
        private set

    var croppedFile: File? = null
        private set

    var mediaListSelected: List<MediaItem>? = null

    var isShowWarningVideoDuration: Boolean = false
        private set

    fun canSelectMultiPhoto(): Boolean {
        return canSelectMultiPhoto
    }

    fun canSelectMultiVideo(): Boolean {
        return canSelectMultiVideo
    }

    fun canSelectPhotoAndVideo(): Boolean {
        return canSelectPhoto && canSelectVideo
    }

    fun canSelectPhoto(): Boolean {
        return canSelectPhoto
    }

    fun canSelectVideo(): Boolean {
        return canSelectVideo
    }

    private constructor(builder: Builder) {
        this.canSelectMultiPhoto = builder.canSelectMultiPhoto
        this.canSelectMultiVideo = builder.canSelectMultiVideo
        this.isCropped = builder.isCropped
        this.maxVideoDuration = builder.maxVideoDuration
        this.minVideoDuration = builder.minVideoDuration
        this.canSelectPhoto = builder.canSelectPhoto
        this.canSelectVideo = builder.canSelectVideo
        this.photoFile = builder.photoFile
        this.aspectX = builder.aspectX
        this.aspectY = builder.aspectY
        this.isFixAspectRatio = builder.fixAspectRatio
        this.croppedFile = builder.croppedFile
        this.mediaListSelected = builder.mediaListSelected
        this.isShowWarningVideoDuration = builder.showWarningBeforeRecord
    }

    /**
     *
     * Builder for [MediaOptions]
     *
     */
    class Builder {
        internal var canSelectMultiPhoto = false
        internal var canSelectMultiVideo = false
        internal var isCropped = false
        internal var maxVideoDuration = Integer.MAX_VALUE
        internal var minVideoDuration = 0
        internal var canSelectPhoto = true
        internal var canSelectVideo = false
        internal var photoFile: File? = null
        internal var aspectX = 1
        internal var aspectY = 1
        internal var fixAspectRatio = true
        internal var croppedFile: File? = null
        internal var mediaListSelected: List<MediaItem>? = null
        internal var showWarningBeforeRecord = false

        /**
         * Should set this option = true when support multi devices and OS
         * version and set [.setMaxVideoDuration] options. (HTC
         * devices usually can not limit video's duration when record, should
         * show message warning before record)
         *
         * @param showWarningBeforeRecord
         * Default is false.
         */
        fun setShowWarningBeforeRecordVideo(
                showWarningBeforeRecord: Boolean): Builder {
            this.showWarningBeforeRecord = showWarningBeforeRecord
            return this
        }

        /**
         * Set media list that already selected before.
         *
         * @param mediaSelecteds
         * Media list selected before.
         */
        fun setMediaListSelected(mediaSelecteds: List<MediaItem>?): Builder {
            this.mediaListSelected = mediaSelecteds
            return this
        }

        /**
         *
         * @param file
         * Use for save image that cropped. Default image cropped
         * saved in file that created by
         * [Utils.createTempFile]
         *
         *
         * *Note: file should not exist when pass to this method.
         * Because if user cancels capture photo, this file will be
         * temporary.*
         * @param croppedFile
         */
        fun setCroppedFile(croppedFile: File): Builder {
            this.croppedFile = croppedFile
            return this
        }

        /**
         * Sets whether the aspect ratio is fixed or not; true fixes the aspect
         * ratio, while false allows it to be changed.
         *
         * @param fixAspectRatio
         * Default is true.
         */
        fun setFixAspectRatio(fixAspectRatio: Boolean): Builder {
            this.fixAspectRatio = fixAspectRatio
            return this
        }

        /**
         *
         * @param aspectX
         * Default is 1.
         */
        fun setAspectX(aspectX: Int): Builder {
            this.aspectX = aspectX
            return this
        }

        /**
         *
         * @param aspectY
         * Default is 1.
         */
        fun setAspectY(aspectY: Int): Builder {
            this.aspectY = aspectY
            return this
        }

        /**
         * Crop photo before return back photo. Not effective for video or set
         * [MediaOptions.Builder.canSelectMultiPhoto] method's
         * parameter =true.
         *
         * @param isCropped
         */
        fun setIsCropped(isCropped: Boolean): Builder {
            this.isCropped = isCropped
            return this
        }

        /**
         * Set can select multiple photos or not. If true then
         * [.setIsCropped] option will be ignored.
         *
         * @param canSelectMulti
         */
        fun canSelectMultiPhoto(canSelectMulti: Boolean): Builder {
            this.canSelectMultiPhoto = canSelectMulti
            return this
        }

        /**
         * Select multiple videos. If true, videos haven't check duration. It's
         * override [.setMaxVideoDuration] and
         * [.setMinVideoDuration]
         *
         * @param canSelectMulti
         */
        fun canSelectMultiVideo(canSelectMulti: Boolean): Builder {
            this.canSelectMultiVideo = canSelectMulti
            if (canSelectMultiVideo) {
                maxVideoDuration = Integer.MAX_VALUE
                minVideoDuration = 0
            }
            return this
        }

        /**
         * Set max video's duration. If set, can't select multiple videos. It's
         * override [.canSelectMultiVideo] option.
         *
         * @param maxDuration
         * in milliseconds.
         *
         * @throws IllegalArgumentException
         * if maxDuration <=0
         * @see .setMinVideoDuration
         */
        fun setMaxVideoDuration(maxDuration: Int): Builder {
            if (maxDuration <= 0) {
                throw IllegalArgumentException("Max duration must be > 0")
            }
            this.maxVideoDuration = maxDuration
            this.canSelectMultiVideo = false
            return this
        }

        /**
         * Set min video's duration. If set, can't select multi videos. It's
         * override [.canSelectMultiVideo] option.
         *
         * @param minDuration
         * in milliseconds.
         *
         * @throws IllegalArgumentException
         * if minDuration <=0
         *
         * @see .setMaxVideoDuration
         */
        fun setMinVideoDuration(minDuration: Int): Builder {
            if (minDuration <= 0) {
                throw IllegalArgumentException("Min duration must be > 0")
            }
            this.minVideoDuration = minDuration
            this.canSelectMultiVideo = false
            return this
        }

        /**
         * Can choose photo or video. Default only choose photo.
         *
         */
        fun canSelectBothPhotoVideo(): Builder {
            canSelectPhoto = true
            canSelectVideo = true
            return this
        }

        /**
         * Only choose video. This method override [.selectPhoto] if set
         * before. Default only choose photo.
         *
         */
        fun selectVideo(): Builder {
            canSelectVideo = true
            canSelectPhoto = false
            return this
        }

        /**
         * Only choose photo. This method override [.selectVideo] if set
         * before.
         *
         * @return
         */
        fun selectPhoto(): Builder {
            canSelectPhoto = true
            canSelectVideo = false
            return this
        }

        /**
         *
         * @param file
         * Use for save image that capture from camera. Default image
         * saved in [Environment.DIRECTORY_PICTURES] folder of
         * device, file create by
         * [MediaUtils.createDefaultImageFile]<br></br>
         * **Note:**<br></br>
         * - File should not exist when pass to this method. Because
         * if use cancle capture photo, this file will be temporary.<br></br>
         * - In HTC devices (maybe others) this options not true,
         * image isn't saved in this file, it's saved by folder
         * default of camera.
         * @return
         */
        fun setPhotoCaptureFile(file: File): Builder {
            photoFile = file
            return this
        }

        /**
         * Build configured [MediaOptions] object.
         *
         * @return
         */
        fun build(): MediaOptions {
            return MediaOptions(this)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(if (canSelectMultiPhoto) 1 else 0)
        dest.writeInt(if (canSelectMultiVideo) 1 else 0)
        dest.writeInt(if (canSelectPhoto) 1 else 0)
        dest.writeInt(if (canSelectVideo) 1 else 0)
        dest.writeInt(if (isCropped) 1 else 0)
        dest.writeInt(if (isFixAspectRatio) 1 else 0)
        dest.writeInt(if (isShowWarningVideoDuration) 1 else 0)
        dest.writeInt(this.maxVideoDuration)
        dest.writeInt(this.minVideoDuration)
        dest.writeInt(aspectX)
        dest.writeInt(aspectY)
        dest.writeSerializable(photoFile)
        dest.writeSerializable(croppedFile)
        dest.writeTypedList(mediaListSelected)
    }

    constructor(parcel: Parcel) {
        canSelectMultiPhoto = parcel.readInt() != 0
        canSelectMultiVideo = parcel.readInt() != 0
        canSelectPhoto = parcel.readInt() != 0
        canSelectVideo = parcel.readInt() != 0
        isCropped = parcel.readInt() != 0
        isFixAspectRatio = parcel.readInt() != 0
        isShowWarningVideoDuration = parcel.readInt() != 0
        this.maxVideoDuration = parcel.readInt()
        this.minVideoDuration = parcel.readInt()
        aspectX = parcel.readInt()
        aspectY = parcel.readInt()
        this.photoFile = parcel.readSerializable() as File?
        this.croppedFile = parcel.readSerializable() as File?
        parcel.readTypedList(this.mediaListSelected ?: listOf(), MediaItem.CREATOR)
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (canSelectMultiPhoto) 1231 else 1237
        result = prime * result + if (canSelectPhoto) 1231 else 1237
        result = prime * result + if (canSelectVideo) 1231 else 1237
        result = prime * result + if (isCropped) 1231 else 1237
        result = prime * result + maxVideoDuration
        result = prime * result + minVideoDuration
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (javaClass != other.javaClass)
            return false
        val other = other as MediaOptions?
        if (canSelectMultiPhoto != other!!.canSelectMultiPhoto)
            return false
        if (canSelectPhoto != other.canSelectPhoto)
            return false
        if (canSelectVideo != other.canSelectVideo)
            return false
        if (isCropped != other.isCropped)
            return false
        if (maxVideoDuration != other.maxVideoDuration)
            return false
        return minVideoDuration == other.minVideoDuration
    }

    companion object {

        /**
         * Create default [MediaOptions] object.
         *
         *
         * With options:
         *
         *  * Only select 1 photo and not crop.
         *
         *
         * @return
         */
        fun createDefault(): MediaOptions {
            return Builder().build()
        }

        @JvmField
        val CREATOR: Parcelable.Creator<MediaOptions> = object : Parcelable.Creator<MediaOptions> {

            override fun newArray(size: Int): Array<MediaOptions?> {
                return arrayOfNulls(size)
            }

            override fun createFromParcel(source: Parcel): MediaOptions {
                return MediaOptions(source)
            }
        }
    }
}