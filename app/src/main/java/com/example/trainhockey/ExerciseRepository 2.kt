package com.example.trainhockey

import android.util.Log
import com.example.trainhockey.data.Exercise
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class ExerciseRepository {

    private val db = Firebase.firestore

    suspend fun addExercise(exercise: Exercise): Result<String> {
        return try {
            val docRef = db.collection("exercises").add(exercise).await()
            Result.success(docRef.id) // Return the newly generated document ID
        } catch (e: Exception) {
            Log.w("ExerciseRepository", "Error adding exercise", e)
            Result.failure(Exception(e.message ?: "Error adding exercise"))
        }
    }
}
