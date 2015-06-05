package vn.tungdx.mediapicker;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView.RecyclerListener;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import vn.tungdx.mediapicker.imageloader.NImageLoader;
import vn.tungdx.mediapicker.utils.NMediaUtils;
import vn.tungdx.mediapicker.widget.PickerImageView;

/**
 * @author TUNGDX
 */

/**
 * Adapter for display media item list.
 */
public class NMediaAdapter extends CursorAdapter implements RecyclerListener {
    private int mMediaType;
    private NImageLoader mImageLoader;
    private List<NMediaItem> mMediaListSelected = new ArrayList<NMediaItem>();
    private NMediaOptions mMediaOptions;
    private int mItemHeight = 0;
    private int mNumColumns = 0;
    private RelativeLayout.LayoutParams mImageViewLayoutParams;
    private List<PickerImageView> mPickerImageViewSelected = new ArrayList<PickerImageView>();

    public NMediaAdapter(Context context, Cursor c, int flags,
                         NImageLoader imageLoader, int mediaType, NMediaOptions mediaOptions) {
        this(context, c, flags, null, imageLoader, mediaType, mediaOptions);
    }

    public NMediaAdapter(Context context, Cursor c, int flags,
                         List<NMediaItem> mediaListSelected, NImageLoader imageLoader,
                         int mediaType, NMediaOptions mediaOptions) {
        super(context, c, flags);
        if (mediaListSelected != null)
            mMediaListSelected = mediaListSelected;
        mImageLoader = imageLoader;
        mMediaType = mediaType;
        mMediaOptions = mediaOptions;
        mImageViewLayoutParams = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        final Uri uri;
        if (mMediaType == NMediaItem.PHOTO) {
            uri = NMediaUtils.getPhotoUri(cursor);
            holder.thumbnail.setVisibility(View.GONE);
        } else {
            uri = NMediaUtils.getVideoUri(cursor);
            holder.thumbnail.setVisibility(View.VISIBLE);
        }
        boolean isSelected = isSelected(uri);
        holder.imageView.setSelected(isSelected);
        if (isSelected) {
            mPickerImageViewSelected.add(holder.imageView);
        }
        holder.imageView.setMediaType(mMediaType);
        mImageLoader.displayImage(uri, holder.imageView);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        ViewHolder holder = new ViewHolder();
        View root = View
                .inflate(mContext, R.layout.list_item_mediapicker, null);
        holder.imageView = (PickerImageView) root.findViewById(R.id.thumbnail);
        holder.thumbnail = root.findViewById(R.id.overlay);

        holder.imageView.setLayoutParams(mImageViewLayoutParams);
        // Check the height matches our calculated column width
        if (holder.imageView.getLayoutParams().height != mItemHeight) {
            holder.imageView.setLayoutParams(mImageViewLayoutParams);
        }
        root.setTag(holder);
        return root;
    }

    private class ViewHolder {
        PickerImageView imageView;
        View thumbnail;
    }

    public boolean hasSelected() {
        return mMediaListSelected.size() > 0 ? true : false;
    }

    /**
     * Check media uri is selected or not.
     *
     * @param uri Uri of media item (photo, video)
     * @return true if selected, false otherwise.
     */
    public boolean isSelected(Uri uri) {
        if (uri == null)
            return false;
        for (NMediaItem item : mMediaListSelected) {
            if (item.getUriOrigin().equals(uri))
                return true;
        }
        return false;
    }

    /**
     * Check {@link NMediaItem} is selected or not.
     *
     * @param item {@link NMediaItem} to check.
     * @return true if selected, false otherwise.
     */
    public boolean isSelected(NMediaItem item) {
        return mMediaListSelected.contains(item);
    }

    /**
     * Set {@link NMediaItem} selected.
     *
     * @param item {@link NMediaItem} to selected.
     */
    public void setMediaSelected(NMediaItem item) {
        syncMediaSelectedAsOptions();
        if (!mMediaListSelected.contains(item))
            mMediaListSelected.add(item);
    }

    /**
     * If item selected then change to unselected and unselected to selected.
     *
     * @param item Item to update.
     */
    public void updateMediaSelected(NMediaItem item,
                                    PickerImageView pickerImageView) {
        if (mMediaListSelected.contains(item)) {
            mMediaListSelected.remove(item);
            pickerImageView.setSelected(false);
            this.mPickerImageViewSelected.remove(pickerImageView);
        } else {
            boolean value = syncMediaSelectedAsOptions();
            if (value) {
                for (PickerImageView picker : this.mPickerImageViewSelected) {
                    picker.setSelected(false);
                }
                this.mPickerImageViewSelected.clear();
            }
            mMediaListSelected.add(item);
            pickerImageView.setSelected(true);
            this.mPickerImageViewSelected.add(pickerImageView);
        }
    }

    /**
     * @return List of {@link NMediaItem} selected.
     */
    public List<NMediaItem> getMediaSelectedList() {
        return mMediaListSelected;
    }

    /**
     * Set list of {@link NMediaItem} selected.
     *
     * @param list
     */
    public void setMediaSelectedList(List<NMediaItem> list) {
        mMediaListSelected = list;
    }

    /**
     * Whether clear or not media selected as options.
     *
     * @return true if clear, false otherwise.
     */
    private boolean syncMediaSelectedAsOptions() {
        switch (mMediaType) {
            case NMediaItem.PHOTO:
                if (!mMediaOptions.canSelectMultiPhoto()) {
                    mMediaListSelected.clear();
                    return true;
                }
                break;
            case NMediaItem.VIDEO:
                if (!mMediaOptions.canSelectMultiVideo()) {
                    mMediaListSelected.clear();
                    return true;
                }

                break;
            default:
                break;
        }
        return false;
    }

    /**
     * {@link NMediaItem#VIDEO} or {@link NMediaItem#PHOTO}
     *
     * @param mediaType
     */
    public void setMediaType(int mediaType) {
        mMediaType = mediaType;
    }

    // set numcols
    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
    }

    public int getNumColumns() {
        return mNumColumns;
    }

    // set photo item height
    public void setItemHeight(int height) {
        if (height == mItemHeight) {
            return;
        }
        mItemHeight = height;
        mImageViewLayoutParams.height = height;
        mImageViewLayoutParams.width = height;
        notifyDataSetChanged();
    }

    @Override
    public void onMovedToScrapHeap(View view) {
        PickerImageView imageView = (PickerImageView) view
                .findViewById(R.id.thumbnail);
        mPickerImageViewSelected.remove(imageView);
    }

    public void onDestroyView() {
        mPickerImageViewSelected.clear();
    }
}