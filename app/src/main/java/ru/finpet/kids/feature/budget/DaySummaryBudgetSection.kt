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
import ru.finpet.kids.core.designsystem.PixelBlueWater
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetCompliance
import ru.finpet.kids.core.domain.model.BudgetPlan

/**
 * блок сводки дня для диалога завершения периода (NextDayConfirmDialog).
 * отображает траты за день, заработанные очки заботы и статус трех звезд бюджета.
 */
@Composable
fun DaySummaryBudgetSection(
    plan: BudgetPlan,
    actual: ActualExpenses,
    compliance: BudgetCompliance,
    carePoints: Int,
    modifier: Modifier = Modifier
) {
    val stars = remember(plan, actual) { calculateBudgetStars(plan, actual) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(PixelParchmentLight)
            .border(1.dp, PixelParchmentBorder, RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ИТОГИ БЮДЖЕТА ДНЯ",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                fontFamily = AppFontFamily,
                color = PixelTextDark
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⭐", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "+$carePoints заботы",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = PixelGoldDark
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // отображение заработанных звезд бюджета
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(PixelParchmentMedium)
                .border(1.dp, PixelParchmentBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Звёзды бюджета: ${stars.count}/3",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PixelTextDark
                )
            }
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${if (stars.careStar) "⭐" else "⚪"} Забота",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (stars.careStar) PixelGreenCrop else PixelTextMuted
                )
                Text(
                    text = "${if (stars.dreamStar) "⭐" else "⚪"} Мечта",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (stars.dreamStar) PixelBlueWater else PixelTextMuted
                )
                Text(
                    text = "${if (stars.wisdomStar) "⭐" else "⚪"} Мудрость",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (stars.wisdomStar) PixelGoldDark else PixelRedBerry
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // разбивка фактических трат по корзинам
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "🍲 Обязательное:",
                    fontSize = 14.sp,
                    color = PixelTextDark
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${actual.actualMandatory} / ${plan.plannedMandatory}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextDark
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    CoinIcon(modifier = Modifier.size(14.dp))
                }
            }
            Column {
                Text(
                    text = "🎈 Желаемое:",
                    fontSize = 14.sp,
                    color = PixelTextDark
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${actual.actualOptional} / ${plan.plannedOptional}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextDark
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    CoinIcon(modifier = Modifier.size(14.dp))
                }
            }
            Column {
                Text(
                    text = "🏦 В копилку:",
                    fontSize = 14.sp,
                    color = PixelTextDark
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${actual.actualSavings} / ${plan.plannedSavings}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextDark
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    CoinIcon(modifier = Modifier.size(14.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // резюме финансовой дисциплины
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (compliance.isDisciplined) Color(0xFFE8F5E9) else Color(0xFFFFF9E6)
                )
                .padding(6.dp)
        ) {
            Text(
                text = "Соответствие плану: ${compliance.totalPercent}%\n${compliance.feedback}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (compliance.isDisciplined) PixelGreenCrop else PixelTextDark,
                lineHeight = 15.sp
            )
        }
    }
}
