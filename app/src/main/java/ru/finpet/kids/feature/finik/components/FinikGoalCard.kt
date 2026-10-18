package ru.finpet.kids.feature.finik.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary

/**
 * Карточка активной цели накопления на главном экране питомца:
 * - Плавный прогресс-бар с контрастным тёплым песочным треком Color(0xFFD7C4A5).
 * - Клик без эффекта вспышки/ховера (indication = null).
 */
@Composable
fun FinikGoalCard(
    activeGoal: GoalEntity,
    onNavigateToGoals: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val progress = (activeGoal.savedAmount.toFloat() / activeGoal.targetCost.toFloat()).coerceIn(0f, 1f)
    val percent = (progress * 100).toInt()
    val goalEmoji = when (activeGoal.id) {
        "goal_ball" -> "⚽"
        "goal_headphones" -> "🎧"
        "goal_scooter" -> "🛴"
        else -> "🎯"
    }
    val animatedGoalProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "goal_anim"
    )
    val goalBrush = remember {
        Brush.horizontalGradient(listOf(Color(0xFFFFB74D), JoyOrange))
    }

    FinCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .then(
                if (onNavigateToGoals != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onNavigateToGoals(activeGoal.id) }
                } else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(text = goalEmoji, fontSize = 17.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Цель: ${activeGoal.title}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${activeGoal.savedAmount} / ${activeGoal.targetCost} ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = JoyOrange
                    )
                    CoinIcon(modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "($percent%)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeGoal.isReached) FreshGreen else TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Контрастный песочный трек для прогресса цели
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(13.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFD7C4A5))
                    .border(1.dp, Color(0xFF8D6E63).copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedGoalProgress)
                        .clip(RoundedCornerShape(6.dp))
                        .background(goalBrush)
                )
            }
        }
    }
}
