package com.example.trainhockey.data


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        // Users table
        db.execSQL(
            """
            CREATE TABLE users (
                id TEXT PRIMARY KEY,
                name TEXT,
                lastname TEXT,
                email TEXT UNIQUE,
                password TEXT,
                userType TEXT
            )
            """.trimIndent()
        )

        // Exercises table
        db.execSQL(
            """
            CREATE TABLE exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                description TEXT,
                videoUrl TEXT,
                isOnIce INTEGER
            )
            """.trimIndent()
        )

        // Workouts table
        db.execSQL(
            """
            CREATE TABLE workouts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT,
                goal TEXT,
                userId TEXT,
                FOREIGN KEY(userId) REFERENCES users(id)
            )
            """.trimIndent()
        )

        // Workout-Exercise join table
        db.execSQL(
            """
            CREATE TABLE workout_exercises (
                workoutId INTEGER,
                exerciseId INTEGER,
                reps INTEGER,
                sets INTEGER,
                FOREIGN KEY(workoutId) REFERENCES workouts(id),
                FOREIGN KEY(exerciseId) REFERENCES exercises(id)
            )
            """.trimIndent()
        )

        // Workout Assignments table (many-to-many: workouts <-> users)
        db.execSQL(
            """
            CREATE TABLE workout_assignments (
                workoutId INTEGER,
                userId TEXT,
                FOREIGN KEY(workoutId) REFERENCES workouts(id),
                FOREIGN KEY(userId) REFERENCES users(id)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE workout_completions (
            date TEXT,
            userId TEXT,
            PRIMARY KEY(date, userId)
    )
    """.trimIndent()
        )
        db.execSQL(
            """
    CREATE TABLE coach_players (
        coachId TEXT,
        playerId TEXT,
        PRIMARY KEY(coachId, playerId),
        FOREIGN KEY(coachId) REFERENCES users(id),
        FOREIGN KEY(playerId) REFERENCES users(id)
    )
    """.trimIndent()
        )

        // Messages table
        db.execSQL(
            """
                CREATE TABLE messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT,
                    sender TEXT,
                    content TEXT
                )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS workout_assignments")
        db.execSQL("DROP TABLE IF EXISTS workout_exercises")
        db.execSQL("DROP TABLE IF EXISTS workouts")
        db.execSQL("DROP TABLE IF EXISTS exercises")
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS workout_completions")
        db.execSQL("DROP TABLE IF EXISTS coach_players")
        db.execSQL("DROP TABLE IF EXISTS messages")

        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "train_hockey.db"
        private const val DATABASE_VERSION = 7
    }
}
