package com.example.snakegame

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun ThemeSelectScreen(
    currentTheme: GameTheme,
    onThemeSelect: (GameTheme) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Themes, 1: Snake, 2: Effects

    val citrusThemes = remember {
        listOf(
            GameTheme.CITRUS_POP,
            GameTheme.SUNSET_ARCADE,
            GameTheme.BUBBLEGUM_STUDIO,
            GameTheme.PAPER_AND_INK,
            GameTheme.AQUA_CARNIVAL
        )
    }

    val classicThemes = remember {
        listOf(
            GameTheme.MATRIX_NEON,
            GameTheme.CYBERPUNK_PINK,
            GameTheme.RETRO_GAMEBOY,
            GameTheme.MIDNIGHT_BLUE
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CitrusColors.BackgroundWarmCream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button `<`
                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { onBackClick() }
                        .shadow(2.dp, shape = CircleShape),
                    shape = CircleShape,
                    color = CitrusColors.SoftCardBg,
                    border = BorderStroke(1.2.dp, CitrusColors.SoftCardBorder)
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

                // Title
                Text(
                    text = "Select Theme",
                    style = CitrusTypography.TitleBold,
                    color = CitrusColors.DeepCobalt
                )

                Spacer(modifier = Modifier.weight(1f))

                Spacer(modifier = Modifier.size(42.dp))
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedTab == 0) {
                    item {
                        SectionHeader(title = "Citrus Pop Collection")
                    }

                    items(citrusThemes) { theme ->
                        ThemePreviewCard(
                            theme = theme,
                            isSelected = theme == currentTheme,
                            onSelect = { onThemeSelect(theme) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(title = "Classic Arcade Themes")
                    }

                    items(classicThemes) { theme ->
                        ThemePreviewCard(
                            theme = theme,
                            isSelected = theme == currentTheme,
                            onSelect = { onThemeSelect(theme) }
                        )
                    }
                } else if (selectedTab == 1) {
                    item {
                        SectionHeader(title = "Snake Mascot Skins")
                    }
                    item {
                        SerpentineCard {
                            Text(
                                text = "Classic Citrus Lime Snake",
                                style = CitrusTypography.Headline
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Equipped by default with expressive cartoon eyes and friendly smile.",
                                style = CitrusTypography.Body
                            )
                        }
                    }
                    item {
                        SerpentineCard {
                            Text(
                                text = "Sunset Coral Striker",
                                style = CitrusTypography.Headline
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Warm gradient serpent with dynamic glowing stripes.",
                                style = CitrusTypography.Body
                            )
                        }
                    }
                } else {
                    item {
                        SectionHeader(title = "Particle & Fruit Effects")
                    }
                    item {
                        SerpentineCard {
                            Text(
                                text = "Citrus Pop Sparkles",
                                style = CitrusTypography.Headline
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bursting fruit particles and floating score indicators upon eating pellets.",
                                style = CitrusTypography.Body
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Bar: Themes, Snake, Effects
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp),
                color = CitrusColors.SoftCardBg,
                border = BorderStroke(1.dp, CitrusColors.SoftCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ThemeBottomTab(
                        title = "Themes",
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { isSel ->
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val c = if (isSel) CitrusColors.TomatoCoral else CitrusColors.SecondaryMutedText
                                drawCircle(color = c, radius = size.width / 2f)
                                drawCircle(color = Color.White, radius = size.width * 0.16f, center = Offset(size.width * 0.32f, size.height * 0.36f))
                                drawCircle(color = Color.White, radius = size.width * 0.14f, center = Offset(size.width * 0.68f, size.height * 0.36f))
                            }
                        }
                    )

                    ThemeBottomTab(
                        title = "Snake",
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { isSel ->
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val c = if (isSel) CitrusColors.TomatoCoral else CitrusColors.SecondaryMutedText
                                val sPath = Path().apply {
                                    moveTo(size.width * 0.2f, size.height * 0.7f)
                                    cubicTo(size.width * 0.4f, size.height * 0.9f, size.width * 0.6f, size.height * 0.1f, size.width * 0.8f, size.height * 0.3f)
                                }
                                drawPath(sPath, color = c, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                            }
                        }
                    )

                    ThemeBottomTab(
                        title = "Effects",
                        isSelected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { isSel ->
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val c = if (isSel) CitrusColors.TomatoCoral else CitrusColors.SecondaryMutedText
                                drawCircle(color = c, radius = 3.dp.toPx(), center = Offset(size.width * 0.3f, size.height * 0.3f))
                                drawCircle(color = c, radius = 4.dp.toPx(), center = Offset(size.width * 0.7f, size.height * 0.4f))
                                drawCircle(color = c, radius = 2.5.dp.toPx(), center = Offset(size.width * 0.4f, size.height * 0.7f))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeBottomTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon(isSelected)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = if (isSelected) CitrusColors.TomatoCoral else CitrusColors.SecondaryMutedText,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
        )
    }
}
