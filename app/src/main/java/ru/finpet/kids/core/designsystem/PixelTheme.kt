package ru.finpet.kids.core.designsystem

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Цвета Stardew Valley фермерского календаря
val PixelWoodDark = Color(0xFF351A0C)
val PixelWoodMedium = Color(0xFF6B3C1A)
val PixelWoodLight = Color(0xFF9E5C2D)
val PixelWoodBevel = Color(0xFFBD7B48)

val PixelParchmentLight = Color(0xFFFFF8E7)
val PixelParchmentMedium = Color(0xFFF5E4BE)
val PixelParchmentDark = Color(0xFFDFC490)
val PixelParchmentBorder = Color(0xFFC7A86E)

val PixelGoldBright = Color(0xFFFFC766)
val PixelGoldGlow = Color(0xFFFFD54F)
val PixelGoldDark = Color(0xFFC67D00)

val PixelTextDark = Color(0xFF26150B)
val PixelTextMuted = Color(0xFF4A3423)
val PixelGreenCrop = Color(0xFF2E7D32)
val PixelRedBerry = Color(0xFFC62828)
val PixelBlueWater = Color(0xFF1565C0)

/**
 * Резная пиксельная доска календаря и панелей на основе top_part, middle_part, bottom_part
 */
@Composable
fun StardewBoard(
    modifier: Modifier = Modifier,
    headerTitle: String? = null,
    headerIcon: Any? = "📅",
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .pixelPanelBackground()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        val uiScale = LocalUiScale.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = (10.dp * uiScale).coerceIn(6.dp, 10.dp),
                    vertical = (8.dp * uiScale).coerceIn(4.dp, 8.dp)
                )
        ) {
            if (headerTitle != null) {
                // Пиксельная плашка заголовка на основе left_part, middle_little_part, right_part
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pixelBarBackground()
                        .padding(
                            vertical = (6.dp * uiScale).coerceIn(3.dp, 8.dp),
                            horizontal = (8.dp * uiScale).coerceIn(4.dp, 12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val iconSize = (20.dp * uiScale).coerceIn(14.dp, 24.dp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        PixelIcon(icon = headerIcon, modifier = Modifier.size(iconSize))
                        Spacer(modifier = Modifier.width((5.dp * uiScale).coerceIn(2.dp, 6.dp)))
                        Text(
                            text = headerTitle,
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = (16f * uiScale).coerceIn(11f, 18f).sp,
                            color = PixelTextDark,
                            letterSpacing = (0.5f * uiScale).coerceIn(0.2f, 1f).sp
                        )
                        Spacer(modifier = Modifier.width((5.dp * uiScale).coerceIn(2.dp, 6.dp)))
                        PixelIcon(icon = headerIcon, modifier = Modifier.size(iconSize))
                    }
                }
                Spacer(modifier = Modifier.height((8.dp * uiScale).coerceIn(4.dp, 10.dp)))
            }
            content()
        }
    }
}

/**
 * Стандартизированный расчет масштабированного размера шрифта с учетом LocalUiScale.
 */
@Composable
fun scaledSp(base: Float, min: Float = base * 0.72f, max: Float = base * 1.25f): TextUnit {
    val uiScale = LocalUiScale.current
    return (base * uiScale).coerceIn(min, max).sp
}

@Composable
fun scaledSp(base: Int, min: Float = base.toFloat() * 0.72f, max: Float = base.toFloat() * 1.25f): TextUnit =
    scaledSp(base.toFloat(), min, max)

/**
 * Стандартизированный расчет масштабированного размера отступов/иконок (Dp) с учетом LocalUiScale.
 */
@Composable
fun scaledDp(base: Dp, min: Dp = base * 0.72f, max: Dp = base * 1.25f): Dp {
    val uiScale = LocalUiScale.current
    return (base * uiScale).coerceIn(min, max)
}

@JvmName("floatScaledSp")
@Composable
fun Float.scaledSp(min: Float = this * 0.72f, max: Float = this * 1.25f): TextUnit =
    scaledSp(this, min, max)

@JvmName("intScaledSp")
@Composable
fun Int.scaledSp(min: Float = this.toFloat() * 0.72f, max: Float = this.toFloat() * 1.25f): TextUnit =
    scaledSp(this.toFloat(), min, max)

@JvmName("dpScaledDp")
@Composable
fun Dp.scaledDp(min: Dp = this * 0.72f, max: Dp = this * 1.25f): Dp =
    scaledDp(this, min, max)

/**
 * Универсальный компонент отображения пиксельных иконок.
 * Поддерживает строковые ключи / эмодзи, Int ID графических ресурсов (R.drawable.*)
 * и автоматически подставляет аутентичные нарисованные иконки вместо стикеров.
 */
@Composable
fun PixelIcon(
    icon: Any?,
    modifier: Modifier = Modifier.size(18.dp),
    contentDescription: String? = null,
    tint: Color? = null
) {
    when (icon) {
        null -> Unit
        is Int -> {
            Image(
                painter = painterResource(id = icon),
                contentDescription = contentDescription,
                modifier = modifier,
                colorFilter = tint?.let { ColorFilter.tint(it) }
            )
        }
        is String -> {
            RenderBoardIcon(icon = icon, modifier = modifier, tint = tint)
        }
        else -> Unit
    }
}

@Composable
fun PixelIcon(
    iconRes: Int,
    modifier: Modifier = Modifier.size(18.dp),
    contentDescription: String? = null,
    tint: Color? = null
) {
    Image(
        painter = painterResource(id = iconRes),
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = tint?.let { ColorFilter.tint(it) }
    )
}

@Composable
fun RenderBoardIcon(
    icon: String,
    modifier: Modifier = Modifier.size(26.dp),
    tint: Color? = null
) {
    when (icon) {
        "📅", "calendar" -> CalendarIcon(modifier = modifier)
        "📊", "diagram", "chart" -> DiagramIcon(modifier = modifier)
        "⚠️", "warning" -> WarningIcon(modifier = modifier)
        "⚡", "lightning" -> LightningIcon(modifier = modifier)
        "❤️", "heart" -> HeartIcon(modifier = modifier)
        "🛡️", "🛡", "shield" -> ShieldIcon(modifier = modifier)
        "🍎", "apple" -> AppleIcon(modifier = modifier)
        "❗", "!", "exclamation" -> ExclamationIcon(modifier = modifier)
        "🔒", "lock" -> LockIcon(modifier = modifier)
        "🎯", "target", "goal_default", "goal" -> TargetIcon(modifier = modifier)
        "🪙", "coin", "ic_coin" -> CoinIcon(modifier = modifier)
        "⭐", "🌟", "✨", "star", "sparkles", "celebration", "🎉" -> StarIcon(modifier = modifier)
        "📋", "📄", "📝", "docs", "note" -> DocsIcon(modifier = modifier)
        "👛", "koshelek", "wallet" -> KoshelekIcon(modifier = modifier)
        "🐾", "paws", "paw" -> PawsIcon(modifier = modifier)
        "⚖️", "⚖", "vesi", "scales" -> VesiIcon(modifier = modifier)
        "💡", "lampochka", "lamp", "hint" -> LampochkaIcon(modifier = modifier)
        "🍯", "🫙", "empty_jar", "🏦", "bank" -> EmptyJarIcon(modifier = modifier)
        "saving_jar" -> SavingJarIcon(modifier = modifier)
        "life_jar" -> LifeJarIcon(modifier = modifier)
        "joy_jar" -> JoyJarIcon(modifier = modifier)
        "🐷", "pig", "piggy" -> PigIcon(modifier = modifier)
        "🍲", "🥣", "miska", "food", "feed" -> MiskaIcon(modifier = modifier)
        "💬", "💭", "cloud" -> CloudIcon(modifier = modifier)
        "🎲", "kubik", "dice" -> KubikIcon(modifier = modifier)
        "😿", "cat_cry" -> CatCryIcon(modifier = modifier)
        "🐱", "😺", "😸", "cat", "cat_happy" -> CatHappyIcon(modifier = modifier)
        "🛒", "telezhka", "cart", "shop_cart" -> TelezhkaIcon(modifier = modifier)
        "📚", "books", "book", "school_book" -> BooksIcon(modifier = modifier)
        "💰", "money_bag", "money" -> MoneyBagIcon(modifier = modifier)
        "🎈", "toy", "toys", "balloon" -> ToyIcon(modifier = modifier)
        "🏷️", "discount", "happy_mood_3vryad", "happy_cat" -> HappyMood3vryadIcon(modifier = modifier)
        "sad_mood_3vryad", "sad_cat" -> SadMood3vryadIcon(modifier = modifier)
        "🏠", "🏡", "house", "home" -> HouseIcon(modifier = modifier)
        "🏫", "🎓", "school" -> SchoolIcon(modifier = modifier)
        "🏪", "🏬", "shop", "store" -> ShopIcon(modifier = modifier)
        "🚪", "wardrobe" -> WardrobeIcon(modifier = modifier)
        "✓", "✔", "✅", "galochka", "check", "done" -> GalochkaIcon(modifier = modifier)
        "✕", "✖", "❌", "close", "cross" -> CloseSignIcon(modifier = modifier)
        "⚽", "ball", "goal_ball" -> BallIcon(modifier = modifier)
        "🎧", "headphones", "goal_headphones" -> HeadphonesIcon(modifier = modifier)
        "🛴", "scooter", "samokat", "goal_scooter" -> ScooterIcon(modifier = modifier)
        "🗑️", "🗑", "bin", "trash", "musor" -> BinIcon(modifier = modifier)
        "🍽️", "dishes", "washing_dishes", "plates" -> DishesIcon(modifier = modifier)
        "🔮", "orb" -> OrbIcon(modifier = modifier)
        "🍂", "autumn", "autumn_game" -> AutumnIcon(modifier = modifier)
        "cat_runaway" -> CatRunawayIcon(modifier = modifier)
        "🔄", "↺", "refresh", "repeat", "sync" -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Text(
                    text = "↺",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = tint ?: PixelTextDark
                )
            }
        }
        "💧", "water" -> WaterIcon(modifier = modifier)
        "💊", "pill", "medicine" -> HeartIcon(modifier = modifier)
        "🧼", "soap", "clean" -> DishesIcon(modifier = modifier)
        "" -> Unit
        else -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

enum class PixelIconPosition {
    Leading,
    Trailing,
    Center
}

// Пиксельная кнопка на основе button_left_part, button_middle_little_part, button_right_part.
@Composable
fun PixelButton(
    text: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = PixelGoldBright,
    textColor: Color = PixelTextDark,
    borderColor: Color = PixelGoldDark,
    enabled: Boolean = true,
    fontSize: TextUnit = 16.sp,
    maxLines: Int = 2,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
    icon: Any? = null,
    iconRes: Int? = null,
    iconSize: Dp? = null,
    iconPosition: PixelIconPosition = PixelIconPosition.Leading,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    repeating: Boolean = false,
    repeatInitialDelayMillis: Long = 350L,
    repeatIntervalMillis: Long = 100L,
    content: (@Composable RowScope.() -> Unit)? = null
) {
    val uiScale = LocalUiScale.current
    val actualBg = if (enabled) containerColor else Color(0xFFC0B098)
    val actualText = if (enabled) textColor else Color(0xFF7A6855)

    // Адаптивное масштабирование внутренних отступов и иконок кнопки под плотность экрана (Density)
    val scaledPadding = remember(contentPadding, uiScale) {
        if (uiScale < 0.95f) {
            PaddingValues(
                start = (contentPadding.calculateStartPadding(LayoutDirection.Ltr) * uiScale).coerceAtLeast(3.dp),
                top = (contentPadding.calculateTopPadding() * uiScale).coerceAtLeast(2.dp),
                end = (contentPadding.calculateEndPadding(LayoutDirection.Ltr) * uiScale).coerceAtLeast(3.dp),
                bottom = (contentPadding.calculateBottomPadding() * uiScale).coerceAtLeast(2.dp)
            )
        } else if (uiScale > 1.05f) {
            PaddingValues(
                start = (contentPadding.calculateStartPadding(LayoutDirection.Ltr) * uiScale).coerceAtMost(contentPadding.calculateStartPadding(LayoutDirection.Ltr) * 1.25f),
                top = (contentPadding.calculateTopPadding() * uiScale).coerceAtMost(contentPadding.calculateTopPadding() * 1.25f),
                end = (contentPadding.calculateEndPadding(LayoutDirection.Ltr) * uiScale).coerceAtMost(contentPadding.calculateEndPadding(LayoutDirection.Ltr) * 1.25f),
                bottom = (contentPadding.calculateBottomPadding() * uiScale).coerceAtMost(contentPadding.calculateBottomPadding() * 1.25f)
            )
        } else {
            contentPadding
        }
    }
    val iconSpacing = (6.dp * uiScale).coerceIn(2.dp, 6.dp)
    val effectiveFontSize = remember(fontSize, uiScale) {
        if (uiScale < 0.95f || uiScale > 1.05f) {
            (fontSize.value * uiScale).coerceIn(fontSize.value * 0.72f, fontSize.value * 1.25f).sp
        } else {
            fontSize
        }
    }

    val defaultIconSize = iconSize ?: (16.dp * uiScale).coerceIn(12.dp, 22.dp)
    val targetIcon = icon ?: iconRes
    val resolvedIcon: (@Composable () -> Unit)? = targetIcon?.let {
        { PixelIcon(icon = it, modifier = Modifier.size(defaultIconSize)) }
    }

    val actualLeadingIcon = leadingIcon ?: if (iconPosition == PixelIconPosition.Leading) resolvedIcon else null
    val actualTrailingIcon = trailingIcon ?: if (iconPosition == PixelIconPosition.Trailing) resolvedIcon else null

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "btn_press_scale"
    )

    val currentOnClick by rememberUpdatedState(onClick)
    val currentEnabled by rememberUpdatedState(enabled)

    val clickModifier = if (repeating) {
        Modifier.pointerInput(enabled) {
            if (!enabled) return@pointerInput
            coroutineScope {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val press = PressInteraction.Press(down.position)
                    launch { interactionSource.emit(press) }
                    val job = launch {
                        currentOnClick()
                        delay(repeatInitialDelayMillis)
                        while (isActive && currentEnabled) {
                            currentOnClick()
                            delay(repeatIntervalMillis)
                        }
                    }
                    val up = waitForUpOrCancellation()
                    job.cancel()
                    launch {
                        if (up != null) {
                            interactionSource.emit(PressInteraction.Release(press))
                        } else {
                            interactionSource.emit(PressInteraction.Cancel(press))
                        }
                    }
                }
            }
        }
    } else {
        Modifier.clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pixelButtonBackground(containerColor = actualBg)
            .then(clickModifier)
            .padding(scaledPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (content != null) {
                content()
            } else if (text.isBlank()) {
                // Если текста нет, отображаем иконку прямо по центру кнопки
                val centerIcon = if (iconPosition == PixelIconPosition.Center && resolvedIcon != null) {
                    resolvedIcon
                } else {
                    actualLeadingIcon ?: actualTrailingIcon ?: resolvedIcon
                }
                centerIcon?.invoke()
            } else {
                if (actualLeadingIcon != null) {
                    actualLeadingIcon()
                    Spacer(modifier = Modifier.width(iconSpacing))
                }
                Text(
                    text = text,
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = effectiveFontSize,
                    color = actualText,
                    textAlign = TextAlign.Center,
                    maxLines = maxLines
                )
                if (actualTrailingIcon != null) {
                    Spacer(modifier = Modifier.width(iconSpacing))
                    actualTrailingIcon()
                }
            }
        }
    }
}

/**
 * Перегрузка PixelButton с графическим ресурсом (R.drawable.*) иконки.
 */
@Composable
fun PixelButton(
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = PixelGoldBright,
    textColor: Color = PixelTextDark,
    borderColor: Color = PixelGoldDark,
    enabled: Boolean = true,
    iconSize: Dp? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
) {
    PixelButton(
        text = "",
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        textColor = textColor,
        borderColor = borderColor,
        enabled = enabled,
        contentPadding = contentPadding,
        iconRes = iconRes,
        iconSize = iconSize,
        iconPosition = PixelIconPosition.Center
    )
}

/**
 * Перегрузка PixelButton с composable-контентом для удобного размещения любой разметки/иконки по центру.
 */
@Composable
fun PixelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = PixelGoldBright,
    textColor: Color = PixelTextDark,
    borderColor: Color = PixelGoldDark,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
    repeating: Boolean = false,
    repeatInitialDelayMillis: Long = 350L,
    repeatIntervalMillis: Long = 100L,
    content: @Composable RowScope.() -> Unit
) {
    PixelButton(
        text = "",
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        textColor = textColor,
        borderColor = borderColor,
        enabled = enabled,
        contentPadding = contentPadding,
        repeating = repeating,
        repeatInitialDelayMillis = repeatInitialDelayMillis,
        repeatIntervalMillis = repeatIntervalMillis,
        content = content
    )
}

/**
 * Аутентичный пиксельный диалог в стиле Stardew Valley.
 */
@Composable
fun PixelAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    icon: Any? = "⚠️",
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FinPetTheme {
            KeepScreenImmersive()
            val configuration = LocalConfiguration.current
            val maxDialogHeight = (configuration.screenHeightDp * 0.85f).dp

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .heightIn(max = maxDialogHeight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    StardewBoard(
                        headerTitle = title,
                        headerIcon = icon,
                        modifier = modifier.heightIn(max = maxDialogHeight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxDialogHeight - 70.dp)
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false)
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                content()
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (dismissButton != null) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        dismissButton()
                                    }
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    confirmButton()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
