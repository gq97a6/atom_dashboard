package com.netDashboard.tile.types.pick

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.createToast
import com.netDashboard.databinding.PopupSelectBinding
import com.netDashboard.globals.G
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.recycler_view.GenericAdapter
import com.netDashboard.recycler_view.GenericItem
import com.netDashboard.tile.Tile

class SelectTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_select

    override val mqttData = MqttData("")

    @JsonIgnore
    override var typeTag = "button"

    val list = mutableMapOf<String, String>("0" to "a", "1" to "b", "2" to "c")

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        if (tag.isBlank()) holder.itemView.findViewById<TextView>(R.id.t_tag)?.visibility =
            View.GONE
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        val dialog = Dialog(adapter.context)
        val adapter = GenericAdapter(adapter.context)

        val list = MutableList(list.size) {
            GenericItem(R.layout.item_select)
        }

        dialog.setContentView(R.layout.popup_select)
        val binding = PopupSelectBinding.bind(dialog.findViewById(R.id.ps_root))

        adapter.setHasStableIds(true)
        adapter.onBindViewHolder = { _, holder, pos ->
            val text = holder.itemView.findViewById<TextView>(R.id.is_text)
            text.text = this.list.keys.toList()[pos]
        }

        adapter.onItemClick = {
            val pos = adapter.list.indexOf(it)
            createToast(adapter.context, "${this.list.values.toList()[pos]}")
            dialog.dismiss()
        }

        binding.psRecyclerView.layoutManager = LinearLayoutManager(adapter.context)
        binding.psRecyclerView.adapter = adapter

        adapter.submitList(list)
        dialog.show()

        val a = dialog.window?.attributes

        a?.dimAmount = 0.9f
        dialog.window?.attributes = a
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        G.theme.apply(binding.root as ViewGroup)
    }
}