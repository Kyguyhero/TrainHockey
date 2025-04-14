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
    fun updateWorkout(
        date: String,
        goal: String,
        userId: String,
        onIceExercises: List<Exercise>,
        offIceExercises: List<Exercise>
    ): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()

        try {
            // Get workout ID
            val workoutCursor = db.rawQuery(
                "SELECT id FROM workouts WHERE date = ? AND userId = ?",
                arrayOf(date, userId)
            )

            if (!workoutCursor.moveToFirst()) {
                workoutCursor.close()
                db.endTransaction()
                db.close()
                return false // workout doesn't exist
            }

            val workoutId = workoutCursor.getInt(workoutCursor.getColumnIndexOrThrow("id"))
            workoutCursor.close()

            // Update goal
            val values = ContentValues().apply {
                put("goal", goal)
            }
            db.update("workouts", values, "id = ?", arrayOf(workoutId.toString()))

            // Delete old exercise links
            db.delete("workout_exercises", "workoutId = ?", arrayOf(workoutId.toString()))

            // Reinsert exercises
            (onIceExercises + offIceExercises).forEach { exercise ->
                val exerciseLink = ContentValues().apply {
                    put("workoutId", workoutId)
                    put("exerciseId", exercise.id)
                }
                db.insert("workout_exercises", null, exerciseLink)
            }

            db.setTransactionSuccessful()
            return true

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
            db.close()
        }
    }
    fun workoutExists(date: String, userId: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM workouts WHERE date = ? AND userId = ?",
            arrayOf(date, userId)
        )

        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }
    fun isWorkoutCompleted(date: String, userId: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM workout_completions WHERE date = ? AND userId = ?",
            arrayOf(date, userId)
        )
        val completed = cursor.moveToFirst()
        cursor.close()
        db.close()
        return completed
    }

    fun markWorkoutCompleted(date: String, userId: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("date", date)
            put("userId", userId)
        }
        val result = db.insertWithOnConflict("workout_completions", null, values, SQLiteDatabase.CONFLICT_IGNORE)
        db.close()
        return result != -1L
    }



}


