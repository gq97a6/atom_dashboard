package com.netDashboard.tile.types.pick

import android.app.Dialog
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.createToast
import com.netDashboard.databinding.DialogSelectBinding
import com.netDashboard.dialogSetup
import com.netDashboard.globals.G
import com.netDashboard.recycler_view.GenericAdapter
import com.netDashboard.recycler_view.GenericItem
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.tile.Tile

class SelectTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_select

    @JsonIgnore
    override var typeTag = "select"

    override var iconKey = "il_business_receipt_alt"

    val options = mutableListOf("" to "")
    var showPayload = false

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        if (tag.isBlank()) holder.itemView.findViewById<TextView>(R.id.t_tag)?.visibility =
            View.GONE
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        if (mqttData.pubs["base"].isNullOrEmpty()) return
        if (dashboard.dg?.mqttd?.client?.isConnected != true) return

        val notEmpty = options.filter { !(it.first.isEmpty() && it.second.isEmpty()) }
        if (notEmpty.size > 0) {
            val dialog = Dialog(adapter.context)
            val adapter = GenericAdapter(adapter.context)

            dialog.setContentView(R.layout.dialog_select)
            val binding = DialogSelectBinding.bind(dialog.findViewById(R.id.root))

            adapter.onBindViewHolder = { _, holder, pos ->
                val text = holder.itemView.findViewById<TextView>(R.id.is_text)
                text.text = if (showPayload) "${notEmpty[pos].first} (${notEmpty[pos].second})"
                else "${notEmpty[pos].first}"
            }

            adapter.onItemClick = {
                val pos = adapter.list.indexOf(it)
                send("${this.options[pos].second}", mqttData.qos)
                dialog.dismiss()
            }

            adapter.setHasStableIds(true)
            adapter.submitList(MutableList(notEmpty.size) { GenericItem(R.layout.item_select) })

            binding.dsRecyclerView.layoutManager = LinearLayoutManager(adapter.context)
            binding.dsRecyclerView.adapter = adapter

            dialog.dialogSetup()
            G.theme.apply(binding.root)
            dialog.show()
        } else createToast(adapter.context, "Add options first")
    }
}