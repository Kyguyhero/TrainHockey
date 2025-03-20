package com.example.trainhockey

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WorkoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Closes this activity and returns to the previous one
        }

        // Get references to the TextViews for each day
        val workoutMon: TextView = findViewById(R.id.workoutMon)
        val workoutTue: TextView = findViewById(R.id.workoutTue)
        val workoutWed: TextView = findViewById(R.id.workoutWed)
        val workoutThu: TextView = findViewById(R.id.workoutThu)
        val workoutFri: TextView = findViewById(R.id.workoutFri)
        val workoutSat: TextView = findViewById(R.id.workoutSat)
        val workoutSun: TextView = findViewById(R.id.workoutSun)

        // Example workout plan testing
        val workoutPlans = listOf(
            "Run 5 miles",  // Monday
            "Leg day",      // Tuesday
            "HIIT workout", // Wednesday
            "Chest day",    // Thursday
            "Back day",     // Friday
            "Rest day",     // Saturday
            "Yoga",         // Sunday
        )

        // Update the workout details for each day
        workoutMon.text = workoutPlans[0]
        workoutTue.text = workoutPlans[1]
        workoutWed.text = workoutPlans[2]
        workoutThu.text = workoutPlans[3]
        workoutFri.text = workoutPlans[4]
        workoutSat.text = workoutPlans[5]
        workoutSun.text = workoutPlans[6]

        // Set up the button to go to previous workouts (for now it just shows a toast)
        //val viewPreviousButton: Button = findViewById(R.id.viewPreviousButton)
        //viewPreviousButton.setOnClickListener {
            // For now, we'll just show a toast to indicate the action
            // You can replace this with navigation to another activity that shows previous workouts
            // or pull data from your database.
        //}
    }
}
