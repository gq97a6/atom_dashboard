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
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.isVisible
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
import com.netDashboard.globals.G.setIconRes
import com.netDashboard.globals.G.settings
import com.netDashboard.globals.G.tile
import com.netDashboard.recycler_view.GenericAdapter
import com.netDashboard.recycler_view.GenericItem
import com.netDashboard.tile.types.button.TextTile
import com.netDashboard.tile.types.pick.SelectTile
import com.netDashboard.tile.types.slider.SliderTile
import com.netDashboard.tile.types.switch.SwitchTile
import com.netDashboard.tile.types.terminal.TerminalTile

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
                val tile = tile as SliderTile

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
                                    tile.from = it
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
                                    tile.to = it
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
                                    tile.step = it
                                }
                            }
                        }
                    }
                })

                b.tpSliderDrag.setOnCheckedChangeListener { _, state ->
                    tile.dragCon = state
                }
            }

            is SwitchTile -> {
                val tile = tile as SwitchTile
                b.tpMqttPayloadTrueEditIcon.setOnClickListener {
                    getIconHSV = { tile.hsvTrue }
                    getIconRes = { tile.iconResTrue }
                    getIconColorPallet = { tile.colorPalletTrue }

                    setIconHSV = { hsv -> tile.hsvTrue = hsv }
                    setIconRes = { res -> tile.iconResTrue = res }

                    (activity as MainActivity).fm.replaceWith(TileIconFragment())
                }

                b.tpMqttPayloadFalseEditIcon.setOnClickListener {
                    getIconHSV = { tile.hsvFalse }
                    getIconRes = { tile.iconResFalse }
                    getIconColorPallet = { tile.colorPalletFalse }

                    setIconHSV = { hsv -> tile.hsvFalse = hsv }
                    setIconRes = { res -> tile.iconResFalse = res }

                    (activity as MainActivity).fm.replaceWith(TileIconFragment())
                }

                b.tpMqttPayloadTrue.addTextChangedListener(object : TextWatcher {
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
                        tile.mqttData.payloads["true"] = cs.toString()
                    }
                })

                b.tpMqttPayloadFalse.addTextChangedListener(object : TextWatcher {
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
                        tile.mqttData.payloads["false"] = cs.toString()
                    }
                })
            }

            is SelectTile -> {
                val tile = tile as SelectTile
                val adapter = GenericAdapter(requireContext())

                val list = MutableList(tile.list.size) {
                    GenericItem(R.layout.item_select_add)
                }

                adapter.setHasStableIds(true)
                adapter.onBindViewHolder = { item, holder, pos ->
                    val a = holder.itemView.findViewById<TextView>(R.id.isa_alias)
                    val p = holder.itemView.findViewById<TextView>(R.id.isa_payload)
                    val r = holder.itemView.findViewById<Button>(R.id.isa_remove)

                    a.text = tile.list[pos].first
                    p.text = tile.list[pos].second

                    a.addTextChangedListener(object : TextWatcher {
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
                            tile.list[pos] = Pair(cs.toString(), tile.list[pos].second)
                        }
                    })

                    p.addTextChangedListener(object : TextWatcher {
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
                            tile.list[pos] = Pair(tile.list[pos].first, cs.toString())
                        }
                    })

                    r.setOnClickListener {
                        adapter.list.indexOf(item).let {
                            if (it >= 0) {
                                tile.list.removeAt(it)
                                adapter.removeItemAt(it)
                            }
                        }
                    }
                }

                b.tpSelectList.layoutManager = LinearLayoutManager(requireContext())
                b.tpSelectList.adapter = adapter

                adapter.submitList(list)

                b.tpSelectAdd.setOnClickListener {
                    tile.list.add(Pair("", ""))
                    list.add(GenericItem(R.layout.item_select_add))
                    adapter.notifyItemInserted(list.size - 1)
                }

                b.tpSelectShowPayload.setOnCheckedChangeListener { _, state ->
                    tile.showPayload = state
                }
            }

            is TerminalTile -> {
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
                val tile = tile as SliderTile

                b.tpSlider.visibility = VISIBLE
                b.tpSliderDrag.isChecked = tile.dragCon
                b.tpSliderFrom.setText(tile.from.toString())
                b.tpSliderTo.setText(tile.to.toString())
                b.tpSliderStep.setText(tile.step.toString())
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
                val tile = tile as SwitchTile

                b.tpMqttPayloadBox.visibility = GONE
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
            }

            is SelectTile -> {
                val tile = tile as SelectTile

                b.tpSelectShowPayload.isChecked = tile.showPayload
            }

            is TerminalTile -> {
                val tile = tile as TerminalTile

                b.tpMqttPayloadTypeBox.visibility = VISIBLE
                b.tpPayloadType.check(
                    if (tile.mqttData.varPayload) {
                        b.tpMqttPayloadBox.visibility = GONE
                        R.id.tp_mqtt_payload_var
                    } else R.id.tp_mqtt_payload_val
                )
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