package com.example.trainhockey

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trainhockey.adapters.ExerciseAdapter
import com.example.trainhockey.data.Exercise
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutEditorActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var onIceRecyclerView: RecyclerView
    private lateinit var offIceRecyclerView: RecyclerView
    private lateinit var onIceAdapter: ExerciseAdapter
    private lateinit var offIceAdapter: ExerciseAdapter
    private lateinit var btnAddNewOnIce: Button
    private lateinit var btnSelectOnIce: Button
    private lateinit var btnAddNewOffIce: Button
    private lateinit var btnSelectOffIce: Button
    private var isCoach: Boolean = false

    private val onIceExerciseList = mutableListOf<Exercise>()
    private val offIceExerciseList = mutableListOf<Exercise>()
    private var userUID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_editor)

        // Initialize Firestore and get user UID
        val userType = intent.getStringExtra("userType") ?: "Player"
        isCoach = userType == "Coach"

        userUID = intent.getStringExtra("userUID")

        Log.d("WorkoutEditor", "Received UID: $userUID")

        // Initialize RecyclerViews
        onIceRecyclerView = findViewById(R.id.onIceRecyclerView)
        offIceRecyclerView = findViewById(R.id.offIceRecyclerView)
        onIceAdapter = ExerciseAdapter(onIceExerciseList, isCoach,
            onEditClicked = { position ->
                // Show edit reps/sets dialog
            },
            onCheckClicked = { position ->
                // Mark exercise complete
            }
        )
        offIceAdapter = ExerciseAdapter(offIceExerciseList, isCoach,
            onEditClicked = { position ->
                // Show edit reps/sets dialog
            },
            onCheckClicked = { position ->
                // Mark exercise complete
            }
        )

        onIceRecyclerView.layoutManager = LinearLayoutManager(this)
        offIceRecyclerView.layoutManager = LinearLayoutManager(this)
        onIceRecyclerView.adapter = onIceAdapter
        offIceRecyclerView.adapter = offIceAdapter
        onIceRecyclerView.visibility = View.GONE
        offIceRecyclerView.visibility = View.GONE

        // Initialize Buttons
        btnAddNewOnIce = findViewById(R.id.btnAddNewOnIce)
        btnSelectOnIce = findViewById(R.id.btnSelectOnIce)
        btnAddNewOffIce = findViewById(R.id.btnAddNewOffIce)
        btnSelectOffIce = findViewById(R.id.btnSelectOffIce)

        // Check user role and enable/disable features
        checkUserRole()

        // Toggle visibility and fetch On-Ice Exercises
        btnSelectOnIce.setOnClickListener {
            if (onIceRecyclerView.visibility == View.GONE) {
                fetchOnIceExercises()
                onIceRecyclerView.visibility = View.VISIBLE
            } else {
                onIceRecyclerView.visibility = View.GONE
            }
        }

        // Toggle visibility and fetch Off-Ice Exercises
        btnSelectOffIce.setOnClickListener {
            if (offIceRecyclerView.visibility == View.GONE) {
                fetchOffIceExercises()
                offIceRecyclerView.visibility = View.VISIBLE
            } else {
                offIceRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun checkUserRole() {
        val currentUserId = userUID
        if (currentUserId != null) {
            db.collection("users").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userType = document.getString("userType") ?: "Regular"
                        Log.d("WorkoutEditor", "userType: $userType")
                        if (userType == "Player") {
                            disableEditing(
                                btnAddNewOnIce,
                                btnSelectOnIce,
                                btnAddNewOffIce,
                                btnSelectOffIce
                            )
                            Toast.makeText(this, "Viewing mode only", Toast.LENGTH_SHORT).show()
                        } else {
                            setupWorkoutButtons()
                        }
                    } else {
                        Log.e("WorkoutEditor", "User document does not exist")
                        Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("WorkoutEditor", "Failed to load user role", e)
                    Toast.makeText(this, "Failed to load user role", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            Log.e("WorkoutEditor", "No userUID found in intent")
        }
    }

    private fun setupWorkoutButtons() {
        btnAddNewOnIce.isEnabled = true
        btnSelectOnIce.isEnabled = true
        btnAddNewOffIce.isEnabled = true
        btnSelectOffIce.isEnabled = true
    }

    private fun disableEditing(vararg buttons: Button) {
        buttons.forEach {
            it.isEnabled = false
        }
    }

    private fun fetchOnIceExercises() {
        db.collection("exercises")
            .whereEqualTo("isOnIce", true)
            .get()
            .addOnSuccessListener { documents ->
                onIceExerciseList.clear()
                for (document in documents) {
                    val exercise = document.toObject(Exercise::class.java)
                    onIceExerciseList.add(exercise)
                }
                onIceAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun fetchOffIceExercises() {
        db.collection("exercises")
            .whereEqualTo("isOnIce", false)
            .get()
            .addOnSuccessListener { documents ->
                offIceExerciseList.clear()
                for (document in documents) {
                    val exercise = document.toObject(Exercise::class.java)
                    offIceExerciseList.add(exercise)
                }
                offIceAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}
