package com.example.trainhockey
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await



class TodaysWorkoutActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private lateinit var workoutTitle: TextView
    private lateinit var onIceListView: ListView
    private lateinit var offIceListView: ListView
    private lateinit var setGoalButton: Button

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todays_workout)

        workoutTitle = findViewById(R.id.workoutTitle)
        onIceListView = findViewById(R.id.onIceWorkoutsList)
        offIceListView = findViewById(R.id.offIceWorkoutsList)
        setGoalButton = findViewById(R.id.setGoalButton)

        findViewById<ImageButton>(R.id.homeButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<ImageButton>(R.id.workoutsButton).setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
        }

        findViewById<ImageButton>(R.id.profileButton).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        setGoalButton.setOnClickListener {
            showGoalInputDialog()// Example Goal
        }



        loadTodaysWorkout()


    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showGoalInputDialog() {
        val editText = EditText(this)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Set Workout Goal")
            .setMessage("Enter your goal for today:")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val goalText = editText.text.toString()
                if (goalText.isNotEmpty()) {
                    workoutTitle.text = "Today's Workout: $goalText"
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentDate(): String {
        return java.time.LocalDate.now().toString() // Format: YYYY-MM-DD
    }

    @SuppressLint("NewApi")
    private fun loadTodaysWorkout() {
        val today = getCurrentDate()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val doc = db.collection("daily_workouts").document(today).get().await()
                val goal = doc.getString("goal") ?: "No goal set"
                workoutTitle.text = "Today's Workout: $goal"

                val onIceSnapshot = db.collection("daily_workouts").document(today)
                    .collection("on_ice").get().await()

                val offIceSnapshot = db.collection("daily_workouts").document(today)
                    .collection("off_ice").get().await()

                val onIceExercises = onIceSnapshot.documents.mapNotNull { it.getString("name") }
                val offIceExercises = offIceSnapshot.documents.mapNotNull { it.getString("name") }

                onIceListView.adapter = ArrayAdapter(this@TodaysWorkoutActivity, android.R.layout.simple_list_item_1, onIceExercises)
                offIceListView.adapter = ArrayAdapter(this@TodaysWorkoutActivity, android.R.layout.simple_list_item_1, offIceExercises)

            } catch (e: Exception) {
                workoutTitle.text = "Error loading workout"
            }
        }
    }



}
