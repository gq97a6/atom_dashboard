package com.netDashboard.tile.types.color

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.databinding.PopupColorPickerBinding
import com.netDashboard.dialogSetup
import com.netDashboard.globals.G
import com.netDashboard.globals.G.theme
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage

class ColorTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_color

    var colorType = "rgb"
        set(value) {
            field = value


            fun build(flag: String, pattern: String, reg: String = flag) {

            }

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

            //find flags
            //flag to index
            //to remove array

        }

    var hsvPicked = floatArrayOf(0f, 1f, 1f)
    var toRemoves = mutableMapOf<String, MutableList<String>>()
    var flagIndexes = mutableMapOf<String, Int>()

    @JsonIgnore
    override var typeTag = "color"

    override fun onCreateTile() {
        super.onCreateTile()

        mqttData.payloads["hsv"] = "@h;@s;@v"
        mqttData.payloads["hex"] = "#@hex"
        mqttData.payloads["rgb"] = "@r;@g;@b"
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        if (mqttData.pubs["base"].isNullOrEmpty()) return
        if (dashboard.dg?.mqttd?.client?.isConnected != true) return

        val dialog = Dialog(adapter.context)
        dialog.setContentView(R.layout.popup_color_picker)
        val binding = PopupColorPickerBinding.bind(dialog.findViewById(R.id.root))

        fun onColorChange() {
            hsvPicked = floatArrayOf(
                binding.pcpHue.value,
                binding.pcpSaturation.value,
                binding.pcpValue.value
            )

            binding.pcpColor.backgroundTintList =
                ColorStateList.valueOf(Color.HSVToColor(hsvPicked))
        }

        //var colorPickedNow = floatArrayOf(0f, 0f, 0f)
        binding.pcpHue.value = hsvPicked[0]
        binding.pcpSaturation.value = hsvPicked[1]
        binding.pcpValue.value = hsvPicked[2]
        onColorChange()

        binding.pcpHue.setOnTouchListener { _, e ->
            onColorChange()
            return@setOnTouchListener e.action == KeyEvent.ACTION_UP
        }

        binding.pcpSaturation.setOnTouchListener { _, e ->
            onColorChange()
            return@setOnTouchListener e.action == KeyEvent.ACTION_UP
        }

        binding.pcpValue.setOnTouchListener { _, e ->
            onColorChange()
            return@setOnTouchListener e.action == KeyEvent.ACTION_UP
        }

        binding.pcpConfirm.setOnClickListener {
            send(
                when (colorType) {
                    "hsv" -> {
                        (mqttData.payloads["hsv"] ?: "")
                            .replace("@h", hsvPicked[0].toInt().toString())
                            .replace("@s", (hsvPicked[1] * 100).toInt().toString())
                            .replace("@v", (hsvPicked[2] * 100).toInt().toString())
                    }
                    "hex" -> {
                        val c = Color.HSVToColor(hsvPicked)
                        (mqttData.payloads["hex"] ?: "")
                            .replace("@hex", String.format("%02x%02x%02x", c.red, c.green, c.blue))
                    }
                    "rgb" -> {
                        val c = Color.HSVToColor(hsvPicked)
                        (mqttData.payloads["rgb"] ?: "")
                            .replace("@r", c.red.toString())
                            .replace("@g", c.green.toString())
                            .replace("@b", c.blue.toString())
                    }
                    else -> {
                        val c = Color.HSVToColor(hsvPicked)
                        (mqttData.payloads["hex"] ?: "")
                            .replace("@hex", String.format("%02x%02x%02x", c.red, c.green, c.blue))
                    }
                }, mqttData.qos
            )

            dialog.dismiss()
        }

        binding.pcpDeny.setOnClickListener {
            dialog.dismiss()
        }

        binding.padding.setOnClickListener {
            dialog.dismiss()
        }

        dialog.dialogSetup()
        G.theme.apply(binding.root)
        dialog.show()
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        super.onReceive(data, jsonResult)

        var value = jsonResult["value"] ?: data.second.toString()

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

            holder?.itemView?.let {
                theme.apply(
                    it as ViewGroup,
                    anim = false,
                    colorPallet = theme.a.getColorPallet(hsvPicked)
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}