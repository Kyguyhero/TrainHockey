package com.example.trainhockey

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.trainhockey.data.Exercise
import com.google.firebase.firestore.FirebaseFirestore

class AddExerciseActivity : AppCompatActivity() {

    private lateinit var edtExerciseName: EditText
    private lateinit var edtExerciseDescription: EditText
    private lateinit var edtVideoUrl: EditText
    private lateinit var btnPickVideo: Button
    private lateinit var btnSaveExercise: Button
    private var selectedVideoUri: Uri? = null
    private var category: String? = null
    private val db = FirebaseFirestore.getInstance()

    private val PICK_VIDEO_REQUEST = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_exercise)

        edtExerciseName = findViewById(R.id.edtExerciseName)
        edtExerciseDescription = findViewById(R.id.edtExerciseDescription)
        edtVideoUrl = findViewById(R.id.edtVideoUrl)
        btnPickVideo = findViewById(R.id.btnPickVideo)
        btnSaveExercise = findViewById(R.id.btnSaveExercise)

        category = intent.getStringExtra("category")

        // Pick video from the gallery
        btnPickVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            intent.type = "video/*"
            startActivityForResult(intent, PICK_VIDEO_REQUEST)
        }

        // Save the exercise to Firestore
        btnSaveExercise.setOnClickListener {
            val name = edtExerciseName.text.toString().trim()
            val description = edtExerciseDescription.text.toString().trim()
            val videoUrl = edtVideoUrl.text.toString().trim()

            if (name.isNotEmpty() && description.isNotEmpty()) {
                val videoUri = if (selectedVideoUri != null) {
                    selectedVideoUri.toString()
                } else {
                    videoUrl
                }

                val isOnIce = category == "onIce"
                val newExercise = Exercise("", name, description, videoUri, 0, 0, isOnIce)

                db.collection("exercises")
                    .add(newExercise)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Exercise added!", Toast.LENGTH_SHORT).show()
                        finish() // Go back to WorkoutEditorActivity
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error adding exercise", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle the result from picking the video
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == PICK_VIDEO_REQUEST) {
            selectedVideoUri = data?.data
            // Optionally, you can display a preview of the selected video URI
            Toast.makeText(this, "Video selected: $selectedVideoUri", Toast.LENGTH_SHORT).show()
        }
    }
}
