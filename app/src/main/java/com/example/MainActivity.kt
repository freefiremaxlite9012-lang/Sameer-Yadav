package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GameScreen
import com.example.ui.MainViewModel
import com.example.ui.screens.ChessScreen
import com.example.ui.screens.CricketScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LudoScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        com.example.ui.SoundManager.startBackgroundMusic()
    }

    override fun onStop() {
        super.onStop()
        com.example.ui.SoundManager.stopBackgroundMusic()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports borderless full-bleed visual layouts
        enableEdgeToEdge()
        
        setContent {
            // Force dark cosmic styling for premium neon aesthetic in and across light/dark system settings
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val ludoState by viewModel.ludoState.collectAsStateWithLifecycle()
                val chessState by viewModel.chessState.collectAsStateWithLifecycle()
                val cricketState by viewModel.cricketState.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // Main layout screen navigation routing
                    val modifierWithPadding = Modifier.padding(innerPadding)
                    
                    when (currentScreen) {
                        is GameScreen.Home -> {
                            HomeScreen(
                                viewModel = viewModel,
                                ludoState = ludoState,
                                chessState = chessState,
                                cricketState = cricketState
                            )
                        }
                        is GameScreen.Ludo -> {
                            LudoScreen(
                                viewModel = viewModel,
                                state = ludoState,
                                onBack = { viewModel.navigateTo(GameScreen.Home) }
                            )
                        }
                        is GameScreen.Chess -> {
                            ChessScreen(
                                viewModel = viewModel,
                                state = chessState,
                                onBack = { viewModel.navigateTo(GameScreen.Home) }
                            )
                        }
                        is GameScreen.Cricket -> {
                            CricketScreen(
                                viewModel = viewModel,
                                state = cricketState,
                                onBack = { viewModel.navigateTo(GameScreen.Home) }
                            )
                        }
                    }
                }
            }
        }
    }
}
