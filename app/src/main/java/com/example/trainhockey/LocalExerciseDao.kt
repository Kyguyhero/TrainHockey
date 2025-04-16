package com.example.trainhockey.data

import android.content.ContentValues
import android.content.Context

class LocalExerciseDao(private val context: Context) {

    private val dbHelper = AppDatabaseHelper(context)

    fun getExercises(isOnIce: Boolean): List<Exercise> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Exercise>()

        val cursor = db.rawQuery(
            "SELECT * FROM exercises WHERE isOnIce = ?",
            arrayOf(if (isOnIce) "1" else "0")
        )

        while (cursor.moveToNext()) {
            val exercise = Exercise(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                videoUrl = cursor.getString(cursor.getColumnIndexOrThrow("videoUrl")),
                reps = 0,
                sets = 0,
                isOnIce = cursor.getInt(cursor.getColumnIndexOrThrow("isOnIce")) == 1
            )
            list.add(exercise)
        }

        cursor.close()
        db.close()

        return list
    }
    fun insertExercise(exercise: Exercise): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", exercise.name)
            put("description", exercise.description)
            put("videoUrl", exercise.videoUrl)
            put("isOnIce", if (exercise.isOnIce) 1 else 0)
        }
        val result = db.insert("exercises", null, values)
        db.close()
        return result != -1L
    }

}
