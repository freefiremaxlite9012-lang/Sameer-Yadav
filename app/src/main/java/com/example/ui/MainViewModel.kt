package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class GameScreen {
    object Home : GameScreen()
    object Ludo : GameScreen()
    object Chess : GameScreen()
    object Cricket : GameScreen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = GameDatabase.getDatabase(application)
    private val repository = GameRepository(database.gameStateDao)

    // Screen navigation state
    private val _currentScreen = MutableStateFlow<GameScreen>(GameScreen.Home)
    val currentScreen: StateFlow<GameScreen> = _currentScreen.asStateFlow()

    // Game state flows
    private val _ludoState = MutableStateFlow(LudoStateData())
    val ludoState: StateFlow<LudoStateData> = _ludoState.asStateFlow()

    private val _chessState = MutableStateFlow(ChessStateData())
    val chessState: StateFlow<ChessStateData> = _chessState.asStateFlow()

    private val _cricketState = MutableStateFlow(CricketStateData())
    val cricketState: StateFlow<CricketStateData> = _cricketState.asStateFlow()

    init {
        // Collect saved games from Room Database
        viewModelScope.launch {
            repository.getGameStateFlow("ludo").collectLatest { saved ->
                saved?.let {
                    _ludoState.value = LudoStateData.deserialize(it.dataString)
                }
            }
        }
        viewModelScope.launch {
            repository.getGameStateFlow("chess").collectLatest { saved ->
                saved?.let {
                    _chessState.value = ChessStateData.deserialize(it.dataString)
                }
            }
        }
        viewModelScope.launch {
            repository.getGameStateFlow("cricket").collectLatest { saved ->
                saved?.let {
                    _cricketState.value = CricketStateData.deserialize(it.dataString)
                }
            }
        }
    }

    fun navigateTo(screen: GameScreen) {
        _currentScreen.value = screen
    }

    // --- Ludo Actions ---
    fun updateLudoState(newState: LudoStateData) {
        _ludoState.value = newState
        viewModelScope.launch {
            repository.saveGameState("ludo", newState.serialize())
        }
    }

    fun resetLudoGame() {
        val resetState = LudoStateData()
        updateLudoState(resetState)
    }

    // --- Chess Actions ---
    fun updateChessState(newState: ChessStateData) {
        _chessState.value = newState
        viewModelScope.launch {
            repository.saveGameState("chess", newState.serialize())
        }
    }

    fun resetChessGame() {
        val resetState = ChessStateData()
        updateChessState(resetState)
    }

    // --- Cricket Actions ---
    fun updateCricketState(newState: CricketStateData) {
        _cricketState.value = newState
        viewModelScope.launch {
            repository.saveGameState("cricket", newState.serialize())
        }
    }

    fun resetCricketGame(mode: String = "QUICK") {
        val targetScore = if (mode == "QUICK") (12..24).random() else 0
        val resetState = CricketStateData(
            runs = 0,
            wickets = 0,
            balls = 0,
            target = targetScore,
            highStreak = _cricketState.value.highStreak,
            gameMode = mode,
            isGameOver = false,
            didWin = false
        )
        updateCricketState(resetState)
    }
}
