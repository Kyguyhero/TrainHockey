package com.example.trainhockey.data

import android.content.ContentValues
import android.content.Context

class CoachPlayerDao(context: Context) {
    private val dbHelper = AppDatabaseHelper(context)

    fun addPlayerToCoach(coachId: String, playerId: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("coachId", coachId)
            put("playerId", playerId)
        }
        val result = db.insert("coach_players", null, values)
        db.close()
        return result != -1L
    }

    fun getPlayersForCoach(coachId: String): List<User> {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT u.* FROM users u
            JOIN coach_players cp ON u.id = cp.playerId
            WHERE cp.coachId = ?
        """
        val cursor = db.rawQuery(query, arrayOf(coachId))
        val players = mutableListOf<User>()

        while (cursor.moveToNext()) {
            val player = User(
                id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                lastname = cursor.getString(cursor.getColumnIndexOrThrow("lastname")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                userType = cursor.getString(cursor.getColumnIndexOrThrow("userType"))
            )
            players.add(player)
        }

        cursor.close()
        db.close()
        return players
    }

    fun getCoachForPlayer(playerId: String): User? {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT u.* FROM users u
            JOIN coach_players cp ON u.id = cp.coachId
            WHERE cp.playerId = ?
        """
        val cursor = db.rawQuery(query, arrayOf(playerId))

        var coach: User? = null
        if (cursor.moveToFirst()) {
            coach = User(
                id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                lastname = cursor.getString(cursor.getColumnIndexOrThrow("lastname")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                userType = cursor.getString(cursor.getColumnIndexOrThrow("userType"))
            )
        }

        cursor.close()
        db.close()
        return coach
    }
}
