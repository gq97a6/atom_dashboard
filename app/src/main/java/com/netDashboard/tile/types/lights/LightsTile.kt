package com.netDashboard.tile.types.lights

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.DialogBuilder.buildConfirm
import com.netDashboard.DialogBuilder.dialogSetup
import com.netDashboard.R
import com.netDashboard.blink
import com.netDashboard.createToast
import com.netDashboard.databinding.DialogLightsBinding
import com.netDashboard.databinding.DialogSelectBinding
import com.netDashboard.databinding.DialogThermostatBinding
import com.netDashboard.globals.G
import com.netDashboard.recycler_view.GenericAdapter
import com.netDashboard.recycler_view.GenericItem
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.round
import com.netDashboard.tile.Tile
import me.tankery.lib.circularseekbar.CircularSeekBar
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.math.abs
import kotlin.math.roundToInt

class LightsTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_lights

    @JsonIgnore
    override var typeTag = "lights"

    override var iconKey = "il_business_lightbulb_alt"

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        if (tag.isBlank()) holder.itemView.findViewById<TextView>(R.id.t_tag)?.visibility =
            View.GONE
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        val value = jsonResult["base"] ?: data.second.toString()
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        //60-120

        val dialog = Dialog(adapter.context)
        dialog.setContentView(R.layout.dialog_lights)
        val binding = DialogLightsBinding.bind(dialog.findViewById(R.id.root))

        dialog.dialogSetup()
        G.theme.apply(binding.root, anim = false)
        dialog.show()
    }
}