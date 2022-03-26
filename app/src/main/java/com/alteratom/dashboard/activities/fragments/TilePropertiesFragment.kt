package com.alteratom.dashboard.activities.fragments

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
import androidx.recyclerview.widget.RecyclerView
import com.alteratom.R
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.databinding.FragmentTilePropertiesBinding
import com.alteratom.dashboard.digitsOnly
import com.alteratom.dashboard.G
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.getIconColorPallet
import com.alteratom.dashboard.G.getIconHSV
import com.alteratom.dashboard.G.getIconRes
import com.alteratom.dashboard.G.setIconHSV
import com.alteratom.dashboard.G.setIconKey
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.recycler_view.GenericAdapter
import com.alteratom.dashboard.recycler_view.GenericItem
import com.alteratom.tile.types.button.TextTile
import com.alteratom.tile.types.color.ColorTile
import com.alteratom.tile.types.lights.LightsTile
import com.alteratom.tile.types.pick.SelectTile
import com.alteratom.tile.types.slider.SliderTile
import com.alteratom.tile.types.switch.SwitchTile
import com.alteratom.tile.types.terminal.TerminalTile
import com.alteratom.tile.types.thermostat.ThermostatTile
import com.alteratom.tile.types.time.TimeTile
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

        setupIcon(tile.iconRes, tile.colorPallet.color, b.tpIconFrame, b.tpIcon)

        b.tpNotSilentSwitch.visibility = if (tile.doNotify) VISIBLE else GONE
        switchMqttTab(settings.mqttTabShow, 0)

        b.tpTag.setText(tile.tag)
        b.tpMqttSub.setText(tile.mqttData.subs["base"])
        b.tpMqttPub.setText(tile.mqttData.pubs["base"])
        b.tpMqttPayload.setText(tile.mqttData.payloads["base"] ?: "")
        b.tpMqttJsonPayloadPath.setText(tile.mqttData.jsonPaths["base"] ?: "")
        b.tpTileType.text = tile.typeTag.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        b.tpMqttSwitch.isChecked = tile.mqttData.isEnabled
        b.tpMqttConfirmSwitch.isChecked = tile.mqttData.confirmPub
        b.tpMqttRetainSwitch.isChecked = tile.mqttData.retain
        b.tpLogSwitch.isChecked = tile.doLog
        b.tpNotSwitch.isChecked = tile.doNotify
        b.tpNotSilentSwitch.isChecked = tile.silentNotify

        b.tpQos.check(
            when (tile.mqttData.qos) {
                0 -> R.id.tp_qos0
                1 -> R.id.tp_qos1
                2 -> R.id.tp_qos2
                else -> R.id.tp_qos1
            }
        )

        tile.mqttData.payloadIsJson.let {
            b.tpMqttJsonSwitch.isChecked = it
            b.tpMqttJsonPayload.visibility = if (it) VISIBLE else GONE
        }

        b.tpTag.addTextChangedListener {
            tile.tag = (it ?: "").toString()
        }

        b.tpEditIcon.setOnClickListener {
            getIconHSV = { tile.hsv }
            getIconRes = { tile.iconRes }
            getIconColorPallet = { tile.colorPallet }

            setIconHSV = { hsv -> tile.hsv = hsv }
            setIconKey = { key -> tile.iconKey = key }

            (activity as MainActivity).fm.replaceWith(TileIconFragment())
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
            b.tpMqttPub.text = b.tpMqttSub.text
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

        b.tpMqttRetainSwitch.setOnCheckedChangeListener { _, state ->
            tile.mqttData.retain = state
        }

        b.tpMqttConfirmSwitch.setOnCheckedChangeListener { _, state ->
            tile.mqttData.confirmPub = state
        }

        b.tpMqttJsonSwitch.setOnCheckedChangeListener { _, state ->
            tile.mqttData.payloadIsJson = state
            b.tpMqttJsonPayload.visibility = if (state) VISIBLE else GONE
        }

        b.tpMqttJsonPayloadPath.addTextChangedListener {
            tile.mqttData.jsonPaths["base"] = (it ?: "").toString()
        }

        b.tpLogSwitch.setOnCheckedChangeListener { _, state ->
            tile.doLog = state
        }

        b.tpNotSwitch.setOnCheckedChangeListener { _, state ->
            tile.doNotify = state
            b.tpNotSilentSwitch.visibility = if (tile.doNotify) VISIBLE else GONE
        }

        b.tpNotSilentSwitch.setOnCheckedChangeListener { _, state ->
            tile.silentNotify = state
        }

        when (tile) {
            //is ButtonTile -> {
            //    val tile = tile as ButtonTile
            //    b.tpButton.visibility = VISIBLE
            //}
//--------------------------------------------------------------------------------------------------
            is TextTile -> {
                val tile = tile as TextTile

                b.tpText.visibility = VISIBLE
                b.tpMqttPayloadTypeBox.visibility = VISIBLE

                b.tpTextBig.isChecked = tile.isBig

                b.tpTextBig.setOnCheckedChangeListener { _, isChecked ->
                    tile.isBig = isChecked
                }

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
                b.tpMqttPayloadHint.visibility = VISIBLE

                b.tpSliderDrag.isChecked = tile.dragCon

                b.tpMqttPayloadHint.text = "Use @value to insert current value"
                b.tpSliderFrom.setText(tile.range[0].toString())
                b.tpSliderTo.setText(tile.range[1].toString())
                b.tpSliderStep.setText(tile.range[2].toString())

                b.tpSliderFrom.addTextChangedListener { it ->
                    (it ?: "").toString().let { raw ->
                        (it ?: "").toString().digitsOnly().let { parsed ->
                            if (raw != parsed) b.tpSliderFrom.setText(parsed)
                            else parsed.toIntOrNull()?.let {
                                tile.range[0] = it
                            }
                        }
                    }
                }

                b.tpSliderTo.addTextChangedListener { it ->
                    (it ?: "").toString().let { raw ->
                        (it ?: "").toString().digitsOnly().let { parsed ->
                            if (raw != parsed) b.tpSliderTo.setText(parsed)
                            else parsed.toIntOrNull()?.let {
                                tile.range[2] = it
                            }
                        }
                    }
                }

                b.tpSliderStep.addTextChangedListener { it ->
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

                //b.tpSwitch.visibility = VISIBLE
                b.tpMqttPayloadsBox.visibility = VISIBLE

                b.tpMqttPayloadTrue.setText(tile.mqttData.payloads["true"] ?: "")
                b.tpMqttPayloadFalse.setText(tile.mqttData.payloads["false"] ?: "")

                setupIcon(
                    tile.iconResTrue,
                    tile.colorPalletTrue.color,
                    b.tpMqttPayloadTrueIconFrame,
                    b.tpMqttPayloadTrueIcon
                )
                setupIcon(
                    tile.iconResFalse,
                    tile.colorPalletFalse.color,
                    b.tpMqttPayloadFalseIconFrame,
                    b.tpMqttPayloadFalseIcon
                )

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

                b.tpSelectShowPayload.setOnCheckedChangeListener { _, state ->
                    tile.showPayload = state
                }

                setupOptionsRecyclerView(tile.options, b.tpSelectRecyclerView, b.tpSelectAdd)
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
                b.tpMqttPayloadHint.visibility = VISIBLE


                b.tpTimeType.check(
                    when (tile.isDate) {
                        false -> {
                            b.tpMqttPayload.setText(tile.mqttData.payloads["time"])
                            b.tpMqttPayloadHint.text =
                                "Use @hour and @minute to insert current values."
                            R.id.tp_time_time
                        }
                        true -> {
                            b.tpMqttPayload.setText(tile.mqttData.payloads["date"])
                            b.tpMqttPayloadHint.text =
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
                    b.tpMqttPayloadHint.text =
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
                b.tpMqttPayloadHint.visibility = VISIBLE
                b.tpColorPaintRawBox.visibility = if (tile.doPaint) VISIBLE else GONE

                b.tpColorDoPaint.isChecked = tile.doPaint
                b.tpColorPaintRaw.isChecked = tile.paintRaw

                b.tpColorColorType.check(
                    when (tile.colorType) {
                        "hsv" -> R.id.tp_color_hsv
                        "hex" -> R.id.tp_color_hex
                        "rgb" -> R.id.tp_color_rgb
                        else -> R.id.tp_color_hsv
                    }
                )

                b.tpMqttPayload.setText(tile.mqttData.payloads[tile.colorType])
                b.tpMqttPayloadHint.text =
                    "Use ${
                        when (tile.colorType) {
                            "hsv" -> "@h, @s, @v"
                            "hex" -> "@hex"
                            "rgb" -> "@r, @g, @b"
                            else -> "@hex"
                        }
                    } to insert current value."

                b.tpColorDoPaint.setOnCheckedChangeListener { _, state ->
                    tile.doPaint = state
                    b.tpColorPaintRawBox.visibility = if (tile.doPaint) VISIBLE else GONE
                }

                b.tpColorPaintRaw.setOnCheckedChangeListener { _, state ->
                    tile.paintRaw = state
                }

                b.tpColorColorType.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
                    tile.colorType = when (id) {
                        R.id.tp_color_hsv -> "hsv"
                        R.id.tp_color_hex -> "hex"
                        R.id.tp_color_rgb -> "rgb"
                        else -> "hex"
                    }

                    b.tpMqttPayload.setText(tile.mqttData.payloads[tile.colorType])
                    b.tpMqttPayloadHint.text =
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

                b.tpMqttRetainBox.visibility = GONE
                b.tpMqttTopics.visibility = GONE
                b.tpMqttJsonPayload.visibility = GONE
                b.tpThermostat.visibility = VISIBLE
                b.tpThermostatTopics.visibility = VISIBLE
                b.tpThermostatPaths.visibility =
                    if (tile.mqttData.payloadIsJson) VISIBLE else GONE

                b.tpThermostatTemperatureSub.setText(tile.mqttData.subs["temp"])
                b.tpThermostatTemperatureSetpointSub.setText(tile.mqttData.subs["temp_set"])
                b.tpThermostatTemperatureSetpointPub.setText(tile.mqttData.pubs["temp_set"])
                b.tpThermostatHumiditySub.setText(tile.mqttData.subs["humi"])
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
                b.tpThermostatTemperatureStep.setText(tile.temperatureStep.toString())

                tile.includeHumiditySetpoint.let {
                    b.tpThermostatHumidityTopicsBox.visibility = if (it) VISIBLE else GONE
                    b.tpThermostatHumidityStepBox.visibility = if (it) VISIBLE else GONE
                    b.tpThermostatIncludeHumiditySetpoint.isChecked = it
                }

                b.tpThermostatShowPayload.isChecked = tile.showPayload
                b.tpThermostatTempRetain.isChecked = tile.retain[0]
                b.tpThermostatHumiRetain.isChecked = tile.retain[1]
                b.tpThermostatModeRetain.isChecked = tile.retain[2]

                b.tpMqttJsonSwitch.setOnCheckedChangeListener { _, state ->
                    tile.mqttData.payloadIsJson = state
                    b.tpThermostatPaths.visibility = if (state) VISIBLE else GONE
                }


                b.tpThermostatTemperatureSub.addTextChangedListener {
                    tile.mqttData.subs["temp"] = (it ?: "").toString()
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
                b.tpThermostatTemperatureSetpointSub.addTextChangedListener {
                    tile.mqttData.subs["temp_set"] = (it ?: "").toString()
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
                b.tpThermostatTemperatureSetpointPub.addTextChangedListener {
                    tile.mqttData.pubs["temp_set"] = (it ?: "").toString()
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
                b.tpThermostatHumiditySub.addTextChangedListener {
                    tile.mqttData.subs["humi"] = (it ?: "").toString()
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
                b.tpThermostatHumiditySetpointSub.addTextChangedListener {
                    tile.mqttData.subs["humi_set"] = (it ?: "").toString()
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
                b.tpThermostatHumiditySetpointPub.addTextChangedListener {
                    tile.mqttData.pubs["humi_set"] = (it ?: "").toString()
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
                b.tpThermostatModeSub.addTextChangedListener {
                    tile.mqttData.subs["mode"] = (it ?: "").toString()
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
                b.tpThermostatModePub.addTextChangedListener {
                    tile.mqttData.pubs["mode"] = (it ?: "").toString()
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }


                b.tpThermostatTemperatureSetpointPubCopy.setOnClickListener {
                    b.tpThermostatTemperatureSetpointPub.text = b.tpThermostatTemperatureSetpointSub.text
                }
                b.tpThermostatHumiditySetpointPubCopy.setOnClickListener {
                    b.tpThermostatHumiditySetpointPub.text = b.tpThermostatHumiditySetpointSub.text
                }
                b.tpThermostatModePubCopy.setOnClickListener {
                    b.tpThermostatModePub.text = b.tpThermostatModeSub.text
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


                b.tpThermostatTempRetain.setOnCheckedChangeListener { _, isChecked ->
                    tile.retain[0] = isChecked
                }
                b.tpThermostatHumiRetain.setOnCheckedChangeListener { _, isChecked ->
                    tile.retain[1] = isChecked
                }
                b.tpThermostatModeRetain.setOnCheckedChangeListener { _, isChecked ->
                    tile.retain[2] = isChecked
                }


                b.tpThermostatHumidityStep.addTextChangedListener {
                    tile.humidityStep = it.toString().toFloatOrNull() ?: 5f
                }
                b.tpThermostatTemperatureFrom.addTextChangedListener {
                    tile.temperatureRange[0] = it.toString().toIntOrNull() ?: 15
                }
                b.tpThermostatTemperatureTo.addTextChangedListener {
                    tile.temperatureRange[1] = it.toString().toIntOrNull() ?: 30
                }
                b.tpThermostatTemperatureStep.addTextChangedListener {
                    tile.temperatureStep = it.toString().toFloatOrNull() ?: .5f
                }


                b.tpThermostatIncludeHumiditySetpoint.setOnCheckedChangeListener { _, state ->
                    tile.includeHumiditySetpoint = state
                    b.tpThermostatHumidityStepBox.visibility = if (state) VISIBLE else GONE
                    b.tpThermostatHumidityTopicsBox.visibility = if (state) VISIBLE else GONE
                }
                b.tpThermostatShowPayload.setOnCheckedChangeListener { _, state ->
                    tile.showPayload = state
                }

                setupOptionsRecyclerView(
                    tile.modes,
                    b.tpThermostatRecyclerView,
                    b.tpThermostatModeAdd
                )
            }
//--------------------------------------------------------------------------------------------------
            is LightsTile -> {
                val tile = tile as LightsTile

                setupIcon(
                    tile.iconResFalse,
                    tile.colorPalletFalse.color,
                    b.tpMqttPayloadFalseIconFrame,
                    b.tpMqttPayloadFalseIcon
                )
                setupIcon(
                    tile.iconResTrue,
                    tile.colorPalletTrue.color,
                    b.tpMqttPayloadTrueIconFrame,
                    b.tpMqttPayloadTrueIcon
                )

                (if (tile.includePicker) VISIBLE else GONE).let {
                    b.tpLightsColorRetain.visibility = it
                    b.tpLightsColorTopics.visibility = it
                    b.tpLightsTypeBox.visibility = it
                    b.tpMqttPayloadBox.visibility = it
                    b.tpLightsColorPathBox.visibility = it
                    b.tpLightsPaintBox.visibility = it
                }

                b.tpMqttRetainBox.visibility = GONE
                b.tpMqttTopics.visibility = GONE
                b.tpMqttJsonPayload.visibility = GONE
                b.tpLights.visibility = VISIBLE
                b.tpLightsTopics.visibility = VISIBLE
                b.tpMqttPayloadsBox.visibility = VISIBLE
                b.tpMqttPayloadHint.visibility = VISIBLE
                b.tpLightsPaintRawBox.visibility = if (tile.doPaint) VISIBLE else GONE
                b.tpLightsPaths.visibility =
                    if (tile.mqttData.payloadIsJson) VISIBLE else GONE

                b.tpLightsStateSub.setText(tile.mqttData.subs["state"])
                b.tpLightsStatePub.setText(tile.mqttData.pubs["state"])
                b.tpLightsColorSub.setText(tile.mqttData.subs["color"])
                b.tpLightsColorPub.setText(tile.mqttData.pubs["color"])
                b.tpLightsBrightnessSub.setText(tile.mqttData.subs["bright"])
                b.tpLightsBrightnessPub.setText(tile.mqttData.pubs["bright"])
                b.tpLightsModeSub.setText(tile.mqttData.subs["mode"])
                b.tpLightsModePub.setText(tile.mqttData.pubs["mode"])

                b.tpLightsStatePath.setText(tile.mqttData.jsonPaths["state"])
                b.tpLightsColorPath.setText(tile.mqttData.jsonPaths["color"])
                b.tpLightsBrightnessPath.setText(tile.mqttData.jsonPaths["bright"])
                b.tpLightsModePath.setText(tile.mqttData.jsonPaths["mode"])


                b.tpMqttPayloadFalse.setText(tile.mqttData.payloads["false"] ?: "")
                b.tpMqttPayloadTrue.setText(tile.mqttData.payloads["true"] ?: "")
                b.tpMqttPayload.setText(tile.mqttData.payloads[tile.colorType])

                b.tpLightsDoPaint.isChecked = tile.doPaint
                b.tpLightsPaintRaw.isChecked = tile.paintRaw
                b.tpLightsShowPayload.isChecked = tile.showPayload
                b.tpLightsStateRetain.isChecked = tile.retain[0]
                b.tpLightsColorRetain.isChecked = tile.retain[1]
                b.tpLightsBrightnessRetain.isChecked = tile.retain[2]
                b.tpLightsModeRetain.isChecked = tile.retain[3]
                b.tpLightsIncludePicker.isChecked = tile.includePicker
                b.tpMqttPayloadTag.text = "Color publish payload"
                b.tpMqttPayloadHint.text =
                    "Use ${
                        when (tile.colorType) {
                            "hsv" -> "@h, @s, @v"
                            "hex" -> "@hex"
                            "rgb" -> "@r, @g, @b"
                            else -> "@hex"
                        }
                    } to insert current value."
                b.tpLightsColorType.check(
                    when (tile.colorType) {
                        "hsv" -> R.id.tp_lights_hsv
                        "hex" -> R.id.tp_lights_hex
                        "rgb" -> R.id.tp_lights_rgb
                        else -> R.id.tp_lights_hsv
                    }
                )


                b.tpLightsStateSub.addTextChangedListener {
                    tile.mqttData.subs["state"] = (it ?: "").toString()
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
                b.tpLightsStatePub.addTextChangedListener {
                    tile.mqttData.pubs["state"] = (it ?: "").toString()
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
                b.tpLightsColorSub.addTextChangedListener {
                    tile.mqttData.subs["color"] = (it ?: "").toString()
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
                b.tpLightsColorPub.addTextChangedListener {
                    tile.mqttData.pubs["color"] = (it ?: "").toString()
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
                b.tpLightsBrightnessSub.addTextChangedListener {
                    tile.mqttData.subs["bright"] = (it ?: "").toString()
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
                b.tpLightsBrightnessPub.addTextChangedListener {
                    tile.mqttData.pubs["bright"] = (it ?: "").toString()
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
                b.tpLightsModeSub.addTextChangedListener {
                    tile.mqttData.subs["mode"] = (it ?: "").toString()
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
                b.tpLightsModePub.addTextChangedListener {
                    tile.mqttData.pubs["mode"] = (it ?: "").toString()
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }


                b.tpLightsStatePubCopy.setOnClickListener {
                    b.tpLightsStatePub.text = b.tpLightsStateSub.text
                }
                b.tpLightsColorPubCopy.setOnClickListener {
                    b.tpLightsColorPub.text = b.tpLightsColorSub.text
                }
                b.tpLightsBrightnessPubCopy.setOnClickListener {
                    b.tpLightsBrightnessPub.text = b.tpLightsBrightnessSub.text
                }
                b.tpLightsModePubCopy.setOnClickListener {
                    b.tpLightsModePub.text = b.tpLightsModeSub.text
                }


                b.tpLightsStatePath.addTextChangedListener {
                    tile.mqttData.jsonPaths["state"] = (it ?: "").toString()
                }
                b.tpLightsColorPath.addTextChangedListener {
                    tile.mqttData.jsonPaths["color"] = (it ?: "").toString()
                }
                b.tpLightsBrightnessPath.addTextChangedListener {
                    tile.mqttData.jsonPaths["brightness"] = (it ?: "").toString()
                }
                b.tpLightsModePath.addTextChangedListener {
                    tile.mqttData.jsonPaths["mode"] = (it ?: "").toString()
                }


                b.tpLightsStateRetain.setOnCheckedChangeListener { _, isChecked ->
                    tile.retain[0] = isChecked
                }
                b.tpLightsColorRetain.setOnCheckedChangeListener { _, isChecked ->
                    tile.retain[1] = isChecked
                }
                b.tpLightsBrightnessRetain.setOnCheckedChangeListener { _, isChecked ->
                    tile.retain[2] = isChecked
                }
                b.tpLightsModeRetain.setOnCheckedChangeListener { _, isChecked ->
                    tile.retain[3] = isChecked
                }


                b.tpMqttPayloadTrue.addTextChangedListener {
                    tile.mqttData.payloads["true"] = (it ?: "").toString()
                }
                b.tpMqttPayloadFalse.addTextChangedListener {
                    tile.mqttData.payloads["false"] = (it ?: "").toString()
                }
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


                b.tpMqttPayload.addTextChangedListener {
                    tile.mqttData.payloads[tile.colorType] = (it ?: "").toString()
                }
                b.tpLightsIncludePicker.setOnCheckedChangeListener { _, state ->
                    tile.includePicker = state
                    (if (tile.includePicker) VISIBLE else GONE).let {
                        b.tpLightsColorRetain.visibility = it
                        b.tpLightsColorTopics.visibility = it
                        b.tpLightsTypeBox.visibility = it
                        b.tpMqttPayloadBox.visibility = it
                        b.tpLightsColorPathBox.visibility = it
                        b.tpLightsPaintBox.visibility = it
                    }
                }
                b.tpLightsDoPaint.setOnCheckedChangeListener { _, state ->
                    tile.doPaint = state
                    b.tpLightsPaintRawBox.visibility = if (tile.doPaint) VISIBLE else GONE
                }

                b.tpLightsPaintRaw.setOnCheckedChangeListener { _, state ->
                    tile.paintRaw = state
                }
                b.tpLightsShowPayload.setOnCheckedChangeListener { _, state ->
                    tile.showPayload = state
                }
                b.tpMqttJsonSwitch.setOnCheckedChangeListener { _, state ->
                    tile.mqttData.payloadIsJson = state
                    b.tpLightsPaths.visibility = if (state) VISIBLE else GONE
                }
                b.tpLightsColorType.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
                    tile.colorType = when (id) {
                        R.id.tp_lights_hsv -> "hsv"
                        R.id.tp_lights_hex -> "hex"
                        R.id.tp_lights_rgb -> "rgb"
                        else -> "hex"
                    }

                    b.tpMqttPayload.setText(tile.mqttData.payloads[tile.colorType])
                    b.tpMqttPayloadHint.text =
                        "Use ${
                            when (tile.colorType) {
                                "hsv" -> "@h, @s, @v"
                                "hex" -> "@hex"
                                "rgb" -> "@r, @g, @b"
                                else -> "@hex"
                            }
                        } to insert current value."
                }


                setupOptionsRecyclerView(tile.modes, b.tpLightsRecyclerView, b.tpLightsAdd)
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

    private fun setupIcon(icon: Int, color: Int, frameView: View, iconView: View) {
        iconView.setBackgroundResource(icon)
        iconView.backgroundTintList = ColorStateList.valueOf(color)

        val drawable = frameView.background as? GradientDrawable
        drawable?.setStroke(1, color)
        drawable?.cornerRadius = 15f
    }

    private fun setupOptionsRecyclerView(
        options: MutableList<Pair<String, String>>,
        rv: RecyclerView,
        add: View
    ) {
        val adapter = GenericAdapter(requireContext())

        val list = MutableList(options.size) {
            GenericItem(R.layout.item_option)
        }

        adapter.setHasStableIds(true)
        adapter.onBindViewHolder = { item, holder, _ ->
            adapter.list.indexOf(item).let { pos ->
                val a = holder.itemView.findViewById<EditText>(R.id.io_alias)
                val p = holder.itemView.findViewById<EditText>(R.id.io_payload)
                val r = holder.itemView.findViewById<Button>(R.id.io_remove)

                a.setText(options[pos].first)
                p.setText(options[pos].second)

                a.addTextChangedListener {
                    adapter.list.indexOf(item).let { pos ->
                        if (pos >= 0)
                            options[pos] =
                                Pair((it ?: "").toString(), options[pos].second)
                    }
                }

                p.addTextChangedListener {
                    adapter.list.indexOf(item).let { pos ->
                        if (pos >= 0)
                            options[pos] =
                                Pair(options[pos].first, (it ?: "").toString())
                    }
                }

                r.setOnClickListener {
                    if (list.size > 1) {
                        adapter.list.indexOf(item).let {
                            if (it >= 0) {
                                options.removeAt(it)
                                adapter.removeItemAt(it)
                            }
                        }
                    }
                }
            }
        }

        add.setOnClickListener {
            options.add(Pair("", ""))
            list.add(GenericItem(R.layout.item_option))
            adapter.notifyItemInserted(list.size - 1)
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        adapter.submitList(list)
    }
}