package com.example.trainhockey.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

class WorkoutDao(val context: Context) {
    val dbHelper = AppDatabaseHelper(context)

    fun saveWorkout(
        date: String,
        goal: String,
        userId: String,
        onIceExercises: List<Exercise>,
        offIceExercises: List<Exercise>
    ): Boolean {
        val userDao = LocalUserDao(context)
        val currentUser = userDao.getUserById(userId)

        var assignedPlayers = emptyList<User>()
        if (currentUser?.userType == "Coach") {
            val coachPlayerDao = CoachPlayerDao(context)
            assignedPlayers = coachPlayerDao.getPlayersForCoach(userId)
        }

        val db = dbHelper.writableDatabase
        db.beginTransaction()

        try {
            val workoutValues = ContentValues().apply {
                put("date", date)
                put("goal", goal)
                put("userId", userId)
            }

            val workoutId = db.insert("workouts", null, workoutValues)
            if (workoutId == -1L) return false

            (onIceExercises + offIceExercises).forEach { exercise ->
                val linkValues = ContentValues().apply {
                    put("workoutId", workoutId)
                    put("exerciseId", exercise.id)
                    put("reps", exercise.reps)
                    put("sets", exercise.sets)
                }
                db.insert("workout_exercises", null, linkValues)
            }

            assignedPlayers.forEach { player ->
                val assignment = ContentValues().apply {
                    put("workoutId", workoutId)
                    put("userId", player.id)
                }
                db.insert("workout_assignments", null, assignment)
            }

            db.setTransactionSuccessful()
            return true
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
            val workoutCursor = db.rawQuery(
                "SELECT id FROM workouts WHERE date = ? AND userId = ?",
                arrayOf(date, userId)
            )

            if (!workoutCursor.moveToFirst()) {
                workoutCursor.close()
                return false
            }

            val workoutId = workoutCursor.getInt(workoutCursor.getColumnIndexOrThrow("id"))
            workoutCursor.close()

            val values = ContentValues().apply {
                put("goal", goal)
            }
            db.update("workouts", values, "id = ?", arrayOf(workoutId.toString()))

            db.delete("workout_exercises", "workoutId = ?", arrayOf(workoutId.toString()))

            (onIceExercises + offIceExercises).forEach { exercise ->
                val linkValues = ContentValues().apply {
                    put("workoutId", workoutId)
                    put("exerciseId", exercise.id)
                    put("reps", exercise.reps)
                    put("sets", exercise.sets)
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
        val result = db.insertWithOnConflict(
            "workout_completions",
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )
        db.close()
        return result != -1L
    }
    fun getAllWorkoutDates(userId: String): List<String> {
        val db = dbHelper.readableDatabase
        val dates = mutableListOf<String>()

        val cursor = db.rawQuery(
            """
            SELECT date FROM workouts 
            WHERE userId = ?
            UNION
            SELECT w.date FROM workouts w
            JOIN workout_assignments wa ON w.id = wa.workoutId
            WHERE wa.userId = ?
            """.trimIndent(), arrayOf(userId, userId)
        )

        while (cursor.moveToNext()) {
            dates.add(cursor.getString(cursor.getColumnIndexOrThrow("date")))
        }

        cursor.close()
        db.close()
        return dates
    }


    fun getWorkoutForDate(date: String, userId: String): Pair<String, List<Exercise>>? {
        val db = dbHelper.readableDatabase

        // Try personal workout first
        var cursor = db.rawQuery(
            "SELECT * FROM workouts WHERE date = ? AND userId = ?",
            arrayOf(date, userId)
        )

        var goal: String? = null
        var workoutId: Int? = null

        if (cursor.moveToFirst()) {
            goal = cursor.getString(cursor.getColumnIndexOrThrow("goal"))
            workoutId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        } else {
            cursor.close()
            // Try from assignments
            cursor = db.rawQuery(
                """
            SELECT w.* FROM workouts w
            JOIN workout_assignments wa ON w.id = wa.workoutId
            WHERE wa.userId = ? AND w.date = ?
            """.trimIndent(), arrayOf(userId, date)
            )
            if (cursor.moveToFirst()) {
                goal = cursor.getString(cursor.getColumnIndexOrThrow("goal"))
                workoutId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            } else {
                cursor.close()
                db.close()
                return null
            }
        }
        cursor.close()

        val exerciseList = mutableListOf<Exercise>()
        val exerciseCursor = db.rawQuery(
            """
        SELECT e.*, we.reps, we.sets FROM exercises e
        JOIN workout_exercises we ON e.id = we.exerciseId
        WHERE we.workoutId = ?
        """.trimIndent(), arrayOf(workoutId.toString())
        )

        while (exerciseCursor.moveToNext()) {
            val ex = Exercise(
                id = exerciseCursor.getInt(exerciseCursor.getColumnIndexOrThrow("id")),
                name = exerciseCursor.getString(exerciseCursor.getColumnIndexOrThrow("name")),
                description = exerciseCursor.getString(exerciseCursor.getColumnIndexOrThrow("description")),
                videoUrl = exerciseCursor.getString(exerciseCursor.getColumnIndexOrThrow("videoUrl")),
                isOnIce = exerciseCursor.getInt(exerciseCursor.getColumnIndexOrThrow("isOnIce")) == 1,
                reps = exerciseCursor.getInt(exerciseCursor.getColumnIndexOrThrow("reps")),
                sets = exerciseCursor.getInt(exerciseCursor.getColumnIndexOrThrow("sets"))
            )
            exerciseList.add(ex)
        }

        exerciseCursor.close()
        db.close()

        return Pair(goal ?: "", exerciseList)
    }


    fun getWorkoutsAssignedToPlayer(playerId: String): List<Pair<String, List<Exercise>>> {
        val db = dbHelper.readableDatabase
        val workouts = mutableListOf<Pair<String, List<Exercise>>>()

        val workoutCursor = db.rawQuery(
            """
                SELECT w.id, w.date, w.goal FROM workouts w
                JOIN workout_assignments wa ON w.id = wa.workoutId
                WHERE wa.userId = ?
            """.trimIndent(),
            arrayOf(playerId)
        )

        while (workoutCursor.moveToNext()) {
            val workoutId = workoutCursor.getInt(0)
            val goal = workoutCursor.getString(2)
            val exercises = mutableListOf<Exercise>()

            val exerciseCursor = db.rawQuery(
                """
                    SELECT e.*, we.reps, we.sets FROM exercises e
                    JOIN workout_exercises we ON e.id = we.exerciseId
                    WHERE we.workoutId = ?
                """.trimIndent(),
                arrayOf(workoutId.toString())
            )

            while (exerciseCursor.moveToNext()) {
                val ex = Exercise(
                    id = exerciseCursor.getInt(exerciseCursor.getColumnIndexOrThrow("id")),
                    name = exerciseCursor.getString(exerciseCursor.getColumnIndexOrThrow("name")),
                    description = exerciseCursor.getString(exerciseCursor.getColumnIndexOrThrow("description")),
                    videoUrl = exerciseCursor.getString(exerciseCursor.getColumnIndexOrThrow("videoUrl")),
                    isOnIce = exerciseCursor.getInt(exerciseCursor.getColumnIndexOrThrow("isOnIce")) == 1,
                    reps = exerciseCursor.getInt(exerciseCursor.getColumnIndexOrThrow("reps")),
                    sets = exerciseCursor.getInt(exerciseCursor.getColumnIndexOrThrow("sets"))
                )
                exercises.add(ex)
            }

            exerciseCursor.close()
            workouts.add(Pair(goal, exercises))
        }

        workoutCursor.close()
        db.close()

        return workouts
    }

    fun getCompletionStatusForCoach(coachId: String, date: String): List<Pair<User, Boolean>> {
        val db = dbHelper.readableDatabase
        val coachPlayerDao = CoachPlayerDao(context)
        val players = coachPlayerDao.getPlayersForCoach(coachId)

        val results = mutableListOf<Pair<User, Boolean>>()

        for (player in players) {
            val cursor = db.rawQuery(
                "SELECT * FROM workout_completions WHERE date = ? AND userId = ?",
                arrayOf(date, player.id)
            )
            val completed = cursor.moveToFirst()
            results.add(Pair(player, completed))
            cursor.close()
        }

        db.close()
        return results
    }

}
