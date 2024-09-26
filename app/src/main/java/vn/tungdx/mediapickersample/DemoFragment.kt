package vn.tungdx.mediapickersample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import vn.tungdx.mediapicker.MediaItem
import vn.tungdx.mediapicker.activities.MediaPickerActivity


class DemoFragment : Fragment() {
    private var mLinearLayout: LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater
            .inflate(R.layout.fragment_demo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.all_default).setOnClickListener {
            MediaPickerActivity.open(this@DemoFragment, REQUEST_MEDIA)
            clearImages()
        }
        mLinearLayout = view.findViewById<View>(R.id.list_image) as LinearLayout
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val mMediaSelectedList: ArrayList<MediaItem>?
        if (requestCode == REQUEST_MEDIA) {
            if (resultCode == Activity.RESULT_OK) {
                mMediaSelectedList = MediaPickerActivity.getMediaItemSelected(data)
                if (mMediaSelectedList !=
                    null
                ) {
                    addImages(mMediaSelectedList[0])
                }
            }
        }
    }

    private fun addImages(mediaItem: MediaItem) {
        val root = LayoutInflater.from(activity).inflate(R.layout.item, null) as LinearLayout

        val imageView = root.findViewById<View>(R.id.image) as ImageView
        val textView = root.findViewById<View>(R.id.textView) as TextView

        val info = String.format(
            "Original Uri [%s]\nOriginal Path [%s] \n\nCropped Uri [%s] \nCropped Path[%s]",
            mediaItem.uriOrigin, mediaItem.uriCropped,
            mediaItem.getPathOrigin(requireActivity()), mediaItem.getPathCropped(requireActivity())
        )
        textView.text = info
        if (mediaItem.uriCropped == null) {
            Glide.with(requireContext()).load(mediaItem.uriOrigin).into(imageView)
        } else {
            Glide.with(requireContext()).load(mediaItem.uriCropped).into(imageView)
        }
        mLinearLayout!!.addView(root)
    }

    private fun clearImages() {
        mLinearLayout!!.removeAllViews()
    }

    companion object {
        private val REQUEST_MEDIA = 100
        private val TAG = "DemoMediaPickerFragment"
    }
}
