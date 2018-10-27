package vn.tungdx.mediapicker.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import androidx.fragment.app.DialogFragment

import vn.tungdx.mediapicker.R

/**
 * @author TUNGDX
 */

/**
 * Create dialog for media picker module. Should only use in this module.
 */
class MediaPickerErrorDialog : DialogFragment() {
    private var mMessage: String? = null
    private var mOnPositionClickListener: OnClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMessage = arguments!!.getString("msg")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setMessage(mMessage)
                .setPositiveButton(R.string.ok, mOnPositionClickListener)
                .create()
    }

    fun setOnOKClickListener(mOnClickListener: OnClickListener) {
        this.mOnPositionClickListener = mOnClickListener
    }

    companion object {

        fun newInstance(msg: String): MediaPickerErrorDialog {
            val dialog = MediaPickerErrorDialog()
            val bundle = Bundle()
            bundle.putString("msg", msg)
            dialog.arguments = bundle
            return dialog
        }
    }
}