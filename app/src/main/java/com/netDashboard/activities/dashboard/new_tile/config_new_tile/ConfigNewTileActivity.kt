package com.netDashboard.activities.dashboard.new_tile.config_new_tile

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.netDashboard.R
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.alpha
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.databinding.ActivityConfigNewTileBinding
import com.netDashboard.getContrastColor
import com.netDashboard.tile.Tile
import com.netDashboard.tile.TileTypeList
import com.netDashboard.toPx

class ConfigNewTileActivity : AppCompatActivity() {
    private lateinit var b: ActivityConfigNewTileBinding

    private lateinit var dashboardName: String
    private lateinit var dashboard: Dashboard
    private lateinit var settings: Dashboard.Settings

    private lateinit var tile: Tile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tileId = intent.getIntExtra("tileId", 0)
        tile = TileTypeList().getById(tileId)

        b = ActivityConfigNewTileBinding.inflate(layoutInflater)
        configView()
        setContentView(b.root)

        dashboardName = intent.getStringExtra("dashboardName") ?: ""
        dashboard = Dashboard(filesDir.canonicalPath, dashboardName)
        settings = dashboard.settings

        if (settings.spanCount.toFloat() > 1f) {
            b.cntWidth.valueFrom = 1f
            b.cntWidth.valueTo = settings.spanCount.toFloat()
        } else {
            b.cntWidth.valueFrom = 0f
            b.cntWidth.valueTo = 1f
            b.cntWidth.isEnabled = false
        }

        b.cntWidth.addOnChangeListener { _, value, _ ->
            b.cntWidthValue.text = value.toInt().toString()
            if (value != settings.spanCount.toFloat()) b.cntHeight.value = 1f
        }

        b.cntHeight.addOnChangeListener { _, value, _ ->
            b.cntHeightValue.text = value.toInt().toString()

            if (settings.spanCount.toFloat() > 1f) {
                if (value != 1f) b.cntWidth.value = settings.spanCount.toFloat()
            }
        }

        b.cntAdd.setOnClickListener {
            configTile()
            configTileExtra()

            Intent(this, DashboardActivity::class.java).also {
                it.putExtra("dashboardName", dashboardName)
                finish()
                startActivity(it)
            }
        }

        b.cntMqttSwitch.setOnCheckedChangeListener { _, state ->
            b.cntMqtt.visibility = if (state) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        b.cntDesignChips.setOnCheckedChangeListener { _, _ ->
            b.cntDesignInfo.visibility = if (b.chip0.isChecked) {
                View.VISIBLE
            } else {
                View.GONE
            }

            b.cntDesignColor.visibility = if (b.chip0.isChecked) {
                View.GONE
            } else {
                View.VISIBLE
            }

            b.chip0.isEnabled = !b.chip0.isChecked
            b.chip1.isEnabled = !b.chip1.isChecked
        }

        b.cntDesignHue.addOnChangeListener { _, _, _ ->
            handleHueSlider()
        }

        b.cntDesignSaturation.addOnChangeListener { _, _, _ ->
            handleSaturationSlider()
        }

        b.cntDesignValue.addOnChangeListener { _, _, _ ->
            handleLightnessSlider()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Intent(this, DashboardActivity::class.java).also {
            it.putExtra("dashboardName", dashboardName)
            startActivity(it)
            finish()
        }
    }

    private fun configView() {

        b.cntTileType.text = tile.type
        handleHueSlider()

        //if (tile.type == "button") {
        //} else if (tile.type == "slider") {
        //}
    }

    private fun configTile() {
        tile.width = b.cntWidth.value.toInt()
        tile.height = b.cntHeight.value.toInt()

        tile.isColouredByTheme = if(b.chip0.isChecked) {
            true
        } else {
            tile.color = Color.HSVToColor(
                floatArrayOf(
                    b.cntDesignHue.value,
                    1 - b.cntDesignSaturation.value,
                    1 - b.cntDesignValue.value
                )
            )

            false
        }

        tile.mqttEnabled = if (b.cntMqttSwitch.isChecked) {
            tile.mqttSubTopics.add(b.cntSliderMqttSub.text.toString())
            tile.mqttPubTopics.add(b.cntSliderMqttPub.text.toString())

            true
        } else {
            false
        }

        var list = dashboard.tiles

        if (list.isEmpty()) {
            list = listOf(tile)
        } else {
            list = list.toMutableList()
            list.add(tile)
            list = list.toList()
        }

        dashboard.tiles = list
    }

    private fun configTileExtra() {
        //if (tile.type == "button") {
        //} else if (tile.type == "slider") {
        //}
    }

    private fun handleHueSlider() {
        val color = Color.HSVToColor(
            floatArrayOf(
                b.cntDesignHue.value,
                1 - b.cntDesignSaturation.value,
                1 - b.cntDesignValue.value
            )
        )

        val backgroundSaturation = ResourcesCompat.getDrawable(
            resources,
            R.drawable.background_bw_slider,
            null
        ) as GradientDrawable

        val backgroundValue = ResourcesCompat.getDrawable(
            resources,
            R.drawable.background_bw_slider,
            null
        ) as GradientDrawable

        val colorListSaturation =
            intArrayOf(color, Color.WHITE)

        val colorListValue =
            intArrayOf(color, Color.BLACK)

        backgroundSaturation.mutate()
        backgroundValue.mutate()

        backgroundSaturation.colors = colorListSaturation
        backgroundValue.colors = colorListValue

        val insetBackgroundSaturation = InsetDrawable(
            backgroundSaturation,
            (14.5f).toPx(),
            (22.5f).toPx(),
            (14.5f).toPx(),
            (22f).toPx()
        )

        val insetBackgroundValue = InsetDrawable(
            backgroundValue,
            (14.5f).toPx(),
            (22.5f).toPx(),
            (14.5f).toPx(),
            (22f).toPx()
        )

        b.cntDesignSaturation.background = insetBackgroundSaturation
        b.cntDesignValue.background = insetBackgroundValue

        setChipColor()
    }

    private fun handleLightnessSlider() {
        setChipColor()
    }

    private fun handleSaturationSlider() {
        setChipColor()
    }

    private fun setChipColor() {
        setChipColor(
            Color.HSVToColor(
                floatArrayOf(
                    b.cntDesignHue.value,
                    1 - b.cntDesignSaturation.value,
                    1 - b.cntDesignValue.value
                )
            )
        )
    }

    private fun setChipColor(color: Int) {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_pressed)
        )

        val chipBackground = b.chip1.chipBackgroundColor

        val colorsBackground = intArrayOf(
            chipBackground?.getColorForState(intArrayOf(android.R.attr.state_enabled), Color.RED)
                ?: Color.RED,
            color,
            chipBackground?.getColorForState(intArrayOf(-android.R.attr.state_checked), Color.RED)
                ?: Color.RED,
            chipBackground?.getColorForState(intArrayOf(-android.R.attr.state_pressed), Color.RED)
                ?: Color.RED,
        )

        val chipText = b.chip1.textColors

        val colorsText = intArrayOf(
            chipText?.getColorForState(intArrayOf(android.R.attr.state_enabled), Color.RED)
                ?: Color.RED,
            getContrastColor(color).alpha(75),
            chipText?.getColorForState(intArrayOf(-android.R.attr.state_checked), Color.RED)
                ?: Color.RED,
            chipText?.getColorForState(intArrayOf(-android.R.attr.state_pressed), Color.RED)
                ?: Color.RED,
        )

        b.chip1.chipBackgroundColor = ColorStateList(states, colorsBackground)
        b.chip1.setTextColor(ColorStateList(states, colorsText))
    }
}