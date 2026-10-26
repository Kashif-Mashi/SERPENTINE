package com.example.snakegame

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import kotlin.math.abs

@Composable
fun GameplayScreen(
    currentTheme: GameTheme,
    score: Int,
    highScore: Int,
    snake: List<Point>,
    currentDirection: Direction,
    currentFood: Food,
    obstacles: Set<Point>,
    particles: List<Particle>,
    floatingTexts: List<FloatingText>,
    isSpeedBoosted: Boolean,
    isGhostActive: Boolean = false,
    isInvulnerable: Boolean = false,
    comboCount: Int = 0,
    pulseScale: Float,
    frameTick: Long,
    gridWidth: Int,
    gridHeight: Int,
    swipeSensitivity: Float,
    portalWallsEnabled: Boolean,
    darkModeEnabled: Boolean,
    onDirectionChange: (Direction) -> Unit,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    val palette = CitrusTheme.palette
    val effectiveBg = if (darkModeEnabled && currentTheme == GameTheme.CITRUS_POP) palette.background else currentTheme.background
    val effectiveText = if (darkModeEnabled && currentTheme == GameTheme.CITRUS_POP) palette.textPrimary else currentTheme.textPrimary
    val effectiveSurface = if (darkModeEnabled && currentTheme == GameTheme.CITRUS_POP) palette.surface else currentTheme.surfaceUi
    val effectiveCheckerLight = if (darkModeEnabled && currentTheme == GameTheme.CITRUS_POP) palette.checkerLight else currentTheme.checkerLight
    val effectiveCheckerDark = if (darkModeEnabled && currentTheme == GameTheme.CITRUS_POP) palette.checkerDark else currentTheme.checkerDark

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(effectiveBg)
    ) {
        // Decorative Citrus Shapes in corners
        CitrusBackgroundDecorations()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Bar: Pause Button | Score & Best | Apple Counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause Button (Coral circular button with ||)
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onPauseClick() }
                        .shadow(3.dp, shape = CircleShape),
                    shape = CircleShape,
                    color = CitrusColors.TomatoCoral
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(16.dp)) {
                            val barW = 3.5.dp.toPx()
                            val h = size.height * 0.75f
                            val topY = size.height * 0.125f
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(size.width * 0.22f, topY),
                                size = Size(barW, h),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(size.width * 0.62f, topY),
                                size = Size(barW, h),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }
                }

                // Center Score & Best
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Score",
                                color = effectiveText.copy(alpha = 0.65f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (comboCount >= 2) {
                                Spacer(modifier = Modifier.width(4.dp))
                                val multiplier = if (comboCount >= 5) "x3" else if (comboCount >= 3) "x2" else ""
                                Surface(
                                    shape = CitrusShapes.Pill,
                                    color = CitrusColors.TomatoCoral
                                ) {
                                    Text(
                                        text = if (multiplier.isNotEmpty()) "x$comboCount ($multiplier)" else "x$comboCount",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "$score",
                            color = effectiveText,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Best",
                            color = effectiveText.copy(alpha = 0.65f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$highScore",
                            color = CitrusColors.DeepCobalt,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Apple Counter + Optional Portal Walls badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (portalWallsEnabled) {
                        Surface(
                            shape = CitrusShapes.Pill,
                            color = CitrusColors.CitrusLime.copy(alpha = 0.25f),
                            border = BorderStroke(1.2.dp, CitrusColors.CitrusLime)
                        ) {
                            Text(
                                text = "🌀",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.shadow(2.dp, shape = CitrusShapes.Pill),
                        shape = CitrusShapes.Pill,
                        color = effectiveSurface,
                        border = BorderStroke(1.2.dp, currentTheme.primaryUi.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Canvas(modifier = Modifier.size(18.dp)) {
                                val r = size.width * 0.42f
                                val cx = size.width * 0.48f
                                val cy = size.height * 0.58f
                                drawCircle(color = currentTheme.normalFood, radius = r, center = Offset(cx, cy))
                                drawCircle(color = Color.White.copy(alpha = 0.65f), radius = r * 0.28f, center = Offset(cx - r * 0.35f, cy - r * 0.35f))
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
                                text = "${score.coerceAtLeast(0)}",
                                color = effectiveText,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val density = LocalDensity.current
            // Ultra-fast zero-touch-slop swipe threshold in pixels
            // swipeSensitivity ranges 0.1f (gentle) to 1.0f (hyper)
            // At default 0.85f: (13.dp - 0.85 * 7.dp) ≈ 7.05.dp (only ~18-20px on high-DPI screens!)
            val swipeThresholdPx = with(density) {
                (13f - (swipeSensitivity.coerceIn(0.1f, 1f) * 7f)).dp.toPx()
            }
            val fastFlickPx = swipeThresholdPx * 0.70f

            val swipeModifier = Modifier.pointerInput(swipeSensitivity, onDirectionChange) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var lastX = down.position.x
                    var lastY = down.position.y
                    var dragX = 0f
                    var dragY = 0f

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (!change.pressed) {
                            // Quick flick on finger lift
                            val absX = abs(dragX)
                            val absY = abs(dragY)
                            if (absX >= fastFlickPx || absY >= fastFlickPx) {
                                if (absX > absY) {
                                    if (dragX > 0) onDirectionChange(Direction.RIGHT)
                                    else onDirectionChange(Direction.LEFT)
                                } else {
                                    if (dragY > 0) onDirectionChange(Direction.DOWN)
                                    else onDirectionChange(Direction.UP)
                                }
                            }
                            break
                        }

                        val curX = change.position.x
                        val curY = change.position.y
                        val dx = curX - lastX
                        val dy = curY - lastY
                        lastX = curX
                        lastY = curY

                        // Instant Reversal Tracking:
                        // When transitioning from left-to-right, right-to-left, top-to-bottom, bottom-to-top,
                        // reset drag accumulation on direction reversal so there is no deadzone lag.
                        if ((dx > 0 && dragX < 0) || (dx < 0 && dragX > 0)) {
                            dragX = dx
                        } else {
                            dragX += dx
                        }

                        if ((dy > 0 && dragY < 0) || (dy < 0 && dragY > 0)) {
                            dragY = dy
                        } else {
                            dragY += dy
                        }

                        val absX = abs(dragX)
                        val absY = abs(dragY)

                        if (absX >= swipeThresholdPx || absY >= swipeThresholdPx) {
                            if (absX > absY) {
                                if (dragX > 0) onDirectionChange(Direction.RIGHT)
                                else onDirectionChange(Direction.LEFT)
                            } else {
                                if (dragY > 0) onDirectionChange(Direction.DOWN)
                                else onDirectionChange(Direction.UP)
                            }
                            change.consume()
                            dragX = 0f
                            dragY = 0f
                        }
                    }
                }
            }

            // 2. Hardware-accelerated Canvas Snake Playfield
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
                    .then(swipeModifier),
                contentAlignment = Alignment.Center
            ) {
                val borderColor = if (portalWallsEnabled) {
                    CitrusColors.WarmGoldenYellow
                } else {
                    CitrusColors.DeepCobalt.copy(alpha = 0.55f)
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(gridWidth.toFloat() / gridHeight.toFloat())
                        .fillMaxSize()
                        .clip(RoundedCornerShape(26.dp))
                        .border(
                            width = if (portalWallsEnabled) 4.5.dp else 4.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(26.dp)
                        )
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (frameTick < 0L) return@Canvas

                        val cellWidth = size.width / gridWidth
                        val cellHeight = size.height / gridHeight

                        // A. Two-Tone Checkerboard Grid
                        for (x in 0 until gridWidth) {
                            for (y in 0 until gridHeight) {
                                val isEven = (x + y) % 2 == 0
                                drawRect(
                                    color = if (isEven) effectiveCheckerLight else effectiveCheckerDark,
                                    topLeft = Offset(x * cellWidth, y * cellHeight),
                                    size = Size(cellWidth, cellHeight)
                                )
                            }
                        }

                        // Subtle grid lines
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

                        // B. Obstacles: Smooth Rounded Pebbles
                        for ((ox, oy) in obstacles) {
                            val stoneCenterX = ox * cellWidth + cellWidth / 2f
                            val stoneCenterY = oy * cellHeight + cellHeight / 2f
                            val stoneR = (cellWidth.coerceAtMost(cellHeight) / 2f) - 2f

                            drawCircle(
                                color = Color.Black.copy(alpha = 0.22f),
                                radius = stoneR * 0.95f,
                                center = Offset(stoneCenterX + 1.5f, stoneCenterY + 2.5f)
                            )
                            drawRoundRect(
                                color = currentTheme.obstacle,
                                topLeft = Offset(stoneCenterX - stoneR, stoneCenterY - stoneR * 0.85f),
                                size = Size(stoneR * 2f, stoneR * 1.7f),
                                cornerRadius = CornerRadius(stoneR * 0.65f, stoneR * 0.65f)
                            )
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.35f),
                                topLeft = Offset(stoneCenterX - stoneR * 0.65f, stoneCenterY - stoneR * 0.65f),
                                size = Size(stoneR * 0.9f, stoneR * 0.55f),
                                cornerRadius = CornerRadius(stoneR * 0.35f, stoneR * 0.35f)
                            )
                        }

                        // C. Food Pellets
                        val foodCenterX = currentFood.position.x * cellWidth + cellWidth / 2f
                        val foodCenterY = currentFood.position.y * cellHeight + cellHeight / 2f
                        val baseRadius = (cellWidth.coerceAtMost(cellHeight) / 2f) - 3f

                        when (currentFood.type) {
                            FoodType.NORMAL -> {
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.22f),
                                    radius = baseRadius * 0.9f,
                                    center = Offset(foodCenterX + 1.5f, foodCenterY + 2.5f)
                                )
                                drawCircle(
                                    color = currentTheme.normalFood,
                                    radius = baseRadius,
                                    center = Offset(foodCenterX, foodCenterY)
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.72f),
                                    radius = baseRadius * 0.30f,
                                    center = Offset(foodCenterX - baseRadius * 0.35f, foodCenterY - baseRadius * 0.35f)
                                )
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
                                val spColor = CitrusColors.WarmGoldenYellow
                                val spSize = 3f * pulseScale
                                drawCircle(spColor, spSize, Offset(foodCenterX - pulseR * 1.3f, foodCenterY - pulseR * 0.6f))
                                drawCircle(spColor, spSize * 1.2f, Offset(foodCenterX + pulseR * 1.35f, foodCenterY - pulseR * 0.5f))

                                drawCircle(
                                    color = currentTheme.goldenFood,
                                    radius = pulseR,
                                    center = Offset(foodCenterX, foodCenterY)
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.85f),
                                    radius = pulseR * 0.32f,
                                    center = Offset(foodCenterX - pulseR * 0.35f, foodCenterY - pulseR * 0.35f)
                                )
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
                            FoodType.GHOST -> {
                                val pulseR = baseRadius * pulseScale
                                drawCircle(
                                    color = currentTheme.ghostFood.copy(alpha = 0.35f),
                                    radius = pulseR * 1.35f,
                                    center = Offset(foodCenterX, foodCenterY)
                                )
                                drawCircle(
                                    color = currentTheme.ghostFood,
                                    radius = pulseR,
                                    center = Offset(foodCenterX, foodCenterY)
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.65f),
                                    radius = pulseR * 0.45f,
                                    center = Offset(foodCenterX, foodCenterY)
                                )
                                val eyeR = pulseR * 0.22f
                                val pupilR = eyeR * 0.55f
                                val eyeLeft = Offset(foodCenterX - pulseR * 0.30f, foodCenterY - pulseR * 0.12f)
                                val eyeRight = Offset(foodCenterX + pulseR * 0.30f, foodCenterY - pulseR * 0.12f)
                                drawCircle(color = Color.White, radius = eyeR, center = eyeLeft)
                                drawCircle(color = Color.White, radius = eyeR, center = eyeRight)
                                drawCircle(color = Color(0xFF1E1035), radius = pupilR, center = eyeLeft)
                                drawCircle(color = Color(0xFF1E1035), radius = pupilR, center = eyeRight)
                            }
                        }

                        // D. Smooth Continuous Tube Snake
                        val tubeRadius = (cellWidth.coerceAtMost(cellHeight) / 2f) * 0.88f
                        val ghostAlphaFactor = if (isGhostActive) 0.38f else if (isInvulnerable) 0.60f else 1f

                        for (i in 0 until snake.size - 1) {
                            val segA = snake[i]
                            val segB = snake[i + 1]
                            if (abs(segA.x - segB.x) <= 1 && abs(segA.y - segB.y) <= 1) {
                                val pA = Offset(segA.x * cellWidth + cellWidth / 2f, segA.y * cellHeight + cellHeight / 2f)
                                val pB = Offset(segB.x * cellWidth + cellWidth / 2f, segB.y * cellHeight + cellHeight / 2f)
                                val progress = 1f - (i.toFloat() / snake.size)
                                val segColor = currentTheme.snakeBody.copy(alpha = (0.85f + 0.15f * progress) * ghostAlphaFactor)
                                drawLine(
                                    color = segColor,
                                    start = pA,
                                    end = pB,
                                    strokeWidth = tubeRadius * 2f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        // Segments discs & Head Details
                        snake.forEachIndexed { index, segment ->
                            val cx = segment.x * cellWidth + cellWidth / 2f
                            val cy = segment.y * cellHeight + cellHeight / 2f
                            val isTail = index == snake.size - 1
                            val r = if (isTail) tubeRadius * 0.72f else tubeRadius

                            if (index == 0) {
                                drawCircle(
                                    color = currentTheme.snakeHead.copy(alpha = 0.25f * ghostAlphaFactor),
                                    radius = tubeRadius * 1.4f,
                                    center = Offset(cx, cy)
                                )
                                drawCircle(color = currentTheme.snakeHead.copy(alpha = ghostAlphaFactor), radius = tubeRadius, center = Offset(cx, cy))

                                if (isInvulnerable) {
                                    drawCircle(
                                        color = CitrusColors.WarmGoldenYellow.copy(alpha = 0.85f),
                                        radius = tubeRadius * 1.55f,
                                        center = Offset(cx, cy),
                                        style = Stroke(width = 3.dp.toPx())
                                    )
                                }

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

                                drawCircle(color = Color.White.copy(alpha = ghostAlphaFactor), radius = eyeRadius, center = eye1Offset)
                                drawCircle(color = Color.White.copy(alpha = ghostAlphaFactor), radius = eyeRadius, center = eye2Offset)
                                drawCircle(color = Color(0xFF202747).copy(alpha = ghostAlphaFactor), radius = pupilRadius, center = eye1Offset)
                                drawCircle(color = Color(0xFF202747).copy(alpha = ghostAlphaFactor), radius = pupilRadius, center = eye2Offset)
                                drawCircle(color = Color.White.copy(alpha = ghostAlphaFactor), radius = pupilRadius * 0.4f, center = Offset(eye1Offset.x - 1f, eye1Offset.y - 1f))
                                drawCircle(color = Color.White.copy(alpha = ghostAlphaFactor), radius = pupilRadius * 0.4f, center = Offset(eye2Offset.x - 1f, eye2Offset.y - 1f))

                                val mouthOffset = when (currentDirection) {
                                    Direction.RIGHT -> Offset(cx + tubeRadius * 0.55f, cy)
                                    Direction.LEFT -> Offset(cx - tubeRadius * 0.55f, cy)
                                    Direction.UP -> Offset(cx, cy - tubeRadius * 0.55f)
                                    Direction.DOWN -> Offset(cx, cy + tubeRadius * 0.55f)
                                }
                                drawCircle(color = Color(0xFF202747).copy(alpha = ghostAlphaFactor), radius = 2.5f, center = mouthOffset)
                            } else {
                                val progress = 1f - (index.toFloat() / snake.size)
                                val bodyColor = currentTheme.snakeBody.copy(alpha = (0.85f + 0.15f * progress) * ghostAlphaFactor)
                                drawCircle(color = bodyColor, radius = r, center = Offset(cx, cy))
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.20f * ghostAlphaFactor),
                                    radius = r * 0.35f,
                                    center = Offset(cx - r * 0.3f, cy - r * 0.3f)
                                )
                            }
                        }

                        // E. Dynamic Burst Particles
                        for ((px, py, _, _, pColor, pAlpha, pRadius) in particles) {
                            drawCircle(
                                color = pColor.copy(alpha = pAlpha.coerceIn(0f, 1f)),
                                radius = pRadius,
                                center = Offset(px * cellWidth + cellWidth / 2, py * cellHeight + cellHeight / 2)
                            )
                        }

                        // F. Floating Score Text
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

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Power-Up Card or Swipe Steer Hint
            if (isGhostActive) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, shape = CitrusShapes.CardMedium)
                        .then(swipeModifier),
                    shape = CitrusShapes.CardMedium,
                    color = effectiveSurface,
                    border = BorderStroke(1.5.dp, Color(0xFF9C27B0).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape,
                            color = Color(0xFF7E57C2)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "👻", fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Ghost Mode",
                                color = effectiveText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Pass through body & stones safely!",
                                color = CitrusColors.SecondaryMutedText,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else if (isSpeedBoosted) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, shape = CitrusShapes.CardMedium)
                        .then(swipeModifier),
                    shape = CitrusShapes.CardMedium,
                    color = effectiveSurface,
                    border = BorderStroke(1.5.dp, CitrusColors.DeepCobalt.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape,
                            color = CitrusColors.DeepCobalt
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "⚡", color = CitrusColors.WarmGoldenYellow, fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Speed Boost",
                                color = effectiveText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Get bonus speed for a short time!",
                                color = CitrusColors.SecondaryMutedText,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, shape = CitrusShapes.Pill)
                        .then(swipeModifier),
                    shape = CitrusShapes.Pill,
                    color = effectiveSurface.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, CitrusColors.SoftCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (portalWallsEnabled) "Portal Walls Active: Pass through boundaries safely! 🌀" else "Swipe anywhere on board to steer",
                            color = if (portalWallsEnabled) CitrusColors.TomatoCoral else CitrusColors.SecondaryMutedText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
