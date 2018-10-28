package vn.tungdx.mediapicker.utils

import android.os.FileObserver
import java.io.File
import java.util.*

/**
 * Same [FileObserver] but support recursive in folder.
 */
class RecursiveFileObserver @JvmOverloads constructor(internal var mPath: String, internal var mMask: Int = FileObserver.ALL_EVENTS) : FileObserver(mPath, mMask) {

    private var mFileCreatedListener: OnFileCreatedListener? = null

    private var mObservers: MutableList<SingleFileObserver>? = null

    /**
     * Listener for event create file.
     */
    interface OnFileCreatedListener {
        fun onFileCreate(file: File)
    }

    fun setFileCreatedListener(listener: OnFileCreatedListener) {
        this.mFileCreatedListener = listener
    }

    override fun startWatching() {
        if (mObservers != null)
            return
        mObservers = ArrayList()
        val stack = Stack<String>()
        stack.push(mPath)

        while (!stack.empty()) {
            val parent = stack.pop()
            mObservers!!.add(SingleFileObserver(parent, mMask))
            val path = File(parent)
            val files = path.listFiles() ?: continue
            for (i in files.indices) {
                if (files[i].isDirectory && files[i].name != "."
                        && files[i].name != "..") {
                    stack.push(files[i].path)
                }
            }
        }
        for (i in mObservers!!.indices)
            mObservers!![i].startWatching()
    }

    override fun stopWatching() {
        if (mObservers == null)
            return

        for (i in mObservers!!.indices)
            mObservers!![i].stopWatching()

        mObservers!!.clear()
        mObservers = null
    }

    override fun onEvent(event: Int, path: String?) {
        if (event == FileObserver.CREATE) {
            if (mFileCreatedListener != null) {
                mFileCreatedListener!!.onFileCreate(File(path!!))
            }
        }
    }

    private inner class SingleFileObserver(private val mPath: String, mask: Int) : FileObserver(mPath, mask) {

        override fun onEvent(event: Int, path: String?) {
            val newPath = "$mPath/$path"
            this@RecursiveFileObserver.onEvent(event, newPath)
        }

    }
}