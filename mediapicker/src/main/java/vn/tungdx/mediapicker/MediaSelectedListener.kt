package vn.tungdx.mediapicker

/**
 * @author TUNGDX
 */

/**
 * Listener for select media item.
 *
 */
interface MediaSelectedListener {

    fun onHasNoSelected()

    fun onHasSelected(mediaSelectedList: List<MediaItem>)
}
