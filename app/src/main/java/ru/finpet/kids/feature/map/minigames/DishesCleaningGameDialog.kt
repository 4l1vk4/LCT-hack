package ru.finpet.kids.feature.map.minigames

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.finpet.kids.R
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.pixelButtonBackground
import kotlin.math.roundToInt

private const val TOTAL_PLATES = 5

/**
 * Миниигра: «Мытье посуды».
 * На фоне стола с раковиной (washing_dishes), растянутом точно на полный размер диалога без рамок.
 * На раковине лежат 5 грязных тарелок.
 * При клике по тарелке в раковине она приближается по центру для мытья.
 * Игрок водит пальцем по тарелке или нажимает на неё, смывая грязь губкой (100% -> 67% -> 30% -> чистая).
 * Как только тарелка отмыта, она плавно перемещается наверх в стопку чистых тарелок.
 * Верхняя плавающая панель, нижняя кнопка и экран победы выполнены в едином стиле с уборкой листьев.
 */
@Composable
fun DishesCleaningGameDialog(
    rewardCoins: Int = 5,
    onCompleteGame: () -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var cleanedCount by remember { mutableIntStateOf(0) }

    var isPlateZoomed by remember { mutableStateOf(false) }
    var currentDirtyPercent by remember { mutableFloatStateOf(100f) }

    // Позиция губки
    var spongePosition by remember { mutableStateOf<Offset?>(null) }
    var isTouching by remember { mutableStateOf(false) }

    // Анимация перемещения тарелки наверх после мытья
    val movingPlateOffsetY = remember { Animatable(0f) }
    val movingPlateScale = remember { Animatable(1f) }
    var isFlyingToTable by remember { mutableStateOf(false) }

    var isWon by remember { mutableStateOf(false) }

    // Автоматическое завершение через 3.5 секунды после победы
    LaunchedEffect(isWon) {
        if (isWon) {
            delay(3500)
            onCompleteGame()
        }
    }

    // Пульсация кнопки победы
    val infiniteTransition = rememberInfiniteTransition(label = "winPulseDishes")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScaleDishes"
    )

    // Анимация покачивания подсказки губки
    val spongeBobbing by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spongeBobbing"
    )

    fun finishCleaningCurrentPlate() {
        if (isFlyingToTable) return
        isFlyingToTable = true
        spongePosition = null
        isTouching = false
        coroutineScope.launch {
            delay(160)
            launch {
                movingPlateOffsetY.animateTo(
                    targetValue = -1f,
                    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
                )
            }
            launch {
                movingPlateScale.animateTo(
                    targetValue = 0.58f,
                    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
                )
            }
            delay(430)

            cleanedCount++
            isPlateZoomed = false
            isFlyingToTable = false
            currentDirtyPercent = 100f
            movingPlateOffsetY.snapTo(0f)
            movingPlateScale.snapTo(1f)

            if (cleanedCount >= TOTAL_PLATES && !isWon) {
                delay(200)
                isWon = true
            }
        }
    }

    fun cleanByAmount(amount: Float) {
        if (currentDirtyPercent > 0f) {
            currentDirtyPercent = (currentDirtyPercent - amount).coerceAtLeast(0f)
            if (currentDirtyPercent <= 0f) {
                finishCleaningCurrentPlate()
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (isWon) onCompleteGame() else onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        KeepScreenImmersive()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (isWon) onCompleteGame()
                }
        ) {
            // 1. Фоновое изображение стола с раковиной — растянуто точно на полный размер диалога
            Image(
                painter = painterResource(R.drawable.washing_dishes),
                contentDescription = "Стол с раковиной",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            // 2. Игровое поле: раковина, тарелки и процесс мытья
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val containerWidth = maxWidth
                val containerHeight = maxHeight

                // Размеры тарелок
                val tablePlateSize = (containerWidth * 0.32f).coerceIn(100.dp, 160.dp)
                val sinkPlateSize = (containerWidth * 0.44f).coerceIn(140.dp, 220.dp)
                val zoomedPlateSize = (containerWidth * 0.70f).coerceIn(230.dp, 350.dp)

                // СТОПКА ЧИСТЫХ ТАРЕЛОК НА СТОЛЕ НАВЕРХУ
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = containerHeight * 0.13f)
                        .size(tablePlateSize + 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    for (i in 0 until cleanedCount) {
                        Image(
                            painter = painterResource(id = R.drawable.tarelka_clear),
                            contentDescription = "Чистая тарелка",
                            modifier = Modifier
                                .size(tablePlateSize)
                                .offset(y = (-i * 6).dp)
                        )
                    }
                }

                // ТАРЕЛКИ В РАКОВИНЕ
                val remainingInSink = TOTAL_PLATES - cleanedCount
                if (remainingInSink > 0) {
                    val currentPlateInSink = remainingInSink - 1

                    if (!isPlateZoomed && !isFlyingToTable) {
                        // Обычное состояние: стопка тарелок лежит в раковине
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(sinkPlateSize + 25.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    isPlateZoomed = true
                                    currentDirtyPercent = 100f
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            for (i in 0 until remainingInSink) {
                                Image(
                                    painter = painterResource(id = R.drawable.tarelka_dirty),
                                    contentDescription = "Грязная тарелка",
                                    modifier = Modifier
                                        .size(sinkPlateSize)
                                        .offset(y = (-i * 5).dp)
                                )
                            }

                            // Подсказка нажать на тарелку
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 22.dp)
                                    .pixelButtonBackground(containerColor = PixelParchmentLight)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Нажми 👆",
                                    fontFamily = AppFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PixelWoodDark
                                )
                            }
                        }
                    } else {
                        // Под активной тарелкой отображаем оставшиеся в стопке тарелки (если есть)
                        if (currentPlateInSink > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(sinkPlateSize + 25.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                for (i in 0 until currentPlateInSink) {
                                    Image(
                                        painter = painterResource(id = R.drawable.tarelka_dirty),
                                        contentDescription = "Грязная тарелка в раковине",
                                        modifier = Modifier
                                            .size(sinkPlateSize)
                                            .offset(y = (-i * 5).dp)
                                    )
                                }
                            }
                        }

                        // ВЫБРАННАЯ ТАРЕЛКА: ПРИБЛИЖЕНА, ОЧИЩАЕТСЯ
                        val plateRes = when {
                            currentDirtyPercent <= 5f -> R.drawable.tarelka_clear
                            currentDirtyPercent <= 48f -> R.drawable.tarelka_30
                            currentDirtyPercent <= 83f -> R.drawable.tarelka_67
                            else -> R.drawable.tarelka_dirty
                        }

                        val flyOffsetPx = movingPlateOffsetY.value * (containerHeight.value * 0.32f)

                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset { IntOffset(0, flyOffsetPx.roundToInt()) }
                                .scale(if (isFlyingToTable) movingPlateScale.value else 1f)
                                .size(zoomedPlateSize)
                                .pointerInput(isFlyingToTable) {
                                    if (isFlyingToTable) return@pointerInput
                                    detectTapGestures(
                                        onPress = { offset ->
                                            spongePosition = offset
                                            isTouching = true
                                            tryAwaitRelease()
                                            isTouching = false
                                        },
                                        onTap = {
                                            cleanByAmount(18f)
                                        }
                                    )
                                }
                                .pointerInput(isFlyingToTable) {
                                    if (isFlyingToTable) return@pointerInput
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            spongePosition = offset
                                            isTouching = true
                                        },
                                        onDragEnd = { isTouching = false },
                                        onDragCancel = { isTouching = false }
                                    ) { change, dragAmount ->
                                        change.consume()
                                        spongePosition = change.position
                                        val dist = dragAmount.getDistance()
                                        cleanByAmount(dist * 0.16f)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = plateRes),
                                contentDescription = "Тарелка на чистке",
                                modifier = Modifier.fillMaxSize()
                            )

                            // Губка и индикатор процесса мытья
                            if (!isFlyingToTable) {
                                val spongeOffset = spongePosition
                                if (isTouching && spongeOffset != null) {
                                    Text(
                                        text = "🧽",
                                        fontSize = 36.sp,
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .offset {
                                                IntOffset(
                                                    (spongeOffset.x - 20.dp.toPx()).roundToInt(),
                                                    (spongeOffset.y - 32.dp.toPx()).roundToInt()
                                                )
                                            }
                                    )
                                } else {
                                    // Подсказка с покачивающейся губкой
                                    Text(
                                        text = "🧽",
                                        fontSize = 34.sp,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .offset(y = spongeBobbing.dp)
                                    )
                                }

                                // Индикатор чистоты под тарелкой
                                val cleanPercent = (100f - currentDirtyPercent).roundToInt()
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .offset(y = 28.dp)
                                        .pixelButtonBackground(containerColor = if (cleanPercent >= 100) FreshGreen else PixelParchmentLight)
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (cleanPercent >= 100) "Чисто! ✨" else "Чистота: $cleanPercent%",
                                        fontFamily = AppFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (cleanPercent >= 100) Color.White else PixelWoodDark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Верхняя панель управления и счетчика (в едином стиле с игрой уборки листьев)
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PixelParchmentLight.copy(alpha = 0.94f))
                    .border(2.dp, PixelParchmentBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Мытье посуды",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = PixelWoodDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🍽️",
                            fontSize = 17.sp
                        )
                    }
                    Text(
                        text = if (!isPlateZoomed) "Нажми на тарелку в раковине, чтобы вымыть!" else "Води пальцем по тарелке, смывая грязь 🧽",
                        fontSize = 13.sp,
                        color = Color(0xFF6B4423)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Прогресс вымытых тарелок
                Box(
                    modifier = Modifier
                        .pixelButtonBackground(containerColor = if (cleanedCount == TOTAL_PLATES) FreshGreen else JoyOrange)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "$cleanedCount / $TOTAL_PLATES",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Кнопка выхода (крестик)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.close_sign),
                        contentDescription = "Закрыть",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // 4. Нижняя кнопка во время игры
            if (!isWon) {
                PixelButton(
                    text = "Закончить позже",
                    onClick = onDismiss,
                    containerColor = PixelWoodMedium.copy(alpha = 0.92f),
                    textColor = PixelParchmentLight,
                    fontSize = 14.sp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                )
            }

            // 5. Оверлей победы поверх экрана
            AnimatedVisibility(
                visible = isWon,
                enter = fadeIn() + scaleIn(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                PixelPanel(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clickable { onCompleteGame() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ВСЯ ПОСУДА ВЫМЫТА!",
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 19.sp,
                            color = FreshGreen,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Раковина пустая, а тарелочки аккуратно сложены на столе и сияют чистотой!",
                            fontFamily = AppFontFamily,
                            fontSize = 14.sp,
                            color = PixelTextDark,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .pixelButtonBackground(containerColor = Color(0xFFDCEDC8))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Награда: +$rewardCoins ",
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = PixelTextDark
                            )
                            CoinIcon(modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        PixelButton(
                            text = "Забрать $rewardCoins монет ➔",
                            onClick = onCompleteGame,
                            containerColor = FreshGreen,
                            textColor = Color.White,
                            fontSize = 16.sp,
                            trailingIcon = { CoinIcon(modifier = Modifier.size(18.dp)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(pulseScale)
                        )
                    }
                }
            }
        }
    }
}
