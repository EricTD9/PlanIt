package com.planit.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planit.R
import com.planit.data.model.Reminder
import java.text.SimpleDateFormat
import java.util.*

class ReminderPreviewAdapter(
    private val onItemClick: (Reminder) -> Unit
) : ListAdapter<Reminder, ReminderPreviewAdapter.PreviewViewHolder>(ReminderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder_preview, parent, false)
        return PreviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: PreviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.text_title)
        private val timeText: TextView = itemView.findViewById(R.id.text_time)

        fun bind(reminder: Reminder) {
            titleText.text = reminder.title
            
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val dateTime = timeFormat.format(reminder.dateTime)
            timeText.text = dateTime

            itemView.setOnClickListener {
                onItemClick(reminder)
            }
        }
    }

    class ReminderDiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem == newItem
        }
    }
}

