package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChessStateData
import com.example.data.CricketStateData
import com.example.data.LudoStateData
import com.example.ui.GameScreen
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    ludoState: LudoStateData,
    chessState: ChessStateData,
    cricketState: CricketStateData
) {
    val scrollState = rememberScrollState()

    // Import the resource ID of our generated neon controller icon
    val neonIconRes = com.example.R.drawable.neon_game_icon_1781745698374

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "NEON ZONE",
                        color = Color(0xFFC77DFF),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Start
                    )
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        // Music Switch Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (com.example.ui.SoundManager.isMusicMuted) Color(0x22FFFFFF) else Color(0x22FF007F))
                                .border(
                                    width = 1.dp,
                                    color = if (com.example.ui.SoundManager.isMusicMuted) Color.Gray else Color(0xFFFF007F),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    com.example.ui.SoundManager.isMusicMuted = !com.example.ui.SoundManager.isMusicMuted
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (com.example.ui.SoundManager.isMusicMuted) "🔇 MUSIC" else "🎵 MUSIC",
                                color = if (com.example.ui.SoundManager.isMusicMuted) Color.Gray else Color(0xFFFF007F),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // SFX Switch Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (com.example.ui.SoundManager.isSfxMuted) Color(0x22FFFFFF) else Color(0x2239FF14))
                                .border(
                                    width = 1.dp,
                                    color = if (com.example.ui.SoundManager.isSfxMuted) Color.Gray else Color(0xFF39FF14),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    com.example.ui.SoundManager.isSfxMuted = !com.example.ui.SoundManager.isSfxMuted
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (com.example.ui.SoundManager.isSfxMuted) "🔇 SFX" else "🔊 SFX",
                                color = if (com.example.ui.SoundManager.isSfxMuted) Color.Gray else Color(0xFF39FF14),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF090014),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF04000D) // Pitch black purple-violet ambient background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant Centered Hero Design Artwork (loaded from R.drawable.neon_game_icon_1781745698374)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFF007F), Color(0xFF9D4EDD), Color(0xFF39FF14))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = neonIconRes),
                    contentDescription = "Cosmic Arcade",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "THREE GAMES IN ONE",
                color = Color(0xFF00FFFF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "CHESS • LUDO • CRICKET",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Game Cards: Chess
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        border = BorderStroke(
                            2.dp,
                            Brush.linearGradient(listOf(Color(0xFF39FF14), Color(0xFF00FFFF)))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { viewModel.navigateTo(GameScreen.Chess) }
                    .testTag("home_chess_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D001F)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "♟️ NEON CHESS",
                            color = Color(0xFF39FF14),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x3339FF14))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "2 PLAYERS",
                                color = Color(0xFF39FF14),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Face off in legendary intellectual combats. Fully functional local pass and play hotseat multiplayer.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Checking Chess Save game state status
                    val isChessActive = chessState.gameStatus == "ACTIVE" && chessState.board != ChessStateData.INITIAL_BOARD
                    if (isChessActive) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val activePName = if (chessState.activeTurn == "w") "Player 1 (Green)" else "Player 2 (Magenta)"
                            Text(
                                text = "Match In-Progress (Turn: $activePName)",
                                color = Color(0xFFFFA500),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "RESUME ➔",
                                color = Color(0xFF39FF14),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = "Ready to play!",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Game Cards: Ludo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        border = BorderStroke(
                            2.dp,
                            Brush.linearGradient(listOf(Color(0xFFFF007F), Color(0xFF9D4EDD)))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { viewModel.navigateTo(GameScreen.Ludo) }
                    .testTag("home_ludo_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF100024)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎲 NEON LUDO",
                            color = Color(0xFFFF007F),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x33FF007F))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "4 PLAYERS + AI",
                                color = Color(0xFFFF007F),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Take turns rolling the dice and moving pieces home. Play against smart AIs with Low, Medium, and High difficulties.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Checking Ludo Active status
                    val isLudoActive = ludoState.tokenPositions.any { it != -1 && it != 57 }
                    if (isLudoActive) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val activePName = when (ludoState.activePlayer) {
                                0 -> "RED (You)"
                                1 -> "GREEN (AI)"
                                2 -> "YELLOW (AI)"
                                else -> "BLUE (AI)"
                            }
                            Text(
                                text = "Match In-Progress (Turn: $activePName - Diff: ${ludoState.difficulty.name})",
                                color = Color(0xFFFFA500),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "RESUME ➔",
                                color = Color(0xFFFF007F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = "Ready to play!",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Game Cards: Cricket
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        border = BorderStroke(
                            2.dp,
                            Brush.linearGradient(listOf(Color(0xFF00FFFF), Color(0xFFFF007F)))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { viewModel.navigateTo(GameScreen.Cricket) }
                    .testTag("home_cricket_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF08001C)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏏 NEON CRICKET",
                            color = Color(0xFF00FFFF),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x3300FFFF))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "TIMING REFLEX",
                                color = Color(0xFF00FFFF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Time your swings inside the neon crease to smash 6s and 4s against fast, spinning, and swinging deliveries.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Highest Batting Record: ${cricketState.highStreak} Runs",
                            color = Color(0xFFFFCC00),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "PLAY ➔",
                            color = Color(0xFF00FFFF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer info about background closing and save
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x33A020F0)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Save Info",
                        tint = Color(0xFFC77DFF)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Close-Friendly Persistence: If you background-exit, your Ludo boards, active Chess matches, and Cricket high scores are securely preserved!",
                        color = Color(0xFFE0AAFF),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
