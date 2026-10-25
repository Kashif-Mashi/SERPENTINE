package com.example.snakegame

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainMenuScreen(
    highScore: Int,
    coins: Int,
    onPlayClick: () -> Unit,
    onThemesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShopClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CitrusColors.BackgroundWarmCream)
    ) {
        // Decorative Citrus Blobs in Corners
        CitrusBackgroundDecorations()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Coin balance & Leaderboard shortcut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Best Score Pill on left
                ScorePill(label = "Best", value = highScore)

                // Coin Balance on right
                CoinBalance(coins = coins, onClick = onShopClick)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Logo & Title Section
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CitrusSerpentineLogo()
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Small Snake. Big Adventure.",
                    style = CitrusTypography.Subtitle,
                    color = CitrusColors.DeepInk.copy(alpha = 0.75f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Centerpiece: Cute Snake Mascot in Warm Circular Badge
            CitrusMascot(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Large Tomato Coral PLAY Button
            SerpentinePrimaryButton(
                text = "PLAY",
                onClick = onPlayClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                icon = {
                    Text(
                        text = "▶",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Navigation Row: Themes, Settings, Shop
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Themes Button
                MenuCircularIconButton(
                    title = "THEMES",
                    onClick = onThemesClick,
                    icon = {
                        // Palette Icon
                        Canvas(modifier = Modifier.size(24.dp)) {
                            val c = CitrusColors.DeepCobalt
                            drawCircle(color = c, radius = size.width / 2f)
                            drawCircle(color = Color.White, radius = size.width * 0.16f, center = Offset(size.width * 0.32f, size.height * 0.36f))
                            drawCircle(color = Color.White, radius = size.width * 0.14f, center = Offset(size.width * 0.68f, size.height * 0.36f))
                            drawCircle(color = Color.White, radius = size.width * 0.14f, center = Offset(size.width * 0.46f, size.height * 0.70f))
                        }
                    }
                )

                // 2. Settings Button
                MenuCircularIconButton(
                    title = "SETTINGS",
                    onClick = onSettingsClick,
                    icon = {
                        // Gear Icon
                        Canvas(modifier = Modifier.size(24.dp)) {
                            val c = CitrusColors.DeepCobalt
                            drawCircle(c, radius = size.width * 0.35f, style = Stroke(width = 3.dp.toPx()))
                            drawCircle(c, radius = size.width * 0.14f)
                            for (angle in 0 until 360 step 45) {
                                val rad = Math.toRadians(angle.toDouble())
                                val startX = (size.width / 2f + Math.cos(rad) * size.width * 0.32f).toFloat()
                                val startY = (size.height / 2f + Math.sin(rad) * size.height * 0.32f).toFloat()
                                val endX = (size.width / 2f + Math.cos(rad) * size.width * 0.48f).toFloat()
                                val endY = (size.height / 2f + Math.sin(rad) * size.height * 0.48f).toFloat()
                                drawLine(c, Offset(startX, startY), Offset(endX, endY), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                            }
                        }
                    }
                )

                // 3. Shop Button
                MenuCircularIconButton(
                    title = "SHOP",
                    onClick = onShopClick,
                    icon = {
                        // Shopping Cart Icon
                        Canvas(modifier = Modifier.size(24.dp)) {
                            val c = CitrusColors.DeepCobalt
                            val cartPath = Path().apply {
                                moveTo(size.width * 0.12f, size.height * 0.22f)
                                lineTo(size.width * 0.30f, size.height * 0.22f)
                                lineTo(size.width * 0.42f, size.height * 0.64f)
                                lineTo(size.width * 0.82f, size.height * 0.64f)
                                lineTo(size.width * 0.90f, size.height * 0.34f)
                                lineTo(size.width * 0.36f, size.height * 0.34f)
                            }
                            drawPath(cartPath, color = c, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                            // Wheels
                            drawCircle(c, radius = 2.2.dp.toPx(), center = Offset(size.width * 0.46f, size.height * 0.80f))
                            drawCircle(c, radius = 2.2.dp.toPx(), center = Offset(size.width * 0.78f, size.height * 0.80f))
                        }
                    }
                )

                // 4. Leaderboard Button
                MenuCircularIconButton(
                    title = "RANKS",
                    onClick = onLeaderboardClick,
                    icon = {
                        // Trophy / Podium Icon
                        Canvas(modifier = Modifier.size(24.dp)) {
                            val c = CitrusColors.DeepCobalt
                            val cupPath = Path().apply {
                                moveTo(size.width * 0.28f, size.height * 0.20f)
                                lineTo(size.width * 0.72f, size.height * 0.20f)
                                lineTo(size.width * 0.68f, size.height * 0.55f)
                                lineTo(size.width * 0.32f, size.height * 0.55f)
                                close()
                            }
                            drawPath(cupPath, c)
                            drawLine(c, Offset(size.width * 0.50f, size.height * 0.55f), Offset(size.width * 0.50f, size.height * 0.75f), strokeWidth = 2.dp.toPx())
                            drawRoundRect(c, topLeft = Offset(size.width * 0.32f, size.height * 0.75f), size = androidx.compose.ui.geometry.Size(size.width * 0.36f, size.height * 0.12f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun MenuCircularIconButton(
    title: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(54.dp)
                .shadow(3.dp, shape = CircleShape),
            shape = CircleShape,
            color = CitrusColors.SoftCardBg,
            border = BorderStroke(1.5.dp, CitrusColors.SoftCardBorder)
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon()
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            color = CitrusColors.DeepInk,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
    }
}
