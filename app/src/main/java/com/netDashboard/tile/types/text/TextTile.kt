package com.netDashboard.tile.types.button

import android.app.Dialog
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.databinding.PopupTextBinding
import com.netDashboard.globals.G
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage

class TextTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_text

    override val mqttData = MqttData("1")

    @JsonIgnore
    override var typeTag = "text"

    var value = ""
        set(value) {
            field = value
            holder?.itemView?.findViewById<TextView>(R.id.tt_value)?.text = value
        }

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        value = value

        holder.itemView.findViewById<TextView>(R.id.tt_tag)?.let {
            it.text = if (tag.isBlank()) "???" else tag
        }
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        if (mqttData.pubs["base"].isNullOrEmpty()) return
        if (dashboard.dg?.mqttd == null) return

        if (mqttData.varPayload) {
            val dialog = Dialog(adapter.context)

            dialog.setContentView(R.layout.popup_text)
            val binding = PopupTextBinding.bind(dialog.findViewById(R.id.pt_root))

            binding.ptTopic.text = mqttData.pubs["base"].toString()

            binding.ptConfirm.setOnClickListener {
                send(binding.ptPayload.text.toString(), mqttData.qos)
                dialog.hide()
            }

            binding.ptDeny.setOnClickListener {
                dialog.hide()
            }

            G.theme.apply(adapter.context, binding.root)
            dialog.show()
        } else send(mqttData.pubPayload, mqttData.qos)
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        value = jsonResult["value"] ?: data.second.toString()
    }
}