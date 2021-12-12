package com.netDashboard.activities.dashboard.tile_properties

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.netDashboard.digitsOnly
import com.netDashboard.globals.G
import com.netDashboard.globals.G.dashboards
import com.netDashboard.tile.Tile
import com.netDashboard.tile.types.slider.SliderTile
import com.netDashboard.toPx

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
        G.theme.apply(this, b.root)
        viewConfig()
        setContentView(b.root)

        b.tpTag.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                var raw = cs.toString()

                val paint = Paint()
                paint.typeface = b.tpTag.typeface
                paint.textSize = b.tpTag.textSize

                val bound = 143f.toPx()
                while (paint.measureText(raw, 0, raw.length) > bound) raw = raw.dropLast(1)

                tile.tag = raw
                b.tpTagWarning.visibility = if (raw.length != cs.length) VISIBLE else GONE
            }
        })

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
            b.tpMqttJsonPayload.visibility = if (state) VISIBLE else GONE
        }

        b.tpMqttJsonPayloadPath.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.mqttData.jsonPaths["value"] = cs.toString()
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
                    cs.toString().let { raw ->
                        cs.toString().digitsOnly().let { parsed ->
                            if (raw != parsed) b.tpSliderFrom.setText(parsed)
                            else parsed.toIntOrNull()?.let {
                                (tile as SliderTile).from = it
                            }
                        }
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
                    cs.toString().let { raw ->
                        cs.toString().digitsOnly().let { parsed ->
                            if (raw != parsed) b.tpSliderTo.setText(parsed)
                            else parsed.toIntOrNull()?.let {
                                (tile as SliderTile).to = it
                            }
                        }
                    }
                }
            })

            b.tpSliderStep.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(cs: Editable) {
                }

                override fun beforeTextChanged(
                    cs: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                    cs.toString().let { raw ->
                        cs.toString().digitsOnly().let { parsed ->
                            if (raw != parsed) b.tpSliderStep.setText(parsed)
                            else parsed.toIntOrNull()?.let {
                                (tile as SliderTile).step = it
                            }
                        }
                    }
                }
            })

            b.tpSliderDrag.setOnCheckedChangeListener { _, state ->
                (tile as SliderTile).dragCon = state
            }
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
        b.tpTag.setText(tile.tag)

        //MQTT
        b.tpMqttSwitch.isChecked = tile.mqttData.isEnabled
        b.tpMqttPub.setText(tile.mqttData.pubs["base"])
        b.tpMqttSub.setText(tile.mqttData.subs["base"])
        b.tpMqttPubPayload.setText(tile.mqttData.pubPayload)
        tile.mqttData.payloadIsJson.let {
            b.tpMqttJsonSwitch.isChecked = it
            b.tpMqttJsonPayload.visibility = if (it) VISIBLE else GONE
        }
        b.tpMqttJsonPayloadPath.setText(tile.mqttData.jsonPaths["value"] ?: "")
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
                b.tpSliderDrag.isChecked = (tile as SliderTile).dragCon
                b.tpSliderFrom.setText((tile as SliderTile).from.toString())
                b.tpSliderTo.setText((tile as SliderTile).to.toString())
                b.tpSliderStep.setText((tile as SliderTile).step.toString())
            }
            else -> b.tpSlider.visibility = GONE
        }
    }

    private fun mqttSwitchOnCheckedChangeListener(state: Boolean) {
        b.tpMqtt.visibility = if (state) {
            VISIBLE
        } else {
            GONE
        }

        tile.mqttData.isEnabled = state
    }
}