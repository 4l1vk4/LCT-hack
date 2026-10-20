package ru.finpet.kids.core.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import ru.finpet.kids.R

@Composable
fun LockIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.lock_emodji),
        contentDescription = "Замок",
        modifier = modifier
    )
}

@Composable
fun TargetIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.target_emodji),
        contentDescription = "Цель",
        modifier = modifier
    )
}

/**
 * Интерактивная банка с пиксельным наполнением.
 * Высота заполнения: 40px при 64x64 (10 делений по 4px).
 * fullfill_the_jar.png: 42x4px, цвет 0xFFD95763.
 *
 * @param jarRes ресурс банки (R.drawable.life_jar, R.drawable.joy_jar, R.drawable.saving_jar)
 * @param fillDivisions уровень наполнения от 0 до 10 делений
 */
@Composable
fun PixelJar(
    jarRes: Int,
    fillDivisions: Int,
    modifier: Modifier = Modifier.size(64.dp),
    fillColor: Color? = null
) {
    val defaultFillColor = when (jarRes) {
        R.drawable.life_jar -> PixelGreenCrop
        R.drawable.joy_jar -> PixelGoldDark
        R.drawable.saving_jar -> PixelBlueWater
        else -> Color(0xFFD95763)
    }
    val effectiveColor = fillColor ?: defaultFillColor
    val clamped = fillDivisions.coerceIn(0, 10)
    val animFill by animateFloatAsState(
        targetValue = clamped.toFloat(),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "pixel_jar_anim"
    )
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / 64f
            val scaleY = size.height / 64f
            if (animFill > 0f) {
                val fillLeft = 11f * scaleX
                val fillWidth = 42f * scaleX
                val fillHeight = (animFill * 4f) * scaleY
                val fillTop = (57f - animFill * 4f) * scaleY
                drawRect(
                    color = effectiveColor,
                    topLeft = Offset(fillLeft, fillTop),
                    size = Size(fillWidth, fillHeight)
                )
            }
        }
        Image(
            painter = painterResource(id = jarRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun CoinIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.ic_coin),
        contentDescription = "Монета",
        modifier = modifier
    )
}

@Composable
fun CalendarIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.calender_emodji),
        contentDescription = "Календарь",
        modifier = modifier
    )
}

@Composable
fun HeartIcon(
    modifier: Modifier = Modifier.size(18.dp),
    tint: Color? = null
) {
    val colorFilter = remember(tint) {
        if (tint != null) {
            val cm = ColorMatrix().apply {
                setToSaturation(0f)
                timesAssign(
                    ColorMatrix(
                        floatArrayOf(
                            0.12f, 0f, 0f, 0f, 0f,
                            0f, 0.12f, 0f, 0f, 0f,
                            0f, 0f, 0.12f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                )
            }
            ColorFilter.colorMatrix(cm)
        } else {
            null
        }
    }
    Image(
        painter = painterResource(id = R.drawable.heart_emodji),
        contentDescription = "Здоровье",
        colorFilter = colorFilter,
        modifier = modifier.then(
            if (tint != null) Modifier.graphicsLayer { alpha = 0.55f } else Modifier
        )
    )
}

@Composable
fun WarningIcon(
    modifier: Modifier = Modifier.size(18.dp),
    animate: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "warning_anim")
    val scale by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.10f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "warning_scale"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    Image(
        painter = painterResource(id = R.drawable.warning_sign_emodji),
        contentDescription = "Внимание",
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    )
}

@Composable
fun LightningIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.lightning_emodji),
        contentDescription = "Энергия",
        modifier = modifier
    )
}

@Composable
fun DiagramIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.diagramm_emodji),
        contentDescription = "Диаграмма",
        modifier = modifier
    )
}

@Composable
fun ExclamationIcon(
    modifier: Modifier = Modifier.size(18.dp),
    animate: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "exclamation_anim")
    val bounceY by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -3f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bounceY"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }
    val scale by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    Image(
        painter = painterResource(id = R.drawable.exclimation_sign_emodji),
        contentDescription = "Важно",
        modifier = modifier.graphicsLayer {
            translationY = bounceY.dp.toPx()
            scaleX = scale
            scaleY = scale
        }
    )
}

@Composable
fun ShieldIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.shield_emodji),
        contentDescription = "Родительский режим",
        modifier = modifier
    )
}

@Composable
fun AppleIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.apple_emodji),
        contentDescription = "Сытость",
        modifier = modifier
    )
}

@Composable
fun StarIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.star_emodji),
        contentDescription = "Звезда",
        modifier = modifier
    )
}

@Composable
fun DocsIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.docs_emodji),
        contentDescription = "Документ",
        modifier = modifier
    )
}

@Composable
fun KoshelekIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.koshelek),
        contentDescription = "Кошелек",
        modifier = modifier
    )
}

@Composable
fun PawsIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.paws),
        contentDescription = "Лапки",
        modifier = modifier
    )
}

@Composable
fun VesiIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.vesi),
        contentDescription = "Весы",
        modifier = modifier
    )
}

@Composable
fun LampochkaIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.lampochka),
        contentDescription = "Лампочка",
        modifier = modifier
    )
}

@Composable
fun LifeJarIcon(
    modifier: Modifier = Modifier.size(24.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.life_jar),
        contentDescription = "Банка обязательных расходов",
        modifier = modifier
    )
}

@Composable
fun JoyJarIcon(
    modifier: Modifier = Modifier.size(24.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.joy_jar),
        contentDescription = "Банка желаний",
        modifier = modifier
    )
}

@Composable
fun EmptyJarIcon(
    modifier: Modifier = Modifier.size(24.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.empty_jar),
        contentDescription = "Банка",
        modifier = modifier
    )
}

@Composable
fun SavingJarIcon(
    modifier: Modifier = Modifier.size(24.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.saving_jar),
        contentDescription = "Банка-копилка со свинкой",
        modifier = modifier
    )
}

@Composable
fun PigIcon(
    modifier: Modifier = Modifier.size(24.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.pig),
        contentDescription = "Свинка",
        modifier = modifier
    )
}

@Composable
fun MiskaIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.miska),
        contentDescription = "Миска",
        modifier = modifier
    )
}

@Composable
fun CloudIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.cloud_emodji),
        contentDescription = "Мысли",
        modifier = modifier
    )
}

@Composable
fun KubikIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.kubik_emodji),
        contentDescription = "Кубик",
        modifier = modifier
    )
}

@Composable
fun CatCryIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.cat_cry_emodji),
        contentDescription = "Грустный котик",
        modifier = modifier
    )
}

@Composable
fun CatHappyIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.cat_happy_emodji),
        contentDescription = "Веселый котик",
        modifier = modifier
    )
}

@Composable
fun TelezhkaIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.telezhka),
        contentDescription = "Тележка",
        modifier = modifier
    )
}

@Composable
fun BooksIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.books_emodji),
        contentDescription = "Книжки",
        modifier = modifier
    )
}

@Composable
fun MoneyBagIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.money_bag_emodji),
        contentDescription = "Мешок монет",
        modifier = modifier
    )
}

@Composable
fun ToyIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.toy),
        contentDescription = "Игрушка",
        modifier = modifier
    )
}

@Composable
fun HappyMood3vryadIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.happy_mood_3vryad),
        contentDescription = "Радостный котик",
        modifier = modifier
    )
}

@Composable
fun SadMood3vryadIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.sad_mood_3vryad),
        contentDescription = "Грустный котик",
        modifier = modifier
    )
}

@Composable
fun HouseIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.house),
        contentDescription = "Домик",
        modifier = modifier
    )
}

@Composable
fun SchoolIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.school),
        contentDescription = "Школа",
        modifier = modifier
    )
}

@Composable
fun ShopIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.shop),
        contentDescription = "Лавка",
        modifier = modifier
    )
}

@Composable
fun WardrobeIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.wardrobe),
        contentDescription = "Шкаф",
        modifier = modifier
    )
}

@Composable
fun GalochkaIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.galochka),
        contentDescription = "Галочка",
        modifier = modifier
    )
}

@Composable
fun CloseSignIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.close_sign),
        contentDescription = "Закрыть",
        modifier = modifier
    )
}

@Composable
fun BallIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.ball),
        contentDescription = "Мяч",
        modifier = modifier
    )
}

@Composable
fun HeadphonesIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.headphones),
        contentDescription = "Наушники",
        modifier = modifier
    )
}

@Composable
fun ScooterIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.samokat),
        contentDescription = "Самокат",
        modifier = modifier
    )
}

@Composable
fun BinIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.bin),
        contentDescription = "Корзина",
        modifier = modifier
    )
}

@Composable
fun DishesIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.washing_dishes),
        contentDescription = "Посуда",
        modifier = modifier
    )
}

@Composable
fun OrbIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.orb),
        contentDescription = "Орб",
        modifier = modifier
    )
}

@Composable
fun AutumnIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.autumn_game),
        contentDescription = "Осень",
        modifier = modifier
    )
}

@Composable
fun CatRunawayIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.cat_runaway),
        contentDescription = "Питомец ушёл погулять",
        modifier = modifier
    )
}

@Composable
fun WaterIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.blue_ball),
        contentDescription = "Вода",
        modifier = modifier
    )
}

/**
 * Аутентичный пиксельный чекбокс на основе checkbox.png и check.png
 */
@Composable
fun PixelCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 28.dp
) {
    val imageRes = if (checked) R.drawable.check else R.drawable.checkbox
    val clickableModifier = if (onCheckedChange != null && enabled) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onCheckedChange(!checked) }
    } else Modifier

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = if (checked) "Выбрано" else "Не выбрано",
        modifier = modifier
            .size(size)
            .then(clickableModifier)
    )
}

/**
 * Аутентичный пиксельный переключатель на основе on_.png и off_.png
 */
@Composable
fun PixelSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val imageRes = if (checked) R.drawable.on_ else R.drawable.off_
    val clickableModifier = if (onCheckedChange != null && enabled) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onCheckedChange(!checked) }
    } else Modifier

    Box(
        modifier = modifier
            .size(width = 56.dp, height = 32.dp)
            .clipToBounds()
            .then(clickableModifier),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = if (checked) "Включено" else "Выключено",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
    }
}


/**
 * Панель карточки в пиксельном стиле на основе top_part, middle_part, bottom_part
 */
@Composable
fun FinCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = CardSurface,
    borderColor: Color = Color(0xFFEEEEEE),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .pixelPanelBackground(
                containerColor = if (backgroundColor != CardSurface && backgroundColor != Color.Unspecified) backgroundColor else null
            )
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        content()
    }
}

/**
 * Текст, в котором эмодзи 🪙 автоматически заменяются на нарисованную пиксельную монетку CoinIcon
 */
@Composable
fun RichFeedbackText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    color: Color = TextPrimary,
    fontWeight: FontWeight = FontWeight.Normal,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign = TextAlign.Center,
    lineHeight: TextUnit = 21.sp
) {
    val coinPlaceholderId = "DRAWN_COIN"
    val annotatedString = buildAnnotatedString {
        val parts = text.split("🪙")
        for (i in parts.indices) {
            append(parts[i])
            if (i < parts.size - 1) {
                appendInlineContent(coinPlaceholderId, "[coin]")
            }
        }
    }

    val iconSize = (fontSize.value * 1.15f).sp
    val inlineContent = mapOf(
        coinPlaceholderId to InlineTextContent(
            Placeholder(
                width = iconSize,
                height = iconSize,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            CoinIcon(modifier = Modifier.fillMaxSize())
        }
    )

    Text(
        text = annotatedString,
        inlineContent = inlineContent,
        fontSize = fontSize,
        color = color,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        textAlign = textAlign,
        lineHeight = lineHeight,
        modifier = modifier
    )
}

/**
 * Пиксельный слайдер на основе shkala.png (фон) и dvigatel.png (бегунок).
 * Заполнение прогресса отображается слева от ползунка в фирменном полукруглом стиле.
 */
@Composable
fun PixelSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val uiScale = LocalUiScale.current
    val sliderHeight = (26.dp * uiScale).coerceIn(20.dp, 30.dp)

    BoxWithConstraints(
        modifier = modifier
            .height(sliderHeight)
            .fillMaxWidth()
    ) {
        val density = LocalDensity.current
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = with(density) { sliderHeight.toPx() }
        val thumbWidthPx = heightPx * (58f / 81f)
        val maxTravelPx = maxOf(1f, widthPx - thumbWidthPx)

        val rangeSpan = valueRange.endInclusive - valueRange.start
        val fraction = if (rangeSpan > 0f) {
            ((value - valueRange.start) / rangeSpan).coerceIn(0f, 1f)
        } else 0f

        val updateValueFromPx: (Float) -> Unit = { xPx ->
            val clampedX = (xPx - thumbWidthPx / 2f).coerceIn(0f, maxTravelPx)
            val newFraction = clampedX / maxTravelPx
            val newValue = valueRange.start + newFraction * rangeSpan
            onValueChange(newValue)
        }

        val thumbOffsetPx = fraction * maxTravelPx
        val fillWidthPx = thumbOffsetPx + thumbWidthPx * (38f / 58f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(percent = 50))
                .pointerInput(enabled, maxTravelPx) {
                    if (!enabled) return@pointerInput
                    detectTapGestures { offset ->
                        updateValueFromPx(offset.x)
                    }
                }
                .pointerInput(enabled, maxTravelPx) {
                    if (!enabled) return@pointerInput
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        updateValueFromPx(change.position.x)
                    }
                }
        ) {
            // Фон шкалы
            Image(
                painter = painterResource(id = R.drawable.shkala),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            // Заполнение шкалы слева до бегунка
            if (fraction > 0.001f) {
                val fillWidthDp = with(density) { fillWidthPx.toDp() }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(fillWidthDp)
                        .background(Color(0xFF8A6F30))
                )
            }

            // Бегунок-двигатель с полукругом слева и разделителем
            val thumbOffsetDp = with(density) { thumbOffsetPx.toDp() }
            Image(
                painter = painterResource(id = R.drawable.dvigatel),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = thumbOffsetDp)
                    .fillMaxHeight()
                    .aspectRatio(58f / 81f),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}