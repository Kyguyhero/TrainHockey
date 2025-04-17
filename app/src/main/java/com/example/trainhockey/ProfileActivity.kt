package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.trainhockey.data.AppDatabaseHelper
import com.example.trainhockey.data.CoachPlayerDao
import com.example.trainhockey.data.LocalUserDao
import com.example.trainhockey.data.User

class ProfileActivity : AppCompatActivity() {

    private lateinit var personalInfoTextView: TextView
    private lateinit var workoutHistoryListView: ListView
    private lateinit var logoutButton: Button
    private lateinit var assignPlayerButton: Button
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
        assignPlayerButton = findViewById(R.id.assignPlayerButton)
        teamInfoTextView = findViewById(R.id.teamInfo)

        assignPlayerButton.visibility = View.GONE

        userDao = LocalUserDao(this)
        dbHelper = AppDatabaseHelper(this)

        val userId = intent.getStringExtra("userUID")

        Log.d("ProfileActivity", "Intent userUID = $userId")

        if (userId != null) {
            currentUser = userDao.getUserById(userId)
        }

        if (currentUser != null) {
            personalInfoTextView.text = "Name: ${currentUser!!.name} ${currentUser!!.lastname}\nEmail: ${currentUser!!.email}"

            val coachPlayerDao = CoachPlayerDao(this)

            if (currentUser!!.userType == "Coach") {
                assignPlayerButton.visibility = View.VISIBLE
                assignPlayerButton.setOnClickListener {
                    showAssignPlayerDialog(currentUser!!.id)
                }

                val players = coachPlayerDao.getPlayersForCoach(currentUser!!.id)
                if (players.isNotEmpty()) {
                    val playerNames = players.joinToString("\n- ") { "${it.name} ${it.lastname}" }
                    teamInfoTextView.text = "Assigned Players:\n- $playerNames"
                } else {
                    teamInfoTextView.text = "No players assigned yet."
                }

            } else if (currentUser!!.userType == "Player") {
                val coach = coachPlayerDao.getCoachForPlayer(currentUser!!.id)
                teamInfoTextView.text =
                    coach?.let { "Coach: ${it.name} ${it.lastname}" } ?: "No coach assigned yet."
            }

        } else {
            personalInfoTextView.text = "User not found."
        }

        loadWorkoutDates(userId)

        logoutButton.setOnClickListener {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        workoutHistoryListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDate = workoutDates[position]
            val intent = Intent(this, WorkoutActivity::class.java)
            intent.putExtra("selectedDate", selectedDate)
            intent.putExtra("userUID", currentUser?.id)
            intent.putExtra("userType", currentUser?.userType)
            intent.putExtra("mode", "view")
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
        val cursor = db.rawQuery(
            """
    SELECT date FROM workout_completions
    WHERE userId = ?
    ORDER BY date DESC
    """.trimIndent(),
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
