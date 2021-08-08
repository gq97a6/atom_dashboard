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
import com.netDashboard.*
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.app_on_destroy.AppOnDestroy
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.databinding.ActivityTilePropertiesBinding
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler
import com.netDashboard.tile.Tile
import com.netDashboard.tile.types.slider.SliderTile

class TilePropertiesActivity : AppCompatActivity() {
    private lateinit var b: ActivityTilePropertiesBinding

    private var dashboardId: Long = 0
    private lateinit var dashboard: Dashboard
    private lateinit var tile: Tile
    private var tileId = 0

    private lateinit var foregroundService: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dashboardId = intent.getLongExtra("dashboardId", 0)
        dashboard = Dashboards.get(dashboardId)

        tileId = intent.getIntExtra("tileId", 0)
        tile = dashboard.tiles[tileId]

        b = ActivityTilePropertiesBinding.inflate(layoutInflater)
        viewConfig()
        setContentView(b.root)

        val foregroundServiceHandler = ForegroundServiceHandler(this)
        foregroundServiceHandler.start()
        foregroundServiceHandler.bind()

        foregroundServiceHandler.service.observe(this, { s ->
            s?.let {
                foregroundService = it
                onServiceReady()
            }
        })

        if (dashboard.spanCount.toFloat() > 1f) {
            b.tpDimenWidth.valueFrom = 1f
            b.tpDimenWidth.valueTo = dashboard.spanCount.toFloat()
        } else {
            b.tpDimenWidth.valueFrom = 0f
            b.tpDimenWidth.valueTo = 1f
            b.tpDimenWidth.isEnabled = false
        }

        b.tpDimenWidth.addOnChangeListener { _, value, _ ->
            dimenOnChangeListener(value, b.tpDimenHeight.value)
        }

        b.tpDimenHeight.addOnChangeListener { _, value, _ ->
            dimenOnChangeListener(b.tpDimenWidth.value, value)
        }

        b.tpMqttSwitch.setOnCheckedChangeListener { _, state ->
            mqttSwitchOnCheckedChangeListener(state)
        }

        b.tpDesignChips.setOnCheckedChangeListener { _, _ ->
            designChipsOnCheckedChangeListener()
        }

        b.tpDesignHue.addOnChangeListener { _, _, _ ->
            colorOnChangeListener()

            tile.color = Color.HSVToColor(
                floatArrayOf(
                    b.tpDesignHue.value,
                    1 - b.tpDesignSaturation.value,
                    1 - b.tpDesignValue.value
                )
            )
        }

        b.tpDesignSaturation.addOnChangeListener { _, _, _ ->
            colorOnChangeListener()

            tile.color = Color.HSVToColor(
                floatArrayOf(
                    b.tpDesignHue.value,
                    1 - b.tpDesignSaturation.value,
                    1 - b.tpDesignValue.value
                )
            )
        }

        b.tpDesignValue.addOnChangeListener { _, _, _ ->
            colorOnChangeListener()

            tile.color = Color.HSVToColor(
                floatArrayOf(
                    b.tpDesignHue.value,
                    1 - b.tpDesignSaturation.value,
                    1 - b.tpDesignValue.value
                )
            )
        }

        b.tpMqttPub.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.mqttTopics.pubs.set(cs.toString(), null, null, "base")
            }
        })

        b.tpMqttSub.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.mqttTopics.subs.set(cs.toString(), null, null, "base")
            }
        })

        b.tpMqttPubValue.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.mqttPubValue = cs.toString()
            }
        })

        b.tpMqttJsonSwitch.setOnCheckedChangeListener { _, state ->
            tile.mqttPayloadIsJSON = state
        }

        b.tpMqttConfirmSwitch.setOnCheckedChangeListener { _, state ->
            tile.mqttPubConfirm = state
        }

        if (tile is SliderTile) {
            b.tpSliderFrom.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(cs: Editable) {}
                override fun beforeTextChanged(
                    cs: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                    cs.toString().let {
                        (tile as SliderTile).from = it.toFloatOrNull() ?: SliderTile().from
                    }
                }
            })

            b.tpSliderTo.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(cs: Editable) {}
                override fun beforeTextChanged(
                    cs: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                    cs.toString().let {
                        (tile as SliderTile).to = it.toFloatOrNull() ?: SliderTile().to
                    }
                }
            })

            b.tpSliderStep.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(cs: Editable) {}
                override fun beforeTextChanged(
                    cs: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                    cs.toString().let {
                        (tile as SliderTile).step = it.toFloatOrNull() ?: SliderTile().step
                    }
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()

        Dashboards.save()
        dashboard.daemonGroup?.mqttd?.reinit()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOnDestroy.call()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Intent(this, DashboardActivity::class.java).also {
            it.putExtra("dashboardId", dashboardId)
            startActivity(it)
            finish()
        }
    }

    private fun onServiceReady() {
    }

    private fun viewConfig() {

        b.tpTileType.text = tile.name

        //Dimensions
        b.tpDimenWidth.value = tile.width.toFloat()
        b.tpDimenHeight.value = tile.height.toFloat()
        dimenOnChangeListener(tile.width.toFloat(), tile.height.toFloat())

        //Design
        val hsv = floatArrayOf(0f, 0f, 0f)
        Color.colorToHSV(tile.color, hsv)

        b.tpDesignHue.value = hsv[0]
        b.tpDesignSaturation.value = 1 - hsv[1]
        b.tpDesignValue.value = 1 - hsv[2]
        colorOnChangeListener()

        b.tpDesignChipTheme.isChecked = tile.isColouredByTheme
        b.tpDesignChipColor.isChecked = !tile.isColouredByTheme
        designChipsOnCheckedChangeListener() //have influence on tile

        //MQTT
        b.tpMqttSwitch.isChecked = tile.mqttEnabled
        b.tpMqttPub.setText(tile.mqttTopics.pubs.get("base").topic)
        b.tpMqttSub.setText(tile.mqttTopics.subs.get("base").topic)
        b.tpMqttPubValue.setText(tile.mqttPubValue)
        b.tpMqttJsonSwitch.isChecked = tile.mqttPayloadIsJSON
        b.tpMqttConfirmSwitch.isChecked = tile.mqttPubConfirm
        mqttSwitchOnCheckedChangeListener(b.tpMqttSwitch.isChecked)

        when (tile) {
            is SliderTile -> {
                b.tpSlider.visibility = View.VISIBLE

                b.tpSliderFrom.setText((tile as SliderTile).from.dezero())
                b.tpSliderTo.setText((tile as SliderTile).to.dezero())
                b.tpSliderStep.setText((tile as SliderTile).step.dezero())
            }
        }
    }

    private fun dimenOnChangeListener(w: Float, h: Float) {
        //Validate
        if (w != dashboard.spanCount.toFloat()) b.tpDimenHeight.value = 1f
        if (dashboard.spanCount.toFloat() > 1f && h != 1f) {
            b.tpDimenWidth.value = dashboard.spanCount.toFloat()
        }

        b.tpDimenWidthText.text = b.tpDimenWidth.value.toInt().toString()
        b.tpDimenHeightText.text = b.tpDimenHeight.value.toInt().toString()

        tile.width = b.tpDimenWidth.value.toInt()
        tile.height = b.tpDimenHeight.value.toInt()
    }

    private fun designChipsOnCheckedChangeListener() {
        b.tpDesignInfo.visibility = if (b.tpDesignChipTheme.isChecked) {
            View.VISIBLE
        } else {
            View.GONE
        }

        b.tpDesignColor.visibility = if (b.tpDesignChipColor.isChecked) {
            View.VISIBLE
        } else {
            View.GONE
        }

        tile.isColouredByTheme = b.tpDesignChipTheme.isChecked

        b.tpDesignChipTheme.isEnabled = !b.tpDesignChipTheme.isChecked
        b.tpDesignChipColor.isEnabled = !b.tpDesignChipColor.isChecked
    }

    private fun colorOnChangeListener() {
        setHSVGradient()
        setChipColor()
    }

    private fun mqttSwitchOnCheckedChangeListener(state: Boolean) {
        b.tpMqtt.visibility = if (state) {
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
                1 - b.tpDesignSaturation.value,
                1 - b.tpDesignValue.value
            ), floatArrayOf(
                60f,
                1 - b.tpDesignSaturation.value,
                1 - b.tpDesignValue.value
            ), floatArrayOf(
                120f,
                1 - b.tpDesignSaturation.value,
                1 - b.tpDesignValue.value
            ), floatArrayOf(
                180f,
                1 - b.tpDesignSaturation.value,
                1 - b.tpDesignValue.value
            ), floatArrayOf(
                240f,
                1 - b.tpDesignSaturation.value,
                1 - b.tpDesignValue.value
            ), floatArrayOf(
                300f,
                1 - b.tpDesignSaturation.value,
                1 - b.tpDesignValue.value
            ), floatArrayOf(
                360f,
                1 - b.tpDesignSaturation.value,
                1 - b.tpDesignValue.value
            )
        )

        val saturationColors = listOf(
            floatArrayOf(
                b.tpDesignHue.value,
                1f,
                1 - b.tpDesignValue.value
            ), floatArrayOf(
                b.tpDesignHue.value,
                0f,
                1 - b.tpDesignValue.value
            )
        )

        val valueColors = listOf(
            floatArrayOf(
                b.tpDesignHue.value,
                1 - b.tpDesignSaturation.value,
                1f
            ), floatArrayOf(
                b.tpDesignHue.value,
                1 - b.tpDesignSaturation.value,
                0f
            )
        )

        b.tpDesignHue.setGradientSliderBackground(hueColors)
        b.tpDesignSaturation.setGradientSliderBackground(saturationColors)
        b.tpDesignValue.setGradientSliderBackground(valueColors)
    }

    private fun setChipColor() {
        setChipColor(
            Color.HSVToColor(
                floatArrayOf(
                    b.tpDesignHue.value,
                    1 - b.tpDesignSaturation.value,
                    1 - b.tpDesignValue.value
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

        val chipBackground = b.tpDesignChipColor.chipBackgroundColor

        val colorsBackground = intArrayOf(
            chipBackground?.getColorForState(intArrayOf(android.R.attr.state_enabled), Color.RED)
                ?: Color.RED,
            color,
            chipBackground?.getColorForState(intArrayOf(-android.R.attr.state_checked), Color.RED)
                ?: Color.RED,
            chipBackground?.getColorForState(intArrayOf(-android.R.attr.state_pressed), Color.RED)
                ?: Color.RED,
        )

        val chipText = b.tpDesignChipColor.textColors

        val colorsText = intArrayOf(
            chipText?.getColorForState(intArrayOf(android.R.attr.state_enabled), Color.RED)
                ?: Color.RED,
            getContrastColor(color).alpha(.75f),
            chipText?.getColorForState(intArrayOf(-android.R.attr.state_checked), Color.RED)
                ?: Color.RED,
            chipText?.getColorForState(intArrayOf(-android.R.attr.state_pressed), Color.RED)
                ?: Color.RED,
        )

        b.tpDesignChipColor.chipBackgroundColor = ColorStateList(states, colorsBackground)
        b.tpDesignChipColor.setTextColor(ColorStateList(states, colorsText))
    }
}