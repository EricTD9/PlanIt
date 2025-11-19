package com.planit.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.planit.R
import com.planit.data.model.Category
import com.planit.data.model.Reminder
import java.text.SimpleDateFormat
import java.util.*

class CalendarDayAdapter(
    private val onDayClick: (Date) -> Unit
) : RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder>() {

    private val days = mutableListOf<DayItem>()
    private var selectedDate: Date? = null
    private var remindersByDate: Map<String, List<Reminder>> = emptyMap()

    data class DayItem(
        val date: Date?,
        val dayNumber: String,
        val isCurrentMonth: Boolean,
        val isToday: Boolean
    )

    fun setDays(daysList: List<DayItem>) {
        days.clear()
        days.addAll(daysList)
        notifyDataSetChanged()
    }

    fun setReminders(reminders: List<Reminder>) {
        remindersByDate = reminders.groupBy { reminder ->
            val calendar = Calendar.getInstance()
            calendar.time = reminder.dateTime
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.format(calendar.time)
        }
        notifyDataSetChanged()
    }

    fun setSelectedDate(date: Date?) {
        val oldSelected = selectedDate
        selectedDate = date
        val oldPosition = days.indexOfFirst { 
            it.date?.let { d -> oldSelected?.let { s -> isSameDay(d, s) } } == true 
        }
        val newPosition = days.indexOfFirst { 
            it.date?.let { d -> date?.let { s -> isSameDay(d, s) } } == true 
        }
        if (oldPosition >= 0) notifyItemChanged(oldPosition)
        if (newPosition >= 0) notifyItemChanged(newPosition)
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int = days.size

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayText: TextView = itemView.findViewById(R.id.text_day_number)
        private val indicatorContainer: LinearLayout = itemView.findViewById(R.id.view_indicators)

        fun bind(day: DayItem) {
            dayText.text = day.dayNumber

            if (day.date == null || !day.isCurrentMonth) {
                dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_hint))
                itemView.isEnabled = false
                indicatorContainer.visibility = View.GONE
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
                return
            }

            itemView.isEnabled = true
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(day.date)
            val reminders = remindersByDate[dateKey] ?: emptyList()

            // Configurar colores según estado
            val isSelected = selectedDate?.let { isSameDay(day.date, it) } == true
            val isToday = day.isToday

            when {
                isSelected -> {
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.primary_light))
                    dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                }
                isToday -> {
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.accent))
                    dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                }
                else -> {
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
                    dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary))
                }
            }

            // Mostrar indicadores de categorías
            if (reminders.isNotEmpty()) {
                indicatorContainer.visibility = View.VISIBLE
                val categories = reminders.map { it.category }.distinct()
                setupIndicators(categories)
            } else {
                indicatorContainer.visibility = View.GONE
            }

            itemView.setOnClickListener {
                day.date?.let { onDayClick(it) }
            }
        }

        private fun setupIndicators(categories: List<Category>) {
            // Limpiar indicadores existentes
            indicatorContainer.removeAllViews()

            categories.take(3).forEach { category ->
                val indicator = View(itemView.context).apply {
                    val size = 8.dpToPx(itemView.context)
                    val margin = 2.dpToPx(itemView.context)
                    layoutParams = LinearLayout.LayoutParams(size, size).apply {
                        setMargins(margin, 0, margin, 0)
                    }
                    val color = when (category) {
                        Category.WORK -> R.color.category_work
                        Category.SCHOOL -> R.color.category_school
                        Category.PERSONAL -> R.color.category_personal
                    }
                    setBackgroundColor(ContextCompat.getColor(itemView.context, color))
                }
                indicatorContainer.addView(indicator)
            }
        }

        private fun Int.dpToPx(context: android.content.Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }
    }
}

