package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var workoutHistoryListView: ListView
    private lateinit var personalInfoTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        workoutHistoryListView = findViewById(R.id.workoutHistoryList)
        personalInfoTextView = findViewById(R.id.personalInfo)
        logoutButton = findViewById(R.id.logoutButton)
        backButton = findViewById(R.id.backButton)

        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        // 🔹 Load user info
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "N/A"
                        val email = document.getString("email") ?: "N/A"
                        personalInfoTextView.text = "Name: $name\nEmail: $email"
                    } else {
                        personalInfoTextView.text = "User info not found."
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileActivity", "Error loading user info", e)
                    personalInfoTextView.text = "Failed to load user info."
                }
        } else {
            personalInfoTextView.text = "User not signed in."
        }

        // 🔹 Load workout history
        val workoutDates = mutableListOf<String>()

        db.collection("daily_workouts")
            .get()
            .addOnSuccessListener { result ->
                workoutDates.clear()
                for (document in result) {
                    workoutDates.add(document.id)  // Assumes document ID is date
                }
                workoutDates.sortDescending()

                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, workoutDates)
                workoutHistoryListView.adapter = adapter
            }
            .addOnFailureListener { e ->
                Log.e("ProfileActivity", "Error loading workouts", e)
                Toast.makeText(this, "Failed to load workout history", Toast.LENGTH_SHORT).show()
            }

        // 🔹 Workout item click (extend this to show detailed view)
        workoutHistoryListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDate = workoutDates[position]
            Toast.makeText(this, "Workout on $selectedDate", Toast.LENGTH_SHORT).show()

            // Example: open WorkoutDetailActivity
            // val intent = Intent(this, WorkoutDetailActivity::class.java)
            // intent.putExtra("WORKOUT_DATE", selectedDate)
            // startActivity(intent)
        }

        // 🔹 Logout
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // 🔹 Back button
        backButton.setOnClickListener {
            finish()
        }
    }
}
