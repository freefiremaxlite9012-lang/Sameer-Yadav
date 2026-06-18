package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_states")
data class GameState(
    @PrimaryKey val gameId: String,
    val dataString: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
