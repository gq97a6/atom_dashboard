import android.app.Dialog
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.alteratom.R
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.helper_objects.DialogBuilder.dialogSetup
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.dashboard.recycler_view.RecyclerViewItem
import com.alteratom.dashboard.tile.Tile
import com.alteratom.databinding.DialogSelectBinding
import com.fasterxml.jackson.annotation.JsonIgnore

class SelectTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_select

    @JsonIgnore
    override var typeTag = "select"

    override var iconKey = "il_business_receipt_alt"

    val options = mutableListOf("" to "", "" to "", "" to "", "" to "", "" to "")
    var showPayload = false

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        if (tag.isBlank()) holder.itemView.findViewById<TextView>(R.id.t_tag)?.visibility =
            View.GONE
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        val notEmpty = options.filter { !(it.first.isEmpty() && it.second.isEmpty()) }
        if (notEmpty.isNotEmpty()) {
            val dialog = Dialog(adapter.context)
            val adapter = RecyclerViewAdapter<RecyclerViewItem>(adapter.context)

            dialog.setContentView(R.layout.dialog_select)
            val binding = DialogSelectBinding.bind(dialog.findViewById(R.id.root))

            adapter.onBindViewHolder = { _, holder, pos ->
                val text = holder.itemView.findViewById<TextView>(R.id.is_text)
                text.text = if (showPayload) "${notEmpty[pos].first} (${notEmpty[pos].second})"
                else notEmpty[pos].first
            }

            adapter.onItemClick = {
                val pos = adapter.list.indexOf(it)
                send(this.options[pos].second)
                dialog.dismiss()
            }

            adapter.setHasStableIds(true)
            adapter.submitList(MutableList(notEmpty.size) {
                RecyclerViewItem(
                    R.layout.item_select
                )
            })

            binding.dsRecyclerView.layoutManager = LinearLayoutManager(adapter.context)
            binding.dsRecyclerView.adapter = adapter

            dialog.dialogSetup()
            aps.theme.apply(binding.root)
            dialog.show()
        } else createToast(adapter.context, "Add options first")
    }
}