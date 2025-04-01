package com.example.trainhockey.data

data class Exercise(
    var exerciseId: String = "",
    val name: String = "",
    val description: String = "",
    val videoUrl: String? = null,
    val repsPerSet: Int = 0,
    val sets: Int = 0,
    val isOnIce: Boolean = false // Determines if the exercise is On-Ice or Off-Ice
)
