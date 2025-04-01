package com.example.trainhockey

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trainhockey.data.Exercise

class ExerciseAdapter(
    private val exercises: List<Exercise>,
    private val onExerciseUpdate: (Int, Int, Int) -> Unit // Callback to update reps and sets
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercise_layout, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.exerciseName.text = exercise.name
        holder.exerciseDescription.text = exercise.description
        holder.repsInput.setText(exercise.repsPerSet.firstOrNull()?.toString() ?: "0")
        holder.setsInput.setText(exercise.sets.toString())

        // Update the exercise when the user enters reps or sets
        holder.repsInput.setOnFocusChangeListener { _, _ ->
            val reps = holder.repsInput.text.toString().toIntOrNull() ?: 0
            val sets = holder.setsInput.text.toString().toIntOrNull() ?: 0
            onExerciseUpdate(position, reps, sets)
        }

        holder.setsInput.setOnFocusChangeListener { _, _ ->
            val reps = holder.repsInput.text.toString().toIntOrNull() ?: 0
            val sets = holder.setsInput.text.toString().toIntOrNull() ?: 0
            onExerciseUpdate(position, reps, sets)
        }
    }

    override fun getItemCount(): Int = exercises.size

    class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val exerciseName: TextView = view.findViewById(R.id.exerciseName)
        val exerciseDescription: TextView = view.findViewById(R.id.exerciseDescription)
        val repsInput: EditText = view.findViewById(R.id.repsInput)
        val setsInput: EditText = view.findViewById(R.id.setsInput)
    }
}

private fun Int.firstOrNull() {
    TODO("Not yet implemented")
}
