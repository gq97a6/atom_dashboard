package com.netDashboard.tile.types.thermostat

import android.app.Dialog
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.blink
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
    var hasReceived = MutableLiveData("")

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
                mqttData.subs["temp"] -> value?.let {
                    temp = it
                    this.hasReceived.postValue("temp")
                }
                mqttData.subs["temp_set"] -> value?.let {
                    tempSetpoint = it
                    this.hasReceived.postValue("temp_set")
                }
                mqttData.subs["humi"] -> value?.let {
                    humi = it
                    this.hasReceived.postValue("humi")
                }
                mqttData.subs["humi_set"] -> value?.let {
                    humiSetpoint = it
                    this.hasReceived.postValue("humi_set")
                }
                mqttData.subs["mode"] -> value?.let {
                    (modes.find { it.second == data.second.toString() })?.let {
                        mode = it.second
                        this.hasReceived.postValue("mode")
                    }
                }
            }
        } else {
            for (e in jsonResult) {
                val value = e.value.toFloatOrNull()
                var hasReceived = true
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
                    else -> hasReceived = false
                }
                if (hasReceived) this.hasReceived.postValue(e.key)
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

        var tempSetpointTmp = tempSetpoint
        var humiSetpointTmp = humiSetpoint

        fun valueBlinking() {
            if (tempSetpointTmp != tempSetpoint) binding.ptTempSetpoint.blink(-1, 200, 0, .4f)
            else binding.ptTempSetpoint.clearAnimation()

            if (humiSetpointTmp != humiSetpoint) binding.ptHumiSetpoint.blink(-1, 200, 0, .4f)
            else binding.ptHumiSetpoint.clearAnimation()
        }

        var observer: (String) -> Unit = {
            when (it) {
                "temp" -> binding.ptTempCurrent.text = "$temp°C"
                "humi" -> binding.ptHumiCurrent.text = "$humi%"
                "temp_set" -> {
                    abs((temperatureRange[1] - temperatureRange[0]) / temperatureRange[2]).let {
                        binding.ptTemp.max = it
                        binding.ptTemp.progress =
                            (tempSetpoint - temperatureRange[0]) / temperatureRange[2]
                    }
                }
                "humi_set" -> {
                    abs(100 / humidityStep).let {
                        binding.ptHumi.max = it
                        binding.ptHumi.progress = humiSetpoint / humidityStep
                    }
                }
            }
        }

        dialog.setOnDismissListener {
            hasReceived.removeObserver(observer)
        }

        hasReceived.observe(adapter.context as LifecycleOwner, observer)

        binding.ptTemp.setOnSeekBarChangeListener(object :
            CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(
                circularSeekBar: CircularSeekBar?,
                progress: Float,
                fromUser: Boolean
            ) {
                tempSetpointTmp = temperatureRange[0] + progress.roundToInt() * temperatureRange[2]
                binding.ptValue.text = "$tempSetpointTmp°C"
                binding.ptTempSetpoint.text = "$tempSetpointTmp°C"
                valueBlinking()
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
                humiSetpointTmp = progress.roundToInt() * humidityStep
                binding.ptValue.text = "$humiSetpointTmp%"
                binding.ptHumiSetpoint.text = "$humiSetpointTmp%"
                valueBlinking()
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {}
            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {}
        })

        abs(100 / humidityStep).let {
            binding.ptHumi.max = it
            binding.ptHumi.progress = humiSetpointTmp / humidityStep
        }

        abs((temperatureRange[1] - temperatureRange[0]) / temperatureRange[2]).let {
            binding.ptTemp.max = it
            binding.ptTemp.progress =
                (tempSetpointTmp - temperatureRange[0]) / temperatureRange[2]
        }

        binding.ptValue.text = "$tempSetpointTmp°C"
        binding.ptTempSetpoint.text = "$tempSetpointTmp°C"
        binding.ptHumiSetpoint.text = "$humiSetpointTmp%"

        binding.ptConfirm.setOnClickListener {
            send(
                mqttData.pubs["temp_set"],
                "$tempSetpointTmp",
                mqttData.qos,
                false,
                mqttData.confirmPub
            )
            send(
                mqttData.pubs["humi_set"],
                "$humiSetpointTmp",
                mqttData.qos,
                false,
                mqttData.confirmPub
            )
            dialog.dismiss()
        }

        binding.ptDeny.setOnClickListener {
            dialog.dismiss()
        }

        dialog.dialogSetup()
        G.theme.apply(binding.root, anim = false)
        dialog.show()
    }
}