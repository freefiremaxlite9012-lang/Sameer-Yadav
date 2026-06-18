package com.example.data

// Models for type-safe representation of game states within the application.

enum class Difficulty { LOW, MEDIUM, HIGH }

data class LudoStateData(
    val activePlayer: Int = 0,         // 0=Red, 1=Green, 2=Yellow, 3=Blue
    val diceRolled: Boolean = false,
    val lastRollValue: Int = 1,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val tokenPositions: List<Int> = List(16) { -1 } // 4 tokens per player. relative pos: -1..57
) {
    fun serialize(): String {
        val posStr = tokenPositions.joinToString(",")
        return "$activePlayer;$diceRolled;$lastRollValue;${difficulty.name};$posStr"
    }

    companion object {
        fun deserialize(serialized: String): LudoStateData {
            return try {
                val parts = serialized.split(";")
                val activePlayer = parts[0].toInt()
                val diceRolled = parts[1].toBoolean()
                val lastRollValue = parts[2].toInt()
                val difficulty = Difficulty.valueOf(parts[3])
                val tokenPositions = parts[4].split(",").map { it.toInt() }
                LudoStateData(activePlayer, diceRolled, lastRollValue, difficulty, tokenPositions)
            } catch (e: Exception) {
                LudoStateData() // Return default on error
            }
        }
    }
}

data class ChessStateData(
    val board: String = INITIAL_BOARD, // 128 chars (each square is 2 chars)
    val activeTurn: String = "w",      // "w"=White, "b"=Black
    val gameStatus: String = "ACTIVE"  // "ACTIVE", "CHECKMATE_W", "CHECKMATE_B", "STALEMATE"
) {
    fun serialize(): String {
        return "$board;$activeTurn;$gameStatus"
    }

    companion object {
        const val INITIAL_BOARD = 
            "brbnbbbqbkbbbnbr" + // Black back row: Rook, Knight, Bishop, Queen, King, Bishop, Knight, Rook
            "bpbpbpbpbpbpbpbp" + // Black pawns
            "................" + // Row 2 empty (8 squares * 2 chars = 16 dots)
            "................" + // Row 3 empty
            "................" + // Row 4 empty
            "................" + // Row 5 empty
            "wpwpwpwpwpwpwpwp" + // White pawns
            "wrwnwbwqwkwbwnwr"   // White back row

        fun deserialize(serialized: String): ChessStateData {
            return try {
                val parts = serialized.split(";")
                ChessStateData(
                    board = parts[0],
                    activeTurn = parts[1],
                    gameStatus = parts[2]
                )
            } catch (e: Exception) {
                ChessStateData()
            }
        }
    }
}

data class CricketStateData(
    val runs: Int = 0,
    val wickets: Int = 0,
    val balls: Int = 0,
    val target: Int = 18,              // Chase target in quick match
    val highStreak: Int = 0,           // Highest scoring streak in endless mode
    val gameMode: String = "QUICK",    // "QUICK" or "ENDLESS"
    val isGameOver: Boolean = false,
    val didWin: Boolean = false
) {
    fun serialize(): String {
        return "$runs;$wickets;$balls;$target;$highStreak;$gameMode;$isGameOver;$didWin"
    }

    companion object {
        fun deserialize(serialized: String): CricketStateData {
            return try {
                val parts = serialized.split(";")
                CricketStateData(
                    runs = parts[0].toInt(),
                    wickets = parts[1].toInt(),
                    balls = parts[2].toInt(),
                    target = parts[3].toInt(),
                    highStreak = parts[4].toInt(),
                    gameMode = parts[5],
                    isGameOver = parts[6].toBoolean(),
                    didWin = parts[7].toBoolean()
                )
            } catch (e: Exception) {
                CricketStateData()
            }
        }
    }
}
