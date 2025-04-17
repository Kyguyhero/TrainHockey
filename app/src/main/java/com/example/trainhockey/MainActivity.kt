package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.example.trainhockey.data.LocalUserDao
import com.example.trainhockey.data.User
import com.example.trainhockey.data.WorkoutDao
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var greetingText: TextView
    private lateinit var plannedWorkout: TextView
    private lateinit var newMessages: TextView

    private lateinit var userDao: LocalUserDao
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Init DAO and views
        userDao = LocalUserDao(this)
        greetingText = findViewById(R.id.greetingText)
        plannedWorkout = findViewById(R.id.plannedWorkout)
        newMessages = findViewById(R.id.newMessages)


        // Get user ID and load user
        val userId = intent.getStringExtra("userUID")
        if (userId != null) {
            currentUser = userDao.getUserById(userId)
        }

        // Inside onCreate
        val workoutDao = WorkoutDao(this)
        val workoutContainer = findViewById<LinearLayout>(R.id.todaysWorkoutContainer)
        val todayDate = getTodayDate()

        if (currentUser != null) {
            val todayWorkout = workoutDao.getWorkoutForDate(todayDate, currentUser!!.id)

            if (todayWorkout != null) {
                val (goal, exercises) = todayWorkout

                // Optional: display goal
                if (goal.isNotEmpty()) {
                    val goalView = TextView(this).apply {
                        text = "Goal: $goal"
                        textSize = 16f
                        setPadding(0, 0, 0, 12)
                    }
                    workoutContainer.addView(goalView)
                }

                exercises.forEach { exercise ->
                    val exView = TextView(this).apply {
                        text = "• ${exercise.name} (${exercise.sets} sets x ${exercise.reps} reps)"
                        textSize = 15f
                        setPadding(0, 4, 0, 4)
                    }
                    workoutContainer.addView(exView)
                }
            } else {
                val noWorkoutView = TextView(this).apply {
                    text = "No workout found for today"
                    textSize = 15f
                }
                workoutContainer.addView(noWorkoutView)
            }
        }

        //  /////////////// end todays workout card


        // Greet the user
        greetingText.text = if (currentUser != null) {
            "Hello, ${currentUser?.name}"
        } else {
            "Hello, Guest"
        }

        // Placeholder workout
        plannedWorkout.text = "Planned workout for today: Run 5 miles"

        // Example click to messages
        newMessages.setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }

        // Bottom navigation buttons
        val homeButton: AppCompatImageButton = findViewById(R.id.homeButton)
        val workoutsButton: AppCompatImageButton = findViewById(R.id.workoutsButton)
        val profileButton: AppCompatImageButton = findViewById(R.id.profileButton)

        workoutsButton.setOnClickListener {
            val intent = Intent(this, WorkoutActivity::class.java)
            intent.putExtra("userUID", currentUser?.id)
            intent.putExtra("userType", currentUser?.userType)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userUID", currentUser?.id)
            intent.putExtra("userType", currentUser?.userType)
            startActivity(intent)

        }

        homeButton.setOnClickListener {
            // Already on home
        }

    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
        return sdf.format(Date())
    }
}
