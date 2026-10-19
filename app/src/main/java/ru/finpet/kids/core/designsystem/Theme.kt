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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Density

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

val SunnyYellow = Color(0xFFFFD54F)
val JoyOrange = Color(0xFFFF9800)
val FreshGreen = Color(0xFF4CAF50)
val SkyBlue = Color(0xFF42A5F5)
val SoftBackground = Color(0xFFE8DAC2)
val CardSurface = Color(0xFFE8DAC2)
val TextPrimary = Color(0xFF26150B)
val TextSecondary = Color(0xFF4A3423)
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

/**
 * Единая система типографики приложения на базе пиксельного шрифта Epilepsy Sans.
 * Оптимизирована под масштабирование LocalDensity:
 * - Основной текст (Body) стандартизирован на 16.sp (как описание в магазине).
 * - Заголовки разделов и карточек стандартизированы на 18.sp.
 * - Заголовки экранов и шторок стандартизированы на 22.sp.
 * - Кнопки и бейджи на 16.sp - 17.sp.
 * - Мелкие подписи (Caption) не опускаются ниже 14.sp.
 */
object FinTypography {
    val ScreenTitle = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 28.sp
    )
    val CardHeader = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 24.sp
    )
    val Body = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 21.sp
    )
    val BodyBold = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 21.sp
    )
    val Button = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 20.sp
    )
    val Caption = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp
    )
    val CaptionBold = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 18.sp
    )
}

private val AppTypography = Typography(
    displayLarge   = Typography().displayLarge.copy(fontFamily = AppFontFamily),
    displayMedium  = Typography().displayMedium.copy(fontFamily = AppFontFamily),
    displaySmall   = Typography().displaySmall.copy(fontFamily = AppFontFamily),

    headlineLarge  = Typography().headlineLarge.copy(fontFamily = AppFontFamily, fontSize = 24.sp),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = AppFontFamily, fontSize = 22.sp),
    headlineSmall  = Typography().headlineSmall.copy(fontFamily = AppFontFamily, fontSize = 20.sp),

    titleLarge     = Typography().titleLarge.copy(fontFamily = AppFontFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold),
    titleMedium    = Typography().titleMedium.copy(fontFamily = AppFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold),
    titleSmall     = Typography().titleSmall.copy(fontFamily = AppFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold),

    bodyLarge      = Typography().bodyLarge.copy(fontFamily = AppFontFamily, fontSize = 17.sp, lineHeight = 22.sp),
    bodyMedium     = Typography().bodyMedium.copy(fontFamily = AppFontFamily, fontSize = 16.sp, lineHeight = 21.sp),
    bodySmall      = Typography().bodySmall.copy(fontFamily = AppFontFamily, fontSize = 14.sp, lineHeight = 18.sp),

    labelLarge     = Typography().labelLarge.copy(fontFamily = AppFontFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
    labelMedium    = Typography().labelMedium.copy(fontFamily = AppFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold),
    labelSmall     = Typography().labelSmall.copy(fontFamily = AppFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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

val LocalUiScale = androidx.compose.runtime.compositionLocalOf { 1f }

@Composable
fun FinPetTheme(
    content: @Composable () -> Unit
) {
    val currentDensity = LocalDensity.current
    val configuration = LocalConfiguration.current

    // Базовый дизайн рассчитан на эталонную ширину 390dp (стандарт современных мобильных макетов).
    val baseWidthDp = 390f
    val screenWidthDp = configuration.screenWidthDp.toFloat().coerceAtLeast(200f)

    // Динамический масштаб шрифта:
    // На эталонной ширине 390dp масштаб составляет 1.18f (оптимально для пиксельного шрифта Epilepsy Sans).
    // При уменьшении ширины экрана (или увеличении Density до 700, где ширина ~247dp)
    // fontScale пропорционально масштабируется вниз (до ~0.70f), благодаря чему текст всегда занимает
    // строго одинаковую долю экрана и никогда не вылезает за пределы кнопок, карточек и диалогов.
    // На планшетах и широких экранах (700dp+) масштаб аккуратно ограничивается (до 1.25f),
    // чтобы текст оставался гармоничным и не выглядел избыточно крупным.
    val widthRatio = (screenWidthDp / baseWidthDp).coerceIn(0.65f, 1.25f)
    val dynamicFontScale = (1.18f * widthRatio).coerceIn(0.68f, 1.25f)

    // Учитываем системную настройку размера шрифта пользователя (Accessibility),
    // защищая игровой интерфейс от поломки при экстремальных значениях.
    val systemScale = currentDensity.fontScale.coerceIn(0.85f, 1.15f)
    val effectiveFontScale = (dynamicFontScale * systemScale).coerceIn(0.60f, 1.30f)

    // ВАЖНО: density НЕ масштабируется (сохраняется currentDensity.density),
    // что сохраняет точную привязку к физическим пикселям экрана, WindowInsets,
    // 9-slice графике и предотвращает искажение размеров dp и канваса.
    val scaledDensity = Density(
        density = currentDensity.density,
        fontScale = effectiveFontScale
    )

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography
    ) {
        CompositionLocalProvider(
            LocalDensity provides scaledDensity,
            LocalUiScale provides widthRatio,
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = AppFontFamily)
        ) {
            content()
        }
    }
}
