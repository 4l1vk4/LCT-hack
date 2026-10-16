package ru.finpet.kids.feature.calendar.dialogs

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
import ru.finpet.kids.core.designsystem.AppFontFamily
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.core.domain.calculator.PetEconomyCalculator
import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetPlan
import ru.finpet.kids.feature.budget.DaySummaryBudgetSection
import ru.finpet.kids.feature.calendar.CalendarMonthInfo

/**
 * Всплывающее окно подтверждения перехода на следующий день с итогами бюджета (ТЗ 2.5.5, 2.5.11).
 */
@Composable
fun NextDayConfirmDialog(
    currentDay: Int,
    nextDay: Int,
    currentPeriod: PeriodEntity?,
    isNextMonday: Boolean,
    monthInfo: CalendarMonthInfo? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isConfirmed = currentPeriod?.isBudgetConfirmed == true
    val isMonthTransition = monthInfo != null && nextDay > monthInfo.daysInMonth
    val plan = remember(currentPeriod) {
        BudgetPlan(
            plannedMandatory = currentPeriod?.plannedMandatory ?: 0,
            plannedOptional = currentPeriod?.plannedOptional ?: 0,
            plannedSavings = currentPeriod?.plannedSavings ?: 0
        )
    }
    val actual = remember(currentPeriod) {
        ActualExpenses(
            actualMandatory = currentPeriod?.actualMandatory ?: 0,
            actualOptional = currentPeriod?.actualOptional ?: 0,
            actualSavings = currentPeriod?.actualSavings ?: 0
        )
    }
    val compliance = remember(plan, actual) {
        PetEconomyCalculator.calculateCompliance(plan, actual)
    }
    val carePointsEarned = remember(plan, actual, compliance) {
        PetEconomyCalculator.calculateCarePoints(
            satiety = 100,
            health = 100,
            plan = plan,
            actual = actual,
            compliance = compliance
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(8.dp))
        ) {
            StardewBoard(
                headerTitle = if (isMonthTransition) "НОВЫЙ МЕСЯЦ" else "НОВЫЙ ДЕНЬ",
                headerIcon = if (isMonthTransition) "🏆" else "🌅"
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isMonthTransition) "Завершить ${monthInfo?.monthName} и перейти на День 1?" else "Перейти на День $nextDay?",
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = PixelTextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Если бюджет был утвержден — показываем карточку «Итоги бюджета дня»
                    if (isConfirmed) {
                        DaySummaryBudgetSection(
                            plan = plan,
                            actual = actual,
                            compliance = compliance,
                            carePoints = carePointsEarned
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(PixelParchmentMedium)
                            .border(1.dp, PixelParchmentBorder, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = if (isMonthTransition) "🏆" else if (isNextMonday) "🎉" else "🐾", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when {
                                        isMonthTransition -> "Завершение месяца ${monthInfo?.monthName ?: ""}"
                                        isNextMonday -> "Понедельник — выплата карманных!"
                                        else -> "Завершение Дня $currentDay"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = PixelTextDark
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when {
                                    isMonthTransition -> "Поздравляем с завершением месяца! Все итоги сохранятся, календарь переключится на новый месяц, а питомец начнет День 1."
                                    isNextMonday -> "Наступает новая неделя! При переходе на День $nextDay тебе будет начислено 200 монет карманных денег."
                                    else -> "Все заметки и траты за сегодня сохранятся в календаре, а питомец перейдет на следующий день."
                                },
                                fontSize = 14.sp,
                                color = PixelTextDark,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PixelButton(
                            text = "✕ Отмена",
                            onClick = onDismiss,
                            containerColor = PixelWoodMedium,
                            textColor = PixelParchmentLight,
                            borderColor = PixelWoodDark,
                            modifier = Modifier.weight(1f),
                            fontSize = 15.sp
                        )
                        PixelButton(
                            text = "🌅 Перейти",
                            onClick = onConfirm,
                            containerColor = PixelGreenCrop,
                            textColor = Color.White,
                            borderColor = Color(0xFF1B5E20),
                            modifier = Modifier.weight(1.3f),
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
