package com.netDashboard.tile.types.button

import android.app.Dialog
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.databinding.PopupTextBinding
import com.netDashboard.dialogSetup
import com.netDashboard.globals.G
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.random.Random

class TextTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_text

    @JsonIgnore
    override var typeTag = "text"

    var value = ""
        set(value) {
            field = value
            holder?.itemView?.findViewById<TextView>(R.id.tt_value)?.text = value
        }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        value = value
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        if (mqttData.pubs["base"].isNullOrEmpty()) return
        if (dashboard.dg?.mqttd?.client?.isConnected != true) return

        if (mqttData.varPayload) {
            val dialog = Dialog(adapter.context)

            dialog.setContentView(R.layout.popup_text)
            val binding = PopupTextBinding.bind(dialog.findViewById(R.id.root))

            binding.ptTopic.text = mqttData.pubs["base"].toString()

            binding.ptConfirm.setOnClickListener {
                send(binding.ptPayload.text.toString(), mqttData.qos)
                dialog.dismiss()
            }

            binding.ptDeny.setOnClickListener {
                dialog.dismiss()
            }

            binding.padding.setOnClickListener {
                dialog.dismiss()
            }

            dialog.dialogSetup()
            G.theme.apply(binding.root)
            dialog.show()
        } else send(Random.nextInt().toString(), mqttData.qos)
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        value = jsonResult["base"] ?: data.second.toString()
    }
}