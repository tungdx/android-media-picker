package vn.tungdx.mediapicker;

import java.util.List;

/**
 * @author TUNGDX
 */

/**
 * Listener for select media item.
 *
 */
public interface NMediaSelectedListener {
    public void onHasNoSelected();

    public void onHasSelected(List<NMediaItem> mediaSelectedList);
}
