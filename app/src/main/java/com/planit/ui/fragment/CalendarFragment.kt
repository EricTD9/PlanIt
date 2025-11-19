package com.planit.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.planit.R
import com.planit.data.model.Reminder
import com.planit.ui.adapter.CalendarDayAdapter
import com.planit.ui.adapter.ReminderPreviewAdapter
import com.planit.ui.viewmodel.CalendarViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    private val viewModel: CalendarViewModel by activityViewModels()
    private lateinit var calendarAdapter: CalendarDayAdapter
    private lateinit var remindersAdapter: ReminderPreviewAdapter
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var remindersRecyclerView: RecyclerView
    private lateinit var monthYearText: TextView
    private lateinit var selectedDateTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        monthYearText = view.findViewById(R.id.text_month_year)
        selectedDateTitle = view.findViewById(R.id.text_selected_date_title)

        // Configurar RecyclerView del calendario
        calendarRecyclerView = view.findViewById(R.id.recycler_view_calendar)
        calendarAdapter = CalendarDayAdapter { date ->
            viewModel.selectDate(date)
        }
        calendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarRecyclerView.adapter = calendarAdapter

        // Configurar RecyclerView de recordatorios del día
        remindersRecyclerView = view.findViewById(R.id.recycler_view_day_reminders)
        remindersAdapter = ReminderPreviewAdapter { reminder ->
            findNavController().navigate(
                R.id.nav_reminder_detail,
                Bundle().apply {
                    putLong("reminderId", reminder.id)
                }
            )
        }
        remindersRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        remindersRecyclerView.adapter = remindersAdapter

        // Botones de navegación de mes
        view.findViewById<MaterialButton>(R.id.button_prev_month).setOnClickListener {
            viewModel.navigateToPreviousMonth()
        }

        view.findViewById<MaterialButton>(R.id.button_next_month).setOnClickListener {
            viewModel.navigateToNextMonth()
        }

        // FAB para agregar recordatorio
        view.findViewById<FloatingActionButton>(R.id.fab_add_reminder).setOnClickListener {
            val selectedDate = viewModel.selectedDate.value
            val dateToPass = selectedDate ?: Date()
            findNavController().navigate(
                R.id.nav_create,
                Bundle().apply {
                    putLong("selectedDate", dateToPass.time)
                }
            )
        }

        // Observar cambios en el mes actual
        viewModel.currentMonth.observe(viewLifecycleOwner) { calendar ->
            if (calendar != null) {
                updateMonthYearText(calendar)
                generateCalendarDays(calendar)
            }
        }
        
        // Inicializar con el mes actual (o con el mes ya presente en el ViewModel).
        // El ViewModel ahora está en scope de actividad, por lo que su estado
        // persistirá mientras la actividad esté viva. No forzamos reset aquí
        // para no sobrescribir la última selección del usuario al navegar.
        val initialMonth = viewModel.getCurrentMonth()
    // Log para diagnóstico: registrar el mes/año que se usará para mostrar
    android.util.Log.d("CalendarFragment", "initialMonth from ViewModel: ${initialMonth.get(Calendar.YEAR)}-${initialMonth.get(Calendar.MONTH)}")
        updateMonthYearText(initialMonth)
        generateCalendarDays(initialMonth)

        // Observar recordatorios del mes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getRemindersForMonth().collect { reminders ->
                calendarAdapter.setReminders(reminders)
            }
        }

        // Observar fecha seleccionada
        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            if (date != null) {
                selectedDateTitle.visibility = View.VISIBLE
                remindersRecyclerView.visibility = View.VISIBLE
                calendarAdapter.setSelectedDate(date)
                
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.getRemindersForDate(date).collect { reminders ->
                        remindersAdapter.submitList(reminders)
                    }
                }
            } else {
                selectedDateTitle.visibility = View.GONE
                remindersRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun updateMonthYearText(calendar: Calendar) {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        val formatted = monthFormat.format(calendar.time)
        monthYearText.text = formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    private fun generateCalendarDays(monthCalendar: Calendar) {
        val days = mutableListOf<CalendarDayAdapter.DayItem>()
        val calendar = monthCalendar.clone() as Calendar
        
        // Ir al primer día del mes
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Ajustar para que domingo sea 0
        val startOffset = if (firstDayOfWeek == Calendar.SUNDAY) 0 else firstDayOfWeek - Calendar.SUNDAY
        
        // Agregar días vacíos antes del primer día del mes
        for (i in 0 until startOffset) {
            days.add(CalendarDayAdapter.DayItem(null, "", false, false))
        }
        
        // Obtener el último día del mes
        val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = Calendar.getInstance()
        
        // Agregar días del mes
        for (day in 1..lastDay) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val isToday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
            
            days.add(CalendarDayAdapter.DayItem(
                date = calendar.time,
                dayNumber = day.toString(),
                isCurrentMonth = true,
                isToday = isToday
            ))
        }
        
        // Completar hasta 42 días (6 semanas) para mantener el grid consistente
        val remainingDays = 42 - days.size
        for (i in 0 until remainingDays) {
            days.add(CalendarDayAdapter.DayItem(null, "", false, false))
        }
        
        calendarAdapter.setDays(days)
    }
}
