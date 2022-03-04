package com.netDashboard.color_picker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.colorToHSV
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.ColorUtils
import com.netDashboard.color_picker.components.ArcComponent
import com.netDashboard.color_picker.components.hsv.HueComponent
import com.netDashboard.color_picker.components.hsv.LightnessComponent
import com.netDashboard.color_picker.components.hsv.SaturationComponent
import com.netDashboard.R
import com.netDashboard.color_picker.listeners.OnColorSelectionListener

open class HSVColorPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val metrics =
        Metrics(color = floatArrayOf(0f, 1f, 0.5f), density = resources.displayMetrics.density)

    private val hueComponent: ArcComponent
    private val saturationComponent: ArcComponent
    private val lightnessComponent: ArcComponent

    private val hueRadiusOffset: Float
    private val saturationRadiusOffset: Float
    private val lightnessRadiusOffset: Float

    protected val paints = Paints()
    protected val config: Config

    internal val color: Int
        get() = metrics.getColor()

    override fun onDraw(canvas: Canvas) {
        hueComponent.drawComponent(canvas)
        saturationComponent.drawComponent(canvas)
        lightnessComponent.drawComponent(canvas)
    }

    override fun onSizeChanged(width: Int, height: Int, oldW: Int, oldH: Int) {
        val minimumSize = if (width > height) height else width
        val padding = (paddingLeft + paddingRight + paddingTop + paddingBottom) / 4f
        val outerRadius = minimumSize.toFloat() / 2f - padding

        hueComponent.setRadius(outerRadius, hueRadiusOffset)
        saturationComponent.setRadius(outerRadius, saturationRadiusOffset)
        lightnessComponent.setRadius(outerRadius, lightnessRadiusOffset)

        metrics.centerX = width / 2f
        metrics.centerY = height / 2f

        hueComponent.updateComponent(hueComponent.angle)
        saturationComponent.updateComponent(saturationComponent.angle)
        lightnessComponent.updateComponent(lightnessComponent.angle)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var isTouched = true
        if (!hueComponent.onTouchEvent(event)) {
            if (!saturationComponent.onTouchEvent(event)) {
                isTouched = lightnessComponent.onTouchEvent(event)
            }
        }
        invalidate()
        return isTouched
    }

    fun setColorSelectionListener(listener: OnColorSelectionListener) {
        hueComponent.setColorSelectionListener(listener)
        saturationComponent.setColorSelectionListener(listener)
        lightnessComponent.setColorSelectionListener(listener)
    }

    fun setColor(color: Int) {
        with(metrics) {
            colorToHSV(color, this.color)
            hueComponent.updateAngle(this.color[0])
            saturationComponent.updateAngle(this.color[1])
            lightnessComponent.updateAngle(this.color[2])
        }
        invalidate()
    }

    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.ColorPicker, defStyleAttr, 0)

        val arcWidth = typedArray.getDimension(R.styleable.ColorPicker_arc_width, dp(5f))
        val strokeWidth = typedArray.getDimension(R.styleable.ColorPicker_stroke_width, 0f)
        val indicatorRadius =
            typedArray.getDimension(R.styleable.ColorPicker_indicator_radius, dp(13f))
        val indicatorStrokeWidth =
            typedArray.getDimension(R.styleable.ColorPicker_indicator_stroke_width, dp(2f))
        val strokeColor = typedArray.getColor(R.styleable.ColorPicker_stroke_color, 0)
        val indicatorStrokeColor =
            typedArray.getColor(R.styleable.ColorPicker_indicator_stroke_color, 0)
        val arcLength = typedArray.getFloat(R.styleable.ColorPicker_arc_length, 0f)
        val radiusOffset = typedArray.getDimension(R.styleable.ColorPicker_radius_offset, 0f)

        typedArray.recycle()

        config = Config(
            arcWidth, strokeWidth, indicatorRadius, indicatorStrokeWidth,
            strokeColor, indicatorStrokeColor, arcLength, radiusOffset
        )

        val typedArrayHSV =
            context.obtainStyledAttributes(attrs, R.styleable.HSVColorPicker, defStyleAttr, 0)

        with(config) {
            val hueArcLength = typedArrayHSV.getFloat(
                R.styleable.HSVColorPicker_hue_arc_length,
                if (arcLength == 0f) 360f else arcLength
            )
            val hueStartAngle =
                typedArrayHSV.getFloat(R.styleable.HSVColorPicker_hue_start_angle, 0f)
            hueComponent = HueComponent(metrics, paints, hueArcLength, hueStartAngle).also {
                it.fillWidth =
                    typedArrayHSV.getDimension(R.styleable.HSVColorPicker_hue_arc_width, arcWidth)
                it.strokeWidth = typedArrayHSV.getDimension(
                    R.styleable.HSVColorPicker_hue_stroke_width,
                    strokeWidth
                )
                it.indicatorStrokeWidth = typedArrayHSV.getDimension(
                    R.styleable.HSVColorPicker_hue_indicator_stroke_width,
                    indicatorStrokeWidth
                )
                it.indicatorStrokeColor = typedArrayHSV.getColor(
                    R.styleable.HSVColorPicker_hue_indicator_stroke_color,
                    indicatorStrokeColor
                )
                it.strokeColor =
                    typedArrayHSV.getColor(R.styleable.HSVColorPicker_hue_stroke_color, strokeColor)
                it.indicatorRadius = typedArrayHSV.getDimension(
                    R.styleable.HSVColorPicker_hue_indicator_radius,
                    indicatorRadius
                )
            }

            val saturationArcLength = typedArrayHSV.getFloat(
                R.styleable.HSVColorPicker_saturation_arc_length,
                if (arcLength == 0f) 155f else arcLength
            )
            val saturationStartAngle =
                typedArrayHSV.getFloat(R.styleable.HSVColorPicker_saturation_start_angle, 100f)
            saturationComponent = SaturationComponent(
                metrics,
                paints,
                saturationArcLength,
                saturationStartAngle
            ).also {
                it.fillWidth = typedArrayHSV.getDimension(
                    R.styleable.HSVColorPicker_saturation_arc_width,
                    arcWidth
                )
                it.strokeWidth = typedArrayHSV.getDimension(
                    R.styleable.HSVColorPicker_saturation_stroke_width,
                    strokeWidth
                )
                it.indicatorStrokeWidth = typedArrayHSV.getDimension(
                    R.styleable.HSVColorPicker_saturation_indicator_stroke_width,
                    indicatorStrokeWidth
                )
                it.indicatorStrokeColor = typedArrayHSV.getColor(
                    R.styleable.HSVColorPicker_saturation_indicator_stroke_color,
                    indicatorStrokeColor
                )
                it.strokeColor = typedArrayHSV.getColor(
                    R.styleable.HSVColorPicker_saturation_stroke_color,
                    strokeColor
                )
                it.indicatorRadius = typedArrayHSV.getDimension(
                    R.styleable.HSVColorPicker_saturation_indicator_radius,
                    indicatorRadius
                )
            }

            val lightnessArcLength = typedArrayHSV.getFloat(
                R.styleable.HSVColorPicker_lightness_arc_length,
                if (arcLength == 0f) 155f else arcLength
            )
            val lightnessStartAngle =
                typedArrayHSV.getFloat(R.styleable.HSVColorPicker_lightness_start_angle, 280f)
            lightnessComponent =
                LightnessComponent(metrics, paints, lightnessArcLength, lightnessStartAngle).also {
                    it.fillWidth = typedArrayHSV.getDimension(
                        R.styleable.HSVColorPicker_lightness_arc_width,
                        arcWidth
                    )
                    it.strokeWidth = typedArrayHSV.getDimension(
                        R.styleable.HSVColorPicker_lightness_stroke_width,
                        strokeWidth
                    )
                    it.indicatorStrokeWidth = typedArrayHSV.getDimension(
                        R.styleable.HSVColorPicker_lightness_indicator_stroke_width,
                        indicatorStrokeWidth
                    )
                    it.indicatorStrokeColor = typedArrayHSV.getColor(
                        R.styleable.HSVColorPicker_lightness_indicator_stroke_color,
                        indicatorStrokeColor
                    )
                    it.strokeColor = typedArrayHSV.getColor(
                        R.styleable.HSVColorPicker_lightness_stroke_color,
                        strokeColor
                    )
                    it.indicatorRadius = typedArrayHSV.getDimension(
                        R.styleable.HSVColorPicker_lightness_indicator_radius,
                        indicatorRadius
                    )
                }

            hueRadiusOffset = typedArrayHSV.getDimension(
                R.styleable.HSVColorPicker_hue_radius_offset,
                if (radiusOffset == 0f) dp(1f) else radiusOffset
            )
            saturationRadiusOffset = typedArrayHSV.getDimension(
                R.styleable.HSVColorPicker_saturation_radius_offset,
                if (radiusOffset == 0f) dp(25f) else radiusOffset
            )
            lightnessRadiusOffset = typedArrayHSV.getDimension(
                R.styleable.HSVColorPicker_lightness_radius_offset,
                if (radiusOffset == 0f) dp(25f) else radiusOffset
            )
        }
        typedArrayHSV.recycle()
    }

    protected fun dp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics
        )
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = super.onSaveInstanceState()
        if (bundle != null) {
            return SavedState(bundle).apply {
                color = this@HSVColorPicker.color
            }
        }
        return null
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            setColor(state.color)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    internal class SavedState : BaseSavedState {
        var color: Int = 0

        constructor(bundle: Parcelable) : super(bundle)

        private constructor(parcel: Parcel) : super(parcel) {
            color = parcel.readInt()
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }

        override fun describeContents() = 0

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(color)
        }
    }

    data class Config(
        val arcWidth: Float,
        val strokeWidth: Float,
        val indicatorRadius: Float,
        val indicatorStrokeWidth: Float,
        val strokeColor: Int,
        val indicatorStrokeColor: Int,
        val arcLength: Float,
        val radiusOffset: Float
    )
}