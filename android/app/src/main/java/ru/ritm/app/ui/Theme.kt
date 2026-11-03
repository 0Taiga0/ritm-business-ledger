package ru.ritm.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Ink = Color(0xFF233B39)
val Forest = Color(0xFF2A6653)
val Mint = Color(0xFFB8E7CE)
val Muted = Color(0xFF667E73)
val Paper = Color(0xFFF4F6F4)
val Line = Color(0xFFE5EBE6)
val Sand = Color(0xFFD9BBA1)

@Composable
fun RitmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme =
            lightColorScheme(
                primary = Forest,
                onPrimary = Color.White,
                primaryContainer = Mint,
                onPrimaryContainer = Ink,
                secondary = Color(0xFF8B9C70),
                secondaryContainer = Color(0xFFE4F1E7),
                onSecondaryContainer = Forest,
                background = Paper,
                surface = Color.White,
                onSurface = Ink,
                outline = Line,
                surfaceVariant = Paper,
                surfaceContainer = Paper,
                surfaceContainerLow = Paper,
                surfaceContainerHigh = Paper,
                surfaceContainerHighest = Paper,
                onSurfaceVariant = Muted,
            ),
        typography =
            Typography(
                headlineLarge =
                    TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = (-1).sp,
                    ),
                headlineMedium =
                    TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        letterSpacing = (-0.6).sp,
                    ),
                titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                bodyLarge = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
                bodyMedium = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
                labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                labelSmall = TextStyle(fontSize = 10.sp, lineHeight = 14.sp),
            ),
        content = content,
    )
}
