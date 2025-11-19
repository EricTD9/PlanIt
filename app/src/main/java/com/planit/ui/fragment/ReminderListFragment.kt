package com.planit.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.planit.R
import com.planit.data.model.Reminder
import com.planit.data.model.ReminderStatus
import com.planit.ui.adapter.ReminderAdapter
import com.planit.ui.viewmodel.ReminderListViewModel
import com.planit.PlanItApplication
import kotlinx.coroutines.launch

class ReminderListFragment : Fragment() {
    private val viewModel: ReminderListViewModel by viewModels()
    private lateinit var adapter: ReminderAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var buttonToday: MaterialButton
    private lateinit var buttonWeek: MaterialButton
    private lateinit var buttonAll: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminder_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view_reminders)
        emptyStateText = view.findViewById(R.id.text_empty_state)
        
        buttonToday = view.findViewById(R.id.button_today)
        buttonWeek = view.findViewById(R.id.button_week)
        buttonAll = view.findViewById(R.id.button_all)

        adapter = ReminderAdapter(
            onItemClick = { reminder ->
                findNavController().navigate(
                    R.id.nav_reminder_detail,
                    Bundle().apply {
                        putLong("reminderId", reminder.id)
                    }
                )
            },
            onCheckboxClick = { reminder, isChecked ->
                if (isChecked) {
                    viewModel.completeReminder(reminder)
                } else {
                    // Desmarcar - cambiar a pendiente
                    viewLifecycleOwner.lifecycleScope.launch {
                        val app = requireActivity().application as PlanItApplication
                        val updatedReminder = reminder.copy(status = ReminderStatus.PENDING)
                        app.repository.updateReminder(updatedReminder)
                    }
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Configurar listeners de los botones
        buttonToday.setOnClickListener {
            viewModel.setFilterType(ReminderListViewModel.FilterType.TODAY)
            updateButtonStates(ReminderListViewModel.FilterType.TODAY)
        }

        buttonWeek.setOnClickListener {
            viewModel.setFilterType(ReminderListViewModel.FilterType.WEEK)
            updateButtonStates(ReminderListViewModel.FilterType.WEEK)
        }

        buttonAll.setOnClickListener {
            viewModel.setFilterType(ReminderListViewModel.FilterType.ALL)
            updateButtonStates(ReminderListViewModel.FilterType.ALL)
        }

        // Observar cambios en el filtro para actualizar los botones
        viewModel.filterType.observe(viewLifecycleOwner) { filterType ->
            updateButtonStates(filterType)
        }

        // Inicializar estado de los botones
        updateButtonStates(ReminderListViewModel.FilterType.TODAY)

        // Observar el Flow de recordatorios que se actualiza automáticamente cuando cambia el filtro
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.reminders.collect { reminders ->
                adapter.submitList(reminders)
                emptyStateText.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateButtonStates(selectedFilter: ReminderListViewModel.FilterType) {
        // Resetear todos los botones
        buttonToday.isSelected = false
        buttonWeek.isSelected = false
        buttonAll.isSelected = false

        // Aplicar estilo al botón seleccionado
        when (selectedFilter) {
            ReminderListViewModel.FilterType.TODAY -> {
                buttonToday.isSelected = true
                buttonToday.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                buttonToday.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                buttonWeek.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                buttonWeek.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                buttonAll.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                buttonAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            ReminderListViewModel.FilterType.WEEK -> {
                buttonWeek.isSelected = true
                buttonWeek.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                buttonWeek.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                buttonToday.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                buttonToday.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                buttonAll.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                buttonAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            ReminderListViewModel.FilterType.ALL -> {
                buttonAll.isSelected = true
                buttonAll.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                buttonAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                buttonToday.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                buttonToday.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                buttonWeek.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                buttonWeek.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }
    }
}

