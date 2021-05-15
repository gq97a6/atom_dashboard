package com.netDashboard.dashboard_activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.netDashboard.R
import com.netDashboard.abyss.Abyss
import com.netDashboard.createToast
import com.netDashboard.dashboard_settings_activity.DashboardSettings
import com.netDashboard.dashboard_settings_activity.DashboardSettingsActivity
import com.netDashboard.databinding.DashboardActivityBinding
import com.netDashboard.main_activity.MainActivity
import com.netDashboard.margins
import com.netDashboard.new_tile_activity.NewTileActivity
import com.netDashboard.tiles.Tiles
import com.netDashboard.tiles.TilesAdapter
import com.netDashboard.toPx
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.nio.file.Files


class DashboardActivity : AppCompatActivity() {
    private lateinit var b: DashboardActivityBinding

    private lateinit var settings: DashboardSettings
    lateinit var dashboardTilesAdapter: TilesAdapter
    //private var abyss = Abyss(this)

    private lateinit var dashboardName: String
    private lateinit var dashboardFileName: String
    private lateinit var dashboardSettingsFileName: String
    private lateinit var dashboardAbyssFileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = DashboardActivityBinding.inflate(layoutInflater)
        setContentView(b.root)

        //Get dashboardName, return to main screen if empty
        dashboardName = intent.getStringExtra("dashboardName") ?: ""
        if (dashboardName.isEmpty()) {
            Intent(this, MainActivity::class.java).also {
                finish()
                startActivity(it)
            }
        }

        //Create root folder, if doesn't exist
        val rootFolder = filesDir.canonicalPath + "/dashboards_data_"
        val f = File(rootFolder)
        if(!f.exists()) {
            f.mkdir()
        }

        //Setup files names
        dashboardFileName = rootFolder + dashboardName + "_tiles"
        dashboardSettingsFileName = rootFolder + dashboardName + "_settings"
        dashboardAbyssFileName = rootFolder + dashboardName + "_abyss"

        //Get settings
        settings = DashboardSettings().getSettings(dashboardSettingsFileName)

        //Setup data
        setUpAbyss()
        setupRecyclerView()

        //TMP
        Log.i("OUY", "SAVED_COUNTER: ${getAbyssCounter()}")

        b.edit.setOnClickListener {
            editButtonOnClick()
        }

        b.set.setOnClickListener {
            setButtonOnClick()
        }

        b.remove.setOnClickListener {
            removeButtonOnClick()
        }

        b.add.setOnClickListener {
            addButtonOnClick()
        }
    }

    override fun onPause() {
        val saveMe = dashboardTilesAdapter.tiles.toList()
        Tiles().saveList(saveMe, dashboardFileName)

        super.onPause()
    }

    override fun onBackPressed() {
        if (dashboardTilesAdapter.swapMode || dashboardTilesAdapter.removeMode) {
            b.edit.callOnClick()
        } else {
            super.onBackPressed()
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun setupRecyclerView() {
        val spanCount = settings.spanCount

        dashboardTilesAdapter = TilesAdapter(this, spanCount)

        b.recyclerView.adapter = dashboardTilesAdapter

        val layoutManager = GridLayoutManager(this, spanCount)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (dashboardTilesAdapter.tiles[position].height == 1) {
                    dashboardTilesAdapter.tiles[position].width
                } else {
                    spanCount
                }
            }
        }

        b.recyclerView.layoutManager = layoutManager
        //b.recyclerView.itemAnimator = TilesAnimator()

        dashboardTilesAdapter.submitList(Tiles().getList(dashboardFileName).toMutableList())

        if (dashboardTilesAdapter.itemCount == 0) {
            b.placeholder.visibility = View.VISIBLE
        }

        //abyss.udpd.receive().observe(this, { data ->
        //    //TODO - move to abyss, update on data
        //    if (data != "C9ZF56ZLF4EW5355") {
        //        //for ((i, _) in dashboardTilesAdapter.tiles.withIndex()) {
        //        //    dashboardTilesAdapter.tiles[i].onData(data)
        //        //}
        //    }
        //})
    }

    //----------------------------------------------------------------------------------------------

    private fun setUpAbyss() {
        Abyss().start(this, dashboardAbyssFileName)
    }

    //----------------------------------------------------------------------------------------------

    private fun editButtonOnClick() {
        dashboardTilesAdapter.swapMode =
            !(dashboardTilesAdapter.swapMode || dashboardTilesAdapter.removeMode)

        dashboardTilesAdapter.removeMode = false

        if (dashboardTilesAdapter.swapMode) {
            b.ban.text = getString(R.string.swap_mode)

            b.remove.visibility = View.VISIBLE
            b.add.visibility = View.VISIBLE
            b.set.setBackgroundResource(R.drawable.button_swap)
        } else {
            b.ban.text = getString(R.string.dashboard)

            b.remove.visibility = View.GONE
            b.add.visibility = View.GONE

            b.set.setBackgroundResource(R.drawable.button_more)

            val saveMe = dashboardTilesAdapter.tiles.toList()
            Tiles().saveList(saveMe, dashboardFileName)
        }

        for ((i, _) in dashboardTilesAdapter.tiles.withIndex()) {
            dashboardTilesAdapter.tiles[i].editMode(dashboardTilesAdapter.swapMode)
            dashboardTilesAdapter.tiles[i].flag(false)
        }

        dashboardTilesAdapter.notifyDataSetChanged()
    }

    //----------------------------------------------------------------------------------------------

    private fun setButtonOnClick() {

        if (dashboardTilesAdapter.removeMode) {
            dashboardTilesAdapter.removeMode = false
            dashboardTilesAdapter.swapMode = true
            b.ban.text = getString(R.string.swap_mode)

            for ((i, _) in dashboardTilesAdapter.tiles.withIndex()) {
                dashboardTilesAdapter.tiles[i].editMode(true)
                dashboardTilesAdapter.tiles[i].flag(false)
            }
        } else if (!dashboardTilesAdapter.swapMode) {
            //abyss.stop()

            Intent(this, DashboardSettingsActivity::class.java).also {
                it.putExtra("dashboardName", dashboardName)
                it.putExtra("dashboardFileName", dashboardFileName)
                it.putExtra("dashboardSettingsFileName", dashboardSettingsFileName)

                finish()

                startActivity(it)
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun addButtonOnClick() {

        Intent(this, NewTileActivity::class.java).also {
            it.putExtra("dashboardName", dashboardName)
            it.putExtra("dashboardFileName", dashboardFileName)
            it.putExtra("dashboardSettingsFileName", dashboardSettingsFileName)
            finish()
            startActivity(it)
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun removeButtonOnClick() {

        if (dashboardTilesAdapter.removeMode) {
            var toDelete = false

            for ((i, _) in dashboardTilesAdapter.tiles.withIndex()) {

                if (dashboardTilesAdapter.tiles[i].flag()) {
                    toDelete = true
                    break
                }
            }

            if (!toDelete) {
                createToast(this, getString(R.string.dashboard_remove), 1)
            } else {

                @SuppressLint("ShowToast")
                val snackbar = Snackbar.make(
                    b.root,
                    getString(R.string.snackbar_confirmation),
                    Snackbar.LENGTH_LONG
                ).margins().setAction("YES") {

                    for ((i, _) in dashboardTilesAdapter.tiles.withIndex()) {

                        if (dashboardTilesAdapter.tiles[i].flag()) {
                            dashboardTilesAdapter.tiles.removeAt(i)

                            dashboardTilesAdapter.notifyItemRemoved(i)
                            dashboardTilesAdapter.notifyItemRangeChanged(
                                i,
                                dashboardTilesAdapter.itemCount - i
                            )

                            if (dashboardTilesAdapter.itemCount == 0) {
                                b.placeholder.visibility = View.VISIBLE
                            }

                            break
                        }
                    }
                }

                val snackBarView = snackbar.view
                snackBarView.translationY = -20.toPx().toFloat()
                snackbar.show()
            }
        } else if (dashboardTilesAdapter.swapMode) {
            dashboardTilesAdapter.swapMode = false
            dashboardTilesAdapter.removeMode = true
            b.ban.text = getString(R.string.remove_mode)

            for ((i, _) in dashboardTilesAdapter.tiles.withIndex()) {
                dashboardTilesAdapter.tiles[i].flag(false)
            }

            createToast(this, getString(R.string.dashboard_remove))
        }
    }

    //TMP
    private fun getAbyssCounter(): Int {
        return try {
            val file = FileInputStream(dashboardAbyssFileName)
            val inStream = ObjectInputStream(file)

            val counter = inStream.readObject() as Int

            inStream.close()
            file.close()

            counter
        } catch (e: Exception) {
            0
        }
    }

    //ObjectAnimator.ofFloat(b.indicator, "translationX", distance)
    //.apply {
    //    this.duration = duration
    //    start()
    //}
}