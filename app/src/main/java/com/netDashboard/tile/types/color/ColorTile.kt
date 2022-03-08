package com.netDashboard.tile.types.color

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.HSVToColor
import android.graphics.Color.colorToHSV
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.color_picker.listeners.SimpleColorSelectionListener
import com.netDashboard.databinding.DialogColorPickerBinding
import com.netDashboard.dialogSetup
import com.netDashboard.globals.G.theme
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage


class ColorTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_color

    @JsonIgnore
    override var typeTag = "color"

    override var iconKey = "il_design_palette"

    var paintRaw = true
    var hsvPicked = floatArrayOf(0f, 0f, 0f)
    var toRemoves = mutableMapOf<String, MutableList<String>>()
    var flagIndexes = mutableMapOf<String, Int>()

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

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        if (tag.isBlank()) holder.itemView.findViewById<TextView>(R.id.t_tag)?.visibility =
            View.GONE
    }

    override fun onCreateTile() {
        super.onCreateTile()

        mqttData.payloads["hsv"] = "@h;@s;@v"
        mqttData.payloads["hex"] = "#@hex"
        mqttData.payloads["rgb"] = "@r;@g;@b"

        colorToHSV(theme.a.colorPallet.color, hsvPicked)
        colorType = colorType
    }

    override fun onSetTheme(holder: RecyclerViewAdapter.ViewHolder) {
        super.onSetTheme(holder)

        theme.apply(
            holder.itemView as ViewGroup,
            anim = false,
            colorPallet = theme.a.getColorPallet(hsvPicked, isRaw = paintRaw)
        )
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        if (mqttData.pubs["base"].isNullOrEmpty()) return
        if (dashboard.dg?.mqttd?.client?.isConnected != true) return

        val dialog = Dialog(adapter.context)
        dialog.setContentView(R.layout.dialog_color_picker)
        val binding = DialogColorPickerBinding.bind(dialog.findViewById(R.id.root))

        fun onColorChange() {
            colorToHSV(binding.dcpPicker.color, hsvPicked)
            binding.dcpColor.backgroundTintList =
                ColorStateList.valueOf(binding.dcpPicker.color)
        }

        binding.dcpPicker.setColor(HSVToColor(hsvPicked))
        onColorChange()

        binding.dcpPicker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                onColorChange()
            }
        })

        binding.dcpConfirm.setOnClickListener {
            send(
                when (colorType) {
                    "hsv" -> {
                        (mqttData.payloads["hsv"] ?: "")
                            .replace("@h", hsvPicked[0].toInt().toString())
                            .replace("@s", (hsvPicked[1] * 100).toInt().toString())
                            .replace("@v", (hsvPicked[2] * 100).toInt().toString())
                    }
                    "hex" -> {
                        val c = HSVToColor(hsvPicked)
                        (mqttData.payloads["hex"] ?: "")
                            .replace("@hex", String.format("%02x%02x%02x", c.red, c.green, c.blue))
                    }
                    "rgb" -> {
                        val c = HSVToColor(hsvPicked)
                        (mqttData.payloads["rgb"] ?: "")
                            .replace("@r", c.red.toString())
                            .replace("@g", c.green.toString())
                            .replace("@b", c.blue.toString())
                    }
                    else -> {
                        val c = HSVToColor(hsvPicked)
                        (mqttData.payloads["hex"] ?: "")
                            .replace("@hex", String.format("%02x%02x%02x", c.red, c.green, c.blue))
                    }
                }, mqttData.qos
            )

            dialog.dismiss()
        }

        binding.dcpDeny.setOnClickListener {
            dialog.dismiss()
        }

        dialog.dialogSetup()
        theme.apply(binding.root)
        dialog.show()
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        super.onReceive(data, jsonResult)

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

                    colorToHSV(Integer.parseInt(value, 16), hsvPicked)
                }
                "rgb" -> {
                    toRemoves["rgb"]?.forEach {
                        value = value.replace(it, " ")
                    }

                    val r = value.split(" ") as MutableList
                    r.removeIf { it.isEmpty() }

                    colorToHSV(
                        Color.rgb(
                            r[flagIndexes["r"]!!].toInt(),
                            r[flagIndexes["g"]!!].toInt(),
                            r[flagIndexes["b"]!!].toInt()
                        ), hsvPicked
                    )
                }
            }

            holder?.itemView?.let {
                theme.apply(
                    it as ViewGroup,
                    anim = false,
                    colorPallet = theme.a.getColorPallet(hsvPicked, isRaw = paintRaw)
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}