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
 * показывает статус трех корзин в одну строку (еда, радости, копилка),
 * число звезд и процент соблюдения плана. по тапу открывает «план vs факт».
 */
@Composable
fun BudgetPulseWidget(
    plan: BudgetPlan,
    actual: ActualExpenses,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // считаем процент выполнения и заработанные звезды
    val compliance = remember(plan, actual) {
        PetEconomyCalculator.calculateCompliance(plan, actual)
    }
    val stars = remember(plan, actual) {
        calculateBudgetStars(plan, actual)
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
            // верхняя строчка: заголовок, звезды и общий процент
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📊", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "ПУЛЬС БЮДЖЕТА",
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = PixelTextDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⭐ ${stars.count}/3",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = PixelGoldDark
                    )
                }

                // плашка процента дисциплины
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
                        fontSize = 11.sp,
                        fontFamily = AppFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // 3 мини-корзины: еда, радости, копилка
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🍲 ${actual.actualMandatory}/${plan.plannedMandatory}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PixelGreenCrop,
                    fontFamily = AppFontFamily
                )
                Text(text = "•", color = PixelParchmentBorder, fontWeight = FontWeight.Bold)
                Text(
                    text = "🎈 ${actual.actualOptional}/${plan.plannedOptional}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (actual.actualOptional > plan.plannedOptional) PixelRedBerry else PixelGoldDark,
                    fontFamily = AppFontFamily
                )
                Text(text = "•", color = PixelParchmentBorder, fontWeight = FontWeight.Bold)
                Text(
                    text = "🏦 ${actual.actualSavings}/${plan.plannedSavings}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PixelBlueWater,
                    fontFamily = AppFontFamily
                )
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
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = PixelTextMuted
                )
            }
        }
    }
}
