package ru.finpet.kids.feature.map.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.finpet.kids.R
import kotlin.math.sin

/**
 * Плавная 60/120 FPS анимация плывущих пиксельных облаков над картой.
 *
 * Вместо дискретных резких скачков между кадрами облако непрерывно и мягко
 * скользит по диагональной траектории неба на RenderThread (без рекомпозиций).
 *
 * Особенности:
 * - 3 эшелона облаков (основное, среднее и далёкое) создают эффект живого параллакса.
 * - Днём синхронно отображается мягкая пиксельная тень на земле (как в Stardew Valley).
 * - Ночью отображаются лунные тёмные облака.
 * - Плавные естественные синусоидальные покачивания по высоте создают ощущение полёта.
 */
@Composable
fun MovingCloudsOverlay(
    isNight: Boolean,
    modifier: Modifier = Modifier,
    speedMultiplier: Float = 1.0f
) {
    val whiteCloudRes = R.drawable.cloud_crop_4
    val darkCloudRes = R.drawable.cloud_dark_crop_4

    val infiniteTransition = rememberInfiniteTransition(label = "clouds_transition")

    // Волна 1: Основное облако (цикл 18 сек)
    val rawProgress1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (18000 / speedMultiplier).toInt().coerceAtLeast(1000),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud1"
    )

    // Волна 2: Среднее облако (цикл 24 сек)
    val rawProgress2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (24000 / speedMultiplier).toInt().coerceAtLeast(1000),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud2"
    )

    // Волна 3: Далёкое маленькое облачко для параллакса (цикл 32 сек)
    val rawProgress3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (32000 / speedMultiplier).toInt().coerceAtLeast(1000),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud3"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val totalWidthPx = with(density) { maxWidth.toPx() }
        val totalHeightPx = with(density) { maxHeight.toPx() }

        // Соотношение сторон пиксельного спрайта облака: 488 x 94
        val spriteAspect = 488f / 94f

        // ----------------------------------------------------
        // ОБЛАКО 1: Основное (верхний и средний эшелон)
        // ----------------------------------------------------
        val width1Dp = maxWidth * 0.65f
        val height1Dp = width1Dp / spriteAspect
        val width1Px = with(density) { width1Dp.toPx() }
        val height1Px = with(density) { height1Dp.toPx() }

        SmoothCloudSprite(
            progressProvider = {
                // Прямой цикл с паузой 15% в конце перед новым появлением
                val phase = rawProgress1
                if (phase < 0.85f) phase / 0.85f else 2.0f
            },
            totalWidthPx = totalWidthPx,
            totalHeightPx = totalHeightPx,
            cloudWidthPx = width1Px,
            cloudHeightPx = height1Px,
            cloudWidthDp = width1Dp,
            cloudHeightDp = height1Dp,
            startYRatio = 0.32f,
            endYRatio = 0.08f,
            sineAmplitudePx = with(density) { 6.dp.toPx() },
            isNight = isNight,
            whiteRes = whiteCloudRes,
            darkRes = darkCloudRes,
            cloudAlpha = 0.92f,
            shadowAlpha = 0.22f,
            shadowOffsetXPx = with(density) { 14.dp.toPx() },
            shadowOffsetYPx = with(density) { 24.dp.toPx() }
        )

        // ----------------------------------------------------
        // ОБЛАКО 2: Среднее (сдвиг фазы на 0.45, средний эшелон)
        // ----------------------------------------------------
        val width2Dp = maxWidth * 0.50f
        val height2Dp = width2Dp / spriteAspect
        val width2Px = with(density) { width2Dp.toPx() }
        val height2Px = with(density) { height2Dp.toPx() }

        SmoothCloudSprite(
            progressProvider = {
                val phase = (rawProgress2 + 0.45f) % 1.0f
                if (phase < 0.85f) phase / 0.85f else 2.0f
            },
            totalWidthPx = totalWidthPx,
            totalHeightPx = totalHeightPx,
            cloudWidthPx = width2Px,
            cloudHeightPx = height2Px,
            cloudWidthDp = width2Dp,
            cloudHeightDp = height2Dp,
            startYRatio = 0.52f,
            endYRatio = 0.28f,
            sineAmplitudePx = with(density) { 5.dp.toPx() },
            isNight = isNight,
            whiteRes = whiteCloudRes,
            darkRes = darkCloudRes,
            cloudAlpha = 0.86f,
            shadowAlpha = 0.18f,
            shadowOffsetXPx = with(density) { 10.dp.toPx() },
            shadowOffsetYPx = with(density) { 18.dp.toPx() }
        )

        // ----------------------------------------------------
        // ОБЛАКО 3: Далёкое маленькое (параллакс глубина)
        // ----------------------------------------------------
        val width3Dp = maxWidth * 0.36f
        val height3Dp = width3Dp / spriteAspect
        val width3Px = with(density) { width3Dp.toPx() }
        val height3Px = with(density) { height3Dp.toPx() }

        SmoothCloudSprite(
            progressProvider = {
                val phase = (rawProgress3 + 0.78f) % 1.0f
                if (phase < 0.88f) phase / 0.88f else 2.0f
            },
            totalWidthPx = totalWidthPx,
            totalHeightPx = totalHeightPx,
            cloudWidthPx = width3Px,
            cloudHeightPx = height3Px,
            cloudWidthDp = width3Dp,
            cloudHeightDp = height3Dp,
            startYRatio = 0.18f,
            endYRatio = 0.04f,
            sineAmplitudePx = with(density) { 4.dp.toPx() },
            isNight = isNight,
            whiteRes = whiteCloudRes,
            darkRes = darkCloudRes,
            cloudAlpha = 0.70f,
            shadowAlpha = 0.12f,
            shadowOffsetXPx = with(density) { 8.dp.toPx() },
            shadowOffsetYPx = with(density) { 14.dp.toPx() }
        )
    }
}

/**
 * Отрисовка отдельного спрайта облака с его тенью.
 * Смещение вычисляется внутри graphicsLayer lambda, гарантируя 60/120 FPS
 * без единой рекомпозиции Composable узлов.
 */
@Composable
private fun SmoothCloudSprite(
    progressProvider: () -> Float,
    totalWidthPx: Float,
    totalHeightPx: Float,
    cloudWidthPx: Float,
    cloudHeightPx: Float,
    cloudWidthDp: androidx.compose.ui.unit.Dp,
    cloudHeightDp: androidx.compose.ui.unit.Dp,
    startYRatio: Float,
    endYRatio: Float,
    sineAmplitudePx: Float,
    isNight: Boolean,
    whiteRes: Int,
    darkRes: Int,
    cloudAlpha: Float,
    shadowAlpha: Float,
    shadowOffsetXPx: Float,
    shadowOffsetYPx: Float
) {
    // 1. Тень облака на земле (только днем)
    if (!isNight) {
        Image(
            painter = painterResource(id = darkRes),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(width = cloudWidthDp, height = cloudHeightDp)
                .graphicsLayer {
                    val p = progressProvider()
                    if (p > 1.0f) {
                        alpha = 0f
                    } else {
                        val startX = -cloudWidthPx
                        val endX = totalWidthPx
                        translationX = startX + p * (endX - startX) + shadowOffsetXPx

                        val startY = totalHeightPx * startYRatio
                        val endY = totalHeightPx * endYRatio
                        val sine = sin(p * Math.PI.toFloat() * 2f) * sineAmplitudePx
                        translationY = startY + p * (endY - startY) + sine + shadowOffsetYPx

                        alpha = shadowAlpha
                    }
                }
        )
    }

    // 2. Само облако
    Image(
        painter = painterResource(id = if (isNight) darkRes else whiteRes),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .size(width = cloudWidthDp, height = cloudHeightDp)
            .graphicsLayer {
                val p = progressProvider()
                if (p > 1.0f) {
                    alpha = 0f
                } else {
                    val startX = -cloudWidthPx
                    val endX = totalWidthPx
                    translationX = startX + p * (endX - startX)

                    val startY = totalHeightPx * startYRatio
                    val endY = totalHeightPx * endYRatio
                    val sine = sin(p * Math.PI.toFloat() * 2f) * sineAmplitudePx
                    translationY = startY + p * (endY - startY) + sine

                    alpha = if (isNight) (cloudAlpha * 0.88f) else cloudAlpha
                }
            }
    )
}
