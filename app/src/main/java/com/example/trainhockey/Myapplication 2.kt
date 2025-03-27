package com.example.trainhockey
import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyApplication : Application() {

    lateinit var db: com.google.firebase.firestore.FirebaseFirestore

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val db = Firebase.firestore
    }


}