package vn.tungdx.mediapicker.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.ImageView

import vn.tungdx.mediapicker.R


/**
 * @author TUNGDX
 */

/**
 * Display thumbnail of video, photo and state when video, photo selected or
 * not.
 */
class PickerImageView : ImageView {
    private var paintBorder: Paint? = null

    private var isSelected: Boolean = false
    private var borderSize = 1

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        paintBorder = Paint()
        paintBorder!!.isAntiAlias = true
        paintBorder!!.color = resources.getColor(R.color.picker_color)
        borderSize = resources.getDimensionPixelSize(
                R.dimen.picker_border_size)
    }

    constructor(context: Context) : super(context) {
        init()
    }

    override fun setSelected(isSelected: Boolean) {
        if (isSelected != this.isSelected) {
            this.isSelected = isSelected
            invalidate()
        }
    }

    override fun isSelected(): Boolean {
        return isSelected
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        setMeasuredDimension(width, width)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isSelected) {
            canvas.drawRect(0f, 0f, borderSize.toFloat(), height.toFloat(), paintBorder!!)
            canvas.drawRect((width - borderSize).toFloat(), 0f, width.toFloat(),
                    height.toFloat(), paintBorder!!)
            canvas.drawRect(0f, 0f, width.toFloat(), borderSize.toFloat(), paintBorder!!)
            canvas.drawRect(0f, (height - borderSize).toFloat(), width.toFloat(),
                    height.toFloat(), paintBorder!!)
        }
    }
}