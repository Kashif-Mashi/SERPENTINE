package com.example.snakegame

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==========================================
// 1. REUSABLE BUTTONS
// ==========================================

@Composable
fun SerpentinePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = CitrusColors.TomatoCoral,
    contentColor: Color = Color.White,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnScale"
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = CitrusShapes.Pill,
                ambientColor = CitrusColors.TomatoCoral.copy(alpha = 0.35f),
                spotColor = CitrusColors.TomatoCoral.copy(alpha = 0.5f)
            ),
        shape = CitrusShapes.Pill,
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = contentColor,
                style = CitrusTypography.ButtonText
            )
        }
    }
}

@Composable
fun SerpentineSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = CitrusColors.SoftCardBg,
    contentColor: Color = CitrusColors.DeepCobalt,
    borderColor: Color = CitrusColors.SoftCardBorder,
    icon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "secBtnScale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 1.dp else 3.dp,
                shape = CitrusShapes.Pill,
                ambientColor = CitrusColors.SubtleShadow,
                spotColor = CitrusColors.SubtleShadow
            ),
        shape = CitrusShapes.Pill,
        color = containerColor,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                color = contentColor,
                style = CitrusTypography.BodyBold.copy(fontSize = 15.sp)
            )
        }
    }
}

// ==========================================
// 2. CARDS & CONTAINERS
// ==========================================

@Composable
fun SerpentineCard(
    modifier: Modifier = Modifier,
    containerColor: Color = CitrusColors.SoftCardBg,
    borderColor: Color = CitrusColors.SoftCardBorder,
    elevation: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.shadow(
            elevation = elevation,
            shape = CitrusShapes.CardLarge,
            ambientColor = CitrusColors.SubtleShadow,
            spotColor = CitrusColors.SubtleShadow
        ),
        shape = CitrusShapes.CardLarge,
        color = containerColor,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            content = content
        )
    }
}

// ==========================================
// 3. PILLS & STATUS INDICATORS
// ==========================================

@Composable
fun CoinBalance(
    coins: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .shadow(2.dp, shape = CitrusShapes.Pill),
        shape = CitrusShapes.Pill,
        color = CitrusColors.SoftCardBg,
        border = BorderStroke(1.5.dp, CitrusColors.WarmGoldenYellow)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Canvas(modifier = Modifier.size(18.dp)) {
                // Gold Coin Outer
                drawCircle(color = CitrusColors.WarmGoldenYellow, radius = size.width / 2f)
                drawCircle(color = CitrusColors.WarmGoldenDark, radius = size.width * 0.42f, style = Stroke(1.5f))
                // Inner star / shine
                drawCircle(color = Color.White.copy(alpha = 0.7f), radius = size.width * 0.16f, center = Offset(size.width * 0.38f, size.height * 0.38f))
            }
            Text(
                text = "$coins",
                color = CitrusColors.DeepInk,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ScorePill(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    accentColor: Color = CitrusColors.DeepCobalt
) {
    Surface(
        modifier = modifier.shadow(2.dp, shape = CitrusShapes.Pill),
        shape = CitrusShapes.Pill,
        color = CitrusColors.SoftCardBg,
        border = BorderStroke(1.2.dp, CitrusColors.SoftCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = CitrusColors.SecondaryMutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "$value",
                color = accentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun GameStatCard(
    score: Int,
    bestScore: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.shadow(4.dp, shape = CitrusShapes.CardMedium),
        shape = CitrusShapes.CardMedium,
        color = CitrusColors.SoftCardBg,
        border = BorderStroke(1.5.dp, CitrusColors.SoftCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score Column
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Score",
                    color = CitrusColors.SecondaryMutedText,
                    style = CitrusTypography.Caption
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$score",
                    color = CitrusColors.DeepInk,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                )
            }

            // Vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(44.dp)
                    .background(CitrusColors.SoftCardBorder)
            )

            // Best Score Column
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Best",
                    color = CitrusColors.SecondaryMutedText,
                    style = CitrusTypography.Caption
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$bestScore",
                    color = CitrusColors.DeepCobalt,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                )
            }
        }
    }
}

// ==========================================
// 4. THEME PREVIEW CARD
// ==========================================

@Composable
fun ThemePreviewCard(
    theme: GameTheme,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .shadow(
                elevation = if (isSelected) 5.dp else 2.dp,
                shape = CitrusShapes.CardMedium
            ),
        shape = CitrusShapes.CardMedium,
        color = CitrusColors.SoftCardBg,
        border = BorderStroke(
            width = if (isSelected) 2.5.dp else 1.2.dp,
            color = if (isSelected) CitrusColors.WarmGoldenYellow else CitrusColors.SoftCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Theme Thumbnail Preview
            Surface(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CitrusShapes.CardSmall),
                shape = CitrusShapes.CardSmall,
                color = theme.background,
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Mini checkerboard
                    val cells = 4
                    val cw = size.width / cells
                    val ch = size.height / cells
                    for (x in 0 until cells) {
                        for (y in 0 until cells) {
                            drawRect(
                                color = if ((x + y) % 2 == 0) theme.checkerLight else theme.checkerDark,
                                topLeft = Offset(x * cw, y * ch),
                                size = Size(cw, ch)
                            )
                        }
                    }
                    // Mini stylized curved snake
                    val sPath = Path().apply {
                        moveTo(cw * 0.9f, ch * 3.1f)
                        cubicTo(cw * 1.5f, ch * 1.3f, cw * 2.3f, ch * 3.0f, cw * 3.1f, ch * 1.6f)
                    }
                    drawPath(
                        path = sPath,
                        color = theme.snakeHead,
                        style = Stroke(width = cw * 0.75f, cap = StrokeCap.Round)
                    )
                    // Mini food
                    drawCircle(
                        color = theme.normalFood,
                        radius = cw * 0.40f,
                        center = Offset(cw * 3.2f, ch * 1.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Center: Name & Tagline
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.themeName,
                    color = CitrusColors.DeepInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = theme.tagline,
                    color = CitrusColors.SecondaryMutedText,
                    fontSize = 12.sp
                )
            }

            // Right: Selected Checkmark
            if (isSelected) {
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = CitrusColors.TomatoCoral
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. SETTINGS & LEADERBOARD ROWS
// ==========================================

@Composable
fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (icon != null) {
                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    icon()
                }
            }
            Text(
                text = title,
                color = CitrusColors.DeepInk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CitrusColors.SuccessGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFC7CBD1)
            )
        )
    }
}

@Composable
fun LeaderboardRow(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier
) {
    val isTop3 = entry.rank in 1..3
    val rankBadgeColor = when (entry.rank) {
        1 -> Color(0xFFFFD34E)
        2 -> Color(0xFFC9D1D9)
        3 -> Color(0xFFE28854)
        else -> Color.Transparent
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (entry.isCurrentPlayer) 3.dp else 1.dp,
                shape = CitrusShapes.CardSmall
            ),
        shape = CitrusShapes.CardSmall,
        color = if (entry.isCurrentPlayer) CitrusColors.WarmGoldenYellow.copy(alpha = 0.35f) else CitrusColors.SoftCardBg,
        border = BorderStroke(
            width = if (entry.isCurrentPlayer) 1.5.dp else 1.dp,
            color = if (entry.isCurrentPlayer) CitrusColors.WarmGoldenYellow else CitrusColors.SoftCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge or Number
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isTop3) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = rankBadgeColor)
                    }
                    Text(
                        text = "${entry.rank}",
                        color = CitrusColors.DeepInk,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = "${entry.rank}",
                        color = CitrusColors.SecondaryMutedText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Cute Player Avatar Icon
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = when (entry.rank % 4) {
                    0 -> Color(0xFFFF8A65)
                    1 -> Color(0xFF81D4FA)
                    2 -> Color(0xFFA5D6A7)
                    else -> Color(0xFFFFCC80)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(22.dp)) {
                        // Smiley face avatar
                        val c = Color(0xFF263238)
                        drawCircle(color = c, radius = 2f, center = Offset(size.width * 0.35f, size.height * 0.4f))
                        drawCircle(color = c, radius = 2f, center = Offset(size.width * 0.65f, size.height * 0.4f))
                        drawArc(
                            color = c,
                            startAngle = 10f,
                            sweepAngle = 160f,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.25f, size.height * 0.45f),
                            size = Size(size.width * 0.5f, size.height * 0.35f),
                            style = Stroke(2f, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Player Name
            Text(
                text = entry.name,
                color = CitrusColors.DeepInk,
                fontWeight = if (entry.isCurrentPlayer) FontWeight.Black else FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )

            // Score
            Text(
                text = "${entry.score}",
                color = if (entry.isCurrentPlayer) CitrusColors.DeepCobalt else CitrusColors.DeepInk,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        color = CitrusColors.DeepCobalt,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = modifier.padding(vertical = 4.dp)
    )
}

// ==========================================
// 6. SERPENTINE LOGO WITH CROWN
// ==========================================

@Composable
fun CitrusSerpentineLogo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating Golden Crown
        Canvas(modifier = Modifier.size(34.dp, 20.dp)) {
            val crownPath = Path().apply {
                moveTo(size.width * 0.15f, size.height * 0.85f)
                lineTo(size.width * 0.05f, size.height * 0.25f)
                lineTo(size.width * 0.35f, size.height * 0.55f)
                lineTo(size.width * 0.50f, size.height * 0.10f)
                lineTo(size.width * 0.65f, size.height * 0.55f)
                lineTo(size.width * 0.95f, size.height * 0.25f)
                lineTo(size.width * 0.85f, size.height * 0.85f)
                close()
            }
            drawPath(crownPath, color = CitrusColors.WarmGoldenYellow, style = Fill)
            drawPath(crownPath, color = CitrusColors.WarmGoldenDark, style = Stroke(width = 2.5f, join = StrokeJoin.Round))

            // Crown jewels
            drawCircle(Color.White, radius = 2.2f, center = Offset(size.width * 0.05f, size.height * 0.25f))
            drawCircle(Color.White, radius = 2.8f, center = Offset(size.width * 0.50f, size.height * 0.10f))
            drawCircle(Color.White, radius = 2.2f, center = Offset(size.width * 0.95f, size.height * 0.25f))
        }

        Spacer(modifier = Modifier.height(2.dp))

        // SERPENTINE title in bold curved deep cobalt typography
        Text(
            text = "SERPENTINE",
            color = CitrusColors.DeepCobalt,
            style = CitrusTypography.DisplayLarge,
            letterSpacing = 2.sp
        )
    }
}

// ==========================================
// 7. CUTE CARTOON SNAKE MASCOT (CANVAS)
// ==========================================

@Composable
fun CitrusMascot(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 190.dp
) {
    // Subtle breathing / floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "mascotFloat")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )
    val tongueFlicker by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tongueFlicker"
    )

    Box(
        modifier = modifier
            .size(sizeDp)
            .offset(y = floatY.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Warm Golden / Coral Circular Backdrop
            val bgCenter = Offset(w * 0.5f, h * 0.52f)
            val bgRadius = w * 0.44f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CitrusColors.WarmGoldenYellow,
                        CitrusColors.TomatoCoral
                    ),
                    center = bgCenter,
                    radius = bgRadius
                ),
                radius = bgRadius,
                center = bgCenter
            )

            // Sparkle stars around backdrop
            fun drawStar(cx: Float, cy: Float, s: Float) {
                val starPath = Path().apply {
                    moveTo(cx, cy - s)
                    lineTo(cx + s * 0.25f, cy - s * 0.25f)
                    lineTo(cx + s, cy)
                    lineTo(cx + s * 0.25f, cy + s * 0.25f)
                    lineTo(cx, cy + s)
                    lineTo(cx - s * 0.25f, cy + s * 0.25f)
                    lineTo(cx - s, cy)
                    lineTo(cx - s * 0.25f, cy - s * 0.25f)
                    close()
                }
                drawPath(starPath, color = CitrusColors.WarmGoldenYellow, style = Fill)
            }
            drawStar(w * 0.12f, h * 0.32f, w * 0.045f)
            drawStar(w * 0.90f, h * 0.40f, w * 0.055f)
            drawStar(w * 0.84f, h * 0.72f, w * 0.038f)

            // 2. Coiled Cartoon Snake Body (Lime Green with Green Shadows)
            val bodyStroke = w * 0.17f
            val outlineStroke = bodyStroke + 10f

            val snakeCurve = Path().apply {
                // Tail curving at bottom left
                moveTo(w * 0.26f, h * 0.74f)
                cubicTo(
                    w * 0.32f, h * 0.88f,
                    w * 0.65f, h * 0.88f,
                    w * 0.76f, h * 0.74f
                )
                // Curve up through middle
                cubicTo(
                    w * 0.84f, h * 0.62f,
                    w * 0.68f, h * 0.50f,
                    w * 0.50f, h * 0.48f
                )
                cubicTo(
                    w * 0.34f, h * 0.46f,
                    w * 0.36f, h * 0.34f,
                    w * 0.52f, h * 0.28f
                )
            }

            // Dark Cobalt / Ink Outline for bold comic feel
            drawPath(
                path = snakeCurve,
                color = CitrusColors.DeepCobaltDark,
                style = Stroke(width = outlineStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Lime Body Fill
            drawPath(
                path = snakeCurve,
                color = CitrusColors.CitrusLime,
                style = Stroke(width = bodyStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Yellow Belly Segment Stripe
            drawPath(
                path = snakeCurve,
                color = CitrusColors.WarmGoldenYellow,
                style = Stroke(width = bodyStroke * 0.35f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Head Disc
            val headCenter = Offset(w * 0.58f, h * 0.28f)
            val headRadius = w * 0.20f

            // Head outline
            drawCircle(color = CitrusColors.DeepCobaltDark, radius = headRadius + 4f, center = headCenter)
            // Head fill
            drawCircle(color = CitrusColors.CitrusLime, radius = headRadius, center = headCenter)
            // Head bottom yellow belly
            drawArc(
                color = CitrusColors.WarmGoldenYellow,
                startAngle = 30f,
                sweepAngle = 120f,
                useCenter = true,
                topLeft = Offset(headCenter.x - headRadius * 0.85f, headCenter.y - headRadius * 0.2f),
                size = Size(headRadius * 1.7f, headRadius * 1.15f)
            )

            // Cheerful Red Forked Tongue
            val tongueLen = w * 0.12f * tongueFlicker
            val tongueStart = Offset(headCenter.x + headRadius * 0.72f, headCenter.y + headRadius * 0.20f)
            val tonguePath = Path().apply {
                moveTo(tongueStart.x, tongueStart.y)
                lineTo(tongueStart.x + tongueLen * 0.65f, tongueStart.y + 4f)
                lineTo(tongueStart.x + tongueLen, tongueStart.y - 4f)
                moveTo(tongueStart.x + tongueLen * 0.65f, tongueStart.y + 4f)
                lineTo(tongueStart.x + tongueLen, tongueStart.y + 12f)
            }
            drawPath(
                path = tonguePath,
                color = CitrusColors.TomatoCoral,
                style = Stroke(width = 4.5f, cap = StrokeCap.Round)
            )

            // Big expressive cartoon eyes
            val eyeRadius = w * 0.07f
            val eye1Center = Offset(headCenter.x - eyeRadius * 0.6f, headCenter.y - eyeRadius * 0.5f)
            val eye2Center = Offset(headCenter.x + eyeRadius * 0.9f, headCenter.y - eyeRadius * 0.5f)

            // Eye whites with thin black stroke
            drawCircle(Color.White, radius = eyeRadius, center = eye1Center)
            drawCircle(CitrusColors.DeepInk, radius = eyeRadius, center = eye1Center, style = Stroke(2f))
            drawCircle(Color.White, radius = eyeRadius, center = eye2Center)
            drawCircle(CitrusColors.DeepInk, radius = eyeRadius, center = eye2Center, style = Stroke(2f))

            // Big dark pupils looking right/forward
            val pupilRadius = eyeRadius * 0.55f
            drawCircle(CitrusColors.DeepInk, radius = pupilRadius, center = Offset(eye1Center.x + pupilRadius * 0.25f, eye1Center.y))
            drawCircle(CitrusColors.DeepInk, radius = pupilRadius, center = Offset(eye2Center.x + pupilRadius * 0.25f, eye2Center.y))

            // Cute Specular Highlights in eyes
            drawCircle(Color.White, radius = pupilRadius * 0.45f, center = Offset(eye1Center.x, eye1Center.y - pupilRadius * 0.25f))
            drawCircle(Color.White, radius = pupilRadius * 0.45f, center = Offset(eye2Center.x, eye2Center.y - pupilRadius * 0.25f))

            // Rosy Pink Cheeks
            val cheekColor = Color(0xFFFF7E92).copy(alpha = 0.55f)
            drawCircle(cheekColor, radius = eyeRadius * 0.5f, center = Offset(headCenter.x - headRadius * 0.6f, headCenter.y + eyeRadius * 0.6f))
            drawCircle(cheekColor, radius = eyeRadius * 0.5f, center = Offset(headCenter.x + headRadius * 0.55f, headCenter.y + eyeRadius * 0.6f))

            // Happy Smiling Mouth
            val smilePath = Path().apply {
                moveTo(headCenter.x - eyeRadius * 0.2f, headCenter.y + eyeRadius * 0.65f)
                cubicTo(
                    headCenter.x + eyeRadius * 0.3f, headCenter.y + eyeRadius * 1.25f,
                    headCenter.x + eyeRadius * 0.8f, headCenter.y + eyeRadius * 1.0f,
                    headCenter.x + eyeRadius * 0.85f, headCenter.y + eyeRadius * 0.65f
                )
            }
            drawPath(smilePath, color = CitrusColors.DeepInk, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
        }
    }
}

// ==========================================
// 8. DIZZY MASCOT (GAME OVER SCREEN)
// ==========================================

@Composable
fun DizzyCitrusMascot(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 180.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dizzySpin")
    val dizzyAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dizzyAngle"
    )

    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Confetti bursts in background
            val confettiColors = listOf(
                CitrusColors.TomatoCoral,
                CitrusColors.WarmGoldenYellow,
                CitrusColors.CitrusLime,
                CitrusColors.DeepCobalt,
                Color(0xFFFF4081)
            )
            val cPoints = listOf(
                Offset(w * 0.15f, h * 0.15f),
                Offset(w * 0.85f, h * 0.18f),
                Offset(w * 0.22f, h * 0.38f),
                Offset(w * 0.80f, h * 0.42f),
                Offset(w * 0.10f, h * 0.65f),
                Offset(w * 0.88f, h * 0.70f)
            )
            cPoints.forEachIndexed { idx, pt ->
                drawRoundRect(
                    color = confettiColors[idx % confettiColors.size],
                    topLeft = pt,
                    size = Size(w * 0.04f, h * 0.02f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }

            // 2. Coiled Snake Body
            val bodyStroke = w * 0.16f
            val snakeCurve = Path().apply {
                moveTo(w * 0.26f, h * 0.76f)
                cubicTo(w * 0.35f, h * 0.90f, w * 0.65f, h * 0.90f, w * 0.75f, h * 0.76f)
                cubicTo(w * 0.82f, h * 0.62f, w * 0.65f, h * 0.50f, w * 0.50f, h * 0.48f)
            }
            drawPath(snakeCurve, color = CitrusColors.DeepCobaltDark, style = Stroke(bodyStroke + 8f, cap = StrokeCap.Round))
            drawPath(snakeCurve, color = CitrusColors.CitrusLime, style = Stroke(bodyStroke, cap = StrokeCap.Round))
            drawPath(snakeCurve, color = CitrusColors.WarmGoldenYellow, style = Stroke(bodyStroke * 0.35f, cap = StrokeCap.Round))

            // Head Disc
            val headCenter = Offset(w * 0.50f, h * 0.38f)
            val headRadius = w * 0.22f
            drawCircle(color = CitrusColors.DeepCobaltDark, radius = headRadius + 4f, center = headCenter)
            drawCircle(color = CitrusColors.CitrusLime, radius = headRadius, center = headCenter)

            // Dizzy "X" Eyes
            val eyeR = w * 0.065f
            fun drawXEye(cx: Float, cy: Float) {
                val strokeW = 4.5f
                drawLine(CitrusColors.TomatoCoral, Offset(cx - eyeR, cy - eyeR), Offset(cx + eyeR, cy + eyeR), strokeWidth = strokeW, cap = StrokeCap.Round)
                drawLine(CitrusColors.TomatoCoral, Offset(cx - eyeR, cy + eyeR), Offset(cx + eyeR, cy - eyeR), strokeWidth = strokeW, cap = StrokeCap.Round)
            }
            drawXEye(headCenter.x - eyeR * 1.2f, headCenter.y - eyeR * 0.4f)
            drawXEye(headCenter.x + eyeR * 1.2f, headCenter.y - eyeR * 0.4f)

            // Tongue sticking out sideways
            val tongueStart = Offset(headCenter.x + headRadius * 0.3f, headCenter.y + headRadius * 0.6f)
            val tPath = Path().apply {
                moveTo(tongueStart.x, tongueStart.y)
                cubicTo(
                    tongueStart.x + 10f, tongueStart.y + 24f,
                    tongueStart.x + 24f, tongueStart.y + 24f,
                    tongueStart.x + 28f, tongueStart.y + 12f
                )
            }
            drawPath(tPath, color = CitrusColors.TomatoCoral, style = Stroke(width = 5f, cap = StrokeCap.Round))

            // Open shocked mouth
            drawCircle(color = CitrusColors.DeepInk, radius = eyeR * 0.7f, center = Offset(headCenter.x, headCenter.y + eyeR * 0.9f))
        }
    }
}

// ==========================================
// 9. PLAYFUL CITRUS BACKGROUND DECORATIONS
// ==========================================

@Composable
fun CitrusBackgroundDecorations(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Top Left: Deep Cobalt Blob + Citrus Lime blob
        val cobaltBlob = Path().apply {
            moveTo(0f, 0f)
            lineTo(w * 0.28f, 0f)
            cubicTo(w * 0.24f, h * 0.06f, w * 0.16f, h * 0.09f, w * 0.08f, h * 0.08f)
            cubicTo(w * 0.02f, h * 0.08f, 0f, h * 0.05f, 0f, 0f)
        }
        drawPath(cobaltBlob, color = CitrusColors.DeepCobalt)

        drawCircle(
            color = CitrusColors.CitrusLime,
            radius = w * 0.09f,
            center = Offset(w * 0.24f, h * 0.035f)
        )

        // Top Right: Tomato Coral & Warm Yellow accents
        drawCircle(
            color = CitrusColors.WarmGoldenYellow,
            radius = w * 0.10f,
            center = Offset(w * 0.96f, h * 0.04f)
        )
        drawCircle(
            color = CitrusColors.TomatoCoral,
            radius = w * 0.05f,
            center = Offset(w * 0.84f, h * 0.06f)
        )

        // Bottom Left: Lime & Deep Cobalt Blobs
        val botLeftBlob = Path().apply {
            moveTo(0f, h)
            lineTo(w * 0.32f, h)
            cubicTo(w * 0.26f, h * 0.92f, w * 0.14f, h * 0.88f, 0f, h * 0.92f)
            close()
        }
        drawPath(botLeftBlob, color = CitrusColors.CitrusLime)

        // Bottom Right: Deep Cobalt and Coral Blobs
        val botRightBlob = Path().apply {
            moveTo(w, h)
            lineTo(w * 0.70f, h)
            cubicTo(w * 0.74f, h * 0.90f, w * 0.86f, h * 0.88f, w, h * 0.92f)
            close()
        }
        drawPath(botRightBlob, color = CitrusColors.DeepCobalt)

        drawCircle(
            color = CitrusColors.TomatoCoral,
            radius = w * 0.07f,
            center = Offset(w * 0.88f, h * 0.92f)
        )
    }
}
