package ru.finpet.kids.feature.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.CoinText
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
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
import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.pixelButtonBackground
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetCompliance
import ru.finpet.kids.core.domain.model.BudgetPlan

/**
 * карточка детального сравнения «план vs факт» по категориям (ТЗ 2.5.5).
 * отображает шкалу соблюдения плана по корзинам.
 */
@Composable
fun BudgetPlanVsFactCard(
    currentDay: Int,
    plan: BudgetPlan,
    actual: ActualExpenses,
    compliance: BudgetCompliance,
    onEditPlan: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    StardewBoard(
        headerTitle = "ПЛАН vs ФАКТ • ДЕНЬ $currentDay",
        headerIcon = "📊"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // верхняя сводная плашка: компактно и последовательно «План: 150 🪙 ➔ Факт: 120 🪙»
            PixelPanel(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "План:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PixelTextDark,
                                fontFamily = AppFontFamily
                            )
                            CoinText(
                                prefix = "${plan.totalPlanned}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PixelTextDark,
                                fontFamily = AppFontFamily
                            )
                            Text(
                                text = "➔ Факт:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PixelTextDark,
                                fontFamily = AppFontFamily
                            )
                            CoinText(
                                prefix = "${actual.totalActual}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (actual.totalActual <= plan.totalPlanned) PixelGreenCrop else PixelRedBerry,
                                fontFamily = AppFontFamily
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (compliance.isDisciplined) "🏆 Отличная дисциплина трат!" else "💡 Следи за покупками в Лавке",
                            fontSize = 13.sp,
                            color = PixelTextMuted
                        )
                    }

                    // бейдж общего процента соблюдения плана
                    Box(
                        modifier = Modifier
                            .pixelButtonBackground(
                                containerColor = when {
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
            }

            // блок расшифровки процентов для ребенка: экономия не снижает процент!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pixelButtonBackground(containerColor = Color(0xFFDCEDC8))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎯", fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Точность бюджета (${compliance.totalPercent}%): совпадение трат с планом в баночках. Экономия монеток и пополнение копилки не снижают процент!",
                        fontFamily = AppFontFamily,
                        fontSize = 13.sp,
                        color = Color(0xFF1B5E20),
                        lineHeight = 16.sp
                    )
                }
            }

            // корзина 1: обязательные траты (еда, уход)
            CategoryComparisonRow(
                icon = "🍲",
                title = "«Миска жизни»",
                categoryLabel = "Обязательное",
                planned = plan.plannedMandatory,
                actual = actual.actualMandatory,
                color = PixelGreenCrop,
                isCompleted = actual.actualMandatory >= plan.plannedMandatory && plan.plannedMandatory > 0
            )

            // корзина 2: спонтанные траты (желаемое)
            CategoryComparisonRow(
                icon = "🎈",
                title = "«Сундучок радости»",
                categoryLabel = "Желаемое",
                planned = plan.plannedOptional,
                actual = actual.actualOptional,
                color = PixelGoldDark,
                isOverspent = actual.actualOptional > plan.plannedOptional
            )

            // корзина 3: накопления в копилку
            CategoryComparisonRow(
                icon = "🏦",
                title = "«Копилка мечты»",
                categoryLabel = "В копилку",
                planned = plan.plannedSavings,
                actual = actual.actualSavings,
                color = PixelBlueWater,
                isCompleted = actual.actualSavings >= plan.plannedSavings && plan.plannedSavings > 0
            )

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
                if (onEditPlan != null) {
                    PixelButton(
                        text = "✏️ Изменить план",
                        onClick = onEditPlan,
                        containerColor = PixelParchmentMedium,
                        textColor = PixelTextDark,
                        borderColor = PixelParchmentBorder,
                        modifier = Modifier.weight(if (onDismiss != null) 1.5f else 1f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(if (onDismiss != null) 1.5f else 1f)
                            .pixelButtonBackground(containerColor = Color(0xFFDCEDC8))
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔒 План утверждён",
                            fontFamily = AppFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
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
    onEditPlan: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        KeepScreenImmersive()
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
 * строка сопоставления плана и факта по категории с индикатором наполнения
 */
@Composable
private fun CategoryComparisonRow(
    icon: String,
    title: String,
    categoryLabel: String,
    planned: Int,
    actual: Int,
    color: Color,
    isCompleted: Boolean = false,
    isOverspent: Boolean = false
) {
    val progress = if (planned > 0) (actual.toFloat() / planned.toFloat()).coerceIn(0f, 1f) else if (actual > 0) 1f else 0f

    PixelPanel(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PixelTextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$actual / $planned",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = if (isOverspent) PixelRedBerry else color,
                    fontFamily = AppFontFamily
                )
                CoinIcon(modifier = Modifier.size(16.dp))

                Box(
                    modifier = Modifier
                        .pixelButtonBackground(containerColor = color.copy(alpha = 0.35f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = categoryLabel,
                        fontSize = 12.sp,
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // шкала выполнения категории (без зеленых точек на конце)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(PixelParchmentBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isOverspent) PixelRedBerry else color)
                )
            }
        }
    }
}
