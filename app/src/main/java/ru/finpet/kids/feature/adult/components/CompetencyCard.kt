package ru.finpet.kids.feature.adult.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.core.domain.usecase.CompetencyItem

/**
 * карточка одной финансовой компетенции ребенка из ТЗ (Минфин РФ).
 * показывает шкалу освоения навыка, оценку и краткий совет родителю.
 */
@Composable
fun CompetencyCard(
    competency: CompetencyItem,
    modifier: Modifier = Modifier
) {
    // подсвечиваем зеленым при успехе от 70%, иначе предупреждающим оранжевым
    val isConfident = competency.scorePercent >= 70
    val accentBrush = remember(isConfident) {
        if (isConfident) {
            Brush.horizontalGradient(listOf(Color(0xFF81C784), FreshGreen))
        } else {
            Brush.horizontalGradient(listOf(Color(0xFFFFB74D), JoyOrange))
        }
    }
    val accentColor = if (isConfident) FreshGreen else JoyOrange

    FinCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = competency.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${competency.scorePercent}%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // прогресс-бар компетенции (контрастный песочный трек как на главном экране)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(11.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFD7C4A5))
                    .border(1.dp, Color(0xFF8D6E63).copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((competency.scorePercent / 100f).coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentBrush)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Статус: ${competency.statusDescription}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = competency.advice,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
