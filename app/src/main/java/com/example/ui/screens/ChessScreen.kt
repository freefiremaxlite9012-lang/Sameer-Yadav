package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChessStateData
import com.example.ui.MainViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessScreen(
    viewModel: MainViewModel,
    state: ChessStateData,
    onBack: () -> Unit
) {
    var selectedSquare by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Convert flat board string (128 chars, 64 * 2-char pieces) to map/list representation
    val boardLength = 64
    val boardString = state.board

    fun getPiece(index: Int): String {
        if (index !in 0..63) return ".."
        val start = index * 2
        return boardString.substring(start, start + 2)
    }

    // Helper to calculate raw valid destinations for a selected square index
    fun calculateValidMoves(fromIdx: Int): List<Int> {
        val moves = mutableListOf<Int>()
        val piece = getPiece(fromIdx)
        if (piece == "..") return moves

        val color = piece[0] // 'w' or 'b'
        val type = piece[1]  // 'p', 'r', 'n', 'b', 'q', 'k'

        val fromRow = fromIdx / 8
        val fromCol = fromIdx % 8

        fun addIfValid(toRow: Int, toCol: Int): Boolean {
            if (toRow !in 0..7 || toCol !in 0..7) return false
            val targetIdx = toRow * 8 + toCol
            val targetPiece = getPiece(targetIdx)
            if (targetPiece == "..") {
                moves.add(targetIdx)
                return true // clear to continue stepping for sliding pieces
            }
            if (targetPiece[0] != color) {
                moves.add(targetIdx)
            }
            return false // hit a piece, stop sliding
        }

        when (type) {
            'p' -> {
                // Pawn movement
                val direction = if (color == 'w') -1 else 1 // White moves up (decrement row)
                val startRow = if (color == 'w') 6 else 1

                // 1 step forward
                val nextRow = fromRow + direction
                if (nextRow in 0..7) {
                    val forwardIdx = nextRow * 8 + fromCol
                    if (getPiece(forwardIdx) == "..") {
                        moves.add(forwardIdx)
                        // 2 steps forward
                        if (fromRow == startRow) {
                            val twoForwardIdx = (fromRow + 2 * direction) * 8 + fromCol
                            if (getPiece(twoForwardIdx) == "..") {
                                moves.add(twoForwardIdx)
                            }
                        }
                    }

                    // Diagonal captures
                    for (side in listOf(-1, 1)) {
                        val diagCol = fromCol + side
                        if (diagCol in 0..7) {
                            val diagIdx = nextRow * 8 + diagCol
                            val target = getPiece(diagIdx)
                            if (target != ".." && target[0] != color) {
                                moves.add(diagIdx)
                            }
                        }
                    }
                }
            }
            'r' -> {
                // Rook movement
                val dirs = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))
                for (d in dirs) {
                    var r = fromRow
                    var c = fromCol
                    while (true) {
                        r += d.first
                        c += d.second
                        if (!addIfValid(r, c)) break
                    }
                }
            }
            'b' -> {
                // Bishop movement
                val dirs = listOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1))
                for (d in dirs) {
                    var r = fromRow
                    var c = fromCol
                    while (true) {
                        r += d.first
                        c += d.second
                        if (!addIfValid(r, c)) break
                    }
                }
            }
            'q' -> {
                // Queen movement (Rook + Bishop combination)
                val dirs = listOf(
                    Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1),
                    Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)
                )
                for (d in dirs) {
                    var r = fromRow
                    var c = fromCol
                    while (true) {
                        r += d.first
                        c += d.second
                        if (!addIfValid(r, c)) break
                    }
                }
            }
            'n' -> {
                // Knight movement
                val offsets = listOf(
                    Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
                    Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
                )
                for (o in offsets) {
                    val r = fromRow + o.first
                    val c = fromCol + o.second
                    if (r in 0..7 && c in 0..7) {
                        val targetIdx = r * 8 + c
                        val targetPiece = getPiece(targetIdx)
                        if (targetPiece == ".." || targetPiece[0] != color) {
                            moves.add(targetIdx)
                        }
                    }
                }
            }
            'k' -> {
                // King movement
                val offsets = listOf(
                    Pair(-1, -1), Pair(-1, 0), Pair(-1, 1),
                    Pair(0, -1),             Pair(0, 1),
                    Pair(1, -1),  Pair(1, 0),  Pair(1, 1)
                )
                for (o in offsets) {
                    val r = fromRow + o.first
                    val c = fromCol + o.second
                    if (r in 0..7 && c in 0..7) {
                        val targetIdx = r * 8 + c
                        val targetPiece = getPiece(targetIdx)
                        if (targetPiece == ".." || targetPiece[0] != color) {
                            moves.add(targetIdx)
                        }
                    }
                }
            }
        }
        return moves
    }

    val activeValidDestinations = selectedSquare?.let { calculateValidMoves(it) } ?: emptyList()

    fun makeMove(from: Int, to: Int) {
        val pieceToMove = getPiece(from)
        val targetPiece = getPiece(to)
        
        if (targetPiece != "..") {
            com.example.ui.SoundManager.playChessCapture()
        } else {
            com.example.ui.SoundManager.playChessMove()
        }
        
        // Build new board string
        val sb = StringBuilder()
        for (i in 0 until 64) {
            when (i) {
                from -> sb.append("..")
                to -> {
                    // Check for Pawn Promotion (auto-promote pawns to Queen on reaching back rank)
                    val color = pieceToMove[0]
                    val type = pieceToMove[1]
                    val toRow = to / 8
                    if (type == 'p' && (toRow == 0 || toRow == 7)) {
                        sb.append("${color}q")
                    } else {
                        sb.append(pieceToMove)
                    }
                }
                else -> sb.append(getPiece(i))
            }
        }

        val newBoard = sb.toString()
        val nextTurn = if (state.activeTurn == "w") "b" else "w"

        // Search if king of other side is absent -> Checkmate / Capture Win
        var whiteKingAlive = false
        var blackKingAlive = false
        for (i in 0 until 64) {
            val square = newBoard.substring(i * 2, i * 2 + 2)
            if (square == "wk") whiteKingAlive = true
            if (square == "bk") blackKingAlive = true
        }

        var status = "ACTIVE"
        if (!whiteKingAlive) {
            status = "CHECKMATE_B" // Black won
        } else if (!blackKingAlive) {
            status = "CHECKMATE_W" // White won
        }

        viewModel.updateChessState(
            ChessStateData(
                board = newBoard,
                activeTurn = nextTurn,
                gameStatus = status
            )
        )
        selectedSquare = null
        errorMessage = null
    }

    fun getPieceSymbol(pieceStr: String): String {
        return when (pieceStr) {
            "wp" -> "♙" // White Pawn
            "wr" -> "♖" // White Rook
            "wn" -> "♘" // White Knight
            "wb" -> "♗" // White Bishop
            "wq" -> "♕" // White Queen
            "wk" -> "♔" // White King
            "bp" -> "♟" // Black Pawn
            "br" -> "♜" // Black Rook
            "bn" -> "♞" // Black Knight
            "bb" -> "♝" // Black Bishop
            "bq" -> "♛" // Black Queen
            "bk" -> "♚" // Black King
            else -> ""
        }
    }

    fun getPieceColorInGame(pieceStr: String): Color {
        return if (pieceStr.startsWith("w")) {
            Color(0xFF39FF14) // Neon Cyber Green for player 1
        } else {
            Color(0xFFFF007F) // Neon Magenta/Pink for player 2
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "NEON CHESS",
                        color = Color(0xFFC77DFF),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chess_back_btn")) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFF007F)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetChessGame() }, modifier = Modifier.testTag("chess_reset_btn")) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reset Game",
                            tint = Color(0xFFE0AAFF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF090014),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF060012)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Hotseat Turn / Game Over Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        border = BorderStroke(2.dp, Brush.linearGradient(listOf(Color(0xFF9D4EDD), Color(0xFF00FFFF)))),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11002A))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.gameStatus.startsWith("CHECKMATE")) {
                        val winner = if (state.gameStatus == "CHECKMATE_W") "PLAYER 1 (GREEN GLOW)" else "PLAYER 2 (MAGENTA GLOW)"
                        val winnerColor = if (state.gameStatus == "CHECKMATE_W") Color(0xFF39FF14) else Color(0xFFFF007F)
                        Text(
                            text = "👑 CHECKMATE WINNER! 👑",
                            color = winnerColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "$winner wins the match!",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        val activeName = if (state.activeTurn == "w") "PLAYER 1" else "PLAYER 2"
                        val turnColor = if (state.activeTurn == "w") Color(0xFF39FF14) else Color(0xFFFF007F)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(turnColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACTIVE TURN: $activeName",
                                color = turnColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Pass and play hotseat! Tap checking pieces to move.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Interactive Neon 8x8 Chessboard Screen Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Keep perfectly square
                    .border(3.dp, Color(0xFF7B2CBF), RoundedCornerShape(8.dp))
                    .background(Color(0xFF050010))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (row in 0..7) {
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            for (col in 0..7) {
                                val index = row * 8 + col
                                val isDarkSquare = (row + col) % 2 == 1
                                val piece = getPiece(index)
                                val isSelected = selectedSquare == index
                                val isPossibleMove = activeValidDestinations.contains(index)

                                val baseBg = if (isDarkSquare) Color(0xFF0B001E) else Color(0xFF1E0B36)
                                val cellBg = when {
                                    isSelected -> Color(0x7F00FFFF) // Clear Neon Cyber Cyan hue
                                    isPossibleMove -> Color(0x7F39FF14) // Glowing green target cell space
                                    else -> baseBg
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(cellBg)
                                        .border(0.5.dp, Color(0x339D4EDD))
                                        .clickable {
                                            if (state.gameStatus.startsWith("CHECKMATE")) return@clickable
                                            
                                            if (isPossibleMove) {
                                                // Execute move!
                                                selectedSquare?.let { from ->
                                                    makeMove(from, index)
                                                }
                                            } else if (piece != ".." && piece[0].toString() == state.activeTurn) {
                                                // Select piece
                                                selectedSquare = index
                                            } else {
                                                selectedSquare = null
                                            }
                                        }
                                        .testTag("chess_square_${row}_${col}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Custom visual indicators for move paths
                                    if (isPossibleMove && piece == "..") {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF39FF14)) // Green circle destination node
                                        )
                                    }

                                    if (piece != "..") {
                                        Text(
                                            text = getPieceSymbol(piece),
                                            color = getPieceColorInGame(piece),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Legendary Neon Info details at bottom
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x55090014))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = "Details",
                        tint = Color(0xFF00FFFF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Auto-save is active. If you background or exit, pick up Chess immediately where you closed!",
                        color = Color(0xFFC77DFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
