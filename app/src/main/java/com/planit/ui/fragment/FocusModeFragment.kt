package com.planit.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.planit.R
import com.planit.data.model.ReminderStatus
import com.planit.ui.adapter.ReminderAdapter
import com.planit.ui.viewmodel.FocusModeViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class FocusModeFragment : Fragment() {
    private val viewModel: FocusModeViewModel by viewModels()
    private lateinit var timerText: TextView
    private lateinit var startButton: MaterialButton
    private lateinit var pauseButton: MaterialButton
    private lateinit var stopButton: MaterialButton
    private lateinit var remindersAdapter: ReminderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_focus_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timerText = view.findViewById(R.id.text_timer)
        startButton = view.findViewById(R.id.button_start)
        pauseButton = view.findViewById(R.id.button_pause)
        stopButton = view.findViewById(R.id.button_stop)

        startButton.setOnClickListener {
            viewModel.startTimer()
        }

        pauseButton.setOnClickListener {
            viewModel.pauseTimer()
        }

        stopButton.setOnClickListener {
            viewModel.stopTimer()
        }

        val remindersRecyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_pending_tasks)
        remindersAdapter = ReminderAdapter(
            onItemClick = { /* Navegar a detalle */ },
            onCheckboxClick = { _, _ -> /* No acción aquí */ }
        )
        remindersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        remindersRecyclerView.adapter = remindersAdapter

        viewModel.timeRemaining.observe(viewLifecycleOwner) { remaining ->
            val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60
            timerText.text = String.format("%02d:%02d", minutes, seconds)
        }

        viewModel.isRunning.observe(viewLifecycleOwner) { isRunning ->
            startButton.isEnabled = !isRunning
            pauseButton.isEnabled = isRunning
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getPendingRemindersToday().collect { reminders ->
                val pendingReminders = reminders.filter { it.status != ReminderStatus.COMPLETED }
                remindersAdapter.submitList(pendingReminders)
            }
        }
    }
}

