package com.netDashboard.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class DashboardsAdapter(
    private val context: Context
) : ListAdapter<String, DashboardsAdapter.DashboardsViewHolder>(DashboardDiffCallback) {

    //var swapMode = false
    //var swapModeLock = false
    //var removeMode = false
    //private var addMode = false

    private lateinit var dashboards: MutableList<String>
    private lateinit var currentDashboard: String

    private val tileOnClick = MutableLiveData(-1)

    fun getTileOnClickLiveData(): LiveData<Int> {
        return tileOnClick
    }

    override fun submitList(list: MutableList<String>?) {
        super.submitList(list)
        dashboards = list!!.toMutableList()
    }

    override fun getCurrentList(): MutableList<String> {
        return dashboards
    }

    override fun getItemCount(): Int {
        return dashboards.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        return DashboardsViewHolder(view)
    }

    class DashboardsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onBindViewHolder(holder: DashboardsViewHolder, position: Int) {
    }
}

object DashboardDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return newItem == oldItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return newItem == oldItem
    }
}

