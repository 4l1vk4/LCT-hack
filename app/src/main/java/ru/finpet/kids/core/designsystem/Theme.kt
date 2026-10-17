package ru.finpet.kids.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ru.finpet.kids.R
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

val SunnyYellow = Color(0xFFFFD54F)
val JoyOrange = Color(0xFFFF9800)
val FreshGreen = Color(0xFF4CAF50)
val SkyBlue = Color(0xFF42A5F5)
val SoftBackground = Color(0xFFE8DAC2)
val CardSurface = Color(0xFFE8DAC2)
val TextPrimary = Color(0xFF2E384D)
val TextSecondary = Color(0xFF8798AD)
val ErrorRed = Color(0xFFE53935)

val AppFontFamily = FontFamily(
    Font(R.font.epilepsy_sans, FontWeight.Normal),
    Font(R.font.epilepsy_sans, FontWeight.Light),
    Font(R.font.epilepsy_sans, FontWeight.Medium),
    Font(R.font.epilepsy_sans_bold, FontWeight.SemiBold),
    Font(R.font.epilepsy_sans_bold, FontWeight.Bold),
    Font(R.font.epilepsy_sans_bold, FontWeight.ExtraBold),
    Font(R.font.epilepsy_sans_bold, FontWeight.Black)
)

private val AppTypography = Typography(
    displayLarge   = Typography().displayLarge.copy(fontFamily = AppFontFamily),
    displayMedium  = Typography().displayMedium.copy(fontFamily = AppFontFamily),
    displaySmall   = Typography().displaySmall.copy(fontFamily = AppFontFamily),

    headlineLarge  = Typography().headlineLarge.copy(fontFamily = AppFontFamily),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = AppFontFamily),
    headlineSmall  = Typography().headlineSmall.copy(fontFamily = AppFontFamily),

    titleLarge     = Typography().titleLarge.copy(fontFamily = AppFontFamily),
    titleMedium    = Typography().titleMedium.copy(fontFamily = AppFontFamily),
    titleSmall     = Typography().titleSmall.copy(fontFamily = AppFontFamily),

    bodyLarge      = Typography().bodyLarge.copy(fontFamily = AppFontFamily),
    bodyMedium     = Typography().bodyMedium.copy(fontFamily = AppFontFamily),
    bodySmall      = Typography().bodySmall.copy(fontFamily = AppFontFamily),

    labelLarge     = Typography().labelLarge.copy(fontFamily = AppFontFamily),
    labelMedium    = Typography().labelMedium.copy(fontFamily = AppFontFamily),
    labelSmall     = Typography().labelSmall.copy(fontFamily = AppFontFamily)
)

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
    val currentDensity = LocalDensity.current
    // пиксельный шрифт Epilepsy Sans оптически компактнее обычных шрифтов,
    // масштабируем fontScale на ~18% (+2..4sp), чтобы текст читался крупно и четко
    val scaledDensity = Density(
        density = currentDensity.density,
        fontScale = currentDensity.fontScale * 1.18f
    )

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography
    ) {
        CompositionLocalProvider(
            LocalDensity provides scaledDensity,
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = AppFontFamily)
        ) {
            content()
        }
    }
}
