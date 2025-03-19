package com.example.trainhockey

import android.util.Log
import com.example.trainhockey.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserRepository {

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    fun registerUser(email: String, password: String, name: String, lastname: String,userType: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val uid = it.uid
                        val newUser = User(uid = uid, name = name, lastname = lastname, email = email, userType = userType)
                        db.collection("users").document(uid)
                            .set(newUser)
                            .addOnSuccessListener {
                                Log.d("Registration", "User data stored successfully")
                                onSuccess() // Callback for successful registration
                            }
                            .addOnFailureListener { e ->
                                Log.w("Registration", "Error storing user data", e)
                                onFailure(e.message ?: "Unknown error storing user data")
                            }
                    }
                } else {
                    Log.w("Registration", "createUserWithEmailAndPassword failed", task.exception)
                    onFailure(task.exception?.message ?: "Unknown registration error")
                }
            }
    }

    fun loginUser(email: String, password: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val auth: FirebaseAuth = Firebase.auth

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //Login Successful
                    val user = auth.currentUser
                    user?.let {
                        val uid = it.uid
                        onSuccess(uid) // Pass the UID to the calling activity
                    }
                } else {
                    //Login Failed
                    onFailure(task.exception?.message ?: "Unknown login error")
                }
            }
    }

}


