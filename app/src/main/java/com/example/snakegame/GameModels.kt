package com.example.snakegame

import androidx.compose.ui.graphics.Color

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

enum class AppScreen {
    MAIN_MENU,
    THEME_SELECT,
    GAMEPLAY,
    PAUSED,
    GAME_OVER,
    SHOP,
    LEADERBOARD,
    SETTINGS
}

enum class FoodType(val points: Int, val durationMs: Long = 0L) {
    NORMAL(points = 1),
    GOLDEN(points = 5, durationMs = 8500L),
    SPEED_BOOST(points = 2, durationMs = 6000L),
    GHOST(points = 2, durationMs = 5000L)
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
    EASY("Easy", 210L, 110L, 3L, 14),
    MEDIUM("Normal", 160L, 75L, 4L, 8),
    HARD("Hard", 115L, 45L, 5L, 5)
}

enum class GameTheme(
    val themeName: String,
    val tagline: String,
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
    val checkerDark: Color,
    val coinPrice: Int = 0,
    val ghostFood: Color = Color(0xFFB388FF)
) {
    CITRUS_POP(
        themeName = "Citrus Pop",
        tagline = "Bright. Fun. Energetic.",
        background = Color(0xFFF5E9CC),
        gridLine = Color(0x1A293B8F),
        snakeHead = Color(0xFF86D62E),
        snakeBody = Color(0xFF72B822),
        normalFood = Color(0xFFF05B42),
        goldenFood = Color(0xFFFFD34E),
        speedFood = Color(0xFF293B8F),
        obstacle = Color(0xFF5A6660),
        obstacleGlow = Color(0x33293B8F),
        primaryUi = Color(0xFFF05B42),
        surfaceUi = Color(0xFFFFF8E8),
        textPrimary = Color(0xFF202747),
        checkerLight = Color(0xFFFAEFD9),
        checkerDark = Color(0xFFEFE1C3),
        coinPrice = 0
    ),
    SUNSET_ARCADE(
        themeName = "Sunset Arcade",
        tagline = "Warm. Retro. Stylish.",
        background = Color(0xFF2E1933),
        gridLine = Color(0x22FFA066),
        snakeHead = Color(0xFFFF7A59),
        snakeBody = Color(0xFFDE5268),
        normalFood = Color(0xFFFFD15C),
        goldenFood = Color(0xFFFF9E44),
        speedFood = Color(0xFF00E5FF),
        obstacle = Color(0xFF5A3668),
        obstacleGlow = Color(0x33FF7A59),
        primaryUi = Color(0xFFFF7A59),
        surfaceUi = Color(0xFF43254B),
        textPrimary = Color(0xFFFFEBF0),
        checkerLight = Color(0xFF3D2144),
        checkerDark = Color(0xFF331B3A),
        coinPrice = 120
    ),
    BUBBLEGUM_STUDIO(
        themeName = "Bubblegum Studio",
        tagline = "Playful. Colorful. Bold.",
        background = Color(0xFFFDE8F1),
        gridLine = Color(0x22FF69B4),
        snakeHead = Color(0xFFFF5288),
        snakeBody = Color(0xFF4ACFD9),
        normalFood = Color(0xFFFF1493),
        goldenFood = Color(0xFFFFCA28),
        speedFood = Color(0xFF7C4DFF),
        obstacle = Color(0xFF8C9BAE),
        obstacleGlow = Color(0x33FF5288),
        primaryUi = Color(0xFFFF5288),
        surfaceUi = Color(0xFFFFFFFF),
        textPrimary = Color(0xFF291B2D),
        checkerLight = Color(0xFFFFF4F8),
        checkerDark = Color(0xFFF9DCED),
        coinPrice = 100
    ),
    PAPER_AND_INK(
        themeName = "Paper & Ink",
        tagline = "Minimal. Clean. Classic.",
        background = Color(0xFFEDE8DD),
        gridLine = Color(0x222C3437),
        snakeHead = Color(0xFF2B3336),
        snakeBody = Color(0xFF4F5C60),
        normalFood = Color(0xFFD32F2F),
        goldenFood = Color(0xFFCC9900),
        speedFood = Color(0xFF1976D2),
        obstacle = Color(0xFF7E898C),
        obstacleGlow = Color(0x222B3336),
        primaryUi = Color(0xFF2B3336),
        surfaceUi = Color(0xFFFAF7F0),
        textPrimary = Color(0xFF1B2022),
        checkerLight = Color(0xFFF5F1E8),
        checkerDark = Color(0xFFE4DDD0),
        coinPrice = 100
    ),
    AQUA_CARNIVAL(
        themeName = "Aqua Carnival",
        tagline = "Fresh. Oceanic. Vivid.",
        background = Color(0xFFE0F7FA),
        gridLine = Color(0x2200838F),
        snakeHead = Color(0xFF00BCD4),
        snakeBody = Color(0xFF0097A7),
        normalFood = Color(0xFFFF5252),
        goldenFood = Color(0xFFFFD740),
        speedFood = Color(0xFFE040FB),
        obstacle = Color(0xFF455A64),
        obstacleGlow = Color(0x3300BCD4),
        primaryUi = Color(0xFF00ACC1),
        surfaceUi = Color(0xFFFFFFFF),
        textPrimary = Color(0xFF00363A),
        checkerLight = Color(0xFFE8FBFD),
        checkerDark = Color(0xFFCEF1F5),
        coinPrice = 100
    ),
    // Preserved classic gameplay themes
    MATRIX_NEON(
        themeName = "Matrix Neon",
        tagline = "Obsidian green terminal glow.",
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
        checkerDark = Color(0xFF133C29),
        coinPrice = 80
    ),
    CYBERPUNK_PINK(
        themeName = "Cyberpunk Pink",
        tagline = "Synthwave neon glow with purple.",
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
        checkerDark = Color(0xFF330E3F),
        coinPrice = 80
    ),
    RETRO_GAMEBOY(
        themeName = "Retro GameBoy",
        tagline = "90s handheld olive matrix.",
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
        checkerDark = Color(0xFF8B956D),
        coinPrice = 80
    ),
    MIDNIGHT_BLUE(
        themeName = "Midnight Blue",
        tagline = "Deep space electric cyan.",
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
        checkerDark = Color(0xFF0B2644),
        coinPrice = 80
    )
}

enum class ShopCategory {
    THEMES,
    SNAKES,
    EFFECTS
}

data class ShopItem(
    val id: String,
    val name: String,
    val category: ShopCategory,
    val price: Int,
    val description: String,
    val theme: GameTheme? = null
)

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val score: Int,
    val isCurrentPlayer: Boolean = false
)
