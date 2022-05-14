import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.Theme.Companion.artist
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.Theme.Companion.isDark
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.databinding.FragmentTilePropertiesBinding
import com.alteratom.tile.types.button.ButtonTile
import com.alteratom.tile.types.button.TextTile
import com.alteratom.tile.types.button.compose.ButtonTileCompose
import com.alteratom.tile.types.color.ColorTile
import com.alteratom.tile.types.color.compose.*
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
        //b = FragmentTilePropertiesBinding.inflate(inflater, container, false)
        //return b.root

        requireActivity().window.statusBarColor = artist.colors.background
        WindowInsetsControllerCompat(requireActivity().window, requireActivity().window.decorView)
            .isAppearanceLightStatusBars = !isDark

        return ComposeView(requireContext()).apply {
            setContent {
                ComposeTheme(isDark) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        G.tile.dashboard?.type?.let {
                            when (G.tile) {
                                is ButtonTile -> ButtonTileCompose
                                is ColorTile -> ColorTileCompose
                                is LightsTile -> LightsTileCompose
                                is SelectTile -> SelectTileCompose
                                is SliderTile -> SliderTileCompose
                                is SwitchTile -> SwitchTileCompose
                                is TerminalTile -> TerminalTileCompose
                                is TextTile -> TextTileCompose
                                is ThermostatTile -> ThermostatTileCompose
                                is TimeTile -> TimeTileCompose
                                else -> ButtonTileCompose
                            }.compose(it)
                        }
                    }
                }
            }
        }
    }

    /*
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        G.theme.apply(b.root, requireActivity())

        setupIcon(tile.iconRes, tile.colorPallet.color, b.tpIconFrame, b.tpIcon)

        b.tpNotSilentSwitch.visibility = if (tile.doNotify) VISIBLE else GONE
        switchMqttTab(settings.mqttTabShow, 0)

        b.tpTag.setText(tile.tag)
        b.tpMqttSub.setText(tile.mqtt.subs["base"])
        b.tpMqttPub.setText(tile.mqtt.pubs["base"])
        b.tpMqttPayload.setText(tile.mqtt.payloads["base"] ?: "")
        b.tpMqttJsonPayloadPath.setText(tile.mqtt.jsonPaths["base"] ?: "")
        b.tpTileType.text = tile.typeTag.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        b.tpMqttSwitch.isChecked = tile.mqtt.isEnabled
        b.tpMqttConfirmSwitch.isChecked = tile.mqtt.confirmPub
        b.tpMqttRetainSwitch.isChecked = tile.mqtt.retain
        b.tpLogSwitch.isChecked = tile.doLog
        b.tpNotSwitch.isChecked = tile.doNotify
        b.tpNotSilentSwitch.isChecked = tile.silentNotify

        b.tpQos.check(
            when (tile.mqtt.qos) {
                0 -> R.id.tp_qos0
                1 -> R.id.tp_qos1
                2 -> R.id.tp_qos2
                else -> R.id.tp_qos1
            }
        )

        tile.mqtt.payloadIsJson.let {
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

            fm.replaceWith(TileIconFragment())
        }

        b.tpMqttSwitch.setOnCheckedChangeListener { _, state ->
            mqttSwitchOnCheckedChangeListener(state)
        }

        b.tpMqttArrow.setOnClickListener {
            switchMqttTab(!b.tpMqtt.isVisible)
        }

        b.tpMqttPub.addTextChangedListener {
            tile.mqtt.pubs["base"] = (it ?: "").toString()
            //dashboard.daemon.notifyOptionsChanged()
        }

        b.tpMqttSub.addTextChangedListener {
            tile.mqtt.subs["base"] = (it ?: "").toString()
            //dashboard.daemon.notifyOptionsChanged()
        }

        b.tpMqttPubCopy.setOnClickListener {
            b.tpMqttPub.text = b.tpMqttSub.text
        }

        b.tpQos.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
            tile.mqtt.qos = when (id) {
                R.id.tp_qos0 -> 0
                R.id.tp_qos1 -> 1
                R.id.tp_qos2 -> 2
                else -> 1
            }
            //dashboard.daemon.notifyOptionsChanged()
        }

        b.tpMqttRetainSwitch.setOnCheckedChangeListener { _, state ->
            tile.mqtt.retain = state
        }

        b.tpMqttConfirmSwitch.setOnCheckedChangeListener { _, state ->
            tile.mqtt.confirmPub = state
        }

        b.tpMqttJsonSwitch.setOnCheckedChangeListener { _, state ->
            tile.mqtt.payloadIsJson = state
            b.tpMqttJsonPayload.visibility = if (state) VISIBLE else GONE
        }

        b.tpMqttJsonPayloadPath.addTextChangedListener {
            tile.mqtt.jsonPaths["base"] = (it ?: "").toString()
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

        b.tpLeft.setOnClickListener {
            TileSwitcher.switch(true)
        }

        b.tpRight.setOnClickListener {
            TileSwitcher.switch(false)
        }

        b.tpRoot.onInterceptTouch = { e ->
            TileSwitcher.handle(e)
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
                    if (tile.mqtt.varPayload) R.id.tp_mqtt_payload_var
                    else {
                        b.tpMqttPayload.visibility = VISIBLE
                        R.id.tp_mqtt_payload_val
                    }
                )
                b.tpPayloadType.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
                    tile.mqtt.varPayload = when (id) {
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
                    tile.mqtt.jsonPaths["base"] = (it ?: "").toString()
                }

                b.tpMqttPayload.addTextChangedListener {
                    tile.mqtt.payloads["base"] = (it ?: "").toString()
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
                                tile.range[1] = it
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
                    tile.mqtt.payloads["base"] = (it ?: "").toString()
                }
            }
//--------------------------------------------------------------------------------------------------
            is SwitchTile -> {
                val tile = tile as SwitchTile

                //b.tpSwitch.visibility = VISIBLE
                b.tpMqttPayloadsBox.visibility = VISIBLE

                b.tpMqttPayloadTrue.setText(tile.mqtt.payloads["true"] ?: "")
                b.tpMqttPayloadFalse.setText(tile.mqtt.payloads["false"] ?: "")

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

                    fm.replaceWith(TileIconFragment())
                }

                b.tpMqttPayloadFalseEditIcon.setOnClickListener {
                    getIconHSV = { tile.hsvFalse }
                    getIconRes = { tile.iconResFalse }
                    getIconColorPallet = { tile.colorPalletFalse }

                    setIconHSV = { hsv -> tile.hsvFalse = hsv }
                    setIconKey = { key -> tile.iconKeyFalse = key }

                    fm.replaceWith(TileIconFragment())
                }

                b.tpMqttPayloadTrue.addTextChangedListener {
                    tile.mqtt.payloads["true"] = (it ?: "").toString()
                }

                b.tpMqttPayloadFalse.addTextChangedListener {
                    tile.mqtt.payloads["false"] = (it ?: "").toString()
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
                    if (tile.mqtt.varPayload) R.id.tp_mqtt_payload_var
                    else {
                        b.tpMqttPayload.visibility = VISIBLE
                        R.id.tp_mqtt_payload_val
                    }
                )

                b.tpPayloadType.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
                    tile.mqtt.varPayload = when (id) {
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
                    tile.mqtt.payloads["base"] = (it ?: "").toString()
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
                            b.tpMqttPayload.setText(tile.mqtt.payloads["time"])
                            b.tpMqttPayloadHint.text =
                                "Use @hour and @minute to insert current values."
                            R.id.tp_time_time
                        }
                        true -> {
                            b.tpMqttPayload.setText(tile.mqtt.payloads["date"])
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
                    b.tpMqttPayload.setText(tile.mqtt.payloads[if (tile.isDate) "date" else "time"])
                    b.tpMqttPayloadHint.text =
                        "Use ${if (tile.isDate) "@day, @month, @year" else "@hour and @minute"} to insert current values."
                }

                if (!tile.isDate) {
                    b.tpTimeMilitary.setOnCheckedChangeListener { _, state ->
                        tile.isMilitary = state
                    }
                }

                b.tpMqttPayload.addTextChangedListener {
                    tile.mqtt.payloads[if (tile.isDate) "date" else "time"] =
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

                b.tpMqttPayload.setText(tile.mqtt.payloads[tile.colorType])
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

                    b.tpMqttPayload.setText(tile.mqtt.payloads[tile.colorType])
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
                    tile.mqtt.payloads[tile.colorType] = (it ?: "").toString()
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
                    if (tile.mqtt.payloadIsJson) VISIBLE else GONE

                b.tpThermostatTemperatureSub.setText(tile.mqtt.subs["temp"])
                b.tpThermostatTemperatureSetpointSub.setText(tile.mqtt.subs["temp_set"])
                b.tpThermostatTemperatureSetpointPub.setText(tile.mqtt.pubs["temp_set"])
                b.tpThermostatHumiditySub.setText(tile.mqtt.subs["humi"])
                b.tpThermostatHumiditySetpointSub.setText(tile.mqtt.subs["humi_set"])
                b.tpThermostatHumiditySetpointPub.setText(tile.mqtt.pubs["humi_set"])
                b.tpThermostatModeSub.setText(tile.mqtt.subs["mode"])
                b.tpThermostatModePub.setText(tile.mqtt.pubs["mode"])

                b.tpThermostatTemperaturePath.setText(tile.mqtt.jsonPaths["temp"])
                b.tpThermostatTemperatureSetpointPath.setText(tile.mqtt.jsonPaths["temp_set"])
                b.tpThermostatHumidityPath.setText(tile.mqtt.jsonPaths["humi"])
                b.tpThermostatHumiditySetpointPath.setText(tile.mqtt.jsonPaths["humi_set"])
                b.tpThermostatModePath.setText(tile.mqtt.jsonPaths["mode"])

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
                    tile.mqtt.payloadIsJson = state
                    b.tpThermostatPaths.visibility = if (state) VISIBLE else GONE
                }


                b.tpThermostatTemperatureSub.addTextChangedListener {
                    tile.mqtt.subs["temp"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpThermostatTemperatureSetpointSub.addTextChangedListener {
                    tile.mqtt.subs["temp_set"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpThermostatTemperatureSetpointPub.addTextChangedListener {
                    tile.mqtt.pubs["temp_set"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpThermostatHumiditySub.addTextChangedListener {
                    tile.mqtt.subs["humi"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpThermostatHumiditySetpointSub.addTextChangedListener {
                    tile.mqtt.subs["humi_set"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpThermostatHumiditySetpointPub.addTextChangedListener {
                    tile.mqtt.pubs["humi_set"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpThermostatModeSub.addTextChangedListener {
                    tile.mqtt.subs["mode"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpThermostatModePub.addTextChangedListener {
                    tile.mqtt.pubs["mode"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }


                b.tpThermostatTemperatureSetpointPubCopy.setOnClickListener {
                    b.tpThermostatTemperatureSetpointPub.text =
                        b.tpThermostatTemperatureSetpointSub.text
                }
                b.tpThermostatHumiditySetpointPubCopy.setOnClickListener {
                    b.tpThermostatHumiditySetpointPub.text = b.tpThermostatHumiditySetpointSub.text
                }
                b.tpThermostatModePubCopy.setOnClickListener {
                    b.tpThermostatModePub.text = b.tpThermostatModeSub.text
                }


                b.tpThermostatTemperaturePath.addTextChangedListener {
                    tile.mqtt.jsonPaths["temp"] = (it ?: "").toString()
                }
                b.tpThermostatTemperatureSetpointPath.addTextChangedListener {
                    tile.mqtt.jsonPaths["temp_set"] = (it ?: "").toString()
                }
                b.tpThermostatHumidityPath.addTextChangedListener {
                    tile.mqtt.jsonPaths["humi"] = (it ?: "").toString()
                }
                b.tpThermostatHumiditySetpointPath.addTextChangedListener {
                    tile.mqtt.jsonPaths["humi_set"] = (it ?: "").toString()
                }
                b.tpThermostatModePath.addTextChangedListener {
                    tile.mqtt.jsonPaths["mode"] = (it ?: "").toString()
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
                    if (tile.mqtt.payloadIsJson) VISIBLE else GONE

                b.tpLightsStateSub.setText(tile.mqtt.subs["state"])
                b.tpLightsStatePub.setText(tile.mqtt.pubs["state"])
                b.tpLightsColorSub.setText(tile.mqtt.subs["color"])
                b.tpLightsColorPub.setText(tile.mqtt.pubs["color"])
                b.tpLightsBrightnessSub.setText(tile.mqtt.subs["bright"])
                b.tpLightsBrightnessPub.setText(tile.mqtt.pubs["bright"])
                b.tpLightsModeSub.setText(tile.mqtt.subs["mode"])
                b.tpLightsModePub.setText(tile.mqtt.pubs["mode"])

                b.tpLightsStatePath.setText(tile.mqtt.jsonPaths["state"])
                b.tpLightsColorPath.setText(tile.mqtt.jsonPaths["color"])
                b.tpLightsBrightnessPath.setText(tile.mqtt.jsonPaths["bright"])
                b.tpLightsModePath.setText(tile.mqtt.jsonPaths["mode"])


                b.tpMqttPayloadFalse.setText(tile.mqtt.payloads["false"] ?: "")
                b.tpMqttPayloadTrue.setText(tile.mqtt.payloads["true"] ?: "")
                b.tpMqttPayload.setText(tile.mqtt.payloads[tile.colorType])

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
                    tile.mqtt.subs["state"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpLightsStatePub.addTextChangedListener {
                    tile.mqtt.pubs["state"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpLightsColorSub.addTextChangedListener {
                    tile.mqtt.subs["color"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpLightsColorPub.addTextChangedListener {
                    tile.mqtt.pubs["color"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpLightsBrightnessSub.addTextChangedListener {
                    tile.mqtt.subs["bright"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpLightsBrightnessPub.addTextChangedListener {
                    tile.mqtt.pubs["bright"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpLightsModeSub.addTextChangedListener {
                    tile.mqtt.subs["mode"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpLightsModePub.addTextChangedListener {
                    tile.mqtt.pubs["mode"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
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
                    tile.mqtt.jsonPaths["state"] = (it ?: "").toString()
                }
                b.tpLightsColorPath.addTextChangedListener {
                    tile.mqtt.jsonPaths["color"] = (it ?: "").toString()
                }
                b.tpLightsBrightnessPath.addTextChangedListener {
                    tile.mqtt.jsonPaths["brightness"] = (it ?: "").toString()
                }
                b.tpLightsModePath.addTextChangedListener {
                    tile.mqtt.jsonPaths["mode"] = (it ?: "").toString()
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
                    tile.mqtt.payloads["true"] = (it ?: "").toString()
                }
                b.tpMqttPayloadFalse.addTextChangedListener {
                    tile.mqtt.payloads["false"] = (it ?: "").toString()
                }
                b.tpMqttPayloadTrueEditIcon.setOnClickListener {
                    getIconHSV = { tile.hsvTrue }
                    getIconRes = { tile.iconResTrue }
                    getIconColorPallet = { tile.colorPalletTrue }

                    setIconHSV = { hsv -> tile.hsvTrue = hsv }
                    setIconKey = { key -> tile.iconKeyTrue = key }

                    fm.replaceWith(TileIconFragment())
                }

                b.tpMqttPayloadFalseEditIcon.setOnClickListener {
                    getIconHSV = { tile.hsvFalse }
                    getIconRes = { tile.iconResFalse }
                    getIconColorPallet = { tile.colorPalletFalse }

                    setIconHSV = { hsv -> tile.hsvFalse = hsv }
                    setIconKey = { key -> tile.iconKeyFalse = key }

                    fm.replaceWith(TileIconFragment())
                }


                b.tpMqttPayload.addTextChangedListener {
                    tile.mqtt.payloads[tile.colorType] = (it ?: "").toString()
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
                    tile.mqtt.payloadIsJson = state
                    b.tpLightsPaths.visibility = if (state) VISIBLE else GONE
                }
                b.tpLightsColorType.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
                    tile.colorType = when (id) {
                        R.id.tp_lights_hsv -> "hsv"
                        R.id.tp_lights_hex -> "hex"
                        R.id.tp_lights_rgb -> "rgb"
                        else -> "hex"
                    }

                    b.tpMqttPayload.setText(tile.mqtt.payloads[tile.colorType])
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

        tile.mqtt.isEnabled = state
        dashboard.daemon.notifyOptionsChanged()
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
        val adapter = RecyclerViewAdapter<RecyclerViewItem>(requireContext())

        val list = MutableList(options.size) {
            RecyclerViewItem(R.layout.item_option)
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
            list.add(RecyclerViewItem(R.layout.item_option))
            adapter.notifyItemInserted(list.size - 1)
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        adapter.submitList(list)
    }

     */
}

object TilePropComp {
    @Composable
    inline fun Box(crossinline content: @Composable () -> Unit) {
        var text by remember { mutableStateOf("") }

        Surface(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(text = "Tile properties", fontSize = 45.sp, color = colors.color)
                Row(
                    modifier = Modifier.padding(top = 5.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedButton(
                        contentPadding = PaddingValues(13.dp),
                        onClick = {},
                        border = BorderStroke(0.dp, colors.color),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(painterResource(G.tile.iconRes), "")
                    }

                    val typeTag = G.tile.typeTag.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }

                    EditText(
                        label = { BoldStartText("$typeTag ", "tile tag") },
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                content()

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            }
        }

        NavigationArrows({}, {})
    }

    @Composable
    inline fun CommunicationBox(crossinline content: @Composable () -> Unit) {
        var state by remember { mutableStateOf(true) }
        val rotation = if (state) 0f else 180f

        val angle: Float by animateFloatAsState(
            targetValue = if (rotation > 360 - rotation) {
                -(360 - rotation)
            } else rotation,
            animationSpec = tween(durationMillis = 200, easing = LinearEasing)
        )

        FrameBox(a = "Communication: ", b = "MQTT") {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabeledSwitch(
                        label = { Text("Enabled:", fontSize = 15.sp, color = colors.a) },
                        checked = state,
                        onCheckedChange = { state = it }
                    )

                    IconButton(
                        modifier = Modifier.size(40.dp),
                        onClick = { state = !state }
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_arrow), "",
                            tint = colors.a,
                            modifier = Modifier
                                .size(40.dp)
                                .rotate(angle)
                        )
                    }
                }

                AnimatedVisibility(visible = state) {
                    Column {
                        content()
                    }
                }
            }
        }
    }

    @Composable
    fun Communication0() {
        var text by remember { mutableStateOf("false") }

        EditText(
            label = { Text("Subscribe topic") },
            value = text,
            onValueChange = { text = it }
        )

        EditText(
            label = { Text("Publish topic") },
            value = text,
            onValueChange = { text = it },
            trailingIcon = {
                IconButton(onClick = {}) {
                    Icon(painterResource(R.drawable.il_file_copy), "", tint = colors.b)
                }
            }
        )
    }

    @Composable
    fun Communication1(
        retain: Boolean = true, pointer: @Composable () -> Unit = {
            var text by remember { mutableStateOf("false") }
            EditText(
                label = { Text("Payload JSON pointer") },
                value = text,
                onValueChange = { text = it }
            )
        }
    ) {
        var index by remember { mutableStateOf(0) }
        var state by remember { mutableStateOf(true) }
        var text by remember { mutableStateOf("false") }

        RadioGroup(
            listOf(
                "QoS 0: At most once. No guarantee.",
                "QoS 1: At least once. (Recommended)",
                "QoS 2: Delivery exactly once."
            ), "Quality of Service (MQTT protocol):",
            index,
            { index = it },
            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
        )

        if (retain) {
            LabeledSwitch(
                label = { Text("Retain massages:", fontSize = 15.sp, color = colors.a) },
                checked = state,
                onCheckedChange = { state = it }
            )
        }

        LabeledSwitch(
            label = { Text("Confirm publishing:", fontSize = 15.sp, color = colors.a) },
            checked = state,
            onCheckedChange = { state = it }
        )

        LabeledSwitch(
            label = { Text("Payload is JSON:", fontSize = 15.sp, color = colors.a) },
            checked = state,
            onCheckedChange = { state = it }
        )

        pointer()
    }

    @Composable
    fun Communication() {
        Communication0()
        Communication1()
    }

    @Composable
    fun Notification() {
        var state by remember { mutableStateOf(true) }

        FrameBox(a = "Notifications and log") {
            Column {
                LabeledSwitch(
                    label = { Text("Log new values:", fontSize = 15.sp, color = colors.a) },
                    checked = state,
                    onCheckedChange = { state = it },
                )

                LabeledSwitch(
                    label = {
                        Text(
                            "Notify on receive:",
                            fontSize = 15.sp,
                            color = colors.a
                        )
                    },
                    checked = state,
                    onCheckedChange = { state = it },
                )

                LabeledCheckbox(
                    label = {
                        Text(
                            "Make notification quiet",
                            fontSize = 15.sp,
                            color = colors.a
                        )
                    },
                    checked = state,
                    onCheckedChange = { state = it },
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }
    }

    @SuppressLint("UnrememberedMutableState")
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun PairList(
        options: MutableList<Pair<String, String>>,
        onRemove: (Int) -> Unit = {},
        onAdd: () -> Unit = {},
        onFirst: (Int, String) -> Unit = { _, _ -> },
        onSecond: (Int, String) -> Unit = { _, _ -> }
    ) {
        var options = remember { options.toMutableStateList() }

        FrameBox(a = "Modes list") {
            Column {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedButton(
                        contentPadding = PaddingValues(13.dp),
                        onClick = {
                            options.add("" to "")
                            onAdd()
                        },
                        border = BorderStroke(0.dp, colors.color),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("ADD OPTION", color = colors.a)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .padding(end = 32.dp, top = 10.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        "ALIAS",
                        fontSize = 13.sp,
                        color = colors.a,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "PAYLOAD",
                        fontSize = 13.sp,
                        color = colors.a,
                        letterSpacing = 2.sp
                    )
                }

                options.forEachIndexed { index, pair ->

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = 5.dp)
                    ) {
                        EditText(
                            label = {},
                            value = pair.first,
                            onValueChange = {
                                options[index] = options[index].copy(first = it)
                                onFirst(index, it)
                            },
                            modifier = Modifier
                                .weight(1f)
                        )

                        EditText(
                            label = {},
                            value = pair.second,
                            onValueChange = {
                                options[index] = options[index].copy(second = it)
                                onSecond(index, it)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        )

                        Icon(
                            painterResource(R.drawable.il_interface_multiply),
                            "",
                            tint = colors.b,
                            modifier = Modifier
                                .padding(start = 10.dp, bottom = 13.dp)
                                .size(30.dp)
                                .nrClickable {
                                    if (options.size > 2) {
                                        options.removeAt(index)
                                        onRemove(index)
                                    }
                                }
                        )
                    }
                }

                //LazyColumn(
                //    modifier = Modifier
                //        .height(500.dp)
                //        .padding(bottom = 16.dp),
                //) {
                //    itemsIndexed(items = opts, key = { i, p -> p.hashCode() }) { index, pair ->
//
                //
                //    }
                //}
            }
        }
    }
}

// Test ------------------------------------------------------------------------------------------

//private object RippleCustomTheme : RippleTheme {
//
//    @Composable
//    override fun defaultColor() =
//        RippleTheme.defaultRippleColor(
//            Color(255, 255, 255),
//            lightTheme = false
//        )
//
//    @Composable
//    override fun rippleAlpha(): RippleAlpha =
//        RippleTheme.defaultRippleAlpha(
//            Color(255, 255, 255),
//            lightTheme = true
//        )
//}

//Column(modifier = Modifier.padding(16.dp)) {
//    OutlinedTextField(
//        value = text,
//        onValueChange = { text = it },
//        label = { Text("Label") }
//    )
//
//    CompositionLocalProvider(LocalRippleTheme provides RippleCustomTheme) {
//        OutlinedButton(
//            onClick = {},
//            border = BorderStroke(0.dp, Color.White),
//            shape = RectangleShape,
//            modifier = Modifier.padding(top = 10.dp)
//        ) {
//            Text("TEST", color = Color.White)
//        }
//    }
//
//    CustomView()
//}

@Composable
fun CustomView() {
    val selectedItem = remember { mutableStateOf(0) }
    AndroidView(
        modifier = Modifier.fillMaxSize(), // Occupy the max size in the Compose UI tree
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.fragment_tile_new, null, false)
            view
        },
        update = { view ->
        }
    )
}