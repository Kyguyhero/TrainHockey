package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.trainhockey.data.User
import com.google.firebase.auth.FirebaseAuth

private val userRepository = UserRepository()

class SignUpActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var lastnameEditText: EditText
    private lateinit var createButton: Button
    private lateinit var loginText: TextView


    private val userRepository = UserRepository() // Instantiate UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Set up sign-up button click listener
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        nameEditText = findViewById(R.id.nameEditText)
        lastnameEditText = findViewById(R.id.lastnameEditText)
        createButton = findViewById(R.id.createButton)
        loginText = findViewById(R.id.loginText)

        // Set click listener for the loginText to navigate to MainActivity
        loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close the SignUpActivity
        }

        createButton.setOnClickListener {
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()
                val name = nameEditText.text.toString().trim()
                val lastname = lastnameEditText.text.toString().trim()
                val userType = "Regular" // You can adjust as needed

                // Check if the fields are not empty
                if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && lastname.isNotEmpty()) {
                    // Call registerUser function from UserRepository
                    userRepository.registerUser(email, password, name, lastname, userType, onSuccess = {
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()

                        // After successful registration, login the user and pass UID to MainActivity
                        userRepository.loginUser(email, password,
                            onSuccess = { uid ->
                                // Pass UID to MainActivity
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("userUID", uid)  // Pass the UID as extra
                                startActivity(intent)
                                finish()  // Close SignUpActivit  y
                            },
                            onFailure = { error ->
                                Toast.makeText(this, "Login failed: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }, onFailure = { error ->
                        Toast.makeText(this, "Registration failed: $error", Toast.LENGTH_SHORT).show()
                    })
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }


        }
}
