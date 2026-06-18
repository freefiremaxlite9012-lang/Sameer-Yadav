package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CricketStateData
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CricketScreen(
    viewModel: MainViewModel,
    state: CricketStateData,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Animation & Gameplay local states
    var ballY by remember { mutableFloatStateOf(0f) } // 0f (top) to 1f (bottom)
    var ballX by remember { mutableFloatStateOf(0.5f) } // horizontal deviation for swing
    var isBallActive by remember { mutableStateOf(false) }
    var currentBowlerType by remember { mutableStateOf("FAST") } // "FAST", "SPIN", "SWING"
    var battingFeedback by remember { mutableStateOf("") } // "NEON SIXER!", "BOUNDARY!", etc.
    var feedbackColor by remember { mutableStateOf(Color.Green) }
    var feedbackVisible by remember { mutableStateOf(false) }
    var swingTriggered by remember { mutableStateOf(false) }
    var pitchHeightPx by remember { mutableFloatStateOf(0f) }

    // Constants for sweet-spot (0.8f to 0.9f)
    val sweetSpotMin = 0.82f
    val sweetSpotMax = 0.90f
    val sweetSpotCenter = 0.86f

    // Bowling logic
    fun bowlBall() {
        if (isBallActive || state.isGameOver) return
        
        // Pick bowler style
        val styles = listOf("FAST", "SPIN", "SWING")
        currentBowlerType = styles.random()
        swingTriggered = false
        ballX = 0.5f
        
        coroutineScope.launch {
            isBallActive = true
            com.example.ui.SoundManager.playCricketBowl()
            ballY = 0f
            
            val duration = when (currentBowlerType) {
                "FAST" -> 1600L
                "SPIN" -> 2400L
                else -> 2000L // SWING
            }
            
            val steps = 100
            val delayInterval = duration / steps
            
            for (i in 1..steps) {
                if (!isBallActive) break
                val progress = i.toFloat() / steps
                ballY = progress
                
                // Devise horizontal trajectory deviation for swing/spin
                if (currentBowlerType == "SWING") {
                    ballX = 0.5f + 0.12f * kotlin.math.sin(progress * Math.PI.toFloat())
                } else if (currentBowlerType == "SPIN") {
                    if (progress > 0.5f) {
                        ballX = 0.5f + 0.15f * (progress - 0.5f)
                    }
                }
                delay(delayInterval)
            }
            
            // If ball reaches bottom without swing -> Miss / Bowled!
            if (isBallActive) {
                isBallActive = false
                val newWickets = state.wickets + 1
                val newBalls = state.balls + 1
                val isMaxWkts = newWickets >= 3
                val isMaxBalls = state.gameMode == "QUICK" && newBalls >= 6
                
                val over = isMaxWkts || isMaxBalls
                val won = state.gameMode == "QUICK" && state.runs >= state.target
                
                battingFeedback = "CLEAN BOWLED! ❌"
                feedbackColor = Color.Red
                feedbackVisible = true
                com.example.ui.SoundManager.playCricketOut()
                
                viewModel.updateCricketState(
                    state.copy(
                        wickets = newWickets,
                        balls = newBalls,
                        isGameOver = over,
                        didWin = won && over
                    )
                )
            }
        }
    }

    fun swingBat() {
        if (!isBallActive || state.isGameOver) return
        
        isBallActive = false // Stop the ball
        swingTriggered = true
        
        val hitPosition = ballY
        val isSweet = hitPosition in sweetSpotMin..sweetSpotMax
        val difference = abs(hitPosition - sweetSpotCenter)
        
        var runsScored = 0
        var gotOut = false
        
        if (isSweet) {
            if (difference < 0.02f) {
                runsScored = 6
                battingFeedback = "🚀 NEON SIXER! (6)"
                feedbackColor = Color(0xFFFF007F) // Glowing Neon Magenta
                com.example.ui.SoundManager.playCricketSix()
            } else if (difference < 0.05f) {
                runsScored = 4
                battingFeedback = "🔥 BOUNDARY CRACK! (4)"
                feedbackColor = Color(0xFF9D4EDD) // Neon Purple Glow
                com.example.ui.SoundManager.playCricketBatClick()
            } else {
                runsScored = 2
                battingFeedback = "⚡ DOUBLE RUNS! (2)"
                feedbackColor = Color(0xFF00FFFF) // Neon Cyan
                com.example.ui.SoundManager.playCricketBatClick()
            }
        } else if (hitPosition < sweetSpotMin) {
            // Early swing
            if (hitPosition > 0.65f) {
                runsScored = 1
                battingFeedback = "🏏 Good Single! (1)"
                feedbackColor = Color.Yellow
                com.example.ui.SoundManager.playCricketBatClick()
            } else {
                gotOut = true
                battingFeedback = "☝️ CAUGHT OUT! Early Swing"
                feedbackColor = Color.Red
                com.example.ui.SoundManager.playCricketOut()
            }
        } else {
            // Late swing
            if (hitPosition < sweetSpotMax + 0.08f) {
                runsScored = 1
                battingFeedback = "⚾ Squeezed Single (1)"
                feedbackColor = Color.Yellow
                com.example.ui.SoundManager.playCricketBatClick()
            } else {
                gotOut = true
                battingFeedback = "❌ STUMPED! Late Swing"
                feedbackColor = Color.Red
                com.example.ui.SoundManager.playCricketOut()
            }
        }
        
        feedbackVisible = true
        
        val newRuns = state.runs + runsScored
        val newWickets = if (gotOut) state.wickets + 1 else state.wickets
        val newBalls = state.balls + 1
        
        val isMaxWkts = newWickets >= 3
        val isMaxBalls = state.gameMode == "QUICK" && newBalls >= 6
        
        var won = false
        var gameOver = isMaxWkts || isMaxBalls
        
        if (state.gameMode == "QUICK") {
            if (newRuns >= state.target) {
                won = true
                gameOver = true
            }
        }
        
        // Update high streak if endless
        val newHighScore = if (state.gameMode == "ENDLESS" && newRuns > state.highStreak) {
            newRuns
        } else {
            state.highStreak
        }
        
        viewModel.updateCricketState(
            state.copy(
                runs = newRuns,
                wickets = newWickets,
                balls = newBalls,
                highStreak = newHighScore,
                isGameOver = gameOver,
                didWin = won
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "NEON CRICKET",
                        color = Color(0xFFE0AAFF),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("cricket_back_btn")) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFF007F)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF090014),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF050010) // Pure ultra-deep cosmic black background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Dashboard scoreboard
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        border = BorderStroke(2.dp, Brush.linearGradient(listOf(Color(0xFF9D4EDD), Color(0xFFFF007F)))),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D001F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MODE: " + state.gameMode,
                                color = Color(0xFF00FFFF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${state.runs}/${state.wickets}",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "OVERS: ${state.balls / 6}.${state.balls % 6}",
                                color = Color(0xFFE0AAFF),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            if (state.gameMode == "QUICK") {
                                Text(
                                    text = "TARGET: ${state.target}",
                                    color = Color(0xFFFF007F),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "HIGHEST: ${state.highStreak}",
                                    color = Color(0xFFFFCC00),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    // Quick stats/chase message
                    if (!state.isGameOver) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (state.gameMode == "QUICK") {
                            val runsNeeded = state.target - state.runs
                            val ballsRemaining = 6 - state.balls
                            Text(
                                text = "Need $runsNeeded runs from $ballsRemaining balls to win!",
                                color = Color(0xFF39FF14),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = "Endless arcade bowling! Speed increments every ball.",
                                color = Color(0xFF39FF14),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Interactive Pitch Grid / Cricket Canvas View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp)
                    .background(Color(0xFF090014), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF4C1D95), RoundedCornerShape(16.dp))
                    .clickable {
                        // Tapping the pitch sweeps!
                        if (isBallActive) swingBat()
                    },
                contentAlignment = Alignment.Center
            ) {
                // Pitch Drawing
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height

                    // Draw dynamic neon rails (pitch borders)
                    drawLine(
                        color = Color(0xFF7B2CBF),
                        start = Offset(width * 0.25f, 0f),
                        end = Offset(width * 0.15f, height),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = Color(0xFF7B2CBF),
                        start = Offset(width * 0.75f, 0f),
                        end = Offset(width * 0.85f, height),
                        strokeWidth = 3f
                    )

                    // Draw wickets at top (bowler end)
                    val topWktY = height * 0.05f
                    drawLine(Color(0xFFFF007F), Offset(width * 0.46f, topWktY), Offset(width * 0.46f, topWktY + 20f), strokeWidth = 4f)
                    drawLine(Color(0xFFFF007F), Offset(width * 0.50f, topWktY), Offset(width * 0.50f, topWktY + 20f), strokeWidth = 4f)
                    drawLine(Color(0xFFFF007F), Offset(width * 0.54f, topWktY), Offset(width * 0.54f, topWktY + 20f), strokeWidth = 4f)
                    drawLine(Color(0xFFFF007F), Offset(width * 0.44f, topWktY), Offset(width * 0.56f, topWktY), strokeWidth = 3f)

                    // Draw batting side crease
                    val creaseY = height * 0.85f
                    drawLine(Color(0xFF00FFFF), Offset(0f, creaseY), Offset(width, creaseY), strokeWidth = 2f)

                    // Draw glowing sweet spot indicator bar inside pitch
                    val sweetYMin = height * sweetSpotMin
                    val sweetYMax = height * sweetSpotMax
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x3300FFFF),
                                Color(0xAAFF007F),
                                Color(0x3300FFFF)
                            )
                        ),
                        topLeft = Offset(width * 0.15f, sweetYMin),
                        size = androidx.compose.ui.geometry.Size(width * 0.7f, sweetYMax - sweetYMin)
                    )

                    // Lines indicating peak spot
                    drawLine(
                        color = Color.White,
                        start = Offset(width * 0.15f, height * sweetSpotCenter),
                        end = Offset(width * 0.85f, height * sweetSpotCenter),
                        strokeWidth = 4f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // If a ball is rolling down, draw it!
                    if (isBallActive) {
                        val currX = width * ballX
                        val currY = height * ballY
                        val densityBallRadius = 14f * ballY // increments size as it rolls closer

                        // Draw ball tail glow
                        drawCircle(
                            color = Color(0x5500FFFF),
                            radius = densityBallRadius + 15f,
                            center = Offset(currX, currY)
                        )
                        // Ball center
                        drawCircle(
                            color = Color.White,
                            radius = densityBallRadius,
                            center = Offset(currX, currY)
                        )
                        drawCircle(
                            color = Color(0xFF00FFFF),
                            radius = densityBallRadius - 3f,
                            center = Offset(currX, currY),
                            style = Stroke(width = 3f)
                        )
                    }
                }

                // Center notification feedback (6s, 4s, Out!)
                androidx.compose.animation.AnimatedVisibility(
                    visible = feedbackVisible,
                    enter = scaleIn(animationSpec = spring(dampingRatio = 0.5f)) + fadeIn(),
                    exit = fadeOut(animationSpec = tween(500))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(Color(0xEE050012), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                            .border(1.dp, feedbackColor, RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = battingFeedback,
                            color = feedbackColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap Bowler to receive the next ball",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }

                // When game over screen overlays inside canvas
                if (state.isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFA090016)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = if (state.gameMode == "QUICK" && state.didWin) "🏆 CHASE COMPLETED!" else "💔 GAME OVER",
                                color = if (state.gameMode == "QUICK" && state.didWin) Color(0xFF39FF14) else Color.Red,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Final Score: ${state.runs} Runs (Wickets lost: ${state.wickets}/3)",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            
                            if (state.gameMode == "QUICK" && state.didWin) {
                                Text(
                                    text = "Spectacular batting under pressure!",
                                    color = Color(0xFFE0AAFF),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Row {
                                Button(
                                    onClick = {
                                        feedbackVisible = false
                                        viewModel.resetCricketGame(state.gameMode)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD)),
                                    modifier = Modifier.testTag("cricket_retry_btn")
                                ) {
                                    Icon(Icons.Filled.Refresh, "Retry")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Play Again")
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                OutlinedButton(
                                    onClick = {
                                        // Change modes
                                        feedbackVisible = false
                                        val otherMode = if (state.gameMode == "QUICK") "ENDLESS" else "QUICK"
                                        viewModel.resetCricketGame(otherMode)
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00FFFF)),
                                    modifier = Modifier.border(1.dp, Color(0xFF00FFFF), RoundedCornerShape(50))
                                ) {
                                    Text(if (state.gameMode == "QUICK") "Endless Mode" else "Quick 1-Over")
                                }
                            }
                        }
                    }
                }
            }

            // Controls layout
            BottomAppBar(
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Active bowler type ticker
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "BOWLER SENSE",
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBallActive) "$currentBowlerType PACER" else "WAITING ON RUN-UP",
                            color = if (isBallActive) Color(0xFFFF007F) else Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Large glowing Action Buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isBallActive && !state.isGameOver) {
                            Button(
                                onClick = {
                                    feedbackVisible = false
                                    bowlBall()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .testTag("bowl_delivery_btn")
                            ) {
                                Icon(Icons.Filled.PlayArrow, "Bowl", tint = Color.Black)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("BOWLER RUN", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { swingBat() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .testTag("swing_bat_btn")
                            ) {
                                Text("⚡ HIT BAT!", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
