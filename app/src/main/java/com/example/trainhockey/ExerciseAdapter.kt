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

class ExerciseAdapter(private val exerciseList: List<Exercise>,
                      private val isCoach: Boolean,
                      private val onEditClicked: ((position: Int) -> Unit)? = null,
                      private val onCheckClicked: ((position: Int) -> Unit)? = null) :
    RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercise_layout, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exerciseList[position]
        holder.name.text = exercise.name
        holder.description.text = "${exercise.description} — ${exercise.sets} sets of ${exercise.reps} reps"

        if (isCoach) {
            holder.editButton.visibility = View.VISIBLE
            holder.editButton.setOnClickListener {
                onEditClicked?.invoke(position)
            }
            holder.checkBox.visibility = View.GONE
        } else {
            holder.editButton.visibility = View.GONE
            holder.checkBox.visibility = View.VISIBLE

            holder.checkBox.isChecked = false // or pull saved state later
            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                onCheckClicked?.invoke(position)
            }
        }
    }


    override fun getItemCount(): Int {
        return exerciseList.size
    }

    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.exerciseName)
        val description: TextView = itemView.findViewById(R.id.exerciseDescription)
        val editButton: ImageButton = itemView.findViewById(R.id.editRepsSetsButton)
        val checkBox: CheckBox = itemView.findViewById(R.id.completeCheckBox)
    }
}
