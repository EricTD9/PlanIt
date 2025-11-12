package com.planit.ui.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.planit.R
import com.planit.data.model.ActivityItem

class ActivityEditableAdapter(
    private val onDeleteClick: (ActivityItem) -> Unit,
    private val onTextChange: (Int, String) -> Unit
) : RecyclerView.Adapter<ActivityEditableAdapter.ActivityViewHolder>() {

    private val activities = mutableListOf<Pair<ActivityItem?, String>>()

    fun addActivity() {
        activities.add(Pair(null, ""))
        notifyItemInserted(activities.size - 1)
    }

    fun getActivities(): List<String> {
        return activities.mapNotNull { it.second.takeIf { text -> text.isNotBlank() } }
    }

    fun setActivities(activitiesList: List<String>) {
        activities.clear()
        activities.addAll(activitiesList.map { Pair(null, it) })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_editable, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(activities[position].second, position)
    }

    override fun getItemCount(): Int = activities.size

    inner class ActivityViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val editText: TextInputEditText = itemView.findViewById(R.id.edit_activity)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete_activity)
        private var textWatcher: TextWatcher? = null

        fun bind(text: String, position: Int) {
            // Remover el listener anterior si existe para evitar múltiples listeners
            textWatcher?.let { editText.removeTextChangedListener(it) }
            
            // Actualizar el texto sin disparar el listener
            editText.setText(text)
            
            // Verificar que la posición sigue siendo válida
            if (position >= 0 && position < activities.size) {
                // Crear y agregar el nuevo listener
                textWatcher = object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        // Verificar nuevamente que la posición es válida
                        val currentPosition = bindingAdapterPosition
                        if (currentPosition != RecyclerView.NO_POSITION && currentPosition < activities.size) {
                            activities[currentPosition] = Pair(null, s?.toString() ?: "")
                            onTextChange(currentPosition, s?.toString() ?: "")
                        }
                    }
                }
                editText.addTextChangedListener(textWatcher)
            }

            deleteButton.setOnClickListener {
                val currentPosition = bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION && currentPosition < activities.size) {
                    activities.removeAt(currentPosition)
                    notifyItemRemoved(currentPosition)
                    notifyItemRangeChanged(currentPosition, activities.size - currentPosition)
                }
            }
        }
    }
}

