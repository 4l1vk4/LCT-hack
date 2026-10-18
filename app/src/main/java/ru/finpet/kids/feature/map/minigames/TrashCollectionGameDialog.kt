package ru.finpet.kids.feature.map.minigames

import android.graphics.Paint
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.StardewBoard
import kotlin.math.abs
import kotlin.random.Random

private class GameTrash(
    val id: Long,
    var xPercent: Float, // 0.08f .. 0.88f
    var yPercent: Float, // 0f .. 1f
    val speed: Float,    // процент в секунду
    val emoji: String,
    var isCollected: Boolean = false,
    var isMissed: Boolean = false
)

private val TRASH_EMOJIS = listOf("🍌", "🥫", "📦", "🥤", "🧃", "📄", "🍎", "🧴", "🍬")

/**
 * Миниигра 2: «Вынос мусора» (Высокопроизводительная реализация на Canvas 60-120 FPS).
 *
 * Оптимизации против лагов:
 * 1. Игровой цикл привязан к аппаратному тику экрана через `withFrameNanos`, с точным deltaTime.
 *    Никаких перегрузок корутин и таймеров `delay(20)`.
 * 2. Полный рендеринг падающих предметов и корзины выполняется в едином аппаратно-ускоренном
 *    `Canvas` через `nativeCanvas.drawText(...)`.
 *    Это исключает создание и уничтожение сотен Compose-узлов `Box` и `Text`, из-за которых происходили лаги и просадки FPS.
 * 3. Логика координат и проверка коллизий считаются в памяти без рекомпозиций внешнего UI.
 *    Внешний UI (счетчик и жизни) обновляется только при реальном изменении очков/жизней.
 */
@Composable
fun TrashCollectionGameDialog(
    rewardCoins: Int = 25,
    onCompleteGame: () -> Unit,
    onDismiss: () -> Unit
) {
    var basketXPercent by remember { mutableFloatStateOf(0.5f) }
    var collectedCount by remember { mutableIntStateOf(0) }
    var livesLeft by remember { mutableIntStateOf(3) }
    var isGameOver by remember { mutableStateOf(false) }

    // Внутренний счетчик кадров для триггера перерисовки Canvas
    var gameRenderTick by remember { mutableLongStateOf(0L) }

    // Список объектов в чистой памяти без реактивных накладных расходов Compose на каждый элемент
    val activeTrash = remember { ArrayList<GameTrash>() }

    // Paint для отрисовки эмодзи на Canvas
    val density = LocalDensity.current
    val trashTextPaint = remember(density) {
        Paint().apply {
            textSize = with(density) { 32.sp.toPx() }
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }
    val basketTextPaint = remember(density) {
        Paint().apply {
            textSize = with(density) { 52.sp.toPx() }
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }
    val labelTextPaint = remember(density) {
        Paint().apply {
            textSize = with(density) { 10.sp.toPx() }
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
            color = android.graphics.Color.WHITE
        }
    }

    // Игровой цикл на VSYNC (withFrameNanos)
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

                // Увеличение сложности от количества собранного мусора
                val difficulty = 1f + (collectedCount / 10f) * 0.35f
                val baseSpeed = (0.35f + rand.nextFloat() * 0.15f) * difficulty
                val spawnInterval = (1.1f / difficulty).coerceAtLeast(0.42f)

                spawnTimer += dt
                if (spawnTimer >= spawnInterval) {
                    spawnTimer = 0f
                    val emoji = TRASH_EMOJIS[rand.nextInt(TRASH_EMOJIS.size)]
                    val x = 0.1f + rand.nextFloat() * 0.8f
                    activeTrash.add(
                        GameTrash(
                            id = nextId++,
                            xPercent = x,
                            yPercent = -0.05f,
                            speed = baseSpeed,
                            emoji = emoji
                        )
                    )
                }

                // Обновление позиций и коллизии
                val basketY = 0.85f
                val hitDistanceX = 0.14f

                var newlyCollected = 0
                var newlyMissed = 0

                val iterator = activeTrash.iterator()
                while (iterator.hasNext()) {
                    val trash = iterator.next()
                    trash.yPercent += trash.speed * dt

                    // Проверка ловли в корзину
                    if (trash.yPercent in (basketY - 0.06f)..(basketY + 0.05f)) {
                        val distanceX = abs(trash.xPercent - basketXPercent)
                        if (distanceX <= hitDistanceX) {
                            trash.isCollected = true
                            newlyCollected++
                            iterator.remove()
                            continue
                        }
                    }

                    // Проверка промаха
                    if (trash.yPercent > 0.98f) {
                        trash.isMissed = true
                        newlyMissed++
                        iterator.remove()
                    }
                }

                if (newlyCollected > 0) {
                    collectedCount += newlyCollected
                }

                if (newlyMissed > 0) {
                    livesLeft = (livesLeft - newlyMissed).coerceAtLeast(0)
                    if (livesLeft <= 0) {
                        isGameOver = true
                    }
                }

                // Сигнал на перерисовку Canvas
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
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* no-op */ },
            contentAlignment = Alignment.Center
        ) {
            StardewBoard(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.88f),
                headerTitle = "СБОР МУСОРА 🗑️",
                headerIcon = "🗑️"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Информационная панель со счетчиком, жизнями и целью
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(PixelParchmentLight)
                            .border(1.5.dp, PixelParchmentBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
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
                                Text(
                                    text = if (hasLife) "❤️" else "🖤",
                                    fontSize = 16.sp
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
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (collectedCount >= 20) FreshGreen else JoyOrange
                            ) {
                                Text(
                                    text = "$collectedCount / 20",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Аппаратно-ускоренная игровая арена
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFE8EAF6),
                                        Color(0xFFC5CAE9),
                                        Color(0xFF9FA8DA)
                                    )
                                )
                            )
                            .border(2.dp, Color(0xFF3F51B5), RoundedCornerShape(12.dp))
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
                        // Единый производительный Canvas для рендера всех игровых объектов
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Чтение renderTick заставляет Canvas перерисовываться каждый кадр анимации
                            @Suppress("UNUSED_VARIABLE")
                            val tick = gameRenderTick

                            val canvasW = size.width
                            val canvasH = size.height
                            val native = drawContext.canvas.nativeCanvas

                            // 1. Отрисовка падающего мусора
                            for (i in 0 until activeTrash.size) {
                                val item = activeTrash.getOrNull(i) ?: continue
                                val px = item.xPercent * canvasW
                                val py = item.yPercent * canvasH
                                native.drawText(item.emoji, px, py, trashTextPaint)
                            }

                            // 2. Отрисовка мусорной корзины игрока
                            val basketPx = basketXPercent * canvasW
                            val basketPy = 0.87f * canvasH
                            native.drawText("🗑️", basketPx, basketPy, basketTextPaint)
                        }

                        // Подсказка управления в первые секунды
                        if (collectedCount < 3 && !isGameOver) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(Color(0x99000000), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Завершение игры: результат
                    if (isGameOver) {
                        val isSuccess = collectedCount >= 20
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSuccess) FreshGreen else Color(0xFFE53935)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (isSuccess) {
                                    Text(
                                        text = "🎉 Отличная работа! Мусор собран ($collectedCount шт.)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = FreshGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Награда за дело: +$rewardCoins ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = PixelWoodDark
                                        )
                                        CoinIcon(modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
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
                                        text = "💔 Не хватило до цели! Собрано: $collectedCount из 20",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color(0xFFD32F2F)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Нужно поймать минимум 20 предметов мусора, чтобы заработать монеты.",
                                        fontSize = 15.sp,
                                        lineHeight = 20.sp,
                                        color = Color(0xFF555555),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = onDismiss,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Выйти", fontSize = 16.sp)
                                        }
                                        Button(
                                            onClick = {
                                                collectedCount = 0
                                                livesLeft = 3
                                                activeTrash.clear()
                                                basketXPercent = 0.5f
                                                isGameOver = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = JoyOrange),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Попробовать снова", fontSize = 16.sp)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Закончить позже", color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
