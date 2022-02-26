package com.netDashboard.tile.types.color

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.databinding.PopupColorPickerBinding
import com.netDashboard.dialogSetup
import com.netDashboard.globals.G
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage

class ColorTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_color

    var colorType = "rgb"
    var hsvPicked = floatArrayOf(0f, 1f, 1f)

    @JsonIgnore
    override var typeTag = "color"

    var value = ""
        set(value) {
            field = value
            holder?.itemView?.findViewById<TextView>(R.id.tc_value)?.text = value
        }

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

        value = jsonResult["value"] ?: data.second.toString()

        //hsvPicked = when (colorType) {
        //    "hsv" -> {
        //        floatArrayOf(0f, 0f, 0f)
        //    }
        //    "hex" -> {
        //        floatArrayOf(0f, 0f, 0f)
        //    }
        //    "rgb" -> {
        //        floatArrayOf(0f, 0f, 0f)
        //    }
        //    else -> {
        //        floatArrayOf(0f, 0f, 0f)
        //    }
        //}
    }
}