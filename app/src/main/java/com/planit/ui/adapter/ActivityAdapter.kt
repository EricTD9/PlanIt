package com.planit.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planit.R
import com.planit.data.model.ActivityItem

class ActivityAdapter(
    private val onCheckboxClick: (ActivityItem, Boolean) -> Unit
) : ListAdapter<ActivityItem, ActivityAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.text_activity_name)
        private val checkbox: CheckBox = itemView.findViewById(R.id.checkbox_activity)

        fun bind(activity: ActivityItem) {
            nameText.text = activity.name
            checkbox.isChecked = activity.isCompleted

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                onCheckboxClick(activity, isChecked)
            }
        }
    }

    class ActivityDiffCallback : DiffUtil.ItemCallback<ActivityItem>() {
        override fun areItemsTheSame(oldItem: ActivityItem, newItem: ActivityItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ActivityItem, newItem: ActivityItem): Boolean {
            return oldItem == newItem
        }
    }
}

