package com.example.trainhockey

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.trainhockey.data.Exercise
import com.example.trainhockey.data.LocalExerciseDao

class AddExerciseActivity : AppCompatActivity() {

    private lateinit var edtExerciseName: EditText
    private lateinit var edtExerciseDescription: EditText
    private lateinit var edtVideoUrl: EditText
    private lateinit var btnPickVideo: Button
    private lateinit var btnSaveExercise: Button

    private var selectedVideoUri: Uri? = null
    private var category: String? = null
    private lateinit var exerciseDao: LocalExerciseDao

    private val PICK_VIDEO_REQUEST = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_exercise)

        edtExerciseName = findViewById(R.id.edtExerciseName)
        edtExerciseDescription = findViewById(R.id.edtExerciseDescription)
        edtVideoUrl = findViewById(R.id.edtVideoUrl)
        btnPickVideo = findViewById(R.id.btnPickVideo)
        btnSaveExercise = findViewById(R.id.btnSaveExercise)

        exerciseDao = LocalExerciseDao(this)
        category = intent.getStringExtra("category") // "onIce" or "offIce"

        btnPickVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            intent.type = "video/*"
            startActivityForResult(intent, PICK_VIDEO_REQUEST)
        }

        btnSaveExercise.setOnClickListener {
            saveExercise()
        }
    }

    private fun saveExercise() {
        val name = edtExerciseName.text.toString().trim()
        val description = edtExerciseDescription.text.toString().trim()
        val videoUrlInput = edtVideoUrl.text.toString().trim()

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val videoUrl = selectedVideoUri?.toString() ?: videoUrlInput
        val isOnIce = category == "onIce"

        val exercise = Exercise(
            name = name,
            description = description,
            videoUrl = videoUrl,
            reps = 0,
            sets = 0,
            isOnIce = isOnIce
        )

        val success = exerciseDao.insertExercise(exercise)

        if (success) {
            Toast.makeText(this, "Exercise added!", Toast.LENGTH_SHORT).show()
            finish() // Go back
        } else {
            Toast.makeText(this, "Error adding exercise", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_VIDEO_REQUEST) {
            selectedVideoUri = data?.data
            Toast.makeText(this, "Video selected: $selectedVideoUri", Toast.LENGTH_SHORT).show()
        }
    }
}
