package vn.tungdx.mediapicker.widget

import android.annotation.TargetApi
import android.content.Context
import android.database.DataSetObservable
import android.database.DataSetObserver
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.util.*

/**
 * **Refers from [Here](https://android.googlesource.com/platform/packages/apps/Gallery2/+/idea133/src/com/android/photos/views). But have been customized for support 2.3 version. See:
 * [HeaderGridView.getNumColumns]** <br></br>
 * <br></br>
 * View.java A [GridView] that supports adding header rows in a very
 * similar way to [ListView]. See
 * [HeaderGridView.addHeaderView]
 */
class HeaderGridView : GridView {

    private val mHeaderViewInfos = ArrayList<FixedViewInfo>()

    val headerViewCount: Int
        get() = mHeaderViewInfos.size

    /**
     * A class that represents a fixed view in a list, for example a header at
     * the top or a footer at the bottom.
     */
    private class FixedViewInfo {
        /**
         * The view to add to the grid
         */
        var view: View? = null
        var viewContainer: ViewGroup? = null
        /**
         * The data backing the view. This is returned from
         * [ListAdapter.getItem].
         */
        var data: Any? = null
        /**
         * `true` if the fixed view should be selectable in the grid
         */
        var isSelectable: Boolean = false
    }

    private fun initHeaderGridView() {
        super.setClipChildren(false)
    }

    constructor(context: Context) : super(context) {
        initHeaderGridView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initHeaderGridView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initHeaderGridView()
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val adapter = adapter
        if (adapter != null && adapter is HeaderViewGridAdapter) {
            adapter.setNumColumns(numColumns)
        }
    }

    override fun setClipChildren(clipChildren: Boolean) {
        // Ignore, since the header rows depend on not being clipped
    }

    /**
     * Add a fixed view to appear at the top of the grid. If addHeaderView is
     * called more than once, the views will appear in the order they were
     * added. Views added using this call can take focus if they want.
     *
     *
     * NOTE: Call this before calling setAdapter. This is so HeaderGridView can
     * wrap the supplied cursor with one that will also account for header
     * views.
     *
     * @param v            The view to add.
     * @param data         Data to associate with this view
     * @param isSelectable whether the item is selectable
     */
    @JvmOverloads
    fun addHeaderView(v: View, data: Any? = null, isSelectable: Boolean = true) {
        val adapter = adapter
        if (adapter != null && adapter !is HeaderViewGridAdapter) {
            throw IllegalStateException(
                    "Cannot add header view to grid -- setAdapter has already been called.")
        }
        val info = FixedViewInfo()
        val fl = FullWidthFixedViewLayout(context)
        fl.addView(v)
        info.view = v
        info.viewContainer = fl
        info.data = data
        info.isSelectable = isSelectable
        mHeaderViewInfos.add(info)
        // in the case of re-adding a header view, or adding one later on,
        // we need to notify the observer
        if (adapter != null) {
            (adapter as HeaderViewGridAdapter).notifyDataSetChanged()
        }
    }

    /**
     * Removes a previously-added header view.
     *
     * @param v The view to remove
     * @return true if the view was removed, false if the view was not a header
     * view
     */
    fun removeHeaderView(v: View): Boolean {
        if (mHeaderViewInfos.size > 0) {
            var result = false
            val adapter = adapter
            if (adapter != null && (adapter as HeaderViewGridAdapter).removeHeader(v)) {
                result = true
            }
            removeFixedViewInfo(v, mHeaderViewInfos)
            return result
        }
        return false
    }

    private fun removeFixedViewInfo(v: View, where: ArrayList<FixedViewInfo>) {
        val len = where.size
        for (i in 0 until len) {
            val info = where[i]
            if (info.view === v) {
                where.removeAt(i)
                break
            }
        }
    }

    /**
     * @author TUNGDX
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun getNumColumns(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            super.getNumColumns()
        } else {
            try {
                val numColumns = javaClass.superclass!!.getDeclaredField(
                        "mNumColumns")
                numColumns.isAccessible = true
                numColumns.getInt(this)
            } catch (e: Exception) {
                2
            }

        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun setAdapter(adapter: ListAdapter) {
        if (mHeaderViewInfos.size > 0) {
            val hadapter = HeaderViewGridAdapter(
                    mHeaderViewInfos, adapter)
            val numColumns = numColumns
            if (numColumns > 1) {
                hadapter.setNumColumns(numColumns)
            }
            super.setAdapter(hadapter)
        } else {
            super.setAdapter(adapter)
        }
    }

    private inner class FullWidthFixedViewLayout(context: Context) : FrameLayout(context) {

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            var widthMeasureSpec = widthMeasureSpec
            val targetWidth = (this@HeaderGridView.measuredWidth
                    - this@HeaderGridView.paddingLeft
                    - this@HeaderGridView.paddingRight)
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(targetWidth,
                    View.MeasureSpec.getMode(widthMeasureSpec))
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    /**
     * ListAdapter used when a HeaderGridView has header views. This ListAdapter
     * wraps another one and also keeps track of the header views and their
     * associated data objects.
     *
     *
     * This is intended as a base class; you will probably not need to use this
     * class directly in your own code.
     */
    private class HeaderViewGridAdapter(// This ArrayList is assumed to NOT be null.
            internal var mHeaderViewInfos: ArrayList<FixedViewInfo>?,
            private val mAdapter: ListAdapter?) : WrapperListAdapter, Filterable {
        // This is used to notify the container of updates relating to number of
        // columns
        // or headers changing, which changes the number of placeholders needed
        private val mDataSetObservable = DataSetObservable()
        private var mNumColumns = 1
        internal var mAreAllFixedViewsSelectable: Boolean = false
        private val mIsFilterable: Boolean

        val headersCount: Int
            get() = mHeaderViewInfos!!.size

        init {
            mIsFilterable = mAdapter is Filterable
            if (mHeaderViewInfos == null) {
                throw IllegalArgumentException(
                        "headerViewInfos cannot be null")
            }
            mAreAllFixedViewsSelectable = areAllListInfosSelectable(mHeaderViewInfos)
        }

        override fun isEmpty(): Boolean {
            return (mAdapter == null || mAdapter.isEmpty) && headersCount == 0
        }

        fun setNumColumns(numColumns: Int) {
            if (numColumns < 1) {
                throw IllegalArgumentException(
                        "Number of columns must be 1 or more")
            }
            if (mNumColumns != numColumns) {
                mNumColumns = numColumns
                notifyDataSetChanged()
            }
        }

        private fun areAllListInfosSelectable(infos: ArrayList<FixedViewInfo>?): Boolean {
            if (infos != null) {
                for (info in infos) {
                    if (!info.isSelectable) {
                        return false
                    }
                }
            }
            return true
        }

        fun removeHeader(v: View): Boolean {
            for (i in mHeaderViewInfos!!.indices) {
                val info = mHeaderViewInfos!![i]
                if (info.view === v) {
                    mHeaderViewInfos!!.removeAt(i)
                    mAreAllFixedViewsSelectable = areAllListInfosSelectable(mHeaderViewInfos)
                    mDataSetObservable.notifyChanged()
                    return true
                }
            }
            return false
        }

        override fun getCount(): Int {
            return if (mAdapter != null) {
                headersCount * mNumColumns + mAdapter.count
            } else {
                headersCount * mNumColumns
            }
        }

        override fun areAllItemsEnabled(): Boolean {
            return if (mAdapter != null) {
                mAreAllFixedViewsSelectable && mAdapter.areAllItemsEnabled()
            } else {
                true
            }
        }

        override fun isEnabled(position: Int): Boolean {
            // Header (negative positions will throw an
            // ArrayIndexOutOfBoundsException)
            val numHeadersAndPlaceholders = headersCount * mNumColumns
            if (position < numHeadersAndPlaceholders) {
                return position % mNumColumns == 0 && mHeaderViewInfos!![position / mNumColumns].isSelectable
            }
            // Adapter
            val adjPosition = position - numHeadersAndPlaceholders
            var adapterCount = 0
            if (mAdapter != null) {
                adapterCount = mAdapter.count
                if (adjPosition < adapterCount) {
                    return mAdapter.isEnabled(adjPosition)
                }
            }
            throw ArrayIndexOutOfBoundsException(position)
        }

        override fun getItem(position: Int): Any? {
            // Header (negative positions will throw an
            // ArrayIndexOutOfBoundsException)
            val numHeadersAndPlaceholders = headersCount * mNumColumns
            if (position < numHeadersAndPlaceholders) {
                return if (position % mNumColumns == 0) {
                    mHeaderViewInfos!!.get(position / mNumColumns).data
                } else null
            }
            // Adapter
            val adjPosition = position - numHeadersAndPlaceholders
            var adapterCount = 0
            if (mAdapter != null) {
                adapterCount = mAdapter.count
                if (adjPosition < adapterCount) {
                    return mAdapter.getItem(adjPosition)
                }
            }
            throw ArrayIndexOutOfBoundsException(position)
        }

        override fun getItemId(position: Int): Long {
            val numHeadersAndPlaceholders = headersCount * mNumColumns
            if (mAdapter != null && position >= numHeadersAndPlaceholders) {
                val adjPosition = position - numHeadersAndPlaceholders
                val adapterCount = mAdapter.count
                if (adjPosition < adapterCount) {
                    return mAdapter.getItemId(adjPosition)
                }
            }
            return -1
        }

        override fun hasStableIds(): Boolean {
            return mAdapter?.hasStableIds() ?: false
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var convertView = convertView
            // Header (negative positions will throw an
            // ArrayIndexOutOfBoundsException)
            val numHeadersAndPlaceholders = headersCount * mNumColumns
            if (position < numHeadersAndPlaceholders) {
                val headerViewContainer = mHeaderViewInfos!!.get(position / mNumColumns).viewContainer
                if (position % mNumColumns == 0) {
                    return headerViewContainer
                } else {
                    if (convertView == null) {
                        convertView = View(parent.context)
                    }
                    // We need to do this because GridView uses the height of
                    // the last item
                    // in a row to determine the height for the entire row.
                    convertView.visibility = View.INVISIBLE
                    convertView.minimumHeight = headerViewContainer!!
                            .height
                    return convertView
                }
            }
            // Adapter
            val adjPosition = position - numHeadersAndPlaceholders
            var adapterCount = 0
            if (mAdapter != null) {
                adapterCount = mAdapter.count
                if (adjPosition < adapterCount) {
                    return mAdapter.getView(adjPosition, convertView, parent)
                }
            }
            throw ArrayIndexOutOfBoundsException(position)
        }

        override fun getItemViewType(position: Int): Int {
            val numHeadersAndPlaceholders = headersCount * mNumColumns
            if (position < numHeadersAndPlaceholders && position % mNumColumns != 0) {
                // Placeholders get the last view type number
                return mAdapter?.viewTypeCount ?: 1
            }
            if (mAdapter != null && position >= numHeadersAndPlaceholders) {
                val adjPosition = position - numHeadersAndPlaceholders
                val adapterCount = mAdapter.count
                if (adjPosition < adapterCount) {
                    return mAdapter.getItemViewType(adjPosition)
                }
            }
            return AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER
        }

        override fun getViewTypeCount(): Int {
            return if (mAdapter != null) {
                mAdapter.viewTypeCount + 1
            } else 2
        }

        override fun registerDataSetObserver(observer: DataSetObserver) {
            mDataSetObservable.registerObserver(observer)
            mAdapter?.registerDataSetObserver(observer)
        }

        override fun unregisterDataSetObserver(observer: DataSetObserver) {
            mDataSetObservable.unregisterObserver(observer)
            mAdapter?.unregisterDataSetObserver(observer)
        }

        override fun getFilter(): Filter? {
            return if (mIsFilterable) {
                (mAdapter as Filterable).filter
            } else null
        }

        override fun getWrappedAdapter(): ListAdapter? {
            return mAdapter
        }

        fun notifyDataSetChanged() {
            mDataSetObservable.notifyChanged()
        }
    }
}
/**
 * Add a fixed view to appear at the top of the grid. If addHeaderView is
 * called more than once, the views will appear in the order they were
 * added. Views added using this call can take focus if they want.
 *
 *
 * NOTE: Call this before calling setAdapter. This is so HeaderGridView can
 * wrap the supplied cursor with one that will also account for header
 * views.
 *
 * @param v The view to add.
 */
