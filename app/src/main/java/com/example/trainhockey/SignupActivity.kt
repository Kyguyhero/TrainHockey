package com.example.trainhockey

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
    private lateinit var signUpButton: Button

    private val userRepository = UserRepository() // Instantiate UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Set up sign-up button click listener
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        nameEditText = findViewById(R.id.nameEditText)
        lastnameEditText = findViewById(R.id.lastnameEditText)
        signUpButton = findViewById(R.id.signUpButton)

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val name = nameEditText.text.toString()
            val lastname = lastnameEditText.text.toString()
            val userType = "Regular" // Set the user type (you can modify this as per your requirements)

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && lastname.isNotEmpty()) {
                // Call registerUser function from UserRepository
                userRepository.registerUser(email, password, name, lastname, userType,
                    onSuccess = {
                        // Show success message
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                        finish()  // Close the SignUpActivity
                    },
                    onFailure = { errorMessage ->
                        // Show error message
                        Toast.makeText(this, "Registration failed: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
