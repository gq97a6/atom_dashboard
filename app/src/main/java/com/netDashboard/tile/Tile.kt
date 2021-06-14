package com.netDashboard.tile

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netDashboard.*
import com.netDashboard.foreground_service.demons.Mqttd
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.io.*
import java.util.*

abstract class Tile(
    var name: String,
    var color: Int,
    private var layout: Int,
    var width: Int,
    var height: Int
) : Serializable {

    val id: Long?

    var isEdit = false
        set(value) { field = value; onEdit(value)}

    var flag = false
        private set

    var lock = false
        set(value) {
            field = value
            
            onLock(value)

            val flagMark = holder?.itemView?.findViewById<View>(R.id.flag_mark)
            val flagBackground = holder?.itemView?.findViewById<View>(R.id.flag_background)

            flagMark?.setBackgroundResource(R.drawable.icon_lock_flag)

            if (value) {
                flagMark?.backgroundTintList = ColorStateList.valueOf(getContrastColor(color))
                flagBackground?.backgroundTintList =
                    ColorStateList.valueOf(getContrastColor(color, true).alpha(60))

                flagMark?.visibility = View.VISIBLE
                flagBackground?.visibility = View.VISIBLE
            } else {
                flagMark?.visibility = View.GONE
                flagBackground?.visibility = View.GONE
            }
        }

    var mqttd: Mqttd? = null
    var context: Context? = null
    var holder: TilesAdapter.TileViewHolder? = null

    private var spanCount = 1

    //MQTT
    var mqttSubTopic = ""
    var mqttPubTopic = ""
    var mqttPubConfirmation = false
    var mqttSubAnswer = false
    var mqttQoS = 0
    var mqttPayloadJSON = ""
    var mqttOutputJSON = ""

    //Bluetooth
    var bltPattern = ""
    var bltDelimiter = ""
    var bltRequestToGet = ""
    var bltPayloadJSON = false
    var bltOutputJSON = ""

    init {
        id = Random().nextLong()
    }

    fun getItemViewType(context: Context, spanCount: Int): Int {
        this.context = context
        this.spanCount = spanCount

        return layout
    }

    open fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TilesAdapter.TileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        return TilesAdapter.TileViewHolder(view)
    }

    open fun onBindViewHolder(holder: TilesAdapter.TileViewHolder, position: Int) {
        this.holder = holder

        val view = holder.itemView
        val params = view.layoutParams

        params.height = ((getScreenWidth() - view.paddingLeft * 2) / spanCount) * height
        view.layoutParams = params

        holder.itemView.setOnLongClickListener {
            if (isEdit) {
                createToast(context!!, "open settings! ${holder.adapterPosition}")
            }

            return@setOnLongClickListener true
        }

        onEdit(isEdit)
        flag(flag)
    }

    fun areItemsTheSame(oldItem: Tile, newItem: Tile): Boolean {
        return oldItem == newItem
    }

    fun areContentsTheSame(oldItem: Tile, newItem: Tile): Boolean {
        return oldItem.id == newItem.id
    }

    open fun onClick() {}

    open fun flag(flag: Boolean, type: String = "") {
        this.flag = flag

        val flagMark = holder?.itemView?.findViewById<View>(R.id.flag_mark)
        val flagBackground = holder?.itemView?.findViewById<View>(R.id.flag_background)

        when (type) {
            "swap" -> flagMark?.setBackgroundResource(R.drawable.icon_swap_flag)
            "remove" -> flagMark?.setBackgroundResource(R.drawable.icon_remove_flag)
        }

        if (flag) {
            flagMark?.backgroundTintList = ColorStateList.valueOf(getContrastColor(color))
            flagBackground?.backgroundTintList =
                ColorStateList.valueOf(getContrastColor(color, true).alpha(60))

            flagMark?.visibility = View.VISIBLE
            flagBackground?.visibility = View.VISIBLE
        } else {
            flagMark?.visibility = View.GONE
            flagBackground?.visibility = View.GONE
        }
    }

    open fun setThemeColor(color: Int) { this.color = color }

    open fun onEdit(isEdit: Boolean) {}

    open fun onLock(isLocked: Boolean) {}

    open fun onData(data: String, isLive: Boolean = true) {}

    open fun onData(topic: String, message: MqttMessage, isLive: Boolean = true) {}
}