package vn.tungdx.mediapicker;

import java.util.List;

/**
 * @author TUNGDX
 */

/**
 * Listener for select media item.
 *
 */
public interface MediaSelectedListener {
    public void onHasNoSelected();

    public void onHasSelected(List<MediaItem> mediaSelectedList);
}
