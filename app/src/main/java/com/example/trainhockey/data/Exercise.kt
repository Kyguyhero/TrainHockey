package com.example.trainhockey.data

data class Exercise(
    val id: Int = 0,  // ← ADD THIS!
    val name: String,
    val description: String,
    val videoUrl: String,
    val reps: Int,
    val sets: Int,
    val isOnIce: Boolean
)
