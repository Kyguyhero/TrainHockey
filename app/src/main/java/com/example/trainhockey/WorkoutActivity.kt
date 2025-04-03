package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trainhockey.adapters.ExerciseAdapter
import com.example.trainhockey.data.Exercise
import java.text.SimpleDateFormat
import java.util.*

class WorkoutActivity : AppCompatActivity() {

    private lateinit var goalDisplayText: TextView
    private lateinit var goalEditText: EditText
    private lateinit var saveGoalButton: Button
    private lateinit var editGoalButton: Button

    private lateinit var onIceRecyclerView: RecyclerView
    private lateinit var offIceRecyclerView: RecyclerView
    private lateinit var onIceAdapter: ExerciseAdapter
    private lateinit var offIceAdapter: ExerciseAdapter
    private val onIceList = mutableListOf<Exercise>()
    private val offIceList = mutableListOf<Exercise>()

    private lateinit var saveWorkoutButton: Button
    private lateinit var previousWorkoutsButton: Button
    private lateinit var todaysWorkoutButton: Button

    private var savedGoal: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        setupWeeklyDates()

        // Goal controls
        goalDisplayText = findViewById(R.id.goalDisplayText)
        goalEditText = findViewById(R.id.goalEditText)
        saveGoalButton = findViewById(R.id.saveGoalButton)
        editGoalButton = findViewById(R.id.editGoalButton)

        saveGoalButton.setOnClickListener {
            val goal = goalEditText.text.toString().trim()
            if (goal.isNotEmpty()) {
                savedGoal = goal
                goalDisplayText.text = "Goal: $savedGoal"
                Toast.makeText(this, "Goal saved", Toast.LENGTH_SHORT).show()
            } else {
                goalDisplayText.text = "No goal set"
            }

            goalEditText.visibility = View.GONE
            saveGoalButton.visibility = View.GONE
            goalDisplayText.visibility = View.VISIBLE
            editGoalButton.visibility = View.VISIBLE
        }

        editGoalButton.setOnClickListener {
            goalEditText.setText(savedGoal)
            goalEditText.visibility = View.VISIBLE
            saveGoalButton.visibility = View.VISIBLE
            goalDisplayText.visibility = View.GONE
            editGoalButton.visibility = View.GONE
        }

        // RecyclerViews setup
        onIceRecyclerView = findViewById(R.id.onIceTodayRecyclerView)
        offIceRecyclerView = findViewById(R.id.offIceTodayRecyclerView)

        onIceAdapter = ExerciseAdapter(onIceList)
        offIceAdapter = ExerciseAdapter(offIceList)

        onIceRecyclerView.layoutManager = LinearLayoutManager(this)
        offIceRecyclerView.layoutManager = LinearLayoutManager(this)

        onIceRecyclerView.adapter = onIceAdapter
        offIceRecyclerView.adapter = offIceAdapter

        // Buttons
        saveWorkoutButton = findViewById(R.id.saveWorkoutButton)
        previousWorkoutsButton = findViewById(R.id.previousWorkoutsButton)


        saveWorkoutButton.setOnClickListener {
            Toast.makeText(this, "Workout saved (functionality not implemented)", Toast.LENGTH_SHORT).show()
        }

        previousWorkoutsButton.setOnClickListener {
            Toast.makeText(this, "Viewing previous workouts (placeholder)", Toast.LENGTH_SHORT).show()
        }


    }

    private fun setupWeeklyDates() {
        val dayViews = listOf(
            findViewById<TextView>(R.id.workoutMon),
            findViewById<TextView>(R.id.workoutTue),
            findViewById<TextView>(R.id.workoutWed),
            findViewById<TextView>(R.id.workoutThu),
            findViewById<TextView>(R.id.workoutFri),
            findViewById<TextView>(R.id.workoutSat),
            findViewById<TextView>(R.id.workoutSun)
        )

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val sdf = SimpleDateFormat("d MMM", Locale.getDefault())

        for (textView in dayViews) {
            textView.text = sdf.format(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}
