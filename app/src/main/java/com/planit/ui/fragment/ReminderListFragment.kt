package com.planit.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
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

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        tabLayout.addTab(tabLayout.newTab().setText(R.string.today))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.week))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.all))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.setFilterType(ReminderListViewModel.FilterType.TODAY)
                    1 -> viewModel.setFilterType(ReminderListViewModel.FilterType.WEEK)
                    2 -> viewModel.setFilterType(ReminderListViewModel.FilterType.ALL)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getReminders().collect { reminders ->
                adapter.submitList(reminders)
                emptyStateText.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}

