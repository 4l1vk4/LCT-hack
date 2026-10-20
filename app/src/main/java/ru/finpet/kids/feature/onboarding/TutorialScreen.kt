package ru.finpet.kids.feature.onboarding

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import ru.finpet.kids.R
import ru.finpet.kids.core.designsystem.LifeJarIcon
import ru.finpet.kids.core.designsystem.JoyJarIcon
import ru.finpet.kids.core.designsystem.SavingJarIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import androidx.compose.foundation.layout.PaddingValues
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium

// положение головы кота
private const val CAT_HEAD_CX = 0.50f
private const val CAT_HEAD_CY = 0.3f
private const val CAT_WIDTH_RATIO = 0.45f

// положение текста на экране
private const val TEXT_CX = 0.50f
private const val TEXT_CY = 0.55f

@Composable
fun TutorialScreen(
    petName: String,
    skinId: String,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(0) }

    val steps = remember(petName) {
        listOf(
            "Привет, я $petName!",
            "Это игра про финансы — здесь ты научишься обращаться с монетками!",
            "Есть 3 типа решений:\n🥣 Обязательное — еда, вода, уход\n🎈 Желаемое — игрушки, вкусняшки\n🐷 Отложить — в копилку на мечту",
            "Наверху — панель с монетками, настроением и меню (инструкция + настройки).",
            "Внизу — вкладки. Покликай и изучи!",
            "Следи за питомцем — если о нём не заботиться, он начнёт грустить или уйдёт погулять.",
            "Удачи!"
        )
    }

    fun goNext() {
        if (step < steps.lastIndex) step++ else onFinish()
    }

    fun goPrevious() {
        if (step > 0) step--
    }

    // анимация облака при смене шага
    var pulseOn by remember { mutableStateOf(false) }
    LaunchedEffect(step) {
        pulseOn = false
        delay(30)
        pulseOn = true
    }
    val pulseScale by animateFloatAsState(
        targetValue = if (pulseOn) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cloud_pulse"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9E6))
            .pointerInput(step) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var totalDragX = 0f
                    var isDrag = false
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (change.changedToUp()) {
                            if (!isDrag) {
                                // Тап в левой части экрана (первые 35%) — листает назад, в остальной части — вперед
                                if (change.position.x < size.width * 0.35f) {
                                    goPrevious()
                                } else {
                                    goNext()
                                }
                            } else {
                                // Свайп вправо (палец двигается слева направо) — назад, влево — вперед
                                if (totalDragX > 40f) {
                                    goPrevious()
                                } else if (totalDragX < -40f) {
                                    goNext()
                                }
                            }
                            break
                        } else {
                            val drag = change.position.x - change.previousPosition.x
                            totalDragX += drag
                            if (abs(totalDragX) > 20f) {
                                isDrag = true
                            }
                        }
                    }
                }
            }
    ) {
        val w = maxWidth
        val h = maxHeight

        val catSize = w * CAT_WIDTH_RATIO
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(previewDrawableFor(skinId))
                .decoderFactory(GifDecoder.Factory())
                .crossfade(false)
                .build(),
            contentDescription = "Питомец",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .offset(
                    x = w * CAT_HEAD_CX - catSize / 2,
                    y = h * CAT_HEAD_CY - catSize / 2
                )
                .size(catSize)
        )

        // 3. Текст по центру экрана в пиксельной плашке диалога
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(
                    y = h * (TEXT_CY - 0.5f)   // сдвиг от центра
                )
                .fillMaxWidth(0.86f)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        ) {
            PixelPanel(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (step == 2) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Есть 3 типа решений (баночки):",
                            fontFamily = AppFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PixelTextDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LifeJarIcon(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Обязательное — еда, вода, уход",
                                fontFamily = AppFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PixelTextDark
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            JoyJarIcon(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Желаемое — игрушки, вкусняшки",
                                fontFamily = AppFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PixelTextDark
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SavingJarIcon(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Отложить — в банку на мечту",
                                fontFamily = AppFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PixelTextDark
                            )
                        }
                    }
                } else {
                    Text(
                        text = steps[step],
                        fontFamily = AppFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextDark,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 4. Точки-индикаторы в виде орбов
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp)
                .fillMaxWidth()
        ) {
            steps.indices.forEach { i ->
                val isFilled = i <= step        // ← заполнены все орбы ДО текущего включительно

                Image(
                    painter = painterResource(id = R.drawable.orb),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        color = if (isFilled) JoyOrange else Color(0xFFD7CCC8),
                        blendMode = BlendMode.SrcIn
                    ),
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(14.dp)
                )
            }
        }

        // кнопка пропуска обучения
        if (step < steps.lastIndex) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp)
            ) {
                PixelButton(
                    text = "Пропустить →",
                    onClick = onFinish,
                    containerColor = PixelWoodMedium,
                    textColor = PixelParchmentLight,
                    fontSize = 15.sp,
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp)
                )
            }
        }
    }
}