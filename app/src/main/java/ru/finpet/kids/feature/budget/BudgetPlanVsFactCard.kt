package ru.finpet.kids.feature.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ru.finpet.kids.R
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.DiagramIcon
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.LockIcon
import ru.finpet.kids.core.designsystem.PixelAlertDialog
import ru.finpet.kids.core.designsystem.PixelBlueWater
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelJar
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.WarningIcon
import ru.finpet.kids.core.designsystem.pixelButtonBackground
import ru.finpet.kids.core.designsystem.StardewBoard
import kotlin.math.roundToInt
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
    modifier: Modifier = Modifier
) {
    val uiScale = LocalUiScale.current
    StardewBoard(
        headerTitle = "ПЛАН vs ФАКТ",
        headerIcon = "📊"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy((8.dp * uiScale).coerceIn(5.dp, 8.dp))
        ) {
            // верхняя сводная плашка: компактно и последовательно «План: 150 🪙 ➔ Факт: 120 🪙»
            PixelPanel(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = (10.dp * uiScale).coerceIn(6.dp, 12.dp),
                    vertical = (6.dp * uiScale).coerceIn(4.dp, 8.dp)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy((3.dp * uiScale).coerceIn(2.dp, 4.dp))
                        ) {
                            Text(
                                text = "План: ${plan.totalPlanned}",
                                fontWeight = FontWeight.Bold,
                                fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                                color = PixelTextDark,
                                fontFamily = AppFontFamily
                            )
                            CoinIcon(modifier = Modifier.size((15.dp * uiScale).coerceIn(11.dp, 17.dp)))
                            Spacer(modifier = Modifier.width((3.dp * uiScale).coerceIn(2.dp, 4.dp)))
                            Text(
                                text = "➔ Факт: ${actual.totalActual}",
                                fontWeight = FontWeight.Bold,
                                fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                                color = if (actual.totalActual <= plan.totalPlanned) PixelGreenCrop else PixelRedBerry,
                                fontFamily = AppFontFamily
                            )
                            CoinIcon(modifier = Modifier.size((15.dp * uiScale).coerceIn(11.dp, 17.dp)))
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        if (compliance.mandatoryOverspend > 0 && compliance.optionalOverspend > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WarningIcon(modifier = Modifier.size((13.dp * uiScale).coerceIn(10.dp, 14.dp)))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Перерасход: обяз. +${compliance.mandatoryOverspend}, жел. +${compliance.optionalOverspend}",
                                    fontSize = (12f * uiScale).coerceIn(9f, 13f).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PixelRedBerry,
                                    fontFamily = AppFontFamily
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                CoinIcon(modifier = Modifier.size((12.dp * uiScale).coerceIn(9.dp, 12.dp)))
                            }
                        } else if (compliance.mandatoryOverspend > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WarningIcon(modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Перерасход: обяз. +${compliance.mandatoryOverspend}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PixelRedBerry,
                                    fontFamily = AppFontFamily
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                CoinIcon(modifier = Modifier.size(12.dp))
                            }
                        } else if (compliance.optionalOverspend > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WarningIcon(modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Перерасход: жел. +${compliance.optionalOverspend}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PixelRedBerry,
                                    fontFamily = AppFontFamily
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                CoinIcon(modifier = Modifier.size(12.dp))
                            }
                        } else if (actual.borrowedFromOptional > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WarningIcon(modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Взято из «Желаемого»: ${actual.borrowedFromOptional}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PixelRedBerry,
                                    fontFamily = AppFontFamily
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                CoinIcon(modifier = Modifier.size(12.dp))
                            }
                        } else if (actual.withdrawnSavings > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WarningIcon(modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Снято из прошлых накоплений: ${actual.withdrawnSavings}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PixelRedBerry,
                                    fontFamily = AppFontFamily
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                CoinIcon(modifier = Modifier.size(12.dp))
                            }
                        } else if (compliance.isPetHungry) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WarningIcon(modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Питомец голоден! Забота о жизни важнее",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PixelRedBerry,
                                    fontFamily = AppFontFamily
                                )
                            }
                        } else {
                            Text(
                                text = if (compliance.isDisciplined) "Отличная точность: траты под контролем!" else "Держи траты в рамках плана, чтобы поднять процент",
                                fontSize = 13.sp,
                                color = PixelTextMuted,
                                lineHeight = 17.sp,
                                fontFamily = AppFontFamily
                            )
                        }
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
                            fontSize = 15.sp,
                            fontFamily = AppFontFamily
                        )
                    }
                }
            }

            var showAccuracyTipsDialog by remember { mutableStateOf(false) }

            // Кнопка под «План -> Факт»: «Как поднять точность бюджета»
            PixelButton(
                text = "Как поднять точность бюджета",
                onClick = { showAccuracyTipsDialog = true },
                containerColor = PixelGoldBright,
                textColor = PixelTextDark,
                borderColor = PixelGoldDark,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 15.sp,
                leadingIcon = { DiagramIcon(modifier = Modifier.size(18.dp)) }
            )

            if (showAccuracyTipsDialog) {
                PixelAlertDialog(
                    onDismissRequest = { showAccuracyTipsDialog = false },
                    title = "Точность бюджета",
                    icon = "🎯",
                    confirmButton = {
                        PixelButton(
                            text = "Понятно",
                            onClick = { showAccuracyTipsDialog = false },
                            containerColor = PixelGreenCrop,
                            textColor = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Как держать точность бюджета на 100%:\n\n" +
                                    "• Главный приоритет — сытый питомец: здоровье и жизнь котика важнее всего! Если питомец голоден, даже полная копилка не спасёт точность бюджета.\n\n" +
                                    "• Баланс трат: не спускай все монеты на хотелки («Сундучок радости») — чрезмерные траты и перерасход снижают процент.\n\n" +
                                    "• Честные накопления: пополняй копилку только после того, как котик накормлен, и держи покупки в рамках плана.\n\n" +
                                    "• Экономить с умом: неизрасходованные монеты в кошельке не снижают процент, если питомец сыт и здоров!",
                            fontFamily = AppFontFamily,
                            fontSize = 17.sp,
                            color = PixelTextDark,
                            lineHeight = 23.sp
                        )
                    }
                }
            }

            // 3 банки бюджета в одну строчку
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Банка 1: «Миска жизни» (Обязательное)
                // Изначально заполнена (10 делений), опустошается по мере трат
                val mandLeft = maxOf(0, plan.plannedMandatory - actual.actualMandatory)
                val mandRatio = if (plan.plannedMandatory > 0) {
                    (mandLeft.toFloat() / plan.plannedMandatory.toFloat()).coerceIn(0f, 1f)
                } else 0f
                val mandDivisions = (mandRatio * 10f).roundToInt()
                val isMandOverspent = actual.actualMandatory > plan.plannedMandatory

                JarDashboardItem(
                    jarRes = R.drawable.life_jar,
                    fillDivisions = mandDivisions,
                    title = "«Миска жизни»",
                    categoryLabel = "Обязательное",
                    themeColor = PixelGreenCrop,
                    leftAmount = mandLeft,
                    plannedAmount = plan.plannedMandatory,
                    actualAmount = actual.actualMandatory,
                    isOverspent = isMandOverspent,
                    isSavings = false,
                    modifier = Modifier.weight(1f)
                )

                // Банка 2: «Сундучок радости» (Желаемое)
                // Изначально заполнена (10 делений), опустошается по мере трат
                val optLeft = maxOf(0, plan.plannedOptional - actual.actualOptional)
                val optRatio = if (plan.plannedOptional > 0) {
                    (optLeft.toFloat() / plan.plannedOptional.toFloat()).coerceIn(0f, 1f)
                } else 0f
                val optDivisions = (optRatio * 10f).roundToInt()
                val isOptOverspent = actual.actualOptional > plan.plannedOptional

                JarDashboardItem(
                    jarRes = R.drawable.joy_jar,
                    fillDivisions = optDivisions,
                    title = "«Сундучок радости»",
                    categoryLabel = "Желаемое",
                    themeColor = PixelGoldDark,
                    leftAmount = optLeft,
                    plannedAmount = plan.plannedOptional,
                    actualAmount = actual.actualOptional,
                    isOverspent = isOptOverspent,
                    isSavings = false,
                    modifier = Modifier.weight(1f)
                )

                // Банка 3: «Копилка мечты» (В копилку)
                // Изначально пустая (0 делений), заполняется по мере накоплений
                val savRatio = if (plan.plannedSavings > 0) {
                    (actual.actualSavings.toFloat() / plan.plannedSavings.toFloat()).coerceIn(0f, 1f)
                } else if (actual.actualSavings > 0) 1f else 0f
                val savDivisions = (savRatio * 10f).roundToInt()
                val isSavCompleted = actual.actualSavings >= plan.plannedSavings && plan.plannedSavings > 0

                JarDashboardItem(
                    jarRes = R.drawable.saving_jar,
                    fillDivisions = savDivisions,
                    title = "«Копилка мечты»",
                    categoryLabel = "В копилку",
                    themeColor = PixelBlueWater,
                    leftAmount = actual.actualSavings,
                    plannedAmount = plan.plannedSavings,
                    actualAmount = actual.actualSavings,
                    isOverspent = false,
                    isSavings = true,
                    isSavingsCompleted = isSavCompleted,
                    modifier = Modifier.weight(1f)
                )
            }

            // Статус утверждения плана
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pixelButtonBackground(containerColor = Color(0xFFDCEDC8))
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LockIcon(modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "План утверждён",
                        fontFamily = AppFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                }
            }
        }
    }
}

/**
 * элемент карточки баночки бюджета для сводки плана vs факта (в одну строчку)
 */
@Composable
private fun JarDashboardItem(
    jarRes: Int,
    fillDivisions: Int,
    title: String,
    categoryLabel: String,
    themeColor: Color,
    leftAmount: Int,
    plannedAmount: Int,
    actualAmount: Int,
    isOverspent: Boolean,
    isSavings: Boolean,
    isSavingsCompleted: Boolean = false,
    modifier: Modifier = Modifier
) {
    PixelPanel(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = categoryLabel,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = themeColor,
                fontFamily = AppFontFamily,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            PixelJar(
                jarRes = jarRes,
                fillDivisions = fillDivisions,
                modifier = Modifier.size(46.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (isSavings) "В копилке:" else "Осталось:",
                fontSize = 12.sp,
                color = PixelTextMuted,
                fontFamily = AppFontFamily
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$leftAmount из $plannedAmount",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = if (isOverspent) PixelRedBerry else PixelTextDark,
                    fontFamily = AppFontFamily
                )
                Spacer(modifier = Modifier.width(2.dp))
                CoinIcon(modifier = Modifier.size(11.dp))
            }

            Spacer(modifier = Modifier.height(1.dp))

            if (isOverspent) {
                val overspend = actualAmount - plannedAmount
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    WarningIcon(modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "+$overspend",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelRedBerry,
                        fontFamily = AppFontFamily
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    CoinIcon(modifier = Modifier.size(11.dp))
                }
            } else if (isSavings && isSavingsCompleted) {
                Text(
                    text = "✓ Цель!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    fontFamily = AppFontFamily
                )
            } else {
                Text(
                    text = if (fillDivisions > 0) "✓ Норма" else "Пусто",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (fillDivisions > 0) Color(0xFF2E7D32) else PixelTextMuted,
                    fontFamily = AppFontFamily
                )
            }
        }
    }
}

