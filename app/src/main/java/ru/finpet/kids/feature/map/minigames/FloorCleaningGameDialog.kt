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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.StardewBoard

private data class FloorSpot(
    val id: Int,
    val percentX: Float, // 0f .. 1f безопасного пространства
    val percentY: Float, // 0f .. 1f безопасного пространства
    val spotEmoji: String,
    val label: String,
    val isCleaned: Boolean = false,
    val isCleaning: Boolean = false
)

private val DIRT_VARIANTS = listOf(
    "🐾" to "Следы лап",
    "🧃" to "Пятно сока",
    "💧" to "Лужица воды",
    "🍪" to "Крошки печенья",
    "🍂" to "Сухой листочек",
    "🐾" to "Грязные лапки"
)

// Безопасные координаты пятен в сетке 2x3
private val SAFE_SPOT_POSITIONS = listOf(
    0.12f to 0.10f,
    0.72f to 0.12f,
    0.15f to 0.45f,
    0.70f to 0.48f,
    0.14f to 0.80f,
    0.74f to 0.82f
)

/**
 * Миниигра 1: «Мытье полов»
 * На полу разбросаны грязные пятна. При клике на пятно появляется швабра,
 * проигрывается быстрая анимация уборки и пятно исчезает.
 * Когда все убрано — победа и гарантированное начисление монет!
 */
@Composable
fun FloorCleaningGameDialog(
    rewardCoins: Int = 50,
    onCompleteGame: () -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val spots = remember {
        val list = mutableStateListOf<FloorSpot>()
        for (i in 0 until 6) {
            val variant = DIRT_VARIANTS[i % DIRT_VARIANTS.size]
            val pos = SAFE_SPOT_POSITIONS[i % SAFE_SPOT_POSITIONS.size]
            list.add(
                FloorSpot(
                    id = i,
                    percentX = pos.first,
                    percentY = pos.second,
                    spotEmoji = variant.first,
                    label = variant.second
                )
            )
        }
        list
    }

    var cleanedCount by remember { mutableIntStateOf(0) }
    var isWon by remember { mutableStateOf(false) }

    // Автоматическое завершение и получение монет через 3.5 секунды после победы
    LaunchedEffect(isWon) {
        if (isWon) {
            delay(3500)
            onCompleteGame()
        }
    }

    fun cleanSpot(spotId: Int) {
        val index = spots.indexOfFirst { it.id == spotId }
        if (index == -1) return
        val current = spots[index]
        if (current.isCleaned || current.isCleaning) return

        // Мгновенно запускаем уборку пятна
        spots[index] = current.copy(isCleaning = true)

        coroutineScope.launch {
            // Анимация взмаха швабры
            delay(320)
            val updatedIndex = spots.indexOfFirst { it.id == spotId }
            if (updatedIndex != -1) {
                spots[updatedIndex] = spots[updatedIndex].copy(isCleaned = true, isCleaning = false)
            }
            cleanedCount = spots.count { it.isCleaned }

            if (cleanedCount >= spots.size && !isWon) {
                delay(200)
                isWon = true
            }
        }
    }

    // Пульсация кнопки победы
    val infiniteTransition = rememberInfiniteTransition(label = "winPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Dialog(
        onDismissRequest = {
            if (isWon) onCompleteGame() else onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (isWon) onCompleteGame()
                },
            contentAlignment = Alignment.Center
        ) {
            StardewBoard(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f),
                headerTitle = "МЫТЬЕ ПОЛОВ 🧹",
                headerIcon = "🧹"
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Информационная плашка со счетчиком
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Нажимай на грязные пятна!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = PixelWoodDark
                                )
                                Text(
                                    text = "Очисти весь пол, чтобы получить награду",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B4423)
                                )
                            }

                            // Прогресс
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (cleanedCount == spots.size) FreshGreen else JoyOrange
                            ) {
                                Text(
                                    text = "$cleanedCount / ${spots.size}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Игровая зона: деревянный паркет
                        BoxWithConstraints(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFEAD2A8),
                                            Color(0xFFD7B989),
                                            Color(0xFFC7A26E)
                                        )
                                    )
                                )
                                .border(2.dp, Color(0xFF8D5B28), RoundedCornerShape(12.dp))
                        ) {
                            // Текстура досок паркета
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val plankH = size.height / 8f
                                for (i in 1..7) {
                                    val y = plankH * i
                                    drawLine(
                                        color = Color(0xFFB58E58),
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = 2f
                                    )
                                }
                            }

                            // Безопасные размеры и отступы: каждый элемент строго внутри границ!
                            val spotSize = 64.dp
                            val availW = (maxWidth - spotSize - 16.dp).coerceAtLeast(0.dp)
                            val availH = (maxHeight - spotSize - 16.dp).coerceAtLeast(0.dp)

                            // Пятна грязи
                            spots.forEach { spot ->
                                val spotX = 8.dp + availW * spot.percentX
                                val spotY = 8.dp + availH * spot.percentY

                                Box(
                                    modifier = Modifier
                                        .offset(x = spotX, y = spotY)
                                        .size(spotSize)
                                        .clip(CircleShape)
                                        .clickable(
                                            enabled = !spot.isCleaned && !spot.isCleaning && !isWon
                                        ) {
                                            cleanSpot(spot.id)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        spot.isCleaning -> {
                                            // Анимация уборки шваброй прямо на пятне
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.scale(1.15f)
                                            ) {
                                                Text(text = "🧹", fontSize = 32.sp)
                                                Text(text = "🫧", fontSize = 16.sp)
                                            }
                                        }
                                        spot.isCleaned -> {
                                            // Чистый пол: сияющая звездочка
                                            Box(
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0x55FFFFFF)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "✨", fontSize = 24.sp)
                                            }
                                        }
                                        else -> {
                                            // Грязное пятно
                                            Box(
                                                modifier = Modifier
                                                    .size(52.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0x555C3A16))
                                                    .border(1.5.dp, Color(0xAA7A481F), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = spot.spotEmoji, fontSize = 26.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Нижняя кнопка во время игры
                        if (!isWon) {
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "Закончить позже", color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }

                    // ВСЕГДА ВИДИМЫЙ ОВЕРЛЕЙ ПОБЕДЫ ПОВЕРХ ЭКРАНА
                    AnimatedVisibility(
                        visible = isWon,
                        enter = fadeIn() + scaleIn(),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE8F5E9),
                            border = androidx.compose.foundation.BorderStroke(2.5.dp, FreshGreen),
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .clickable { onCompleteGame() }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🎉 ВСЁ ЧИСТО! 🎉",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = FreshGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Пол блестит и сияет чистотой!",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2E7D32),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .border(1.5.dp, FreshGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Награда: +$rewardCoins ",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = FreshGreen
                                    )
                                    CoinIcon(modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onCompleteGame,
                                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .scale(pulseScale)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Забрать $rewardCoins монет ➔",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        CoinIcon(modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
