package com.netDashboard.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class Adapter<a, b : RecyclerView.ViewHolder?>(c:DiffUtil.ItemCallback<a>) : ListAdapter<a, b>(c) {
}