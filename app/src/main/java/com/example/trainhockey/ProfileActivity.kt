package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.trainhockey.data.AppDatabaseHelper
import com.example.trainhockey.data.LocalUserDao
import com.example.trainhockey.data.User

class ProfileActivity : AppCompatActivity() {

    private lateinit var personalInfoTextView: TextView
    private lateinit var workoutHistoryListView: ListView
    private lateinit var logoutButton: Button

    private lateinit var userDao: LocalUserDao
    private lateinit var dbHelper: AppDatabaseHelper
    private var currentUser: User? = null
    private var workoutDates = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        personalInfoTextView = findViewById(R.id.personalInfo)
        workoutHistoryListView = findViewById(R.id.workoutHistoryList)
        logoutButton = findViewById(R.id.logoutButton)

        userDao = LocalUserDao(this)
        dbHelper = AppDatabaseHelper(this)

        // Get user ID passed from MainActivity
        val userId = intent.getStringExtra("userUID")

        Log.d("ProfileActivity", "Intent userUID = $userId")

// Debug: Print all users in the DB
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users", null)

        Log.d("ProfileActivity", "=== USER TABLE DUMP ===")
        while (cursor.moveToNext()) {
            val logId = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val logName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val logEmail = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            Log.d("ProfileActivity", "User: id=$logId, name=$logName, email=$logEmail")
        }
        cursor.close()

        if (userId != null) {
            currentUser = userDao.getUserById(userId)
        }

        // Load user info
        if (currentUser != null) {
            personalInfoTextView.text = "Name: ${currentUser?.name} ${currentUser?.lastname}\nEmail: ${currentUser?.email}"
        } else {
            personalInfoTextView.text = "User not found."
        }

        // Load workout history
        loadWorkoutDates(userId)

        // Logout
        logoutButton.setOnClickListener {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Workout item click
        workoutHistoryListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDate = workoutDates[position]
            Toast.makeText(this, "Workout on $selectedDate", Toast.LENGTH_SHORT).show()

            // Example: launch WorkoutDetailActivity (optional)
            // val intent = Intent(this, WorkoutDetailActivity::class.java)
            // intent.putExtra("WORKOUT_DATE", selectedDate)
            // startActivity(intent)
        }

        // Bottom Nav
        findViewById<ImageButton>(R.id.homeButton).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("userUID", currentUser?.id)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.workoutsButton).setOnClickListener {
            val intent = Intent(this, WorkoutActivity::class.java)
            intent.putExtra("userUID", currentUser?.id)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.profileButton).setOnClickListener {
            // Already here
        }
    }

    private fun loadWorkoutDates(userId: String?) {
        if (userId == null) return

        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT date FROM workouts WHERE userId = ? ORDER BY date DESC",
            arrayOf(userId)
        )

        workoutDates.clear()
        if (cursor.moveToFirst()) {
            do {
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                workoutDates.add(date)
            } while (cursor.moveToNext())
        }
        cursor.close()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, workoutDates)
        workoutHistoryListView.adapter = adapter
    }
}
