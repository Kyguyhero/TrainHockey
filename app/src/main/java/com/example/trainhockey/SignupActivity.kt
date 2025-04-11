package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.trainhockey.data.LocalUserDao
import com.example.trainhockey.data.User
import java.util.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var lastnameEditText: EditText
    private lateinit var createButton: Button
    private lateinit var loginText: TextView
    private lateinit var userTypeRadioGroup: RadioGroup
    private lateinit var playerRadioButton: RadioButton
    private lateinit var coachRadioButton: RadioButton

    private lateinit var userDao: LocalUserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize DAO
        userDao = LocalUserDao(this)

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        nameEditText = findViewById(R.id.nameEditText)
        lastnameEditText = findViewById(R.id.lastnameEditText)
        createButton = findViewById(R.id.createButton)
        loginText = findViewById(R.id.loginText)
        userTypeRadioGroup = findViewById(R.id.userTypeRadioGroup)
        playerRadioButton = findViewById(R.id.playerRadioButton)
        coachRadioButton = findViewById(R.id.coachRadioButton)

        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        createButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim()
            val lastname = lastnameEditText.text.toString().trim()

            val selectedUserType = when (userTypeRadioGroup.checkedRadioButtonId) {
                R.id.playerRadioButton -> "Player"
                R.id.coachRadioButton -> "Coach"
                else -> "Player" // default
            }

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && lastname.isNotEmpty()) {
                val userId = UUID.randomUUID().toString()
                val user = User(userId, name, lastname, email, selectedUserType)

                val registered = userDao.registerUser(user, password)

                if (registered) {
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()

                    // Optional: log them in immediately
                    val loggedInUser = userDao.loginUser(email, password)
                    if (loggedInUser != null) {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("userUID", loggedInUser.id)
                            putExtra("userType", loggedInUser.userType)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Login failed after signup", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
