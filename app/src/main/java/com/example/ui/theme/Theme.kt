package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Parse custom colors safely
fun parseColorHex(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFF0D9488) // default Teal
    }
}

// Generates an interactive dynamic light color scheme from a base color
fun getCustomLightScheme(primaryColor: Color): ColorScheme {
    return lightColorScheme(
        primary = primaryColor,
        onPrimary = Color.White,
        primaryContainer = primaryColor.copy(alpha = 0.12f),
        onPrimaryContainer = primaryColor,
        secondary = primaryColor.copy(alpha = 0.8f),
        onSecondary = Color.White,
        background = Color(0xFFFAFAFA),
        surface = Color.White,
        onBackground = Color(0xFF1E1E1E),
        onSurface = Color(0xFF1E1E1E),
        surfaceVariant = Color(0xFFF2F2F2),
        onSurfaceVariant = Color(0xFF424242)
    )
}

// Generates an interactive dynamic dark color scheme from a base color
fun getCustomDarkScheme(primaryColor: Color): ColorScheme {
    return darkColorScheme(
        primary = primaryColor,
        onPrimary = Color.Black,
        primaryContainer = primaryColor.copy(alpha = 0.2f),
        onPrimaryContainer = primaryColor,
        secondary = primaryColor.copy(alpha = 0.8f),
        onSecondary = Color.Black,
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        onBackground = Color(0xFFE3E3E3),
        onSurface = Color(0xFFE3E3E3),
        surfaceVariant = Color(0xFF2C2C2C),
        onSurfaceVariant = Color(0xFFC4C4C4)
    )
}

// Generate dynamic Typography
fun getCustomTypography(fontFamily: FontFamily): Typography {
    return Typography(
        displayLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 57.sp, lineHeight = 64.sp),
        displayMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 45.sp, lineHeight = 52.sp),
        displaySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 36.sp, lineHeight = 44.sp),
        headlineLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
        headlineMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 28.sp, lineHeight = 36.sp),
        headlineSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 32.sp),
        titleLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
        titleMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
        titleSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
        bodyMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
        bodySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
        labelLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        labelMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
        labelSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean,
    themeColorHex: String,
    content: @Composable () -> Unit
) {
    val primaryColor = parseColorHex(themeColorHex)
    val colorScheme = if (darkTheme) {
        getCustomDarkScheme(primaryColor)
    } else {
        getCustomLightScheme(primaryColor)
    }

    val actualFontFamily = FontFamily.Default

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getCustomTypography(actualFontFamily),
        content = content
    )
}
