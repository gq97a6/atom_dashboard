package com.alteratom.tile.types.lights

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.HSVToColor
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.fasterxml.jackson.annotation.JsonIgnore
import com.alteratom.dashboard.DialogBuilder.buildConfirm
import com.alteratom.dashboard.DialogBuilder.dialogSetup
import com.alteratom.R
import com.alteratom.databinding.DialogLightsBinding
import com.alteratom.databinding.DialogSelectBinding
import com.alteratom.dashboard.toPx
import me.tankery.lib.circularseekbar.CircularSeekBar
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.math.roundToInt

class LightsTile : com.alteratom.dashboard.tile.Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_lights

    @JsonIgnore
    override var typeTag = "lights"

    override var iconKey = "il_business_lightbulb_alt"

    private var hasReceived = MutableLiveData("")

    var state: Boolean? = null
    var mode: String? = null
    private var hsvPicked = floatArrayOf(0f, 0f, 0f)
    private var brightness: Int? = null
    private var toRemoves = mutableMapOf<String, MutableList<String>>()
    private var flagIndexes = mutableMapOf<String, Int>()

    val modes = mutableListOf("Solid" to "0", "Blink" to "1", "Breathe" to "2", "Rainbow" to "3")
    val retain = mutableListOf(false, false, false, false) //state, color, brightness, mode

    var iconKeyTrue = "il_interface_toggle_on"
    val iconResTrue: Int
        get() = com.alteratom.dashboard.icon.Icons.icons[iconKeyTrue]?.res ?: R.drawable.il_interface_toggle_on

    var iconKeyFalse = "il_interface_toggle_off"
    val iconResFalse: Int
        get() = com.alteratom.dashboard.icon.Icons.icons[iconKeyFalse]?.res ?: R.drawable.il_interface_toggle_off

    var hsvTrue = floatArrayOf(179f, 1f, 1f)
    val colorPalletTrue: com.alteratom.dashboard.Theme.ColorPallet
        get() = com.alteratom.dashboard.G.theme.a.getColorPallet(hsvTrue, true)

    var hsvFalse = floatArrayOf(0f, 0f, 0f)
    val colorPalletFalse: com.alteratom.dashboard.Theme.ColorPallet
        get() = com.alteratom.dashboard.G.theme.a.getColorPallet(hsvFalse, true)

    private val colorPalletState
        get() = when (state) {
            true -> colorPalletTrue
            false -> colorPalletFalse
            null -> colorPallet
        }

    private val iconResState
        get() = when (state) {
            true -> {
                iconResTrue
            }
            false -> {
                iconResFalse
            }
            null -> {
                iconRes
            }
        }

    var includePicker = false
    var showPayload = false
    var paintRaw = true
    var doPaint = false

    var colorType = "hex"
        set(value) {
            field = value

            mqtt.payloads[colorType]?.let { pattern ->
                when (colorType) {
                    "hsv" -> {
                        toRemoves["hsv"] = Regex("@[hsv]").split(pattern) as MutableList
                        toRemoves["hsv"]?.removeIf { it.isEmpty() }

                        val indexes = Regex("(?<=@)[hsv]").findAll(pattern).map { it.value }
                        flagIndexes["h"] = indexes.indexOf("h")
                        flagIndexes["s"] = indexes.indexOf("s")
                        flagIndexes["v"] = indexes.indexOf("v")
                    }
                    "hex" -> {
                        toRemoves["hex"] = Regex("@hex").split(pattern) as MutableList
                        toRemoves["hex"]?.removeIf { it.isEmpty() }
                    }
                    "rgb" -> {
                        toRemoves["rgb"] = Regex("@[rgb]").split(pattern) as MutableList
                        toRemoves["rgb"]?.removeIf { it.isEmpty() }

                        val indexes = Regex("(?<=@)[rgb]").findAll(pattern).map { it.value }
                        flagIndexes["r"] = indexes.indexOf("r")
                        flagIndexes["g"] = indexes.indexOf("g")
                        flagIndexes["b"] = indexes.indexOf("b")
                    }
                    else -> {}
                }
            }
        }

    override fun onCreateTile() {
        super.onCreateTile()

        mqtt.payloads["hsv"] = "@h;@s;@v"
        mqtt.payloads["hex"] = "#@hex"
        mqtt.payloads["rgb"] = "@r;@g;@b"

        Color.colorToHSV(com.alteratom.dashboard.G.theme.a.colorPallet.color, hsvPicked)
        colorType = colorType
    }

    override fun onBindViewHolder(holder: com.alteratom.dashboard.recycler_view.RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        if (tag.isBlank()) holder.itemView.findViewById<TextView>(R.id.t_tag)?.visibility = GONE
        holder.itemView.findViewById<View>(R.id.t_icon).setBackgroundResource(iconResState)
    }

    override fun onSetTheme(holder: com.alteratom.dashboard.recycler_view.RecyclerViewAdapter.ViewHolder) {
        super.onSetTheme(holder)

        com.alteratom.dashboard.G.theme.apply(
            holder.itemView as ViewGroup,
            anim = false,
            colorPallet = if (!doPaint) colorPalletState
            else com.alteratom.dashboard.G.theme.a.getColorPallet(hsvPicked, isRaw = paintRaw)
        )
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        val dialog = Dialog(adapter.context)
        var modeAdapter = com.alteratom.dashboard.recycler_view.GenericAdapter(adapter.context)

        dialog.setContentView(R.layout.dialog_lights)
        val binding = DialogLightsBinding.bind(dialog.findViewById(R.id.root))

        val observer: (String) -> Unit = { it ->
            when (it) {
                "state" -> state?.let {
                    binding.dlSwitch.text = if (it) "ON" else "OFF"
                }
                "color" -> binding.dlPicker.setColor(HSVToColor(hsvPicked))
                "bright" -> brightness?.let { binding.dlBright.progress = it.toFloat() }
                "mode" -> modeAdapter.notifyDataSetChanged()
            }
        }

        dialog.setOnDismissListener {
            hasReceived.removeObserver(observer)
        }

        hasReceived.observe(adapter.context as LifecycleOwner, observer)

        binding.dlPicker.visibility = if (includePicker) VISIBLE else GONE
        binding.dlBright.startAngle = if (includePicker) 30f else 150f
        binding.dlBright.endAngle = if (includePicker) 150f else 30f

        val param = binding.dlBright.layoutParams as ViewGroup.MarginLayoutParams
        (if (includePicker) 10.toPx() else 40.toPx()).let {
            param.setMargins(it, it, it, it)
        }
        binding.dlBright.layoutParams = param

        val hsvPickedTmp = floatArrayOf(hsvPicked[0], hsvPicked[1], hsvPicked[2])
        var brightnessTmp = brightness

        fun onColorChange() {
            Color.colorToHSV(binding.dlPicker.color, hsvPickedTmp)

            (binding.dlColor.background as? GradientDrawable?)?.setStroke(
                10f.toPx(),
                binding.dlPicker.color
            )
        }

        binding.dlPicker.setColorSelectionListener(object : com.alteratom.dashboard.color_picker.listeners.SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                onColorChange()
            }
        })

        binding.dlBright.setOnSeekBarChangeListener(object :
            CircularSeekBar.OnCircularSeekBarChangeListener {
            override fun onProgressChanged(
                circularSeekBar: CircularSeekBar?,
                progress: Float,
                fromUser: Boolean
            ) {
                brightnessTmp = progress.roundToInt()
                binding.dlValue.text = brightnessTmp.toString()
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar?) {}
            override fun onStartTrackingTouch(seekBar: CircularSeekBar?) {}
        })

        binding.dlConfirm.setOnClickListener {
            fun send() {
                send("$brightnessTmp", mqtt.pubs["bright"], mqtt.qos, retain[2], true)
                if (includePicker) send(
                    when (colorType) {
                        "hsv" -> {
                            (mqtt.payloads["hsv"] ?: "")
                                .replace("@h", hsvPickedTmp[0].toInt().toString())
                                .replace("@s", (hsvPickedTmp[1] * 100).toInt().toString())
                                .replace("@v", (hsvPickedTmp[2] * 100).toInt().toString())
                        }
                        "hex" -> {
                            val c = HSVToColor(hsvPickedTmp)
                            (mqtt.payloads["hex"] ?: "")
                                .replace(
                                    "@hex",
                                    String.format("%02x%02x%02x", c.red, c.green, c.blue)
                                )
                        }
                        "rgb" -> {
                            val c = HSVToColor(hsvPickedTmp)
                            (mqtt.payloads["rgb"] ?: "")
                                .replace("@r", c.red.toString())
                                .replace("@g", c.green.toString())
                                .replace("@b", c.blue.toString())
                        }
                        else -> {
                            val c = HSVToColor(hsvPickedTmp)
                            (mqtt.payloads["hex"] ?: "")
                                .replace(
                                    "@hex",
                                    String.format("%02x%02x%02x", c.red, c.green, c.blue)
                                )
                        }
                    }, mqtt.pubs["color"], mqtt.qos, retain[1], true
                )
            }

            if (mqtt.confirmPub) {
                with(adapter.context) {
                    buildConfirm("PUBLISH", "Confirm publishing", {
                        send()
                    })
                }
            } else send()

            dialog.dismiss()
        }

        binding.dlDeny.setOnClickListener {
            dialog.dismiss()
        }

        binding.dlMode.setOnClickListener {
            val notEmpty = modes.filter { !(it.first.isEmpty() && it.second.isEmpty()) }
            if (notEmpty.isNotEmpty()) {
                val dialog = Dialog(adapter.context)
                modeAdapter = com.alteratom.dashboard.recycler_view.GenericAdapter(adapter.context)

                dialog.setContentView(R.layout.dialog_select)
                val binding = DialogSelectBinding.bind(dialog.findViewById(R.id.root))

                modeAdapter.onBindViewHolder = { _, holder, pos ->
                    val text = holder.itemView.findViewById<TextView>(R.id.is_text)
                    text.text = if (showPayload) "${notEmpty[pos].first} (${notEmpty[pos].second})"
                    else notEmpty[pos].first

                    holder.itemView.findViewById<View>(R.id.is_background).let {
                        it.backgroundTintList =
                            ColorStateList.valueOf(com.alteratom.dashboard.G.theme.a.colorPallet.color)
                        it.alpha = if (mode == notEmpty[pos].second) 0.15f else 0f
                    }
                }

                modeAdapter.onItemClick = {
                    val pos = modeAdapter.list.indexOf(it)

                    send(
                        this.modes[pos].second,
                        mqtt.pubs["mode"],
                        mqtt.qos,
                        retain[3]
                    )

                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                    }, 50)
                }

                modeAdapter.setHasStableIds(true)
                modeAdapter.submitList(MutableList(notEmpty.size) {
                    com.alteratom.dashboard.recycler_view.GenericItem(
                        R.layout.item_select
                    )
                })

                binding.dsRecyclerView.layoutManager = LinearLayoutManager(adapter.context)
                binding.dsRecyclerView.adapter = modeAdapter

                dialog.dialogSetup()
                com.alteratom.dashboard.G.theme.apply(binding.root)
                dialog.show()
            }
        }

        binding.dlSwitch.setOnClickListener {
            send(
                mqtt.payloads[if (state == false) "true" else "false"] ?: "",
                mqtt.pubs["state"],
                mqtt.qos,
                retain[0]
            )
        }

        state?.let {
            binding.dlSwitch.text = if (it) "ON" else "OFF"
        }

        brightnessTmp?.let {
            binding.dlBright.progress = it.toFloat()
            binding.dlValue.text = brightnessTmp.toString()
        }

        binding.dlPicker.setColor(HSVToColor(hsvPickedTmp))
        onColorChange()

        dialog.dialogSetup()
        com.alteratom.dashboard.G.theme.apply(binding.root, anim = false)
        dialog.show()
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        super.onReceive(data, jsonResult)

        fun parse(value: String, field: String?) {
            var hasReceived = true

            when (field) {
                "state" -> {
                    state = when (value) {
                        mqtt.payloads["true"] -> true
                        mqtt.payloads["false"] -> false
                        else -> null
                    }

                    holder?.itemView?.findViewById<View>(R.id.t_icon)
                        ?.setBackgroundResource(iconResState)

                    holder?.itemView?.let {
                        com.alteratom.dashboard.G.theme.apply(
                            it as ViewGroup,
                            anim = false,
                            colorPallet = if (!doPaint) colorPalletState
                            else com.alteratom.dashboard.G.theme.a.getColorPallet(hsvPicked, isRaw = paintRaw)
                        )
                    }
                }
                "color" -> {
                    var value = value

                    toRemoves[colorType]?.forEach {
                        value = value.replace(it, " ")
                    }

                    val v = value.split(" ") as MutableList
                    v.removeIf { it.isEmpty() }

                    when (colorType) {
                        "hex" -> Color.colorToHSV(Integer.parseInt(v[0], 16), hsvPicked)
                        "hsv" -> {
                            hsvPicked = floatArrayOf(
                                v[flagIndexes["h"]!!].toFloat(),
                                v[flagIndexes["s"]!!].toFloat() / 100,
                                v[flagIndexes["v"]!!].toFloat() / 100
                            )
                        }
                        "rgb" -> {
                            Color.colorToHSV(
                                Color.rgb(
                                    v[flagIndexes["r"]!!].toInt(),
                                    v[flagIndexes["g"]!!].toInt(),
                                    v[flagIndexes["b"]!!].toInt()
                                ), hsvPicked
                            )
                        }
                    }

                    if (doPaint) {
                        holder?.itemView?.let {
                            com.alteratom.dashboard.G.theme.apply(
                                it as ViewGroup,
                                anim = false,
                                colorPallet = com.alteratom.dashboard.G.theme.a.getColorPallet(hsvPicked, isRaw = paintRaw)
                            )
                        }
                    }
                }
                "bright" -> brightness = value.toIntOrNull()
                "mode" -> (modes.find { it.second == value })?.let {
                    mode = it.second
                }
                else -> hasReceived = false
            }

            if (hasReceived) this.hasReceived.postValue(field)
        }

        if (jsonResult.isEmpty()) {
            val value = data.second.toString()
            parse(
                value, when (data.first) {
                    mqtt.subs["state"] -> "state"
                    mqtt.subs["color"] -> "color"
                    mqtt.subs["bright"] -> "bright"
                    mqtt.subs["mode"] -> "mode"
                    else -> null
                }
            )
        } else {
            for (e in jsonResult) {
                parse(e.value, e.key)
            }
        }
    }
}