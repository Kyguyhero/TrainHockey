package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    private lateinit var workoutHistoryList: ListView
    private val workoutHistory = listOf(
        "2025-03-25",
        "2025-03-26",
        "2025-03-27",
        "2025-03-28"
    ) // Example workout dates

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Closes this activity and returns to the previous one
        }

        val logoutButton = findViewById<Button>(R.id.logoutButton)

        logoutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Clears current session
        }
        // Workout history list
        workoutHistoryList = findViewById(R.id.workoutHistoryList)

        // Set up ListView adapter
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, workoutHistory)
        workoutHistoryList.adapter = adapter

        // Handle clicking on a workout date
        //workoutHistoryList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
        //    val selectedDate = workoutHistory[position]
        //    val intent = Intent(this, WorkoutDetailActivity::class.java)
        //    intent.putExtra("WORKOUT_DATE", selectedDate)
        //    startActivity(intent)
        }
    }





