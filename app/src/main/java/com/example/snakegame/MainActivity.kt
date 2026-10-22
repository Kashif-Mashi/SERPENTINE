package com.example.snakegame

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

// ==========================================
// 0. APP BRANDING (change the name here)
// ==========================================

// Also change app_name in res/values/strings.xml so the launcher label matches.
const val APP_NAME = "Serpentine"
const val APP_TAGLINE = "Grow. Evolve. Dominate."

// ==========================================
// 1. DATA MODELS & ENUMS
// ==========================================

data class Point(val x: Int, val y: Int)

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    fun isOpposite(other: Direction): Boolean =
        (dx + other.dx == 0 && dy + other.dy == 0)
}

enum class GameState {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER
}

enum class FoodType(val points: Int, val durationMs: Long = 0L) {
    NORMAL(points = 1),
    GOLDEN(points = 5, durationMs = 8500L),
    SPEED_BOOST(points = 2, durationMs = 6000L)
}

data class Food(
    val position: Point,
    val type: FoodType,
    val spawnTime: Long = System.currentTimeMillis()
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var alpha: Float = 1f,
    val radius: Float = 5f,
    var life: Int = 20
)

data class FloatingText(
    val text: String,
    val gridX: Int,
    val gridY: Int,
    val color: Color,
    var offsetY: Float = 0f,
    var alpha: Float = 1f,
    var life: Int = 50
)

enum class Difficulty(
    val displayName: String,
    val baseDelayMs: Long,
    val minDelayMs: Long,
    val speedStepMs: Long,
    val obstacleInterval: Int
) {
    EASY("Chill", 210L, 110L, 3L, 12),
    MEDIUM("Standard", 160L, 75L, 4L, 7),
    HARD("Insane", 115L, 45L, 5L, 4)
}

enum class GameTheme(
    val themeName: String,
    val background: Color,
    val gridLine: Color,
    val snakeHead: Color,
    val snakeBody: Color,
    val normalFood: Color,
    val goldenFood: Color,
    val speedFood: Color,
    val obstacle: Color,
    val obstacleGlow: Color,
    val primaryUi: Color,
    val surfaceUi: Color,
    val textPrimary: Color
) {
    CYBERPUNK(
        themeName = "Cyberpunk",
        background = Color(0xFF0A0717),
        gridLine = Color(0x1800FFFF),
        snakeHead = Color(0xFFFF007F),
        snakeBody = Color(0xFF00F0FF),
        normalFood = Color(0xFFFF2A6D),
        goldenFood = Color(0xFFFFD700),
        speedFood = Color(0xFF05FFA1),
        obstacle = Color(0xFF7928CA),
        obstacleGlow = Color(0x4D7928CA),
        primaryUi = Color(0xFF00F0FF),
        surfaceUi = Color(0xFF140E2D),
        textPrimary = Color(0xFFE2F3F5)
    ),
    RETRO_GREEN(
        themeName = "Retro LCD",
        background = Color(0xFF8B956D),
        gridLine = Color(0x1F222B1E),
        snakeHead = Color(0xFF1F2417),
        snakeBody = Color(0xFF38402A),
        normalFood = Color(0xFF1F2417),
        goldenFood = Color(0xFF4C5B32),
        speedFood = Color(0xFF38402A),
        obstacle = Color(0xFF15190F),
        obstacleGlow = Color(0x3315190F),
        primaryUi = Color(0xFF1F2417),
        surfaceUi = Color(0xFF7E8A60),
        textPrimary = Color(0xFF1F2417)
    ),
    NEON_SUNSET(
        themeName = "Neon Sunset",
        background = Color(0xFF160628),
        gridLine = Color(0x20FF5E7E),
        snakeHead = Color(0xFFFF3366),
        snakeBody = Color(0xFFFF9900),
        normalFood = Color(0xFFFF0055),
        goldenFood = Color(0xFFFFEE55),
        speedFood = Color(0xFF00F5D4),
        obstacle = Color(0xFF9B5DE5),
        obstacleGlow = Color(0x4D9B5DE5),
        primaryUi = Color(0xFFFF5E7E),
        surfaceUi = Color(0xFF280B45),
        textPrimary = Color(0xFFFFF0F5)
    ),
    MATRIX(
        themeName = "Matrix",
        background = Color(0xFF040C06),
        gridLine = Color(0x1600FF66),
        snakeHead = Color(0xFF00FF66),
        snakeBody = Color(0xFF009933),
        normalFood = Color(0xFF66FF99),
        goldenFood = Color(0xFFFFFF33),
        speedFood = Color(0xFF00E5FF),
        obstacle = Color(0xFF004D1A),
        obstacleGlow = Color(0x3300FF66),
        primaryUi = Color(0xFF00FF66),
        surfaceUi = Color(0xFF0A1C0E),
        textPrimary = Color(0xFFD4FFDD)
    )
}

// ==========================================
// 2. MAIN ACTIVITY
// ==========================================

class MainActivity : ComponentActivity() {
    private var isForeground by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnakeGameApp(isForeground = isForeground)
        }
    }

    override fun onResume() {
        super.onResume()
        isForeground = true
    }

    override fun onPause() {
        super.onPause()
        isForeground = false
    }
}

// ==========================================
// 3. CORE GAME COMPOSABLE
// ==========================================

@Composable
fun SnakeGameApp(isForeground: Boolean) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("snake_game_prefs", Context.MODE_PRIVATE) }
    val textMeasurer = rememberTextMeasurer()
    val music = remember { MusicPlayer() }

    // Grid Dimensions
    val gridWidth = 20
    val gridHeight = 24

    // Game Core State
    var gameState by remember { mutableStateOf(GameState.MENU) }
    var currentTheme by remember { mutableStateOf(GameTheme.CYBERPUNK) }
    var currentDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var wrapAroundWalls by remember { mutableStateOf(false) }

    // UI Flags
    var showSplash by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }

    // Persistent Settings
    var vibrationEnabled by remember { mutableStateOf(prefs.getBoolean("vibration", true)) }
    var musicEnabled by remember { mutableStateOf(prefs.getBoolean("music_on", true)) }
    var musicVolume by remember { mutableFloatStateOf(prefs.getFloat("music_volume", 0.5f)) }

    // Score Tracking
    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(prefs.getInt("high_score", 0)) }

    // Snake State
    var snake by remember {
        mutableStateOf(
            listOf(
                Point(10, 12),
                Point(9, 12),
                Point(8, 12)
            )
        )
    }
    var currentDirection by remember { mutableStateOf(Direction.RIGHT) }
    val directionQueue = remember { mutableStateListOf<Direction>() }

    // Board Entities
    var currentFood by remember {
        mutableStateOf(Food(Point(15, 12), FoodType.NORMAL))
    }
    var obstacles by remember { mutableStateOf(setOf<Point>()) }

    // Buffs & Effects
    var speedBoostEndTime by remember { mutableLongStateOf(0L) }
    val particles = remember { mutableStateListOf<Particle>() }
    val floatingTexts = remember { mutableStateListOf<FloatingText>() }
    var frameTick by remember { mutableLongStateOf(0L) }

    // Animation Ticks
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Helper: Haptics (respects the vibration setting)
    fun triggerHaptic(type: HapticType) {
        if (vibrationEnabled) performHaptic(context, type)
    }

    // Helper: Input Buffering & Validation
    fun queueDirection(newDir: Direction) {
        if (gameState != GameState.PLAYING) return
        val lastDir = directionQueue.lastOrNull() ?: currentDirection
        if (!newDir.isOpposite(lastDir) && newDir != lastDir) {
            if (directionQueue.size < 2) {
                directionQueue.add(newDir)
                triggerHaptic(HapticType.LIGHT)
            }
        }
    }

    // Helper: Spawn Safe Food
    fun generateFood(snakeBody: List<Point>, wallObstacles: Set<Point>): Food {
        val occupied = snakeBody.toSet() + wallObstacles
        val emptySlots = mutableListOf<Point>()
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val p = Point(x, y)
                if (p !in occupied) emptySlots.add(p)
            }
        }
        val targetPoint = if (emptySlots.isNotEmpty()) {
            emptySlots.random()
        } else {
            Point(0, 0)
        }

        val roll = Random.nextFloat()
        val type = when {
            roll < 0.15f -> FoodType.GOLDEN
            roll < 0.30f -> FoodType.SPEED_BOOST
            else -> FoodType.NORMAL
        }
        return Food(targetPoint, type)
    }

    // Helper: Spawn Dynamic Obstacles on Score Thresholds
    fun maybeSpawnObstacle(currentObstacles: Set<Point>, snakeBody: List<Point>, food: Food): Set<Point> {
        val occupied = snakeBody.toSet() + currentObstacles + food.position
        val head = snakeBody.first()
        val safeZone = mutableSetOf<Point>()
        for (dx in -2..2) {
            for (dy in -2..2) {
                safeZone.add(Point(head.x + dx, head.y + dy))
            }
        }

        val candidates = mutableListOf<Point>()
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val p = Point(x, y)
                if (p !in occupied && p !in safeZone) {
                    candidates.add(p)
                }
            }
        }
        return if (candidates.isNotEmpty()) {
            currentObstacles + candidates.random()
        } else {
            currentObstacles
        }
    }

    // Helper: Spawn Explosion Particles
    fun spawnParticles(gx: Int, gy: Int, color: Color, count: Int = 16) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val speed = Random.nextFloat() * 6f + 2f
            particles.add(
                Particle(
                    x = gx.toFloat(),
                    y = gy.toFloat(),
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    alpha = 1f,
                    radius = Random.nextFloat() * 3f + 3f,
                    life = Random.nextInt(24, 42)
                )
            )
        }
    }

    // Reset Game State
    fun resetGame() {
        val startHead = Point(gridWidth / 2, gridHeight / 2)
        snake = listOf(
            startHead,
            Point(startHead.x - 1, startHead.y),
            Point(startHead.x - 2, startHead.y)
        )
        currentDirection = Direction.RIGHT
        directionQueue.clear()
        obstacles = emptySet()
        particles.clear()
        floatingTexts.clear()
        score = 0
        speedBoostEndTime = 0L
        currentFood = generateFood(snake, obstacles)
        gameState = GameState.PLAYING
        triggerHaptic(HapticType.MEDIUM)
    }

    // ------------------------------------------
    // Splash progress (real work: music synthesis runs while the bar fills)
    // ------------------------------------------
    val splashProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        val prepJob = launch(Dispatchers.Default) { music.prepare() }
        splashProgress.animateTo(0.7f, tween(900, easing = LinearEasing))
        prepJob.join()
        splashProgress.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        delay(150)
        showSplash = false
    }

    // ------------------------------------------
    // Music control
    // ------------------------------------------
    val shouldPlayMusic = musicEnabled && isForeground && !showSplash && gameState != GameState.PAUSED
    LaunchedEffect(shouldPlayMusic) {
        if (shouldPlayMusic) music.start() else music.pause()
    }
    LaunchedEffect(musicVolume) {
        music.setVolume(musicVolume)
    }
    DisposableEffect(Unit) {
        onDispose { music.release() }
    }

    // Auto-pause when the app goes to the background
    LaunchedEffect(isForeground) {
        if (!isForeground && gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED
        }
    }

    // Back button behaviour
    BackHandler(enabled = !showSplash && (showSettings || gameState != GameState.MENU)) {
        when {
            showSettings -> showSettings = false
            gameState == GameState.PLAYING -> gameState = GameState.PAUSED
            else -> gameState = GameState.MENU
        }
    }

    // Frame-based particle + floating text animation (smooth at 60fps, independent of snake speed)
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { }
            if (particles.isNotEmpty() || floatingTexts.isNotEmpty()) {
                val particleIterator = particles.iterator()
                while (particleIterator.hasNext()) {
                    val p = particleIterator.next()
                    p.x += p.vx * 0.0085f
                    p.y += p.vy * 0.0085f
                    p.alpha *= 0.94f
                    p.life--
                    if (p.life <= 0 || p.alpha < 0.05f) {
                        particleIterator.remove()
                    }
                }

                val textIterator = floatingTexts.iterator()
                while (textIterator.hasNext()) {
                    val ft = textIterator.next()
                    ft.offsetY -= 0.025f
                    ft.alpha *= 0.94f
                    ft.life--
                    if (ft.life <= 0 || ft.alpha < 0.05f) {
                        textIterator.remove()
                    }
                }
                frameTick++
            }
        }
    }

    // Coroutine Game Loop Execution
    LaunchedEffect(gameState, currentDifficulty, speedBoostEndTime) {
        while (isActive && gameState == GameState.PLAYING) {
            val now = System.currentTimeMillis()
            val isBoosted = now < speedBoostEndTime

            // Calculate current tick delay
            val speedDeduction = (score * currentDifficulty.speedStepMs)
            val computedDelay = (currentDifficulty.baseDelayMs - speedDeduction)
                .coerceAtLeast(currentDifficulty.minDelayMs)
            val effectiveDelay = if (isBoosted) (computedDelay * 0.72f).toLong() else computedDelay

            delay(effectiveDelay)

            // Step 1: Direction from Queue
            if (directionQueue.isNotEmpty()) {
                currentDirection = directionQueue.removeAt(0)
            }

            // Step 2: Next Head Position
            var nextX = snake.first().x + currentDirection.dx
            var nextY = snake.first().y + currentDirection.dy

            if (wrapAroundWalls) {
                if (nextX < 0) nextX = gridWidth - 1
                if (nextX >= gridWidth) nextX = 0
                if (nextY < 0) nextY = gridHeight - 1
                if (nextY >= gridHeight) nextY = 0
            } else {
                // Wall Collision
                if (nextX !in 0 until gridWidth || nextY !in 0 until gridHeight) {
                    gameState = GameState.GAME_OVER
                    triggerHaptic(HapticType.HEAVY)
                    continue
                }
            }

            val nextHead = Point(nextX, nextY)

            // Step 3: Self or Obstacle Collision Check
            if (nextHead in snake.dropLast(1) || nextHead in obstacles) {
                gameState = GameState.GAME_OVER
                triggerHaptic(HapticType.HEAVY)
                continue
            }

            // Step 4: Check Food Expiry (Golden Food or Speed Boost)
            if (currentFood.type.durationMs > 0L) {
                if (now - currentFood.spawnTime > currentFood.type.durationMs) {
                    currentFood = generateFood(snake, obstacles)
                }
            }

            // Step 5: Eat Food or Advance
            val hasEaten = (nextHead == currentFood.position)
            val newSnake = mutableListOf(nextHead)
            if (hasEaten) {
                newSnake.addAll(snake) // grow by retaining tail

                val pointsEarned = currentFood.type.points
                score += pointsEarned
                if (score > highScore) {
                    highScore = score
                    prefs.edit().putInt("high_score", highScore).apply()
                }

                val foodColor = when (currentFood.type) {
                    FoodType.NORMAL -> currentTheme.normalFood
                    FoodType.GOLDEN -> currentTheme.goldenFood
                    FoodType.SPEED_BOOST -> currentTheme.speedFood
                }

                // Add Particle & Text Effects
                spawnParticles(currentFood.position.x, currentFood.position.y, foodColor)
                floatingTexts.add(
                    FloatingText(
                        text = "+$pointsEarned",
                        gridX = currentFood.position.x,
                        gridY = currentFood.position.y,
                        color = foodColor
                    )
                )

                // Apply Food Buffs
                when (currentFood.type) {
                    FoodType.NORMAL -> triggerHaptic(HapticType.LIGHT)
                    FoodType.GOLDEN -> triggerHaptic(HapticType.DOUBLE)
                    FoodType.SPEED_BOOST -> {
                        speedBoostEndTime = now + currentFood.type.durationMs
                        triggerHaptic(HapticType.MEDIUM)
                    }
                }

                // Maybe Spawn Obstacle
                if (score % currentDifficulty.obstacleInterval == 0) {
                    obstacles = maybeSpawnObstacle(obstacles, newSnake, currentFood)
                }

                // Spawn New Food
                currentFood = generateFood(newSnake, obstacles)
            } else {
                newSnake.addAll(snake.dropLast(1)) // move forward
            }
            snake = newSnake
        }
    }

    // Root Container
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = currentTheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header / Score Bar
                GameTopBar(
                    score = score,
                    highScore = highScore,
                    currentTheme = currentTheme,
                    isSpeedBoosted = System.currentTimeMillis() < speedBoostEndTime,
                    gameState = gameState,
                    onPauseToggle = {
                        if (gameState == GameState.PLAYING) {
                            gameState = GameState.PAUSED
                        } else if (gameState == GameState.PAUSED) {
                            gameState = GameState.PLAYING
                        }
                    },
                    onSettingsClick = {
                        if (gameState == GameState.PLAYING) gameState = GameState.PAUSED
                        showSettings = true
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Interactive 2D Canvas Playfield with Gesture Detection
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    var accumulatedDragX by remember { mutableFloatStateOf(0f) }
                    var accumulatedDragY by remember { mutableFloatStateOf(0f) }

                    Box(
                        modifier = Modifier
                            .aspectRatio(gridWidth.toFloat() / gridHeight.toFloat())
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        currentTheme.primaryUi.copy(alpha = 0.8f),
                                        currentTheme.primaryUi.copy(alpha = 0.2f)
                                    )
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .pointerInput(gameState) {
                                if (gameState != GameState.PLAYING) return@pointerInput
                                detectDragGestures(
                                    onDragStart = {
                                        accumulatedDragX = 0f
                                        accumulatedDragY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        accumulatedDragX += dragAmount.x
                                        accumulatedDragY += dragAmount.y
                                        val swipeThreshold = 28f
                                        if (abs(accumulatedDragX) > swipeThreshold || abs(accumulatedDragY) > swipeThreshold) {
                                            if (abs(accumulatedDragX) > abs(accumulatedDragY)) {
                                                if (accumulatedDragX > 0) queueDirection(Direction.RIGHT)
                                                else queueDirection(Direction.LEFT)
                                            } else {
                                                if (accumulatedDragY > 0) queueDirection(Direction.DOWN)
                                                else queueDirection(Direction.UP)
                                            }
                                            accumulatedDragX = 0f
                                            accumulatedDragY = 0f
                                        }
                                    }
                                )
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Reading frameTick here makes the canvas redraw every frame while effects are alive
                            if (frameTick < 0L) return@Canvas

                            val cellWidth = size.width / gridWidth
                            val cellHeight = size.height / gridHeight

                            // 1. Draw Grid Lines
                            for (x in 0..gridWidth) {
                                drawLine(
                                    color = currentTheme.gridLine,
                                    start = Offset(x * cellWidth, 0f),
                                    end = Offset(x * cellWidth, size.height),
                                    strokeWidth = 1f
                                )
                            }
                            for (y in 0..gridHeight) {
                                drawLine(
                                    color = currentTheme.gridLine,
                                    start = Offset(0f, y * cellHeight),
                                    end = Offset(size.width, y * cellHeight),
                                    strokeWidth = 1f
                                )
                            }

                            // 2. Draw Obstacles
                            for (obs in obstacles) {
                                val left = obs.x * cellWidth
                                val top = obs.y * cellHeight

                                // Outer Glow
                                drawRoundRect(
                                    color = currentTheme.obstacleGlow,
                                    topLeft = Offset(left - 2f, top - 2f),
                                    size = Size(cellWidth + 4f, cellHeight + 4f),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                                // Base Wall Block
                                drawRoundRect(
                                    color = currentTheme.obstacle,
                                    topLeft = Offset(left + 2f, top + 2f),
                                    size = Size(cellWidth - 4f, cellHeight - 4f),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )
                                // Hazard Center Core
                                drawCircle(
                                    color = currentTheme.background.copy(alpha = 0.7f),
                                    radius = cellWidth * 0.18f,
                                    center = Offset(left + cellWidth / 2, top + cellHeight / 2)
                                )
                            }

                            // 3. Draw Food with Layered Glow & Polish
                            val foodCenterX = currentFood.position.x * cellWidth + cellWidth / 2
                            val foodCenterY = currentFood.position.y * cellHeight + cellHeight / 2
                            val baseRadius = (cellWidth.coerceAtMost(cellHeight) / 2) - 3f

                            when (currentFood.type) {
                                FoodType.NORMAL -> {
                                    // Glow Ring
                                    drawCircle(
                                        color = currentTheme.normalFood.copy(alpha = 0.25f),
                                        radius = baseRadius * 1.5f,
                                        center = Offset(foodCenterX, foodCenterY)
                                    )
                                    // Solid Orb
                                    drawCircle(
                                        color = currentTheme.normalFood,
                                        radius = baseRadius,
                                        center = Offset(foodCenterX, foodCenterY)
                                    )
                                    // Specular Highlight
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.75f),
                                        radius = baseRadius * 0.32f,
                                        center = Offset(foodCenterX - baseRadius * 0.35f, foodCenterY - baseRadius * 0.35f)
                                    )
                                }
                                FoodType.GOLDEN -> {
                                    val pulseR = baseRadius * pulseScale
                                    // Outer Radiant Halo
                                    drawCircle(
                                        color = currentTheme.goldenFood.copy(alpha = 0.35f),
                                        radius = pulseR * 1.6f,
                                        center = Offset(foodCenterX, foodCenterY)
                                    )
                                    // Golden Core
                                    drawCircle(
                                        color = currentTheme.goldenFood,
                                        radius = pulseR,
                                        center = Offset(foodCenterX, foodCenterY)
                                    )
                                    // Inner Spark
                                    drawCircle(
                                        color = Color.White,
                                        radius = pulseR * 0.4f,
                                        center = Offset(foodCenterX, foodCenterY)
                                    )
                                }
                                FoodType.SPEED_BOOST -> {
                                    // Diamond Shape with Energy Glow
                                    val diamondPath = Path().apply {
                                        moveTo(foodCenterX, foodCenterY - baseRadius * 1.2f)
                                        lineTo(foodCenterX + baseRadius * 1.2f, foodCenterY)
                                        lineTo(foodCenterX, foodCenterY + baseRadius * 1.2f)
                                        lineTo(foodCenterX - baseRadius * 1.2f, foodCenterY)
                                        close()
                                    }
                                    drawPath(
                                        path = diamondPath,
                                        color = currentTheme.speedFood.copy(alpha = 0.3f),
                                        style = Stroke(width = 4f)
                                    )
                                    drawPath(
                                        path = diamondPath,
                                        color = currentTheme.speedFood,
                                        style = Fill
                                    )
                                    // Core Bright Point
                                    drawCircle(
                                        color = Color.White,
                                        radius = baseRadius * 0.35f,
                                        center = Offset(foodCenterX, foodCenterY)
                                    )
                                }
                            }

                            // 4. Draw Snake Body & Head
                            snake.forEachIndexed { index, segment ->
                                val segLeft = segment.x * cellWidth
                                val segTop = segment.y * cellHeight

                                if (index == 0) {
                                    // Soft head glow (skipped on the light Retro LCD theme)
                                    if (currentTheme != GameTheme.RETRO_GREEN) {
                                        drawCircle(
                                            color = currentTheme.snakeHead.copy(alpha = 0.22f),
                                            radius = cellWidth * 0.95f,
                                            center = Offset(segLeft + cellWidth / 2, segTop + cellHeight / 2)
                                        )
                                    }

                                    // Snake Head
                                    drawRoundRect(
                                        color = currentTheme.snakeHead,
                                        topLeft = Offset(segLeft + 1f, segTop + 1f),
                                        size = Size(cellWidth - 2f, cellHeight - 2f),
                                        cornerRadius = CornerRadius(10f, 10f)
                                    )

                                    // Eyes with Directional Placement
                                    val eyeRadius = cellWidth * 0.14f
                                    val pupilRadius = eyeRadius * 0.55f
                                    val (eye1Offset, eye2Offset) = when (currentDirection) {
                                        Direction.UP -> Pair(
                                            Offset(segLeft + cellWidth * 0.3f, segTop + cellHeight * 0.3f),
                                            Offset(segLeft + cellWidth * 0.7f, segTop + cellHeight * 0.3f)
                                        )
                                        Direction.DOWN -> Pair(
                                            Offset(segLeft + cellWidth * 0.3f, segTop + cellHeight * 0.7f),
                                            Offset(segLeft + cellWidth * 0.7f, segTop + cellHeight * 0.7f)
                                        )
                                        Direction.LEFT -> Pair(
                                            Offset(segLeft + cellWidth * 0.3f, segTop + cellHeight * 0.3f),
                                            Offset(segLeft + cellWidth * 0.3f, segTop + cellHeight * 0.7f)
                                        )
                                        Direction.RIGHT -> Pair(
                                            Offset(segLeft + cellWidth * 0.7f, segTop + cellHeight * 0.3f),
                                            Offset(segLeft + cellWidth * 0.7f, segTop + cellHeight * 0.7f)
                                        )
                                    }

                                    // Draw Eyes
                                    drawCircle(color = Color.White, radius = eyeRadius, center = eye1Offset)
                                    drawCircle(color = Color.White, radius = eyeRadius, center = eye2Offset)
                                    drawCircle(color = Color.Black, radius = pupilRadius, center = eye1Offset)
                                    drawCircle(color = Color.Black, radius = pupilRadius, center = eye2Offset)
                                } else {
                                    // Snake Body Segments (Tapered Alpha & Gradient)
                                    val progress = 1f - (index.toFloat() / (snake.size + 2f))
                                    val bodyColor = currentTheme.snakeBody.copy(alpha = 0.65f + 0.35f * progress)
                                    drawRoundRect(
                                        color = bodyColor,
                                        topLeft = Offset(segLeft + 2f, segTop + 2f),
                                        size = Size(cellWidth - 4f, cellHeight - 4f),
                                        cornerRadius = CornerRadius(8f, 8f)
                                    )
                                }
                            }

                            // 5. Draw Particles
                            for (p in particles) {
                                drawCircle(
                                    color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                                    radius = p.radius,
                                    center = Offset(p.x * cellWidth + cellWidth / 2, p.y * cellHeight + cellHeight / 2)
                                )
                            }

                            // 6. Draw Floating Score Text
                            for (ft in floatingTexts) {
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = ft.text,
                                    topLeft = Offset(
                                        x = ft.gridX * cellWidth + cellWidth * 0.15f,
                                        y = (ft.gridY + ft.offsetY) * cellHeight
                                    ),
                                    style = TextStyle(
                                        color = ft.color.copy(alpha = ft.alpha.coerceIn(0f, 1f)),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tactile Virtual D-Pad Controller
                VirtualDPad(
                    theme = currentTheme,
                    enabled = gameState == GameState.PLAYING,
                    onDirectionClick = { dir -> queueDirection(dir) }
                )
            }

            // OVERLAYS: Menu, Pause, Game Over, Settings, Splash
            AnimatedVisibility(
                visible = gameState == GameState.MENU,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut() + scaleOut(targetScale = 0.9f)
            ) {
                MenuOverlay(
                    theme = currentTheme,
                    highScore = highScore,
                    selectedDifficulty = currentDifficulty,
                    selectedTheme = currentTheme,
                    wrapAroundWalls = wrapAroundWalls,
                    onDifficultySelected = { currentDifficulty = it },
                    onThemeSelected = { currentTheme = it },
                    onWrapToggle = { wrapAroundWalls = it },
                    onOpenSettings = { showSettings = true },
                    onStartGame = { resetGame() }
                )
            }

            AnimatedVisibility(
                visible = gameState == GameState.PAUSED,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                PauseOverlay(
                    theme = currentTheme,
                    onResume = { gameState = GameState.PLAYING },
                    onRestart = { resetGame() },
                    onSettings = { showSettings = true },
                    onMenu = { gameState = GameState.MENU }
                )
            }

            AnimatedVisibility(
                visible = gameState == GameState.GAME_OVER,
                enter = fadeIn() + scaleIn(initialScale = 0.85f),
                exit = fadeOut() + scaleOut(targetScale = 0.85f)
            ) {
                GameOverOverlay(
                    theme = currentTheme,
                    finalScore = score,
                    highScore = highScore,
                    onPlayAgain = { resetGame() },
                    onMenu = { gameState = GameState.MENU }
                )
            }

            AnimatedVisibility(
                visible = showSettings,
                enter = fadeIn() + scaleIn(initialScale = 0.92f),
                exit = fadeOut() + scaleOut(targetScale = 0.92f)
            ) {
                SettingsOverlay(
                    theme = currentTheme,
                    vibrationEnabled = vibrationEnabled,
                    musicEnabled = musicEnabled,
                    musicVolume = musicVolume,
                    onVibrationChange = {
                        vibrationEnabled = it
                        prefs.edit().putBoolean("vibration", it).apply()
                        if (it) performHaptic(context, HapticType.MEDIUM) // let the user feel it
                    },
                    onMusicChange = {
                        musicEnabled = it
                        prefs.edit().putBoolean("music_on", it).apply()
                    },
                    onVolumeChange = { musicVolume = it },
                    onVolumeChangeFinished = {
                        prefs.edit().putFloat("music_volume", musicVolume).apply()
                    },
                    onClose = { showSettings = false }
                )
            }

            AnimatedVisibility(
                visible = showSplash,
                enter = fadeIn(),
                exit = fadeOut(tween(350))
            ) {
                SplashOverlay(
                    theme = currentTheme,
                    progress = { splashProgress.value }
                )
            }
        }
    }
}

// ==========================================
// 4. TOP APP BAR / SCOREBOARD
// ==========================================

@Composable
fun GameTopBar(
    score: Int,
    highScore: Int,
    currentTheme: GameTheme,
    isSpeedBoosted: Boolean,
    gameState: GameState,
    onPauseToggle: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val shownScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(250),
        label = "scoreCount"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceUi.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Score & High Score
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SCORE",
                        color = currentTheme.primaryUi.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    if (isSpeedBoosted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⚡ BOOSTED",
                            color = currentTheme.speedFood,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Text(
                    text = "$shownScore",
                    color = currentTheme.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Center: Trophy High Score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🏆 BEST",
                    color = currentTheme.goldenFood,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$highScore",
                    color = currentTheme.goldenFood,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Right: Pause & Settings Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    onClick = onPauseToggle,
                    enabled = gameState == GameState.PLAYING || gameState == GameState.PAUSED,
                    modifier = Modifier.size(42.dp),
                    color = currentTheme.primaryUi.copy(alpha = 0.15f),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (gameState == GameState.PAUSED) "▶" else "❚❚",
                            color = currentTheme.primaryUi,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(42.dp),
                    color = currentTheme.primaryUi.copy(alpha = 0.15f),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "⚙",
                            color = currentTheme.primaryUi,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. ON-SCREEN VIRTUAL D-PAD
// ==========================================

@Composable
fun VirtualDPad(
    theme: GameTheme,
    enabled: Boolean,
    onDirectionClick: (Direction) -> Unit
) {
    val dpadSize = 146.dp
    val buttonSize = 44.dp

    Box(
        modifier = Modifier
            .size(dpadSize)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        // Decorative Center Ring
        Surface(
            modifier = Modifier.size(buttonSize),
            shape = CircleShape,
            color = theme.surfaceUi.copy(alpha = 0.5f),
            border = BorderStroke(1.5.dp, theme.primaryUi.copy(alpha = 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(theme.primaryUi.copy(alpha = 0.6f), CircleShape)
                )
            }
        }

        // UP Button
        DPadArrowButton(
            symbol = "▲",
            modifier = Modifier.align(Alignment.TopCenter),
            size = buttonSize,
            theme = theme,
            enabled = enabled
        ) {
            onDirectionClick(Direction.UP)
        }

        // DOWN Button
        DPadArrowButton(
            symbol = "▼",
            modifier = Modifier.align(Alignment.BottomCenter),
            size = buttonSize,
            theme = theme,
            enabled = enabled
        ) {
            onDirectionClick(Direction.DOWN)
        }

        // LEFT Button
        DPadArrowButton(
            symbol = "◄",
            modifier = Modifier.align(Alignment.CenterStart),
            size = buttonSize,
            theme = theme,
            enabled = enabled
        ) {
            onDirectionClick(Direction.LEFT)
        }

        // RIGHT Button
        DPadArrowButton(
            symbol = "►",
            modifier = Modifier.align(Alignment.CenterEnd),
            size = buttonSize,
            theme = theme,
            enabled = enabled
        ) {
            onDirectionClick(Direction.RIGHT)
        }
    }
}

@Composable
fun DPadArrowButton(
    symbol: String,
    modifier: Modifier,
    size: Dp,
    theme: GameTheme,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(size)
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = theme.surfaceUi,
        border = BorderStroke(1.5.dp, theme.primaryUi.copy(alpha = 0.6f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                color = if (enabled) theme.primaryUi else theme.primaryUi.copy(alpha = 0.3f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

// ==========================================
// 6. SPLASH SCREEN (with progress bar)
// ==========================================

@Composable
fun SplashOverlay(
    theme: GameTheme,
    progress: () -> Float
) {
    val p = progress().coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .pointerInput(Unit) { detectTapGestures { } }, // swallow touches while loading
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = APP_NAME.uppercase(),
                color = theme.primaryUi,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = APP_TAGLINE,
                color = theme.textPrimary.copy(alpha = 0.65f),
                fontSize = 13.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Progress bar drawn on Canvas (no extra Material API needed)
            Canvas(
                modifier = Modifier
                    .width(230.dp)
                    .height(8.dp)
            ) {
                val radius = CornerRadius(size.height / 2f, size.height / 2f)
                drawRoundRect(
                    color = theme.primaryUi.copy(alpha = 0.18f),
                    size = size,
                    cornerRadius = radius
                )
                if (p > 0f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            listOf(theme.primaryUi, theme.snakeHead)
                        ),
                        size = Size(size.width * p, size.height),
                        cornerRadius = radius
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Loading  ${(p * 100).toInt()}%",
                color = theme.textPrimary.copy(alpha = 0.55f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }
    }
}

// ==========================================
// 7. MENU OVERLAY
// ==========================================

@Composable
fun MenuOverlay(
    theme: GameTheme,
    highScore: Int,
    selectedDifficulty: Difficulty,
    selectedTheme: GameTheme,
    wrapAroundWalls: Boolean,
    onDifficultySelected: (Difficulty) -> Unit,
    onThemeSelected: (GameTheme) -> Unit,
    onWrapToggle: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onStartGame: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.86f))
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceUi),
            border = BorderStroke(2.dp, theme.primaryUi.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Game Title
                Text(
                    text = APP_NAME.uppercase(),
                    color = theme.primaryUi,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "High Score: $highScore",
                    color = theme.goldenFood,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Difficulty Selector
                Text(
                    text = "SELECT DIFFICULTY",
                    color = theme.textPrimary.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Difficulty.values().forEach { diff ->
                        val isSelected = diff == selectedDifficulty
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { onDifficultySelected(diff) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) theme.primaryUi else theme.surfaceUi.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) theme.primaryUi else theme.primaryUi.copy(alpha = 0.3f)
                            ),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text(
                                text = diff.displayName,
                                color = if (isSelected) theme.background else theme.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Theme Selector (2 x 2 grid with colour swatches)
                Text(
                    text = "VISUAL THEME",
                    color = theme.textPrimary.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                GameTheme.values().toList().chunked(2).forEach { rowThemes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowThemes.forEach { t ->
                            ThemeChip(
                                chipTheme = t,
                                isSelected = t == selectedTheme,
                                accent = theme.primaryUi,
                                modifier = Modifier.weight(1f),
                                onClick = { onThemeSelected(t) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Wrap-Around Walls Setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Portal Walls (Wrap Around)",
                            color = theme.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (wrapAroundWalls) "Snake passes through borders" else "Borders are fatal",
                            color = theme.textPrimary.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = wrapAroundWalls,
                        onCheckedChange = { onWrapToggle(it) },
                        colors = themedSwitchColors(theme)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Play Button
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    onClick = onStartGame,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryUi),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = "START MISSION",
                        color = theme.background,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenSettings
                ) {
                    Text(
                        text = "⚙  SETTINGS  (sound & vibration)",
                        color = theme.textPrimary.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeChip(
    chipTheme: GameTheme,
    isSelected: Boolean,
    accent: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = chipTheme.background,
        border = BorderStroke(
            width = if (isSelected) 2.5.dp else 1.dp,
            color = if (isSelected) accent else Color.Gray.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.size(12.dp).background(chipTheme.snakeHead, CircleShape))
            Box(Modifier.size(12.dp).background(chipTheme.snakeBody, CircleShape))
            Text(
                text = chipTheme.themeName,
                color = chipTheme.primaryUi,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun themedSwitchColors(theme: GameTheme): SwitchColors =
    SwitchDefaults.colors(
        checkedThumbColor = theme.background,
        checkedTrackColor = theme.primaryUi,
        uncheckedThumbColor = theme.primaryUi.copy(alpha = 0.5f),
        uncheckedTrackColor = theme.surfaceUi
    )

// ==========================================
// 8. SETTINGS OVERLAY
// ==========================================

@Composable
fun SettingsOverlay(
    theme: GameTheme,
    vibrationEnabled: Boolean,
    musicEnabled: Boolean,
    musicVolume: Float,
    onVibrationChange: (Boolean) -> Unit,
    onMusicChange: (Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onVolumeChangeFinished: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceUi),
            border = BorderStroke(2.dp, theme.primaryUi.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SETTINGS",
                    color = theme.primaryUi,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                SettingSwitchRow(
                    title = "Vibration",
                    subtitle = if (vibrationEnabled) "Haptics on eat and crash" else "Haptics off",
                    checked = vibrationEnabled,
                    theme = theme,
                    onCheckedChange = onVibrationChange
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingSwitchRow(
                    title = "Background music",
                    subtitle = if (musicEnabled) "Chiptune loop playing" else "Music off",
                    checked = musicEnabled,
                    theme = theme,
                    onCheckedChange = onMusicChange
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Music volume",
                        color = theme.textPrimary.copy(alpha = if (musicEnabled) 1f else 0.4f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${(musicVolume * 100).toInt()}%",
                        color = theme.textPrimary.copy(alpha = if (musicEnabled) 0.7f else 0.3f),
                        fontSize = 13.sp
                    )
                }
                Slider(
                    value = musicVolume,
                    onValueChange = onVolumeChange,
                    onValueChangeFinished = onVolumeChangeFinished,
                    valueRange = 0f..1f,
                    enabled = musicEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = theme.primaryUi,
                        activeTrackColor = theme.primaryUi,
                        inactiveTrackColor = theme.primaryUi.copy(alpha = 0.25f),
                        disabledThumbColor = theme.primaryUi.copy(alpha = 0.3f),
                        disabledActiveTrackColor = theme.primaryUi.copy(alpha = 0.2f),
                        disabledInactiveTrackColor = theme.primaryUi.copy(alpha = 0.1f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    onClick = onClose,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryUi)
                ) {
                    Text(
                        text = "DONE",
                        color = theme.background,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    theme: GameTheme,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = theme.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = theme.textPrimary.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = themedSwitchColors(theme)
        )
    }
}

// ==========================================
// 9. PAUSE OVERLAY
// ==========================================

@Composable
fun PauseOverlay(
    theme: GameTheme,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onSettings: () -> Unit,
    onMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(280.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceUi),
            border = BorderStroke(2.dp, theme.primaryUi.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "GAME PAUSED",
                    color = theme.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryUi),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("RESUME", color = theme.background, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRestart,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, theme.primaryUi)
                ) {
                    Text("RESTART", color = theme.primaryUi, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSettings,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, theme.primaryUi.copy(alpha = 0.5f))
                ) {
                    Text("SETTINGS", color = theme.textPrimary, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onMenu
                ) {
                    Text("MAIN MENU", color = theme.textPrimary.copy(alpha = 0.7f))
                }
            }
        }
    }
}

// ==========================================
// 10. GAME OVER OVERLAY
// ==========================================

@Composable
fun GameOverOverlay(
    theme: GameTheme,
    finalScore: Int,
    highScore: Int,
    onPlayAgain: () -> Unit,
    onMenu: () -> Unit
) {
    val isNewHighScore = finalScore >= highScore && finalScore > 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceUi),
            border = BorderStroke(2.dp, if (isNewHighScore) theme.goldenFood else theme.normalFood),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isNewHighScore) "RECORD SHATTERED!" else "MISSION FAILED",
                    color = if (isNewHighScore) theme.goldenFood else theme.normalFood,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "FINAL SCORE",
                    color = theme.textPrimary.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$finalScore",
                    color = theme.textPrimary,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )

                if (isNewHighScore) {
                    Text(
                        text = "★ NEW ALL-TIME BEST ★",
                        color = theme.goldenFood,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                } else {
                    Text(
                        text = "BEST: $highScore",
                        color = theme.goldenFood,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryUi),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "PLAY AGAIN",
                        color = theme.background,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onMenu,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, theme.textPrimary.copy(alpha = 0.4f))
                ) {
                    Text("MAIN MENU", color = theme.textPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 11. HAPTIC FEEDBACK SUBSYSTEM
// ==========================================

enum class HapticType {
    LIGHT,
    MEDIUM,
    DOUBLE,
    HEAVY
}

fun performHaptic(context: Context, type: HapticType) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (type) {
                HapticType.LIGHT -> {
                    vibrator.vibrate(VibrationEffect.createOneShot(18L, 60))
                }
                HapticType.MEDIUM -> {
                    vibrator.vibrate(VibrationEffect.createOneShot(35L, 140))
                }
                HapticType.DOUBLE -> {
                    val timings = longArrayOf(0, 25, 40, 30)
                    val amplitudes = intArrayOf(0, 160, 0, 200)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                }
                HapticType.HEAVY -> {
                    val timings = longArrayOf(0, 60, 50, 120)
                    val amplitudes = intArrayOf(0, 255, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                }
            }
        } else {
            @Suppress("DEPRECATION")
            when (type) {
                HapticType.LIGHT -> vibrator.vibrate(20L)
                HapticType.MEDIUM -> vibrator.vibrate(40L)
                HapticType.DOUBLE -> vibrator.vibrate(longArrayOf(0, 30, 40, 30), -1)
                HapticType.HEAVY -> vibrator.vibrate(180L)
            }
        }
    } catch (_: Exception) {
        // Silently catch in environments lacking vibrator hardware or permissions
    }
}

// ==========================================
// 12. BACKGROUND MUSIC (synthesised chiptune loop, no audio file needed)
// ==========================================

private const val SAMPLE_RATE = 22050

private fun midiToFreq(midi: Int): Double = 440.0 * 2.0.pow((midi - 69) / 12.0)

/**
 * Builds a seamless 4-bar loop (Am - F - C - G, 128 BPM): triangle bass, pulse-wave arpeggio, soft hi-hat.
 * Every note starts with a 4 ms attack and decays, so there are no clicks at step or loop boundaries.
 */
private fun synthLoop(): ShortArray {
    val stepFrames = (SAMPLE_RATE * 60.0 / 128.0 / 2.0).toInt() // one eighth note
    // [bass, arp1, arp2, arp3] as MIDI note numbers
    val chords = arrayOf(
        intArrayOf(45, 57, 60, 64), // Am
        intArrayOf(41, 53, 57, 60), // F
        intArrayOf(48, 60, 64, 67), // C
        intArrayOf(43, 55, 59, 62)  // G
    )
    val arpPattern = intArrayOf(1, 2, 3, 2, 1, 3, 2, 3)
    val totalSteps = chords.size * 8
    val out = ShortArray(totalSteps * stepFrames)
    val noise = Random(7)

    for (step in 0 until totalSteps) {
        val chord = chords[step / 8]
        val inBar = step % 8
        val arpMidi = chord[arpPattern[inBar]] + (if (inBar % 4 == 3) 12 else 0)
        val bassMidi = chord[0] + (if (inBar == 4) 12 else 0)
        val arpFreq = midiToFreq(arpMidi)
        val bassFreq = midiToFreq(bassMidi)
        val base = step * stepFrames

        for (i in 0 until stepFrames) {
            val t = i.toDouble() / SAMPLE_RATE
            val attack = (t / 0.004).coerceAtMost(1.0)
            var s = 0.0

            // Arpeggio: 25% duty pulse wave
            val arpPhase = (arpFreq * t) % 1.0
            s += (if (arpPhase < 0.25) 1.0 else -1.0) * 0.16 * attack * exp(-5.0 * i / stepFrames)

            // Bass: triangle wave on even steps
            if (inBar % 2 == 0) {
                val bassPhase = (bassFreq * t) % 1.0
                s += (4.0 * abs(bassPhase - 0.5) - 1.0) * 0.32 * attack * exp(-3.0 * i / stepFrames)
            }

            // Hi-hat: short noise burst on odd steps
            if (inBar % 2 == 1 && t < 0.035) {
                s += (noise.nextDouble() * 2.0 - 1.0) * 0.05 * (1.0 - t / 0.035)
            }

            out[base + i] = (s.coerceIn(-1.0, 1.0) * 32767.0 * 0.8).toInt().toShort()
        }
    }
    return out
}

class MusicPlayer {
    private var track: AudioTrack? = null
    private var volume: Float = 0.5f

    /** Builds the loop. Safe to call from a background thread; does nothing if already built. */
    @Synchronized
    fun prepare() {
        if (track != null) return
        try {
            val pcm = synthLoop()
            val t = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            t.write(pcm, 0, pcm.size)
            t.setLoopPoints(0, pcm.size, -1) // -1 = loop forever
            t.setVolume(volume)
            track = t
        } catch (_: Exception) {
            track = null
        }
    }

    @Synchronized
    fun start() {
        prepare()
        try {
            track?.play()
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun pause() {
        try {
            val t = track ?: return
            if (t.playState == AudioTrack.PLAYSTATE_PLAYING) t.pause()
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun setVolume(v: Float) {
        volume = v.coerceIn(0f, 1f)
        try {
            track?.setVolume(volume)
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun release() {
        try {
            track?.release()
        } catch (_: Exception) {
        }
        track = null
    }
}