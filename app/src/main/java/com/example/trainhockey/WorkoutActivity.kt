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
    private lateinit var markCompleteButton: Button
    private lateinit var btnPreviousWeek: ImageButton
    private lateinit var btnNextWeek: ImageButton
    private lateinit var weekLabel: TextView

    private lateinit var workoutDao: WorkoutDao
    private var currentUserId: String = ""
    private var selectedDate: String = ""
    private var isCoach: Boolean = false
    private var currentWeekCalendar: Calendar = Calendar.getInstance()
    private lateinit var dayViews: List<TextView>

    private val sdfDisplay = SimpleDateFormat("d MMM", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        currentUserId = intent.getStringExtra("userUID") ?: ""

        val userType = intent.getStringExtra("userType") ?: "Player"
        isCoach = userType == "Coach"
        selectedDate = intent.getStringExtra("selectedDate") ?: getTodayDate()

        workoutDao = WorkoutDao(this)
        currentWeekCalendar.time = parseDate(selectedDate)

        goalDisplayText = findViewById(R.id.goalDisplayText)
        goalEditText = findViewById(R.id.goalEditText)
        saveGoalButton = findViewById(R.id.saveGoalButton)
        editGoalButton = findViewById(R.id.editGoalButton)
        saveWorkoutButton = findViewById(R.id.saveWorkoutButton)
        markCompleteButton = findViewById(R.id.markCompleteButton)
        btnPreviousWeek = findViewById(R.id.btnPreviousWeek)
        btnNextWeek = findViewById(R.id.btnNextWeek)
        weekLabel = findViewById(R.id.weekLabel)

        onIceRecyclerView = findViewById(R.id.onIceTodayRecyclerView)
        offIceRecyclerView = findViewById(R.id.offIceTodayRecyclerView)

        onIceAdapter = ExerciseAdapter(onIceList, isCoach,
            onEditClicked = { position ->
                showRepsSetsDialog(onIceList[position]) { reps, sets ->
                    onIceList[position] = onIceList[position].copy(reps = reps, sets = sets)
                    onIceAdapter.notifyItemChanged(position)
                }
            },
            onCheckClicked = { position ->
                Toast.makeText(this, "Marked ${onIceList[position].name} complete", Toast.LENGTH_SHORT).show()
            }
        )

        offIceAdapter = ExerciseAdapter(offIceList, isCoach,
            onEditClicked = { position ->
                showRepsSetsDialog(offIceList[position]) { reps, sets ->
                    offIceList[position] = offIceList[position].copy(reps = reps, sets = sets)
                    offIceAdapter.notifyItemChanged(position)
                }
            },
            onCheckClicked = { position ->
                Toast.makeText(this, "Marked ${offIceList[position].name} complete", Toast.LENGTH_SHORT).show()
            }
        )

        onIceRecyclerView.layoutManager = LinearLayoutManager(this)
        offIceRecyclerView.layoutManager = LinearLayoutManager(this)
        onIceRecyclerView.adapter = onIceAdapter
        offIceRecyclerView.adapter = offIceAdapter

        findViewById<Button>(R.id.addOnIceExerciseButton).setOnClickListener {
            showExercisePickerDialog(isOnIce = true)
        }
        findViewById<Button>(R.id.addOffIceExerciseButton).setOnClickListener {
            showExercisePickerDialog(isOnIce = false)
        }
        saveWorkoutButton.setOnClickListener { saveWorkout() }

        editGoalButton.setOnClickListener {
            goalEditText.setText(goalDisplayText.text.toString().removePrefix("Goal: "))
            goalEditText.visibility = View.VISIBLE
            saveGoalButton.visibility = View.VISIBLE
            goalDisplayText.visibility = View.GONE
            editGoalButton.visibility = View.GONE
        }
        saveGoalButton.setOnClickListener {
            val goal = goalEditText.text.toString().trim()
            goalDisplayText.text = if (goal.isNotEmpty()) "Goal: $goal" else "No goal set"
            goalEditText.visibility = View.GONE
            saveGoalButton.visibility = View.GONE
            goalDisplayText.visibility = View.VISIBLE
            editGoalButton.visibility = View.VISIBLE
        }

        btnPreviousWeek.setOnClickListener {
            currentWeekCalendar.add(Calendar.WEEK_OF_YEAR, -1)
            updateWeekFromCalendar()
        }
        btnNextWeek.setOnClickListener {
            currentWeekCalendar.add(Calendar.WEEK_OF_YEAR, 1)
            updateWeekFromCalendar()
        }

        if (!isCoach) {
            goalEditText.visibility = View.GONE
            saveGoalButton.visibility = View.GONE
            editGoalButton.visibility = View.GONE
            findViewById<Button>(R.id.addOnIceExerciseButton).visibility = View.GONE
            findViewById<Button>(R.id.addOffIceExerciseButton).visibility = View.GONE
            saveWorkoutButton.visibility = View.GONE

            markCompleteButton.visibility = View.VISIBLE
            val isCompleted = workoutDao.isWorkoutCompleted(selectedDate, currentUserId)
            if (isCompleted) {
                markCompleteButton.text = "✔ Workout Completed"
                markCompleteButton.isEnabled = false
            }
            markCompleteButton.setOnClickListener {
                val marked = workoutDao.markWorkoutCompleted(selectedDate, currentUserId)
                if (marked) {
                    markCompleteButton.text = "✔ Workout Completed"
                    markCompleteButton.isEnabled = false
                    Toast.makeText(this, "Workout marked as complete!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            markCompleteButton.visibility = View.GONE
        }

        updateWeekFromCalendar()
    }

    private fun highlightSelectedDay(selectedView: TextView) {
        dayViews.forEach {
            it.setBackgroundColor(resources.getColor(android.R.color.transparent))
        }
        selectedView.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
    }

    private fun saveWorkout() {
        val goal = goalDisplayText.text.toString().removePrefix("Goal: ").trim()
        val exists = workoutDao.workoutExists(selectedDate, currentUserId)
        val success = if (exists) {
            workoutDao.updateWorkout(selectedDate, goal, currentUserId, onIceList, offIceList)
        } else {
            workoutDao.saveWorkout(selectedDate, goal, currentUserId, onIceList, offIceList)
        }
        Toast.makeText(this, if (success) "Workout saved!" else "Failed to save", Toast.LENGTH_SHORT).show()
    }

    private fun updateWeekFromCalendar() {
        val monday = currentWeekCalendar.clone() as Calendar
        monday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val displayRange = "${sdfDisplay.format(monday.time)} - ${sdfDisplay.format(monday.apply { add(Calendar.DAY_OF_MONTH, 6) }.time)}"
        weekLabel.text = displayRange
        setupWeeklyDates(sdfDisplay.format(currentWeekCalendar.time))
    }

    private fun setupWeeklyDates(referenceDate: String) {
        val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
        val parsedDate = parseDate(referenceDate)
        val calendar = Calendar.getInstance()
        calendar.time = parsedDate
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        dayViews = listOf(
            findViewById(R.id.workoutMon),
            findViewById(R.id.workoutTue),
            findViewById(R.id.workoutWed),
            findViewById(R.id.workoutThu),
            findViewById(R.id.workoutFri),
            findViewById(R.id.workoutSat),
            findViewById(R.id.workoutSun)
        )

        dayViews.forEach { dayView ->
            val dateText = sdf.format(calendar.time)
            dayView.text = dateText
            dayView.setOnClickListener {
                selectedDate = dateText
                highlightSelectedDay(dayView)
                loadWorkoutForDate(selectedDate)
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun loadWorkoutForDate(date: String) {
        onIceList.clear()
        offIceList.clear()

        val workout = workoutDao.getWorkoutForDate(date, currentUserId)
        if (workout != null) {
            val (goal, exercises) = workout
            goalDisplayText.text = "Goal: $goal"
            goalDisplayText.visibility = View.VISIBLE
            goalEditText.visibility = View.GONE
            saveGoalButton.visibility = View.GONE
            editGoalButton.visibility = if (isCoach) View.VISIBLE else View.GONE

            exercises.forEach { exercise ->
                if (exercise.isOnIce) onIceList.add(exercise)
                else offIceList.add(exercise)
            }
        } else {
            goalDisplayText.text = "No goal set"
            editGoalButton.visibility = if (isCoach) View.VISIBLE else View.GONE
        }

        onIceAdapter.notifyDataSetChanged()
        offIceAdapter.notifyDataSetChanged()
    }

    private fun getTodayDate(): String {
        return sdfDisplay.format(Date())
    }

    private fun parseDate(date: String): Date {
        return try {
            sdfDisplay.parse(date) ?: Date()
        } catch (e: Exception) {
            Date()
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
                        val intent = Intent(this, AddExerciseActivity::class.java)
                        intent.putExtra("category", if (isOnIce) "onIce" else "offIce")
                        startActivity(intent)
                    } else {
                        showRepsSetsDialog(dialogExercises[which]) { reps, sets ->
                            val selected = dialogExercises[which].copy(reps = reps, sets = sets)
                            if (isOnIce) {
                                onIceList.add(selected)
                                onIceAdapter.notifyItemInserted(onIceList.size - 1)
                            } else {
                                offIceList.add(selected)
                                offIceAdapter.notifyItemInserted(offIceList.size - 1)
                            }
                        }
                    }
                }
                builder.setNegativeButton("Cancel", null)
                builder.show()
            }
        }.start()
    }

    private fun showRepsSetsDialog(exercise: Exercise, onConfirm: (Int, Int) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reps_sets, null)
        val repsInput = dialogView.findViewById<EditText>(R.id.repsInput)
        val setsInput = dialogView.findViewById<EditText>(R.id.setsInput)

        AlertDialog.Builder(this@WorkoutActivity)
            .setTitle("Set Reps and Sets")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val reps = repsInput.text.toString().toIntOrNull() ?: 0
                val sets = setsInput.text.toString().toIntOrNull() ?: 0
                onConfirm(reps, sets)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}