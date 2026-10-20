package ru.finpet.kids.core.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import ru.finpet.kids.R

/**
 * Ряд пиксельных орбов для дискретного отображения прогресса в стиле Stardew Valley.
 * Каждый орб = (100 / orbCount) %.
 * Заполненные орбы тонируются в tintColor, пустые — в emptyColor.
 */
@Composable
fun OrbRow(
    filledRatio: Float,            // 0f..1f
    tintColor: Color,
    modifier: Modifier = Modifier,
    orbCount: Int = 20,            // сколько орбов в ряду
    emptyColor: Color = Color(0xFFD7CCC8)
) {
    val safeRatio = filledRatio.coerceIn(0f, 1f)
    val animatedRatio by animateFloatAsState(
        targetValue = safeRatio,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "orb_row_anim"
    )
    val filledCount = (animatedRatio * orbCount).roundToInt().coerceIn(0, orbCount)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(orbCount) { i ->
            val isFilled = i < filledCount
            Image(
                painter = painterResource(R.drawable.orb),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(
                    color = if (isFilled) tintColor else emptyColor,
                    blendMode = BlendMode.SrcIn
                ),
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
        }
    }
}

/**
 * Стат-бар, заполненный орбами с заголовком и иконкой.
 */
@Composable
fun OrbStatBar(
    label: String,
    icon: String,
    value: Int,                    // 0..100
    tintColor: Color,
    modifier: Modifier = Modifier,
    orbCount: Int = 20,            // сколько орбов на полный бар
    showWarning: Boolean = true,
    valueText: String? = null
) {
    val uiScale = LocalUiScale.current
    val safeValue = value.coerceIn(0, 100)
    val animatedValue by animateIntAsState(
        targetValue = safeValue,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "orb_stat_anim"
    )

    Column(modifier = modifier) {
        // Заголовок: иконка + название + процент/значение
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RenderBoardIcon(icon = icon, modifier = Modifier.size((16.dp * uiScale).coerceIn(12.dp, 18.dp)))
                Spacer(modifier = Modifier.width((4.dp * uiScale).coerceIn(3.dp, 6.dp)))
                Text(
                    text = label,
                    fontSize = (16f * uiScale).coerceIn(12f, 17f).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                if (safeValue <= 30 && showWarning) {
                    Spacer(modifier = Modifier.width((6.dp * uiScale).coerceIn(4.dp, 8.dp)))
                    ExclamationIcon(modifier = Modifier.size((16.dp * uiScale).coerceIn(12.dp, 18.dp)))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = valueText ?: "$animatedValue%",
                    fontSize = (16f * uiScale).coerceIn(12f, 17f).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (safeValue <= 30 && showWarning) Color(0xFFD32F2F) else tintColor
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Ряд орбов
        OrbRow(
            filledRatio = safeValue / 100f,
            tintColor = tintColor,
            orbCount = orbCount,
            emptyColor = Color(0xFFE0E0E0)
        )
    }
}
