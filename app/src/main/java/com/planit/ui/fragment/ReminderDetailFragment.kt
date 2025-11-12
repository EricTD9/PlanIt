package com.planit.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.planit.R
import com.planit.data.model.ReminderStatus
import com.planit.ui.adapter.ActivityAdapter
import com.planit.ui.viewmodel.ReminderDetailViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReminderDetailFragment : Fragment() {
    private val viewModel: ReminderDetailViewModel by viewModels()
    private var reminderId: Long = -1L
    private lateinit var activitiesAdapter: ActivityAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        reminderId = arguments?.getLong("reminderId", -1L) ?: -1L
        return inflater.inflate(R.layout.fragment_reminder_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (reminderId == -1L) {
            findNavController().popBackStack()
            return
        }

        viewModel.loadReminder(reminderId)

        val activitiesRecyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_activities)
        activitiesAdapter = ActivityAdapter { activity, isCompleted ->
            viewModel.updateActivityCompletion(activity, isCompleted)
        }
        activitiesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        activitiesRecyclerView.adapter = activitiesAdapter

        viewModel.reminder.observe(viewLifecycleOwner) { reminder ->
            reminder?.let {
                displayReminder(it, view)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getActivities(reminderId).collect { activities ->
                activitiesAdapter.submitList(activities)
            }
        }

        view.findViewById<MaterialButton>(R.id.button_edit).setOnClickListener {
            // Navegar a crear/editar con el recordatorio
            findNavController().navigate(R.id.nav_create)
        }

        view.findViewById<MaterialButton>(R.id.button_delete).setOnClickListener {
            viewModel.reminder.value?.let { reminder ->
                viewModel.deleteReminder(reminder)
                Toast.makeText(requireContext(), "Recordatorio eliminado", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        view.findViewById<MaterialButton>(R.id.button_complete).setOnClickListener {
            viewModel.reminder.value?.let { reminder ->
                viewModel.markReminderAsCompleted(reminder)
                Toast.makeText(requireContext(), "Recordatorio completado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayReminder(reminder: com.planit.data.model.Reminder, view: View) {
        view.findViewById<TextView>(R.id.text_title).text = reminder.title
        view.findViewById<TextView>(R.id.text_date_time).text = formatDateTime(reminder.dateTime)
        view.findViewById<TextView>(R.id.text_category).text = reminder.category.displayName
        view.findViewById<TextView>(R.id.text_description).apply {
            text = reminder.description
            visibility = if (reminder.description.isNullOrBlank()) View.GONE else View.VISIBLE
        }
        view.findViewById<TextView>(R.id.text_location).apply {
            text = reminder.location?.let { "📍 $it" } ?: ""
            visibility = if (reminder.location.isNullOrBlank()) View.GONE else View.VISIBLE
        }
        view.findViewById<TextView>(R.id.text_status).apply {
            text = when (reminder.status) {
                ReminderStatus.PENDING -> getString(R.string.pending)
                ReminderStatus.IN_PROGRESS -> getString(R.string.in_progress)
                ReminderStatus.COMPLETED -> getString(R.string.completed)
            }
        }
    }

    private fun formatDateTime(date: Date): String {
        val format = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        return format.format(date)
    }
}

