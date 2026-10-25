package com.example.snakegame

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Serpentine: Citrus Pop Design System tokens & palette.
 * Supports both Light mode (Warm Cream) and Dark mode (Deep Obsidian Midnight).
 */
data class CitrusPalette(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val softCardBg: Color,
    val softCardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val deepCobalt: Color,
    val deepCobaltDark: Color,
    val tomatoCoral: Color,
    val tomatoCoralPressed: Color,
    val citrusLime: Color,
    val citrusLimeDark: Color,
    val warmGoldenYellow: Color,
    val warmGoldenDark: Color,
    val successGreen: Color,
    val successGreenLight: Color,
    val subtleShadow: Color,
    val boardBorder: Color,
    val checkerLight: Color,
    val checkerDark: Color
)

val LightCitrusPalette = CitrusPalette(
    isDark = false,
    background = Color(0xFFF5E9CC), // Warm Cream
    surface = Color(0xFFFFF8E8),
    softCardBg = Color(0xFFFFF8E8),
    softCardBorder = Color(0xFFE6D6B5),
    textPrimary = Color(0xFF202747), // Deep Ink
    textSecondary = Color(0xFF70778A),
    deepCobalt = Color(0xFF293B8F),
    deepCobaltDark = Color(0xFF1D2A68),
    tomatoCoral = Color(0xFFF05B42),
    tomatoCoralPressed = Color(0xFFD9462E),
    citrusLime = Color(0xFFD6EF43),
    citrusLimeDark = Color(0xFF9ECE28),
    warmGoldenYellow = Color(0xFFFFD34E),
    warmGoldenDark = Color(0xFFE5B932),
    successGreen = Color(0xFF168A61),
    successGreenLight = Color(0xFFE0F4EC),
    subtleShadow = Color(0x24202747),
    boardBorder = Color(0x73293B8F),
    checkerLight = Color(0xFFFAEFD9),
    checkerDark = Color(0xFFEFE1C3)
)

val DarkCitrusPalette = CitrusPalette(
    isDark = true,
    background = Color(0xFF121726), // Deep Obsidian Midnight Ink
    surface = Color(0xFF1B2236),
    softCardBg = Color(0xFF1B2236), // Dark Slate Navy Card
    softCardBorder = Color(0xFF2B3654),
    textPrimary = Color(0xFFF3F0E6), // Crisp Light Ivory Text
    textSecondary = Color(0xFF9AA4BA), // Soft Muted Slate
    deepCobalt = Color(0xFF5C7CFA), // Vibrant Electric Cobalt
    deepCobaltDark = Color(0xFF101422),
    tomatoCoral = Color(0xFFF05B42),
    tomatoCoralPressed = Color(0xFFD9462E),
    citrusLime = Color(0xFFD6EF43),
    citrusLimeDark = Color(0xFF9ECE28),
    warmGoldenYellow = Color(0xFFFFD34E),
    warmGoldenDark = Color(0xFFE5B932),
    successGreen = Color(0xFF20C997),
    successGreenLight = Color(0xFF1A3B34),
    subtleShadow = Color(0x66000000),
    boardBorder = Color(0x995C7CFA),
    checkerLight = Color(0xFF1D263B),
    checkerDark = Color(0xFF151D2E)
)

val LocalCitrusPalette = staticCompositionLocalOf { LightCitrusPalette }

@Composable
fun SerpentineTheme(
    darkMode: Boolean,
    content: @Composable () -> Unit
) {
    val palette = if (darkMode) DarkCitrusPalette else LightCitrusPalette
    CompositionLocalProvider(LocalCitrusPalette provides palette) {
        content()
    }
}

object CitrusTheme {
    val palette: CitrusPalette
        @Composable
        get() = LocalCitrusPalette.current

    val shapes = CitrusShapes
    val typography = CitrusTypography
}

object CitrusColors {
    val BackgroundWarmCream = Color(0xFFF5E9CC)
    val TomatoCoral = Color(0xFFF05B42)
    val TomatoCoralPressed = Color(0xFFD9462E)
    val CitrusLime = Color(0xFFD6EF43)
    val CitrusLimeDark = Color(0xFF9ECE28)
    val DeepCobalt = Color(0xFF293B8F)
    val DeepCobaltDark = Color(0xFF1D2A68)
    val WarmGoldenYellow = Color(0xFFFFD34E)
    val WarmGoldenDark = Color(0xFFE5B932)
    val DeepInk = Color(0xFF202747)
    val SoftCardBg = Color(0xFFFFF8E8)
    val SoftCardBorder = Color(0xFFE6D6B5)
    val SecondaryMutedText = Color(0xFF70778A)
    val SuccessGreen = Color(0xFF168A61)
    val SuccessGreenLight = Color(0xFFE0F4EC)
    val DarkSnakeMouth = Color(0xFF141F36)
    val SubtleShadow = Color(0x24202747)
    val SoftHighlight = Color(0x66FFFFFF)
}

object CitrusShapes {
    val Pill = RoundedCornerShape(999.dp)
    val CardLarge = RoundedCornerShape(26.dp)
    val CardMedium = RoundedCornerShape(20.dp)
    val CardSmall = RoundedCornerShape(14.dp)
    val CardMini = RoundedCornerShape(10.dp)
    val BoardShape = RoundedCornerShape(28.dp)
}

object CitrusTypography {
    val DisplayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 34.sp,
        letterSpacing = 0.5.sp
    )

    val DisplayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 26.sp
    )

    val TitleBold = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 20.sp
    )

    val Headline = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp
    )

    val Subtitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )

    val BodyBold = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )

    val Body = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp
    )

    val Caption = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp
    )

    val ButtonText = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 16.sp,
        letterSpacing = 1.sp
    )
}
