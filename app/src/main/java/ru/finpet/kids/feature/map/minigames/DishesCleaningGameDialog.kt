package ru.finpet.kids.feature.map.minigames

import androidx.compose.animation.AnimatedVisibility
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

private data class DishItem(
    val id: Int,
    val percentX: Float, // 0f .. 1f безопасного пространства
    val percentY: Float, // 0f .. 1f безопасного пространства
    val dirtyEmoji: String,
    val cleanEmoji: String,
    val name: String,
    val isCleaned: Boolean = false,
    val isCleaning: Boolean = false
)

private val DISH_VARIANTS = listOf(
    Triple("🍽️", "✨🍽️", "Тарелка"),
    Triple("🥣", "✨🥣", "Миска"),
    Triple("☕", "✨☕", "Чашка"),
    Triple("🍴", "✨🍴", "Вилка"),
    Triple("🥛", "✨🥛", "Стакан")
)

// Безопасные координаты посуды в раковине
private val SAFE_DISH_POSITIONS = listOf(
    0.16f to 0.14f,
    0.74f to 0.16f,
    0.45f to 0.46f,
    0.18f to 0.78f,
    0.74f to 0.78f
)

/**
 * Миниигра 3: «Мытье посуды»
 * В раковине лежит грязная посуда (тарелки, кружки, приборы).
 * Ребенок кликает на грязный предмет — появляется губка с мыльной пеной,
 * проигрывается быстрая анимация мытья, и предмет начинает сверкать чистотой!
 * Когда вся посуда вымыта — гарантированная победа и начисление монет!
 */
@Composable
fun DishesCleaningGameDialog(
    rewardCoins: Int = 20,
    onCompleteGame: () -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val dishes = remember {
        val list = mutableStateListOf<DishItem>()
        for (i in 0 until 5) {
            val variant = DISH_VARIANTS[i % DISH_VARIANTS.size]
            val pos = SAFE_DISH_POSITIONS[i % SAFE_DISH_POSITIONS.size]
            list.add(
                DishItem(
                    id = i,
                    percentX = pos.first,
                    percentY = pos.second,
                    dirtyEmoji = variant.first,
                    cleanEmoji = variant.second,
                    name = variant.third
                )
            )
        }
        list
    }

    var cleanedCount by remember { mutableIntStateOf(0) }
    var isWon by remember { mutableStateOf(false) }

    // Автоматическое получение монет через 3.5 секунды после победы
    LaunchedEffect(isWon) {
        if (isWon) {
            delay(3500)
            onCompleteGame()
        }
    }

    fun cleanDish(dishId: Int) {
        val index = dishes.indexOfFirst { it.id == dishId }
        if (index == -1) return
        val current = dishes[index]
        if (current.isCleaned || current.isCleaning) return

        // Мгновенный отклик на нажатие
        dishes[index] = current.copy(isCleaning = true)

        coroutineScope.launch {
            // Быстрая анимация мытья губкой
            delay(320)
            val updatedIndex = dishes.indexOfFirst { it.id == dishId }
            if (updatedIndex != -1) {
                dishes[updatedIndex] = dishes[updatedIndex].copy(isCleaned = true, isCleaning = false)
            }
            cleanedCount = dishes.count { it.isCleaned }

            if (cleanedCount >= dishes.size && !isWon) {
                delay(200)
                isWon = true
            }
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
                headerTitle = "МЫТЬЕ ПОСУДЫ 🍽️",
                headerIcon = "🍽️"
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Информационная плашка
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
                                    text = "Нажимай на посуду в раковине!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = PixelWoodDark
                                )
                                Text(
                                    text = "Отмой все тарелочки губкой до блеска",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B4423)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (cleanedCount == dishes.size) FreshGreen else JoyOrange
                            ) {
                                Text(
                                    text = "$cleanedCount / ${dishes.size}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Игровая зона: раковина с водой и мыльными пузырями
                        BoxWithConstraints(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFE0F2F1), // светлая вода
                                            Color(0xFFB2DFDB),
                                            Color(0xFF80CBC4)
                                        )
                                    )
                                )
                                .border(2.5.dp, Color(0xFF00796B), RoundedCornerShape(14.dp))
                        ) {
                            // Фоновые мыльные пузыри в воде
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = Color(0x33FFFFFF),
                                    radius = size.width * 0.15f,
                                    center = Offset(size.width * 0.2f, size.height * 0.3f)
                                )
                                drawCircle(
                                    color = Color(0x33FFFFFF),
                                    radius = size.width * 0.22f,
                                    center = Offset(size.width * 0.8f, size.height * 0.7f)
                                )
                            }

                            // Безопасные размеры: все элементы строго внутри раковины
                            val dishSize = 68.dp
                            val availW = (maxWidth - dishSize - 16.dp).coerceAtLeast(0.dp)
                            val availH = (maxHeight - dishSize - 16.dp).coerceAtLeast(0.dp)

                            // Посуда в раковине
                            dishes.forEach { dish ->
                                val dishX = 8.dp + availW * dish.percentX
                                val dishY = 8.dp + availH * dish.percentY

                                Box(
                                    modifier = Modifier
                                        .offset(x = dishX, y = dishY)
                                        .size(dishSize)
                                        .clip(CircleShape)
                                        .clickable(
                                            enabled = !dish.isCleaned && !dish.isCleaning && !isWon
                                        ) {
                                            cleanDish(dish.id)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        dish.isCleaning -> {
                                            // Анимация губки с мыльной пеной прямо на посуде
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.scale(1.15f)
                                            ) {
                                                Text(text = "🧽", fontSize = 34.sp)
                                                Text(text = "🫧🫧", fontSize = 14.sp)
                                            }
                                        }
                                        dish.isCleaned -> {
                                            // Чистая сияющая посуда
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0x88FFFFFF))
                                                    .border(2.dp, FreshGreen, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = dish.cleanEmoji, fontSize = 24.sp)
                                            }
                                        }
                                        else -> {
                                            // Грязная посуда
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0x66BCAAA4))
                                                    .border(2.dp, Color(0xFF8D6E63), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = dish.dirtyEmoji, fontSize = 28.sp)
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

                    // ВСЕГДА ВИДИМЫЙ ОВЕРЛЕЙ ПОБЕДЫ ПОВЕРХ РАКОВИНЫ
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
                                    text = "🎉 ВСЯ ПОСУДА ВЫМЫТА! 🎉",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 19.sp,
                                    color = FreshGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Тарелки и чашки сверкают до скрипа!",
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
