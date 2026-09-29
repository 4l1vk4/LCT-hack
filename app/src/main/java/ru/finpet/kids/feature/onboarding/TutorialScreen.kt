package ru.finpet.kids.feature.onboarding

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import ru.finpet.kids.R
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.TextPrimary

// Где на картинке голова кота (доля)
private const val CAT_HEAD_CX = 0.50f
private const val CAT_HEAD_CY = 0.3f
private const val CAT_WIDTH_RATIO = 0.45f

// Где рисовать текст (доля от размера экрана) — центр
private const val TEXT_CX = 0.50f
private const val TEXT_CY = 0.55f
private const val TEXT_WIDTH_RATIO = 0.75f

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
            "Следи за питомцем — если о нём не заботиться, он убежит.",
            "Удачи!"
        )
    }

    fun goNext() {
        if (step < steps.lastIndex) step++ else onFinish()
    }

    // Пульс облака при смене шага
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { goNext() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF9E6))
        )

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

        // 3. Текст по центру экрана, БЕЗ белого фона
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(
                    y = h * (TEXT_CY - 0.5f)   // сдвиг от центра
                )
                .fillMaxWidth(TEXT_WIDTH_RATIO)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        ) {
            Text(
                text = steps[step],
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF381F0E),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 4. Точки-индикаторы
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp)
                .fillMaxWidth()
        ) {
            steps.indices.forEach { i ->
                val isFilled = i <= step        // ← заполнены все точки ДО текущей включительно

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) JoyOrange                       // оранжевая — заполненная
                            else Color(0xFFFFFDF7)                         // кремовая — пустая
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (isFilled) Color(0xFFC67D00) else Color(0xFFFFD54F),
                            shape = CircleShape
                        )
                )
            }
        }

        // 5. Кнопка «Пропустить» — по центру снизу
        if (step < steps.lastIndex) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .border(1.5.dp, Color(0xFFFFE082), RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onFinish() }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Пропустить →",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}