package com.netDashboard.tiles.tiles_types.button

import android.content.res.ColorStateList
import android.widget.Button
import com.netDashboard.R
import com.netDashboard.alpha
import com.netDashboard.getContrastColor
import com.netDashboard.getRandomColor
import com.netDashboard.tiles.Tile
import com.netDashboard.tiles.TilesAdapter

class ButtonTile(name: String, color: Int, width: Int, height: Int) :
    Tile(name, color, R.layout.button_tile, width, height) {

    var text = "Default value"
    private var counter = 0

    override fun onBindViewHolder(holder: TilesAdapter.TileViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.itemView.findViewById<Button>(R.id.tile_button).setOnClickListener {
            holder.itemView.callOnClick()

            if (!editMode()) {
                counter = 0
                text = counter.toString()

                holder.itemView.findViewById<Button>(R.id.tile_button).text = text

                setThemeColor(getRandomColor())
            }
        }

        holder.itemView.findViewById<Button>(R.id.tile_button).text = text

        setThemeColor(color)
    }

    override fun setThemeColor(color: Int) {
        super.setThemeColor(color)

        val button = holder?.itemView?.findViewById<Button>(R.id.tile_button)

        button?.backgroundTintList =
            ColorStateList.valueOf(
                color
            )

        button?.setTextColor(getContrastColor(color).alpha(75))
    }

    override fun onData(data: String, isLive: Boolean) {
        //if (!editMode()) {
        //    super.onData(data, isLive)
        //    //val temperature = data.split(";").toTypedArray()[2]
        //    val temperature = Random().nextInt(99999).toString()
        //    holder?.itemView?.findViewById<Button>(R.id.button)
        //        ?.text = temperature
        //    text = temperature
        //}

        counter += data.toIntOrNull() ?: 1
        text = counter.toString()
    }
}