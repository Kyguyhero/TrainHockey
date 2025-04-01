package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth


    private val userRepository = UserRepository()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val createAccountText = findViewById<TextView>(R.id.createAccountButton)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                userRepository.loginUser(email, password,
                    onSuccess = { uid ->
                        // Login successful, now you have the UID
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                        // Pass the UID to MainActivity
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("userUID", uid) // Pass the UID to MainActivity
                        startActivity(intent)
                        finish()
                    },
                    onFailure = { errorMessage ->
                        Toast.makeText(this, "Login failed: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        createAccountText.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}

