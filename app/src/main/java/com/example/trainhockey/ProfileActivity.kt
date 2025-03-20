package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Closes this activity and returns to the previous one
        }

        val logoutButton = findViewById<Button>(R.id.logoutButton)

        logoutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Clears current session
        }
    }
}
