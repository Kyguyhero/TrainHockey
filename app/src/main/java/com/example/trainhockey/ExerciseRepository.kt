package com.example.trainhockey

import android.util.Log
import com.example.trainhockey.data.Exercise
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class ExerciseRepository {
    private val db = Firebase.firestore

    suspend fun getExercises(): List<Exercise> {
        return try {
            val snapshot = db.collection("exercises").get().await()
            snapshot.documents.map { it.toObject(Exercise::class.java)!! }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

