package vn.tungdx.mediapicker;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import vn.tungdx.mediapicker.activities.NMediaPickerActivity;
import vn.tungdx.mediapicker.utils.NMediaUtils;

/**
 * @author TUNGDX
 */

/**
 * Contains information of photo or video that selected and return back in
 * {@link NMediaPickerActivity}
 */
public class NMediaItem implements Parcelable {
    public static final int PHOTO = 1;
    public static final int VIDEO = 2;
    private int type;
    private Uri uriCropped;
    private Uri uriOrigin;

    /**
     * @param mediaType Whether {@link #PHOTO} or {@link #VIDEO}
     * @param uriOrigin {@link Uri} of media item.
     */
    public NMediaItem(int mediaType, Uri uriOrigin) {
        this.type = mediaType;
        this.uriOrigin = uriOrigin;
    }

    /**
     * @return type of media item. Whether {@link #PHOTO} or {@link #VIDEO}
     */
    public int getType() {
        return type;
    }

    /**
     * Set type of media.
     *
     * @param type is {@link #PHOTO} or {@link #VIDEO}
     */
    public void setType(int type) {
        this.type = type;
    }

    public Uri getUriCropped() {
        return uriCropped;
    }

    public void setUriCropped(Uri uriCropped) {
        this.uriCropped = uriCropped;
    }

    public Uri getUriOrigin() {
        return uriOrigin;
    }

    public void setUriOrigin(Uri uriOrigin) {
        this.uriOrigin = uriOrigin;
    }

    public boolean isVideo() {
        return type == VIDEO;
    }

    public boolean isPhoto() {
        return type == PHOTO;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        if (this.uriCropped == null) {
            dest.writeString(null);
        } else {
            dest.writeString(this.uriCropped.toString());
        }
        if (this.uriOrigin == null) {
            dest.writeString(null);
        } else {
            dest.writeString(this.uriOrigin.toString());
        }
    }

    public NMediaItem(Parcel in) {
        this.type = in.readInt();
        String crop = in.readString();
        if (!TextUtils.isEmpty(crop))
            this.uriCropped = Uri.parse(crop);
        String origin = in.readString();
        if (!TextUtils.isEmpty(origin))
            this.uriOrigin = Uri.parse(origin);
    }

    public static final Creator<NMediaItem> CREATOR = new Creator<NMediaItem>() {

        @Override
        public NMediaItem[] newArray(int size) {
            return new NMediaItem[size];
        }

        @Override
        public NMediaItem createFromParcel(Parcel source) {
            return new NMediaItem(source);
        }
    };

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((uriCropped == null) ? 0 : uriCropped.hashCode());
        result = prime * result
                + ((uriOrigin == null) ? 0 : uriOrigin.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NMediaItem other = (NMediaItem) obj;
        if (uriCropped == null) {
            if (other.uriCropped != null)
                return false;
        } else if (!uriCropped.equals(other.uriCropped))
            return false;
        if (uriOrigin == null) {
            if (other.uriOrigin != null)
                return false;
        } else if (!uriOrigin.equals(other.uriOrigin))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MediaItem [type=" + type + ", uriCropped=" + uriCropped
                + ", uriOrigin=" + uriOrigin + "]";
    }

    /**
     * @param context
     * @return Path of origin file.
     */
    public String getPathOrigin(Context context) {
        return getPathFromUri(context, uriOrigin);
    }

    /**
     * @param context
     * @return Path of cropped file.
     */
    public String getPathCropped(Context context) {
        return getPathFromUri(context, uriCropped);
    }

    private String getPathFromUri(Context context, Uri uri) {
        if (uri == null)
            return null;
        String scheme = uri.getScheme();
        if (scheme.equals(ContentResolver.SCHEME_FILE)) {
            return uri.getPath();
        } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            if (isPhoto()) {
                return NMediaUtils.getRealImagePathFromURI(
                        context.getContentResolver(), uri);
            } else {
                return NMediaUtils.getRealVideoPathFromURI(
                        context.getContentResolver(), uri);
            }
        }
        return uri.toString();
    }
}
