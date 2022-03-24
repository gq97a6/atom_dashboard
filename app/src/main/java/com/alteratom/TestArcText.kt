package com.alteratom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs


class TemperatureArcTextView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) :
    ArcTextView(context, attrs, defStyleAttr, defStyleRes, "TEMPERATURE", 180f, 90f) {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

}

class HumidityArcTextView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) :
    ArcTextView(context, attrs, defStyleAttr, defStyleRes, "HUMIDITY", 90f, -90f) {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )
}

@SuppressLint("ResourceType")
open class ArcTextView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
    var label: String,
    val startAngle: Float,
    val sweepAngle: Float
) :
    View(context, attrs, defStyleAttr, defStyleRes) {

    private val mPaintText: Paint
    private val mAngle: Float
    private val mText: String
    private val mTextSize: Float

    override fun onDraw(canvas: Canvas) {
        mPaintText.textSize = mTextSize

        val b = Rect()
        mPaintText.getTextBounds(label, 0, label.length, b)
        b.height()
        b.width()

        val p = this.paddingLeft.toFloat() + b.height()
        val circle = RectF(p, p, canvas.width.toFloat() - p, canvas.height.toFloat() - p)
        val arc = Path()

        arc.addArc(circle, startAngle, sweepAngle)

        val len = (canvas.width.toFloat() - (2 * p)) * 3.1415f * abs(sweepAngle) / 360f
        val offset = (len - b.width()) / 2
        canvas.drawTextOnPath(label, arc, offset, 0f, mPaintText)

        invalidate()
    }

    init {
        mPaintText = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintText.style = Paint.Style.FILL_AND_STROKE
        mPaintText.color = Color.parseColor("#999999")

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ArcTextView, 0, 0
        ).apply {
            try {
                mText = getString(R.styleable.ArcTextView_text) ?: "placeholder"
                mAngle = getFloat(R.styleable.ArcTextView_angle, 0f)
                mTextSize = getDimension(R.styleable.ArcTextView_textSize, 30f)
            } finally {
                recycle()
            }
        }
    }
}