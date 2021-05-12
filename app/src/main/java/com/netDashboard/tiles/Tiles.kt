package com.netDashboard.tiles

import android.graphics.Color
import com.netDashboard.tiles.tiles_types.button.ButtonTile
import com.netDashboard.tiles.tiles_types.slider.SliderTile
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


class Tiles {
    val color = Color.parseColor("#A3A3A3")

    private val tileList = listOf(
        ButtonTile("Button", color, 3, 1),
        SliderTile("Slider", color, 3, 1)
    )

    fun getTileList(): List<Tile> {
        return this.tileList
    }

    fun saveList(list: List<Tile>, name: String) {
        for ((i, _) in list.withIndex()) {
            list[i].context = null
            list[i].holder = null

            list[i].editMode(false)
            list[i].flag(false)
        }

        val file = FileOutputStream(name)

        val outStream = ObjectOutputStream(file)

        outStream.writeObject(list)

        outStream.close()
        file.close()
    }

    fun getList(name: String): List<Tile> {
        return try {
            val file = FileInputStream(name)
            val inStream = ObjectInputStream(file)

            val list = inStream.readObject() as List<*>

            inStream.close()
            file.close()

            list.filterIsInstance<Tile>().takeIf { it.size == list.size } ?: listOf()
        } catch (e: Exception) {
            listOf()
        }
    }

    //private val tileLiveData = MutableLiveData(initialTileList)

    //fun getTileList(): LiveData<List<Tile>> {
    //    return tileLiveData
    //}

    //fun addTile(tile: Tile) {
    //    val currentList = tileLiveData.value
    //    if (currentList == null) {
    //        tileLiveData.postValue(listOf(tile))
    //    } else {
    //        val updatedList = currentList.toMutableList()
    //        updatedList.add(0, tile)
    //        tileLiveData.postValue(updatedList)
    //    }
    //}

    //fun removeTile() {
    //    val currentList = tileLiveData.value
    //    if (currentList != null) {
    //        val updatedList = currentList.toMutableList()
    //        updatedList.removeAt(0)
    //        tileLiveData.postValue(updatedList)
    //    }
    //}

    //companion object {
    //    private var INSTANCE: TilesSource? = null
    //    fun getDataSource(resources: Resources): TilesSource {
    //        return synchronized(TilesSource::class) {
    //            val newInstance = INSTANCE ?: TilesSource()
    //            INSTANCE = newInstance
    //            newInstance
    //        }
    //    }
    //}
}