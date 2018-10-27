package vn.tungdx.mediapicker.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle

import androidx.fragment.app.Fragment
import vn.tungdx.mediapicker.imageloader.MediaImageLoader

/**
 * Created by TungDX
 */
open class BaseFragment : Fragment() {

    protected var mContext: Context? = null

    protected lateinit var mMediaImageLoader: MediaImageLoader

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        val host = activity as FragmentHost?
        mMediaImageLoader = host!!.imageLoader
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mContext = activity
    }
}
