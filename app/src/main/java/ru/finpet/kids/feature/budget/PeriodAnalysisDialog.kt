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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FinPetTheme
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.LampochkaIcon
import ru.finpet.kids.core.designsystem.PawsIcon
import ru.finpet.kids.core.designsystem.StarIcon
import ru.finpet.kids.core.designsystem.PixelBlueWater
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.RichFeedbackText
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.core.designsystem.TargetIcon
import ru.finpet.kids.core.designsystem.WarningIcon
import ru.finpet.kids.core.domain.calculator.PetEconomyCalculator
import ru.finpet.kids.core.domain.model.GrowthStage
import ru.finpet.kids.core.domain.model.PeriodResolution

/**
 * всплывающий диалог анализа после завершения периода (ТЗ 2.5.5, 2.5.11).
 * подробно показывает процент выполнения плана, разбор план vs факт,
 * заработанные очки заботы и по кнопке «Понятно» сразу ведет к составлению плана на новый день.
 */
@Composable
fun PeriodAnalysisDialog(
    resolution: PeriodResolution,
    onConfirm: () -> Unit
) {
    val compliance = resolution.compliance
    val plan = resolution.plan
    val actual = resolution.actual
    val newStats = resolution.newStats

    val complianceEmoji = when {
        compliance.totalPercent >= 85 -> "🏆"
        compliance.totalPercent >= 60 -> "👍"
        else -> "🌱"
    }

    val complianceBadgeTitle = when {
        compliance.totalPercent >= 85 -> "Отличная точность бюджета! (${compliance.totalPercent}%)"
        compliance.totalPercent >= 60 -> "Хорошая точность бюджета! (${compliance.totalPercent}%)"
        else -> "Точность бюджета: ${compliance.totalPercent}%"
    }

    Dialog(
        onDismissRequest = onConfirm,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FinPetTheme {
            KeepScreenImmersive()

            Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(8.dp))
        ) {
            StardewBoard(
                headerTitle = "ИТОГИ ПЕРИОДА (ДЕНЬ ${resolution.closedDayIndex})",
                headerIcon = "📊"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Сообщение о новом месяце
                    if (resolution.isMonthTransition) {
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
                                    Text(text = "🏆", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (resolution.newMonthName.isNotBlank()) "Новый месяц: ${resolution.newMonthName}!" else "Наступил новый месяц!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = PixelTextDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Поздравляем с завершением месяца! Все заметки и итоги сохранены в календаре, а питомец перешел на День 1.",
                                    fontSize = 16.sp,
                                    color = PixelTextDark,
                                    lineHeight = 21.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 2. Сообщение о дне выплат (понедельник)
                    if (resolution.isIncomeDay || resolution.periodIncome > 0) {
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
                                    Text(
                                        text = "Понедельник — выплата карманных!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = PixelTextDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                RichFeedbackText(
                                    text = "Наступает новая неделя! Тебе начислено +${resolution.periodIncome} 🪙 карманных денег.",
                                    fontSize = 16.sp,
                                    color = PixelTextDark,
                                    lineHeight = 21.sp,
                                    textAlign = TextAlign.Left
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 3. Предупреждение если бюджет не был составлен на этот день
                    val isBudgetUnplanned = plan.plannedMandatory == 0 && plan.plannedOptional == 0 && plan.plannedSavings == 0
                    if (isBudgetUnplanned) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFF3E0))
                                .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                WarningIcon(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "План бюджета не был составлен!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color(0xFFE65100)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Переходить в новый день без распределения бюджета — не очень хорошая практика. Всегда планируй обязательные расходы и накопления заранее, чтобы питомец был сыт и копилка росла!",
                                        fontSize = 16.sp,
                                        color = PixelTextDark,
                                        lineHeight = 21.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 4. Главный баннер соблюдения бюджета (только если план БЫЛ составлен!)
                    if (!isBudgetUnplanned) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (compliance.totalPercent >= 75) Color(0xFFE8F5E9)
                                    else Color(0xFFFFF8E1)
                                )
                                .border(
                                    1.dp,
                                    if (compliance.totalPercent >= 75) PixelGreenCrop else PixelParchmentBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = complianceEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = complianceBadgeTitle,
                                        fontFamily = AppFontFamily,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = PixelTextDark
                                    )
                                }
                                val problemMessages = remember(compliance, actual, plan) {
                                    buildList {
                                        if (compliance.isPetHungry) {
                                            add("Питомец остался голодным (экономия на питании)")
                                        }
                                        if (compliance.mandatoryOverspend > 0) {
                                            add("Перерасход в «Обязательном»: +${compliance.mandatoryOverspend} 🪙")
                                        }
                                        if (compliance.optionalOverspend > 0) {
                                            add("Перерасход в «Желаемом»: +${compliance.optionalOverspend} 🪙")
                                        }
                                        if (compliance.overspendAmount > 0 && compliance.mandatoryOverspend == 0 && compliance.optionalOverspend == 0) {
                                            add("Перерасход на ${compliance.overspendAmount} 🪙")
                                        }
                                        if (actual.borrowedFromOptional > 0) {
                                            add("Взято из «Желаемого» в «Обязательное»: ${actual.borrowedFromOptional} 🪙")
                                        }
                                        if (actual.withdrawnSavings > 0) {
                                            add("Снято из прошлых накоплений: ${actual.withdrawnSavings} 🪙")
                                        }
                                        if (plan.plannedSavings > 0 && actual.actualSavings < plan.plannedSavings) {
                                            val diff = plan.plannedSavings - actual.actualSavings
                                            val savingsText = if (actual.actualSavings == 0) {
                                                "Копилка мечты не пополнена (план: ${plan.plannedSavings} 🪙)"
                                            } else {
                                                "Недобор в копилку: недоложено $diff 🪙 (план: ${plan.plannedSavings} 🪙)"
                                            }
                                            add(savingsText)
                                        }
                                    }
                                }

                                if (problemMessages.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFFEBEE))
                                            .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        for (msg in problemMessages) {
                                            BudgetProblemRow(text = msg)
                                        }
                                    }
                                }
                                if (compliance.feedback.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    RichFeedbackText(
                                        text = compliance.feedback,
                                        fontSize = 16.sp,
                                        color = PixelTextDark,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 21.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 5. Блок «Разбор трат за период»
                    Text(
                        text = "РАЗБОР ТРАТ ЗА ПЕРИОД",
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PixelTextMuted,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(PixelParchmentMedium)
                            .border(1.dp, PixelParchmentBorder, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Обязательные расходы (еда, уход)
                        PlanFactRow(
                            icon = "🍲",
                            title = "Обязательное",
                            planVal = if (!isBudgetUnplanned) plan.plannedMandatory else null,
                            actualVal = actual.actualMandatory,
                            color = PixelRedBerry,
                            isOk = if (!isBudgetUnplanned) (actual.actualMandatory >= plan.plannedMandatory || actual.hasPurchasedFood) else null,
                            actualPrefix = if (isBudgetUnplanned) "Потрачено: " else "Факт: "
                        )
                        // Желаемое (сладости, радости)
                        PlanFactRow(
                            icon = "🎈",
                            title = "Желаемое",
                            planVal = if (!isBudgetUnplanned) plan.plannedOptional else null,
                            actualVal = actual.actualOptional,
                            color = PixelBlueWater,
                            isOk = if (!isBudgetUnplanned) (actual.actualOptional <= plan.plannedOptional) else null,
                            actualPrefix = if (isBudgetUnplanned) "Потрачено: " else "Факт: "
                        )
                        // Накопления (копилка)
                        PlanFactRow(
                            icon = "🎯",
                            title = "В копилку",
                            planVal = if (!isBudgetUnplanned) plan.plannedSavings else null,
                            actualVal = actual.actualSavings,
                            color = PixelGreenCrop,
                            isOk = if (!isBudgetUnplanned) (actual.actualSavings >= plan.plannedSavings) else null,
                            actualPrefix = if (isBudgetUnplanned) "Отложено: " else "Факт: "
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Блок наград и эволюции питомца
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Настроение питомца
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(PixelParchmentLight)
                                .border(1.dp, PixelParchmentBorder, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val effectiveMood = PetEconomyCalculator.calculateEffectiveMood(
                                    mood = newStats.mood,
                                    satiety = newStats.satiety,
                                    health = newStats.health
                                )
                                val moodColor = when {
                                    effectiveMood >= 70 -> PixelGreenCrop
                                    effectiveMood >= 40 -> PixelGoldDark
                                    else -> PixelRedBerry
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$effectiveMood%",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        color = moodColor
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    PawsIcon(modifier = Modifier.size(15.dp))
                                }
                                Text(
                                    text = "Настроение",
                                    fontSize = 14.sp,
                                    color = PixelTextMuted
                                )
                            }
                        }

                        // Очки заботы за период
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(PixelParchmentLight)
                                .border(1.dp, PixelParchmentBorder, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "+${resolution.carePointsEarned}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        color = if (resolution.carePointsEarned > 0) PixelGoldDark else PixelTextMuted
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    StarIcon(modifier = Modifier.size(15.dp))
                                }
                                Text(
                                    text = "Очки заботы",
                                    fontSize = 14.sp,
                                    color = PixelTextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Карточка прогресса эволюции
                    val stage = newStats.stage
                    val stageTitle = when (stage) {
                        GrowthStage.BABY -> "Малыш 🐾"
                        GrowthStage.TEEN -> "Подросток 🐾"
                        GrowthStage.ADULT -> "Взрослый кот 👑"
                    }
                    val targetPoints = when (stage) {
                        GrowthStage.BABY -> GrowthStage.TEEN.minPoints
                        GrowthStage.TEEN -> GrowthStage.ADULT.minPoints
                        GrowthStage.ADULT -> GrowthStage.ADULT.minPoints
                    }
                    val progressFraction = when (stage) {
                        GrowthStage.BABY -> (newStats.carePoints.toFloat() / GrowthStage.TEEN.minPoints.toFloat()).coerceIn(0f, 1f)
                        GrowthStage.TEEN -> (newStats.carePoints.toFloat() / GrowthStage.ADULT.minPoints.toFloat()).coerceIn(0f, 1f)
                        GrowthStage.ADULT -> 1f
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StarIcon(modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Стадия: $stageTitle",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = PixelTextDark
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (stage == GrowthStage.ADULT) "${newStats.carePoints}" else "${newStats.carePoints} / $targetPoints",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = PixelGoldDark
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    StarIcon(modifier = Modifier.size(14.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Прогресс-бар
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFD7CCC8))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progressFraction)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (stage == GrowthStage.ADULT) PixelGreenCrop else PixelGoldDark)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val remainingMsg = when (stage) {
                                GrowthStage.BABY -> "До «Подростка» осталось: ${maxOf(0, GrowthStage.TEEN.minPoints - newStats.carePoints)} очков (порог ${GrowthStage.TEEN.minPoints})"
                                GrowthStage.TEEN -> "До «Взрослого» осталось: ${maxOf(0, GrowthStage.ADULT.minPoints - newStats.carePoints)} очков (порог ${GrowthStage.ADULT.minPoints})"
                                GrowthStage.ADULT -> "Максимальная стадия роста достигнута!"
                            }
                            Text(
                                text = remainingMsg,
                                fontSize = 13.sp,
                                color = PixelTextMuted
                            )

                            if (resolution.carePointsEarned == 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.Top) {
                                    LampochkaIcon(modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Чтобы получать очки заботы: накорми питомца (сытость ≥60%, здоровье ≥80%), пополни копилку и держи точность бюджета от 75%.",
                                        fontSize = 13.sp,
                                        color = Color(0xFFC62828),
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Кнопка «Понятно, к новому плану!»
                    PixelButton(
                        text = "Понятно, к новому плану!",
                        onClick = onConfirm,
                        containerColor = PixelGreenCrop,
                        textColor = Color.White,
                        borderColor = Color(0xFF1B5E20),
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun PlanFactRow(
    icon: String,
    title: String,
    planVal: Int? = null,
    actualVal: Int,
    color: Color,
    isOk: Boolean? = null,
    actualPrefix: String = "Факт: "
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(PixelParchmentLight)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon == "🎯") {
                    TargetIcon(modifier = Modifier.size(16.dp))
                } else {
                    Text(text = icon, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                if (planVal != null) {
                    Text(
                        text = "$actualVal / $planVal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextDark
                    )
                } else {
                    Text(
                        text = "$actualPrefix$actualVal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextDark
                    )
                }
                Spacer(modifier = Modifier.width(3.dp))
                CoinIcon(modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title, // "Обязательное", "Желаемое", "В копилку" ПОД монетками
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = color
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isOk != null) {
                if (isOk) {
                    Text(text = "✅", fontSize = 14.sp)
                } else {
                    WarningIcon(modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun BudgetProblemRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        WarningIcon(modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        RichFeedbackText(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = PixelRedBerry,
            fontFamily = AppFontFamily,
            textAlign = TextAlign.Start,
            lineHeight = 17.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

