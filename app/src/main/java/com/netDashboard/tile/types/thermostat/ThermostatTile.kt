package com.netDashboard.tile.types.thermostat

import android.app.Dialog
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.databinding.DialogThermostatBinding
import com.netDashboard.dialogSetup
import com.netDashboard.globals.G
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.tile.Tile
import me.tankery.lib.circularseekbar.CircularSeekBar
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.math.abs
import kotlin.math.roundToInt

class ThermostatTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_thermostat

    @JsonIgnore
    override var typeTag = "thermostat"

    override var iconKey = "il_weather_temperature_half"

    var humi = 0f
    var humiSetpoint = 0f
    var temp = 0f
    var tempSetpoint = 0f
    var mode = "auto"

    var humiditySetpoint = false
    var showPayload = false
    val modes = mutableListOf("Auto" to "0", "Heat" to "1", "Cool" to "2", "Off" to "3")
    var humidityStep = 5f
    var temperatureRange = mutableListOf(15f, 30f, .5f)

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        if (tag.isBlank()) holder.itemView.findViewById<TextView>(R.id.t_tag)?.visibility =
            View.GONE
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        if (jsonResult.isEmpty()) {
            val value = data.second.toString().toFloatOrNull()
            when (data.first) {
                mqttData.subs["temp"] -> value?.let { temp = it }
                mqttData.subs["temp_set"] -> value?.let { tempSetpoint = it }
                mqttData.subs["humi"] -> value?.let { humi = it }
                mqttData.subs["humi_set"] -> value?.let { humiSetpoint = it }
                mqttData.subs["mode"] -> value?.let {
                    (modes.find { it.second == data.second.toString() })?.let {
                        mode = it.second
                    }
                }
            }
        } else {
            for (e in jsonResult) {
                val value = e.value.toFloatOrNull()
                when (e.key) {
                    "temp" -> value?.let { temp = it }
                    "temp_set" -> value?.let { tempSetpoint = it }
                    "humi" -> value?.let { humi = it }
                    "humi_set" -> value?.let { humiSetpoint = it }
                    "mode" -> value?.let {
                        (modes.find { it.second == data.second.toString() })?.let {
                            mode = it.second
                        }
                    }
                }
            }
        }
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        //if (mqttData.pubs["base"].isNullOrEmpty()) return
        //if (dashboard.dg?.mqttd?.client?.isConnected != true) return

        val dialog = Dialog(adapter.context)
        dialog.setContentView(R.layout.dialog_thermostat)
        val binding = DialogThermostatBinding.bind(dialog.findViewById(R.id.root))

        binding.ptTemp.setOnSeekBarChangeListener(object :
            CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(
                circularSeekBar: CircularSeekBar?,
                progress: Float,
                fromUser: Boolean
            ) {
                temp = temperatureRange[0] + progress.roundToInt() * temperatureRange[2]
                binding.ptValue.text = "$temp°C"
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {}
            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {}
        })

        binding.ptHumi.setOnSeekBarChangeListener(object :
            CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(
                circularSeekBar: CircularSeekBar?,
                progress: Float,
                fromUser: Boolean
            ) {
                humi = progress.roundToInt() * humidityStep
                binding.ptValue.text = "$humi%"
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {}
            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {}
        })

        abs((temperatureRange[1] - temperatureRange[0]) / temperatureRange[2]).let {
            binding.ptTemp.max = it
            binding.ptTemp.progress = (temp - temperatureRange[0]) / temperatureRange[2]
        }

        abs(100 / humidityStep).let {
            binding.ptHumi.max = it
            binding.ptHumi.progress = humi / humidityStep
        }

        dialog.dialogSetup()
        G.theme.apply(binding.root, anim = false)
        dialog.show()
    }
}