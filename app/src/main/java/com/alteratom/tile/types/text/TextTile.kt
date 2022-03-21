package com.alteratom.tile.types.button

import android.app.Dialog
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fasterxml.jackson.annotation.JsonIgnore
import com.alteratom.DialogBuilder.dialogSetup
import com.alteratom.R
import com.alteratom.databinding.DialogTextBinding
import com.alteratom.G
import com.alteratom.recycler_view.RecyclerViewAdapter
import com.alteratom.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.random.Random

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

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val layoutParams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
        layoutParams.isFullSpan = isBig

        value = value
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        if (mqttData.pubs["base"].isNullOrEmpty()) return
        if (dashboard.dg?.mqttd?.client?.isConnected != true) return

        if (mqttData.varPayload) {
            val dialog = Dialog(adapter.context)

            dialog.setContentView(R.layout.dialog_text)
            val binding = DialogTextBinding.bind(dialog.findViewById(R.id.root))

            binding.dtTopic.text = mqttData.pubs["base"].toString()

            binding.dtConfirm.setOnClickListener {
                send(binding.dtPayload.text.toString())
                dialog.dismiss()
            }

            binding.dtDeny.setOnClickListener {
                dialog.dismiss()
            }

            dialog.dialogSetup()
            G.theme.apply(binding.root)
            dialog.show()
        } else send(Random.nextInt().toString())
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        super.onReceive(data, jsonResult)
        value = jsonResult["base"] ?: data.second.toString()
    }
}