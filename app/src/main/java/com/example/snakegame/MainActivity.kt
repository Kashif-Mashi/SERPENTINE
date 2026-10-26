package com.example.snakegame

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Suppress("unused")
const val APP_NAME = "Serpentine"
const val APP_TAGLINE = "Small Snake. Big Adventure."

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

@Composable
fun SnakeGameApp(isForeground: Boolean) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("snake_game_prefs", Context.MODE_PRIVATE) }
    val music = remember { MusicPlayer() }
    val sfx = remember { SoundEffectsPlayer() }

    // Grid Dimensions
    val gridWidth = 20
    val gridHeight = 24

    // Navigation & Screen State
    var currentScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }
    var previousScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }
    var showSplash by remember { mutableStateOf(true) }

    // Theme & Difficulty State
    val savedThemeName = prefs.getString("selected_theme", GameTheme.CITRUS_POP.themeName)
    val initialTheme = GameTheme.entries.find { it.themeName == savedThemeName } ?: GameTheme.CITRUS_POP
    var currentTheme by remember { mutableStateOf(initialTheme) }

    val savedDiffName = prefs.getString("difficulty", Difficulty.MEDIUM.name)
    val initialDiff = Difficulty.entries.find { it.name == savedDiffName } ?: Difficulty.MEDIUM
    var currentDifficulty by remember { mutableStateOf(initialDiff) }

    // Economy & Unlocks
    var coins by remember { mutableIntStateOf(prefs.getInt("coins", 250)) }
    var unlockedThemes by remember {
        val set = prefs.getStringSet("unlocked_themes", setOf(GameTheme.CITRUS_POP.themeName)) ?: setOf(GameTheme.CITRUS_POP.themeName)
        mutableStateOf(set)
    }

    // Audio & Haptics & Game Rules Settings
    var vibrationEnabled by remember { mutableStateOf(prefs.getBoolean("vibration", true)) }
    var hapticsEnabled by remember { mutableStateOf(prefs.getBoolean("haptics_on", true)) }
    var musicEnabled by remember { mutableStateOf(prefs.getBoolean("music_on", true)) }
    var soundEffectsEnabled by remember { mutableStateOf(prefs.getBoolean("sfx_on", true)) }
    var musicVolume by remember { mutableFloatStateOf(prefs.getFloat("music_volume", 0.6f)) }
    var swipeSensitivity by remember { mutableFloatStateOf(prefs.getFloat("swipe_sensitivity", 0.85f)) }
    var darkModeEnabled by remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }
    var portalWallsEnabled by remember { mutableStateOf(prefs.getBoolean("portal_walls", false)) }

    // Score Tracking
    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(prefs.getInt("high_score", 542)) }

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

    // Board Entities & Buffs
    var currentFood by remember { mutableStateOf(Food(Point(15, 12), FoodType.NORMAL)) }
    var obstacles by remember { mutableStateOf(setOf<Point>()) }
    var speedBoostEndTime by remember { mutableLongStateOf(0L) }
    val particles = remember { mutableStateListOf<Particle>() }
    val floatingTexts = remember { mutableStateListOf<FloatingText>() }
    var frameTick by remember { mutableLongStateOf(0L) }

    // Pulse animation for golden items
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

    // Helper: Haptics
    fun triggerHaptic(type: HapticType) {
        if (vibrationEnabled && hapticsEnabled) performHaptic(context, type)
    }

    // Helper: Direction Input
    fun queueDirection(newDir: Direction) {
        if (currentScreen != AppScreen.GAMEPLAY) return
        if (directionQueue.isEmpty()) {
            if (!newDir.isOpposite(currentDirection) && newDir != currentDirection) {
                directionQueue.add(newDir)
                triggerHaptic(HapticType.LIGHT)
            }
        } else if (directionQueue.size == 1) {
            val pendingDir = directionQueue[0]
            // Case A: User rapidly reverses their mind (e.g. flicked UP then immediately DOWN, or LEFT then RIGHT)
            // If newDir is opposite to the pending direction, and not opposite to currentDirection, replace the pending turn
            if (newDir.isOpposite(pendingDir)) {
                if (!newDir.isOpposite(currentDirection) && newDir != currentDirection) {
                    directionQueue[0] = newDir
                    triggerHaptic(HapticType.LIGHT)
                }
            } else if (newDir == currentDirection) {
                // User canceled pending turn by swiping in the direction snake is already moving
                directionQueue.clear()
                triggerHaptic(HapticType.LIGHT)
            } else if (!newDir.isOpposite(pendingDir) && newDir != pendingDir) {
                // Buffer second turn (e.g. quick cornering: right -> up -> left)
                directionQueue.add(newDir)
                triggerHaptic(HapticType.LIGHT)
            }
        } else if (directionQueue.size >= 2) {
            val firstQueued = directionQueue[0]
            // User made another rapid flick before tick; update the second buffered turn with freshest intent
            if (!newDir.isOpposite(firstQueued) && newDir != firstQueued) {
                directionQueue[1] = newDir
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
        val targetPoint = if (emptySlots.isNotEmpty()) emptySlots.random() else Point(0, 0)
        val roll = Random.nextFloat()
        val type = when {
            roll < 0.15f -> FoodType.GOLDEN
            roll < 0.30f -> FoodType.SPEED_BOOST
            else -> FoodType.NORMAL
        }
        return Food(targetPoint, type)
    }

    // Helper: Spawn Dynamic Obstacle
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
        return if (candidates.isNotEmpty()) currentObstacles + candidates.random() else currentObstacles
    }

    // Helper: Spawn Particles
    fun spawnParticles(gx: Int, gy: Int, color: Color, count: Int = 18) {
        repeat(count) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val speed = Random.nextFloat() * 6f + 2f
            particles.add(
                Particle(
                    x = gx.toFloat(),
                    y = gy.toFloat(),
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed,
                    color = color,
                    alpha = 1f,
                    radius = Random.nextFloat() * 3.5f + 3f,
                    life = Random.nextInt(25, 42)
                )
            )
        }
    }

    // Reset Game State
    fun startNewGame() {
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
        currentScreen = AppScreen.GAMEPLAY
        triggerHaptic(HapticType.MEDIUM)
        if (soundEffectsEnabled) sfx.playButtonClick()
    }

    // ------------------------------------------
    // Splash Loading Progress
    // ------------------------------------------
    val splashProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        val prepJob = launch(Dispatchers.Default) { music.prepare() }
        splashProgress.animateTo(0.7f, tween(850, easing = LinearEasing))
        prepJob.join()
        splashProgress.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        delay(120.milliseconds)
        showSplash = false
    }

    // ------------------------------------------
    // Music Engine Control
    // ------------------------------------------
    val shouldPlayMusic = musicEnabled && isForeground && !showSplash && currentScreen != AppScreen.PAUSED
    LaunchedEffect(shouldPlayMusic) {
        if (shouldPlayMusic) music.start() else music.pause()
    }
    LaunchedEffect(musicVolume) {
        music.setVolume(musicVolume)
    }
    DisposableEffect(Unit) {
        onDispose { music.release() }
    }

    // Auto-pause when going to background
    LaunchedEffect(isForeground) {
        if (!isForeground && currentScreen == AppScreen.GAMEPLAY) {
            currentScreen = AppScreen.PAUSED
        }
    }

    // ------------------------------------------
    // Back Button Handling
    // ------------------------------------------
    BackHandler(enabled = !showSplash && currentScreen != AppScreen.MAIN_MENU) {
        when (currentScreen) {
            AppScreen.GAMEPLAY -> currentScreen = AppScreen.PAUSED
            AppScreen.PAUSED -> currentScreen = AppScreen.MAIN_MENU
            AppScreen.THEME_SELECT, AppScreen.SETTINGS -> {
                currentScreen = previousScreen
            }
            AppScreen.SHOP, AppScreen.LEADERBOARD, AppScreen.GAME_OVER -> {
                currentScreen = AppScreen.MAIN_MENU
            }
            AppScreen.MAIN_MENU -> {}
        }
    }

    // ------------------------------------------
    // 60 FPS Particle & Floating Text Physics
    // ------------------------------------------
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { }
            if (particles.isNotEmpty() || floatingTexts.isNotEmpty()) {
                val pIt = particles.iterator()
                while (pIt.hasNext()) {
                    val p = pIt.next()
                    p.x += p.vx * 0.0085f
                    p.y += p.vy * 0.0085f
                    p.alpha *= 0.94f
                    p.life--
                    if (p.life <= 0 || p.alpha < 0.05f) pIt.remove()
                }

                val ftIt = floatingTexts.iterator()
                while (ftIt.hasNext()) {
                    val ft = ftIt.next()
                    ft.offsetY -= 0.025f
                    ft.alpha *= 0.94f
                    ft.life--
                    if (ft.life <= 0 || ft.alpha < 0.05f) ftIt.remove()
                }
                frameTick++
            }
        }
    }

    // ------------------------------------------
    // Main Game Tick Loop
    // ------------------------------------------
    LaunchedEffect(currentScreen, currentDifficulty, speedBoostEndTime, portalWallsEnabled) {
        while (isActive && currentScreen == AppScreen.GAMEPLAY) {
            val now = System.currentTimeMillis()
            val isBoosted = now < speedBoostEndTime

            val speedDeduction = (score * currentDifficulty.speedStepMs)
            val computedDelay = (currentDifficulty.baseDelayMs - speedDeduction)
                .coerceAtLeast(currentDifficulty.minDelayMs)
            val effectiveDelay = if (isBoosted) (computedDelay * 0.70f).toLong() else computedDelay

            delay(effectiveDelay.milliseconds)

            // Step 1: Update direction from queue
            if (directionQueue.isNotEmpty()) {
                currentDirection = directionQueue.removeAt(0)
            }

            // Step 2: Next Head Position
            var nextX = snake.first().x + currentDirection.dx
            var nextY = snake.first().y + currentDirection.dy

            // Boundary handling: Portal Walls wrap around, or fatal collision
            if (portalWallsEnabled) {
                if (nextX < 0) nextX = gridWidth - 1
                if (nextX >= gridWidth) nextX = 0
                if (nextY < 0) nextY = gridHeight - 1
                if (nextY >= gridHeight) nextY = 0
            } else {
                if (nextX !in 0 until gridWidth || nextY !in 0 until gridHeight) {
                    currentScreen = AppScreen.GAME_OVER
                    triggerHaptic(HapticType.HEAVY)
                    if (soundEffectsEnabled) sfx.playCrash()
                    continue
                }
            }

            val nextHead = Point(nextX, nextY)

            // Self or obstacle collision
            if (nextHead in snake.dropLast(1) || nextHead in obstacles) {
                currentScreen = AppScreen.GAME_OVER
                triggerHaptic(HapticType.HEAVY)
                if (soundEffectsEnabled) sfx.playCrash()
                continue
            }

            // Check food expiry
            if (currentFood.type.durationMs > 0L) {
                if (now - currentFood.spawnTime > currentFood.type.durationMs) {
                    currentFood = generateFood(snake, obstacles)
                }
            }

            // Eat food or move forward
            val hasEaten = (nextHead == currentFood.position)
            val newSnake = mutableListOf(nextHead)
            if (hasEaten) {
                newSnake.addAll(snake) // grow

                val pts = currentFood.type.points
                score += pts
                if (score > highScore) {
                    highScore = score
                    prefs.edit { putInt("high_score", highScore) }
                }

                // Economy: earn coins by playing!
                val earnedCoins = when (currentFood.type) {
                    FoodType.NORMAL -> 1
                    FoodType.GOLDEN -> 5
                    FoodType.SPEED_BOOST -> 2
                }
                coins += earnedCoins
                prefs.edit { putInt("coins", coins) }

                val foodColor = when (currentFood.type) {
                    FoodType.NORMAL -> currentTheme.normalFood
                    FoodType.GOLDEN -> currentTheme.goldenFood
                    FoodType.SPEED_BOOST -> currentTheme.speedFood
                }

                spawnParticles(currentFood.position.x, currentFood.position.y, foodColor)
                floatingTexts.add(
                    FloatingText(
                        text = "+$pts",
                        gridX = currentFood.position.x,
                        gridY = currentFood.position.y,
                        color = foodColor
                    )
                )

                when (currentFood.type) {
                    FoodType.NORMAL -> {
                        triggerHaptic(HapticType.LIGHT)
                        if (soundEffectsEnabled) sfx.playEat()
                    }
                    FoodType.GOLDEN -> {
                        triggerHaptic(HapticType.DOUBLE)
                        if (soundEffectsEnabled) sfx.playGoldenChime()
                    }
                    FoodType.SPEED_BOOST -> {
                        speedBoostEndTime = now + currentFood.type.durationMs
                        triggerHaptic(HapticType.MEDIUM)
                        if (soundEffectsEnabled) sfx.playSpeedBoost()
                    }
                }

                // Spawn obstacles at score intervals
                if (score % currentDifficulty.obstacleInterval == 0) {
                    obstacles = maybeSpawnObstacle(obstacles, newSnake, currentFood)
                }

                currentFood = generateFood(newSnake, obstacles)
            } else {
                newSnake.addAll(snake.dropLast(1))
            }
            snake = newSnake
        }
    }

    // ------------------------------------------
    // ROOT UI CONTAINER WITH DYNAMIC CITRUS THEME
    // ------------------------------------------
    SerpentineTheme(darkMode = darkModeEnabled) {
        val currentPalette = CitrusTheme.palette
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = currentPalette.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    AppScreen.MAIN_MENU -> {
                        MainMenuScreen(
                            highScore = highScore,
                            coins = coins,
                            portalWallsEnabled = portalWallsEnabled,
                            onPortalWallsToggle = {
                                portalWallsEnabled = it
                                prefs.edit { putBoolean("portal_walls", it) }
                            },
                            onPlayClick = { startNewGame() },
                            onThemesClick = {
                                previousScreen = AppScreen.MAIN_MENU
                                currentScreen = AppScreen.THEME_SELECT
                            },
                            onSettingsClick = {
                                previousScreen = AppScreen.MAIN_MENU
                                currentScreen = AppScreen.SETTINGS
                            },
                            onShopClick = {
                                currentScreen = AppScreen.SHOP
                            },
                            onLeaderboardClick = {
                                currentScreen = AppScreen.LEADERBOARD
                            }
                        )
                    }

                    AppScreen.THEME_SELECT -> {
                        ThemeSelectScreen(
                            currentTheme = currentTheme,
                            onThemeSelect = { newTheme ->
                                currentTheme = newTheme
                                prefs.edit { putString("selected_theme", newTheme.themeName) }
                                if (soundEffectsEnabled) sfx.playButtonClick()
                            },
                            onBackClick = {
                                currentScreen = previousScreen
                            }
                        )
                    }

                    AppScreen.GAMEPLAY -> {
                        GameplayScreen(
                            currentTheme = currentTheme,
                            score = score,
                            highScore = highScore,
                            snake = snake,
                            currentDirection = currentDirection,
                            currentFood = currentFood,
                            obstacles = obstacles,
                            particles = particles,
                            floatingTexts = floatingTexts,
                            isSpeedBoosted = System.currentTimeMillis() < speedBoostEndTime,
                            pulseScale = pulseScale,
                            frameTick = frameTick,
                            gridWidth = gridWidth,
                            gridHeight = gridHeight,
                            swipeSensitivity = swipeSensitivity,
                            portalWallsEnabled = portalWallsEnabled,
                            darkModeEnabled = darkModeEnabled,
                            onDirectionChange = { dir -> queueDirection(dir) },
                            onPauseClick = { currentScreen = AppScreen.PAUSED }
                        )
                    }

                    AppScreen.PAUSED -> {
                        PauseScreen(
                            onResume = {
                                currentScreen = AppScreen.GAMEPLAY
                                if (soundEffectsEnabled) sfx.playButtonClick()
                            },
                            onRestart = { startNewGame() },
                            onChangeTheme = {
                                previousScreen = AppScreen.PAUSED
                                currentScreen = AppScreen.THEME_SELECT
                            },
                            onSettings = {
                                previousScreen = AppScreen.PAUSED
                                currentScreen = AppScreen.SETTINGS
                            },
                            onQuit = {
                                currentScreen = AppScreen.MAIN_MENU
                            }
                        )
                    }

                    AppScreen.GAME_OVER -> {
                        GameOverScreen(
                            finalScore = score,
                            highScore = highScore,
                            onTryAgain = { startNewGame() },
                            onMainMenu = { currentScreen = AppScreen.MAIN_MENU }
                        )
                    }

                    AppScreen.SHOP -> {
                        ShopScreen(
                            coins = coins,
                            unlockedThemes = unlockedThemes,
                            currentTheme = currentTheme,
                            onThemePurchased = { purchasedTheme ->
                                if (purchasedTheme.themeName !in unlockedThemes && coins >= purchasedTheme.coinPrice) {
                                    coins -= purchasedTheme.coinPrice
                                    val updatedSet = unlockedThemes + purchasedTheme.themeName
                                    unlockedThemes = updatedSet
                                    prefs.edit {
                                        putInt("coins", coins)
                                        putStringSet("unlocked_themes", updatedSet)
                                    }
                                    if (soundEffectsEnabled) sfx.playCoin()
                                }
                                currentTheme = purchasedTheme
                                prefs.edit { putString("selected_theme", purchasedTheme.themeName) }
                            },
                            onCoinsRewarded = { reward ->
                                coins += reward
                                prefs.edit { putInt("coins", coins) }
                                if (soundEffectsEnabled) sfx.playCoin()
                                triggerHaptic(HapticType.DOUBLE)
                            },
                            onBackClick = { currentScreen = AppScreen.MAIN_MENU }
                        )
                    }

                    AppScreen.LEADERBOARD -> {
                        LeaderboardScreen(
                            userHighScore = highScore,
                            onBackClick = { currentScreen = AppScreen.MAIN_MENU }
                        )
                    }

                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            musicEnabled = musicEnabled,
                            soundEffectsEnabled = soundEffectsEnabled,
                            hapticsEnabled = hapticsEnabled,
                            portalWallsEnabled = portalWallsEnabled,
                            selectedDifficulty = currentDifficulty,
                            darkModeEnabled = darkModeEnabled,
                            vibrationEnabled = vibrationEnabled,
                            swipeSensitivity = swipeSensitivity,
                            onMusicToggle = {
                                musicEnabled = it
                                prefs.edit { putBoolean("music_on", it) }
                            },
                            onSoundEffectsToggle = {
                                soundEffectsEnabled = it
                                prefs.edit { putBoolean("sfx_on", it) }
                            },
                            onHapticsToggle = {
                                hapticsEnabled = it
                                prefs.edit { putBoolean("haptics_on", it) }
                                if (it) performHaptic(context, HapticType.MEDIUM)
                            },
                            onPortalWallsToggle = {
                                portalWallsEnabled = it
                                prefs.edit { putBoolean("portal_walls", it) }
                            },
                            onDifficultySelected = {
                                currentDifficulty = it
                                prefs.edit { putString("difficulty", it.name) }
                            },
                            onDarkModeToggle = {
                                darkModeEnabled = it
                                prefs.edit { putBoolean("dark_mode", it) }
                            },
                            onVibrationToggle = {
                                vibrationEnabled = it
                                prefs.edit { putBoolean("vibration", it) }
                                if (it) performHaptic(context, HapticType.MEDIUM)
                            },
                            onSwipeSensitivityChange = {
                                swipeSensitivity = it
                                prefs.edit { putFloat("swipe_sensitivity", it) }
                            },
                            onBackClick = { currentScreen = previousScreen }
                        )
                    }
                }

                // Splash Screen Overlay
                AnimatedVisibility(
                    visible = showSplash,
                    enter = fadeIn(),
                    exit = fadeOut(tween(350))
                ) {
                    val splashPalette = CitrusTheme.palette
                    val splashBarBg = splashPalette.tomatoCoral.copy(alpha = 0.2f)
                    val splashBarGradient = listOf(splashPalette.tomatoCoral, splashPalette.warmGoldenYellow)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(splashPalette.background)
                            .pointerInput(Unit) { detectTapGestures { } },
                        contentAlignment = Alignment.Center
                    ) {
                        CitrusBackgroundDecorations()

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CitrusSerpentineLogo()

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = APP_TAGLINE,
                                style = CitrusTypography.Subtitle,
                                color = splashPalette.textPrimary.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(36.dp))

                            val p = splashProgress.value.coerceIn(0f, 1f)
                            Canvas(
                                modifier = Modifier
                                    .width(220.dp)
                                    .height(10.dp)
                            ) {
                                val r = CornerRadius(size.height / 2f, size.height / 2f)
                                drawRoundRect(
                                    color = splashBarBg,
                                    size = size,
                                    cornerRadius = r
                                )
                                if (p > 0f) {
                                    drawRoundRect(
                                        brush = Brush.horizontalGradient(splashBarGradient),
                                        size = Size(size.width * p, size.height),
                                        cornerRadius = r
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Loading ${(p * 100).toInt()}%",
                                color = splashPalette.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}