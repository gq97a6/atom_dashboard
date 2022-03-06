package com.netDashboard.activities.fragments

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.netDashboard.R
import com.netDashboard.activities.MainActivity
import com.netDashboard.databinding.FragmentTilePropertiesBinding
import com.netDashboard.digitsOnly
import com.netDashboard.globals.G
import com.netDashboard.globals.G.dashboard
import com.netDashboard.globals.G.getIconColorPallet
import com.netDashboard.globals.G.getIconHSV
import com.netDashboard.globals.G.getIconRes
import com.netDashboard.globals.G.setIconHSV
import com.netDashboard.globals.G.setIconKey
import com.netDashboard.globals.G.settings
import com.netDashboard.globals.G.tile
import com.netDashboard.recycler_view.GenericAdapter
import com.netDashboard.recycler_view.GenericItem
import com.netDashboard.tile.types.button.TextTile
import com.netDashboard.tile.types.color.ColorTile
import com.netDashboard.tile.types.lights.LightsTile
import com.netDashboard.tile.types.pick.SelectTile
import com.netDashboard.tile.types.slider.SliderTile
import com.netDashboard.tile.types.switch.SwitchTile
import com.netDashboard.tile.types.terminal.TerminalTile
import com.netDashboard.tile.types.thermostat.ThermostatTile
import com.netDashboard.tile.types.time.TimeTile
import java.util.*

class TilePropertiesFragment : Fragment(R.layout.fragment_tile_properties) {
    private lateinit var b: FragmentTilePropertiesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentTilePropertiesBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        G.theme.apply(b.root, requireActivity())

        b.tpTag.setText(tile.tag)
        b.tpIcon.setBackgroundResource(tile.iconRes)
        b.tpTileType.text = tile.typeTag.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        b.tpIcon.backgroundTintList = ColorStateList.valueOf(tile.colorPallet.color)
        val drawable = b.tpIconFrame.background as? GradientDrawable
        drawable?.mutate()
        drawable?.setStroke(1, tile.colorPallet.color)
        drawable?.cornerRadius = 15f

        b.tpMqttSwitch.isChecked = tile.mqttData.isEnabled
        b.tpMqttPub.setText(tile.mqttData.pubs["base"])
        b.tpMqttSub.setText(tile.mqttData.subs["base"])
        b.tpMqttPayload.setText(tile.mqttData.payloads["base"] ?: "")

        tile.mqttData.payloadIsJson.let {
            b.tpMqttJsonSwitch.isChecked = it
            b.tpMqttJsonPayload.visibility = if (it) VISIBLE else GONE
        }

        b.tpMqttJsonPayloadPath.setText(tile.mqttData.jsonPaths["base"] ?: "")
        b.tpMqttConfirmSwitch.isChecked = tile.mqttData.confirmPub
        b.tpQos.check(
            when (tile.mqttData.qos) {
                0 -> R.id.tp_qos0
                1 -> R.id.tp_qos1
                2 -> R.id.tp_qos2
                else -> R.id.tp_qos1
            }
        )

        switchMqttTab(settings.mqttTabShow, 0)

        b.tpNotSilentSwitch.isChecked = tile.doLog
        b.tpNotSwitch.isChecked = tile.doNotify
        b.tpNotSilentSwitch.isChecked = tile.silentNotify

        b.tpTag.addTextChangedListener {
            tile.tag = (it ?: "").toString()
        }

        b.tpMqttSwitch.setOnCheckedChangeListener { _, state ->
            mqttSwitchOnCheckedChangeListener(state)
        }

        b.tpMqttArrow.setOnClickListener {
            switchMqttTab(!b.tpMqtt.isVisible)
        }

        b.tpMqttPub.addTextChangedListener {
            tile.mqttData.pubs["base"] = (it ?: "").toString()
            dashboard.dg?.mqttd?.notifyOptionsChanged()
        }

        b.tpMqttSub.addTextChangedListener {
            tile.mqttData.subs["base"] = (it ?: "").toString()
            dashboard.dg?.mqttd?.notifyOptionsChanged()
        }

        b.tpMqttPubCopy.setOnClickListener {
            b.tpMqttPub.setText(b.tpMqttSub.text)
        }

        b.tpMqttJsonSwitch.setOnCheckedChangeListener { _, state ->
            tile.mqttData.payloadIsJson = state
            b.tpMqttJsonPayload.visibility = if (state) VISIBLE else GONE
        }

        b.tpMqttJsonPayloadPath.addTextChangedListener {
            tile.mqttData.jsonPaths["base"] = (it ?: "").toString()
        }

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
            dashboard.dg?.mqttd?.notifyOptionsChanged()
        }

        b.tpEditIcon.setOnClickListener {
            getIconHSV = { tile.hsv }
            getIconRes = { tile.iconRes }
            getIconColorPallet = { tile.colorPallet }

            setIconHSV = { hsv -> tile.hsv = hsv }
            setIconKey = { key -> tile.iconKey = key }

            (activity as MainActivity).fm.replaceWith(TileIconFragment())
        }

        b.tpLogSwitch.setOnCheckedChangeListener { _, state ->
            tile.doLog = state
        }

        b.tpNotSwitch.setOnCheckedChangeListener { _, state ->
            tile.doNotify = state
            b.tpNotSilent.visibility = if (state) VISIBLE else GONE
        }

        b.tpNotSilentSwitch.setOnCheckedChangeListener { _, state ->
            tile.silentNotify = state
        }

        when (tile) {
            is TextTile -> {
                b.tpMqttPayloadTypeBox.visibility = VISIBLE
                b.tpPayloadType.check(
                    if (tile.mqttData.varPayload) R.id.tp_mqtt_payload_var
                    else {
                        b.tpMqttPayload.visibility = VISIBLE
                        R.id.tp_mqtt_payload_val
                    }
                )
                b.tpPayloadType.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
                    tile.mqttData.varPayload = when (id) {
                        R.id.tp_mqtt_payload_val -> {
                            b.tpMqttPayloadBox.visibility = VISIBLE
                            false
                        }
                        R.id.tp_mqtt_payload_var -> {
                            b.tpMqttPayloadBox.visibility = GONE
                            true
                        }
                        else -> true
                    }
                }

                b.tpMqttJsonPayloadPath.addTextChangedListener {
                    tile.mqttData.jsonPaths["base"] = (it ?: "").toString()
                }

                b.tpMqttPayload.addTextChangedListener {
                    tile.mqttData.payloads["base"] = (it ?: "").toString()
                }
            }
//--------------------------------------------------------------------------------------------------
            is SliderTile -> {
                val tile = tile as SliderTile

                b.tpSlider.visibility = VISIBLE
                b.tpMqttPayloadBox.visibility = VISIBLE
                b.tpPayloadHint.visibility = VISIBLE

                b.tpSliderDrag.isChecked = tile.dragCon

                b.tpPayloadHint.text = "Use @value to insert current value"
                b.tpSliderFrom.setText(tile.range[0].toString())
                b.tpSliderTo.setText(tile.range[1].toString())
                b.tpSliderStep.setText(tile.range[2].toString())

                b.tpSliderFrom.addTextChangedListener {
                    (it ?: "").toString().let { raw ->
                        (it ?: "").toString().digitsOnly().let { parsed ->
                            if (raw != parsed) b.tpSliderFrom.setText(parsed)
                            else parsed.toIntOrNull()?.let {
                                tile.range[0] = it
                            }
                        }
                    }
                }

                b.tpSliderTo.addTextChangedListener {
                    (it ?: "").toString().let { raw ->
                        (it ?: "").toString().digitsOnly().let { parsed ->
                            if (raw != parsed) b.tpSliderTo.setText(parsed)
                            else parsed.toIntOrNull()?.let {
                                tile.range[2] = it
                            }
                        }
                    }
                }

                b.tpSliderStep.addTextChangedListener {
                    (it ?: "").toString().let { raw ->
                        (it ?: "").toString().digitsOnly().let { parsed ->
                            if (raw != parsed) b.tpSliderStep.setText(parsed)
                            else parsed.toIntOrNull()?.let {
                                tile.range[2] = it
                            }
                        }
                    }
                }

                b.tpSliderDrag.setOnCheckedChangeListener { _, state ->
                    tile.dragCon = state
                }

                b.tpMqttPayload.addTextChangedListener {
                    tile.mqttData.payloads["base"] = (it ?: "").toString()
                }
            }
//--------------------------------------------------------------------------------------------------
            is SwitchTile -> {
                val tile = tile as SwitchTile

                b.tpMqttPayloadsBox.visibility = VISIBLE

                b.tpMqttPayloadTrue.setText(tile.mqttData.payloads["true"] ?: "")
                b.tpMqttPayloadTrueIcon.setBackgroundResource(tile.iconResTrue)
                b.tpMqttPayloadTrueIcon.backgroundTintList =
                    ColorStateList.valueOf(tile.colorPalletTrue.color)

                val drawableTrue = b.tpMqttPayloadTrueIconFrame.background as? GradientDrawable
                drawableTrue?.mutate()
                drawableTrue?.setStroke(1, tile.colorPalletTrue.color)
                drawableTrue?.cornerRadius = 15f


                b.tpMqttPayloadFalse.setText(tile.mqttData.payloads["false"] ?: "")
                b.tpMqttPayloadFalseIcon.setBackgroundResource(tile.iconResFalse)
                b.tpMqttPayloadFalseIcon.backgroundTintList =
                    ColorStateList.valueOf(tile.colorPalletFalse.color)

                val drawableFalse = b.tpMqttPayloadFalseIconFrame.background as? GradientDrawable
                drawableFalse?.mutate()
                drawableFalse?.setStroke(1, tile.colorPalletFalse.color)
                drawableFalse?.cornerRadius = 15f

                b.tpMqttPayloadTrueEditIcon.setOnClickListener {
                    getIconHSV = { tile.hsvTrue }
                    getIconRes = { tile.iconResTrue }
                    getIconColorPallet = { tile.colorPalletTrue }

                    setIconHSV = { hsv -> tile.hsvTrue = hsv }
                    setIconKey = { key -> tile.iconKeyTrue = key }

                    (activity as MainActivity).fm.replaceWith(TileIconFragment())
                }

                b.tpMqttPayloadFalseEditIcon.setOnClickListener {
                    getIconHSV = { tile.hsvFalse }
                    getIconRes = { tile.iconResFalse }
                    getIconColorPallet = { tile.colorPalletFalse }

                    setIconHSV = { hsv -> tile.hsvFalse = hsv }
                    setIconKey = { key -> tile.iconKeyFalse = key }

                    (activity as MainActivity).fm.replaceWith(TileIconFragment())
                }

                b.tpMqttPayloadTrue.addTextChangedListener {
                    tile.mqttData.payloads["true"] = (it ?: "").toString()
                }

                b.tpMqttPayloadFalse.addTextChangedListener {
                    tile.mqttData.payloads["false"] = (it ?: "").toString()
                }
            }
//--------------------------------------------------------------------------------------------------
            is SelectTile -> {
                val tile = tile as SelectTile

                b.tpSelect.visibility = VISIBLE
                b.tpSelectShowPayload.isChecked = tile.showPayload

                val adapter = GenericAdapter(requireContext())

                val list = MutableList(tile.options.size) {
                    GenericItem(R.layout.item_option)
                }

                adapter.setHasStableIds(true)
                adapter.onBindViewHolder = { item, holder, _ ->
                    adapter.list.indexOf(item).let { pos ->
                        val a = holder.itemView.findViewById<EditText>(R.id.io_alias)
                        val p = holder.itemView.findViewById<EditText>(R.id.io_payload)
                        val r = holder.itemView.findViewById<Button>(R.id.io_remove)

                        a.setText(tile.options[pos].first)
                        p.setText(tile.options[pos].second)

                        a.addTextChangedListener {
                            adapter.list.indexOf(item).let { pos ->
                                if (pos >= 0)
                                    tile.options[pos] =
                                        Pair((it ?: "").toString(), tile.options[pos].second)
                            }
                        }

                        p.addTextChangedListener {
                            adapter.list.indexOf(item).let { pos ->
                                if (pos >= 0)
                                    tile.options[pos] =
                                        Pair(tile.options[pos].first, (it ?: "").toString())
                            }
                        }

                        r.setOnClickListener {
                            if (list.size > 1) {
                                adapter.list.indexOf(item).let {
                                    if (it >= 0) {
                                        tile.options.removeAt(it)
                                        adapter.removeItemAt(it)
                                    }
                                }
                            }
                        }
                    }
                }

                b.tpSelectRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                b.tpSelectRecyclerView.adapter = adapter

                adapter.submitList(list)

                b.tpSelectAdd.setOnClickListener {
                    tile.options.add(Pair("", ""))
                    list.add(GenericItem(R.layout.item_option))
                    adapter.notifyItemInserted(list.size - 1)
                }

                b.tpSelectShowPayload.setOnCheckedChangeListener { _, state ->
                    tile.showPayload = state
                }
            }
//--------------------------------------------------------------------------------------------------
            is TerminalTile -> {
                b.tpMqttPayloadTypeBox.visibility = VISIBLE
                b.tpPayloadType.check(
                    if (tile.mqttData.varPayload) R.id.tp_mqtt_payload_var
                    else {
                        b.tpMqttPayload.visibility = VISIBLE
                        R.id.tp_mqtt_payload_val
                    }
                )

                b.tpPayloadType.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
                    tile.mqttData.varPayload = when (id) {
                        R.id.tp_mqtt_payload_val -> {
                            b.tpMqttPayloadBox.visibility = VISIBLE
                            false
                        }
                        R.id.tp_mqtt_payload_var -> {
                            b.tpMqttPayloadBox.visibility = GONE
                            true
                        }
                        else -> true
                    }
                }

                b.tpMqttPayload.addTextChangedListener {
                    tile.mqttData.payloads["base"] = (it ?: "").toString()
                }
            }
//--------------------------------------------------------------------------------------------------
            is TimeTile -> {
                val tile = tile as TimeTile

                b.tpTime.visibility = VISIBLE
                b.tpMqttPayloadBox.visibility = VISIBLE
                b.tpPayloadHint.visibility = VISIBLE

                b.tpTimeType.check(
                    when (tile.isDate) {
                        false -> {
                            b.tpMqttPayload.setText(tile.mqttData.payloads["time"])
                            b.tpPayloadHint.text = "Use @hour and @minute to insert current values."
                            R.id.tp_time_time
                        }
                        true -> {
                            b.tpMqttPayload.setText(tile.mqttData.payloads["date"])
                            b.tpPayloadHint.text =
                                "Use @day, @month, @year to insert current values."
                            R.id.tp_time_date
                        }
                    }
                )

                if (!tile.isDate) {
                    b.tpTimeMilitaryBox.visibility = VISIBLE
                    b.tpTimeMilitary.isChecked = tile.isMilitary
                }

                b.tpTimeType.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
                    tile.isDate = when (id) {
                        R.id.tp_time_time -> false
                        R.id.tp_time_date -> true
                        else -> false
                    }

                    b.tpTimeMilitaryBox.visibility = if (tile.isDate) GONE else VISIBLE
                    b.tpMqttPayload.setText(tile.mqttData.payloads[if (tile.isDate) "date" else "time"])
                    b.tpPayloadHint.text =
                        "Use ${if (tile.isDate) "@day, @month, @year" else "@hour and @minute"} to insert current values."
                }

                if (!tile.isDate) {
                    b.tpTimeMilitary.setOnCheckedChangeListener { _, state ->
                        tile.isMilitary = state
                    }
                }

                b.tpMqttPayload.addTextChangedListener {
                    tile.mqttData.payloads[if (tile.isDate) "date" else "time"] =
                        (it ?: "").toString()
                }
            }
//--------------------------------------------------------------------------------------------------
            is ColorTile -> {
                val tile = tile as ColorTile

                b.tpColor.visibility = VISIBLE
                b.tpMqttPayloadBox.visibility = VISIBLE
                b.tpPayloadHint.visibility = VISIBLE

                b.tpColorPaintRaw.isChecked = tile.paintRaw

                b.tpColorType.check(
                    when (tile.colorType) {
                        "hsv" -> R.id.tp_color_hsv
                        "hex" -> R.id.tp_color_hex
                        "rgb" -> R.id.tp_color_rgb
                        else -> R.id.tp_color_hsv
                    }
                )

                b.tpMqttPayload.setText(tile.mqttData.payloads[tile.colorType])
                b.tpPayloadHint.text =
                    "Use ${
                        when (tile.colorType) {
                            "hsv" -> "@h, @s, @v"
                            "hex" -> "@hex"
                            "rgb" -> "@r, @g, @b"
                            else -> "@hex"
                        }
                    } to insert current value."

                b.tpColorPaintRaw.setOnCheckedChangeListener { _, state ->
                    tile.paintRaw = state
                }

                b.tpColorType.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
                    tile.colorType = when (id) {
                        R.id.tp_color_hsv -> "hsv"
                        R.id.tp_color_hex -> "hex"
                        R.id.tp_color_rgb -> "rgb"
                        else -> "hex"
                    }

                    b.tpMqttPayload.setText(tile.mqttData.payloads[tile.colorType])
                    b.tpPayloadHint.text =
                        "Use ${
                            when (tile.colorType) {
                                "hsv" -> "@h, @s, @v"
                                "hex" -> "@hex"
                                "rgb" -> "@r, @g, @b"
                                else -> "@hex"
                            }
                        } to insert current value."
                }

                b.tpMqttPayload.addTextChangedListener {
                    tile.mqttData.payloads[tile.colorType] = (it ?: "").toString()
                }
            }
//--------------------------------------------------------------------------------------------------
            is ThermostatTile -> {
                val tile = tile as ThermostatTile

                b.tpMqttJsonSwitch.setOnCheckedChangeListener { _, state ->
                    tile.mqttData.payloadIsJson = state
                    b.tpThermostatPaths.visibility = if (state) VISIBLE else GONE
                }

                b.tpMqttTopics.visibility = GONE
                b.tpMqttJsonPayload.visibility = GONE
                b.tpThermostat.visibility = VISIBLE
                b.tpThermostatTopics.visibility = VISIBLE
                b.tpThermostatPaths.visibility =
                    if (tile.mqttData.payloadIsJson) VISIBLE else GONE

                b.tpThermostatTemperatureSub.setText(tile.mqttData.subs["temp"])
                b.tpThermostatTemperaturePub.setText(tile.mqttData.pubs["temp"])
                b.tpThermostatTemperatureSetpointSub.setText(tile.mqttData.subs["temp_set"])
                b.tpThermostatTemperatureSetpointPub.setText(tile.mqttData.pubs["temp_set"])
                b.tpThermostatHumiditySub.setText(tile.mqttData.subs["humi"])
                b.tpThermostatHumidityPub.setText(tile.mqttData.pubs["humi"])
                b.tpThermostatHumiditySetpointSub.setText(tile.mqttData.subs["humi_set"])
                b.tpThermostatHumiditySetpointPub.setText(tile.mqttData.pubs["humi_set"])
                b.tpThermostatModeSub.setText(tile.mqttData.subs["mode"])
                b.tpThermostatModePub.setText(tile.mqttData.pubs["mode"])

                b.tpThermostatTemperaturePath.setText(tile.mqttData.jsonPaths["temp"])
                b.tpThermostatTemperatureSetpointPath.setText(tile.mqttData.jsonPaths["temp_set"])
                b.tpThermostatHumidityPath.setText(tile.mqttData.jsonPaths["humi"])
                b.tpThermostatHumiditySetpointPath.setText(tile.mqttData.jsonPaths["humi_set"])
                b.tpThermostatModePath.setText(tile.mqttData.jsonPaths["mode"])

                b.tpThermostatHumidityStep.setText(tile.humidityStep.toString())
                b.tpThermostatTemperatureFrom.setText(tile.temperatureRange[0].toString())
                b.tpThermostatTemperatureTo.setText(tile.temperatureRange[1].toString())
                b.tpThermostatTemperatureStep.setText(tile.temperatureRange[2].toString())

                tile.humiditySetpoint.let {
                    b.tpThermostatHumidityTopicBox.visibility = if (it) VISIBLE else GONE
                    b.tpThermostatHumidityStepBox.visibility = if (it) VISIBLE else GONE
                    b.tpThermostatHumiditySetpoint.isChecked = it
                }

                b.tpThermostatShowPayload.isChecked = tile.showPayload

                b.tpThermostatTemperatureSub.addTextChangedListener {
                    tile.mqttData.subs["temp"] = (it ?: "").toString()
                }
                b.tpThermostatTemperaturePub.addTextChangedListener {
                    tile.mqttData.pubs["temp"] = (it ?: "").toString()
                }
                b.tpThermostatTemperatureSetpointSub.addTextChangedListener {
                    tile.mqttData.subs["temp_set"] = (it ?: "").toString()
                }
                b.tpThermostatTemperatureSetpointPub.addTextChangedListener {
                    tile.mqttData.pubs["temp_set"] = (it ?: "").toString()
                }
                b.tpThermostatHumiditySub.addTextChangedListener {
                    tile.mqttData.subs["humi"] = (it ?: "").toString()
                }
                b.tpThermostatHumidityPub.addTextChangedListener {
                    tile.mqttData.pubs["humi"] = (it ?: "").toString()
                }
                b.tpThermostatHumiditySetpointSub.addTextChangedListener {
                    tile.mqttData.subs["humi_set"] = (it ?: "").toString()
                }
                b.tpThermostatHumiditySetpointPub.addTextChangedListener {
                    tile.mqttData.pubs["humi_set"] = (it ?: "").toString()
                }
                b.tpThermostatModeSub.addTextChangedListener {
                    tile.mqttData.subs["mode"] = (it ?: "").toString()
                }
                b.tpThermostatModePub.addTextChangedListener {
                    tile.mqttData.pubs["mode"] = (it ?: "").toString()
                }

                b.tpThermostatTemperaturePath.addTextChangedListener {
                    tile.mqttData.jsonPaths["temp"] = (it ?: "").toString()
                }
                b.tpThermostatTemperatureSetpointPath.addTextChangedListener {
                    tile.mqttData.jsonPaths["temp_set"] = (it ?: "").toString()
                }
                b.tpThermostatHumidityPath.addTextChangedListener {
                    tile.mqttData.jsonPaths["humi"] = (it ?: "").toString()
                }
                b.tpThermostatHumiditySetpointPath.addTextChangedListener {
                    tile.mqttData.jsonPaths["humi_set"] = (it ?: "").toString()
                }
                b.tpThermostatModePath.addTextChangedListener {
                    tile.mqttData.jsonPaths["mode"] = (it ?: "").toString()
                }

                b.tpThermostatHumidityStep.addTextChangedListener {
                    tile.humidityStep = it.toString().toFloatOrNull() ?: 5f
                }

                b.tpThermostatTemperatureFrom.addTextChangedListener {
                    tile.temperatureRange[0] = it.toString().toFloatOrNull() ?: 15f
                }
                b.tpThermostatTemperatureTo.addTextChangedListener {
                    tile.temperatureRange[1] = it.toString().toFloatOrNull() ?: 30f
                }
                b.tpThermostatTemperatureStep.addTextChangedListener {
                    tile.temperatureRange[2] = it.toString().toFloatOrNull() ?: .5f
                }

                b.tpThermostatHumiditySetpoint.setOnCheckedChangeListener { _, state ->
                    tile.humiditySetpoint = state
                    b.tpThermostatHumidityStepBox.visibility = if (state) VISIBLE else GONE
                    b.tpThermostatHumidityTopicBox.visibility = if (state) VISIBLE else GONE
                }

                b.tpThermostatShowPayload.setOnCheckedChangeListener { _, state ->
                    tile.showPayload = state
                }

                b.tpThermostatTemperaturePubCopy.setOnClickListener {
                    b.tpThermostatTemperaturePub.setText(b.tpThermostatTemperatureSub.text)
                }

                b.tpThermostatTemperatureSetpointPubCopy.setOnClickListener {
                    b.tpThermostatTemperatureSetpointPub.setText(b.tpThermostatTemperatureSetpointSub.text)
                }

                b.tpThermostatHumidityPubCopy.setOnClickListener {
                    b.tpThermostatHumidityPub.setText(b.tpThermostatHumiditySub.text)
                }

                b.tpThermostatModePubCopy.setOnClickListener {
                    b.tpThermostatModePub.setText(b.tpThermostatModeSub.text)
                }

                val adapter = GenericAdapter(requireContext())

                val list = MutableList(tile.modes.size) {
                    GenericItem(R.layout.item_option)
                }

                adapter.setHasStableIds(true)
                adapter.onBindViewHolder = { item, holder, _ ->
                    adapter.list.indexOf(item).let { pos ->
                        val a = holder.itemView.findViewById<EditText>(R.id.io_alias)
                        val p = holder.itemView.findViewById<EditText>(R.id.io_payload)
                        val r = holder.itemView.findViewById<Button>(R.id.io_remove)

                        a.setText(tile.modes[pos].first)
                        p.setText(tile.modes[pos].second)

                        a.addTextChangedListener {
                            adapter.list.indexOf(item).let { pos ->
                                if (pos >= 0)
                                    tile.modes[pos] =
                                        Pair((it ?: "").toString(), tile.modes[pos].second)
                            }
                        }

                        p.addTextChangedListener {
                            adapter.list.indexOf(item).let { pos ->
                                if (pos >= 0)
                                    tile.modes[pos] =
                                        Pair(tile.modes[pos].first, (it ?: "").toString())
                            }
                        }

                        r.setOnClickListener {
                            if (list.size > 1) {
                                adapter.list.indexOf(item).let {
                                    if (it >= 0) {
                                        tile.modes.removeAt(it)
                                        adapter.removeItemAt(it)
                                    }
                                }
                            }
                        }
                    }
                }

                b.tpThermostatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                b.tpThermostatRecyclerView.adapter = adapter

                adapter.submitList(list)

                b.tpThermostatModeAdd.setOnClickListener {
                    tile.modes.add(Pair("", ""))
                    list.add(GenericItem(R.layout.item_option))
                    adapter.notifyItemInserted(list.size - 1)
                }
            }
//--------------------------------------------------------------------------------------------------
            is LightsTile -> {
                val tile = tile as LightsTile
            }
        }
    }

    private fun mqttSwitchOnCheckedChangeListener(state: Boolean) {
        switchMqttTab(state)

        tile.mqttData.isEnabled = state
        dashboard.dg?.mqttd?.notifyOptionsChanged()
    }

    private fun switchMqttTab(state: Boolean, duration: Long = 250) {
        b.tpMqtt.let {
            it.visibility = if (state) VISIBLE else GONE
            b.tpMqttArrow.animate()
                .rotation(if (state) 0f else 180f)
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = duration
        }

        settings.mqttTabShow = state
    }
}