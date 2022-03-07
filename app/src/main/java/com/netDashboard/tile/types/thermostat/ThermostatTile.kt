package com.netDashboard.tile.types.thermostat

import android.app.Dialog
import android.view.MotionEvent
import android.view.View
import android.view.View.INVISIBLE
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.*
import com.netDashboard.databinding.DialogThermostatBinding
import com.netDashboard.globals.G
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.tile.Tile
import me.tankery.lib.circularseekbar.CircularSeekBar
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt

class ThermostatTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_thermostat

    @JsonIgnore
    override var typeTag = "thermostat"

    override var iconKey = "il_weather_temperature_half"

    var hasReceived = MutableLiveData("")

    var mode = "auto"
    var humi: Float? = null
    var temp: Float? = null
    var humiSetpoint: Float? = null
    var tempSetpoint: Float? = null

    var humidityStep = 5f
    var temperatureRange = mutableListOf(15f, 30f, .5f)
    val modes = mutableListOf("Auto" to "0", "Heat" to "1", "Cool" to "2", "Off" to "3")

    var includeHumiditySetpoint = false
    var showPayload = false

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

        if (temperatureRange[0] > temperatureRange[1]) {
            val tmp = temperatureRange[0]
            temperatureRange[0] = temperatureRange[1]
            temperatureRange[1] = tmp
        }
        tempSetpoint?.let {
            if (it !in temperatureRange[0]..temperatureRange[1]) {
                tempSetpoint = temperatureRange[0]
            }
        }

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
                "temp" -> {
                    temp?.let {
                        binding.ptTempCurrent.text = "${it.round(3)}°C"
                    }
                }
                "humi" -> {
                    humi?.let {
                        binding.ptHumiCurrent.text = "${it.round(3)}%"
                    }
                }
                "temp_set" -> {
                    tempSetpoint?.let {
                        binding.ptTemp.progress = (it - temperatureRange[0]) / temperatureRange[2]
                    }
                }
                "humi_set" -> {
                    if (includeHumiditySetpoint) {
                        humiSetpoint?.let {
                            binding.ptHumi.progress = it / humidityStep
                        }
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
                if (fromUser) {
                    tempSetpointTmp =
                        temperatureRange[0] + progress.roundToInt() * temperatureRange[2]
                    binding.ptValue.text = "${tempSetpointTmp!!.round(3)}°C"
                    binding.ptTempSetpoint.text = "${tempSetpointTmp!!.round(3)}°C"
                    valueBlinking()
                }
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
                if (fromUser) {
                    humiSetpointTmp = progress.roundToInt() * humidityStep
                    binding.ptValue.text = "${humiSetpointTmp!!.round(3)}%"
                    binding.ptHumiSetpoint.text = "${humiSetpointTmp!!.round(3)}%"
                    valueBlinking()
                }
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {}
            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {}
        })

        if (!includeHumiditySetpoint) {
            binding.ptHumiCurrent.text = "--%"
            binding.ptHumi.visibility = INVISIBLE
        } else {
            abs(100 / humidityStep).let {
                binding.ptHumi.max = it
            }
            humiSetpointTmp?.let {
                binding.ptHumi.progress = it / humidityStep
            }

            humi?.let { binding.ptHumiCurrent.text = "${it.round(3)}%" }
            humiSetpointTmp?.let { binding.ptHumiSetpoint.text = "${it.round(3)}%" }
        }

        abs((temperatureRange[1] - temperatureRange[0]) / temperatureRange[2]).let {
            binding.ptTemp.max = it
        }
        tempSetpointTmp?.let {
            binding.ptTemp.progress = (it - temperatureRange[0]) / temperatureRange[2]
        }

        tempSetpointTmp?.let { binding.ptValue.text = "${it.round(3)}°C" }
        tempSetpointTmp?.let { binding.ptTempSetpoint.text = "${it.round(3)}°C" }
        temp?.let { binding.ptTempCurrent.text = "${it.round(3)}°C" }

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