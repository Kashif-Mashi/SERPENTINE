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
import androidx.core.content.edit
import kotlin.time.Duration.Companion.milliseconds
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
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
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

@Suppress("SpellCheckingInspection")
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
    val textPrimary: Color,
    val checkerLight: Color,
    val checkerDark: Color
) {
    MATRIX_NEON(
        themeName = "Matrix Neon",
        background = Color(0xFF071A12),
        gridLine = Color(0x2240DF88),
        snakeHead = Color(0xFF40DF88),
        snakeBody = Color(0xFF26B265),
        normalFood = Color(0xFFFF3355),
        goldenFood = Color(0xFFFFD129),
        speedFood = Color(0xFF00E5FF),
        obstacle = Color(0xFF5A6660),
        obstacleGlow = Color(0x3340DF88),
        primaryUi = Color(0xFF40DF88),
        surfaceUi = Color(0xFF0E271B),
        textPrimary = Color(0xFFD8F3DC),
        checkerLight = Color(0xFF1B4D36),
        checkerDark = Color(0xFF133C29)
    ),
    CYBERPUNK_PINK(
        themeName = "Cyberpunk Pink",
        background = Color(0xFF180A20),
        gridLine = Color(0x22FF2A7A),
        snakeHead = Color(0xFFFF2A8D),
        snakeBody = Color(0xFFCC1F70),
        normalFood = Color(0xFFFF3355),
        goldenFood = Color(0xFFFFD129),
        speedFood = Color(0xFF00F0FF),
        obstacle = Color(0xFF563566),
        obstacleGlow = Color(0x33FF2A8D),
        primaryUi = Color(0xFFFF2A8D),
        surfaceUi = Color(0xFF281236),
        textPrimary = Color(0xFFFFE0F0),
        checkerLight = Color(0xFF421550),
        checkerDark = Color(0xFF330E3F)
    ),
    RETRO_GAMEBOY(
        themeName = "Retro GameBoy",
        background = Color(0xFF8B956D),
        gridLine = Color(0x202B331F),
        snakeHead = Color(0xFF1F2417),
        snakeBody = Color(0xFF38402A),
        normalFood = Color(0xFF1F2417),
        goldenFood = Color(0xFF4C5B32),
        speedFood = Color(0xFF38402A),
        obstacle = Color(0xFF2E3622),
        obstacleGlow = Color(0x221F2417),
        primaryUi = Color(0xFF1F2417),
        surfaceUi = Color(0xFF7E8A60),
        textPrimary = Color(0xFF1F2417),
        checkerLight = Color(0xFF98A377),
        checkerDark = Color(0xFF8B956D)
    ),
    MIDNIGHT_BLUE(
        themeName = "Midnight Blue",
        background = Color(0xFF071426),
        gridLine = Color(0x2200D4FF),
        snakeHead = Color(0xFF00D4FF),
        snakeBody = Color(0xFF0090D9),
        normalFood = Color(0xFFFF3355),
        goldenFood = Color(0xFFFFD129),
        speedFood = Color(0xFF00FFA3),
        obstacle = Color(0xFF324B6B),
        obstacleGlow = Color(0x3300D4FF),
        primaryUi = Color(0xFF00D4FF),
        surfaceUi = Color(0xFF0F223D),
        textPrimary = Color(0xFFDDF5FF),
        checkerLight = Color(0xFF103358),
        checkerDark = Color(0xFF0B2644)
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
    var currentTheme by remember { mutableStateOf(GameTheme.MATRIX_NEON) }
    var currentDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var wrapAroundWalls by remember { mutableStateOf(false) }

    // UI Flags
    var showSplash by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }

    // Persistent Settings
    var vibrationEnabled by remember { mutableStateOf(prefs.getBoolean("vibration", true)) }
    var musicEnabled by remember { mutableStateOf(prefs.getBoolean("music_on", true)) }
    var musicVolume by remember { mutableFloatStateOf(prefs.getFloat("music_volume", 0.6f)) }
    var swipeSensitivity by remember { mutableFloatStateOf(prefs.getFloat("swipe_sensitivity", 0.65f)) }

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
        repeat(count) {
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
        delay(150.milliseconds)
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

    // Back button behavior
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

            delay(effectiveDelay.milliseconds)

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
                    prefs.edit { putInt("high_score", highScore) }
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
                    musicEnabled = musicEnabled,
                    onMusicToggle = {
                        musicEnabled = !musicEnabled
                        prefs.edit { putBoolean("music_on", musicEnabled) }
                    },
                    onPauseToggle = {
                        if (gameState == GameState.PLAYING) {
                            gameState = GameState.PAUSED
                        } else if (gameState == GameState.PAUSED) {
                            gameState = GameState.PLAYING
                        }
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
                            .clip(RoundedCornerShape(26.dp))
                            .border(
                                width = 5.dp,
                                color = currentTheme.surfaceUi,
                                shape = RoundedCornerShape(26.dp)
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
                                        val swipeThreshold = 52f - (swipeSensitivity.coerceIn(0.1f, 1f) * 36f)
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

                            // 1. Draw Two-Tone Checkerboard Grid
                            for (x in 0 until gridWidth) {
                                for (y in 0 until gridHeight) {
                                    val isEven = (x + y) % 2 == 0
                                    drawRect(
                                        color = if (isEven) currentTheme.checkerLight else currentTheme.checkerDark,
                                        topLeft = Offset(x * cellWidth, y * cellHeight),
                                        size = Size(cellWidth, cellHeight)
                                    )
                                }
                            }
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

                            // 2. Draw Obstacles: Smooth Rounded Grey Stones
                            for ((ox, oy) in obstacles) {
                                val stoneCenterX = ox * cellWidth + cellWidth / 2f
                                val stoneCenterY = oy * cellHeight + cellHeight / 2f
                                val stoneR = (cellWidth.coerceAtMost(cellHeight) / 2f) - 2f

                                // Soft drop shadow
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.28f),
                                    radius = stoneR * 0.95f,
                                    center = Offset(stoneCenterX + 2f, stoneCenterY + 3f)
                                )
                                // Smooth grey stone pebble base
                                drawRoundRect(
                                    color = Color(0xFF6B7F76),
                                    topLeft = Offset(stoneCenterX - stoneR, stoneCenterY - stoneR * 0.85f),
                                    size = Size(stoneR * 2f, stoneR * 1.7f),
                                    cornerRadius = CornerRadius(stoneR * 0.65f, stoneR * 0.65f)
                                )
                                // Top highlight reflection
                                drawRoundRect(
                                    color = Color(0xFF8CA598).copy(alpha = 0.75f),
                                    topLeft = Offset(stoneCenterX - stoneR * 0.65f, stoneCenterY - stoneR * 0.65f),
                                    size = Size(stoneR * 0.9f, stoneR * 0.55f),
                                    cornerRadius = CornerRadius(stoneR * 0.35f, stoneR * 0.35f)
                                )
                            }

                            // 3. Draw Food (Glossy Red Apple or Golden Apple with Sparkles)
                            val foodCenterX = currentFood.position.x * cellWidth + cellWidth / 2f
                            val foodCenterY = currentFood.position.y * cellHeight + cellHeight / 2f
                            val baseRadius = (cellWidth.coerceAtMost(cellHeight) / 2f) - 3f

                            when (currentFood.type) {
                                FoodType.NORMAL -> {
                                    // Soft drop shadow
                                    drawCircle(
                                        color = Color.Black.copy(alpha = 0.25f),
                                        radius = baseRadius * 0.9f,
                                        center = Offset(foodCenterX + 1.5f, foodCenterY + 2.5f)
                                    )
                                    // Red Apple Body
                                    drawCircle(
                                        color = currentTheme.normalFood,
                                        radius = baseRadius,
                                        center = Offset(foodCenterX, foodCenterY)
                                    )
                                    // Glossy Specular Highlight
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.72f),
                                        radius = baseRadius * 0.30f,
                                        center = Offset(foodCenterX - baseRadius * 0.35f, foodCenterY - baseRadius * 0.35f)
                                    )
                                    // Green Leaf & Stem
                                    val leafColor = Color(0xFF5BA829)
                                    drawArc(
                                        color = leafColor,
                                        startAngle = -45f,
                                        sweepAngle = 75f,
                                        useCenter = true,
                                        topLeft = Offset(foodCenterX + 1f, foodCenterY - baseRadius * 1.35f),
                                        size = Size(baseRadius * 0.75f, baseRadius * 0.6f)
                                    )
                                }
                                FoodType.GOLDEN -> {
                                    val pulseR = baseRadius * pulseScale
                                    // Tiny sparkles around golden apple
                                    val spColor = Color(0xFFFFE066)
                                    val spSize = 3f * pulseScale
                                    drawCircle(spColor, spSize, Offset(foodCenterX - pulseR * 1.3f, foodCenterY - pulseR * 0.6f))
                                    drawCircle(spColor, spSize * 1.2f, Offset(foodCenterX + pulseR * 1.35f, foodCenterY - pulseR * 0.5f))
                                    drawCircle(spColor, spSize * 0.8f, Offset(foodCenterX + pulseR * 1.1f, foodCenterY + pulseR * 0.8f))

                                    // Golden Body
                                    drawCircle(
                                        color = currentTheme.goldenFood,
                                        radius = pulseR,
                                        center = Offset(foodCenterX, foodCenterY)
                                    )
                                    // Highlight
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.85f),
                                        radius = pulseR * 0.32f,
                                        center = Offset(foodCenterX - pulseR * 0.35f, foodCenterY - pulseR * 0.35f)
                                    )
                                    // Golden Leaf
                                    drawArc(
                                        color = Color(0xFFD4AF37),
                                        startAngle = -45f,
                                        sweepAngle = 75f,
                                        useCenter = true,
                                        topLeft = Offset(foodCenterX + 1f, foodCenterY - pulseR * 1.35f),
                                        size = Size(pulseR * 0.75f, pulseR * 0.6f)
                                    )
                                }
                                FoodType.SPEED_BOOST -> {
                                    val diamondPath = Path().apply {
                                        moveTo(foodCenterX, foodCenterY - baseRadius * 1.2f)
                                        lineTo(foodCenterX + baseRadius * 1.2f, foodCenterY)
                                        lineTo(foodCenterX, foodCenterY + baseRadius * 1.2f)
                                        lineTo(foodCenterX - baseRadius * 1.2f, foodCenterY)
                                        close()
                                    }
                                    drawPath(diamondPath, color = currentTheme.speedFood.copy(alpha = 0.35f), style = Stroke(width = 4f))
                                    drawPath(diamondPath, color = currentTheme.speedFood, style = Fill)
                                    drawCircle(color = Color.White, radius = baseRadius * 0.35f, center = Offset(foodCenterX, foodCenterY))
                                }
                            }

                            // 4. Draw ONE Smooth Continuous Rounded Tube Snake
                            val tubeRadius = (cellWidth.coerceAtMost(cellHeight) / 2f) * 0.88f

                            // Continuous body segment links
                            for (i in 0 until snake.size - 1) {
                                val segA = snake[i]
                                val segB = snake[i + 1]
                                if (abs(segA.x - segB.x) <= 1 && abs(segA.y - segB.y) <= 1) {
                                    val pA = Offset(segA.x * cellWidth + cellWidth / 2f, segA.y * cellHeight + cellHeight / 2f)
                                    val pB = Offset(segB.x * cellWidth + cellWidth / 2f, segB.y * cellHeight + cellHeight / 2f)
                                    val progress = 1f - (i.toFloat() / snake.size)
                                    val segColor = currentTheme.snakeBody.copy(alpha = 0.85f + 0.15f * progress)
                                    drawLine(
                                        color = segColor,
                                        start = pA,
                                        end = pB,
                                        strokeWidth = tubeRadius * 2f,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }

                            // Segment discs (tail tapered, head detailed with eyes and smile)
                            snake.forEachIndexed { index, segment ->
                                val cx = segment.x * cellWidth + cellWidth / 2f
                                val cy = segment.y * cellHeight + cellHeight / 2f
                                val isTail = index == snake.size - 1
                                val r = if (isTail) tubeRadius * 0.72f else tubeRadius

                                if (index == 0) {
                                    if (currentTheme != GameTheme.RETRO_GAMEBOY) {
                                        drawCircle(
                                            color = currentTheme.snakeHead.copy(alpha = 0.22f),
                                            radius = tubeRadius * 1.4f,
                                            center = Offset(cx, cy)
                                        )
                                    }
                                    drawCircle(color = currentTheme.snakeHead, radius = tubeRadius, center = Offset(cx, cy))

                                    // Big Cartoon Eyes with Black Pupils
                                    val eyeRadius = tubeRadius * 0.34f
                                    val pupilRadius = eyeRadius * 0.52f
                                    val (eye1Offset, eye2Offset) = when (currentDirection) {
                                        Direction.UP -> Pair(
                                            Offset(cx - tubeRadius * 0.38f, cy - tubeRadius * 0.25f),
                                            Offset(cx + tubeRadius * 0.38f, cy - tubeRadius * 0.25f)
                                        )
                                        Direction.DOWN -> Pair(
                                            Offset(cx - tubeRadius * 0.38f, cy + tubeRadius * 0.25f),
                                            Offset(cx + tubeRadius * 0.38f, cy + tubeRadius * 0.25f)
                                        )
                                        Direction.LEFT -> Pair(
                                            Offset(cx - tubeRadius * 0.25f, cy - tubeRadius * 0.38f),
                                            Offset(cx - tubeRadius * 0.25f, cy + tubeRadius * 0.38f)
                                        )
                                        Direction.RIGHT -> Pair(
                                            Offset(cx + tubeRadius * 0.25f, cy - tubeRadius * 0.38f),
                                            Offset(cx + tubeRadius * 0.25f, cy + tubeRadius * 0.38f)
                                        )
                                    }

                                    drawCircle(color = Color.White, radius = eyeRadius, center = eye1Offset)
                                    drawCircle(color = Color.White, radius = eyeRadius, center = eye2Offset)
                                    drawCircle(color = Color.Black, radius = pupilRadius, center = eye1Offset)
                                    drawCircle(color = Color.Black, radius = pupilRadius, center = eye2Offset)

                                    // Friendly mouth curve
                                    val mouthC = Color.Black.copy(alpha = 0.6f)
                                    val mouthOffset = when (currentDirection) {
                                        Direction.RIGHT -> Offset(cx + tubeRadius * 0.55f, cy)
                                        Direction.LEFT -> Offset(cx - tubeRadius * 0.55f, cy)
                                        Direction.UP -> Offset(cx, cy - tubeRadius * 0.55f)
                                        Direction.DOWN -> Offset(cx, cy + tubeRadius * 0.55f)
                                    }
                                    drawCircle(color = mouthC, radius = 2.5f, center = mouthOffset)
                                } else {
                                    val progress = 1f - (index.toFloat() / snake.size)
                                    val bodyColor = currentTheme.snakeBody.copy(alpha = 0.85f + 0.15f * progress)
                                    drawCircle(color = bodyColor, radius = r, center = Offset(cx, cy))

                                    // Subtle glossy highlight
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.20f),
                                        radius = r * 0.35f,
                                        center = Offset(cx - r * 0.3f, cy - r * 0.3f)
                                    )
                                }
                            }

                            // 5. Draw Particles
                            for ((px, py, _, _, pColor, pAlpha, pRadius) in particles) {
                                drawCircle(
                                    color = pColor.copy(alpha = pAlpha.coerceIn(0f, 1f)),
                                    radius = pRadius,
                                    center = Offset(px * cellWidth + cellWidth / 2, py * cellHeight + cellHeight / 2)
                                )
                            }

                            // 6. Draw Floating Score Text
                            for ((fText, fGridX, fGridY, fColor, fOffsetY, fAlpha) in floatingTexts) {
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = fText,
                                    topLeft = Offset(
                                        x = fGridX * cellWidth + cellWidth * 0.15f,
                                        y = (fGridY + fOffsetY) * cellHeight
                                    ),
                                    style = TextStyle(
                                        color = fColor.copy(alpha = fAlpha.coerceIn(0f, 1f)),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Gesture Hint ("Swipe anywhere to steer") - No D-pad / buttons
                SwipeSteerHint(theme = currentTheme)
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
                    swipeSensitivity = swipeSensitivity,
                    onVibrationChange = {
                        vibrationEnabled = it
                        prefs.edit { putBoolean("vibration", it) }
                        if (it) performHaptic(context, HapticType.MEDIUM) // let the user feel it
                    },
                    onMusicChange = {
                        musicEnabled = it
                        prefs.edit { putBoolean("music_on", it) }
                    },
                    onVolumeChange = { musicVolume = it },
                    onVolumeChangeFinished = {
                        prefs.edit { putFloat("music_volume", musicVolume) }
                    },
                    onSwipeSensitivityChange = { swipeSensitivity = it },
                    onSwipeSensitivityChangeFinished = {
                        prefs.edit { putFloat("swipe_sensitivity", swipeSensitivity) }
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
    musicEnabled: Boolean,
    onMusicToggle: () -> Unit,
    onPauseToggle: () -> Unit
) {
    val shownScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(250),
        label = "scoreCount"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Apple Icon + Score
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Canvas(modifier = Modifier.size(28.dp)) {
                val r = size.width * 0.38f
                val cx = size.width * 0.48f
                val cy = size.height * 0.56f
                // Apple
                drawCircle(color = currentTheme.normalFood, radius = r, center = Offset(cx, cy))
                drawCircle(color = Color.White.copy(alpha = 0.7f), radius = r * 0.28f, center = Offset(cx - r * 0.35f, cy - r * 0.35f))
                // Stem / Leaf
                drawArc(
                    color = Color(0xFF5BA829),
                    startAngle = -45f,
                    sweepAngle = 75f,
                    useCenter = true,
                    topLeft = Offset(cx + 1f, cy - r * 1.35f),
                    size = Size(r * 0.8f, r * 0.65f)
                )
            }
            Text(
                text = "$shownScore",
                color = currentTheme.textPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
            if (isSpeedBoosted) {
                Text(
                    text = "⚡",
                    color = currentTheme.speedFood,
                    fontSize = 20.sp
                )
            }
        }

        // Center: Trophy Icon + High Score
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Canvas(modifier = Modifier.size(28.dp)) {
                val c = Color(0xFFFFD129)
                // Trophy Cup
                val cupPath = Path().apply {
                    moveTo(size.width * 0.25f, size.height * 0.22f)
                    lineTo(size.width * 0.75f, size.height * 0.22f)
                    lineTo(size.width * 0.70f, size.height * 0.58f)
                    cubicTo(
                        size.width * 0.65f, size.height * 0.72f,
                        size.width * 0.35f, size.height * 0.72f,
                        size.width * 0.30f, size.height * 0.58f
                    )
                    close()
                }
                drawPath(cupPath, c)
                // Stem
                drawRect(c, topLeft = Offset(size.width * 0.44f, size.height * 0.68f), size = Size(size.width * 0.12f, size.height * 0.14f))
                // Base
                drawRoundRect(c, topLeft = Offset(size.width * 0.28f, size.height * 0.80f), size = Size(size.width * 0.44f, size.height * 0.10f), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()))
                // Handles
                drawArc(c, startAngle = 100f, sweepAngle = 160f, useCenter = false, topLeft = Offset(size.width * 0.12f, size.height * 0.28f), size = Size(size.width * 0.25f, size.height * 0.28f), style = Stroke(2.dp.toPx()))
                drawArc(c, startAngle = -80f, sweepAngle = 160f, useCenter = false, topLeft = Offset(size.width * 0.63f, size.height * 0.28f), size = Size(size.width * 0.25f, size.height * 0.28f), style = Stroke(2.dp.toPx()))
            }
            Text(
                text = "$highScore",
                color = currentTheme.textPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Right: Speaker Toggle & Pause Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Speaker Button
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onMusicToggle() },
                shape = CircleShape,
                color = currentTheme.surfaceUi,
                border = BorderStroke(1.dp, currentTheme.textPrimary.copy(alpha = 0.1f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        val c = currentTheme.textPrimary.copy(alpha = if (musicEnabled) 0.9f else 0.4f)
                        val spkPath = Path().apply {
                            moveTo(size.width * 0.15f, size.height * 0.35f)
                            lineTo(size.width * 0.38f, size.height * 0.35f)
                            lineTo(size.width * 0.65f, size.height * 0.15f)
                            lineTo(size.width * 0.65f, size.height * 0.85f)
                            lineTo(size.width * 0.38f, size.height * 0.65f)
                            lineTo(size.width * 0.15f, size.height * 0.65f)
                            close()
                        }
                        drawPath(spkPath, c)
                        if (musicEnabled) {
                            drawArc(c, startAngle = -45f, sweepAngle = 90f, useCenter = false, topLeft = Offset(size.width * 0.55f, size.height * 0.25f), size = Size(size.width * 0.35f, size.height * 0.50f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                        } else {
                            drawLine(c, Offset(size.width * 0.2f, size.height * 0.2f), Offset(size.width * 0.8f, size.height * 0.8f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                        }
                    }
                }
            }

            // Pause Button
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onPauseToggle() },
                shape = CircleShape,
                color = currentTheme.surfaceUi,
                border = BorderStroke(1.dp, currentTheme.textPrimary.copy(alpha = 0.1f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        val c = currentTheme.textPrimary
                        if (gameState == GameState.PAUSED) {
                            // Play triangle
                            val playPath = Path().apply {
                                moveTo(size.width * 0.30f, size.height * 0.18f)
                                lineTo(size.width * 0.82f, size.height * 0.50f)
                                lineTo(size.width * 0.30f, size.height * 0.82f)
                                close()
                            }
                            drawPath(playPath, c)
                        } else {
                            // Two pause bars
                            val barW = 3.5.dp.toPx()
                            drawRoundRect(c, topLeft = Offset(size.width * 0.25f, size.height * 0.15f), size = Size(barW, size.height * 0.70f), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()))
                            drawRoundRect(c, topLeft = Offset(size.width * 0.60f, size.height * 0.15f), size = Size(barW, size.height * 0.70f), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. BOTTOM GESTURE HINT ("Swipe anywhere to steer")
// ==========================================

@Composable
fun SwipeSteerHint(
    theme: GameTheme,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Swipe anywhere to steer",
            color = theme.textPrimary.copy(alpha = 0.72f),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Canvas(modifier = Modifier.size(22.dp)) {
            val c = theme.textPrimary.copy(alpha = 0.85f)
            val strokeW = 1.8.dp.toPx()
            // Gesture ripple arc
            drawArc(
                color = c.copy(alpha = 0.45f),
                startAngle = -140f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(size.width * 0.22f, size.height * 0.05f),
                size = Size(size.width * 0.50f, size.height * 0.40f),
                style = Stroke(strokeW, cap = StrokeCap.Round)
            )
            // Hand silhouette
            val handPath = Path().apply {
                moveTo(size.width * 0.42f, size.height * 0.48f)
                lineTo(size.width * 0.42f, size.height * 0.28f)
                cubicTo(
                    size.width * 0.42f, size.height * 0.20f,
                    size.width * 0.54f, size.height * 0.20f,
                    size.width * 0.54f, size.height * 0.28f
                )
                lineTo(size.width * 0.54f, size.height * 0.50f)
                lineTo(size.width * 0.72f, size.height * 0.54f)
                lineTo(size.width * 0.72f, size.height * 0.72f)
                lineTo(size.width * 0.45f, size.height * 0.90f)
                lineTo(size.width * 0.28f, size.height * 0.68f)
                close()
            }
            drawPath(handPath, c)
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
            .background(theme.background)
    ) {
        // Decorative pixel matrix in top-left and top-right corners
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pixelGreen = theme.primaryUi.copy(alpha = 0.35f)
            val pSize = 10.dp.toPx()
            // Top Left pixels
            drawRect(pixelGreen, topLeft = Offset(24.dp.toPx(), 36.dp.toPx()), size = Size(pSize, pSize))
            drawRect(pixelGreen, topLeft = Offset(38.dp.toPx(), 36.dp.toPx()), size = Size(pSize, pSize))
            drawRect(pixelGreen, topLeft = Offset(24.dp.toPx(), 50.dp.toPx()), size = Size(pSize, pSize))
            drawRect(pixelGreen, topLeft = Offset(38.dp.toPx(), 64.dp.toPx()), size = Size(pSize, pSize))

            // Top Right pixels
            val rightX = size.width - 48.dp.toPx()
            drawRect(pixelGreen, topLeft = Offset(rightX, 36.dp.toPx()), size = Size(pSize, pSize))
            drawRect(pixelGreen, topLeft = Offset(rightX + 14.dp.toPx(), 36.dp.toPx()), size = Size(pSize, pSize))
            drawRect(pixelGreen, topLeft = Offset(rightX + 14.dp.toPx(), 50.dp.toPx()), size = Size(pSize, pSize))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Cute Snake S Logo
            SnakeSLogo(modifier = Modifier.size(58.dp), theme = theme)

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = APP_NAME.uppercase(),
                color = theme.textPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Pill: Best Score
            Surface(
                shape = CircleShape,
                color = theme.surfaceUi.copy(alpha = 0.6f),
                border = BorderStroke(1.5.dp, theme.primaryUi.copy(alpha = 0.8f))
            ) {
                Text(
                    text = "Best $highScore",
                    color = theme.primaryUi,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Decorative Center Checkerboard Board
            DecorativeCheckerboardCard(
                theme = theme,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.05f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Difficulty Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bar Chart Icon
                Canvas(modifier = Modifier.size(16.dp)) {
                    val w = size.width / 5f
                    val c = theme.primaryUi
                    drawRoundRect(c, topLeft = Offset(0f, size.height * 0.6f), size = Size(w, size.height * 0.4f), cornerRadius = CornerRadius(2f, 2f))
                    drawRoundRect(c, topLeft = Offset(w * 1.5f, size.height * 0.35f), size = Size(w, size.height * 0.65f), cornerRadius = CornerRadius(2f, 2f))
                    drawRoundRect(c, topLeft = Offset(w * 3f, 0f), size = Size(w, size.height), cornerRadius = CornerRadius(2f, 2f))
                }
                Text(
                    text = "Difficulty",
                    color = theme.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Difficulty.entries.forEach { diff ->
                    val isSelected = diff == selectedDifficulty
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clickable { onDifficultySelected(diff) },
                        shape = RoundedCornerShape(21.dp),
                        color = if (isSelected) theme.primaryUi else theme.surfaceUi.copy(alpha = 0.6f),
                        border = if (isSelected) null else BorderStroke(1.dp, theme.textPrimary.copy(alpha = 0.12f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = diff.displayName,
                                color = if (isSelected) Color(0xFF061A0E) else theme.textPrimary.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Theme Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Palette Icon
                Canvas(modifier = Modifier.size(16.dp)) {
                    drawCircle(color = theme.primaryUi, radius = size.width / 2f)
                    drawCircle(color = theme.background, radius = size.width * 0.18f, center = Offset(size.width * 0.32f, size.height * 0.35f))
                    drawCircle(color = theme.background, radius = size.width * 0.15f, center = Offset(size.width * 0.68f, size.height * 0.35f))
                    drawCircle(color = theme.background, radius = size.width * 0.15f, center = Offset(size.width * 0.45f, size.height * 0.72f))
                }
                Text(
                    text = "Theme",
                    color = theme.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GameTheme.entries.forEach { t ->
                    ThemeSwatchTile(
                        tileTheme = t,
                        isSelected = t == selectedTheme,
                        accentColor = theme.primaryUi,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeSelected(t) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Portal Walls Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = theme.surfaceUi.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, theme.textPrimary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Canvas(modifier = Modifier.size(18.dp)) {
                            drawRoundRect(
                                color = theme.primaryUi,
                                style = Stroke(width = 2.dp.toPx()),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                            drawLine(
                                color = theme.primaryUi,
                                start = Offset(size.width * 0.35f, size.height * 0.25f),
                                end = Offset(size.width * 0.65f, size.height * 0.75f),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                        Text(
                            text = "Portal Walls",
                            color = theme.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Switch(
                        checked = wrapAroundWalls,
                        onCheckedChange = onWrapToggle,
                        colors = themedSwitchColors(theme)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Action Bar: Big PLAY button + Settings Gear
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStartGame,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primaryUi
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = "PLAY",
                        color = Color(0xFF071F11),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }

                Surface(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = theme.surfaceUi,
                    border = BorderStroke(1.dp, theme.textPrimary.copy(alpha = 0.15f)),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "⚙",
                            color = theme.textPrimary,
                            fontSize = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun SnakeSLogo(modifier: Modifier = Modifier, theme: GameTheme) {
    Canvas(modifier = modifier) {
        val strokeW = size.width * 0.18f
        val bodyColor = theme.snakeHead

        // Draw 'S' curve
        val sPath = Path().apply {
            moveTo(size.width * 0.72f, size.height * 0.28f)
            cubicTo(
                size.width * 0.7f, size.height * 0.08f,
                size.width * 0.3f, size.height * 0.08f,
                size.width * 0.3f, size.height * 0.32f
            )
            cubicTo(
                size.width * 0.3f, size.height * 0.52f,
                size.width * 0.72f, size.height * 0.54f,
                size.width * 0.72f, size.height * 0.75f
            )
            cubicTo(
                size.width * 0.72f, size.height * 0.95f,
                size.width * 0.26f, size.height * 0.95f,
                size.width * 0.22f, size.height * 0.76f
            )
        }
        drawPath(
            path = sPath,
            color = bodyColor,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Cute Tongue
        val tonguePath = Path().apply {
            moveTo(size.width * 0.78f, size.height * 0.28f)
            lineTo(size.width * 0.92f, size.height * 0.26f)
            lineTo(size.width * 0.98f, size.height * 0.22f)
            moveTo(size.width * 0.92f, size.height * 0.26f)
            lineTo(size.width * 0.98f, size.height * 0.30f)
        }
        drawPath(
            path = tonguePath,
            color = Color(0xFFFF3355),
            style = Stroke(width = strokeW * 0.22f, cap = StrokeCap.Round)
        )

        // Eyes on the head
        val eyeRadius = size.width * 0.09f
        val pupilRadius = eyeRadius * 0.5f

        val eye1Center = Offset(size.width * 0.58f, size.height * 0.22f)
        val eye2Center = Offset(size.width * 0.72f, size.height * 0.22f)

        // Eye whites
        drawCircle(Color.White, radius = eyeRadius, center = eye1Center)
        drawCircle(Color.White, radius = eyeRadius, center = eye2Center)

        // Eye pupils
        drawCircle(Color.Black, radius = pupilRadius, center = Offset(eye1Center.x + pupilRadius * 0.3f, eye1Center.y))
        drawCircle(Color.Black, radius = pupilRadius, center = Offset(eye2Center.x + pupilRadius * 0.3f, eye2Center.y))
    }
}

@Composable
fun DecorativeCheckerboardCard(theme: GameTheme, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceUi),
        border = BorderStroke(2.5.dp, theme.surfaceUi.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cols = 8
            val rows = 8
            val cellW = size.width / cols
            val cellH = size.height / rows

            // 1. Two-Tone Checkerboard
            for (c in 0 until cols) {
                for (r in 0 until rows) {
                    val isEven = (c + r) % 2 == 0
                    drawRect(
                        color = if (isEven) theme.checkerLight else theme.checkerDark,
                        topLeft = Offset(c * cellW, r * cellH),
                        size = Size(cellW, cellH)
                    )
                }
            }

            // 2. Obstacle rounded stones
            val stoneColor = Color(0xFF75857E)
            val stoneHighlight = Color(0xFF94A39D)
            fun drawStone(cx: Float, cy: Float, rad: Float) {
                drawRoundRect(
                    color = stoneColor,
                    topLeft = Offset(cx - rad, cy - rad * 0.8f),
                    size = Size(rad * 2f, rad * 1.6f),
                    cornerRadius = CornerRadius(rad * 0.8f, rad * 0.8f)
                )
                drawCircle(
                    color = stoneHighlight.copy(alpha = 0.6f),
                    radius = rad * 0.35f,
                    center = Offset(cx - rad * 0.35f, cy - rad * 0.25f)
                )
            }
            drawStone(cellW * 1.5f, cellH * 1.8f, cellW * 0.42f)
            drawStone(cellW * 6.5f, cellH * 4.8f, cellW * 0.45f)
            drawStone(cellW * 5.0f, cellH * 6.8f, cellW * 0.44f)

            // 3. Smooth Curved Snake Body
            val snakePath = Path().apply {
                moveTo(cellW * 1.8f, cellH * 6.2f)
                cubicTo(
                    cellW * 1.8f, cellH * 5.0f,
                    cellW * 2.8f, cellH * 4.9f,
                    cellW * 3.5f, cellH * 4.9f
                )
                cubicTo(
                    cellW * 4.2f, cellH * 4.9f,
                    cellW * 4.2f, cellH * 3.7f,
                    cellW * 4.2f, cellH * 3.2f
                )
                cubicTo(
                    cellW * 4.2f, cellH * 2.7f,
                    cellW * 4.6f, cellH * 2.7f,
                    cellW * 5.4f, cellH * 2.7f
                )
            }
            // Outline / Shadow
            drawPath(
                path = snakePath,
                color = theme.snakeBody.copy(alpha = 0.4f),
                style = Stroke(width = cellW * 0.74f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Body Fill
            drawPath(
                path = snakePath,
                color = theme.snakeHead,
                style = Stroke(width = cellW * 0.62f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Soft Top Highlight
            drawPath(
                path = snakePath,
                color = Color.White.copy(alpha = 0.22f),
                style = Stroke(width = cellW * 0.22f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Snake Head Eyes (on cell 5.4, 2.7)
            val headX = cellW * 5.4f
            val headY = cellH * 2.7f
            val eyeRad = cellW * 0.16f
            val pupilRad = eyeRad * 0.55f

            val e1 = Offset(headX - eyeRad * 0.4f, headY - eyeRad * 0.5f)
            val e2 = Offset(headX + eyeRad * 0.8f, headY - eyeRad * 0.5f)

            drawCircle(Color.White, radius = eyeRad, center = e1)
            drawCircle(Color.White, radius = eyeRad, center = e2)
            drawCircle(Color.Black, radius = pupilRad, center = Offset(e1.x + pupilRad * 0.4f, e1.y))
            drawCircle(Color.Black, radius = pupilRad, center = Offset(e2.x + pupilRad * 0.4f, e2.y))

            // Cute smiling mouth
            drawArc(
                color = Color(0xFF0F3B20),
                startAngle = 10f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(headX + eyeRad * 0.2f, headY + eyeRad * 0.2f),
                size = Size(eyeRad * 1.4f, eyeRad * 0.8f),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )

            // 4. Glossy Apple with Leaf & Sparkles
            val appleX = cellW * 6.8f
            val appleY = cellH * 2.8f
            val appleR = cellW * 0.45f

            // Apple Body
            drawCircle(Color(0xFFFF2A4D), radius = appleR, center = Offset(appleX, appleY))
            // Apple highlight
            drawCircle(Color.White.copy(alpha = 0.65f), radius = appleR * 0.32f, center = Offset(appleX - appleR * 0.35f, appleY - appleR * 0.35f))

            // Apple Leaf
            val leafPath = Path().apply {
                moveTo(appleX, appleY - appleR * 0.9f)
                quadraticTo(
                    appleX + appleR * 0.5f, appleY - appleR * 1.5f,
                    appleX + appleR * 0.7f, appleY - appleR * 1.1f
                )
                quadraticTo(
                    appleX + appleR * 0.3f, appleY - appleR * 0.8f,
                    appleX, appleY - appleR * 0.9f
                )
            }
            drawPath(leafPath, color = Color(0xFF40DF88), style = Fill)

            // Sparkles around apple
            fun drawSparkle(sx: Float, sy: Float, sSize: Float) {
                val sp = Path().apply {
                    moveTo(sx, sy - sSize)
                    lineTo(sx + sSize * 0.25f, sy - sSize * 0.25f)
                    lineTo(sx + sSize, sy)
                    lineTo(sx + sSize * 0.25f, sy + sSize * 0.25f)
                    lineTo(sx, sy + sSize)
                    lineTo(sx - sSize * 0.25f, sy + sSize * 0.25f)
                    lineTo(sx - sSize, sy)
                    lineTo(sx - sSize * 0.25f, sy - sSize * 0.25f)
                    close()
                }
                drawPath(sp, color = Color(0xFFFFD129), style = Fill)
            }
            drawSparkle(appleX - appleR * 1.1f, appleY - appleR * 0.7f, cellW * 0.16f)
            drawSparkle(appleX + appleR * 1.0f, appleY - appleR * 0.9f, cellW * 0.14f)
            drawSparkle(appleX + appleR * 1.1f, appleY + appleR * 0.6f, cellW * 0.15f)
        }
    }
}

@Composable
fun ThemeSwatchTile(
    tileTheme: GameTheme,
    isSelected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = RoundedCornerShape(16.dp),
            color = tileTheme.background,
            border = BorderStroke(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) accentColor else Color.White.copy(alpha = 0.15f)
            ),
            shadowElevation = if (isSelected) 6.dp else 2.dp
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cells = 4
                val cw = size.width / cells
                val ch = size.height / cells
                for (x in 0 until cells) {
                    for (y in 0 until cells) {
                        drawRect(
                            color = if ((x + y) % 2 == 0) tileTheme.checkerLight else tileTheme.checkerDark,
                            topLeft = Offset(x * cw, y * ch),
                            size = Size(cw, ch)
                        )
                    }
                }
                // Mini snake in swatch
                val sPath = Path().apply {
                    moveTo(cw * 0.8f, ch * 2.8f)
                    cubicTo(cw * 1.5f, ch * 1.2f, cw * 2.2f, ch * 2.8f, cw * 3.0f, ch * 1.5f)
                }
                drawPath(
                    path = sPath,
                    color = tileTheme.snakeHead,
                    style = Stroke(width = cw * 0.7f, cap = StrokeCap.Round)
                )
                // Mini apple
                drawCircle(tileTheme.normalFood, radius = cw * 0.42f, center = Offset(cw * 3.2f, ch * 1.2f))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tileTheme.themeName,
            color = if (isSelected) accentColor else Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp
        )
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
    swipeSensitivity: Float,
    onVibrationChange: (Boolean) -> Unit,
    onMusicChange: (Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onVolumeChangeFinished: () -> Unit,
    onSwipeSensitivityChange: (Float) -> Unit,
    onSwipeSensitivityChangeFinished: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        // Decorative pixel matrix in top-left and top-right corners (same as Menu)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pixelGreen = theme.primaryUi.copy(alpha = 0.35f)
            val pSize = 10.dp.toPx()
            // Top Left pixels
            drawRect(pixelGreen, topLeft = Offset(24.dp.toPx(), 36.dp.toPx()), size = Size(pSize, pSize))
            drawRect(pixelGreen, topLeft = Offset(38.dp.toPx(), 36.dp.toPx()), size = Size(pSize, pSize))
            drawRect(pixelGreen, topLeft = Offset(24.dp.toPx(), 50.dp.toPx()), size = Size(pSize, pSize))
            drawRect(pixelGreen, topLeft = Offset(38.dp.toPx(), 64.dp.toPx()), size = Size(pSize, pSize))

            // Top Right pixels
            val rightX = size.width - 48.dp.toPx()
            drawRect(pixelGreen, topLeft = Offset(rightX, 36.dp.toPx()), size = Size(pSize, pSize))
            drawRect(pixelGreen, topLeft = Offset(rightX + 14.dp.toPx(), 36.dp.toPx()), size = Size(pSize, pSize))
            drawRect(pixelGreen, topLeft = Offset(rightX + 14.dp.toPx(), 50.dp.toPx()), size = Size(pSize, pSize))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 26.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Top Header: Back Button + "Settings" Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clickable { onClose() },
                    shape = CircleShape,
                    color = theme.surfaceUi,
                    border = BorderStroke(1.5.dp, theme.primaryUi.copy(alpha = 0.35f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(22.dp)) {
                            val strokeW = 3.dp.toPx()
                            val c = theme.textPrimary
                            val headX = size.width * 0.22f
                            val tailX = size.width * 0.78f
                            val midY = size.height * 0.5f

                            // Main horizontal stem
                            drawLine(
                                color = c,
                                start = Offset(headX, midY),
                                end = Offset(tailX, midY),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                            // Top arrow wing
                            drawLine(
                                color = c,
                                start = Offset(headX, midY),
                                end = Offset(headX + size.width * 0.32f, midY - size.height * 0.32f),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                            // Bottom arrow wing
                            drawLine(
                                color = c,
                                start = Offset(headX, midY),
                                end = Offset(headX + size.width * 0.32f, midY + size.height * 0.32f),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Text(
                    text = "Settings",
                    color = theme.textPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Settings Cards in Scrollable Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card 1: Vibration
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = theme.surfaceUi,
                    border = BorderStroke(1.dp, theme.textPrimary.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Vibrating Phone Icon
                            Canvas(modifier = Modifier.size(28.dp)) {
                                val c = theme.textPrimary.copy(alpha = 0.9f)
                                val strokeW = 2.dp.toPx()
                                val pw = size.width * 0.44f
                                val ph = size.height * 0.74f
                                val px = (size.width - pw) / 2f
                                val py = (size.height - ph) / 2f

                                // Phone rectangle
                                drawRoundRect(
                                    color = c,
                                    topLeft = Offset(px, py),
                                    size = Size(pw, ph),
                                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                                    style = Stroke(width = strokeW)
                                )
                                // Speaker slot
                                drawLine(
                                    color = c,
                                    start = Offset(px + pw * 0.32f, py + ph * 0.15f),
                                    end = Offset(px + pw * 0.68f, py + ph * 0.15f),
                                    strokeWidth = 1.5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                // Home button dot
                                drawCircle(
                                    color = c,
                                    radius = 1.5.dp.toPx(),
                                    center = Offset(px + pw / 2f, py + ph * 0.85f)
                                )
                                // Left waves
                                val waveL = Path().apply {
                                    moveTo(px - 4.dp.toPx(), py + ph * 0.2f)
                                    lineTo(px - 7.dp.toPx(), py + ph * 0.35f)
                                    lineTo(px - 4.dp.toPx(), py + ph * 0.5f)
                                    lineTo(px - 7.dp.toPx(), py + ph * 0.65f)
                                    lineTo(px - 4.dp.toPx(), py + ph * 0.8f)
                                }
                                drawPath(waveL, color = c, style = Stroke(strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))

                                // Right waves
                                val waveR = Path().apply {
                                    moveTo(px + pw + 4.dp.toPx(), py + ph * 0.2f)
                                    lineTo(px + pw + 7.dp.toPx(), py + ph * 0.35f)
                                    lineTo(px + pw + 4.dp.toPx(), py + ph * 0.5f)
                                    lineTo(px + pw + 7.dp.toPx(), py + ph * 0.65f)
                                    lineTo(px + pw + 4.dp.toPx(), py + ph * 0.8f)
                                }
                                drawPath(waveR, color = c, style = Stroke(strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
                            }

                            Text(
                                text = "Vibration",
                                color = theme.textPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = onVibrationChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = theme.primaryUi,
                                uncheckedThumbColor = theme.textPrimary.copy(alpha = 0.45f),
                                uncheckedTrackColor = theme.surfaceUi.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                // Card 2: Music
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = theme.surfaceUi,
                    border = BorderStroke(1.dp, theme.textPrimary.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Music Note / Speaker Icon
                            Canvas(modifier = Modifier.size(28.dp)) {
                                val c = theme.textPrimary.copy(alpha = 0.9f)
                                val strokeW = 2.dp.toPx()
                                // Speaker body
                                val spkPath = Path().apply {
                                    moveTo(size.width * 0.12f, size.height * 0.36f)
                                    lineTo(size.width * 0.32f, size.height * 0.36f)
                                    lineTo(size.width * 0.50f, size.height * 0.18f)
                                    lineTo(size.width * 0.50f, size.height * 0.82f)
                                    lineTo(size.width * 0.32f, size.height * 0.64f)
                                    lineTo(size.width * 0.12f, size.height * 0.64f)
                                    close()
                                }
                                drawPath(spkPath, c)

                                // Note 1
                                drawCircle(c, radius = 2.5.dp.toPx(), center = Offset(size.width * 0.70f, size.height * 0.70f))
                                drawLine(c, Offset(size.width * 0.70f + 2.dp.toPx(), size.height * 0.70f), Offset(size.width * 0.70f + 2.dp.toPx(), size.height * 0.34f), strokeWidth = strokeW, cap = StrokeCap.Round)

                                // Note 2
                                drawCircle(c, radius = 2.5.dp.toPx(), center = Offset(size.width * 0.90f, size.height * 0.52f))
                                drawLine(c, Offset(size.width * 0.90f + 2.dp.toPx(), size.height * 0.52f), Offset(size.width * 0.90f + 2.dp.toPx(), size.height * 0.22f), strokeWidth = strokeW, cap = StrokeCap.Round)

                                // Connecting Beam
                                drawLine(c, Offset(size.width * 0.70f + 2.dp.toPx(), size.height * 0.34f), Offset(size.width * 0.90f + 2.dp.toPx(), size.height * 0.22f), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            }

                            Text(
                                text = "Music",
                                color = theme.textPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Switch(
                            checked = musicEnabled,
                            onCheckedChange = onMusicChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = theme.primaryUi,
                                uncheckedThumbColor = theme.textPrimary.copy(alpha = 0.45f),
                                uncheckedTrackColor = theme.surfaceUi.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                // Card 3: Music Volume
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = theme.surfaceUi,
                    border = BorderStroke(1.dp, theme.textPrimary.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Music Volume",
                                color = theme.textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Center)
                            )

                            // Tooltip Pill showing percentage
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.textPrimary.copy(alpha = 0.9f),
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) {
                                Text(
                                    text = "${(musicVolume * 100).toInt()}%",
                                    color = Color(0xFF061A0E),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Speaker icon
                            Canvas(modifier = Modifier.size(24.dp)) {
                                val c = theme.textPrimary.copy(alpha = if (musicEnabled) 0.85f else 0.35f)
                                val path = Path().apply {
                                    moveTo(size.width * 0.15f, size.height * 0.35f)
                                    lineTo(size.width * 0.38f, size.height * 0.35f)
                                    lineTo(size.width * 0.62f, size.height * 0.15f)
                                    lineTo(size.width * 0.62f, size.height * 0.85f)
                                    lineTo(size.width * 0.38f, size.height * 0.65f)
                                    lineTo(size.width * 0.15f, size.height * 0.65f)
                                    close()
                                }
                                drawPath(path, c)

                                drawArc(
                                    color = c,
                                    startAngle = -45f,
                                    sweepAngle = 90f,
                                    useCenter = false,
                                    topLeft = Offset(size.width * 0.48f, size.height * 0.28f),
                                    size = Size(size.width * 0.32f, size.height * 0.44f),
                                    style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = c,
                                    startAngle = -45f,
                                    sweepAngle = 90f,
                                    useCenter = false,
                                    topLeft = Offset(size.width * 0.58f, size.height * 0.16f),
                                    size = Size(size.width * 0.48f, size.height * 0.68f),
                                    style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Slider(
                                value = musicVolume,
                                onValueChange = onVolumeChange,
                                onValueChangeFinished = onVolumeChangeFinished,
                                valueRange = 0f..1f,
                                enabled = musicEnabled,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = theme.primaryUi,
                                    activeTrackColor = theme.primaryUi,
                                    inactiveTrackColor = theme.primaryUi.copy(alpha = 0.22f),
                                    disabledThumbColor = theme.primaryUi.copy(alpha = 0.3f),
                                    disabledActiveTrackColor = theme.primaryUi.copy(alpha = 0.18f),
                                    disabledInactiveTrackColor = theme.primaryUi.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }

                // Card 4: Swipe Sensitivity
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = theme.surfaceUi,
                    border = BorderStroke(1.dp, theme.textPrimary.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Touch / Tap gesture icon
                                Canvas(modifier = Modifier.size(26.dp)) {
                                    val c = theme.textPrimary.copy(alpha = 0.9f)
                                    val strokeW = 1.8.dp.toPx()

                                    // Outer pulse arc
                                    drawArc(
                                        color = c.copy(alpha = 0.5f),
                                        startAngle = -140f,
                                        sweepAngle = 100f,
                                        useCenter = false,
                                        topLeft = Offset(size.width * 0.24f, size.height * 0.05f),
                                        size = Size(size.width * 0.48f, size.height * 0.40f),
                                        style = Stroke(strokeW, cap = StrokeCap.Round)
                                    )

                                    // Inner pulse arc
                                    drawArc(
                                        color = c,
                                        startAngle = -140f,
                                        sweepAngle = 100f,
                                        useCenter = false,
                                        topLeft = Offset(size.width * 0.34f, size.height * 0.15f),
                                        size = Size(size.width * 0.28f, size.height * 0.25f),
                                        style = Stroke(strokeW, cap = StrokeCap.Round)
                                    )

                                    // Pointing hand silhouette
                                    val handPath = Path().apply {
                                        moveTo(size.width * 0.42f, size.height * 0.48f)
                                        lineTo(size.width * 0.42f, size.height * 0.28f)
                                        // Fingertip
                                        cubicTo(
                                            size.width * 0.42f, size.height * 0.20f,
                                            size.width * 0.54f, size.height * 0.20f,
                                            size.width * 0.54f, size.height * 0.28f
                                        )
                                        lineTo(size.width * 0.54f, size.height * 0.50f)
                                        lineTo(size.width * 0.72f, size.height * 0.54f)
                                        lineTo(size.width * 0.72f, size.height * 0.72f)
                                        lineTo(size.width * 0.45f, size.height * 0.90f)
                                        lineTo(size.width * 0.28f, size.height * 0.68f)
                                        close()
                                    }
                                    drawPath(handPath, c)
                                }

                                Text(
                                    text = "Swipe Sensitivity",
                                    color = theme.textPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.surfaceUi.copy(alpha = 0.8f),
                                border = BorderStroke(1.dp, theme.primaryUi.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "${(swipeSensitivity * 100).toInt()}%",
                                    color = theme.primaryUi,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Slider(
                            value = swipeSensitivity,
                            onValueChange = onSwipeSensitivityChange,
                            onValueChangeFinished = onSwipeSensitivityChangeFinished,
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.primaryUi,
                                activeTrackColor = theme.primaryUi,
                                inactiveTrackColor = theme.primaryUi.copy(alpha = 0.22f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Full-Width Matrix Neon DONE Button
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                onClick = onClose,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryUi),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "DONE",
                    color = Color(0xFF061A0E),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }
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