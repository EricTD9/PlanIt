package com.planit.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planit.R
import com.planit.data.model.Category
import com.planit.data.model.Reminder
import java.text.SimpleDateFormat
import java.util.*

class ReminderAdapter(
    private val onItemClick: (Reminder) -> Unit,
    private val onCheckboxClick: (Reminder, Boolean) -> Unit
) : ListAdapter<Reminder, ReminderAdapter.ReminderViewHolder>(ReminderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.item_reminder_card)
        private val categoryIndicator: View = itemView.findViewById(R.id.view_category_indicator)
        private val titleText: TextView = itemView.findViewById(R.id.text_title)
        private val timeText: TextView = itemView.findViewById(R.id.text_time)
        private val descriptionText: TextView = itemView.findViewById(R.id.text_description)
        private val checkbox: CheckBox = itemView.findViewById(R.id.checkbox_completed)

        fun bind(reminder: Reminder) {
            titleText.text = reminder.title
            timeText.text = formatDateTime(reminder.dateTime)
            descriptionText.text = reminder.description
            descriptionText.visibility = if (reminder.description.isNullOrBlank()) View.GONE else View.VISIBLE
            checkbox.isChecked = reminder.status == com.planit.data.model.ReminderStatus.COMPLETED

            // Color del indicador de categoría
            val categoryColor = when (reminder.category) {
                Category.WORK -> ContextCompat.getColor(itemView.context, R.color.category_work)
                Category.SCHOOL -> ContextCompat.getColor(itemView.context, R.color.category_school)
                Category.PERSONAL -> ContextCompat.getColor(itemView.context, R.color.category_personal)
            }
            categoryIndicator.setBackgroundColor(categoryColor)

            cardView.setOnClickListener {
                onItemClick(reminder)
            }

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                onCheckboxClick(reminder, isChecked)
            }
        }

        private fun formatDateTime(date: Date): String {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return timeFormat.format(date)
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

