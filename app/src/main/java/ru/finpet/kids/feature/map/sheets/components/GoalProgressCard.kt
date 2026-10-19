package ru.finpet.kids.feature.map.sheets.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.StarIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.PixelAlertDialog
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.TargetIcon
import ru.finpet.kids.core.designsystem.TextPrimary
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import ru.finpet.kids.R
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.core.designsystem.pixelButtonBackground

@Composable
fun GoalStickerIcon(
    goalId: String,
    modifier: Modifier = Modifier.size(28.dp)
) {
    when (goalId) {
        "goal_ball" -> Image(
            painter = painterResource(id = R.drawable.ball),
            contentDescription = "Футбольный мяч",
            modifier = modifier
        )
        "goal_headphones" -> Image(
            painter = painterResource(id = R.drawable.headphones),
            contentDescription = "Беспроводные наушники",
            modifier = modifier
        )
        "goal_scooter" -> Image(
            painter = painterResource(id = R.drawable.samokat),
            contentDescription = "Городской самокат",
            modifier = modifier
        )
        else -> TargetIcon(modifier = modifier)
    }
}

/**
 * Карточка финансовой цели в копилке мечты:
 * - Отображает название цели, иконку-стикер, целевую сумму и накопленный прогресс.
 * - Прогресс-бар использует тёплый песочный трек Color(0xFFD7C4A5) вместо блеклого серого.
 * - Кнопки отложить и снять выполнены в пиксельном дизайне PixelButton.
 * - При достижении 100% блокируется снятие денег и отображается золотая плашка успеха.
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
    onWithdrawClick: (GoalEntity) -> Unit,
    onCustomWithdraw: (String, Int) -> Unit = { _, _ -> }
) {
    val isReached = goal.isReached || goal.savedAmount >= goal.targetCost
    val progress = (goal.savedAmount.toFloat() / goal.targetCost.toFloat()).coerceIn(0f, 1f)
    val percent = (progress * 100).toInt()
    val remaining = (goal.targetCost - goal.savedAmount).coerceAtLeast(0)

    var showCustomDialog by remember { mutableStateOf(false) }
    var isDepositTab by remember { mutableStateOf(true) }
    var customAmountText by remember { mutableStateOf("10") }

    if (showCustomDialog) {
        val maxDeposit = minOf(balance, remaining)
        val maxWithdraw = goal.savedAmount
        val currentMax = if (isDepositTab) maxDeposit else maxWithdraw

        val focusManager = LocalFocusManager.current
        PixelAlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = if (isDepositTab) "ОТЛОЖИТЬ В КОПИЛКУ" else "ЗАБРАТЬ ИЗ КОПИЛКИ",
            icon = "",
            confirmButton = {
                PixelButton(
                    text = if (isDepositTab) "Положить" else "Забрать",
                    onClick = {
                        val entered = customAmountText.toIntOrNull() ?: 0
                        if (isDepositTab) {
                            val actual = entered.coerceIn(1, maxOf(1, maxDeposit))
                            if (actual > 0) onDeposit(goal.id, actual)
                        } else {
                            val actual = entered.coerceIn(1, maxOf(1, maxWithdraw))
                            if (actual > 0) onCustomWithdraw(goal.id, actual)
                        }
                        showCustomDialog = false
                    },
                    enabled = (customAmountText.toIntOrNull() ?: 0) in 1..currentMax,
                    containerColor = if (isDepositTab) PixelGreenCrop else PixelRedBerry,
                    textColor = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 16.sp
                )
            },
            dismissButton = {
                PixelButton(
                    text = "Отмена",
                    onClick = { showCustomDialog = false },
                    containerColor = PixelParchmentMedium,
                    textColor = PixelTextDark,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 16.sp
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
            ) {
                Text(
                    text = "Цель: ${goal.title}",
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PixelTextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "В копилке: ${goal.savedAmount} из ${goal.targetCost}",
                        fontFamily = AppFontFamily,
                        fontSize = 14.sp,
                        color = PixelTextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    CoinIcon(modifier = Modifier.size(15.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Вкладки: Положить / Снять
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PixelButton(
                        text = "Положить",
                        onClick = {
                            isDepositTab = true
                            customAmountText = minOf(10, maxDeposit).toString()
                        },
                        containerColor = if (isDepositTab) PixelGoldBright else PixelParchmentLight,
                        fontSize = 14.sp,
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    PixelButton(
                        text = "Забрать",
                        onClick = {
                            isDepositTab = false
                            customAmountText = minOf(10, maxWithdraw).toString()
                        },
                        enabled = maxWithdraw > 0,
                        containerColor = if (!isDepositTab) PixelGoldBright else PixelParchmentLight,
                        fontSize = 14.sp,
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isDepositTab) {
                        Text(
                            text = "Доступно: $balance",
                            fontFamily = AppFontFamily,
                            fontSize = 13.sp,
                            color = PixelTextMuted
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        CoinIcon(modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "(макс.: $maxDeposit",
                            fontFamily = AppFontFamily,
                            fontSize = 13.sp,
                            color = PixelTextMuted
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        CoinIcon(modifier = Modifier.size(14.dp))
                        Text(
                            text = ")",
                            fontFamily = AppFontFamily,
                            fontSize = 13.sp,
                            color = PixelTextMuted
                        )
                    } else {
                        Text(
                            text = "В копилке: $maxWithdraw",
                            fontFamily = AppFontFamily,
                            fontSize = 13.sp,
                            color = PixelTextMuted
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        CoinIcon(modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = customAmountText,
                    onValueChange = { customAmountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Количество монет", fontFamily = AppFontFamily, fontSize = 15.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PixelParchmentBorder,
                        unfocusedBorderColor = PixelParchmentBorder,
                        focusedContainerColor = PixelParchmentLight,
                        unfocusedContainerColor = PixelParchmentLight,
                        focusedTextColor = PixelTextDark,
                        unfocusedTextColor = PixelTextDark
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PixelButton(
                        text = "+10",
                        trailingIcon = { CoinIcon(modifier = Modifier.size(13.dp)) },
                        repeating = true,
                        onClick = {
                            val curr = customAmountText.toIntOrNull() ?: 0
                            customAmountText = (curr + 10).coerceAtMost(currentMax).toString()
                        },
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        containerColor = PixelParchmentLight,
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                    )
                    PixelButton(
                        text = "+50",
                        trailingIcon = { CoinIcon(modifier = Modifier.size(13.dp)) },
                        repeating = true,
                        onClick = {
                            val curr = customAmountText.toIntOrNull() ?: 0
                            customAmountText = (curr + 50).coerceAtMost(currentMax).toString()
                        },
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        containerColor = PixelParchmentLight,
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                    )
                    PixelButton(
                        text = "Все ($currentMax)",
                        onClick = { customAmountText = currentMax.toString() },
                        modifier = Modifier.weight(1.3f),
                        fontSize = 13.sp,
                        containerColor = PixelParchmentLight,
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                    )
                }
            }
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "goal_card_progress"
    )

    val checkboxScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "checkbox_scale"
    )

    val uiScale = LocalUiScale.current

    FinCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isTarget) Modifier.bringIntoViewRequester(bringIntoViewRequester) else Modifier)
    ) {
        Column(modifier = Modifier.padding((14.dp * uiScale).coerceIn(8.dp, 16.dp))) {
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
                            .graphicsLayer {
                                scaleX = checkboxScale
                                scaleY = checkboxScale
                            }
                            .size((26.dp * uiScale).coerceIn(18.dp, 28.dp))
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
                                fontSize = (16f * uiScale).coerceIn(12f, 18f).sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width((8.dp * uiScale).coerceIn(4.dp, 10.dp)))
                    GoalStickerIcon(goalId = goal.id, modifier = Modifier.size((26.dp * uiScale).coerceIn(18.dp, 28.dp)))
                    Spacer(modifier = Modifier.width((6.dp * uiScale).coerceIn(4.dp, 8.dp)))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.title,
                            fontSize = (17f * uiScale).coerceIn(12f, 18f).sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (isSelected) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StarIcon(modifier = Modifier.size((13.dp * uiScale).coerceIn(10.dp, 14.dp)))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Главная цель",
                                    fontSize = (13f * uiScale).coerceIn(10f, 15f).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JoyOrange
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = !isReached) {
                        customAmountText = minOf(10, balance, remaining).coerceAtLeast(1).toString()
                        isDepositTab = true
                        showCustomDialog = true
                    }
                ) {
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
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(6.dp))
                        .background(FreshGreen)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isReached) "Мечта достигнута! Предмет уже в домике!" else "Осталось накопить: $remaining монет ($percent%)",
                fontSize = 16.sp,
                lineHeight = 21.sp,
                color = if (isReached) FreshGreen else TextSecondary,
                fontWeight = if (isReached) FontWeight.Bold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопки пополнения и снятия в пиксельном дизайне
            if (isReached) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pixelButtonBackground(containerColor = PixelGoldBright)
                        .padding(vertical = 10.dp, horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Цель достигнута! Предмет украшает комнату котика!",
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = PixelTextDark,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                val depositText = if (uiScale < 0.85f) "+10" else "+10 в копилку"
                val withdrawText = if (uiScale < 0.85f) "-10" else "Взять -10"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy((4.dp * uiScale).coerceIn(3.dp, 6.dp))
                ) {
                    PixelButton(
                        text = depositText,
                        onClick = { onDeposit(goal.id, 10) },
                        enabled = balance >= 10,
                        repeating = true,
                        containerColor = PixelGreenCrop,
                        textColor = Color.White,
                        borderColor = Color(0xFF1B5E20),
                        fontSize = (14f * uiScale).coerceIn(11f, 15f).sp,
                        contentPadding = PaddingValues(
                            horizontal = (4.dp * uiScale).coerceIn(3.dp, 6.dp),
                            vertical = (5.dp * uiScale).coerceIn(3.dp, 6.dp)
                        ),
                        modifier = Modifier.weight(1.3f)
                    )

                    if (goal.savedAmount > 0) {
                        PixelButton(
                            text = withdrawText,
                            onClick = { onWithdrawClick(goal) },
                            containerColor = Color(0xFFFFF3E0),
                            textColor = PixelRedBerry,
                            borderColor = Color(0xFFE57373),
                            fontSize = (14f * uiScale).coerceIn(11f, 15f).sp,
                            contentPadding = PaddingValues(
                                horizontal = (4.dp * uiScale).coerceIn(3.dp, 6.dp),
                                vertical = (5.dp * uiScale).coerceIn(3.dp, 6.dp)
                            ),
                            modifier = Modifier.weight(1.1f)
                        )
                    }

                    PixelButton(
                        text = "Своя",
                        onClick = {
                            customAmountText = minOf(10, balance, remaining).coerceAtLeast(1).toString()
                            isDepositTab = true
                            showCustomDialog = true
                        },
                        containerColor = PixelGoldBright,
                        textColor = PixelTextDark,
                        fontSize = (13f * uiScale).coerceIn(11f, 14f).sp,
                        contentPadding = PaddingValues(
                            horizontal = (4.dp * uiScale).coerceIn(3.dp, 6.dp),
                            vertical = (5.dp * uiScale).coerceIn(3.dp, 6.dp)
                        ),
                        modifier = Modifier.weight(0.9f)
                    )
                }
            }

        }
    }
}
