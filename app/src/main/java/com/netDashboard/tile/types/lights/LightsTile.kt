package com.netDashboard.tile.types.lights

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.DialogBuilder.buildConfirm
import com.netDashboard.DialogBuilder.dialogSetup
import com.netDashboard.R
import com.netDashboard.Theme
import com.netDashboard.color_picker.listeners.SimpleColorSelectionListener
import com.netDashboard.createToast
import com.netDashboard.databinding.DialogLightsBinding
import com.netDashboard.databinding.DialogSelectBinding
import com.netDashboard.globals.G
import com.netDashboard.icon.Icons
import com.netDashboard.recycler_view.GenericAdapter
import com.netDashboard.recycler_view.GenericItem
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.tile.Tile
import com.netDashboard.toPx
import me.tankery.lib.circularseekbar.CircularSeekBar
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.math.roundToInt

class LightsTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_lights

    @JsonIgnore
    override var typeTag = "lights"

    override var iconKey = "il_business_lightbulb_alt"

    var state: Boolean? = null
    var mode: String? = null
    var hsvPicked = floatArrayOf(0f, 0f, 0f)
    var brightness: Int? = null
    var toRemoves = mutableMapOf<String, MutableList<String>>()
    var flagIndexes = mutableMapOf<String, Int>()

    val modes = mutableListOf("Solid" to "0", "Blink" to "1", "Breathe" to "2", "Rainbow" to "3")
    val retain = mutableListOf(false, false, false, false) //state, color, brightness, mode

    var iconKeyTrue = "il_interface_toggle_on"
    val iconResTrue: Int
        get() = Icons.icons[iconKeyTrue]?.res ?: R.drawable.il_interface_toggle_on

    var iconKeyFalse = "il_interface_toggle_off"
    val iconResFalse: Int
        get() = Icons.icons[iconKeyFalse]?.res ?: R.drawable.il_interface_toggle_off

    var hsvTrue = floatArrayOf(179f, 1f, 1f)
    val colorPalletTrue: Theme.ColorPallet
        get() = G.theme.a.getColorPallet(hsvTrue, true)

    var hsvFalse = floatArrayOf(0f, 0f, 0f)
    val colorPalletFalse: Theme.ColorPallet
        get() = G.theme.a.getColorPallet(hsvFalse, true)

    private val colorPalletState
        get() = when (state) {
            true -> colorPalletTrue
            false -> colorPalletFalse
            null -> colorPallet
        }

    var includePicker = false
    var showPayload = false
    var paintRaw = true
    var doPaint = true

    var colorType = "hex"
        set(value) {
            field = value

            mqttData.payloads[colorType]?.let { pattern ->
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

        mqttData.payloads["hsv"] = "@h;@s;@v"
        mqttData.payloads["hex"] = "#@hex"
        mqttData.payloads["rgb"] = "@r;@g;@b"

        Color.colorToHSV(G.theme.a.colorPallet.color, hsvPicked)
        colorType = colorType
    }

    override fun onSetTheme(holder: RecyclerViewAdapter.ViewHolder) {
        super.onSetTheme(holder)

        G.theme.apply(
            holder.itemView as ViewGroup,
            anim = false,
            colorPallet = if (!doPaint) colorPalletState
            else G.theme.a.getColorPallet(hsvPicked, isRaw = paintRaw)
        )
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        if (tag.isBlank()) holder.itemView.findViewById<TextView>(R.id.t_tag)?.visibility = GONE
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {

        if (jsonResult.isEmpty()) {
            val value = data.second.toString().toFloatOrNull()
            when (data.first) {
                mqttData.subs["state"] -> value?.let {
                }
                mqttData.subs["color"] -> value?.let {
                }
                mqttData.subs["bright"] -> value?.let {
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
                    "state" -> value?.let { temp = it }
                    "color" -> value?.let { tempSetpoint = it }
                    "bright" -> value?.let { humi = it }
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

        var value = jsonResult["base"] ?: data.second.toString()

        try {
            when (colorType) {
                "hsv" -> {
                    toRemoves["hsv"]?.forEach {
                        value = value.replace(it, " ")
                    }

                    val r = value.split(" ") as MutableList
                    r.removeIf { it.isEmpty() }

                    hsvPicked = floatArrayOf(
                        r[flagIndexes["h"]!!].toFloat(),
                        r[flagIndexes["s"]!!].toFloat() / 100,
                        r[flagIndexes["v"]!!].toFloat() / 100
                    )
                }
                "hex" -> {
                    toRemoves["hex"]?.forEach {
                        value = value.replace(it, "")
                    }

                    Color.colorToHSV(Integer.parseInt(value, 16), hsvPicked)
                }
                "rgb" -> {
                    toRemoves["rgb"]?.forEach {
                        value = value.replace(it, " ")
                    }

                    val r = value.split(" ") as MutableList
                    r.removeIf { it.isEmpty() }

                    Color.colorToHSV(
                        Color.rgb(
                            r[flagIndexes["r"]!!].toInt(),
                            r[flagIndexes["g"]!!].toInt(),
                            r[flagIndexes["b"]!!].toInt()
                        ), hsvPicked
                    )
                }
            }

            if (doPaint) {
                holder?.itemView?.let {
                    G.theme.apply(
                        it as ViewGroup,
                        anim = false,
                        colorPallet = G.theme.a.getColorPallet(hsvPicked, isRaw = paintRaw)
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        if (mqttData.pubs["base"].isNullOrEmpty()) return
        if (dashboard.dg?.mqttd?.client?.isConnected != true) return

        val dialog = Dialog(adapter.context)
        dialog.setContentView(R.layout.dialog_lights)
        val binding = DialogLightsBinding.bind(dialog.findViewById(R.id.root))

        binding.dlPicker.visibility = if (includePicker) VISIBLE else GONE
        binding.dlBright.startAngle = if (includePicker) 30f else 150f
        binding.dlBright.endAngle = if (includePicker) 150f else 30f

        val param = binding.dlBright.layoutParams as ViewGroup.MarginLayoutParams
        (if (includePicker) 10.toPx() else 40.toPx()).let {
            param.setMargins(it, it, it, it)
        }
        binding.dlBright.layoutParams = param


        var hsvPickedTmp = floatArrayOf(hsvPicked[0], hsvPicked[1], hsvPicked[2])
        var brightnessTmp = brightness

        fun onColorChange() {
            Color.colorToHSV(binding.dlPicker.color, hsvPickedTmp)

            (binding.dlColor.background as? GradientDrawable?)?.setStroke(
                10f.toPx(),
                binding.dlPicker.color
            )
        }

        binding.dlPicker.setColorSelectionListener(object : SimpleColorSelectionListener() {
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

        brightnessTmp?.let { binding.dlBright.progress = it.toFloat() }
        binding.dlPicker.setColor(Color.HSVToColor(hsvPickedTmp))
        onColorChange()

        binding.dlConfirm.setOnClickListener {
            fun send() {
                send("$brightnessTmp", mqttData.pubs["bright"], mqttData.qos, retain[1], true)
                if (includePicker) send(
                    when (colorType) {
                        "hsv" -> {
                            (mqttData.payloads["hsv"] ?: "")
                                .replace("@h", hsvPickedTmp[0].toInt().toString())
                                .replace("@s", (hsvPickedTmp[1] * 100).toInt().toString())
                                .replace("@v", (hsvPickedTmp[2] * 100).toInt().toString())
                        }
                        "hex" -> {
                            val c = HSVToColor(hsvPickedTmp)
                            (mqttData.payloads["hex"] ?: "")
                                .replace(
                                    "@hex",
                                    String.format("%02x%02x%02x", c.red, c.green, c.blue)
                                )
                        }
                        "rgb" -> {
                            val c = HSVToColor(hsvPickedTmp)
                            (mqttData.payloads["rgb"] ?: "")
                                .replace("@r", c.red.toString())
                                .replace("@g", c.green.toString())
                                .replace("@b", c.blue.toString())
                        }
                        else -> {
                            val c = HSVToColor(hsvPickedTmp)
                            (mqttData.payloads["hex"] ?: "")
                                .replace(
                                    "@hex",
                                    String.format("%02x%02x%02x", c.red, c.green, c.blue)
                                )
                        }
                    }
                )
            }

            if (mqttData.confirmPub) {
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
            if (notEmpty.size > 0 && !mqttData.pubs["mode"].isNullOrEmpty()) {

                val dialog = Dialog(adapter.context)
                val adapter = GenericAdapter(adapter.context)

                dialog.setContentView(R.layout.dialog_select)
                val binding = DialogSelectBinding.bind(dialog.findViewById(R.id.root))

                adapter.onBindViewHolder = { _, holder, pos ->
                    val text = holder.itemView.findViewById<TextView>(R.id.is_text)
                    text.text = if (showPayload) "${notEmpty[pos].first} (${notEmpty[pos].second})"
                    else "${notEmpty[pos].first}"

                    if (mode == notEmpty[pos].second) {
                        holder?.itemView?.findViewById<View>(R.id.is_background).let {
                            it.backgroundTintList =
                                ColorStateList.valueOf(G.theme.a.colorPallet.color)
                            it.alpha = 0.15f
                        }
                    }
                }

                adapter.onItemClick = {
                    val pos = adapter.list.indexOf(it)

                    send(
                        "${this.modes[pos].second}",
                        mqttData.pubs["mode"],
                        mqttData.qos,
                        retain[3]
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
            } else createToast(adapter.context, "Check setup")
        }

        dialog.dialogSetup()
        G.theme.apply(binding.root, anim = false)
        dialog.show()
    }
}