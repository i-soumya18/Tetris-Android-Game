package com.example.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TetrisTheme {
                TetrisGame()
            }
        }
    }
}

@Composable
fun TetrisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF411EDE),
            secondary = Color(0xFF03DAC6),
            surface = Color(0xFF121212),
            background = Color(0xFF000000)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}

@Composable
fun TetrisGame(viewModel: TetrisViewModel = remember { TetrisViewModel() }) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameStats(score = viewModel.score, level = viewModel.level)

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp)
                .aspectRatio(0.5f)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                .shadow(8.dp)
        ) {
            GameBoard(viewModel)
        }

        GameControls(viewModel)
    }
}
// Update the GameBoard composable to observe the current piece
@Composable
fun GameBoard(viewModel: TetrisViewModel) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cellSize = size.width / 10

        // Draw grid lines
        for (i in 0..10) {
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(i * cellSize, 0f),
                end = Offset(i * cellSize, size.height),
                strokeWidth = 1f
            )
        }
        for (i in 0..20) {
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(0f, i * cellSize),
                end = Offset(size.width, i * cellSize),
                strokeWidth = 1f
            )
        }

        // Draw placed blocks
        for (y in 0 until 20) {
            for (x in 0 until 10) {
                if (viewModel.board[y][x] != Color.Black) {
                    drawRect(
                        color = viewModel.board[y][x],
                        topLeft = Offset(x * cellSize + 1, y * cellSize + 1),
                        size = androidx.compose.ui.geometry.Size(cellSize - 2, cellSize - 2),
                        style = Fill
                    )
                    drawRect(
                        color = Color.White.copy(alpha = 0.3f),
                        topLeft = Offset(x * cellSize + 2, y * cellSize + 2),
                        size = androidx.compose.ui.geometry.Size(cellSize * 0.3f, cellSize * 0.3f),
                        style = Fill
                    )
                }
            }
        }

        // Draw current piece
        viewModel.currentPiece?.let { piece ->
            for (y in piece.shape.indices) {
                for (x in piece.shape[y].indices) {
                    if (piece.shape[y][x]) {
                        val boardX = viewModel.position.first + x
                        val boardY = viewModel.position.second + y
                        if (boardY >= 0) {
                            drawRect(
                                color = piece.color,
                                topLeft = Offset(boardX * cellSize + 1, boardY * cellSize + 1),
                                size = androidx.compose.ui.geometry.Size(cellSize - 2, cellSize - 2),
                                style = Fill
                            )
                            drawRect(
                                color = Color.White.copy(alpha = 0.3f),
                                topLeft = Offset(boardX * cellSize + 2, boardY * cellSize + 2),
                                size = androidx.compose.ui.geometry.Size(cellSize * 0.3f, cellSize * 0.3f),
                                style = Fill
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameStats(score: Int, level: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatCard("SCORE", score)
        StatCard("LEVEL", level)
    }
}

@Composable
fun StatCard(label: String, value: Int) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .width(120.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
            Text(
                text = value.toString(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GameControls(viewModel: TetrisViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ControlButton(text = "←", onClick = { viewModel.moveLeft() })
        ControlButton(text = "↻", onClick = { viewModel.rotate() })
        ControlButton(text = "→", onClick = { viewModel.moveRight() })
        ControlButton(text = "↓", onClick = { viewModel.drop() })
    }
}

@Composable
fun ControlButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(64.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

class TetrisViewModel : ViewModel() {
    // Use MutableState for reactive updates
    private var _board = mutableStateOf(Array(20) { Array(10) { Color.Black } })
    val board: Array<Array<Color>> get() = _board.value

    private var _currentPiece = mutableStateOf<Tetromino?>(Tetromino.random())
    val currentPiece: Tetromino? get() = _currentPiece.value

    private var _position = mutableStateOf(Pair(4, 0))
    val position: Pair<Int, Int> get() = _position.value

    private var _score = mutableStateOf(0)
    val score: Int get() = _score.value

    private var _level = mutableStateOf(1)
    val level: Int get() = _level.value

    private var gameSpeed = 500L
    private var isGameActive = true

    init {
        startGameLoop()
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            while (isGameActive) {
                delay(gameSpeed)
                moveDown()
            }
        }
    }

    fun moveLeft() {
        if (isValidMove(position.first - 1, position.second, currentPiece)) {
            _position.value = _position.value.copy(first = _position.value.first - 1)
        }
    }

    fun moveRight() {
        if (isValidMove(position.first + 1, position.second, currentPiece)) {
            _position.value = _position.value.copy(first = _position.value.first + 1)
        }
    }

    fun rotate() {
        currentPiece?.let { piece ->
            val rotatedPiece = piece.rotate()
            if (isValidMove(position.first, position.second, rotatedPiece)) {
                _currentPiece.value = rotatedPiece
            }
        }
    }

    fun drop() {
        while (moveDown()) {}
    }

    private fun moveDown(): Boolean {
        return if (isValidMove(position.first, position.second + 1, currentPiece)) {
            _position.value = _position.value.copy(second = _position.value.second + 1)
            true
        } else {
            placePiece()
            val linesCleared = clearLines()
            updateScore(linesCleared)
            _currentPiece.value = Tetromino.random()
            _position.value = Pair(4, 0)

            // Check for game over
            if (!isValidMove(4, 0, currentPiece)) {
                isGameActive = false
            }
            false
        }
    }

    private fun isValidMove(newX: Int, newY: Int, piece: Tetromino?): Boolean {
        if (piece == null) return false

        for (y in piece.shape.indices) {
            for (x in piece.shape[y].indices) {
                if (piece.shape[y][x]) {
                    val boardX = newX + x
                    val boardY = newY + y
                    if (boardX < 0 || boardX >= 10 || boardY >= 20 ||
                        (boardY >= 0 && board[boardY][boardX] != Color.Black)) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun placePiece() {
        currentPiece?.let { piece ->
            val newBoard = Array(20) { y -> Array(10) { x -> board[y][x] } }
            for (y in piece.shape.indices) {
                for (x in piece.shape[y].indices) {
                    if (piece.shape[y][x]) {
                        val boardX = position.first + x
                        val boardY = position.second + y
                        if (boardY in 0 until 20 && boardX in 0 until 10) {
                            newBoard[boardY][boardX] = piece.color
                        }
                    }
                }
            }
            _board.value = newBoard
        }
    }

    private fun clearLines(): Int {
        var linesCleared = 0
        val newBoard = Array(20) { Array(10) { Color.Black } }
        var newRow = 19

        for (y in 19 downTo 0) {
            if (board[y].any { it == Color.Black }) {
                board[y].copyInto(newBoard[newRow])
                newRow--
            } else {
                linesCleared++
            }
        }

        if (linesCleared > 0) {
            _board.value = newBoard
        }
        return linesCleared
    }

    private fun updateScore(linesCleared: Int) {
        val points = when (linesCleared) {
            1 -> 100
            2 -> 300
            3 -> 500
            4 -> 800
            else -> 0
        }
        _score.value += points
        _level.value = (_score.value / 1000) + 1
        gameSpeed = maxOf(100L, 500L - (_level.value - 1) * 50L)
    }
}

data class Tetromino(val shape: List<List<Boolean>>, val color: Color) {
    fun rotate(): Tetromino {
        val newShape = shape[0].indices.map { col ->
            shape.indices.map { row ->
                shape[row][col]
            }.reversed()
        }
        return copy(shape = newShape)
    }

    companion object {
        private val TETROMINOES = listOf(
            // I piece
            listOf(
                listOf(true, true, true, true)
            ) to Color(0xFF00F0F0),
            // O piece
            listOf(
                listOf(true, true),
                listOf(true, true)
            ) to Color(0xFFF0F000),
            // T piece
            listOf(
                listOf(false, true, false),
                listOf(true, true, true)
            ) to Color(0xFFA000F0),
            // L piece
            listOf(
                listOf(false, false, true),
                listOf(true, true, true)
            ) to Color(0xFFF0A000),
            // J piece
            listOf(
                listOf(true, false, false),
                listOf(true, true, true)
            ) to Color(0xFF0000F0),
            // S piece
            listOf(
                listOf(false, true, true),
                listOf(true, true, false)
            ) to Color(0xFF00F000),
            // Z piece
            listOf(
                listOf(true, true, false),
                listOf(false, true, true)
            ) to Color(0xFFF00000)
        )

        fun random(): Tetromino {
            val (shape, color) = TETROMINOES.random()
            return Tetromino(shape, color)
        }
    }
}

