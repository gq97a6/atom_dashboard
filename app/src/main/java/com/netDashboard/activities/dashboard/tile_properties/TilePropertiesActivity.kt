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
import com.netDashboard.app_on_destroy.AppOnDestroy
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
    private lateinit var tile: Tile
    private var tileId = 0

    private lateinit var foregroundService: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dashboardName = intent.getStringExtra("dashboardName") ?: ""
        dashboard = Dashboards.get(dashboardName)!!

        tileId = intent.getIntExtra("tileId", 0)
        tile = dashboard.tiles[tileId]

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

        if (dashboard.spanCount.toFloat() > 1f) {
            b.cntDimenWidth.valueFrom = 1f
            b.cntDimenWidth.valueTo = dashboard.spanCount.toFloat()
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

            tile.color = Color.HSVToColor(
                floatArrayOf(
                    b.cntDesignHue.value,
                    1 - b.cntDesignSaturation.value,
                    1 - b.cntDesignValue.value
                )
            )
        }

        b.cntDesignSaturation.addOnChangeListener { _, _, _ ->
            colorOnChangeListener()

            tile.color = Color.HSVToColor(
                floatArrayOf(
                    b.cntDesignHue.value,
                    1 - b.cntDesignSaturation.value,
                    1 - b.cntDesignValue.value
                )
            )
        }

        b.cntDesignValue.addOnChangeListener { _, _, _ ->
            colorOnChangeListener()

            tile.color = Color.HSVToColor(
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
                tile.mqttTopics.pubs.set(cs.toString(), null, null,"base")
            }
        })

        b.cntMqttSub.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.mqttTopics.subs.set(cs.toString(), null, null,"base")
            }
        })

        //b.cntMqttPubValue.addTextChangedListener(object : TextWatcher {
        //    override fun afterTextChanged(cs: Editable) {}
        //    override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
        //    override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
        //        tile.mqttTopics.pubs.topic = cs.toString()
        //    }
        //})
    }

    override fun onPause() {
        super.onPause()

        Dashboards.save(dashboardName)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOnDestroy.call()
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

        b.cntTileType.text = tile.name

        //Dimensions
        b.cntDimenWidth.value = tile.width.toFloat()
        b.cntDimenHeight.value = tile.height.toFloat()
        dimenOnChangeListener(tile.width.toFloat(), tile.height.toFloat())

        //Design
        val hsv = floatArrayOf(0f, 0f, 0f)
        Color.colorToHSV(tile.color, hsv)

        b.cntDesignHue.value = hsv[0]
        b.cntDesignSaturation.value = 1 - hsv[1]
        b.cntDesignValue.value = 1 - hsv[2]
        colorOnChangeListener()

        b.cntDesignChipTheme.isChecked = tile.isColouredByTheme
        b.cntDesignChipColor.isChecked = !tile.isColouredByTheme
        designChipsOnCheckedChangeListener() //have influence on tile

        //MQTT
        b.cntMqttSwitch.isChecked = tile.mqttEnabled
        b.cntMqttPub.setText(tile.mqttTopics.pubs.get("base").topic)
        b.cntMqttSub.setText(tile.mqttTopics.subs.get("base").topic)
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
        if (w != dashboard.spanCount.toFloat()) b.cntDimenHeight.value = 1f
        if (dashboard.spanCount.toFloat() > 1f && h != 1f) {
            b.cntDimenWidth.value = dashboard.spanCount.toFloat()
        }

        b.cntDimenWidthText.text = b.cntDimenWidth.value.toInt().toString()
        b.cntDimenHeightText.text = b.cntDimenHeight.value.toInt().toString()

        tile.width = b.cntDimenWidth.value.toInt()
        tile.height = b.cntDimenHeight.value.toInt()
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

        tile.isColouredByTheme = b.cntDesignChipTheme.isChecked

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

        tile.mqttEnabled = state
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