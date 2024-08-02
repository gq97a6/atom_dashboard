import android.app.Dialog
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alteratom.R
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.daemon.daemons.mqttd.Mqttd
import com.alteratom.dashboard.helper_objects.DialogBuilder.dialogSetup
import com.alteratom.dashboard.tile.Tile
import com.alteratom.databinding.DialogTextBinding
import com.fasterxml.jackson.annotation.JsonIgnore

class TextTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_text

    @JsonIgnore
    override var typeTag = "text"

    override var iconKey = "il_design_illustration"

    var isBig = false

    var value = ""
        set(value) {
            field = value
            holder?.itemView?.findViewById<TextView>(R.id.tt_values)?.text = value
        }

    override fun onBindViewHolder(
        holder: com.alteratom.dashboard.recycler_view.RecyclerViewAdapter.ViewHolder,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)

        val layoutParams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
        layoutParams.isFullSpan = isBig

        value = value
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        if (mqtt.pubs["base"].isNullOrEmpty()) return

        (dashboard?.daemon as? Mqttd?)?.let {
            if (it.state != Mqttd.State.CONNECTED) return
        }

        if (mqtt.payloadIsVar) {
            val dialog = Dialog(adapter!!.context)

            dialog.setContentView(R.layout.dialog_text)
            val binding = DialogTextBinding.bind(dialog.findViewById(R.id.root))

            binding.dtTopic.text = mqtt.pubs["base"].toString()

            binding.dtConfirm.setOnClickListener {
                send(binding.dtPayload.text.toString())
                dialog.dismiss()
            }

            binding.dtDeny.setOnClickListener {
                dialog.dismiss()
            }

            dialog.dialogSetup()
            aps.theme.apply(binding.root)
            dialog.show()
        } else send(mqtt.payloads["base"] ?: "err")
    }

    override fun onReceive(
        topic: String,
        msg: String,
        jsonResult: MutableMap<String, String>
    ) {
        super.onReceive(topic, msg, jsonResult)
        value = jsonResult["base"] ?: msg
    }
}