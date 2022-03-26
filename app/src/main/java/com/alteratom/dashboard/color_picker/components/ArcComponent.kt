package com.alteratom.dashboard.color_picker.components

import android.graphics.*
import android.graphics.Color.WHITE
import android.graphics.Paint.Cap.ROUND
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.STROKE
import android.view.MotionEvent
import com.alteratom.dashboard.color_picker.Metrics
import com.alteratom.dashboard.color_picker.Paints
import com.alteratom.dashboard.color_picker.listeners.OnColorSelectionListener
import kotlin.math.*

internal abstract class ArcComponent(
    private val metrics: Metrics,
    private val paints: Paints,
    private val arcLength: Float,
    private val arcStartAngle: Float
) {

    private var radius: Float = 0f

    var fillWidth: Float = 0f
    var strokeWidth: Float = 0f
    var strokeColor: Int = 0

    var indicatorRadius: Float = 0f
    var indicatorStrokeWidth: Float = 0f
    var indicatorStrokeColor: Int = 0

    private var indicatorX: Float = 0f
    private var indicatorY: Float = 0f

    var angle: Double = 0.0

    protected abstract val componentIndex: Int
    abstract val noOfColors: Int
    internal abstract val colors: IntArray
    internal abstract val colorPosition: FloatArray

    private val matrix = Matrix()
    private lateinit var shader: Shader
    private var innerCircleArcReference: RectF? = null

    private var isTouched = false
    private var colorSelectionListener: OnColorSelectionListener? = null

    fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (PointF(x, y) in this) {
                    colorSelectionListener?.onColorSelectionStart(metrics.getColor())
                    isTouched = true
                    calculateAngle(x, y)
                    updateComponent(angle)
                    colorSelectionListener?.onColorSelected(metrics.getColor())
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isTouched) {
                    calculateAngle(x, y)
                    updateComponent(angle)
                    colorSelectionListener?.onColorSelected(metrics.getColor())
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isTouched) colorSelectionListener?.onColorSelectionEnd(metrics.getColor())
                isTouched = false
            }
        }

        return isTouched
    }

    operator fun contains(point: PointF): Boolean {
        val touchRadius = indicatorRadius * 2
        return point.x in (indicatorX - touchRadius)..(indicatorX + touchRadius) && point.y in (indicatorY - touchRadius)..(indicatorY + touchRadius)
    }

    internal fun setColorSelectionListener(listener: OnColorSelectionListener) {
        colorSelectionListener = listener
    }

    internal fun setRadius(outerRadius: Float, offset: Float) {
        radius =
            outerRadius - (max(indicatorRadius + indicatorStrokeWidth, fillWidth)) - offset
    }

    /**
     * This is the max value of the component. For now the min value is taken as 0
     */
    abstract val range: Float

    private val arcEndAngle: Float
        get() {
            val end = arcStartAngle + arcLength
            return if (end > 360f) end - 360f else end
        }

    init {
        angle = (arcStartAngle + arcLength / 2f).toDouble()
    }

    fun drawComponent(canvas: Canvas) {
        val shaderPaint = paints.shaderPaint
        shaderPaint.style = STROKE
        shaderPaint.strokeCap = ROUND

        if (innerCircleArcReference == null) {
            innerCircleArcReference = RectF(
                metrics.centerX - radius,
                metrics.centerY - radius,
                metrics.centerX + radius,
                metrics.centerY + radius
            )
        }
        innerCircleArcReference?.let {
            if (strokeWidth > 0) {
                shaderPaint.shader = null
                shaderPaint.color = if (strokeColor == 0) WHITE else strokeColor
                shaderPaint.strokeWidth = fillWidth + strokeWidth * 2
                canvas.drawArc(it, arcStartAngle, arcLength, false, shaderPaint)
            }

            shaderPaint.strokeWidth = fillWidth
            shaderPaint.shader = getShader()
            canvas.drawArc(it, arcStartAngle, arcLength, false, shaderPaint)
        }

        indicatorX = (metrics.centerX + radius * cos(Math.toRadians(angle))).toFloat()
        indicatorY = (metrics.centerY + radius * sin(Math.toRadians(angle))).toFloat()

        val indicatorPaint = paints.indicatorPaint
        indicatorPaint.style = FILL

        indicatorPaint.color = WHITE
        canvas.drawCircle(indicatorX, indicatorY, indicatorRadius, indicatorPaint)
    }

    private fun getShader(): Shader {
        with(metrics) {
            updateColorArray(color.copyOf())

            for (i in 0 until noOfColors) {
                colorPosition[i] = i * (arcLength / (noOfColors - 1)) / 360f
            }

            shader = SweepGradient(centerX, centerY, colors, colorPosition)
            // We need a margin of rotation due to the Paint.Cap.Round
            matrix.setRotate(arcStartAngle - (fillWidth / 3f / density), centerX, centerY)
            shader.setLocalMatrix(matrix)
        }

        return shader
    }

    internal abstract fun updateColorArray(color: FloatArray): IntArray

    private fun calculateAngle(x1: Float, y1: Float) {
        val x = x1 - metrics.centerX
        val y = y1 - metrics.centerY
        val c = sqrt((x * x + y * y).toDouble())

        angle = Math.toDegrees(acos(x / c))
        if (y < 0) {
            angle = 360 - angle
        }

        // Don't let the indicator go outside the arc
        // limit the indicator between arcStartAngle and arcEndAngle
        val associatedArcLength = 360f - arcLength
        val middleOfAssociatedArc = arcEndAngle + associatedArcLength / 2f
        if (arcEndAngle < arcStartAngle) {
            calculateAngleInContinuousRange(middleOfAssociatedArc)
        } else if (arcEndAngle > arcStartAngle) {
            calculateAngleInNonContinuousRange(middleOfAssociatedArc)
        }
    }

    /**
     * This would be the case when [arcStartAngle]=285 and [arcEndAngle]=75, so that the arc has the 0 degree crossover. This means that the
     * associated arc (360 - [arcLength]) is a continuous range. When the angle is in this range, we need to either set it to the [arcStartAngle]
     * or the [arcEndAngle]
     *
     * @param middle the middle point (in angle) of the associated arc
     */
    private fun calculateAngleInContinuousRange(middle: Float) {
        when (angle) {
            in arcEndAngle..middle -> angle = arcEndAngle.toDouble()
            in middle..arcStartAngle -> angle = arcStartAngle.toDouble()
        }
    }

    /**
     * This is the case where the arc is a continuous range, i.e, the 0 crossover occurs in the associated arc. This can happen in two ways.
     *
     * 1. The [middle] point can be before the 0 degree. Eg. [arcStartAngle]=10 and [arcEndAngle]=120
     * 2. The [middle] point can be after the 0 degree. Eg. [arcStartAngle]=100 and [arcEndAngle]=350
     *
     * @param middle the middle point (in angle) of the associated arc
     */
    private fun calculateAngleInNonContinuousRange(middle: Float) {
        if (middle > 360f) {
            val correctedMiddle = middle - 360f
            when (angle) {
                in arcEndAngle..360f, in 0f..correctedMiddle -> angle = arcEndAngle.toDouble()
                in correctedMiddle..arcStartAngle -> angle = arcStartAngle.toDouble()
            }
        } else {
            when (angle) {
                in arcEndAngle..middle -> angle = arcEndAngle.toDouble()
                in middle..360f, in 0f..arcStartAngle -> angle = arcStartAngle.toDouble()
            }
        }
    }

    fun updateComponent(angle: Double) {
        var relativeAngle = angle
        if (angle < arcStartAngle) {
            relativeAngle += 360f
        }

        val baseAngle = relativeAngle - arcStartAngle
        val component = (baseAngle / arcLength) * range

        metrics.color[componentIndex] = component.toFloat()
    }

    fun updateAngle(component: Float) {
        val baseAngle = component / range * arcLength
        val relativeAngle = baseAngle + arcStartAngle

        angle = relativeAngle.toDouble()
    }
}