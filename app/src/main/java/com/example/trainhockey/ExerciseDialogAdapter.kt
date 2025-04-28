package com.example.trainhockey.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trainhockey.R
import com.example.trainhockey.data.Exercise

class ExerciseDialogAdapter(
    private val exercises: MutableList<Exercise>,
    private val onSelect: (Exercise) -> Unit,
    private val onDelete: (Exercise) -> Unit
) : RecyclerView.Adapter<ExerciseDialogAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.exerciseName)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dialog_exercise_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = exercises.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.name.text = exercise.name

        holder.name.setOnClickListener {
            onSelect(exercise)
        }

        holder.deleteButton.setOnClickListener {
            onDelete(exercise)
            exercises.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}