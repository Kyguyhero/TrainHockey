package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trainhockey.adapters.ExerciseAdapter
import com.example.trainhockey.data.Exercise
import com.example.trainhockey.data.LocalExerciseDao
import com.example.trainhockey.data.WorkoutDao
import java.text.SimpleDateFormat
import java.util.*

class WorkoutActivity : AppCompatActivity() {
    private lateinit var workoutTitle: TextView



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

    private lateinit var workoutDao: WorkoutDao
    private var currentUserId: String = ""
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        workoutDao = WorkoutDao(this)

        // Get user ID
        currentUserId = intent.getStringExtra("userUID") ?: ""
        selectedDate = getTodayDate()

        // UI
        goalDisplayText = findViewById(R.id.goalDisplayText)
        goalEditText = findViewById(R.id.goalEditText)
        saveGoalButton = findViewById(R.id.saveGoalButton)
        editGoalButton = findViewById(R.id.editGoalButton)
        saveWorkoutButton = findViewById(R.id.saveWorkoutButton)
        previousWorkoutsButton = findViewById(R.id.previousWorkoutsButton)

        // Recycler setup
        onIceRecyclerView = findViewById(R.id.onIceTodayRecyclerView)
        offIceRecyclerView = findViewById(R.id.offIceTodayRecyclerView)
        onIceAdapter = ExerciseAdapter(onIceList)
        offIceAdapter = ExerciseAdapter(offIceList)
        onIceRecyclerView.layoutManager = LinearLayoutManager(this)
        offIceRecyclerView.layoutManager = LinearLayoutManager(this)
        onIceRecyclerView.adapter = onIceAdapter
        offIceRecyclerView.adapter = offIceAdapter

        setupWeeklyDates()
        loadWorkoutForDate(selectedDate)
        findViewById<Button>(R.id.addOnIceExerciseButton).setOnClickListener {
            showExercisePickerDialog(isOnIce = true)
        }

        findViewById<Button>(R.id.addOffIceExerciseButton).setOnClickListener {
            showExercisePickerDialog(isOnIce = false)
        }

        // Goal editing
        saveGoalButton.setOnClickListener {
            val goal = goalEditText.text.toString().trim()
            if (goal.isNotEmpty()) {
                goalDisplayText.text = "Goal: $goal"
                Toast.makeText(this, "Goal saved (not stored yet)", Toast.LENGTH_SHORT).show()
            } else {
                goalDisplayText.text = "No goal set"
            }
            goalEditText.visibility = View.GONE
            saveGoalButton.visibility = View.GONE
            goalDisplayText.visibility = View.VISIBLE
            editGoalButton.visibility = View.VISIBLE
        }

        editGoalButton.setOnClickListener {
            val currentGoal = goalDisplayText.text.toString().removePrefix("Goal: ").trim()
            goalEditText.setText(currentGoal)
            goalEditText.visibility = View.VISIBLE
            saveGoalButton.visibility = View.VISIBLE
            goalDisplayText.visibility = View.GONE
            editGoalButton.visibility = View.GONE
        }

        // Save workout button (not implemented)
        saveWorkoutButton.setOnClickListener {
            Toast.makeText(this, "Workout saving not hooked up yet", Toast.LENGTH_SHORT).show()
        }

        previousWorkoutsButton.setOnClickListener {
            Toast.makeText(this, "Viewing previous workouts (placeholder)", Toast.LENGTH_SHORT).show()
        }
        saveWorkoutButton.setOnClickListener {
            saveWorkout()
        }

    }

    private fun setupWeeklyDates() {
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
            dayView.setOnClickListener {
                selectedDate = dateText
                loadWorkoutForDate(selectedDate)
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun loadWorkoutForDate(date: String) {
        val workoutTitle = findViewById<TextView>(R.id.workoutTitle)
        val formattedTitle = formatDateToTitle(date)
        workoutTitle.text = formattedTitle
        onIceList.clear()
        offIceList.clear()

        val workout = workoutDao.getWorkoutForDate(date, currentUserId)
        if (workout != null) {
            val (goal, exercises) = workout
            goalDisplayText.text = "Goal: $goal"
            goalDisplayText.visibility = View.VISIBLE
            goalEditText.visibility = View.GONE
            saveGoalButton.visibility = View.GONE
            editGoalButton.visibility = View.VISIBLE

            // Separate on-ice and off-ice
            exercises.forEach { exercise ->
                if (exercise.isOnIce) onIceList.add(exercise)
                else offIceList.add(exercise)
            }

        } else {
            goalDisplayText.text = "No goal set"
            editGoalButton.visibility = View.GONE
        }

        onIceAdapter.notifyDataSetChanged()
        offIceAdapter.notifyDataSetChanged()
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
        return sdf.format(Date())
    }
    private fun formatDateToTitle(date: String): String {
        // Converts "7 Apr" to "April 7th"
        val inputFormat = SimpleDateFormat("d MMM", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM d", Locale.getDefault())
        return try {
            val parsedDate = inputFormat.parse(date)
            val formatted = outputFormat.format(parsedDate!!)
            formatted + getDaySuffix(formatted)
        } catch (e: Exception) {
            date // fallback if parsing fails
        }
    }

    private fun getDaySuffix(formattedDate: String): String {
        val day = formattedDate.takeLastWhile { it.isDigit() }.toIntOrNull() ?: return ""
        return when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
    }
    private fun showExercisePickerDialog(isOnIce: Boolean) {
        val exerciseDao = LocalExerciseDao(this)
        val dialogExercises = mutableListOf<Exercise>()

        Thread {
            val allExercises = exerciseDao.getExercises(isOnIce)
            runOnUiThread {
                dialogExercises.clear()
                dialogExercises.addAll(allExercises)

                val exerciseNames = dialogExercises.map { it.name } + "+ Create New Exercise"

                val builder = AlertDialog.Builder(this)
                builder.setTitle("Select Exercise")

                builder.setItems(exerciseNames.toTypedArray()) { _, which ->
                    if (which == dialogExercises.size) {
                        // Create new exercise
                        val intent = Intent(this, AddExerciseActivity::class.java)
                        intent.putExtra("category", if (isOnIce) "onIce" else "offIce")
                        startActivity(intent)
                    } else {
                        val selected = dialogExercises[which]
                        if (isOnIce) {
                            onIceList.add(selected)
                            onIceAdapter.notifyItemInserted(onIceList.size - 1)
                        } else {
                            offIceList.add(selected)
                            offIceAdapter.notifyItemInserted(offIceList.size - 1)
                        }
                    }
                }

                builder.setNegativeButton("Cancel", null)
                builder.show()
            }
        }.start()
    }
    private fun saveWorkout() {
        val goal = goalDisplayText.text.toString().removePrefix("Goal: ").trim()

        if (goal.isEmpty()) {
            Toast.makeText(this, "Set a goal first", Toast.LENGTH_SHORT).show()
            return
        }

        val success = workoutDao.saveWorkout(
            date = selectedDate,
            goal = goal,
            userId = currentUserId,
            onIceExercises = onIceList,
            offIceExercises = offIceList
        )

        if (success) {
            Toast.makeText(this, "Workout saved!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to save workout", Toast.LENGTH_SHORT).show()
        }
    }





}
