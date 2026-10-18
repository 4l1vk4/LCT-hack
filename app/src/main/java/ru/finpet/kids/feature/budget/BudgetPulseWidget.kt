package ru.finpet.kids.feature.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.DiagramIcon
import ru.finpet.kids.core.designsystem.WarningIcon
import ru.finpet.kids.core.designsystem.PixelBlueWater
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.PixelWoodBevel
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.domain.calculator.PetEconomyCalculator
import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetPlan

/**
 * компактный виджет «пульс бюджета» для главного экрана (ТЗ 2.5.3, 2.5.5).
 * показывает статус трех корзин в одну строку (еда, радости, копилка)
 * и процент соблюдения плана. по тапу открывает «план vs факт».
 */
@Composable
fun BudgetPulseWidget(
    plan: BudgetPlan,
    actual: ActualExpenses,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    petMood: Int = 70,
    petSatiety: Int = 100,
    petHealth: Int = 100,
    isPetRunaway: Boolean = false
) {
    val compliance = remember(plan, actual, petMood, petSatiety, petHealth, isPetRunaway) {
        PetEconomyCalculator.calculateCompliance(
            plan = plan,
            actual = actual,
            isPetRunaway = isPetRunaway,
            petMood = petMood,
            petSatiety = petSatiety,
            petHealth = petHealth
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PixelWoodDark)
            .padding(2.dp)
            .border(1.5.dp, PixelWoodBevel, RoundedCornerShape(8.dp))
            .background(PixelWoodMedium)
            .padding(2.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(PixelParchmentLight)
                .border(1.dp, PixelParchmentBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            // верхняя строчка: заголовок и общий процент
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DiagramIcon(modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Column {
                        Text(
                            text = "ПУЛЬС БЮДЖЕТА",
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = PixelTextDark
                        )
                        Text(
                            text = "Точность бюджета",
                            fontSize = 11.sp,
                            color = PixelTextMuted
                        )
                    }
                }

                // плашка процента дисциплины / точности
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
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${compliance.totalPercent}%",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        fontFamily = AppFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // 3 мини-корзины: Обязательное, Желаемое, В копилку (слова ПОД монетками)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Обязательное
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${actual.actualMandatory}/${plan.plannedMandatory}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (actual.actualMandatory > plan.plannedMandatory && plan.plannedMandatory > 0) PixelRedBerry else PixelGreenCrop,
                            fontFamily = AppFontFamily
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        CoinIcon(modifier = Modifier.size(13.dp))
                    }
                    Text(
                        text = "Обязательное",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextMuted,
                        fontFamily = AppFontFamily
                    )
                }

                Text(text = "•", color = PixelParchmentBorder, fontWeight = FontWeight.Bold)

                // 2. Желаемое
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${actual.actualOptional}/${plan.plannedOptional}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (actual.actualOptional > plan.plannedOptional && plan.plannedOptional > 0) PixelRedBerry else PixelGoldDark,
                            fontFamily = AppFontFamily
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        CoinIcon(modifier = Modifier.size(13.dp))
                    }
                    Text(
                        text = "Желаемое",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextMuted,
                        fontFamily = AppFontFamily
                    )
                }

                Text(text = "•", color = PixelParchmentBorder, fontWeight = FontWeight.Bold)

                // 3. В копилку
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${actual.actualSavings}/${plan.plannedSavings}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PixelBlueWater,
                            fontFamily = AppFontFamily
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        CoinIcon(modifier = Modifier.size(13.dp))
                    }
                    Text(
                        text = "В копилку",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextMuted,
                        fontFamily = AppFontFamily
                    )
                }
            }

            if (compliance.overspendAmount > 0 || actual.borrowedFromOptional > 0 || actual.withdrawnSavings > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                val overspendText = when {
                    compliance.mandatoryOverspend > 0 && compliance.optionalOverspend > 0 ->
                        "Перерасход: обяз. +${compliance.mandatoryOverspend}, жел. +${compliance.optionalOverspend} снизил точность"
                    compliance.mandatoryOverspend > 0 ->
                        "Перерасход в обязательном (+${compliance.mandatoryOverspend}) снизил точность"
                    compliance.optionalOverspend > 0 ->
                        "Перерасход в желаемом (+${compliance.optionalOverspend}) снизил точность"
                    actual.borrowedFromOptional > 0 ->
                        "Взято из «Желаемого» (+${actual.borrowedFromOptional}) на обязательное"
                    actual.withdrawnSavings > 0 ->
                        "Снято из прошлых накоплений (-${actual.withdrawnSavings})"
                    else -> "Перерасход на ${compliance.overspendAmount} снизил точность"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFFEBEE))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WarningIcon(modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = overspendText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelRedBerry
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // подсказка о возможности нажать
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "План vs Факт ➔",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PixelTextMuted
                )
            }
        }
    }
}
