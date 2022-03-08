package com.netDashboard.tile.types.thermostat

import android.app.Dialog
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.View.INVISIBLE
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.*
import com.netDashboard.activities.MainActivity
import com.netDashboard.databinding.DialogConfirmBinding
import com.netDashboard.databinding.DialogSelectBinding
import com.netDashboard.databinding.DialogThermostatBinding
import com.netDashboard.globals.G
import com.netDashboard.recycler_view.GenericAdapter
import com.netDashboard.recycler_view.GenericItem
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
            if (tempSetpointTmp != tempSetpoint) binding.dtTempSetpoint.blink(-1, 200, 0, .4f)
            else binding.dtTempSetpoint.clearAnimation()

            if (humiSetpointTmp != humiSetpoint) binding.dtHumiSetpoint.blink(-1, 200, 0, .4f)
            else binding.dtHumiSetpoint.clearAnimation()
        }

        var observer: (String) -> Unit = {
            when (it) {
                "temp" -> {
                    temp?.let {
                        binding.dtTempCurrent.text = "${it.round(3)}°C"
                    }
                }
                "humi" -> {
                    humi?.let {
                        binding.dtHumiCurrent.text = "${it.round(3)}%"
                    }
                }
                "temp_set" -> {
                    tempSetpoint?.let {
                        binding.dtTemp.progress = (it - temperatureRange[0]) / temperatureRange[2]
                    }
                }
                "humi_set" -> {
                    if (includeHumiditySetpoint) {
                        humiSetpoint?.let {
                            binding.dtHumi.progress = it / humidityStep
                        }
                    }
                }
            }
        }

        dialog.setOnDismissListener {
            hasReceived.removeObserver(observer)
        }

        hasReceived.observe(adapter.context as LifecycleOwner, observer)

        binding.dtTemp.setOnSeekBarChangeListener(object :
            CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(
                circularSeekBar: CircularSeekBar?,
                progress: Float,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    tempSetpointTmp =
                        temperatureRange[0] + progress.roundToInt() * temperatureRange[2]
                    binding.dtValue.text = "${tempSetpointTmp!!.round(3)}°C"
                    binding.dtTempSetpoint.text = "${tempSetpointTmp!!.round(3)}°C"
                    valueBlinking()
                }
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {}
            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {}
        })

        binding.dtHumi.setOnSeekBarChangeListener(object :
            CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(
                circularSeekBar: CircularSeekBar?,
                progress: Float,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    humiSetpointTmp = progress.roundToInt() * humidityStep
                    binding.dtValue.text = "${humiSetpointTmp!!.round(3)}%"
                    binding.dtHumiSetpoint.text = "${humiSetpointTmp!!.round(3)}%"
                    valueBlinking()
                }
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {}
            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {}
        })

        if (!includeHumiditySetpoint) {
            binding.dtHumiCurrent.text = "--%"
            binding.dtHumi.visibility = INVISIBLE
        } else {
            abs(100 / humidityStep).let {
                binding.dtHumi.max = it
            }
            humiSetpointTmp?.let {
                binding.dtHumi.progress = it / humidityStep
            }

            humi?.let { binding.dtHumiCurrent.text = "${it.round(3)}%" }
            humiSetpointTmp?.let { binding.dtHumiSetpoint.text = "${it.round(3)}%" }
        }

        abs((temperatureRange[1] - temperatureRange[0]) / temperatureRange[2]).let {
            binding.dtTemp.max = it
        }
        tempSetpointTmp?.let {
            binding.dtTemp.progress = (it - temperatureRange[0]) / temperatureRange[2]
        }

        tempSetpointTmp?.let { binding.dtValue.text = "${it.round(3)}°C" }
        tempSetpointTmp?.let { binding.dtTempSetpoint.text = "${it.round(3)}°C" }
        temp?.let { binding.dtTempCurrent.text = "${it.round(3)}°C" }

        binding.dtConfirm.setOnClickListener {
            val dialogConfirm = Dialog(adapter.context)

            dialogConfirm.setContentView(R.layout.dialog_confirm)
            val binding = DialogConfirmBinding.bind(dialogConfirm.findViewById(R.id.root))

            binding.dcConfirm.setOnClickListener {
                send(
                    mqttData.pubs["temp_set"],
                    "$tempSetpointTmp",
                    mqttData.qos,
                    false,
                    true
                )
                send(
                    mqttData.pubs["humi_set"],
                    "$humiSetpointTmp",
                    mqttData.qos,
                    false,
                    true
                )
                dialogConfirm.dismiss()
            }

            binding.dcDeny.setOnClickListener {
                dialogConfirm.dismiss()
            }

            binding.dcConfirm.text = "PUBLISH"
            binding.dcText.text = "Confirm publishing"

            dialogConfirm.dialogSetup()
            G.theme.apply(binding.root)
            dialogConfirm.show()

            dialog.dismiss()
        }

        binding.dtDeny.setOnClickListener {
            dialog.dismiss()
        }

        binding.dtMode.setOnClickListener {
            val notEmpty = modes.filter { !(it.first.isEmpty() && it.second.isEmpty()) }
            if (notEmpty.size > 0) {
                val dialog = Dialog(adapter.context)
                val adapter = GenericAdapter(adapter.context)

                dialog.setContentView(R.layout.dialog_select)
                val binding = DialogSelectBinding.bind(dialog.findViewById(R.id.root))

                adapter.onBindViewHolder = { _, holder, pos ->
                    val text = holder.itemView.findViewById<TextView>(R.id.is_text)
                    text.text = if (showPayload) "${notEmpty[pos].first} (${notEmpty[pos].second})"
                    else "${notEmpty[pos].first}"

                    if(mode == notEmpty[pos].second) text.text = "${text.text} (S)"
                }

                adapter.onItemClick = {
                    val pos = adapter.list.indexOf(it)

                    send(
                        mqttData.pubs["mode"],
                        "${this.modes[pos].second}",
                        mqttData.qos,
                        false
                    )

                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                    }, 50)
                }

                adapter.setHasStableIds(true)
                adapter.submitList(MutableList(notEmpty.size) { GenericItem(R.layout.item_select) })

                binding.dsRecyclerView.layoutManager = LinearLayoutManager(adapter.context)
                binding.dsRecyclerView.adapter = adapter

                dialog.dialogSetup()
                G.theme.apply(binding.root)
                dialog.show()
            } else createToast(adapter.context, "Add modes first")
        }

        dialog.dialogSetup()
        G.theme.apply(binding.root, anim = false)
        dialog.show()
    }
}