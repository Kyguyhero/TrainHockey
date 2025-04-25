package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.trainhockey.data.LocalUserDao

class MessagesActivity : AppCompatActivity() {

    private lateinit var messageListView: ListView
    private lateinit var noMessagesText: TextView
    private var isCoach: Boolean = false

    // Sample placeholder data — you can replace with real data later
    private val messages = listOf<String>() // or listOf("Welcome to TrainHockey!", "Don't forget your practice today!")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.messages)

        // Check for coach user type
        val userType = intent.getStringExtra("userType") ?: "Player"
        isCoach = userType == "Coach"

        // Get variables for send message items
        val message:EditText = findViewById(R.id.createMessageText)
        val sendMessageButton: Button = findViewById(R.id.sendMessageButton)

        if (isCoach) {
            message.visibility = View.VISIBLE
            sendMessageButton.visibility = View.VISIBLE
        }

        /*
        findViewById<ImageButton>(R.id.homeButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<ImageButton>(R.id.workoutsButton).setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
        }

        findViewById<ImageButton>(R.id.profileButton).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        */

        messageListView = findViewById(R.id.messageListView)
        noMessagesText = findViewById(R.id.noMessagesText)

        if (messages.isEmpty()) {
            noMessagesText.visibility = TextView.VISIBLE
            messageListView.visibility = ListView.GONE
        } else {
            noMessagesText.visibility = TextView.GONE
            messageListView.visibility = ListView.VISIBLE

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, messages)
            messageListView.adapter = adapter
        }

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
