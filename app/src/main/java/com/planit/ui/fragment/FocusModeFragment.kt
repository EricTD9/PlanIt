package com.planit.ui.fragment

import android.media.RingtoneManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.planit.R
import com.planit.data.model.ReminderStatus
import com.planit.ui.adapter.ReminderAdapter
import com.planit.ui.viewmodel.FocusModeViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class FocusModeFragment : Fragment() {
    private val viewModel: FocusModeViewModel by viewModels()
    private lateinit var timerText: TextView
    private lateinit var durationText: TextView
    private lateinit var durationSlider: Slider
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
        durationText = view.findViewById(R.id.text_duration_value)
        durationSlider = view.findViewById(R.id.slider_duration)
        startButton = view.findViewById(R.id.button_start)
        pauseButton = view.findViewById(R.id.button_pause)
        stopButton = view.findViewById(R.id.button_stop)

        // Configurar slider de duración
        var isUpdatingFromViewModel = false
        durationSlider.addOnChangeListener { _, value, _ ->
            if (!isUpdatingFromViewModel) {
                val minutes = value.toInt()
                durationText.text = "$minutes min"
                viewModel.setDuration(value)
            }
        }

        // Inicializar slider con el valor del ViewModel
        viewModel.durationMinutes.observe(viewLifecycleOwner) { minutes ->
            isUpdatingFromViewModel = true
            durationSlider.value = minutes
            durationText.text = "${minutes.toInt()} min"
            isUpdatingFromViewModel = false
        }

        // Configurar callback cuando el temporizador termine
        viewModel.setOnTimerComplete {
            showTimerComplete()
        }

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
            onItemClick = { reminder ->
                // Navegar a detalle si es necesario
            },
            onCheckboxClick = { _, _ -> /* No acción aquí */ }
        )
        remindersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        remindersRecyclerView.adapter = remindersAdapter

        // Observar tiempo restante
        viewModel.timeRemaining.observe(viewLifecycleOwner) { remaining ->
            val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60
            timerText.text = String.format("%02d:%02d", minutes, seconds)
        }

        // Observar estado del temporizador
        viewModel.isRunning.observe(viewLifecycleOwner) { isRunning ->
            startButton.isEnabled = !isRunning
            pauseButton.isEnabled = isRunning
            durationSlider.isEnabled = !isRunning
        }

        viewModel.isPaused.observe(viewLifecycleOwner) { isPaused ->
            if (isPaused) {
                pauseButton.text = getString(R.string.start_timer)
            } else {
                pauseButton.text = getString(R.string.pause_timer)
            }
        }

        // Cargar tareas pendientes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getPendingRemindersToday().collect { reminders ->
                val pendingReminders = reminders.filter { it.status != ReminderStatus.COMPLETED }
                remindersAdapter.submitList(pendingReminders)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Actualizar el tiempo restante cuando el Fragment vuelve a estar visible
        viewModel.updateTimeRemaining()
    }

    private fun showTimerComplete() {
        // Vibrar
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = requireContext().getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                requireContext().getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { vib ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(longArrayOf(0, 500, 200, 500), -1)
                }
            }
        } catch (e: Exception) {
            // Ignorar errores de vibración
        }

        // Mostrar notificación
        Toast.makeText(requireContext(), R.string.focus_completed, Toast.LENGTH_LONG).show()
    }
}
