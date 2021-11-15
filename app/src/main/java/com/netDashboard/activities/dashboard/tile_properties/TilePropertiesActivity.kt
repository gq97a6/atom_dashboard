package com.netDashboard.activities.dashboard.tile_properties

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.R
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.app_on.AppOn
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboard.Companion.byId
import com.netDashboard.databinding.ActivityTilePropertiesBinding
import com.netDashboard.globals.G.dashboards
import com.netDashboard.tile.Tile
import com.netDashboard.tile.types.slider.SliderTile

class TilePropertiesActivity : AppCompatActivity() {
    private lateinit var b: ActivityTilePropertiesBinding

    private var dashboardId: Long = 0
    private lateinit var dashboard: Dashboard
    private lateinit var tile: Tile
    private var tileIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppOn.create(this)

        dashboardId = intent.getLongExtra("dashboardId", 0)
        dashboard = dashboards.byId(dashboardId)

        tileIndex = intent.getIntExtra("tileIndex", 0)
        tile = dashboard.tiles[tileIndex]

        b = ActivityTilePropertiesBinding.inflate(layoutInflater)
        dashboard.resultTheme.apply(this, b.root)
        viewConfig()
        setContentView(b.root)

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

        b.tpMqttPub.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.mqttData.pubs["base"] = cs.toString()
            }
        })

        b.tpMqttSub.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.mqttData.subs["base"] = cs.toString()
            }
        })

        b.tpMqttPubPayload.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.mqttData.pubPayload = cs.toString()
            }
        })

        b.tpMqttJsonSwitch.setOnCheckedChangeListener { _, state ->
            tile.mqttData.payloadIsJson = state
            b.tpMqttJsonPayload.visibility = if(state) VISIBLE else GONE
        }

        b.tpMqttJsonPayloadPath.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.mqttData.jsonPaths["path"] = cs.toString()
            }
        })

        b.tpMqttConfirmSwitch.setOnCheckedChangeListener { _, state ->
            tile.mqttData.confirmPub = state
        }

        b.tpQos.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
            tile.mqttData.qos = when (id) {
                R.id.tp_qos0 -> 0
                R.id.tp_qos1 -> 1
                R.id.tp_qos2 -> 2
                else -> 1
            }
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
                        (tile as SliderTile).from = it.toIntOrNull() ?: SliderTile().from
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
                        (tile as SliderTile).to = it.toIntOrNull() ?: SliderTile().to
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
                        (tile as SliderTile).step = it.toIntOrNull() ?: SliderTile().step
                    }
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()

        dashboard.daemonGroup?.mqttd?.reinit()

        AppOn.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOn.destroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Intent(this, DashboardActivity::class.java).also {
            it.putExtra("dashboardId", dashboardId)
            startActivity(it)
        }
    }

    private fun viewConfig() {

        b.tpTileType.text = tile.typeTag

        //Dimensions
        b.tpDimenWidth.value = tile.width.toFloat()
        b.tpDimenHeight.value = tile.height.toFloat()
        dimenOnChangeListener(tile.width.toFloat(), tile.height.toFloat())

        //MQTT
        b.tpMqttSwitch.isChecked = tile.mqttData.isEnabled
        b.tpMqttPub.setText(tile.mqttData.pubs["base"])
        b.tpMqttSub.setText(tile.mqttData.subs["base"])
        b.tpMqttPubPayload.setText(tile.mqttData.pubPayload)
        tile.mqttData.payloadIsJson.let {
            b.tpMqttJsonSwitch.isChecked = it
            b.tpMqttJsonPayload.visibility = if(it) VISIBLE else GONE
        }
        val t = tile.mqttData.jsonPaths["path"] ?: ""
        b.tpMqttJsonPayloadPath.setText("")
        b.tpMqttConfirmSwitch.isChecked = tile.mqttData.confirmPub
        b.tpQos.check(
            when (tile.mqttData.qos) {
                0 -> R.id.tp_qos0
                1 -> R.id.tp_qos1
                2 -> R.id.tp_qos2
                else -> R.id.tp_qos1
            }
        )

        mqttSwitchOnCheckedChangeListener(b.tpMqttSwitch.isChecked)

        when (tile) {
            is SliderTile -> {
                b.tpSlider.visibility = View.VISIBLE

                b.tpSliderFrom.setText((tile as SliderTile).from.toString())
                b.tpSliderTo.setText((tile as SliderTile).to.toString())
                b.tpSliderStep.setText((tile as SliderTile).step.toString())
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

    private fun mqttSwitchOnCheckedChangeListener(state: Boolean) {
        b.tpMqtt.visibility = if (state) {
            View.VISIBLE
        } else {
            View.GONE
        }

        tile.mqttData.isEnabled = state
    }
}