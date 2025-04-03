package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trainhockey.adapters.ExerciseAdapter
import com.example.trainhockey.data.Exercise
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutEditorActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var onIceRecyclerView: RecyclerView
    private lateinit var offIceRecyclerView: RecyclerView
    private lateinit var onIceAdapter: ExerciseAdapter
    private lateinit var offIceAdapter: ExerciseAdapter
    private val onIceExerciseList = mutableListOf<Exercise>()
    private val offIceExerciseList = mutableListOf<Exercise>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_editor)

        db = FirebaseFirestore.getInstance()

        onIceRecyclerView = findViewById(R.id.onIceRecyclerView)
        offIceRecyclerView = findViewById(R.id.offIceRecyclerView)

        onIceAdapter = ExerciseAdapter(onIceExerciseList)
        offIceAdapter = ExerciseAdapter(offIceExerciseList)

        onIceRecyclerView.layoutManager = LinearLayoutManager(this)
        offIceRecyclerView.layoutManager = LinearLayoutManager(this)

        onIceRecyclerView.adapter = onIceAdapter
        offIceRecyclerView.adapter = offIceAdapter

        onIceRecyclerView.visibility = RecyclerView.GONE
        offIceRecyclerView.visibility = RecyclerView.GONE

        val btnAddNewOnIce = findViewById<Button>(R.id.btnAddNewOnIce)
        val btnSelectOnIce = findViewById<Button>(R.id.btnSelectOnIce)
        val btnAddNewOffIce = findViewById<Button>(R.id.btnAddNewOffIce)
        val btnSelectOffIce = findViewById<Button>(R.id.btnSelectOffIce)

        // 🔐 Get user type from Firestore
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            db.collection("users").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userType = document.getString("userType") ?: "regular"
                        if (userType == "player") {
                            // View-only mode for players
                            disableEditing(btnAddNewOnIce, btnSelectOnIce, btnAddNewOffIce, btnSelectOffIce)
                            Toast.makeText(this, "Viewing mode only", Toast.LENGTH_SHORT).show()
                        } else {
                            // Coaches or regular users can edit
                            setupWorkoutButtons()
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load user role", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun disableEditing(vararg buttons: Button) {
        for (btn in buttons) {
            btn.isEnabled = false
            btn.alpha = 0.4f
        }
    }

    private fun setupWorkoutButtons() {
        val btnAddNewOnIce = findViewById<Button>(R.id.btnAddNewOnIce)
        val btnSelectOnIce = findViewById<Button>(R.id.btnSelectOnIce)
        val btnAddNewOffIce = findViewById<Button>(R.id.btnAddNewOffIce)
        val btnSelectOffIce = findViewById<Button>(R.id.btnSelectOffIce)

        btnAddNewOnIce.setOnClickListener {
            val intent = Intent(this, AddExerciseActivity::class.java)
            intent.putExtra("category", "onIce")
            startActivity(intent)
        }

        btnSelectOnIce.setOnClickListener {
            showExerciseSelectionDialog(isOnIce = true)
            onIceRecyclerView.visibility = RecyclerView.VISIBLE
        }

        btnAddNewOffIce.setOnClickListener {
            val intent = Intent(this, AddExerciseActivity::class.java)
            intent.putExtra("category", "offIce")
            startActivity(intent)
        }

        btnSelectOffIce.setOnClickListener {
            showExerciseSelectionDialog(isOnIce = false)
            offIceRecyclerView.visibility = RecyclerView.VISIBLE
        }
    }

    private fun showExerciseSelectionDialog(isOnIce: Boolean) {
        db.collection("exercises")
            .whereEqualTo("isOnIce", isOnIce)
            .get()
            .addOnSuccessListener { snapshot ->
                val exercises = mutableListOf<Exercise>()
                val exerciseNames = mutableListOf<String>()

                Log.d("WorkoutEditor", "Found ${snapshot.size()} documents for isOnIce=$isOnIce")

                for (doc in snapshot.documents) {
                    Log.d("WorkoutEditor", "Doc ${doc.id}: ${doc.data}")
                    val exercise = doc.toObject(Exercise::class.java)
                    if (exercise != null && exercise.name.isNotEmpty()) {
                        exercises.add(exercise)
                        exerciseNames.add(exercise.name)
                    }
                }

                if (exerciseNames.isEmpty()) {
                    Toast.makeText(this, "No exercises found. Please add one.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_exercise, null)
                val listView = dialogView.findViewById<ListView>(R.id.exerciseListView)
                val addButton = dialogView.findViewById<Button>(R.id.addNewExerciseButton)
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, exerciseNames)
                listView.adapter = adapter

                val dialog = AlertDialog.Builder(this)
                    .setTitle("Select an Exercise")
                    .setView(dialogView)
                    .setNegativeButton("Cancel", null)
                    .create()

                dialog.show()

                listView.setOnItemClickListener { _, _, position, _ ->
                    val selected = exercises[position]
                    if (isOnIce) {
                        onIceExerciseList.add(selected)
                        onIceAdapter.notifyDataSetChanged()
                    } else {
                        offIceExerciseList.add(selected)
                        offIceAdapter.notifyDataSetChanged()
                    }
                    Toast.makeText(this, "${selected.name} added", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }

                addButton.setOnClickListener {
                    val intent = Intent(this, AddExerciseActivity::class.java)
                    intent.putExtra("category", if (isOnIce) "onIce" else "offIce")
                    startActivity(intent)
                    dialog.dismiss()
                }
            }
            .addOnFailureListener { error ->
                Log.e("WorkoutEditor", "Error fetching exercises: ", error)
                Toast.makeText(this, "Failed to load exercises.", Toast.LENGTH_SHORT).show()
            }
    }
}
