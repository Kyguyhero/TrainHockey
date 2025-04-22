package com.example.trainhockey

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.trainhockey.data.*
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var personalInfoTextView: TextView
    private lateinit var workoutHistoryListView: ListView
    private lateinit var logoutButton: Button
    private lateinit var assignIcon: ImageButton
    private lateinit var teamInfoTextView: TextView


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
        assignIcon = findViewById(R.id.assignIcon)
        teamInfoTextView = findViewById(R.id.teamInfo)

        assignIcon.visibility = View.GONE // hide by default

        userDao = LocalUserDao(this)
        dbHelper = AppDatabaseHelper(this)

        val userId = intent.getStringExtra("userUID")
        if (userId != null) {
            currentUser = userDao.getUserById(userId)
        }

        if (currentUser != null) {
            personalInfoTextView.text =
                "Name:\n${currentUser!!.name} ${currentUser!!.lastname}\n\nEmail:\n${currentUser!!.email}"

            val coachPlayerDao = CoachPlayerDao(this)
            if (currentUser!!.userType == "Coach") {
                assignIcon.visibility = View.VISIBLE
                assignIcon.setOnClickListener {
                    showAssignPlayerDialog(currentUser!!.id)
                }

                val players = coachPlayerDao.getPlayersForCoach(currentUser!!.id)
                val playerList = if (players.isNotEmpty()) {
                    players.joinToString("\n") { "• ${it.name} ${it.lastname}" }
                } else {
                    "• None"
                }
                teamInfoTextView.text = "$playerList"

            } else if (currentUser!!.userType == "Player") {
                val coach = coachPlayerDao.getCoachForPlayer(currentUser!!.id)
                teamInfoTextView.text = coach?.let {
                    "🎯 Assigned Coach:\n\n• ${it.name} ${it.lastname}"
                } ?: "🎯 Assigned Coach:\n\n• None"
            }
        } else {
            personalInfoTextView.text = "User not found."
        }

        loadWorkoutDates(currentUser?.id)



        logoutButton.setOnClickListener {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        workoutHistoryListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDate = workoutDates[position]
            val intent = Intent(this, WorkoutActivity::class.java).apply {
                putExtra("selectedDate", selectedDate)
                putExtra("userUID", currentUser?.id)
                putExtra("userType", currentUser?.userType)
                putExtra("mode", "view")
            }
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.homeButton).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("userUID", currentUser?.id)
            intent.putExtra("userType", currentUser?.userType)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.workoutsButton).setOnClickListener {
            val intent = Intent(this, WorkoutActivity::class.java)
            intent.putExtra("userUID", currentUser?.id)
            intent.putExtra("userType", currentUser?.userType)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.profileButton).setOnClickListener {
            // Already here
        }
    }

    private fun loadWorkoutDates(userId: String?) {
        if (userId == null) return

        val db = dbHelper.readableDatabase
        val uniqueDates = mutableSetOf<String>()

        if (currentUser?.userType == "Coach") {
            val coachPlayerDao = CoachPlayerDao(this)
            val players = coachPlayerDao.getPlayersForCoach(userId)

            for (player in players) {
                val cursor = db.rawQuery(
                    """
                    SELECT DISTINCT w.date FROM workouts w
                    JOIN workout_assignments wa ON w.id = wa.workoutId
                    WHERE wa.userId = ?
                    """.trimIndent(),
                    arrayOf(player.id)
                )
                while (cursor.moveToNext()) {
                    uniqueDates.add(cursor.getString(cursor.getColumnIndexOrThrow("date")))
                }
                cursor.close()
            }
        } else {
            val cursor = db.rawQuery(
                """
                SELECT DISTINCT w.date FROM workouts w
                JOIN workout_assignments wa ON w.id = wa.workoutId
                WHERE wa.userId = ?
                """.trimIndent(),
                arrayOf(userId)
            )
            while (cursor.moveToNext()) {
                uniqueDates.add(cursor.getString(cursor.getColumnIndexOrThrow("date")))
            }
            cursor.close()
        }

        db.close()
        workoutDates.clear()
        workoutDates.addAll(uniqueDates.sortedDescending())

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, workoutDates)
        workoutHistoryListView.adapter = adapter
    }

    private fun filterWorkoutDatesThisWeek() {
        val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val thisWeekDates = mutableSetOf<String>()
        repeat(7) {
            thisWeekDates.add(sdf.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val filtered = workoutDates.filter { it in thisWeekDates }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filtered)
        workoutHistoryListView.adapter = adapter
    }

    private fun showDatePicker() {
        val today = Calendar.getInstance()
        DatePickerDialog(this,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
                val picked = sdf.format(cal.time)
                val filtered = workoutDates.filter { it == picked }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filtered)
                workoutHistoryListView.adapter = adapter
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showAssignPlayerDialog(coachId: String) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE userType = 'Player'", null)

        val players = mutableListOf<User>()
        while (cursor.moveToNext()) {
            players.add(
                User(
                    id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    lastname = cursor.getString(cursor.getColumnIndexOrThrow("lastname")),
                    email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    userType = cursor.getString(cursor.getColumnIndexOrThrow("userType"))
                )
            )
        }
        cursor.close()

        if (players.isEmpty()) {
            Toast.makeText(this, "No players available.", Toast.LENGTH_SHORT).show()
            return
        }

        val playerNames = players.map { "${it.name} ${it.lastname}" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Assign Player")
            .setItems(playerNames) { _, which ->
                val selectedPlayer = players[which]
                val coachPlayerDao = CoachPlayerDao(this)
                val success = coachPlayerDao.addPlayerToCoach(coachId, selectedPlayer.id)
                if (success) {
                    Toast.makeText(this, "Player assigned!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Player already assigned", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
