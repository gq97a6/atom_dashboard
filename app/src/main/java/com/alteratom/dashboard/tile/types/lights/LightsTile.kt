import ColorTile.Companion.ColorTypes
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.HSVToColor
import android.graphics.Color.colorToHSV
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
import com.alteratom.R
import com.alteratom.dashboard.objects.DialogBuilder.buildConfirm
import com.alteratom.dashboard.objects.DialogBuilder.dialogSetup
import com.alteratom.dashboard.objects.G.theme
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.dashboard.recycler_view.RecyclerViewItem
import com.alteratom.dashboard.tile.Tile
import com.alteratom.dashboard.toPx
import com.alteratom.databinding.DialogLightsBinding
import com.alteratom.databinding.DialogSelectBinding
import com.fasterxml.jackson.annotation.JsonIgnore
import me.tankery.lib.circularseekbar.CircularSeekBar
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.math.roundToInt

class LightsTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_lights

    @JsonIgnore
    override var typeTag = "lights"

    override var iconKey = "il_business_lightbulb_alt"

    private var hasReceived = MutableLiveData("")

    private var state: Boolean? = null
    private var mode: String? = null
    private var hsvPicked = floatArrayOf(0f, 0f, 0f)
    private var brightness: Int? = null
    private var toRemoves = mutableListOf<String>()
    private var flagIndexes = mutableMapOf<String, Int>()

    val modes = mutableListOf("Solid" to "0", "Blink" to "1", "Breathe" to "2", "Rainbow" to "3")
    val retain = mutableListOf(false, false, false, false) //state, brightness, color, mode

    var iconKeyTrue = "il_interface_toggle_on"
    val iconResTrue: Int
        get() = com.alteratom.dashboard.icon.Icons.icons[iconKeyTrue]?.res
            ?: R.drawable.il_interface_toggle_on

    private var iconKeyFalse = "il_interface_toggle_off"
    val iconResFalse: Int
        get() = com.alteratom.dashboard.icon.Icons.icons[iconKeyFalse]?.res
            ?: R.drawable.il_interface_toggle_off

    var hsvTrue = theme.a.hsv
    val palletTrue: Theme.ColorPallet
        get() = theme.a.getColorPallet(hsvTrue, true)

    private var hsvFalse = floatArrayOf(0f, 0f, 0f)
    val palletFalse: Theme.ColorPallet
        get() = theme.a.getColorPallet(hsvFalse, true)

    private val colorPalletState
        get() = when (state) {
            true -> palletTrue
            false -> palletFalse
            null -> pallet
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

        if (tag.isBlank()) holder.itemView.findViewById<TextView>(R.id.t_tag)?.visibility = GONE
        holder.itemView.findViewById<View>(R.id.t_icon).setBackgroundResource(iconResState)
    }

    override fun onSetTheme(holder: RecyclerViewAdapter.ViewHolder) {
        super.onSetTheme(holder)

        theme.apply(
            holder.itemView as ViewGroup,
            anim = false,
            colorPallet = if (!doPaint) colorPalletState
            else theme.a.getColorPallet(hsvPicked, isRaw = paintRaw)
        )
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        val dialog = Dialog(adapter.context)
        var modeAdapter = RecyclerViewAdapter<RecyclerViewItem>(adapter.context)

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
            colorToHSV(binding.dlPicker.color, hsvPickedTmp)

            (binding.dlColor.background as? GradientDrawable?)?.setStroke(
                10f.toPx(),
                binding.dlPicker.color
            )
        }

        binding.dlPicker.setColorSelectionListener(object :
            com.alteratom.dashboard.color_picker.listeners.SimpleColorSelectionListener() {
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
                send("$brightnessTmp", mqtt.pubs["bright"], retain = retain[1], raw = true)
                if (includePicker) send(
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
                                .replace(
                                    "@hex",
                                    String.format("%02x%02x%02x", c.red, c.green, c.blue)
                                )
                        }
                        ColorTypes.RGB -> {
                            val c = HSVToColor(hsvPickedTmp)
                            (mqtt.payloads[ColorTypes.RGB.name] ?: "")
                                .replace("@r", c.red.toString())
                                .replace("@g", c.green.toString())
                                .replace("@b", c.blue.toString())
                        }
                    },
                    mqtt.pubs["color"],
                    retain = retain[2],
                    raw = true
                )
            }

            if (mqtt.doConfirmPub) {
                with(adapter.context) {
                    buildConfirm("Confirm publishing", "PUBLISH") {
                        send()
                    }
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
                modeAdapter = RecyclerViewAdapter(adapter.context)

                dialog.setContentView(R.layout.dialog_select)
                val binding = DialogSelectBinding.bind(dialog.findViewById(R.id.root))

                modeAdapter.onBindViewHolder = { _, holder, pos ->
                    val text = holder.itemView.findViewById<TextView>(R.id.is_text)
                    text.text = if (showPayload) "${notEmpty[pos].first} (${notEmpty[pos].second})"
                    else notEmpty[pos].first

                    holder.itemView.findViewById<View>(R.id.is_background).let {
                        it.backgroundTintList =
                            ColorStateList.valueOf(theme.a.pallet.color)
                        it.alpha = if (mode == notEmpty[pos].second) 0.15f else 0f
                    }
                }

                modeAdapter.onItemClick = {
                    val pos = modeAdapter.list.indexOf(it)

                    send(
                        this.modes[pos].second,
                        mqtt.pubs["mode"],
                        retain = retain[3]
                    )

                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                    }, 50)
                }

                modeAdapter.setHasStableIds(true)
                modeAdapter.submitList(MutableList(notEmpty.size) {
                    RecyclerViewItem(
                        R.layout.item_select
                    )
                })

                binding.dsRecyclerView.layoutManager = LinearLayoutManager(adapter.context)
                binding.dsRecyclerView.adapter = modeAdapter

                dialog.dialogSetup()
                theme.apply(binding.root)
                dialog.show()
            }
        }

        binding.dlSwitch.setOnClickListener {
            send(
                mqtt.payloads[if (state == false) "true" else "false"] ?: "",
                mqtt.pubs["state"],
                retain = retain[0]
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
        theme.apply(binding.root, anim = false)
        dialog.show()
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        super.onReceive(data, jsonResult)

        fun parse(data: String, field: String?) {
            var hasReceived = true

            when (field) {
                "state" -> {
                    state = when (data) {
                        this.mqtt.payloads["true"] -> true
                        this.mqtt.payloads["false"] -> false
                        else -> null
                    }

                    holder?.itemView?.findViewById<View>(R.id.t_icon)
                        ?.setBackgroundResource(iconResState)

                    holder?.itemView?.let {
                        theme.apply(
                            it as ViewGroup,
                            anim = false,
                            colorPallet = if (!doPaint) colorPalletState
                            else theme.a.getColorPallet(
                                hsvPicked,
                                isRaw = paintRaw
                            )
                        )
                    }
                }
                "color" -> {
                    var data = data

                    toRemoves.forEach { data = data.replace(it, " ") }

                    val split = data.split(" ") as MutableList
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
                                colorPallet = theme.a.getColorPallet(
                                    hsvPicked,
                                    isRaw = paintRaw
                                )
                            )
                        }
                    }
                }
                "bright" -> brightness = data.toIntOrNull()
                "mode" -> (modes.find { it.second == data })?.let {
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
                    this.mqtt.subs["state"] -> "state"
                    this.mqtt.subs["color"] -> "color"
                    this.mqtt.subs["bright"] -> "bright"
                    this.mqtt.subs["mode"] -> "mode"
                    else -> null
                }
            )
        } else for (e in jsonResult) parse(e.value, e.key)
    }
}