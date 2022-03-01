package com.netDashboard.tile

import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IntRange
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.netDashboard.Parser.byJSONPath
import com.netDashboard.R
import com.netDashboard.Theme.ColorPallet
import com.netDashboard.createNotification
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.databinding.PopupConfirmBinding
import com.netDashboard.dialogSetup
import com.netDashboard.globals.G.theme
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.recycler_view.RecyclerViewItem
import com.netDashboard.screenWidth
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

@Suppress("UNUSED")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
abstract class Tile : RecyclerViewItem() {

    @JsonIgnore
    var dashboard: Dashboard = Dashboard(isInvalid = true)

    @JsonIgnore
    open var height = 1

    var tag = ""
    var iconRes = R.drawable.il_interface_plus

    var hsv = floatArrayOf(0f, 0f, 0f)
    val colorPallet: ColorPallet
        get() = theme.a.getColorPallet(hsv, true)

    abstract var typeTag: String

    val mqttData = MqttData()

    var doLog = false
    var doNotify = false
    var silentNotify = false

    companion object {
        fun MutableList<Tile>.byId(id: Long): Tile? =
            this.find { it.id == id }

        fun send() {
            TODO("Not yet implemented")
        }
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height = ((screenWidth - view.paddingLeft * 2) * height / 3.236).toInt()
        view.layoutParams = params

        holder.itemView.findViewById<View>(R.id.t_icon)?.setBackgroundResource(iconRes)
        holder.itemView.findViewById<TextView>(R.id.t_tag)?.text = tag.ifBlank { "???" }
    }

    open fun onSetTheme(holder: RecyclerViewAdapter.ViewHolder) {
        theme.apply(
            holder.itemView as ViewGroup,
            anim = false,
            colorPallet = colorPallet
        )
    }

    open class MqttData() {
        var isEnabled = true
        var lastReceive: Date? = null

        val subs = mutableMapOf<String, String>()
        val pubs = mutableMapOf<String, String>()
        val colors = mutableMapOf<String, Int>()
        val jsonPaths = mutableMapOf<String, String>()

        var qos = 0
            set(value) {
                field = minOf(2, maxOf(0, value))
            }

        var varPayload = true
        var payloads: MutableMap<String, String> =
            mutableMapOf("base" to "", "true" to "1", "false" to "0")
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
        if (dashboard.dg?.mqttd == null) return

        fun send() {
            dashboard.dg?.mqttd?.publish(topic, msg, qos, retained)
            onSend(topic, msg, qos, retained)
        }

        if (!mqttData.confirmPub || raw) {
            send()
            return
        }

        val dialog = Dialog(adapter.context)

        dialog.setContentView(R.layout.popup_confirm)
        val binding = PopupConfirmBinding.bind(dialog.findViewById(R.id.root))

        binding.pcConfirm.setOnClickListener {
            send()
            dialog.dismiss()
        }

        binding.pcDeny.setOnClickListener {
            dialog.dismiss()
        }

        binding.padding.setOnClickListener {
            dialog.dismiss()
        }

        binding.pcConfirm.text = "PUBLISH"
        binding.pcText.text = "Confirm publishing"

        dialog.dialogSetup()
        theme.apply(binding.root)
        dialog.show()
    }

    open fun onSend(
        topic: String?,
        msg: String,
        @IntRange(from = 0, to = 2) qos: Int,
        retained: Boolean = false
    ) {
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
        mqttData.lastReceive = Date()

        if (doNotify) {
            dashboard.dg?.context?.let {
                createNotification(
                    it,
                    dashboard.name.uppercase(Locale.getDefault()),
                    "${
                        if (tag.isBlank() || data.second.toString().isBlank())
                            "New value for: ${data.first}"
                        else "$tag: ${data.second.toString()}"
                    }",
                    silentNotify,
                    dashboard.id.toInt()
                )
            }
        }

        if (doLog) dashboard.log.newEntry("${tag.ifBlank { data.first }}: ${data.second}")

        onReceive(data, jsonResult)
    }

    open fun onReceive(data: Pair<String?, MqttMessage?>, jsonResult: MutableMap<String, String>) {
    }

    open fun onCreateTile() {}
}