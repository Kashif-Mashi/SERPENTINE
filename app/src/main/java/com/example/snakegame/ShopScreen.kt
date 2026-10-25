package com.example.snakegame

import android.widget.Toast
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
fun ShopScreen(
    coins: Int,
    unlockedThemes: Set<String>,
    currentTheme: GameTheme,
    onThemePurchased: (GameTheme) -> Unit,
    onCoinsRewarded: (Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(ShopCategory.THEMES) }

    val shopThemes = remember {
        listOf(
            GameTheme.CITRUS_POP,
            GameTheme.SUNSET_ARCADE,
            GameTheme.BUBBLEGUM_STUDIO,
            GameTheme.PAPER_AND_INK,
            GameTheme.AQUA_CARNIVAL,
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
            // 1. Top App Bar: Back Button | Title | Coin Balance
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
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

                // Title
                Text(
                    text = "Shop",
                    style = CitrusTypography.TitleBold,
                    color = CitrusColors.DeepCobalt
                )

                // Coin Balance
                CoinBalance(coins = coins)
            }

            // 2. Segmented Category Tabs: Themes | Snakes | Effects
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShopTabButton(
                    title = "Themes",
                    isSelected = selectedTab == ShopCategory.THEMES,
                    onClick = { selectedTab = ShopCategory.THEMES },
                    modifier = Modifier.weight(1f)
                )
                ShopTabButton(
                    title = "Snakes",
                    isSelected = selectedTab == ShopCategory.SNAKES,
                    onClick = { selectedTab = ShopCategory.SNAKES },
                    modifier = Modifier.weight(1f)
                )
                ShopTabButton(
                    title = "Effects",
                    isSelected = selectedTab == ShopCategory.EFFECTS,
                    onClick = { selectedTab = ShopCategory.EFFECTS },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Scrollable Shop Items List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab == ShopCategory.THEMES) {
                    items(shopThemes) { theme ->
                        val isOwned = theme == GameTheme.CITRUS_POP || theme.themeName in unlockedThemes
                        val isEquipped = theme == currentTheme

                        ShopThemeItemRow(
                            theme = theme,
                            isOwned = isOwned,
                            isEquipped = isEquipped,
                            userCoins = coins,
                            onBuy = {
                                if (coins >= theme.coinPrice) {
                                    onThemePurchased(theme)
                                } else {
                                    Toast.makeText(context, "Not enough coins! Watch an ad to get 50 coins.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onEquip = {
                                onThemePurchased(theme)
                            }
                        )
                    }
                } else if (selectedTab == ShopCategory.SNAKES) {
                    item {
                        ShopGenericItemRow(
                            title = "Lime Classic",
                            price = 0,
                            isOwned = true,
                            isEquipped = true,
                            description = "Standard cheerful cartoon serpent with green scales.",
                            onAction = {}
                        )
                    }
                    item {
                        ShopGenericItemRow(
                            title = "Coral Dragon",
                            price = 150,
                            isOwned = false,
                            isEquipped = false,
                            description = "Fiery orange serpent with fierce horns and ruby eyes.",
                            onAction = {
                                Toast.makeText(context, "Need 150 coins to unlock!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    item {
                        ShopGenericItemRow(
                            title = "Cobalt Cyber",
                            price = 200,
                            isOwned = false,
                            isEquipped = false,
                            description = "Electric blue mechanical snake with neon stripes.",
                            onAction = {
                                Toast.makeText(context, "Need 200 coins to unlock!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                } else {
                    item {
                        ShopGenericItemRow(
                            title = "Fruit Burst",
                            price = 0,
                            isOwned = true,
                            isEquipped = true,
                            description = "Juicy fruit particle splashes upon eating apples.",
                            onAction = {}
                        )
                    }
                    item {
                        ShopGenericItemRow(
                            title = "Rainbow Trail",
                            price = 120,
                            isOwned = false,
                            isEquipped = false,
                            description = "Multicolor sparkling dust trail following the snake.",
                            onAction = {
                                Toast.makeText(context, "Need 120 coins to unlock!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Bottom Rewarded Video Ad Card: "Watch Ad & Get 50 Coins"
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
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Video Play Icon inside gold circle
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = CitrusShapes.CardSmall,
                            color = CitrusColors.WarmGoldenYellow
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.size(16.dp)) {
                                    val playPath = Path().apply {
                                        moveTo(size.width * 0.3f, size.height * 0.2f)
                                        lineTo(size.width * 0.8f, size.height * 0.5f)
                                        lineTo(size.width * 0.3f, size.height * 0.8f)
                                        close()
                                    }
                                    drawPath(playPath, color = CitrusColors.DeepInk)
                                }
                            }
                        }

                        Column {
                            Text(
                                text = "Watch Ad & Get",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "50 Coins",
                                color = CitrusColors.WarmGoldenYellow,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // "Watch" Button (Golden Yellow Pill)
                    Surface(
                        modifier = Modifier
                            .clickable {
                                onCoinsRewarded(50)
                                Toast.makeText(context, "+50 Coins Earned! 🎉", Toast.LENGTH_SHORT).show()
                            }
                            .shadow(2.dp, shape = CitrusShapes.Pill),
                        shape = CitrusShapes.Pill,
                        color = CitrusColors.WarmGoldenYellow
                    ) {
                        Text(
                            text = "Watch",
                            color = CitrusColors.DeepInk,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun ShopTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(38.dp)
            .clickable { onClick() }
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
                text = title,
                color = CitrusColors.DeepInk,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun ShopThemeItemRow(
    theme: GameTheme,
    isOwned: Boolean,
    isEquipped: Boolean,
    userCoins: Int,
    onBuy: () -> Unit,
    onEquip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, shape = CitrusShapes.CardSmall),
        shape = CitrusShapes.CardSmall,
        color = CitrusColors.SoftCardBg,
        border = BorderStroke(1.2.dp, CitrusColors.SoftCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme Preview Icon
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CitrusShapes.CardSmall),
                shape = CitrusShapes.CardSmall,
                color = theme.background
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(theme.checkerLight)
                    drawCircle(theme.snakeHead, radius = size.width * 0.28f, center = Offset(size.width * 0.4f, size.height * 0.5f))
                    drawCircle(theme.normalFood, radius = size.width * 0.18f, center = Offset(size.width * 0.72f, size.height * 0.4f))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.themeName,
                    color = CitrusColors.DeepInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (!isOwned) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Canvas(modifier = Modifier.size(14.dp)) {
                            drawCircle(CitrusColors.WarmGoldenYellow)
                        }
                        Text(
                            text = "${theme.coinPrice}",
                            color = CitrusColors.SecondaryMutedText,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Text(
                        text = if (isEquipped) "Active" else "Unlocked",
                        color = CitrusColors.SuccessGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }

            // Action Button
            when {
                isEquipped -> {
                    Surface(
                        shape = CitrusShapes.Pill,
                        color = CitrusColors.SuccessGreenLight
                    ) {
                        Text(
                            text = "Equipped",
                            color = CitrusColors.SuccessGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
                isOwned -> {
                    Surface(
                        modifier = Modifier.clickable { onEquip() },
                        shape = CitrusShapes.Pill,
                        color = CitrusColors.DeepCobalt
                    ) {
                        Text(
                            text = "Use",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
                else -> {
                    Surface(
                        modifier = Modifier.clickable { onBuy() },
                        shape = CitrusShapes.Pill,
                        color = CitrusColors.SuccessGreen
                    ) {
                        Text(
                            text = "Buy",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShopGenericItemRow(
    title: String,
    price: Int,
    isOwned: Boolean,
    isEquipped: Boolean,
    description: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, shape = CitrusShapes.CardSmall),
        shape = CitrusShapes.CardSmall,
        color = CitrusColors.SoftCardBg,
        border = BorderStroke(1.2.dp, CitrusColors.SoftCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = CitrusColors.DeepInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = description,
                    color = CitrusColors.SecondaryMutedText,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                modifier = Modifier.clickable { onAction() },
                shape = CitrusShapes.Pill,
                color = if (isOwned) CitrusColors.SuccessGreenLight else CitrusColors.SuccessGreen
            ) {
                Text(
                    text = if (isOwned) "Owned" else "$price Coins",
                    color = if (isOwned) CitrusColors.SuccessGreen else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}
