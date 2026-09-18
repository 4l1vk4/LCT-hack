package ru.finpet.kids.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SunnyYellow = Color(0xFFFFD54F)
val JoyOrange = Color(0xFFFF9800)
val FreshGreen = Color(0xFF4CAF50)
val SkyBlue = Color(0xFF42A5F5)
val SoftBackground = Color(0xFFFFFDF7)
val CardSurface = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF2E384D)
val TextSecondary = Color(0xFF8798AD)
val ErrorRed = Color(0xFFE53935)

private val LightColorScheme = lightColorScheme(
    primary = JoyOrange,
    onPrimary = Color.White,
    primaryContainer = SunnyYellow,
    onPrimaryContainer = TextPrimary,
    secondary = SkyBlue,
    onSecondary = Color.White,
    tertiary = FreshGreen,
    onTertiary = Color.White,
    background = SoftBackground,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun FinPetTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
