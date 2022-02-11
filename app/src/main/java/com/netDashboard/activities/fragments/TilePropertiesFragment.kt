package com.netDashboard.activities.fragments

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RadioGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.netDashboard.R
import com.netDashboard.Settings
import com.netDashboard.activities.MainActivity
import com.netDashboard.databinding.FragmentTilePropertiesBinding
import com.netDashboard.digitsOnly
import com.netDashboard.globals.G
import com.netDashboard.globals.G.dashboard
import com.netDashboard.globals.G.getIconColorPallet
import com.netDashboard.globals.G.getIconHSV
import com.netDashboard.globals.G.getIconRes
import com.netDashboard.globals.G.setIconHSV
import com.netDashboard.globals.G.setIconRes
import com.netDashboard.globals.G.settings
import com.netDashboard.globals.G.tile
import com.netDashboard.tile.types.button.TextTile
import com.netDashboard.tile.types.slider.SliderTile
import com.netDashboard.tile.types.switch.SwitchTile

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
        viewConfig()

        b.tpTag.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.tag = cs.toString()
            }
        })

        b.tpMqttSwitch.setOnCheckedChangeListener { _, state ->
            mqttSwitchOnCheckedChangeListener(state)
        }

        b.tpMqttArrow.setOnClickListener {
            switchMqttTab(!b.tpMqtt.isVisible)
        }

        b.tpMqttPub.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.mqttData.pubs["base"] = cs.toString()
                dashboard.dg?.mqttd?.notifyOptionsChanged()
            }
        })

        b.tpMqttSub.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.mqttData.subs["base"] = cs.toString()
                dashboard.dg?.mqttd?.notifyOptionsChanged()
            }
        })

        b.tpMqttPayload.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                tile.mqttData.payloads["base"] = cs.toString()
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
            dashboard.dg?.mqttd?.notifyOptionsChanged()
        }

        //val fragment = TileIconFragment()
        //fragment.apply {
        //    arguments = Bundle().apply {
        //        putInt("index", dashboard.tiles.indexOf(tile))
        //    }
        //}
        //(activity as MainActivity).fm.replaceWith(fragment)

        b.tpEditIcon.setOnClickListener {
            getIconHSV = { tile.hsv }
            getIconRes = { tile.iconRes }
            getIconColorPallet = { tile.colorPallet }

            setIconHSV = { hsv -> tile.hsv = hsv }
            setIconRes = { res -> tile.iconRes = res }

            (activity as MainActivity).fm.replaceWith(TileIconFragment())
        }

        b.tpNotSwitch.setOnCheckedChangeListener { _, state ->
            tile.mqttData.doNotify = state
            b.tpNotSilent.visibility = if (state) VISIBLE else GONE
        }

        b.tpNotSilentSwitch.setOnCheckedChangeListener { _, state ->
            tile.mqttData.silentNotify = state
        }

        when (tile) {
            is TextTile -> {
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
            }

            is SliderTile -> {
                b.tpSliderFrom.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(cs: Editable) {}
                    override fun beforeTextChanged(
                        cs: CharSequence,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        cs: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
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

                    override fun onTextChanged(
                        cs: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
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

                    override fun onTextChanged(
                        cs: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
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

            is SwitchTile -> {
                b.tpMqttPayloadTrueEditIcon.setOnClickListener {
                    getIconHSV = { (tile as SwitchTile).hsvTrue }
                    getIconRes = { (tile as SwitchTile).iconResTrue }
                    getIconColorPallet = { (tile as SwitchTile).colorPalletTrue }

                    setIconHSV = { hsv -> (tile as SwitchTile).hsvTrue = hsv }
                    setIconRes = { res -> (tile as SwitchTile).iconResTrue = res }

                    (activity as MainActivity).fm.replaceWith(TileIconFragment())
                }

                b.tpMqttPayloadFalseEditIcon.setOnClickListener {
                    getIconHSV = { (tile as SwitchTile).hsvFalse }
                    getIconRes = { (tile as SwitchTile).iconResFalse }
                    getIconColorPallet = { (tile as SwitchTile).colorPalletFalse }

                    setIconHSV = { hsv -> (tile as SwitchTile).hsvFalse = hsv }
                    setIconRes = { res -> (tile as SwitchTile).iconResFalse = res }

                    (activity as MainActivity).fm.replaceWith(TileIconFragment())
                }

                b.tpMqttPayloadTrue.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(cs: Editable) {}
                    override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                        tile.mqttData.payloads["true"] = cs.toString()
                    }
                })

                b.tpMqttPayloadFalse.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(cs: Editable) {}
                    override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                        tile.mqttData.payloads["false"] = cs.toString()
                    }
                })
            }
        }
    }

    private fun viewConfig() {

        b.tpTag.setText(tile.tag)
        b.tpIcon.setBackgroundResource(tile.iconRes)

        b.tpIcon.backgroundTintList = ColorStateList.valueOf(tile.colorPallet.color)
        val drawable = b.tpIconFrame.background as? GradientDrawable
        drawable?.mutate()
        drawable?.setStroke(1, tile.colorPallet.color)
        drawable?.cornerRadius = 15f

        //MQTT
        b.tpMqttSwitch.isChecked = tile.mqttData.isEnabled
        b.tpMqttPub.setText(tile.mqttData.pubs["base"])
        b.tpMqttSub.setText(tile.mqttData.subs["base"])
        b.tpMqttPayload.setText(tile.mqttData.payloads["base"] ?: "")

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

        switchMqttTab(settings.mqttTabShow, 0)

        b.tpNotSwitch.isChecked = tile.mqttData.doNotify
        b.tpNotSilentSwitch.isChecked = tile.mqttData.silentNotify

        when (tile) {
            is SliderTile -> {
                b.tpSlider.visibility = VISIBLE
                b.tpSliderDrag.isChecked = (tile as SliderTile).dragCon
                b.tpSliderFrom.setText((tile as SliderTile).from.toString())
                b.tpSliderTo.setText((tile as SliderTile).to.toString())
                b.tpSliderStep.setText((tile as SliderTile).step.toString())
                b.tpPayloadHint.visibility = VISIBLE
            }
            is TextTile -> {
                //b.tpText.visibility = VISIBLE
                b.tpMqttPayloadTypeBox.visibility = VISIBLE
                b.tpPayloadType.check(
                    if (tile.mqttData.varPayload) {
                        b.tpMqttPayloadBox.visibility = GONE
                        R.id.tp_mqtt_payload_var
                    } else R.id.tp_mqtt_payload_val
                )
            }

            is SwitchTile -> {
                b.tpMqttPayloadBox.visibility = GONE
                b.tpMqttPayloadsBox.visibility = VISIBLE

                b.tpMqttPayloadTrue.setText(tile.mqttData.payloads["true"] ?: "")
                b.tpMqttPayloadTrueIcon.setBackgroundResource((tile as SwitchTile).iconResTrue)
                b.tpMqttPayloadTrueIcon.backgroundTintList =
                    ColorStateList.valueOf((tile as SwitchTile).colorPalletTrue.color)

                val drawableTrue = b.tpMqttPayloadTrueIconFrame.background as? GradientDrawable
                drawableTrue?.mutate()
                drawableTrue?.setStroke(1, (tile as SwitchTile).colorPalletTrue.color)
                drawableTrue?.cornerRadius = 15f


                b.tpMqttPayloadFalse.setText(tile.mqttData.payloads["false"] ?: "")
                b.tpMqttPayloadFalseIcon.setBackgroundResource((tile as SwitchTile).iconResFalse)
                b.tpMqttPayloadFalseIcon.backgroundTintList =
                    ColorStateList.valueOf((tile as SwitchTile).colorPalletFalse.color)

                val drawableFalse = b.tpMqttPayloadFalseIconFrame.background as? GradientDrawable
                drawableFalse?.mutate()
                drawableFalse?.setStroke(1, (tile as SwitchTile).colorPalletFalse.color)
                drawableFalse?.cornerRadius = 15f
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