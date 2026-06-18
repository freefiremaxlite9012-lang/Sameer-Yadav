package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameStateDao: GameStateDao) {
    fun getGameStateFlow(gameId: String): Flow<GameState?> =
        gameStateDao.getGameStateFlow(gameId)

    suspend fun getGameState(gameId: String): GameState? =
        gameStateDao.getGameState(gameId)

    suspend fun saveGameState(gameId: String, dataString: String) {
        gameStateDao.saveGameState(GameState(gameId, dataString))
    }

    suspend fun clearGameState(gameId: String) {
        gameStateDao.clearGameState(gameId)
    }
}
