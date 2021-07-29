package com.netDashboard.dashboard

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class DashboardAdapter(
    private val context: Context
) : ListAdapter<Dashboard, DashboardAdapter.DashboardsViewHolder>(DashboardDiffCallback) {

    private lateinit var dashboards: MutableList<Dashboard>
    private lateinit var currentDashboard: Dashboard

    override fun submitList(list: MutableList<Dashboard>?) {
        super.submitList(list)
        dashboards = list ?: mutableListOf()
    }

    override fun getCurrentList(): MutableList<Dashboard> {
        return dashboards
    }

    override fun getItemCount(): Int {
        return dashboards.size
    }
    
    override fun getItemId(position: Int): Long {
        return dashboards[position].id ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        currentDashboard = dashboards[position]

        return dashboards[position].getItemViewType(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardsViewHolder {
        return currentDashboard.onCreateViewHolder(parent, viewType)
    }

    class DashboardsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onBindViewHolder(holder: DashboardsViewHolder, position: Int) {
    }
}

object DashboardDiffCallback : DiffUtil.ItemCallback<Dashboard>() {
    override fun areItemsTheSame(oldItem: Dashboard, newItem: Dashboard): Boolean {
        return oldItem.areItemsTheSame(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: Dashboard, newItem: Dashboard): Boolean {
        return oldItem.areContentsTheSame(oldItem, newItem)
    }
}
