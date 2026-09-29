package ru.finpet.kids.core.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * Стат-бар, заполненный орбами.
 * Каждый орб = (100 / orbCount) %.
 * Заполненные орбы тонируются в tintColor, пустые — серые.
 */
@Composable
fun OrbStatBar(
    label: String,
    icon: String,
    value: Int,                    // 0..100
    tintColor: Color,
    modifier: Modifier = Modifier,
    orbCount: Int = 20             // сколько орбов на полный бар
) {
    val safeValue = value.coerceIn(0, 100)
    val filledCount = ((safeValue / 100f) * orbCount).roundToInt().coerceIn(0, orbCount)

    Column(modifier = modifier) {
        // Заголовок: иконка + название + процент
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Text(
                text = "$safeValue%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = tintColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Ряд орбов
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        color = if (isFilled) tintColor else Color(0xFFE0E0E0),
                        blendMode = BlendMode.SrcIn
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                )
            }
        }
    }
}