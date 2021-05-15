package com.netDashboard.tiles.tiles_types.button

import android.content.res.ColorStateList
import android.widget.Button
import com.netDashboard.R
import com.netDashboard.abyss.Abyss
import com.netDashboard.alpha
import com.netDashboard.getContrastColor
import com.netDashboard.getRandomColor
import com.netDashboard.tiles.Tile
import com.netDashboard.tiles.TilesAdapter
import java.util.*

class ButtonTile(name: String, color: Int, width: Int, height: Int) :
    Tile(name, color, R.layout.button_tile, width, height) {

    var text = "Default value"

    override fun onBindViewHolder(holder: TilesAdapter.TileViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.itemView.findViewById<Button>(R.id.button).setOnClickListener {
            holder.itemView.callOnClick()

            if (!editMode()) {
                setThemeColor(getRandomColor())

                //Udpd().send("wen;fan;-1", "192.168.0.19", 54091)
            }
        }

        holder.itemView.findViewById<Button>(R.id.button).text = text

        setThemeColor(color)
    }

    override fun setThemeColor(color: Int) {
        super.setThemeColor(color)

        holder?.itemView?.findViewById<Button>(R.id.button)?.backgroundTintList =
            ColorStateList.valueOf(
                color
            )

        holder?.itemView?.findViewById<Button>(R.id.button)
            ?.setTextColor(getContrastColor(color).alpha(75))
    }

    override fun onData(data: String) {
        if (!editMode()) {
            super.onData(data)

            //val temperature = data.split(";").toTypedArray()[2]
            val temperature = Random().nextInt(99999).toString()

            holder?.itemView?.findViewById<Button>(R.id.button)
                ?.text = temperature

            text = temperature
        }
    }
}