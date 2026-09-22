package ru.finpet.kids.feature.finik

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Было: два infiniteRepeatable-аниматора перерисовывали ВЕСЬ Canvas 240dp
 * на каждом кадре (дыхание ±2px + моргание) — 60fps CPU-растра даже когда
 * вкладка скрыта.
 * Стало:
 * - дыхание применяется через graphicsLayer.translationY — двигает GPU-слой,
 *   перерисовки Canvas нет вообще;
 * - моргание дискретное (2 перерисовки за ~3.5с вместо ~210);
 * - кресло и питомец — два отдельных статичных Canvas;
 * - enabled=false (вкладка скрыта): аниматоров нет в композиции, ток CPU ~0.
 */
@Composable
fun FinikInArmchair(
    modifier: Modifier = Modifier,
    mood: String = "HAPPY", // HAPPY, NEUTRAL, SAD
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    if (enabled) {
        AnimatedFinik(modifier = modifier, mood = mood, onClick = onClick)
    } else {
        FinikFigure(
            modifier = modifier,
            mood = mood,
            eyesOpen = true,
            breathState = null,
            onClick = onClick
        )
    }
}

@Composable
private fun AnimatedFinik(
    modifier: Modifier = Modifier,
    mood: String,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "finik_motion")

    // Дыхание. Значение НЕ читается в композиции — только внутри graphicsLayer,
    // поэтому каждый кадр это инвалидация GPU-слоя, а не рекомпозиция/растр.
    val breathState: State<Float> = infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // Моргание — дискретное состояние вместо непрерывного твина 0..1
    var eyesOpen by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3400)
            eyesOpen = false
            delay(150)
            eyesOpen = true
        }
    }

    FinikFigure(
        modifier = modifier,
        mood = mood,
        eyesOpen = eyesOpen,
        breathState = breathState,
        onClick = onClick
    )
}

@Composable
private fun FinikFigure(
    modifier: Modifier = Modifier,
    mood: String,
    eyesOpen: Boolean,
    breathState: State<Float>?,
    onClick: () -> Unit
) {
    var isTapped by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .size(240.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isTapped = true
                onClick()
                scope.launch {
                    delay(300)
                    isTapped = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // --- 1. Уютное кресло: полностью статичный слой, рисуется один раз ---
        Canvas(modifier = Modifier.size(240.dp)) {
            val width = size.width
            val height = size.height

            // Спинка кресла
            drawRoundRect(
                color = Color(0xFFD87D56),
                topLeft = Offset(width * 0.18f, height * 0.16f),
                size = Size(width * 0.64f, height * 0.58f),
                cornerRadius = CornerRadius(40f, 40f)
            )
            // Внутренняя подушка спинки
            drawRoundRect(
                color = Color(0xFFE58F69),
                topLeft = Offset(width * 0.23f, height * 0.20f),
                size = Size(width * 0.54f, height * 0.48f),
                cornerRadius = CornerRadius(30f, 30f)
            )

            // Подлокотники
            drawRoundRect(
                color = Color(0xFFBD643F),
                topLeft = Offset(width * 0.10f, height * 0.46f),
                size = Size(width * 0.16f, height * 0.36f),
                cornerRadius = CornerRadius(24f, 24f)
            )
            drawRoundRect(
                color = Color(0xFFBD643F),
                topLeft = Offset(width * 0.74f, height * 0.46f),
                size = Size(width * 0.16f, height * 0.36f),
                cornerRadius = CornerRadius(24f, 24f)
            )

            // Сиденье
            drawRoundRect(
                color = Color(0xFFD87D56),
                topLeft = Offset(width * 0.16f, height * 0.62f),
                size = Size(width * 0.68f, height * 0.24f),
                cornerRadius = CornerRadius(26f, 26f)
            )

            // Ножки кресла
            drawLine(
                color = Color(0xFF6D381E),
                start = Offset(width * 0.22f, height * 0.84f),
                end = Offset(width * 0.18f, height * 0.96f),
                strokeWidth = 14f
            )
            drawLine(
                color = Color(0xFF6D381E),
                start = Offset(width * 0.78f, height * 0.84f),
                end = Offset(width * 0.82f, height * 0.96f),
                strokeWidth = 14f
            )
        }

        // --- 2. Финни: статичный растр, движение только GPU-слоем ---
        Canvas(
            modifier = Modifier
                .size(240.dp)
                .graphicsLayer {
                    // Чтение State внутри слоя = инвалидация слоя без рекомпозиции
                    translationY = (breathState?.value ?: 0f) + (if (isTapped) -8f else 0f)
                }
        ) {
            val width = size.width
            val height = size.height

            // Хвостик выглядывает из-за кресла
            val tailPath = Path().apply {
                moveTo(width * 0.72f, height * 0.64f)
                quadraticTo(
                    width * 0.92f, height * 0.50f,
                    width * 0.85f, height * 0.38f
                )
            }
            drawPath(
                path = tailPath,
                color = Color(0xFFFF9800),
                style = Stroke(width = 24f)
            )
            // Белый кончик хвоста
            drawCircle(
                color = Color(0xFFFFF3E0),
                radius = 14f,
                center = Offset(width * 0.85f, height * 0.38f)
            )

            // Тело Финни
            drawRoundRect(
                color = Color(0xFFFF9800),
                topLeft = Offset(width * 0.32f, height * 0.44f),
                size = Size(width * 0.36f, height * 0.32f),
                cornerRadius = CornerRadius(36f, 36f)
            )
            // Животик
            drawRoundRect(
                color = Color(0xFFFFF3E0),
                topLeft = Offset(width * 0.38f, height * 0.52f),
                size = Size(width * 0.24f, height * 0.22f),
                cornerRadius = CornerRadius(24f, 24f)
            )

            // Голова
            drawCircle(
                color = Color(0xFFFF9800),
                radius = width * 0.20f,
                center = Offset(width * 0.50f, height * 0.34f)
            )

            // Ушки
            val leftEar = Path().apply {
                moveTo(width * 0.34f, height * 0.24f)
                lineTo(width * 0.28f, height * 0.10f)
                lineTo(width * 0.44f, height * 0.18f)
                close()
            }
            drawPath(leftEar, Color(0xFFFF9800), style = Fill)
            val leftEarInner = Path().apply {
                moveTo(width * 0.35f, height * 0.22f)
                lineTo(width * 0.30f, height * 0.13f)
                lineTo(width * 0.42f, height * 0.18f)
                close()
            }
            drawPath(leftEarInner, Color(0xFFFFCCBC), style = Fill)

            val rightEar = Path().apply {
                moveTo(width * 0.66f, height * 0.24f)
                lineTo(width * 0.72f, height * 0.10f)
                lineTo(width * 0.56f, height * 0.18f)
                close()
            }
            drawPath(rightEar, Color(0xFFFF9800), style = Fill)
            val rightEarInner = Path().apply {
                moveTo(width * 0.65f, height * 0.22f)
                lineTo(width * 0.70f, height * 0.13f)
                lineTo(width * 0.58f, height * 0.18f)
                close()
            }
            drawPath(rightEarInner, Color(0xFFFFCCBC), style = Fill)

            // Глазки: два дискретных состояния вместо непрерывного твина
            val eyeHeight = if (eyesOpen) 10f else 2f
            drawRoundRect(
                color = Color(0xFF2E384D),
                topLeft = Offset(width * 0.40f, height * 0.32f - eyeHeight / 2),
                size = Size(10f, eyeHeight),
                cornerRadius = CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = Color(0xFF2E384D),
                topLeft = Offset(width * 0.57f, height * 0.32f - eyeHeight / 2),
                size = Size(10f, eyeHeight),
                cornerRadius = CornerRadius(5f, 5f)
            )

            // Щечки
            drawCircle(
                color = Color(0xFFFF8A80).copy(alpha = 0.6f),
                radius = 10f,
                center = Offset(width * 0.36f, height * 0.37f)
            )
            drawCircle(
                color = Color(0xFFFF8A80).copy(alpha = 0.6f),
                radius = 10f,
                center = Offset(width * 0.64f, height * 0.37f)
            )

            // Носик
            drawCircle(
                color = Color(0xFF5D4037),
                radius = 5f,
                center = Offset(width * 0.50f, height * 0.35f)
            )

            // Ротик в зависимости от настроения
            when (mood) {
                "SAD" -> {
                    // Грустный ротик
                    val mouthPath = Path().apply {
                        moveTo(width * 0.46f, height * 0.40f)
                        quadraticTo(
                            width * 0.50f, height * 0.37f,
                            width * 0.54f, height * 0.40f
                        )
                    }
                    drawPath(mouthPath, Color(0xFF5D4037), style = Stroke(width = 3.5f))
                }
                else -> {
                    // Улыбающийся ротик
                    val mouthPath = Path().apply {
                        moveTo(width * 0.45f, height * 0.38f)
                        quadraticTo(
                            width * 0.50f, height * 0.42f,
                            width * 0.55f, height * 0.38f
                        )
                    }
                    drawPath(mouthPath, Color(0xFF5D4037), style = Stroke(width = 3.5f))
                }
            }

            // Лапки на подлокотниках кресла
            drawCircle(
                color = Color(0xFFFFF3E0),
                radius = 14f,
                center = Offset(width * 0.22f, height * 0.56f)
            )
            drawCircle(
                color = Color(0xFFFFF3E0),
                radius = 14f,
                center = Offset(width * 0.78f, height * 0.56f)
            )
        }
    }
}
