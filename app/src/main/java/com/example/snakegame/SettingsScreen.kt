package com.example.snakegame

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
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
import kotlin.math.abs

@Composable
fun SettingsScreen(
    musicEnabled: Boolean,
    soundEffectsEnabled: Boolean,
    hapticsEnabled: Boolean,
    portalWallsEnabled: Boolean,
    selectedDifficulty: Difficulty,
    darkModeEnabled: Boolean,
    vibrationEnabled: Boolean,
    swipeSensitivity: Float = 0.85f,
    onMusicToggle: (Boolean) -> Unit,
    onSoundEffectsToggle: (Boolean) -> Unit,
    onHapticsToggle: (Boolean) -> Unit,
    onPortalWallsToggle: (Boolean) -> Unit,
    onDifficultySelected: (Difficulty) -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onSwipeSensitivityChange: (Float) -> Unit = {},
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = CitrusTheme.palette
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        // Decorative blobs at bottom
        CitrusBackgroundDecorations()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { onBackClick() }
                        .shadow(2.dp, shape = CircleShape),
                    shape = CircleShape,
                    color = palette.softCardBg,
                    border = BorderStroke(1.2.dp, palette.softCardBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(16.dp)) {
                            val c = CitrusColors.DeepCobalt
                            val path = Path().apply {
                                moveTo(size.width * 0.65f, size.height * 0.15f)
                                lineTo(size.width * 0.30f, size.height * 0.50f)
                                lineTo(size.width * 0.65f, size.height * 0.85f)
                            }
                            drawPath(path, color = c, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Settings",
                    style = CitrusTypography.TitleBold,
                    color = CitrusColors.DeepCobalt
                )

                Spacer(modifier = Modifier.weight(1f))

                Spacer(modifier = Modifier.size(42.dp))
            }

            // Scrollable Settings Cards
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Game Settings
                SectionHeader(title = "Game Settings")

                SerpentineCard {
                    SettingsToggleRow(
                        title = "Music",
                        checked = musicEnabled,
                        onCheckedChange = onMusicToggle,
                        icon = {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val c = CitrusColors.DeepCobalt
                                drawCircle(c, radius = 3.dp.toPx(), center = Offset(size.width * 0.35f, size.height * 0.7f))
                                drawLine(c, Offset(size.width * 0.35f + 3.dp.toPx(), size.height * 0.7f), Offset(size.width * 0.35f + 3.dp.toPx(), size.height * 0.25f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                                drawCircle(c, radius = 3.dp.toPx(), center = Offset(size.width * 0.75f, size.height * 0.55f))
                                drawLine(c, Offset(size.width * 0.75f + 3.dp.toPx(), size.height * 0.55f), Offset(size.width * 0.75f + 3.dp.toPx(), size.height * 0.15f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                                drawLine(c, Offset(size.width * 0.35f + 3.dp.toPx(), size.height * 0.25f), Offset(size.width * 0.75f + 3.dp.toPx(), size.height * 0.15f), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            }
                        }
                    )

                    HorizontalDivider(color = CitrusColors.SoftCardBorder, thickness = 1.dp)

                    SettingsToggleRow(
                        title = "Sound Effects",
                        checked = soundEffectsEnabled,
                        onCheckedChange = onSoundEffectsToggle,
                        icon = {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val c = CitrusColors.DeepCobalt
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
                                drawArc(c, startAngle = -45f, sweepAngle = 90f, useCenter = false, topLeft = Offset(size.width * 0.55f, size.height * 0.25f), size = androidx.compose.ui.geometry.Size(size.width * 0.35f, size.height * 0.50f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                            }
                        }
                    )

                    HorizontalDivider(color = CitrusColors.SoftCardBorder, thickness = 1.dp)

                    SettingsToggleRow(
                        title = "Haptic Feedback",
                        checked = hapticsEnabled,
                        onCheckedChange = onHapticsToggle,
                        icon = {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val c = CitrusColors.DeepCobalt
                                drawRoundRect(
                                    color = c,
                                    topLeft = Offset(size.width * 0.25f, size.height * 0.15f),
                                    size = androidx.compose.ui.geometry.Size(size.width * 0.50f, size.height * 0.70f),
                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                                    style = Stroke(1.8.dp.toPx())
                                )
                                drawLine(c, Offset(size.width * 0.10f, size.height * 0.35f), Offset(size.width * 0.10f, size.height * 0.65f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
                                drawLine(c, Offset(size.width * 0.90f, size.height * 0.35f), Offset(size.width * 0.90f, size.height * 0.65f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
                            }
                        }
                    )

                    HorizontalDivider(color = CitrusColors.SoftCardBorder, thickness = 1.dp)

                    SettingsToggleRow(
                        title = "Portal Walls",
                        checked = portalWallsEnabled,
                        onCheckedChange = onPortalWallsToggle,
                        icon = {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val c = CitrusColors.DeepCobalt
                                drawRoundRect(
                                    color = c,
                                    topLeft = Offset(size.width * 0.15f, size.height * 0.15f),
                                    size = androidx.compose.ui.geometry.Size(size.width * 0.70f, size.height * 0.70f),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                                    style = Stroke(1.8.dp.toPx())
                                )
                                drawLine(
                                    color = CitrusColors.TomatoCoral,
                                    start = Offset(size.width * 0.35f, size.height * 0.25f),
                                    end = Offset(size.width * 0.65f, size.height * 0.75f),
                                    strokeWidth = 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    )
                }

                // Section 2: Difficulty
                SectionHeader(title = "Difficulty")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Difficulty.entries.forEach { diff ->
                        val isSelected = diff == selectedDifficulty
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable { onDifficultySelected(diff) }
                                .shadow(if (isSelected) 3.dp else 1.dp, shape = CitrusShapes.Pill),
                            shape = CitrusShapes.Pill,
                            color = if (isSelected) CitrusColors.WarmGoldenYellow else CitrusColors.SoftCardBg,
                            border = BorderStroke(
                                1.2.dp,
                                if (isSelected) CitrusColors.WarmGoldenDark else CitrusColors.SoftCardBorder
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = diff.displayName,
                                    color = CitrusColors.DeepInk,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Section 3: Touch Sensitivity
                SectionHeader(title = "Touch Sensitivity")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val sensitivityOptions = listOf(
                        Pair("Normal", 0.65f),
                        Pair("Fast", 0.85f),
                        Pair("Hyper", 0.98f)
                    )
                    sensitivityOptions.forEach { (label, value) ->
                        val isSelected = abs(swipeSensitivity - value) < 0.08f
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable { onSwipeSensitivityChange(value) }
                                .shadow(if (isSelected) 3.dp else 1.dp, shape = CitrusShapes.Pill),
                            shape = CitrusShapes.Pill,
                            color = if (isSelected) CitrusColors.WarmGoldenYellow else CitrusColors.SoftCardBg,
                            border = BorderStroke(
                                1.2.dp,
                                if (isSelected) CitrusColors.WarmGoldenDark else CitrusColors.SoftCardBorder
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    color = CitrusColors.DeepInk,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Controls speed and threshold for quick directional swipes (Left, Right, Up, Down).",
                    color = palette.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                // Section 4: Display
                SectionHeader(title = "Display")

                SerpentineCard {
                    SettingsToggleRow(
                        title = "Dark Mode",
                        checked = darkModeEnabled,
                        onCheckedChange = onDarkModeToggle,
                        icon = {
                            // Moon icon
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val c = CitrusColors.DeepCobalt
                                val moonPath = Path().apply {
                                    moveTo(size.width * 0.6f, size.height * 0.15f)
                                    cubicTo(
                                        size.width * 0.2f, size.height * 0.25f,
                                        size.width * 0.2f, size.height * 0.75f,
                                        size.width * 0.6f, size.height * 0.85f
                                    )
                                    cubicTo(
                                        size.width * 0.4f, size.height * 0.70f,
                                        size.width * 0.4f, size.height * 0.30f,
                                        size.width * 0.6f, size.height * 0.15f
                                    )
                                    close()
                                }
                                drawPath(moonPath, c, style = Fill)
                            }
                        }
                    )

                    HorizontalDivider(color = CitrusColors.SoftCardBorder, thickness = 1.dp)

                    SettingsToggleRow(
                        title = "Vibration",
                        checked = vibrationEnabled,
                        onCheckedChange = onVibrationToggle,
                        icon = {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val c = CitrusColors.DeepCobalt
                                drawCircle(c, radius = 6.dp.toPx(), style = Stroke(1.8.dp.toPx()))
                                drawCircle(c, radius = 2.dp.toPx())
                            }
                        }
                    )
                }

                // Section 5: About
                SectionHeader(title = "About")

                SerpentineCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Serpentine Arcade",
                                color = palette.textPrimary,
                                style = CitrusTypography.BodyBold
                            )
                            Text(
                                text = "Citrus Pop Edition",
                                color = palette.textSecondary,
                                style = CitrusTypography.Body
                            )
                        }
                        Surface(
                            shape = CitrusShapes.Pill,
                            color = palette.background
                        ) {
                            Text(
                                text = "v1.0.0",
                                color = CitrusColors.DeepCobalt,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
