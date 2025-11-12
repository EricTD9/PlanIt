package com.planit.ui.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.planit.R
import com.planit.data.model.Category
import com.planit.data.model.RepetitionType
import com.planit.ui.adapter.ActivityEditableAdapter
import com.planit.ui.viewmodel.CreateReminderViewModel
import java.text.SimpleDateFormat
import java.util.*

class CreateReminderFragment : Fragment() {
    private val viewModel: CreateReminderViewModel by viewModels()
    private lateinit var titleEdit: TextInputEditText
    private lateinit var descriptionEdit: TextInputEditText
    private lateinit var dateTimeText: android.widget.TextView
    private lateinit var repetitionGroup: ChipGroup
    private lateinit var categoryGroup: ChipGroup
    private lateinit var activitiesRecyclerView: RecyclerView
    private lateinit var activitiesAdapter: ActivityEditableAdapter
    private var selectedDateTime = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_reminder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleEdit = view.findViewById(R.id.edit_title)
        descriptionEdit = view.findViewById(R.id.edit_description)
        dateTimeText = view.findViewById(R.id.text_date_time)
        repetitionGroup = view.findViewById(R.id.chip_group_repetition)
        categoryGroup = view.findViewById(R.id.chip_group_category)

        updateDateTimeText()

        view.findViewById<View>(R.id.layout_date_time).setOnClickListener {
            showDateTimePicker()
        }

        activitiesRecyclerView = view.findViewById(R.id.recycler_view_activities)
        activitiesAdapter = ActivityEditableAdapter(
            onDeleteClick = {},
            onTextChange = { _, _ -> }
        )
        activitiesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        activitiesRecyclerView.adapter = activitiesAdapter

        view.findViewById<MaterialButton>(R.id.button_add_activity).setOnClickListener {
            activitiesAdapter.addActivity()
        }

        view.findViewById<MaterialButton>(R.id.button_save).setOnClickListener {
            saveReminder()
        }

        view.findViewById<MaterialButton>(R.id.button_cancel).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun showDateTimePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDateTime.set(year, month, dayOfMonth)
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        selectedDateTime.set(Calendar.MINUTE, minute)
                        updateDateTimeText()
                    },
                    selectedDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedDateTime.get(Calendar.MINUTE),
                    false
                ).show()
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateTimeText() {
        val format = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        dateTimeText.text = format.format(selectedDateTime.time)
    }

    private fun saveReminder() {
        val title = titleEdit.text?.toString() ?: ""
        if (title.isBlank()) {
            Toast.makeText(requireContext(), R.string.title_required, Toast.LENGTH_SHORT).show()
            return
        }

        val description = descriptionEdit.text?.toString()?.takeIf { it.isNotBlank() }
        val repetitionType = when (repetitionGroup.checkedChipId) {
            R.id.chip_once -> RepetitionType.ONCE
            R.id.chip_daily -> RepetitionType.DAILY
            R.id.chip_weekly -> RepetitionType.WEEKLY
            else -> RepetitionType.ONCE
        }

        val category = when (categoryGroup.checkedChipId) {
            R.id.chip_work -> Category.WORK
            R.id.chip_school -> Category.SCHOOL
            R.id.chip_personal -> Category.PERSONAL
            else -> Category.PERSONAL
        }

        val activities = activitiesAdapter.getActivities()

        viewModel.saveReminder(
            title = title,
            description = description,
            dateTime = selectedDateTime.time,
            category = category,
            repetitionType = repetitionType,
            location = null,
            hasVibration = true,
            activities = activities
        )

        Toast.makeText(requireContext(), "Recordatorio guardado", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }
}

