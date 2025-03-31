package com.example.trainhockey.data

data class Exercise(
    val exerciseId: String = "", // Could be auto-generated
    val name: String = "",
    val description: String = "",
    val videoUrl: String? = null
)
