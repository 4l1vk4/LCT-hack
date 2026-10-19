package ru.finpet.kids.feature.map.minigames

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import kotlin.random.Random

private data class AutumnLeaf(
    val id: Int,
    val xPercent: Float,
    val yPercent: Float,
    val rotation: Float,
    val scale: Float
)

private data class SpawnZone(
    val minX: Float, val maxX: Float,
    val minY: Float, val maxY: Float
)

// Зоны спавна строго на нижней зеленой траве осенней поляны с широким и органичным разбросом
private val GRASS_SPAWN_ZONES = listOf(
    // 1. Дальний левый фланг: трава у левого края поляны
    SpawnZone(minX = 0.02f, maxX = 0.18f, minY = 0.52f, maxY = 0.67f),
    // 2. Ближний левый угол: нижний левый край экрана
    SpawnZone(minX = 0.04f, maxX = 0.24f, minY = 0.74f, maxY = 0.89f),
    // 3. Дальний правый фланг: трава у правого края поляны
    SpawnZone(minX = 0.80f, maxX = 0.96f, minY = 0.52f, maxY = 0.67f),
    // 4. Ближний правый угол: нижний правый край экрана
    SpawnZone(minX = 0.76f, maxX = 0.95f, minY = 0.74f, maxY = 0.89f),
    // 5. Центр-лево: поляна чуть в глубине
    SpawnZone(minX = 0.26f, maxX = 0.45f, minY = 0.58f, maxY = 0.71f),
    // 6. Центр-право: поляна чуть в глубине
    SpawnZone(minX = 0.54f, maxX = 0.73f, minY = 0.58f, maxY = 0.71f),
    // 7. Передний план по центру: широкая поляна внизу
    SpawnZone(minX = 0.35f, maxX = 0.65f, minY = 0.73f, maxY = 0.83f)
)

private fun generateAutumnLeaves(count: Int = 7): List<AutumnLeaf> {
    val leaves = mutableListOf<AutumnLeaf>()
    val random = Random.Default

    // Перемешиваем зоны, чтобы порядок появления был уникальным
    val selectedZones = GRASS_SPAWN_ZONES.shuffled(random).take(count)

    selectedZones.forEachIndexed { i, zone ->
        var bestX = (zone.minX + zone.maxX) / 2f
        var bestY = (zone.minY + zone.maxY) / 2f

        for (attempt in 0 until 25) {
            val candidateX = random.nextFloat() * (zone.maxX - zone.minX) + zone.minX
            val candidateY = random.nextFloat() * (zone.maxY - zone.minY) + zone.minY

            if (leaves.isEmpty()) {
                bestX = candidateX
                bestY = candidateY
                break
            }

            val minDist = leaves.minOf { l ->
                val dx = (l.xPercent - candidateX)
                val dy = (l.yPercent - candidateY) * 1.5f
                kotlin.math.sqrt(dx * dx + dy * dy)
            }

            if (minDist >= 0.14f) {
                bestX = candidateX
                bestY = candidateY
                break
            }
        }

        // Ближние листочки чуть крупнее дальних (атмосферная перспектива)
        val depthScale = if (bestY > 0.74f) 1.10f else 0.95f
        // Полный естественный угол поворота упавших осенних листьев (0..360°)
        val rotation = random.nextFloat() * 360f
        val scale = (0.95f + random.nextFloat() * 0.15f) * depthScale

        leaves.add(
            AutumnLeaf(
                id = i,
                xPercent = bestX,
                yPercent = bestY,
                rotation = rotation,
                scale = scale
            )
        )
    }

    return leaves
}

/**
 * Миниигра: «Уборка листьев» (заменяет мытье полов).
 * На фоне осенней поляны с деревьями (autumn_game) растянутой на полный размер диалога
 * на траве лежат осенние листочки (autumn_sheet) — по одному на точку.
 * При клике по листику он сначала чуть увеличивается, затем уменьшается и исчезает.
 * Когда все листочки собраны — победа и получение монет!
 */
@Composable
fun FloorCleaningGameDialog(
    rewardCoins: Int = 10,
    onCompleteGame: () -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val leaves = remember { generateAutumnLeaves(7) }
    val cleanedLeafIds = remember { mutableStateListOf<Int>() }

    var cleanedCount by remember { mutableIntStateOf(0) }
    var isWon by remember { mutableStateOf(false) }

    // Автоматическое завершение и получение монет через 3.5 секунды после победы
    LaunchedEffect(isWon) {
        if (isWon) {
            delay(3500)
            onCompleteGame()
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
            // 1. Фоновое изображение осенней поляны с деревьями — растянуто точно на полный размер диалога
            Image(
                painter = painterResource(R.drawable.autumn_game),
                contentDescription = "Осенняя поляна с деревьями",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            // 2. Игровое поле с отдельными осенними листочками
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val itemSize = 78.dp
                val availW = (maxWidth - itemSize - 16.dp).coerceAtLeast(0.dp)
                val availH = (maxHeight - itemSize - 16.dp).coerceAtLeast(0.dp)

                leaves.forEach { leaf ->
                    val spotX = 8.dp + availW * leaf.xPercent
                    val spotY = 8.dp + availH * leaf.yPercent
                    val isCleaned = cleanedLeafIds.contains(leaf.id)

                    Box(
                        modifier = Modifier.offset(x = spotX, y = spotY)
                    ) {
                        AutumnLeafItem(
                            leaf = leaf,
                            isCleaned = isCleaned,
                            onClean = {
                                if (!cleanedLeafIds.contains(leaf.id)) {
                                    cleanedLeafIds.add(leaf.id)
                                    cleanedCount = cleanedLeafIds.size

                                    if (cleanedCount >= leaves.size && !isWon) {
                                        coroutineScope.launch {
                                            delay(200)
                                            isWon = true
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // 3. Верхняя панель управления и счетчика
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
                            text = "Уборка листьев",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = PixelWoodDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Image(
                            painter = painterResource(R.drawable.autumn_sheet),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Нажимай на осенние листочки, чтобы убрать их!",
                        fontSize = 13.sp,
                        color = Color(0xFF6B4423)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Прогресс сбора
                Box(
                    modifier = Modifier
                        .pixelButtonBackground(containerColor = if (cleanedCount == leaves.size) FreshGreen else JoyOrange)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "$cleanedCount / ${leaves.size}",
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
                            text = "🎉 ВСЕ ЛИСТЬЯ СОБРАНЫ! 🎉",
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 19.sp,
                            color = FreshGreen,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Осенняя поляна сияет чистотой, двор в полном порядке!",
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

/**
 * Элемент отдельного осеннего листочка.
 * При клике листик сначала чуть увеличивается (1.0f -> 1.30f),
 * а затем плавно уменьшается и исчезает (1.30f -> 0.0f, alpha -> 0f).
 */
@Composable
private fun AutumnLeafItem(
    leaf: AutumnLeaf,
    isCleaned: Boolean,
    onClean: () -> Unit
) {
    var isCleaning by remember { mutableStateOf(false) }
    val animScale = remember { Animatable(1f) }
    val animAlpha = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    if (isCleaned && !isCleaning) {
        return
    }

    Box(
        modifier = Modifier
            .size(78.dp)
            .scale(animScale.value)
            .alpha(animAlpha.value)
            .clickable(
                enabled = !isCleaning && !isCleaned,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isCleaning || isCleaned) return@clickable
                isCleaning = true
                coroutineScope.launch {
                    // 1. Сначала чуть увеличиться при клике
                    animScale.animateTo(
                        targetValue = 1.30f,
                        animationSpec = tween(durationMillis = 130, easing = FastOutSlowInEasing)
                    )
                    // 2. Потом уменьшиться и исчезнуть
                    launch {
                        animAlpha.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing)
                        )
                    }
                    animScale.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing)
                    )
                    onClean()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Мягкая естественная тень под листочком на траве
        Box(
            modifier = Modifier
                .size(46.dp)
                .offset(y = 4.dp)
                .clip(CircleShape)
                .background(Color(0x22000000))
        )

        // Один аккуратный осенний листочек
        Image(
            painter = painterResource(R.drawable.autumn_sheet),
            contentDescription = null,
            modifier = Modifier
                .size(58.dp)
                .scale(leaf.scale)
                .rotate(leaf.rotation)
        )
    }
}
