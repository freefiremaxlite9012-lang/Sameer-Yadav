package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStateDao {
    @Query("SELECT * FROM game_states WHERE gameId = :gameId")
    fun getGameStateFlow(gameId: String): Flow<GameState?>

    @Query("SELECT * FROM game_states WHERE gameId = :gameId")
    suspend fun getGameState(gameId: String): GameState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameState(gameState: GameState)

    @Query("DELETE FROM game_states WHERE gameId = :gameId")
    suspend fun clearGameState(gameId: String)
}
