package com.netDashboard.colorPlotter

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import com.netDashboard.Activity
import com.netDashboard.activities.DataPoint
import com.netDashboard.databinding.ActivityColorPlotterBinding
import com.netDashboard.globals.G

class ColorPlotterActivity : AppCompatActivity() {
    private lateinit var b: ActivityColorPlotterBinding
    var hue = 0f

    companion object {
        var con = 1.7f

        var valueRange = floatArrayOf(1f, 0f)
        var saturationRange = floatArrayOf(1f, 0f)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Activity.onCreate(this)

        b = ActivityColorPlotterBinding.inflate(layoutInflater)
        setContentView(b.root)
        G.theme.apply(this, b.root)

        b.tHue.setOnTouchListener { _, e ->
            hue = b.tHue.value
            b.conText.text = "$con | ${hue.toInt()}"

            val dark = getList(true, b.tHue.value)
            val light = getList(false, b.tHue.value)

            b.graphView.setData(dark)
            b.graphView2.setData(light)

            return@setOnTouchListener e.action == KeyEvent.ACTION_UP
        }

        b.contrast.setOnTouchListener { _, e ->
            con = b.contrast.value
            b.conText.text = "$con | ${hue.toInt()}"

            val dark = getList(true, b.tHue.value)
            val light = getList(false, b.tHue.value)

            b.graphView.setData(dark)
            b.graphView2.setData(light)

            return@setOnTouchListener e.action == KeyEvent.ACTION_UP
        }
    }

    fun getList(isDark: Boolean, hue: Float): List<DataPoint> {
        val list = mutableListOf<DataPoint>()
        var x = 0
        var c = 0

        val colorBackground = if (isDark) Color.rgb(20, 20, 20)
        else Color.rgb(240, 240, 240)

        //Compute maximal saturation/value
        for (ii in 100 downTo 0) {
            for (i in 100 downTo 0) {
                c = Color.HSVToColor(
                    floatArrayOf(
                        hue,
                        i / 100f,
                        (100 - ii) / 100f,
                    )
                )

                list.add(
                    DataPoint(
                        i.toFloat(), // Saturation left-right
                        ii.toFloat(), // Value top-bottomC
                        ColorUtils.calculateContrast(c, colorBackground).toFloat() > con,
                        Paint().apply {
                            color = c
                        }
                    )
                )
                x++
            }
        }

        return list
    }
}
