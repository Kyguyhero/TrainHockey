package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WorkoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener { finish() }

        val todaysWorkoutButton: Button = findViewById(R.id.todaysWorkoutButton)
        todaysWorkoutButton.setOnClickListener {
            val intent = Intent(this, TodaysWorkoutActivity::class.java)
            startActivity(intent)
        }

        // Get references to TextViews for each day
        val dateMon: TextView = findViewById(R.id.workoutMon)
        val dateTue: TextView = findViewById(R.id.workoutTue)
        val dateWed: TextView = findViewById(R.id.workoutWed)
        val dateThu: TextView = findViewById(R.id.workoutThu)
        val dateFri: TextView = findViewById(R.id.workoutFri)
        val dateSat: TextView = findViewById(R.id.workoutSat)
        val dateSun: TextView = findViewById(R.id.workoutSun)

        val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val days = listOf(dateMon, dateTue, dateWed, dateThu, dateFri, dateSat, dateSun)

        for (dayView in days) {
            val dateText = sdf.format(calendar.time)
            dayView.text = dateText
            dayView.setOnClickListener { openWorkoutEditor(dateText) }
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun openWorkoutEditor(date: String) {
        val intent = Intent(this, WorkoutEditorActivity::class.java)
        intent.putExtra("selectedDate", date)
        startActivity(intent)
    }
}






        // Set up the button to go to previous workouts (for now it just shows a toast)
        //val viewPreviousButton: Button = findViewById(R.id.viewPreviousButton)
        //viewPreviousButton.setOnClickListener {
            // For now, we'll just show a toast to indicate the action
            // You can replace this with navigation to another activity that shows previous workouts
            // or pull data from your database.
        //}

