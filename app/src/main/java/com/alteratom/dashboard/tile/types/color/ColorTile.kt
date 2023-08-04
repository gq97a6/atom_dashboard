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
import com.alteratom.R
import com.alteratom.dashboard.objects.DialogBuilder.dialogSetup
import com.alteratom.dashboard.objects.G.theme
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.dashboard.tile.Tile
import com.alteratom.databinding.DialogColorPickerBinding
import com.fasterxml.jackson.annotation.JsonIgnore


class ColorTile : Tile() {

    companion object {
        enum class ColorTypes { HSV, HEX, RGB }
    }

    @JsonIgnore
    override val layout = R.layout.tile_color

    @JsonIgnore
    override var typeTag = "color"

    override var iconKey = "il_design_palette"

    var paintRaw = true
    var doPaint = true
    private var hsvPicked = floatArrayOf(0f, 0f, 0f)
    private var toRemoves = mutableListOf<String>()
    private var flagIndexes = mutableMapOf<String, Int>()

    var colorType = ColorTypes.HSV
        set(value) {
            field = value

            mqtt.payloads[colorType.name]?.let { pattern ->
                when (colorType) {
                    ColorTypes.HSV -> {
                        toRemoves = Regex("@[hsv]").split(pattern).toMutableList()

                        val indexes = Regex("(?<=@)[hsv]").findAll(pattern).map { it.value }
                        flagIndexes["h"] = indexes.indexOf("h")
                        flagIndexes["s"] = indexes.indexOf("s")
                        flagIndexes["v"] = indexes.indexOf("v")
                    }

                    ColorTypes.HEX -> toRemoves = Regex("@hex").split(pattern).toMutableList()
                    ColorTypes.RGB -> {
                        toRemoves = Regex("@[rgb]").split(pattern).toMutableList()

                        val indexes = Regex("(?<=@)[rgb]").findAll(pattern).map { it.value }
                        flagIndexes["r"] = indexes.indexOf("r")
                        flagIndexes["g"] = indexes.indexOf("g")
                        flagIndexes["b"] = indexes.indexOf("b")
                    }
                }

                toRemoves.removeIf { it.isEmpty() }
            }
        }

    override fun onCreateTile() {
        super.onCreateTile()

        mqtt.payloads[ColorTypes.HSV.name] = "@h;@s;@v"
        mqtt.payloads[ColorTypes.HEX.name] = "#@hex"
        mqtt.payloads[ColorTypes.RGB.name] = "@r;@g;@b"

        colorToHSV(theme.a.pallet.color, hsvPicked)
        colorType = colorType
    }

    override fun onBindViewHolder(
        holder: RecyclerViewAdapter.ViewHolder,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)

        if (tag.isBlank()) holder.itemView.findViewById<TextView>(R.id.t_tag)?.visibility =
            View.GONE
    }

    override fun onSetTheme(holder: RecyclerViewAdapter.ViewHolder) {
        super.onSetTheme(holder)

        if (doPaint) {
            theme.apply(
                holder.itemView as ViewGroup,
                anim = false,
                colorPallet = theme.a.getColorPallet(hsvPicked, isRaw = paintRaw)
            )
        }
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        val dialog = Dialog(adapter.context)
        dialog.setContentView(R.layout.dialog_color_picker)
        val binding = DialogColorPickerBinding.bind(dialog.findViewById(R.id.root))

        val hsvPickedTmp = floatArrayOf(hsvPicked[0], hsvPicked[1], hsvPicked[2])

        fun onColorChange() {
            colorToHSV(binding.dcpPicker.color, hsvPickedTmp)
            binding.dcpColor.backgroundTintList =
                ColorStateList.valueOf(binding.dcpPicker.color)
        }

        binding.dcpPicker.setColor(HSVToColor(hsvPickedTmp))
        onColorChange()

        binding.dcpPicker.setColorSelectionListener(object :
            com.alteratom.dashboard.color_picker.listeners.SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                onColorChange()
            }
        })

        binding.dcpConfirm.setOnClickListener {
            send(
                when (colorType) {
                    ColorTypes.HSV -> {
                        (mqtt.payloads[ColorTypes.HSV.name] ?: "")
                            .replace("@h", hsvPickedTmp[0].toInt().toString())
                            .replace("@s", (hsvPickedTmp[1] * 100).toInt().toString())
                            .replace("@v", (hsvPickedTmp[2] * 100).toInt().toString())
                    }

                    ColorTypes.HEX -> {
                        val c = HSVToColor(hsvPickedTmp)
                        (mqtt.payloads[ColorTypes.HEX.name] ?: "")
                            .replace("@hex", String.format("%02x%02x%02x", c.red, c.green, c.blue))
                    }

                    ColorTypes.RGB -> {
                        val c = HSVToColor(hsvPickedTmp)
                        (mqtt.payloads[ColorTypes.RGB.name] ?: "")
                            .replace("@r", c.red.toString())
                            .replace("@g", c.green.toString())
                            .replace("@b", c.blue.toString())
                    }
                }
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
        data: Pair<String, String>,
        jsonResult: MutableMap<String, String>
    ) {
        super.onReceive(data, jsonResult)

        var payload = jsonResult["base"] ?: data.second

        toRemoves.forEach { payload = payload.replace(it, " ") }

        val split = payload.split(" ") as MutableList
        split.removeIf { it.isEmpty() }

        when (colorType) {
            ColorTypes.HEX -> colorToHSV(Integer.parseInt(split[0], 16), hsvPicked)
            ColorTypes.HSV -> {
                hsvPicked = floatArrayOf(
                    split[flagIndexes["h"] ?: 0].toFloat(),
                    split[flagIndexes["s"] ?: 0].toFloat() / 100,
                    split[flagIndexes["v"] ?: 0].toFloat() / 100
                )
                run {}
            }

            ColorTypes.RGB -> {
                colorToHSV(
                    Color.rgb(
                        split[flagIndexes["r"] ?: 0].toInt(),
                        split[flagIndexes["g"] ?: 0].toInt(),
                        split[flagIndexes["b"] ?: 0].toInt()
                    ), hsvPicked
                )
            }
        }

        if (doPaint) {
            holder?.itemView?.let {
                theme.apply(
                    it as ViewGroup,
                    anim = false,
                    colorPallet = theme.a.getColorPallet(hsvPicked, isRaw = paintRaw)
                )
            }
        }
    }
}