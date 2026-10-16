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
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.PixelBlueWater
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGoldGlow
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.StardewBoard
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
        compliance.totalPercent >= 85 -> "🌟"
        compliance.totalPercent >= 60 -> "👍"
        else -> "🌱"
    }

    val complianceBadgeTitle = when {
        compliance.totalPercent >= 85 -> "Отличный баланс! (${compliance.totalPercent}%)"
        compliance.totalPercent >= 60 -> "Хороший результат! (${compliance.totalPercent}%)"
        else -> "План выполнен на ${compliance.totalPercent}%"
    }

    Dialog(
        onDismissRequest = onConfirm,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        KeepScreenImmersive()

        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(8.dp))
        ) {
            StardewBoard(
                headerTitle = "ИТОГИ ДНЯ ${resolution.closedDayIndex}",
                headerIcon = "📊"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Главный баннер процента выполнения
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
                                    fontSize = 15.sp,
                                    color = PixelTextDark
                                )
                            }
                            if (compliance.feedback.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = compliance.feedback,
                                    fontSize = 13.sp,
                                    color = PixelTextDark,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Блок «План vs Факт» по 3 категориям баночек
                    Text(
                        text = "РАЗБОР ТРАТ ЗА ДЕНЬ",
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
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
                            planVal = plan.plannedMandatory,
                            actualVal = actual.actualMandatory,
                            color = PixelRedBerry,
                            isOk = actual.actualMandatory >= plan.plannedMandatory || actual.hasPurchasedFood
                        )
                        // Хотелки (сладости, радости)
                        PlanFactRow(
                            icon = "✨",
                            title = "Хотелки",
                            planVal = plan.plannedOptional,
                            actualVal = actual.actualOptional,
                            color = PixelBlueWater,
                            isOk = actual.actualOptional <= plan.plannedOptional
                        )
                        // Накопления (копилка)
                        PlanFactRow(
                            icon = "🎯",
                            title = "В копилку",
                            planVal = plan.plannedSavings,
                            actualVal = actual.actualSavings,
                            color = PixelGreenCrop,
                            isOk = actual.actualSavings >= plan.plannedSavings
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Блок наград и состояния питомца
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Очки заботы
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
                                Text(
                                    text = "+${resolution.carePointsEarned} ⭐",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = PixelGoldDark
                                )
                                Text(
                                    text = "Очки заботы",
                                    fontSize = 12.sp,
                                    color = PixelTextMuted
                                )
                            }
                        }

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
                                Text(
                                    text = "${newStats.mood}% 🐾",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = PixelGreenCrop
                                )
                                Text(
                                    text = "Настроение",
                                    fontSize = 12.sp,
                                    color = PixelTextMuted
                                )
                            }
                        }
                    }

                    // Начисление карманных денег при понедельнике
                    if (resolution.periodIncome > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(PixelWoodDark)
                                .padding(1.dp)
                                .background(PixelGoldGlow)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "🪙", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Карманные деньги на неделю: +${resolution.periodIncome} монет!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = PixelTextDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Кнопка «Понятно, к новому плану!»
                    PixelButton(
                        text = "Понятно, к новому плану! ➔",
                        onClick = onConfirm,
                        containerColor = PixelGreenCrop,
                        textColor = Color.White,
                        borderColor = Color(0xFF1B5E20),
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanFactRow(
    icon: String,
    title: String,
    planVal: Int,
    actualVal: Int,
    color: Color,
    isOk: Boolean
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = color
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "План: $planVal",
                fontSize = 12.sp,
                color = PixelTextMuted
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Факт: $actualVal",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PixelTextDark
            )
            Spacer(modifier = Modifier.width(4.dp))
            CoinIcon(modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = if (isOk) "✅" else "⚠️", fontSize = 12.sp)
        }
    }
}
