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
fun LeaderboardScreen(
    userHighScore: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Global, 1: Friends

    // Dynamic leaderboard incorporating user's real high score
    val globalEntries = remember(userHighScore) {
        val staticCompetitors = listOf(
            LeaderboardEntry(1, "NovaSnake", 842),
            LeaderboardEntry(2, "PixelRider", 736),
            LeaderboardEntry(3, "SnakeKing", 621),
            LeaderboardEntry(4, "TurboTail", 498),
            LeaderboardEntry(5, "ByteBite", 412),
            LeaderboardEntry(6, "NeonSlither", 387),
            LeaderboardEntry(7, "GridMaster", 354),
            LeaderboardEntry(8, "LoopLegend", 321),
            LeaderboardEntry(9, "DarkSnake", 298)
        )
        val userEntry = LeaderboardEntry(
            rank = 4,
            name = "You",
            score = if (userHighScore > 0) userHighScore else 542,
            isCurrentPlayer = true
        )
        val all = (staticCompetitors + userEntry).sortedByDescending { it.score }
        all.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
    }

    val friendsEntries = remember(userHighScore) {
        listOf(
            LeaderboardEntry(1, "You", if (userHighScore > 0) userHighScore else 542, isCurrentPlayer = true),
            LeaderboardEntry(2, "Alex (Nearby)", 480),
            LeaderboardEntry(3, "Sam_Arcade", 390),
            LeaderboardEntry(4, "Jordan_99", 250)
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
            // Top Bar: Back Button | Title
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

                Text(
                    text = "Leaderboard",
                    style = CitrusTypography.TitleBold,
                    color = CitrusColors.DeepCobalt
                )

                Spacer(modifier = Modifier.weight(1f))

                Spacer(modifier = Modifier.size(42.dp))
            }

            // Segmented Tabs: Global | Friends
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clickable { selectedTab = 0 }
                        .shadow(if (selectedTab == 0) 3.dp else 1.dp, shape = CitrusShapes.Pill),
                    shape = CitrusShapes.Pill,
                    color = if (selectedTab == 0) CitrusColors.WarmGoldenYellow else CitrusColors.SoftCardBg,
                    border = BorderStroke(1.2.dp, if (selectedTab == 0) CitrusColors.WarmGoldenDark else CitrusColors.SoftCardBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Global",
                            color = CitrusColors.DeepInk,
                            fontWeight = if (selectedTab == 0) FontWeight.Black else FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clickable { selectedTab = 1 }
                        .shadow(if (selectedTab == 1) 3.dp else 1.dp, shape = CitrusShapes.Pill),
                    shape = CitrusShapes.Pill,
                    color = if (selectedTab == 1) CitrusColors.WarmGoldenYellow else CitrusColors.SoftCardBg,
                    border = BorderStroke(1.2.dp, if (selectedTab == 1) CitrusColors.WarmGoldenDark else CitrusColors.SoftCardBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Friends",
                            color = CitrusColors.DeepInk,
                            fontWeight = if (selectedTab == 1) FontWeight.Black else FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ranked List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val list = if (selectedTab == 0) globalEntries else friendsEntries
                items(list) { entry ->
                    LeaderboardRow(entry = entry)
                }
            }

            // Bottom Motivation Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .shadow(4.dp, shape = CitrusShapes.CardMedium),
                shape = CitrusShapes.CardMedium,
                color = CitrusColors.DeepCobalt
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(18.dp, 14.dp)) {
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Climb the ranks, show your skills!",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
