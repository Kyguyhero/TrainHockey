package com.example.trainhockey.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

class WorkoutDao(context: Context) {
    private val dbHelper = AppDatabaseHelper(context)

    // Save a workout and its exercises
    fun saveWorkout(
        date: String,
        goal: String,
        userId: String,
        onIceExercises: List<Exercise>,
        offIceExercises: List<Exercise>
    ): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()

        try {
            // Insert workout row
            val workoutValues = ContentValues().apply {
                put("date", date)
                put("goal", goal)
                put("userId", userId)
            }

            val workoutId = db.insert("workouts", null, workoutValues)
            if (workoutId == -1L) return false

            // Link exercises (On-Ice + Off-Ice)
            (onIceExercises + offIceExercises).forEach { exercise ->
                val linkValues = ContentValues().apply {
                    put("workoutId", workoutId)

                }
                db.insert("workout_exercises", null, linkValues)
            }

            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    // Load a workout's goal and exercises by date + user
    fun getWorkoutForDate(date: String, userId: String): Pair<String, List<Exercise>>? {
        val db = dbHelper.readableDatabase

        // Get the workout
        val workoutCursor = db.rawQuery(
            "SELECT * FROM workouts WHERE date = ? AND userId = ?",
            arrayOf(date, userId)
        )

        if (!workoutCursor.moveToFirst()) {
            workoutCursor.close()
            db.close()
            return null
        }

        val goal = workoutCursor.getString(workoutCursor.getColumnIndexOrThrow("goal"))
        val workoutId = workoutCursor.getInt(workoutCursor.getColumnIndexOrThrow("id"))
        workoutCursor.close()

        // Get associated exercises
        val exerciseList = mutableListOf<Exercise>()
        val exerciseCursor = db.rawQuery(
            """
            SELECT e.* FROM exercises e
            JOIN workout_exercises we ON e.id = we.exerciseId
            WHERE we.workoutId = ?
            """.trimIndent(),
            arrayOf(workoutId.toString())
        )

        while (exerciseCursor.moveToNext()) {
            val exercise = Exercise(
                id = exerciseCursor.getInt(exerciseCursor.getColumnIndexOrThrow("id")),
                name = exerciseCursor.getString(exerciseCursor.getColumnIndexOrThrow("name")),
                description = exerciseCursor.getString(exerciseCursor.getColumnIndexOrThrow("description")),
                videoUrl = exerciseCursor.getString(exerciseCursor.getColumnIndexOrThrow("videoUrl")),
                reps = 0,  // Set default or extend your schema if needed
                sets = 0,
                isOnIce = exerciseCursor.getInt(exerciseCursor.getColumnIndexOrThrow("isOnIce")) == 1
            )
            exerciseList.add(exercise)
        }

        exerciseCursor.close()
        db.close()

        return Pair(goal, exerciseList)
    }
}


