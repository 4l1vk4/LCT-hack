package ru.finpet.kids.feature.map.sheets.components

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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.TextPrimary
import androidx.compose.foundation.ExperimentalFoundationApi
import ru.finpet.kids.core.designsystem.TextSecondary

/**
 * Карточка финансовой цели в копилке мечты:
 * - Отображает название цели, иконку, целевую сумму и накопленный прогресс.
 * - Прогресс-бар использует тёплый песочный трек Color(0xFFD7C4A5) вместо блеклого серого.
 * - Кнопки отложить и снять выполнены в пиксельном дизайне PixelButton.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GoalProgressCard(
    goal: GoalEntity,
    isSelected: Boolean,
    isTarget: Boolean,
    balance: Int,
    bringIntoViewRequester: BringIntoViewRequester,
    onSelectActiveGoal: (String) -> Unit,
    onDeposit: (String, Int) -> Unit,
    onWithdrawClick: (GoalEntity) -> Unit
) {
    val progress = (goal.savedAmount.toFloat() / goal.targetCost.toFloat()).coerceIn(0f, 1f)
    val percent = (progress * 100).toInt()
    val remaining = (goal.targetCost - goal.savedAmount).coerceAtLeast(0)

    val goalEmoji = when (goal.id) {
        "goal_ball" -> "⚽"
        "goal_headphones" -> "🎧"
        "goal_scooter" -> "🛴"
        else -> "🎯"
    }

    FinCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isTarget) Modifier.bringIntoViewRequester(bringIntoViewRequester) else Modifier)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Переключатель активной главной цели
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelectActiveGoal(goal.id) }
                        .padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) JoyOrange else Color(0xFFD7C4A5))
                            .border(
                                width = 1.5.dp,
                                color = if (isSelected) PixelGoldDark else Color(0xFF8D6E63).copy(alpha = 0.5f),
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text(
                                text = "✓",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = goalEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = goal.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (isSelected) {
                            Text(
                                text = "⭐ Главная цель",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = JoyOrange
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${goal.savedAmount} / ${goal.targetCost} ",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = JoyOrange
                    )
                    CoinIcon(modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Прогресс-бар накопления: тёплый песочный трек с чёткой границей
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFD7C4A5))
                    .border(1.dp, Color(0xFF8D6E63).copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(6.dp))
                        .background(FreshGreen)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (goal.isReached) "🎉 Мечта достигнута!" else "Осталось накопить: $remaining монет ($percent%)",
                fontSize = 15.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопки пополнения и снятия в пиксельном дизайне
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PixelButton(
                    text = "+10 в копилку",
                    onClick = { onDeposit(goal.id, 10) },
                    enabled = balance >= 10,
                    containerColor = PixelGreenCrop,
                    textColor = Color.White,
                    borderColor = Color(0xFF1B5E20),
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )

                if (goal.savedAmount > 0) {
                    PixelButton(
                        text = "Взять -10",
                        onClick = { onWithdrawClick(goal) },
                        containerColor = Color(0xFFFFF3E0),
                        textColor = PixelRedBerry,
                        borderColor = Color(0xFFE57373),
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
