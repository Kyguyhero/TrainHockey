package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 🟦 Find views safely (make sure they exist in activity_profile.xml)
        val workoutList = findViewById<ListView>(R.id.workoutHistoryList)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        // 🔄 Example workout list (can be replaced with real data)
        val workouts = listOf("Monday: Skating Drills", "Tuesday: Off-Ice Strength", "Wednesday: Shooting Practice")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, workouts)
        workoutList.adapter = adapter

        // 🔒 Log out logic (replace with Firebase logic if needed)
        logoutButton.setOnClickListener {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }



        // ✅ Navigation Bar Functionality
        findViewById<ImageButton>(R.id.homeButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<ImageButton>(R.id.workoutsButton).setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
        }

        findViewById<ImageButton>(R.id.profileButton).setOnClickListener {
            // Already on this screen – no action needed or you can refresh
        }
    }
}
