package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.trainhockey.data.User
import com.google.firebase.auth.FirebaseAuth

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

    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

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

        // Set click listener for the loginText to navigate to LoginActivity
        loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        createButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim()
            val lastname = lastnameEditText.text.toString().trim()

            // Get selected user type
            val selectedUserType = when (userTypeRadioGroup.checkedRadioButtonId) {
                R.id.playerRadioButton -> "Player"
                R.id.coachRadioButton -> "Coach"
                else -> "Player" // default to Player
            }

            // Check if the fields are not empty
            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && lastname.isNotEmpty()) {
                // Call registerUser function from UserRepository
                userRepository.registerUser(
                    email,
                    password,
                    name,
                    lastname,
                    selectedUserType,
                    onSuccess = {
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()

                        // After successful registration, login the user and pass UID to MainActivity
                        userRepository.loginUser(email, password,
                            onSuccess = { uid ->
                                // Pass UID and userType to MainActivity
                                val intent = Intent(this, MainActivity::class.java).apply {
                                    putExtra("userUID", uid)
                                    putExtra("userType", selectedUserType)
                                }
                                startActivity(intent)
                                finish()
                            },
                            onFailure = { error ->
                                Toast.makeText(this, "Login failed: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onFailure = { error ->
                        Toast.makeText(this, "Registration failed: $error", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}