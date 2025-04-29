package com.example.trainhockey.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trainhockey.R
import com.example.trainhockey.data.Exercise
import android.graphics.Color


class ExerciseAdapter(
    private val exerciseList: MutableList<Exercise>, // must be MutableList to remove items
    private val isCoach: Boolean,
    private val onEditClicked: ((position: Int) -> Unit)? = null,
    private val onCheckClicked: ((position: Int) -> Unit)? = null,
    private val onDeleteClicked: ((position: Int) -> Unit)? = null // ADD THIS
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercise_layout, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exerciseList[position]
        holder.name.text = exercise.name
        holder.description.text =
            "${exercise.description} — ${exercise.sets} sets of ${exercise.reps} reps"

        holder.name.setTextColor(Color.BLACK)
        holder.description.setTextColor(Color.DKGRAY)

        if (isCoach) {
            holder.checkBox.visibility = View.GONE
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                onDeleteClicked?.invoke(holder.adapterPosition)
            }
        } else {
            holder.checkBox.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.GONE

            holder.checkBox.setOnCheckedChangeListener { _, _ ->
                onCheckClicked?.invoke(holder.adapterPosition)
            }
        }
    }


    override fun getItemCount(): Int {
        return exerciseList.size
    }

    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.exerciseName)
        val description: TextView = itemView.findViewById(R.id.exerciseDescription)
        val checkBox: CheckBox = itemView.findViewById(R.id.completeCheckBox)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton) // ADD THIS LINE
    }
}

