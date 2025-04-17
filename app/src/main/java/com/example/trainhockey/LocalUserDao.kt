

package com.example.trainhockey.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.trainhockey.data.AppDatabaseHelper
import com.example.trainhockey.data.User

class LocalUserDao(context: Context) {

    private val dbHelper = AppDatabaseHelper(context)

    // Register new user
    fun registerUser(user: User, password: String): Boolean {
        val db = dbHelper.writableDatabase

        // Check if email already exists
        val cursor = db.rawQuery("SELECT * FROM users WHERE email = ?", arrayOf(user.email))
        if (cursor.moveToFirst()) {
            cursor.close()
            db.close()
            return false // Email already exists
        }
        cursor.close()

        val values = ContentValues().apply {
            put("id", user.id)
            put("name", user.name)
            put("lastname", user.lastname)
            put("email", user.email)
            put("password", password)
            put("userType", user.userType)
        }

        val result = db.insert("users", null, values)
        db.close()
        return result != -1L
    }

    // Login user
    fun loginUser(email: String, password: String): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE email = ? AND password = ?",
            arrayOf(email, password)
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                lastname = cursor.getString(cursor.getColumnIndexOrThrow("lastname")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                userType = cursor.getString(cursor.getColumnIndexOrThrow("userType"))
            )
        }

        cursor.close()
        db.close()
        return user
    }

    // Get user by ID
    fun getUserById(userId: String): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE id = ?",
            arrayOf(userId)
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                lastname = cursor.getString(cursor.getColumnIndexOrThrow("lastname")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                userType = cursor.getString(cursor.getColumnIndexOrThrow("userType"))
            )
        }

        cursor.close()
        db.close()
        return user
    }
}
