package com.example.snakegame

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Serpentine: Citrus Pop Design System tokens & palette.
 * Reference: Warm cream, vibrant tomato coral, citrus lime, deep cobalt, warm yellow, and deep ink.
 */
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

    // Secondary tones
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
        color = CitrusColors.DeepCobalt,
        letterSpacing = 0.5.sp
    )

    val DisplayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 26.sp,
        color = CitrusColors.DeepCobalt
    )

    val TitleBold = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 20.sp,
        color = CitrusColors.DeepInk
    )

    val Headline = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = CitrusColors.DeepInk
    )

    val Subtitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = CitrusColors.SecondaryMutedText
    )

    val BodyBold = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = CitrusColors.DeepInk
    )

    val Body = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        color = CitrusColors.SecondaryMutedText
    )

    val Caption = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        color = CitrusColors.SecondaryMutedText
    )

    val ButtonText = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 16.sp,
        letterSpacing = 1.sp
    )
}
