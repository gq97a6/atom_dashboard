package com.netDashboard.tile.types.button

import android.content.res.ColorStateList
import android.widget.Button
import com.netDashboard.R
import com.netDashboard.alpha
import com.netDashboard.getContrastColor
import com.netDashboard.tile.Tile
import com.netDashboard.tile.TilesAdapter

class ButtonTile(name: String, color: Int, width: Int, height: Int) :
    Tile("button", name, color, R.layout.tile_button, width, height) {

    var text = "Default value"

    override fun onBindViewHolder(holder: TilesAdapter.TileViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.itemView.findViewById<Button>(R.id.tb_button).setOnClickListener {
            holder.itemView.callOnClick()
        }

        holder.itemView.findViewById<Button>(R.id.tb_button).text = text

        setThemeColor(color)
    }

    override fun setThemeColor(color: Int) {
        super.setThemeColor(color)

        val button = holder?.itemView?.findViewById<Button>(R.id.tb_button)

        button?.backgroundTintList =
            ColorStateList.valueOf(
                color
            )

        button?.setTextColor(getContrastColor(color).alpha(75))
    }

    override fun onClick() {
        super.onClick()
    }
}