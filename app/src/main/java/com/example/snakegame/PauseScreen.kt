package com.example.snakegame

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PauseScreen(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onChangeTheme: () -> Unit,
    onSettings: () -> Unit,
    onQuit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CitrusColors.BackgroundWarmCream)
    ) {
        // Playful citrus blob decorations around edges
        CitrusBackgroundDecorations()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Golden Crown
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
                drawPath(crownPath, color = CitrusColors.WarmGoldenDark, style = Stroke(width = 2.5f))
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Heading: Paused
            Text(
                text = "Paused",
                color = CitrusColors.DeepCobalt,
                style = CitrusTypography.DisplayLarge
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Action Buttons Stack
            Column(
                modifier = Modifier.fillMaxWidth(0.92f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Resume (Large Coral Primary Button)
                SerpentinePrimaryButton(
                    text = "Resume",
                    onClick = onResume,
                    containerColor = CitrusColors.TomatoCoral,
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        Text(
                            text = "▶",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                )

                // 2. Restart Button
                SerpentineSecondaryButton(
                    text = "Restart",
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        // Restart counter-clockwise arrow
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val c = CitrusColors.DeepCobalt
                            drawArc(
                                color = c,
                                startAngle = 45f,
                                sweepAngle = 270f,
                                useCenter = false,
                                topLeft = Offset(size.width * 0.15f, size.height * 0.15f),
                                size = androidx.compose.ui.geometry.Size(size.width * 0.7f, size.height * 0.7f),
                                style = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round)
                            )
                            val arrPath = Path().apply {
                                moveTo(size.width * 0.45f, size.height * 0.05f)
                                lineTo(size.width * 0.50f, size.height * 0.22f)
                                lineTo(size.width * 0.32f, size.height * 0.25f)
                            }
                            drawPath(arrPath, color = c, style = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round))
                        }
                    }
                )

                // 3. Change Theme Button
                SerpentineSecondaryButton(
                    text = "Change Theme",
                    onClick = onChangeTheme,
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        // Palette icon
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val c = CitrusColors.DeepCobalt
                            drawCircle(c, radius = size.width / 2f)
                            drawCircle(Color.White, radius = size.width * 0.15f, center = Offset(size.width * 0.35f, size.height * 0.35f))
                            drawCircle(Color.White, radius = size.width * 0.12f, center = Offset(size.width * 0.65f, size.height * 0.4f))
                        }
                    }
                )

                // 4. Settings Button
                SerpentineSecondaryButton(
                    text = "Settings",
                    onClick = onSettings,
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        // Gear icon
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val c = CitrusColors.DeepCobalt
                            drawCircle(c, radius = size.width * 0.32f, style = Stroke(2.dp.toPx()))
                            drawCircle(c, radius = size.width * 0.12f)
                        }
                    }
                )

                // 5. Quit Button
                SerpentineSecondaryButton(
                    text = "Quit",
                    onClick = onQuit,
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        // Door / Exit arrow icon
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val c = CitrusColors.DeepCobalt
                            val doorPath = Path().apply {
                                moveTo(size.width * 0.45f, size.height * 0.15f)
                                lineTo(size.width * 0.15f, size.height * 0.15f)
                                lineTo(size.width * 0.15f, size.height * 0.85f)
                                lineTo(size.width * 0.45f, size.height * 0.85f)
                            }
                            drawPath(doorPath, color = c, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                            // Arrow out
                            drawLine(c, Offset(size.width * 0.35f, size.height * 0.5f), Offset(size.width * 0.85f, size.height * 0.5f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                            drawLine(c, Offset(size.width * 0.65f, size.height * 0.32f), Offset(size.width * 0.85f, size.height * 0.5f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                            drawLine(c, Offset(size.width * 0.65f, size.height * 0.68f), Offset(size.width * 0.85f, size.height * 0.5f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                        }
                    }
                )
            }
        }
    }
}
