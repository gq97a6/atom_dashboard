import android.app.Dialog
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alteratom.R
import com.alteratom.dashboard.DialogBuilder.dialogSetup
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.dashboard.recycler_view.RecyclerViewItem
import com.alteratom.dashboard.tile.Tile
import com.alteratom.databinding.DialogTextBinding
import com.fasterxml.jackson.annotation.JsonIgnore
import org.eclipse.paho.client.mqttv3.MqttMessage


class TerminalTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_terminal

    @JsonIgnore
    override var typeTag = "terminal"

    @JsonIgnore
    override var height = 2f

    override var iconKey = "il_device_desktop"

    var log = mutableListOf<String>()

    @JsonIgnore
    lateinit var terminalAdapter: RecyclerViewAdapter<RecyclerViewItem>

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val layoutParams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
        layoutParams.isFullSpan = true

        terminalAdapter = RecyclerViewAdapter(adapter.context)
        terminalAdapter.setHasStableIds(true)

        terminalAdapter.onBindViewHolder = { _, holder, pos ->
            val text = holder.itemView.findViewById<TextView>(R.id.ite_text)
            text.text = log[pos]
        }

        terminalAdapter.submitList(MutableList(log.size) {
            RecyclerViewItem(
                R.layout.item_terminal_entry
            )
        })

        val layoutManager = LinearLayoutManager(adapter.context)
        layoutManager.reverseLayout = true

        val rv = holder.itemView.findViewById<RecyclerView>(R.id.tt_recycler_view)
        rv?.layoutManager = layoutManager
        rv?.adapter = terminalAdapter
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        if (mqttData.payloadIsVar) {
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
            theme.apply(binding.root)
            dialog.show()
        } else send(mqttData.payloads["base"] ?: "")
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        super.onReceive(data, jsonResult)

        val entry = jsonResult["base"] ?: data.second.toString()
        log.add(0, entry)

        terminalAdapter.let {
            it.list.add(RecyclerViewItem(R.layout.item_terminal_entry))

            if (log.size > 10) {
                log.removeLast()
                it.removeItemAt(it.list.lastIndex, false)
            }

            it.notifyDataSetChanged()
        }
    }
}