package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Difficulty
import com.example.data.LudoStateData
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LudoScreen(
    viewModel: MainViewModel,
    state: LudoStateData,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isRollingAnimation by remember { mutableStateOf(false) }

    // Evaluate Win check
    val winnerPlayer = remember(state.tokenPositions) {
        var finishedPlayer: Int? = null
        for (p in 0 until 4) {
            val base = p * 4
            var completedCount = 0
            for (t in 0 until 4) {
                if (state.tokenPositions[base + t] == 57) {
                    completedCount++
                }
            }
            if (completedCount == 4) {
                finishedPlayer = p
                break
            }
        }
        finishedPlayer
    }

    // Map of absolute track positions on 15x15 Ludo grid
    val absolutePath = listOf(
        Pair(6, 1), Pair(6, 2), Pair(6, 3), Pair(6, 4), Pair(6, 5), // Red start track path
        Pair(5, 6), Pair(4, 6), Pair(3, 6), Pair(2, 6), Pair(1, 6), Pair(0, 6), // Top segment Green side
        Pair(0, 7), // Peak North
        Pair(0, 8), Pair(1, 8), Pair(2, 8), Pair(3, 8), Pair(4, 8), Pair(5, 8), // Down Green side
        Pair(6, 9), Pair(6, 10), Pair(6, 11), Pair(6, 12), Pair(6, 13), Pair(6, 14), // Right
        Pair(7, 14), // Peak East
        Pair(8, 14), Pair(8, 13), Pair(8, 12), Pair(8, 11), Pair(8, 10), Pair(8, 9), // Left Yellow return
        Pair(9, 8), Pair(10, 8), Pair(11, 8), Pair(12, 8), Pair(13, 8), Pair(14, 8), // Down Blue segment
        Pair(14, 7), // Peak South
        Pair(14, 6), Pair(13, 6), Pair(12, 6), Pair(11, 6), Pair(10, 6), Pair(9, 6), // Up
        Pair(8, 5), Pair(8, 4), Pair(8, 3), Pair(8, 2), Pair(8, 1), Pair(8, 0), // Return left Red side
        Pair(7, 0), // Peak West
        Pair(6, 0)  // Red last entry
    )

    // Player Colors
    val playerColors = listOf(
        Color(0xFFFF007F), // Red/Pink Neon
        Color(0xFF39FF14), // Green Neon
        Color(0xFFFFCC00), // Yellow/Gold Neon
        Color(0xFF00FFFF)  // Blue/Cyan Neon
    )
    val playerNames = listOf("RED (YOU)", "GREEN (AI)", "YELLOW (AI)", "BLUE (AI)")

    // Helper to identify player offset in the absolute configuration path
    val startOffsets = listOf(0, 13, 26, 39)

    // Function to calculate grid Row and Column of an active token
    fun getTokenCoordinates(player: Int, tokenIndex: Int, pos: Int): Pair<Int, Int> {
        if (pos == -1) {
            // Yard locations
            return when (player) {
                0 -> when (tokenIndex) {
                    0 -> Pair(2, 2); 1 -> Pair(2, 3); 2 -> Pair(3, 2); else -> Pair(3, 3)
                }
                1 -> when (tokenIndex) {
                    0 -> Pair(2, 11); 1 -> Pair(2, 12); 2 -> Pair(3, 11); else -> Pair(3, 12)
                }
                2 -> when (tokenIndex) {
                    0 -> Pair(11, 11); 1 -> Pair(11, 12); 2 -> Pair(12, 11); else -> Pair(12, 12)
                }
                else -> when (tokenIndex) {
                    0 -> Pair(11, 2); 1 -> Pair(11, 3); 2 -> Pair(12, 2); else -> Pair(12, 3)
                }
            }
        }

        if (pos == 57) {
            // Home center destination (7,7)
            return Pair(7, 7)
        }

        if (pos in 51..56) {
            // Home stretch pathways
            val step = pos - 51
            return when (player) {
                0 -> Pair(7, 1 + step)
                1 -> Pair(1 + step, 7)
                2 -> Pair(7, 13 - step)
                else -> Pair(13 - step, 7)
            }
        }

        // Relative on absolute path
        val absolutePos = (pos + startOffsets[player]) % 52
        return absolutePath[absolutePos]
    }

    // Identify if player has any playable moves for their current rolled value
    fun getPlayableTokens(player: Int, roll: Int): List<Int> {
        val playable = mutableListOf<Int>()
        val baseOffset = player * 4
        for (i in 0 until 4) {
            val tokenIdx = baseOffset + i
            val currentPos = state.tokenPositions[tokenIdx]
            
            if (currentPos == -1) {
                if (roll == 6) {
                    playable.add(i) // Putting Out from Yard requires 6
                }
            } else if (currentPos in 0..56) {
                if (currentPos + roll <= 57) {
                    playable.add(i) // Moving forward
                }
            }
        }
        return playable
    }

    // Method to execute a turn movement
    fun moveToken(player: Int, tokenIndex: Int) {
        if (isRollingAnimation) return
        com.example.ui.SoundManager.playPieceMove()
        
        val baseOffset = player * 4
        val targetTokenGlobalIdx = baseOffset + tokenIndex
        val currentRelativePos = state.tokenPositions[targetTokenGlobalIdx]
        val rollValue = state.lastRollValue

        // Compute destination
        val nextRelativePos = if (currentRelativePos == -1) {
            0 // Put token out of base
        } else {
            currentRelativePos + rollValue
        }

        // Update token positions array
        val updatedPositions = state.tokenPositions.toMutableList()
        updatedPositions[targetTokenGlobalIdx] = nextRelativePos

        // Check for collisions and kicks (if landing on opponent's token)
        if (nextRelativePos in 0..50) {
            val destinationGrid = getTokenCoordinates(player, tokenIndex, nextRelativePos)
            for (p in 0 until 4) {
                if (p == player) continue // Ignore own tokens
                val otherBaseOffset = p * 4
                for (t in 0 until 4) {
                    val otherGlobalIdx = otherBaseOffset + t
                    val otherRelativePos = updatedPositions[otherGlobalIdx]
                    if (otherRelativePos in 0..50) {
                        val otherGrid = getTokenCoordinates(p, t, otherRelativePos)
                        if (otherGrid == destinationGrid) {
                            // Safe spot verification: Star/starting squares
                            val isSafe = when (destinationGrid) {
                                Pair(6, 1), Pair(0, 8), Pair(8, 13), Pair(14, 6) -> true
                                else -> false
                            }
                            if (!isSafe) {
                                // Kick back opponent's token to base!
                                updatedPositions[otherGlobalIdx] = -1
                            }
                        }
                    }
                }
            }
        }

        // Advance player turn
        val nextPlayer = (player + 1) % 4
        val nextStateObj = state.copy(
            tokenPositions = updatedPositions,
            activePlayer = nextPlayer,
            diceRolled = false
        )
        viewModel.updateLudoState(nextStateObj)
    }

    // AI Auto Decision Engine
    fun executeAILogic() {
        if (state.activePlayer == 0 || winnerPlayer != null) return // Player 0 is YOU

        coroutineScope.launch {
            // Dice roll pause emulation to look natural
            delay(1000)
            val roll = (1..6).random()
            
            // Trigger rolling visual cue
            isRollingAnimation = true
            com.example.ui.SoundManager.playDiceRoll()
            delay(700)
            isRollingAnimation = false
            
            val playable = getPlayableTokens(state.activePlayer, roll)
            
            if (playable.isEmpty()) {
                // Pass turn on no playable tokens after visual roll
                viewModel.updateLudoState(
                    state.copy(
                        lastRollValue = roll,
                        diceRolled = false,
                        activePlayer = (state.activePlayer + 1) % 4
                    )
                )
            } else {
                // Pick token based on AI difficulty selection Level
                val selectedTokenIndex = when (state.difficulty) {
                    Difficulty.LOW -> playable.random()
                    Difficulty.MEDIUM -> {
                        // Prefer putting pieces out with 6, or pieces nearest home
                        val putOut = playable.find { state.tokenPositions[state.activePlayer * 4 + it] == -1 }
                        if (putOut != null && roll == 6) {
                            putOut
                        } else {
                            playable.maxByOrNull { state.tokenPositions[state.activePlayer * 4 + it] } ?: playable.random()
                        }
                    }
                    Difficulty.HIGH -> {
                        // Looks ahead to catch/kick players first
                        var hitToken: Int? = null
                        val baseLocalOffset = state.activePlayer * 4
                        
                        for (tokenIdx in playable) {
                            val currPos = state.tokenPositions[baseLocalOffset + tokenIdx]
                            val targetRelative = if (currPos == -1) 0 else currPos + roll
                            if (targetRelative in 0..50) {
                                val targetGrid = getTokenCoordinates(state.activePlayer, tokenIdx, targetRelative)
                                // See if any opponent piece is here
                                opponentSearch@for (p in 0 until 4) {
                                    if (p == state.activePlayer) continue
                                    val otherBase = p * 4
                                    for (t in 0 until 4) {
                                        val opPos = state.tokenPositions[otherBase + t]
                                        if (opPos in 0..50 && getTokenCoordinates(p, t, opPos) == targetGrid) {
                                            hitToken = tokenIdx
                                            break@opponentSearch
                                        }
                                    }
                                }
                            }
                        }
                        
                        hitToken ?: playable.maxByOrNull { state.tokenPositions[state.activePlayer * 4 + it] } ?: playable.random()
                    }
                }
                
                // Show roll result first
                viewModel.updateLudoState(state.copy(lastRollValue = roll, diceRolled = true))
                delay(800)
                moveToken(state.activePlayer, selectedTokenIndex)
            }
        }
    }

    // Watch for AI Turn
    LaunchedEffect(state.activePlayer, state.diceRolled) {
        if (state.activePlayer != 0 && !state.diceRolled && winnerPlayer == null) {
            executeAILogic()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "NEON LUDO",
                        color = Color(0xFFC77DFF),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("ludo_back_btn")) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFF007F)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetLudoGame() }, modifier = Modifier.testTag("ludo_reset_btn")) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = Color(0xFF00FFFF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF090014),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF050010)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Settings controls: AI difficulty select
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("AI Opponent Difficulty:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row {
                        listOf(Difficulty.LOW, Difficulty.MEDIUM, Difficulty.HIGH).forEach { diff ->
                            val isSelected = state.difficulty == diff
                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp, top = 2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0xFF9D4EDD) else Color(0x339D4EDD))
                                    .border(1.dp, if (isSelected) Color(0xFFE0AAFF) else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable {
                                        viewModel.updateLudoState(state.copy(difficulty = diff))
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = diff.name,
                                    color = if (isSelected) Color.White else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Header status displays active turn
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140129)),
                    modifier = Modifier.border(1.dp, Color(0xFF7B2CBF), RoundedCornerShape(8.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("ACTIVE TURN", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        val currCol = playerColors[state.activePlayer]
                        Text(
                            text = playerNames[state.activePlayer],
                            color = currCol,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Interactive Drawing Ludo Board Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Keep perfectly square
                    .border(2.dp, Color(0xFF7B2CBF), RoundedCornerShape(6.dp))
                    .background(Color(0xFF050010)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val boardWidth = size.width
                    val cellWidth = boardWidth / 15f

                    // Draw outer border and grid backing lines
                    drawRect(Color(0xFF030009), size = size)

                    // Draw base regions (large glowing corner color plates)
                    // Top-Left (Red)
                    drawRect(Color(0x33FF007F), topLeft = Offset(0f, 0f), size = Size(cellWidth * 6, cellWidth * 6))
                    drawRect(Color(0xFFFF007F), topLeft = Offset(0f, 0f), size = Size(cellWidth * 6, cellWidth * 6), style = Stroke(width = 3f))
                    // Top-Right (Green)
                    drawRect(Color(0x3339FF14), topLeft = Offset(cellWidth * 9, 0f), size = Size(cellWidth * 6, cellWidth * 6))
                    drawRect(Color(0xFF39FF14), topLeft = Offset(cellWidth * 9, 0f), size = Size(cellWidth * 6, cellWidth * 6), style = Stroke(width = 3f))
                    // Bottom-Right (Yellow)
                    drawRect(Color(0x33FFCC00), topLeft = Offset(cellWidth * 9, cellWidth * 9), size = Size(cellWidth * 6, cellWidth * 6))
                    drawRect(Color(0xFFFFCC00), topLeft = Offset(cellWidth * 9, cellWidth * 9), size = Size(cellWidth * 6, cellWidth * 6), style = Stroke(width = 3f))
                    // Bottom-Left (Blue)
                    drawRect(Color(0x3300FFFF), topLeft = Offset(0f, cellWidth * 9), size = Size(cellWidth * 6, cellWidth * 6))
                    drawRect(Color(0xFF00FFFF), topLeft = Offset(0f, cellWidth * 9), size = Size(cellWidth * 6, cellWidth * 6), style = Stroke(width = 3f))

                    // Draw home destination target triangles in center from cell cell (6,6) to (8,8)
                    val centerMin = cellWidth * 6
                    val centerMax = cellWidth * 9
                    // Home center point (7.5 cell offsets)
                    val centerPt = Offset(boardWidth / 2f, boardWidth / 2f)

                    // Draw colored triangles merging into home center
                    // West (Red Triangle)
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(centerMin, centerMin)
                            lineTo(centerPt.x, centerPt.y)
                            lineTo(centerMin, centerMax)
                            close()
                        },
                        color = Color(0x33FF007F)
                    )
                    // North (Green Triangle)
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(centerMin, centerMin)
                            lineTo(centerPt.x, centerPt.y)
                            lineTo(centerMax, centerMin)
                            close()
                        },
                        color = Color(0x3339FF14)
                    )
                    // East (Yellow Triangle)
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(centerMax, centerMin)
                            lineTo(centerPt.x, centerPt.y)
                            lineTo(centerMax, centerMax)
                            close()
                        },
                        color = Color(0x33FFCC00)
                    )
                    // South (Blue Triangle)
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(centerMin, centerMax)
                            lineTo(centerPt.x, centerPt.y)
                            lineTo(centerMax, centerMax)
                            close()
                        },
                        color = Color(0x3300FFFF)
                    )

                    // Draw all grid square nodes on 15x15 board
                    for (row in 0 until 15) {
                        for (col in 0 until 15) {
                            val isSpecialZone = (row < 6 && col < 6) || (row < 6 && col > 8) || (row > 8 && col < 6) || (row > 8 && col > 8)
                            val isCenterHome = row in 6..8 && col in 6..8
                            
                            if (!isSpecialZone && !isCenterHome) {
                                // Draw regular square outlines with subtle purple glow
                                drawRect(
                                    color = Color(0x229D4EDD),
                                    topLeft = Offset(col * cellWidth, row * cellWidth),
                                    size = Size(cellWidth, cellWidth),
                                    style = Stroke(width = 1.dp.toPx())
                                )

                                // Highlight home stretches with solid neon base tints
                                if (row == 7 && col in 1..5) drawRect(Color(0x33FF007F), topLeft = Offset(col * cellWidth, row * cellWidth), size = Size(cellWidth, cellWidth))
                                if (col == 7 && row in 1..5) drawRect(Color(0x3339FF14), topLeft = Offset(col * cellWidth, row * cellWidth), size = Size(cellWidth, cellWidth))
                                if (row == 7 && col in 9..13) drawRect(Color(0x33FFCC00), topLeft = Offset(col * cellWidth, row * cellWidth), size = Size(cellWidth, cellWidth))
                                if (col == 7 && row in 9..13) drawRect(Color(0x3300FFFF), topLeft = Offset(col * cellWidth, row * cellWidth), size = Size(cellWidth, cellWidth))

                                // Highlight start star nodes
                                if (row == 6 && col == 1) drawCircle(Color(0xFFFF007F), radius = cellWidth * 0.2f, center = Offset(col * cellWidth + cellWidth / 2f, row * cellWidth + cellWidth / 2f))
                                if (row == 1 && col == 8) drawCircle(Color(0xFF39FF14), radius = cellWidth * 0.2f, center = Offset(col * cellWidth + cellWidth / 2f, row * cellWidth + cellWidth / 2f))
                                if (row == 8 && col == 13) drawCircle(Color(0xFFFFCC00), radius = cellWidth * 0.2f, center = Offset(col * cellWidth + cellWidth / 2f, row * cellWidth + cellWidth / 2f))
                                if (row == 13 && col == 6) drawCircle(Color(0xFF00FFFF), radius = cellWidth * 0.2f, center = Offset(col * cellWidth + cellWidth / 2f, row * cellWidth + cellWidth / 2f))
                            }
                        }
                    }
                }

                // Render interactive glowing tokens mathematically atop Canvas inside the Box
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val boardSize = maxWidth
                    val cellSize = boardSize / 15f

                    // Draw all player pieces
                    for (player in 0 until 4) {
                        val baseCol = playerColors[player]
                        val baseOffset = player * 4
                        
                        for (tokenIdx in 0 until 4) {
                            val globalTokenIdx = baseOffset + tokenIdx
                            val pos = state.tokenPositions[globalTokenIdx]
                            val coords = getTokenCoordinates(player, tokenIdx, pos)
                            
                            val tokenLeft = cellSize * coords.second
                            val tokenTop = cellSize * coords.first

                            // Determine if token is currently playable by USER (Red Only, active roll, has moves)
                            val isMyTurn = state.activePlayer == 0
                            val validMovesList = if (isMyTurn && state.diceRolled && !isRollingAnimation) getPlayableTokens(0, state.lastRollValue) else emptyList()
                            val canClick = isMyTurn && validMovesList.contains(tokenIdx)

                            Box(
                                modifier = Modifier
                                    .size(cellSize)
                                    .offset(x = tokenLeft, y = tokenTop)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(baseCol)
                                    .border(
                                        width = if (canClick) 2.dp else 1.dp,
                                        color = if (canClick) Color.White else Color.Black,
                                        shape = CircleShape
                                    )
                                    .clickable(enabled = canClick) {
                                        moveToken(0, tokenIdx)
                                    }
                                    .testTag("ludo_piece_${player}_${tokenIdx}"),
                                contentAlignment = Alignment.Center
                            ) {
                                // Add subtle glowing layout to piece
                                Box(
                                    modifier = Modifier
                                        .size(cellSize * 0.4f)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Rolling Dice Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        border = BorderStroke(2.dp, Brush.linearGradient(listOf(Color(0xFFFF007F), Color(0xFF00FFFF)))),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0020))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (state.activePlayer == 0) "YOUR TURN" else "AI PROCESSING...",
                            color = playerColors[state.activePlayer],
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        
                        // Show hint messages
                        val playable = if (state.activePlayer == 0 && state.diceRolled) getPlayableTokens(0, state.lastRollValue) else emptyList()
                        Text(
                            text = when {
                                isRollingAnimation -> "Shaking the cup..."
                                state.activePlayer != 0 -> "AI is making moves strategically"
                                !state.diceRolled -> "Roll the cosmic dice!"
                                playable.isEmpty() -> "No valid moves. Auto passing.."
                                else -> "Tap a highlighted piece to move!"
                            },
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    // Numeric Neon Dice Box
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF14002F))
                            .border(2.dp, playerColors[state.activePlayer], RoundedCornerShape(8.dp))
                            .clickable(enabled = state.activePlayer == 0 && !state.diceRolled && !isRollingAnimation) {
                                isRollingAnimation = true
                                com.example.ui.SoundManager.playDiceRoll()
                                coroutineScope.launch {
                                    delay(700)
                                    isRollingAnimation = false
                                    val roll = (1..6).random()
                                    viewModel.updateLudoState(state.copy(lastRollValue = roll, diceRolled = true))
                                    
                                    // If no possible moves, auto-advance play turn
                                    val playableList = getPlayableTokens(0, roll)
                                    if (playableList.isEmpty()) {
                                        delay(1500)
                                        viewModel.updateLudoState(
                                            state.copy(
                                                lastRollValue = roll,
                                                diceRolled = false,
                                                activePlayer = 1
                                            )
                                        )
                                    }
                                }
                            }
                            .testTag("ludo_dice"),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = if (isRollingAnimation) "?" else state.lastRollValue.toString()
                        ) { value ->
                            Text(
                                text = value,
                                color = playerColors[state.activePlayer],
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Instruction details / Game Over Modal
            if (winnerPlayer != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xEE090014)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(Color(0xFF0F0024), RoundedCornerShape(12.dp))
                            .padding(24.dp)
                            .border(2.dp, playerColors[winnerPlayer], RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = "🍾 VICTORY CHAMPION! 🍾",
                            color = playerColors[winnerPlayer],
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${playerNames[winnerPlayer]} completed all 4 tokens home successfully!",
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.resetLudoGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = playerColors[winnerPlayer]),
                            modifier = Modifier.testTag("ludo_play_again_btn")
                        ) {
                            Text("Play Again", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
