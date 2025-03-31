package com.example.trainhockey
import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.initialize
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck


class MyApplication : Application() {

    lateinit var db: com.google.firebase.firestore.FirebaseFirestore

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )
        FirebaseApp.initializeApp(this)
        val db = Firebase.firestore
    }
}