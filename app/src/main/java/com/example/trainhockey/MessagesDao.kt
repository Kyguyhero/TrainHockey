package com.example.trainhockey

import android.content.ContentValues
import android.content.Context
import com.example.trainhockey.data.AppDatabaseHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessagesDao (val context: Context){
    private val dbHelper = AppDatabaseHelper(context)

    fun insertMessage(sender: String, content: String): Boolean {

        val dateSent = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val sql = "INSERT INTO messages (dateSent, sender, content) VALUES (?, ?, ?)"
        val db = dbHelper.writableDatabase
        db.beginTransaction()

        try {
            val messageValues = ContentValues().apply {
                put("date", dateSent)
                put("sender", sender)
                put("content", content)
            }

            val messageId = db.insert("messages", null, messageValues)
            if (messageId == -1L) return false

            db.setTransactionSuccessful()

            return true

        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun getAllMessages(): MutableList<String> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT date, sender, content FROM messages ORDER BY id DESC", null)
        val messages = mutableListOf<String>()
        if (cursor.moveToFirst()) {
            do {
                val date = cursor.getString(0)
                val sender = cursor.getString(1)
                val content = cursor.getString(2)
                //messages.add("$date - $sender: $content")
                messages.add(messageLayout(sender, content))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return messages
    }

    fun messageLayout(name: String, content: String): String {
        return "$name says: \n$content"
    }
}