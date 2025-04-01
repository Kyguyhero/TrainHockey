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

    fun registerUser(email: String, password: String, name: String, lastname: String, userType: String, onSuccess: (uid: String) -> Unit, onFailure: (exception: Exception) -> Unit){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let { firebaseUser ->
                        val uid = firebaseUser.uid
                        val newUser = User(uid, name, lastname, email, userType)

                        db.collection("users").document(uid)
                            .set(newUser)
                            .addOnSuccessListener {
                                onSuccess(uid)
                            }
                            .addOnFailureListener { e ->
                                firebaseUser.delete().addOnCompleteListener {
                                    onFailure(e)  // Now passing Exception
                                }
                            }
                    } ?: onFailure(Exception("User creation failed - null user"))
                } else {
                    onFailure(task.exception ?: Exception("Unknown registration error"))
                }
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: (uid: String) -> Unit,
        onFailure: (exception: Exception) -> Unit  // Now properly accepts Exception
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        onSuccess(it.uid)  // Pass the UID on success
                    } ?: onFailure(Exception("Login successful but user is null"))
                } else {
                    // Pass the actual exception (or create one if null)
                    onFailure(task.exception ?: Exception("Unknown login error"))
                }
            }
    }



}


