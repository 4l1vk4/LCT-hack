package ru.finpet.kids.feature.map.minigames

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.R
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.HeartIcon
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.pixelButtonBackground
import ru.finpet.kids.core.designsystem.pixelPanelBackground
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

private class GameTrash(
    val id: Long,
    var xPercent: Float,
    var yPercent: Float,
    val speed: Float,
    val typeIndex: Int,
    var isCollected: Boolean = false,
    var isMissed: Boolean = false
)

/**
 * Миниигра: «Вынос мусора».
 * На весь экран отображается musor_drop.
 * Падают предметы: korobka_musor, bottle_musor, literally_musor.
 * Корзина игрока: во время игры bin_opened, по завершении/паузе — bin.
 */
@Composable
fun TrashCollectionGameDialog(
    rewardCoins: Int = 8,
    onCompleteGame: () -> Unit,
    onDismiss: () -> Unit
) {
    var basketXPercent by remember { mutableFloatStateOf(0.5f) }
    var collectedCount by remember { mutableIntStateOf(0) }
    var livesLeft by remember { mutableIntStateOf(3) }
    var isGameOver by remember { mutableStateOf(false) }

    var gameRenderTick by remember { mutableLongStateOf(0L) }
    val activeTrash = remember { ArrayList<GameTrash>() }

    // Загрузка пиксельных спрайтов
    val trashBitmaps = listOf(
        ImageBitmap.imageResource(id = R.drawable.korobka_musor),
        ImageBitmap.imageResource(id = R.drawable.bottle_musor),
        ImageBitmap.imageResource(id = R.drawable.literally_musor)
    )
    val binOpenedBitmap = ImageBitmap.imageResource(id = R.drawable.bin_opened)
    val binClosedBitmap = ImageBitmap.imageResource(id = R.drawable.bin)

    val density = LocalDensity.current

    // Игровой цикл на VSYNC
    LaunchedEffect(isGameOver) {
        if (isGameOver) return@LaunchedEffect

        var lastFrameNanos = 0L
        var nextId = 0L
        var spawnTimer = 0f
        val rand = Random(System.currentTimeMillis())

        while (!isGameOver) {
            withFrameNanos { nowNanos ->
                if (lastFrameNanos == 0L) {
                    lastFrameNanos = nowNanos
                    return@withFrameNanos
                }

                val dt = ((nowNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastFrameNanos = nowNanos

                val difficulty = 1f + (collectedCount / 10f) * 0.35f
                val baseSpeed = (0.35f + rand.nextFloat() * 0.15f) * difficulty
                val spawnInterval = (1.1f / difficulty).coerceAtLeast(0.40f)

                spawnTimer += dt
                if (spawnTimer >= spawnInterval) {
                    spawnTimer = 0f
                    val typeIdx = rand.nextInt(trashBitmaps.size)
                    val x = 0.10f + rand.nextFloat() * 0.80f
                    activeTrash.add(
                        GameTrash(
                            id = nextId++,
                            xPercent = x,
                            yPercent = -0.06f,
                            speed = baseSpeed,
                            typeIndex = typeIdx
                        )
                    )
                }

                // Проверка ловли корзиной
                val basketY = 0.84f
                val hitDistanceX = 0.14f

                var newlyCollected = 0
                var newlyMissed = 0

                val iterator = activeTrash.iterator()
                while (iterator.hasNext()) {
                    val trash = iterator.next()
                    trash.yPercent += trash.speed * dt

                    if (trash.yPercent in (basketY - 0.06f)..(basketY + 0.05f)) {
                        val distanceX = abs(trash.xPercent - basketXPercent)
                        if (distanceX <= hitDistanceX) {
                            trash.isCollected = true
                            newlyCollected++
                            iterator.remove()
                            continue
                        }
                    }

                    if (trash.yPercent > 0.96f) {
                        trash.isMissed = true
                        newlyMissed++
                        iterator.remove()
                    }
                }

                if (newlyCollected > 0) {
                    collectedCount += newlyCollected
                    if (collectedCount >= 20) {
                        isGameOver = true
                    }
                }

                if (newlyMissed > 0) {
                    livesLeft = (livesLeft - newlyMissed).coerceAtLeast(0)
                    if (livesLeft <= 0) {
                        isGameOver = true
                    }
                }

                gameRenderTick = nowNanos
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (isGameOver) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        KeepScreenImmersive()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* no-op */ }
        ) {
            // 1. Полноэкранный фон musor_drop на весь экран
            Image(
                painter = painterResource(R.drawable.musor_drop),
                contentDescription = "Фон сбора мусора",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            // 2. Игровая арена Canvas с падающими предметами и корзиной
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isGameOver) {
                        if (!isGameOver) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val newPercent = (offset.x / size.width.toFloat())
                                        .coerceIn(0.12f, 0.88f)
                                    basketXPercent = newPercent
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val newPercent = (change.position.x / size.width.toFloat())
                                        .coerceIn(0.12f, 0.88f)
                                    basketXPercent = newPercent
                                }
                            )
                        }
                    }
            ) {
                val trashItemSizePx = with(density) { 44.dp.roundToPx() }
                val basketSizePx = with(density) { 68.dp.roundToPx() }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    @Suppress("UNUSED_VARIABLE")
                    val tick = gameRenderTick

                    val canvasW = size.width
                    val canvasH = size.height

                    // Отрисовка падающего мусора
                    for (i in 0 until activeTrash.size) {
                        val item = activeTrash.getOrNull(i) ?: continue
                        val px = item.xPercent * canvasW
                        val py = item.yPercent * canvasH
                        val bmp = trashBitmaps[item.typeIndex % trashBitmaps.size]

                        drawImage(
                            image = bmp,
                            dstOffset = IntOffset(
                                (px - trashItemSizePx / 2f).roundToInt(),
                                (py - trashItemSizePx / 2f).roundToInt()
                            ),
                            dstSize = IntSize(trashItemSizePx, trashItemSizePx)
                        )
                    }

                    // Отрисовка корзины (bin_opened во время игры, bin при завершении)
                    val basketBitmap = if (isGameOver) binClosedBitmap else binOpenedBitmap
                    val basketPx = basketXPercent * canvasW
                    val basketPy = 0.85f * canvasH

                    drawImage(
                        image = basketBitmap,
                        dstOffset = IntOffset(
                            (basketPx - basketSizePx / 2f).roundToInt(),
                            (basketPy - basketSizePx / 2f).roundToInt()
                        ),
                        dstSize = IntSize(basketSizePx, basketSizePx)
                    )
                }

                // Подсказка управления в начале
                if (collectedCount < 3 && !isGameOver) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color(0xBB000000), RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "👆 Удерживай палец и веди корзину влево-вправо!",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 3. Верхняя плашка со статусом, жизнями и кнопкой закрытия
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PixelParchmentLight.copy(alpha = 0.94f))
                    .border(2.dp, PixelParchmentBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Жизни
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Жизни: ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PixelWoodDark
                    )
                    repeat(3) { i ->
                        val hasLife = i < livesLeft
                        HeartIcon(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .size(20.dp),
                            tint = if (hasLife) null else Color(0xFF222222)
                        )
                    }
                }

                // Счетчик мусора и цель 20
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Собрано: ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PixelWoodDark
                    )
                    Box(
                        modifier = Modifier
                            .pixelButtonBackground(containerColor = if (collectedCount >= 20) FreshGreen else JoyOrange)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$collectedCount / 20",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }

                // Кнопка выхода (крестик)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.close_sign),
                        contentDescription = "Закрыть",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // 4. Кнопка "Закончить позже" во время игры
            if (!isGameOver) {
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

            // 5. Оверлей завершения игры
            AnimatedVisibility(
                visible = isGameOver,
                enter = fadeIn() + scaleIn(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                val isSuccess = collectedCount >= 20
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .pixelPanelBackground()
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isSuccess) {
                            Text(
                                text = "🎉 Отличная работа!",
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = FreshGreen,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Весь мусор собран в бак ($collectedCount шт.)",
                                fontSize = 15.sp,
                                color = PixelWoodDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Награда за дело: +$rewardCoins ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = PixelWoodDark
                                )
                                CoinIcon(modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            PixelButton(
                                text = "Забрать монеты ➔",
                                onClick = onCompleteGame,
                                containerColor = FreshGreen,
                                textColor = Color.White,
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 16.sp
                            )
                        } else {
                            Text(
                                text = "💔 Мусор улетел мимо!",
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color(0xFFD32F2F),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Собрано: $collectedCount из 20 предметов.\nНужно поймать минимум 20 предметов в мусорку.",
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                                color = PixelWoodDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                PixelButton(
                                    text = "Выйти",
                                    onClick = onDismiss,
                                    containerColor = PixelWoodMedium,
                                    textColor = PixelParchmentLight,
                                    fontSize = 15.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                PixelButton(
                                    text = "Ещё раз",
                                    onClick = {
                                        collectedCount = 0
                                        livesLeft = 3
                                        activeTrash.clear()
                                        basketXPercent = 0.5f
                                        isGameOver = false
                                    },
                                    containerColor = JoyOrange,
                                    textColor = Color.White,
                                    fontSize = 15.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
