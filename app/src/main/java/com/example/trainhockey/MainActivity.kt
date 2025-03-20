package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //testing
        // Set username greeting
        val greetingText = findViewById<TextView>(R.id.greetingText)
        greetingText.text = "Hello," //add username var

        // test todays workout
        val plannedWorkout = findViewById<TextView>(R.id.plannedWorkout)
        plannedWorkout.text = "Planned workout for today: Run 5 miles"// change this to accept DB workouts

        // Set new messages (example)
        val newMessages = findViewById<TextView>(R.id.newMessages)
        newMessages.text = "No new messages."//change to accept new messages

        // Bottom Navigation Buttons (using AppCompatImageButton, not Button)
        val homeButton: AppCompatImageButton = findViewById(R.id.homeButton)
        val workoutsButton: AppCompatImageButton = findViewById(R.id.workoutsButton)
        val profileButton: AppCompatImageButton = findViewById(R.id.profileButton)

        // Navigate to workout page
        workoutsButton.setOnClickListener {
            val intent = Intent(this, WorkoutActivity::class.java)
            startActivity(intent)
        }

        // Navigate to profile page
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Stay on the home page when home button is clicked
        homeButton.setOnClickListener {
            // Just to show we're on the home page already.
        }
    }
}
