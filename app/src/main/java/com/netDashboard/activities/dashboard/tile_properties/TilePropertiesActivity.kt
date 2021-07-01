package com.netDashboard.activities.dashboard.tile_properties

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.netDashboard.R
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.alpha
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.databinding.ActivityTilePropertiesBinding
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler
import com.netDashboard.getContrastColor
import com.netDashboard.tile.Tile
import com.netDashboard.toPx

class TilePropertiesActivity : AppCompatActivity() {
    private lateinit var b: ActivityTilePropertiesBinding

    private lateinit var dashboardName: String
    private lateinit var dashboard: Dashboard
    private lateinit var properties: Dashboard.Properties

    private lateinit var foregroundService: ForegroundService

    private lateinit var tileProperties: Tile.Properties
    private var tileId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dashboardName = intent.getStringExtra("dashboardName") ?: ""
        dashboard = Dashboards.get(dashboardName)!!
        properties = dashboard.p

        tileId = intent.getIntExtra("tileId", 0)
        tileProperties = dashboard.tiles[tileId].p

        b = ActivityTilePropertiesBinding.inflate(layoutInflater)
        viewConfig()
        setContentView(b.root)

        val foregroundServiceHandler = ForegroundServiceHandler(this)
        foregroundServiceHandler.start()
        foregroundServiceHandler.bind()

        foregroundServiceHandler.service.observe(this, { s ->
            if (s != null) {
                foregroundService = s
                onServiceReady()
            }
        })

        if (properties.spanCount.toFloat() > 1f) {
            b.cntDimenWidth.valueFrom = 1f
            b.cntDimenWidth.valueTo = properties.spanCount.toFloat()
        } else {
            b.cntDimenWidth.valueFrom = 0f
            b.cntDimenWidth.valueTo = 1f
            b.cntDimenWidth.isEnabled = false
        }

        b.cntDimenWidth.addOnChangeListener { _, value, _ ->
            dimenOnChangeListener(value, b.cntDimenHeight.value)
        }

        b.cntDimenHeight.addOnChangeListener { _, value, _ ->
            dimenOnChangeListener(b.cntDimenWidth.value, value)
        }

        b.cntMqttSwitch.setOnCheckedChangeListener { _, state ->
            mqttSwitchOnCheckedChangeListener(state)
        }

        b.cntDesignChips.setOnCheckedChangeListener { _, _ ->
            designChipsOnCheckedChangeListener()
        }

        b.cntDesignHue.addOnChangeListener { _, _, _ ->
            colorOnChangeListener()

            tileProperties.color = Color.HSVToColor(
                floatArrayOf(
                    b.cntDesignHue.value,
                    1 - b.cntDesignSaturation.value,
                    1 - b.cntDesignValue.value
                )
            )
        }

        b.cntDesignSaturation.addOnChangeListener { _, _, _ ->
            colorOnChangeListener()

            tileProperties.color = Color.HSVToColor(
                floatArrayOf(
                    b.cntDesignHue.value,
                    1 - b.cntDesignSaturation.value,
                    1 - b.cntDesignValue.value
                )
            )
        }

        b.cntDesignValue.addOnChangeListener { _, _, _ ->
            colorOnChangeListener()

            tileProperties.color = Color.HSVToColor(
                floatArrayOf(
                    b.cntDesignHue.value,
                    1 - b.cntDesignSaturation.value,
                    1 - b.cntDesignValue.value
                )
            )
        }

        b.cntMqttPub.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tileProperties.mqttTopics.pub.set(cs.toString(), "pub")
            }
        })

        b.cntMqttSub.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tileProperties.mqttTopics.sub.set(cs.toString(), "sub")
            }
        })
    }

    override fun onPause() {
        super.onPause()

        val list = dashboard.tiles
        list[tileId].p = tileProperties

        dashboard.tiles = list
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Intent(this, DashboardActivity::class.java).also {
            it.putExtra("dashboardName", dashboardName)
            startActivity(it)
            finish()
        }
    }

    private fun onServiceReady() {

    }

    private fun viewConfig() {

        b.cntTileType.text = tileProperties.name

        //Dimensions
        b.cntDimenWidth.value = tileProperties.width.toFloat()
        b.cntDimenHeight.value = tileProperties.height.toFloat()
        dimenOnChangeListener(tileProperties.width.toFloat(), tileProperties.height.toFloat())

        //Design
        val hsv = floatArrayOf(0f, 0f, 0f)
        Color.colorToHSV(tileProperties.color, hsv)

        b.cntDesignHue.value = hsv[0]
        b.cntDesignSaturation.value = 1 - hsv[1]
        b.cntDesignValue.value = 1 - hsv[2]
        colorOnChangeListener()

        b.cntDesignChipTheme.isChecked = tileProperties.isColouredByTheme
        b.cntDesignChipColor.isChecked = !tileProperties.isColouredByTheme
        designChipsOnCheckedChangeListener() //have influence on tile

        //MQTT
        b.cntMqttSwitch.isChecked = tileProperties.mqttEnabled
        b.cntMqttPub.setText(tileProperties.mqttTopics.pub.get("pub"))
        b.cntMqttSub.setText(tileProperties.mqttTopics.sub.get("sub"))
        mqttSwitchOnCheckedChangeListener(b.cntMqttSwitch.isChecked)

        //when (tile) {
        //    is ButtonTile -> {
        //    }
        //    is SliderTile -> {
        //    }
        //}
    }

    private fun dimenOnChangeListener(w: Float, h: Float) {
        //Validate
        if (w != properties.spanCount.toFloat()) b.cntDimenHeight.value = 1f
        if (properties.spanCount.toFloat() > 1f && h != 1f) {
            b.cntDimenWidth.value = properties.spanCount.toFloat()
        }

        b.cntDimenWidthText.text = b.cntDimenWidth.value.toInt().toString()
        b.cntDimenHeightText.text = b.cntDimenHeight.value.toInt().toString()

        tileProperties.width = b.cntDimenWidth.value.toInt()
        tileProperties.height = b.cntDimenHeight.value.toInt()
    }

    private fun designChipsOnCheckedChangeListener() {
        b.cntDesignInfo.visibility = if (b.cntDesignChipTheme.isChecked) {
            View.VISIBLE
        } else {
            View.GONE
        }

        b.cntDesignColor.visibility = if (b.cntDesignChipColor.isChecked) {
            View.VISIBLE
        } else {
            View.GONE
        }

        tileProperties.isColouredByTheme = b.cntDesignChipTheme.isChecked

        b.cntDesignChipTheme.isEnabled = !b.cntDesignChipTheme.isChecked
        b.cntDesignChipColor.isEnabled = !b.cntDesignChipColor.isChecked
    }

    private fun colorOnChangeListener() {
        setHSVGradient()
        setChipColor()
    }

    private fun mqttSwitchOnCheckedChangeListener(state: Boolean) {
        b.cntMqtt.visibility = if (state) {
            View.VISIBLE
        } else {
            View.GONE
        }

        tileProperties.mqttEnabled = state
    }

    private fun View.setGradientSliderBackground(colors: List<FloatArray>) {

        val colorList = IntArray(colors.size)

        for ((i, c) in colors.withIndex()) {
            colorList[i] = Color.HSVToColor(c)
        }

        val background = ResourcesCompat.getDrawable(
            resources,
            R.drawable.background_bw_slider,
            null
        ) as GradientDrawable

        background.mutate()
        background.colors = colorList

        this.background = InsetDrawable(
            background,
            (14.5f).toPx(),
            (22.5f).toPx(),
            (14.5f).toPx(),
            (22f).toPx()
        )
    }

    private fun setHSVGradient() {
        val hueColors = listOf(
            floatArrayOf(
                0f,
                1 - b.cntDesignSaturation.value,
                1 - b.cntDesignValue.value
            ), floatArrayOf(
                60f,
                1 - b.cntDesignSaturation.value,
                1 - b.cntDesignValue.value
            ), floatArrayOf(
                120f,
                1 - b.cntDesignSaturation.value,
                1 - b.cntDesignValue.value
            ), floatArrayOf(
                180f,
                1 - b.cntDesignSaturation.value,
                1 - b.cntDesignValue.value
            ), floatArrayOf(
                240f,
                1 - b.cntDesignSaturation.value,
                1 - b.cntDesignValue.value
            ), floatArrayOf(
                300f,
                1 - b.cntDesignSaturation.value,
                1 - b.cntDesignValue.value
            ), floatArrayOf(
                360f,
                1 - b.cntDesignSaturation.value,
                1 - b.cntDesignValue.value
            )
        )

        val saturationColors = listOf(
            floatArrayOf(
                b.cntDesignHue.value,
                1f,
                1 - b.cntDesignValue.value
            ), floatArrayOf(
                b.cntDesignHue.value,
                0f,
                1 - b.cntDesignValue.value
            )
        )

        val valueColors = listOf(
            floatArrayOf(
                b.cntDesignHue.value,
                1 - b.cntDesignSaturation.value,
                1f
            ), floatArrayOf(
                b.cntDesignHue.value,
                1 - b.cntDesignSaturation.value,
                0f
            )
        )

        b.cntDesignHue.setGradientSliderBackground(hueColors)
        b.cntDesignSaturation.setGradientSliderBackground(saturationColors)
        b.cntDesignValue.setGradientSliderBackground(valueColors)
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

        val chipBackground = b.cntDesignChipColor.chipBackgroundColor

        val colorsBackground = intArrayOf(
            chipBackground?.getColorForState(intArrayOf(android.R.attr.state_enabled), Color.RED)
                ?: Color.RED,
            color,
            chipBackground?.getColorForState(intArrayOf(-android.R.attr.state_checked), Color.RED)
                ?: Color.RED,
            chipBackground?.getColorForState(intArrayOf(-android.R.attr.state_pressed), Color.RED)
                ?: Color.RED,
        )

        val chipText = b.cntDesignChipColor.textColors

        val colorsText = intArrayOf(
            chipText?.getColorForState(intArrayOf(android.R.attr.state_enabled), Color.RED)
                ?: Color.RED,
            getContrastColor(color).alpha(.75f),
            chipText?.getColorForState(intArrayOf(-android.R.attr.state_checked), Color.RED)
                ?: Color.RED,
            chipText?.getColorForState(intArrayOf(-android.R.attr.state_pressed), Color.RED)
                ?: Color.RED,
        )

        b.cntDesignChipColor.chipBackgroundColor = ColorStateList(states, colorsBackground)
        b.cntDesignChipColor.setTextColor(ColorStateList(states, colorsText))
    }
}