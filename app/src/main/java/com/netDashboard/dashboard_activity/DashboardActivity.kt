package com.netDashboard.dashboard_activity

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.netDashboard.R
import com.netDashboard.abyss.Abyss
import com.netDashboard.abyss.udpd
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


class DashboardActivity : AppCompatActivity() {
    private lateinit var b: DashboardActivityBinding

    private lateinit var settings: DashboardSettings
    lateinit var dashboardTilesAdapter: TilesAdapter
    private var abyss = Abyss(this) //TODO - don't recreate

    private lateinit var dashboardName: String
    private lateinit var dashboardFileName: String
    private lateinit var dashboardSettingsFileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = DashboardActivityBinding.inflate(layoutInflater)
        setContentView(b.root)

        dashboardName = intent.getStringExtra("dashboardName") ?: ""

        if (dashboardName.isEmpty()) {
            Intent(this, MainActivity::class.java).also {
                finish()
                startActivity(it)
            }
        }

        dashboardFileName = filesDir.canonicalPath + "/" + dashboardName
        dashboardSettingsFileName = filesDir.canonicalPath + "/settings_" + dashboardName
        settings = DashboardSettings().getSettings(dashboardSettingsFileName)

        abyss.start()

        abyss.udpd.port = settings.udpPort
        abyss.udpd.start()

        abyss.udpd.send("test send", "192.168.0.18", 5452) //TODO - implement ?intents

        setupRecyclerView()

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

        dashboardTilesAdapter.submitList(Tiles().getList(dashboardFileName).toMutableList())

        if (dashboardTilesAdapter.itemCount == 0) {
            b.placeholder.visibility = View.VISIBLE
        }
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
            abyss.stop()

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

    private fun swapButtonOnClick() {

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

    //ObjectAnimator.ofFloat(b.indicator, "translationX", distance)
    //.apply {
    //    this.duration = duration
    //    start()
    //}

    //Handler(Looper.getMainLooper()).postDelayed({
    //    moveIndicator(0f, 300)
    //}, 400)


    //b.get.setOnClickListener() {
    //    if (abyss.isBounded) {
    //        val data:String = abyss.service.udpd.getData()
    //        Toast.makeText(this, "number: $data", Toast.LENGTH_SHORT).show()
    //    }
    //}
//
    //override fun onStart() {
    //    super.onStart()

    //}
//
    //override fun onDestroy() {
    //    abyss.unbound()
    //    super.onDestroy()
    //}
//
    //override fun onStop() {
    //    abyss.unbound()
    //    super.onStop()
    //}
}