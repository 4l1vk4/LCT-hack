package ru.finpet.kids.feature.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinText
import ru.finpet.kids.core.designsystem.PixelBlueWater
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
import ru.finpet.kids.core.designsystem.PixelWoodBevel
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.PixelWoodLight
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetCompliance
import ru.finpet.kids.core.domain.model.BudgetPlan

/**
 * карточка детального сравнения «план vs факт» по категориям (ТЗ 2.5.5).
 * отображает статус трех звезд бюджета и шкалу соблюдения плана по корзинам.
 */
@Composable
fun BudgetPlanVsFactCard(
    currentDay: Int,
    plan: BudgetPlan,
    actual: ActualExpenses,
    compliance: BudgetCompliance,
    onEditPlan: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    val stars = remember(plan, actual) { calculateBudgetStars(plan, actual) }

    StardewBoard(
        headerTitle = "ПЛАН vs ФАКТ • ДЕНЬ $currentDay",
        headerIcon = "📊"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // верхняя сводная плашка: запланировано vs фактически потрачено
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(PixelParchmentMedium)
                    .border(1.dp, PixelParchmentBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "План: ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PixelTextDark,
                        fontFamily = AppFontFamily
                    )
                    CoinText(
                        prefix = "${plan.totalPlanned}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PixelTextDark,
                        fontFamily = AppFontFamily
                    )
                    Text(
                        text = "• Факт: ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PixelTextDark,
                        fontFamily = AppFontFamily
                    )
                    CoinText(
                        prefix = "${actual.totalActual}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PixelTextDark,
                        fontFamily = AppFontFamily
                    )
                    Text(
                        text = if (compliance.isDisciplined) "🌟 Отличная дисциплина трат!" else "💡 Следи за покупками в Лавке",
                        fontSize = 11.sp,
                        color = PixelTextMuted
                    )
                }

                // бейдж общего процента соблюдения плана
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                compliance.totalPercent >= 75 -> PixelGreenCrop
                                compliance.totalPercent >= 50 -> PixelGoldDark
                                else -> PixelRedBerry
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${compliance.totalPercent}%",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        fontFamily = AppFontFamily
                    )
                }
            }

            // блок трех звезд бюджета по ТЗ 2.5.5
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(PixelWoodDark)
                    .padding(1.dp)
                    .border(1.dp, PixelWoodBevel, RoundedCornerShape(5.dp))
                    .background(PixelWoodMedium)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⭐⭐⭐", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "3 ЗВЕЗДЫ БЮДЖЕТА",
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = PixelGoldBright
                            )
                        }
                        Text(
                            text = "${stars.count}/3 ЗВЁЗД",
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = if (stars.isAllEarned) PixelGoldBright else PixelParchmentLight
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BudgetStarChip(
                            title = "Забота",
                            isEarned = stars.careStar,
                            activeDesc = "Обязательное",
                            inactiveDesc = "Купи еду",
                            modifier = Modifier.weight(1f)
                        )
                        BudgetStarChip(
                            title = "Мечта",
                            isEarned = stars.dreamStar,
                            activeDesc = "В копилке",
                            inactiveDesc = "Отложи монетки",
                            modifier = Modifier.weight(1f)
                        )
                        BudgetStarChip(
                            title = "Мудрость",
                            isEarned = stars.wisdomStar,
                            activeDesc = "В лимите",
                            inactiveDesc = "Перерасход",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // корзина 1: обязательные траты (еда, уход)
            CategoryComparisonRow(
                icon = "🍲",
                title = "«Миска жизни» (Обязательное)",
                starStatus = if (stars.careStar) "⭐" else "⚪",
                planned = plan.plannedMandatory,
                actual = actual.actualMandatory,
                color = PixelGreenCrop,
                isCompleted = actual.actualMandatory >= plan.plannedMandatory && plan.plannedMandatory > 0
            )

            // корзина 2: спонтанные траты (желаемое)
            CategoryComparisonRow(
                icon = "🎈",
                title = "«Сундучок радости» (Желаемое)",
                starStatus = if (stars.wisdomStar) "⭐" else "⚪",
                planned = plan.plannedOptional,
                actual = actual.actualOptional,
                color = PixelGoldDark,
                isOverspent = actual.actualOptional > plan.plannedOptional
            )

            // корзина 3: накопления в копилку
            CategoryComparisonRow(
                icon = "🏦",
                title = "«Копилка мечты» (В копилку)",
                starStatus = if (stars.dreamStar) "⭐" else "⚪",
                planned = plan.plannedSavings,
                actual = actual.actualSavings,
                color = PixelBlueWater,
                isCompleted = actual.actualSavings >= plan.plannedSavings && plan.plannedSavings > 0
            )

            // совет от питомца
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFFFF9E6))
                    .border(1.dp, PixelGoldBright, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "💬 ${compliance.feedback}",
                    fontSize = 11.sp,
                    color = PixelTextDark,
                    lineHeight = 15.sp
                )
            }

            // кнопки действий
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onDismiss != null) {
                    PixelButton(
                        text = "✕ Закрыть",
                        onClick = onDismiss,
                        containerColor = PixelWoodMedium,
                        textColor = PixelParchmentLight,
                        borderColor = PixelWoodDark,
                        modifier = Modifier.weight(1f)
                    )
                }
                PixelButton(
                    text = "✏️ Изменить план",
                    onClick = onEditPlan,
                    containerColor = PixelParchmentMedium,
                    textColor = PixelTextDark,
                    borderColor = PixelParchmentBorder,
                    modifier = Modifier.weight(if (onDismiss != null) 1.5f else 1f)
                )
            }
        }
    }
}

/**
 * диалоговая обертка над BudgetPlanVsFactCard для открытия по тапу на Пульс Бюджета
 */
@Composable
fun BudgetPlanVsFactDialog(
    currentDay: Int,
    plan: BudgetPlan,
    actual: ActualExpenses,
    compliance: BudgetCompliance,
    onEditPlan: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(8.dp))
        ) {
            BudgetPlanVsFactCard(
                currentDay = currentDay,
                plan = plan,
                actual = actual,
                compliance = compliance,
                onEditPlan = onEditPlan,
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * компактная плашка отдельной звезды бюджета
 */
@Composable
private fun BudgetStarChip(
    title: String,
    isEarned: Boolean,
    activeDesc: String,
    inactiveDesc: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isEarned) Color(0xFF1B5E20) else PixelWoodDark)
            .border(
                1.dp,
                if (isEarned) PixelGoldBright else PixelWoodLight,
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 4.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (isEarned) "⭐" else "⚪", fontSize = 11.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (isEarned) PixelGoldBright else PixelParchmentLight
                )
            }
            Text(
                text = if (isEarned) activeDesc else inactiveDesc,
                fontSize = 8.sp,
                color = if (isEarned) Color(0xFFC8E6C9) else Color(0xFFB0BEC5),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * строка сопоставления плана и факта по категории с индикатором наполнения
 */
@Composable
private fun CategoryComparisonRow(
    icon: String,
    title: String,
    starStatus: String,
    planned: Int,
    actual: Int,
    color: Color,
    isCompleted: Boolean = false,
    isOverspent: Boolean = false
) {
    val progress = if (planned > 0) (actual.toFloat() / planned.toFloat()).coerceIn(0f, 1f) else if (actual > 0) 1f else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(PixelParchmentLight)
            .border(1.dp, PixelParchmentBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PixelTextDark
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = starStatus, fontSize = 14.sp)
            }
            CoinText(
                prefix = "$actual / $planned",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = if (isOverspent) PixelRedBerry else color,
                fontFamily = AppFontFamily
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progress },
            color = if (isOverspent) PixelRedBerry else color,
            trackColor = PixelParchmentBorder,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}
