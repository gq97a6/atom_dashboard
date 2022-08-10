import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alteratom.R
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.tile.Tile
import com.fasterxml.jackson.annotation.JsonIgnore
import org.eclipse.paho.client.mqttv3.MqttMessage

class SwitchTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_switch

    @JsonIgnore
    override var typeTag = "switch"

    override var iconKey = "il_interface_toggle_on"

    var state: Boolean? = false

    var iconKeyTrue = "il_interface_toggle_on"
    val iconResTrue: Int
        get() = com.alteratom.dashboard.icon.Icons.icons[iconKeyTrue]?.res
            ?: R.drawable.il_interface_toggle_on

    var iconKeyFalse = "il_interface_toggle_off"
    val iconResFalse: Int
        get() = com.alteratom.dashboard.icon.Icons.icons[iconKeyFalse]?.res
            ?: R.drawable.il_interface_toggle_off

    var hsvTrue = floatArrayOf(179f, 1f, 1f)
    val palletTrue: com.alteratom.dashboard.Theme.ColorPallet
        get() = theme.a.getColorPallet(hsvTrue, true)

    var hsvFalse = floatArrayOf(0f, 0f, 0f)
    val palletFalse: com.alteratom.dashboard.Theme.ColorPallet
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

    override fun onSetTheme(holder: com.alteratom.dashboard.recycler_view.RecyclerViewAdapter.ViewHolder) {
        theme.apply(
            holder.itemView as ViewGroup,
            anim = false,
            colorPallet = colorPalletState
        )
    }

    override fun onBindViewHolder(
        holder: com.alteratom.dashboard.recycler_view.RecyclerViewAdapter.ViewHolder,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)

        if (tag.isBlank()) holder.itemView.findViewById<TextView>(R.id.t_tag)?.visibility =
            View.GONE
        holder.itemView.findViewById<View>(R.id.t_icon)?.setBackgroundResource(iconResState)
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        send(mqttData.payloads[if (state == false) "true" else "false"] ?: "")
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        super.onReceive(data, jsonResult)

        state = when (data.second.toString()) {
            mqttData.payloads["true"] -> true
            mqttData.payloads["false"] -> false
            else -> null
        }

        holder?.itemView?.findViewById<View>(R.id.t_icon)?.setBackgroundResource(iconResState)

        holder?.itemView?.let {
            theme.apply(
                it as ViewGroup,
                anim = false,
                colorPallet = colorPalletState
            )
        }
    }
}