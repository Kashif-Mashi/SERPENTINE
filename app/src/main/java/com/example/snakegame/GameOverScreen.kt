package com.example.snakegame

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameOverScreen(
    finalScore: Int,
    highScore: Int,
    coins: Int = 0,
    canRevive: Boolean = false,
    onRevive: () -> Unit = {},
    onTryAgain: () -> Unit,
    onMainMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isNewBest = finalScore >= highScore && finalScore > 0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xE6141D2B)) // Soft dark vignette backdrop
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Dizzy Snake Mascot with Confetti
            DizzyCitrusMascot(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .aspectRatio(1.2f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            val palette = CitrusTheme.palette
            // 2. White / Soft Card Container
            SerpentineCard(
                modifier = Modifier.fillMaxWidth(0.92f),
                containerColor = palette.softCardBg,
                borderColor = palette.softCardBorder
            ) {
                // Game Over Heading
                Text(
                    text = "Game Over",
                    color = CitrusColors.DeepCobalt,
                    style = CitrusTypography.DisplayMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Total Coins Badge
                Surface(
                    shape = CitrusShapes.Pill,
                    color = CitrusColors.WarmGoldenYellow.copy(alpha = 0.20f),
                    border = BorderStroke(1.2.dp, CitrusColors.WarmGoldenYellow),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🪙", fontSize = 14.sp)
                        Text(
                            text = "$coins Total Coins",
                            color = CitrusColors.DeepCobalt,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Score & Best Stat Card
                GameStatCard(score = finalScore, bestScore = highScore)

                Spacer(modifier = Modifier.height(18.dp))

                // Revive Button (once per run)
                if (canRevive) {
                    val hasEnoughCoins = coins >= 20
                    SerpentinePrimaryButton(
                        text = if (hasEnoughCoins) "Revive (20 coins)" else "Revive (Need 20 coins)",
                        onClick = onRevive,
                        enabled = hasEnoughCoins,
                        containerColor = if (hasEnoughCoins) CitrusColors.CitrusLime else Color(0xFFC7CBD1),
                        contentColor = if (hasEnoughCoins) CitrusColors.DeepCobalt else Color(0xFF6B7280),
                        icon = {
                            Text(text = "💖", fontSize = 16.sp)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Try Again Button (Coral)
                SerpentinePrimaryButton(
                    text = "Try Again",
                    onClick = onTryAgain,
                    containerColor = CitrusColors.TomatoCoral,
                    icon = {
                        // Reload circular arrow
                        Canvas(modifier = Modifier.size(18.dp)) {
                            drawArc(
                                color = Color.White,
                                startAngle = 45f,
                                sweepAngle = 270f,
                                useCenter = false,
                                topLeft = Offset(size.width * 0.15f, size.height * 0.15f),
                                size = androidx.compose.ui.geometry.Size(size.width * 0.7f, size.height * 0.7f),
                                style = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Main Menu Button
                SerpentineSecondaryButton(
                    text = "Main Menu",
                    onClick = onMainMenu,
                    icon = {
                        // Home icon
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val c = CitrusColors.DeepCobalt
                            val homePath = Path().apply {
                                moveTo(size.width * 0.5f, size.height * 0.15f)
                                lineTo(size.width * 0.85f, size.height * 0.50f)
                                lineTo(size.width * 0.75f, size.height * 0.50f)
                                lineTo(size.width * 0.75f, size.height * 0.85f)
                                lineTo(size.width * 0.25f, size.height * 0.85f)
                                lineTo(size.width * 0.25f, size.height * 0.50f)
                                lineTo(size.width * 0.15f, size.height * 0.50f)
                                close()
                            }
                            drawPath(homePath, color = c, style = Fill)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Share Score Button
                SerpentineSecondaryButton(
                    text = "Share Score",
                    onClick = {
                        shareScore(context, finalScore, highScore)
                    },
                    icon = {
                        // Share nodes icon
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val c = CitrusColors.DeepCobalt
                            val r = 2.5.dp.toPx()
                            val p1 = Offset(size.width * 0.75f, size.height * 0.25f)
                            val p2 = Offset(size.width * 0.25f, size.height * 0.50f)
                            val p3 = Offset(size.width * 0.75f, size.height * 0.75f)

                            drawLine(c, p2, p1, strokeWidth = 1.8.dp.toPx())
                            drawLine(c, p2, p3, strokeWidth = 1.8.dp.toPx())
                            drawCircle(c, radius = r, center = p1)
                            drawCircle(c, radius = r, center = p2)
                            drawCircle(c, radius = r, center = p3)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Friendly note with tiny crown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(16.dp, 12.dp)) {
                        val cp = Path().apply {
                            moveTo(size.width * 0.1f, size.height * 0.9f)
                            lineTo(0f, size.height * 0.2f)
                            lineTo(size.width * 0.35f, size.height * 0.5f)
                            lineTo(size.width * 0.5f, 0f)
                            lineTo(size.width * 0.65f, size.height * 0.5f)
                            lineTo(size.width, size.height * 0.2f)
                            lineTo(size.width * 0.9f, size.height * 0.9f)
                            close()
                        }
                        drawPath(cp, color = CitrusColors.WarmGoldenYellow, style = Fill)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isNewBest) "New All-Time High Score!" else "Better luck next time!",
                        color = if (isNewBest) CitrusColors.TomatoCoral else CitrusColors.SecondaryMutedText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun shareScore(context: Context, score: Int, best: Int) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "I scored $score points in Serpentine (High Score: $best)! Can you beat it? 🐍🕹️"
            )
        }
        context.startActivity(Intent.createChooser(intent, "Share your Serpentine score"))
    } catch (_: Exception) {
    }
}
