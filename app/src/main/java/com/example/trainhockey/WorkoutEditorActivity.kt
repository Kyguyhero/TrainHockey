package com.example.trainhockey

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trainhockey.data.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class WorkoutEditorActivity : AppCompatActivity() {

    private lateinit var onIceRecyclerView: RecyclerView
    private lateinit var offIceRecyclerView: RecyclerView
    private lateinit var addOnIceExerciseButton: Button
    private lateinit var addOffIceExerciseButton: Button
    private lateinit var onIceAdapter: ExerciseAdapter
    private lateinit var offIceAdapter: ExerciseAdapter
    private val onIceExercises = mutableListOf<Exercise>()
    private val offIceExercises = mutableListOf<Exercise>()
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_editor)

        // Initialize views
        onIceRecyclerView = findViewById(R.id.onIceRecyclerView)
        offIceRecyclerView = findViewById(R.id.offIceRecyclerView)
        addOnIceExerciseButton = findViewById(R.id.addOnIceExerciseButton)
        addOffIceExerciseButton = findViewById(R.id.addOffIceExerciseButton)

        // Set up RecyclerViews
        onIceRecyclerView.layoutManager = LinearLayoutManager(this)
        offIceRecyclerView.layoutManager = LinearLayoutManager(this)

        onIceAdapter = ExerciseAdapter(onIceExercises) { position, reps, sets ->
            updateExercise(onIceExercises, position, reps, sets)
        }

        offIceAdapter = ExerciseAdapter(offIceExercises) { position, reps, sets ->
            updateExercise(offIceExercises, position, reps, sets)
        }

        onIceRecyclerView.adapter = onIceAdapter
        offIceRecyclerView.adapter = offIceAdapter

        // Set up button click listeners to add exercises
        addOnIceExerciseButton.setOnClickListener {
            showAddExerciseDialog(true) // true indicates "On-Ice" exercise
        }

        addOffIceExerciseButton.setOnClickListener {
            showAddExerciseDialog(false) // false indicates "Off-Ice" exercise
        }

        // Fetch existing exercises from Firebase Firestore when activity starts
        fetchExercisesFromFirebase()
    }

    private fun showAddExerciseDialog(isOnIce: Boolean) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_exercise, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.exerciseNameInput)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.exerciseDescriptionInput)
        val repsInput = dialogView.findViewById<EditText>(R.id.repsInput)
        val setsInput = dialogView.findViewById<EditText>(R.id.setsInput)
        val isOnIceSwitch = dialogView.findViewById<Switch>(R.id.isOnIceSwitch)

        // Pre-select the category based on the dialog context (On or Off-Ice)
        isOnIceSwitch.isChecked = isOnIce

        AlertDialog.Builder(this)
            .setTitle("Add New Exercise")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()
                val reps = repsInput.text.toString().toIntOrNull() ?: 0
                val sets = setsInput.text.toString().toIntOrNull() ?: 0

                if (name.isNotEmpty() && description.isNotEmpty()) {
                    val newExercise = Exercise(
                        name = name,
                        description = description,
                        repsPerSet = reps,
                        sets = sets,
                        isOnIce = isOnIce
                    )
                    addExerciseToFirestore(newExercise)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addExerciseToFirestore(exercise: Exercise) {
        db.collection("exercises")
            .add(exercise)
            .addOnSuccessListener { documentReference ->
                // Store the Firestore document ID
                exercise.exerciseId = documentReference.id
                if (exercise.isOnIce) {
                    onIceExercises.add(exercise)
                    onIceAdapter.notifyDataSetChanged()
                } else {
                    offIceExercises.add(exercise)
                    offIceAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Log.e("WorkoutEditor", "Error adding exercise", e)
            }
    }

    private fun updateExercise(list: MutableList<Exercise>, position: Int, reps: Int, sets: Int) {
        val updatedExercise = list[position].copy(repsPerSet = reps, sets = sets)
        list[position] = updatedExercise

        // Notify adapter about the change
        if (updatedExercise.isOnIce) {
            onIceAdapter.notifyItemChanged(position)
        } else {
            offIceAdapter.notifyItemChanged(position)
        }
    }

    private fun fetchExercisesFromFirebase() {
        // Fetch exercises categorized as "On-Ice"
        db.collection("exercises")
            .whereEqualTo("isOnIce", true)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null) {
                    for (document in snapshot) {
                        val exercise = document.toObject(Exercise::class.java)
                        exercise.exerciseId = document.id
                        onIceExercises.add(exercise)
                    }
                    onIceAdapter.notifyDataSetChanged()
                }
            }

        // Fetch exercises categorized as "Off-Ice"
        db.collection("exercises")
            .whereEqualTo("isOnIce", false)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null) {
                    for (document in snapshot) {
                        val exercise = document.toObject(Exercise::class.java)
                        exercise.exerciseId = document.id
                        offIceExercises.add(exercise)
                    }
                    offIceAdapter.notifyDataSetChanged()
                }
            }
    }
}
