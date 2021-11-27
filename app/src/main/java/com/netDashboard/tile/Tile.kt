package com.netDashboard.tile

import android.app.Dialog
import androidx.annotation.IntRange
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.netDashboard.R
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.databinding.PopupConfirmBinding
import com.netDashboard.parser.Parser.byJSONPath
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.recycler_view.BaseRecyclerViewItem
import com.netDashboard.screenWidth
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

@Suppress("UNUSED")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
abstract class Tile : BaseRecyclerViewItem() {

    @JsonIgnore
    var dashboard: Dashboard = Dashboard("err")

    val tag = "name"
    abstract var typeTag: String

    abstract val mqttData: MqttData

    companion object {
        fun MutableList<Tile>.byId(id: Long): Tile? =
            this.find { it.id == id }
    }

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height =
            ((screenWidth - view.paddingLeft * 2) / (adapter.spanCount)) * height
        view.layoutParams = params
    }

    open class MqttData(defaultPubValue: String = "") {
        var isEnabled = true
        var lastReceive = Date()

        val subs: MutableMap<String, String> = mutableMapOf()
        val pubs: MutableMap<String, String> = mutableMapOf()
        val jsonPaths: MutableMap<String, String> = mutableMapOf()

        var qos = 0
            set(value) {
                field = minOf(2, maxOf(0, value))
            }

        var pubPayload = defaultPubValue
        var confirmPub = false
        var payloadIsJson = false
    }

    fun send(
        msg: String,
        @IntRange(from = 0, to = 2) qos: Int,
        retained: Boolean = false,
        raw: Boolean = false
    ) = send(mqttData.pubs["base"], msg, qos, retained, raw)

    @JvmOverloads
    fun send(
        topic: String?,
        msg: String,
        @IntRange(from = 0, to = 2) qos: Int,
        retained: Boolean = false,
        raw: Boolean = false
    ) {
        if (topic.isNullOrEmpty()) return
        if (dashboard.daemonGroup?.mqttd == null) return

        fun send() {
            dashboard.daemonGroup?.mqttd?.publish(topic, msg, qos, retained)
            onSend(topic, msg, qos, retained)
        }

        if (!mqttData.confirmPub || raw) {
            send()
            return
        }

        val dialog = Dialog(adapter.context)

        dialog.setContentView(R.layout.popup_confirm)
        val binding = PopupConfirmBinding.bind(dialog.findViewById(R.id.pc_root))

        binding.pcConfirm.setOnClickListener {
            send()
            dialog.hide()
        }

        binding.pcDeny.setOnClickListener {
            dialog.hide()
        }

        binding.pcConfirm.text = "PUBLISH"
        binding.pcText.text = "Confirm publishing"

        adapter.theme.apply(adapter.context, binding.root)
        dialog.show()
    }

    open fun onSend(
        topic: String?,
        msg: String,
        @IntRange(from = 0, to = 2) qos: Int,
        retained: Boolean = false
    ) {
    }

    open fun onReceive(data: Pair<String?, MqttMessage?>, jsonResult: MutableMap<String, String>) {
    }

    fun receive(data: Pair<String?, MqttMessage?>) {
        if (!mqttData.isEnabled) return
        if (!mqttData.subs.containsValue(data.first)) return

        //Build map of jsonPath key and value of at it. Null on absence or fail.
        val jsonResult = mutableMapOf<String, String>()
        if (mqttData.payloadIsJson) {
            for (p in mqttData.jsonPaths) {
                data.second.toString().byJSONPath(p.value)?.let {
                    jsonResult[p.key] = it
                }
            }
        }

        onReceive(data, jsonResult)
    }
}