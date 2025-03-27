package com.example.trainhockey.data

data class User( // This is the CLASS
    val uid: String = "",
    val name: String = "",
    val lastname: String = "",
    val email: String = "",
    val userType: String = "",
    val profileImageUrl: String? = null
)
