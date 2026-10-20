package ru.finpet.kids.feature.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.local.entity.PurchaseEntity
import ru.finpet.kids.core.data.repository.PurchaseItem
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.DiagramIcon
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.LockIcon
import ru.finpet.kids.core.designsystem.MiskaIcon
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
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.core.designsystem.TargetIcon
import ru.finpet.kids.core.designsystem.ToyIcon
import ru.finpet.kids.core.designsystem.WarningIcon
import ru.finpet.kids.core.designsystem.pixelButtonBackground
import kotlin.math.roundToInt
import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetCompliance
import ru.finpet.kids.core.domain.model.BudgetPlan

/**
 * Карточка детального сравнения «план vs факт» по категориям (ТЗ 2.5.5).
 * Отображает шкалу соблюдения плана по корзинам и открывает лог трат при нажатии на банку.
 */
@Composable
fun BudgetPlanVsFactCard(
    currentDay: Int,
    plan: BudgetPlan,
    actual: ActualExpenses,
    compliance: BudgetCompliance,
    modifier: Modifier = Modifier,
    profile: ProfileEntity? = null,
    periodPurchases: List<PurchaseEntity> = emptyList(),
    catalogPurchases: List<PurchaseItem> = emptyList(),
    goals: List<GoalEntity> = emptyList()
) {
    val uiScale = LocalUiScale.current
    var selectedJarCategory by remember { mutableStateOf<String?>(null) }

    StardewBoard(
        headerTitle = "ПЛАН vs ФАКТ",
        headerIcon = "📊"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy((8.dp * uiScale).coerceIn(5.dp, 8.dp))
        ) {
            // Верхняя сводная плашка: нейтральный цвет «План: 150 🪙 ➔ Факт: 120 🪙» без раскрашивания факта
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
                                color = PixelTextDark,
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
                            }
                        } else if (compliance.mandatoryOverspend > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WarningIcon(modifier = Modifier.size((13.dp * uiScale).coerceIn(10.dp, 14.dp)))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Перерасход на обязательное: +${compliance.mandatoryOverspend}",
                                    fontSize = (12f * uiScale).coerceIn(9f, 13f).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PixelRedBerry,
                                    fontFamily = AppFontFamily
                                )
                            }
                        } else if (compliance.optionalOverspend > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WarningIcon(modifier = Modifier.size((13.dp * uiScale).coerceIn(10.dp, 14.dp)))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Перерасход на желаемое: +${compliance.optionalOverspend}",
                                    fontSize = (12f * uiScale).coerceIn(9f, 13f).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PixelRedBerry,
                                    fontFamily = AppFontFamily
                                )
                            }
                        } else if (actual.borrowedFromOptional > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WarningIcon(modifier = Modifier.size((13.dp * uiScale).coerceIn(10.dp, 14.dp)))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Взято из «Желаемого»: ${actual.borrowedFromOptional}",
                                    fontSize = (12f * uiScale).coerceIn(9f, 13f).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JoyOrange,
                                    fontFamily = AppFontFamily
                                )
                            }
                        } else if (actual.withdrawnSavings > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WarningIcon(modifier = Modifier.size((13.dp * uiScale).coerceIn(10.dp, 14.dp)))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Снято из прошлых накоплений: ${actual.withdrawnSavings}",
                                    fontSize = (12f * uiScale).coerceIn(9f, 13f).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PixelRedBerry,
                                    fontFamily = AppFontFamily
                                )
                            }
                        } else {
                            Text(
                                text = "Все траты в рамках плана",
                                fontSize = (12f * uiScale).coerceIn(9f, 13f).sp,
                                color = PixelTextMuted,
                                fontFamily = AppFontFamily
                            )
                        }
                    }

                    // Бейдж соблюдения бюджета
                    var showHelpDialog by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    compliance.totalPercent >= 75 -> Color(0xFFE8F5E9)
                                    compliance.totalPercent >= 50 -> Color(0xFFFFF8E1)
                                    else -> Color(0xFFFFEBEE)
                                }
                            )
                            .border(
                                1.dp,
                                when {
                                    compliance.totalPercent >= 75 -> PixelGreenCrop
                                    compliance.totalPercent >= 50 -> PixelGoldDark
                                    else -> PixelRedBerry
                                },
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { showHelpDialog = true }
                            .padding(
                                horizontal = (8.dp * uiScale).coerceIn(6.dp, 10.dp),
                                vertical = (4.dp * uiScale).coerceIn(3.dp, 6.dp)
                            )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${compliance.totalPercent}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = (14f * uiScale).coerceIn(10f, 15f).sp,
                                color = when {
                                    compliance.totalPercent >= 75 -> PixelGreenCrop
                                    compliance.totalPercent >= 50 -> PixelGoldDark
                                    else -> PixelRedBerry
                                },
                                fontFamily = AppFontFamily
                            )
                            Spacer(modifier = Modifier.width((3.dp * uiScale).coerceIn(2.dp, 4.dp)))
                            Text(
                                text = "ℹ️",
                                fontSize = (11f * uiScale).coerceIn(9f, 13f).sp
                            )
                        }
                    }

                    if (showHelpDialog) {
                        BudgetHelpDialog(onDismiss = { showHelpDialog = false })
                    }
                }
            }

            // 3 баночки бюджета в один ряд: Обязательное, Желаемое, Копилка
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((6.dp * uiScale).coerceIn(3.dp, 6.dp))
            ) {
                // Банка 1: «Миска жизни» (Обязательное)
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
                    onClick = { selectedJarCategory = "MANDATORY" },
                    modifier = Modifier.weight(1f)
                )

                // Банка 2: «Сундучок радости» (Желаемое)
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
                    onClick = { selectedJarCategory = "OPTIONAL" },
                    modifier = Modifier.weight(1f)
                )

                // Банка 3: «Копилка мечты» (В копилку)
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
                    onClick = { selectedJarCategory = "SAVINGS" },
                    modifier = Modifier.weight(1f)
                )
            }

            // Подсказка о кликабельности банок
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = (2.dp * uiScale).coerceIn(1.dp, 3.dp)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Нажми на банку, чтобы посмотреть лог трат",
                    fontFamily = AppFontFamily,
                    fontSize = (12f * uiScale).coerceIn(10f, 13f).sp,
                    color = PixelTextMuted,
                    textAlign = TextAlign.Center
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

    // Диалог лога трат выбранной банки и влияния на кота
    if (selectedJarCategory != null) {
        JarExpenseLogDialog(
            category = selectedJarCategory!!,
            plan = plan,
            actual = actual,
            compliance = compliance,
            profile = profile,
            periodPurchases = periodPurchases,
            catalogPurchases = catalogPurchases,
            goals = goals,
            onDismiss = { selectedJarCategory = null }
        )
    }
}

/**
 * Элемент карточки баночки бюджета для сводки плана vs факта
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
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiScale = LocalUiScale.current
    PixelPanel(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClick() }
                } else Modifier
            ),
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
                fillColor = themeColor,
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
                    color = if (isOverspent) themeColor else PixelTextDark,
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
                        color = themeColor,
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

/**
 * Диалог детального лога трат по выбранной банке бюджета и описания влияния на питомца
 */
@Composable
private fun JarExpenseLogDialog(
    category: String,
    plan: BudgetPlan,
    actual: ActualExpenses,
    compliance: BudgetCompliance,
    profile: ProfileEntity?,
    periodPurchases: List<PurchaseEntity>,
    catalogPurchases: List<PurchaseItem>,
    goals: List<GoalEntity>,
    onDismiss: () -> Unit
) {
    val uiScale = LocalUiScale.current
    val title = when (category) {
        "MANDATORY" -> "«Миска жизни»"
        "OPTIONAL" -> "«Сундучок радости»"
        else -> "«Копилка мечты»"
    }
    val subtitle = when (category) {
        "MANDATORY" -> "Обязательные расходы • Еда, вода и здоровье"
        "OPTIONAL" -> "Желаемые расходы • Игрушки, лакомства и радости"
        else -> "Накопления на мечту • Достижение больших целей"
    }
    val jarRes = when (category) {
        "MANDATORY" -> R.drawable.life_jar
        "OPTIONAL" -> R.drawable.joy_jar
        else -> R.drawable.saving_jar
    }
    val themeColor = when (category) {
        "MANDATORY" -> PixelGreenCrop
        "OPTIONAL" -> PixelGoldDark
        else -> PixelBlueWater
    }

    PixelAlertDialog(
        onDismissRequest = onDismiss,
        title = title,
        icon = jarRes,
        confirmButton = {
            PixelButton(
                text = "Понятно",
                onClick = onDismiss,
                containerColor = themeColor,
                textColor = Color.White,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy((10.dp * uiScale).coerceIn(6.dp, 12.dp))
        ) {
            // Подзаголовок
            Text(
                text = subtitle,
                fontFamily = AppFontFamily,
                fontSize = (13f * uiScale).coerceIn(10f, 14f).sp,
                color = PixelTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // 1. Финансовая сводка банки
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PixelParchmentLight)
                    .border(1.5.dp, PixelParchmentBorder, RoundedCornerShape(8.dp))
                    .padding((10.dp * uiScale).coerceIn(6.dp, 12.dp))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    when (category) {
                        "MANDATORY" -> {
                            val mandLeft = maxOf(0, plan.plannedMandatory - actual.actualMandatory)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Запланировано:", fontFamily = AppFontFamily, fontSize = 13.sp, color = PixelTextDark)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${plan.plannedMandatory}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PixelTextDark)
                                    Spacer(Modifier.width(2.dp))
                                    CoinIcon(Modifier.size(13.dp))
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Потрачено (факт):", fontFamily = AppFontFamily, fontSize = 13.sp, color = PixelTextDark)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${actual.actualMandatory}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PixelTextDark)
                                    Spacer(Modifier.width(2.dp))
                                    CoinIcon(Modifier.size(13.dp))
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Осталось в банке:", fontFamily = AppFontFamily, fontSize = 13.sp, color = PixelTextDark)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("$mandLeft", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (actual.actualMandatory > plan.plannedMandatory) PixelRedBerry else PixelGreenCrop)
                                    Spacer(Modifier.width(2.dp))
                                    CoinIcon(Modifier.size(13.dp))
                                }
                            }
                            if (actual.borrowedFromOptional > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Взято из «Желаемого»:", fontFamily = AppFontFamily, fontSize = 12.sp, color = JoyOrange, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${actual.borrowedFromOptional}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = JoyOrange)
                                        Spacer(Modifier.width(2.dp))
                                        CoinIcon(Modifier.size(12.dp))
                                    }
                                }
                            }
                            if (actual.withdrawnSavings > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Взято из накоплений:", fontFamily = AppFontFamily, fontSize = 12.sp, color = PixelBlueWater, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${actual.withdrawnSavings}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PixelBlueWater)
                                        Spacer(Modifier.width(2.dp))
                                        CoinIcon(Modifier.size(12.dp))
                                    }
                                }
                            }
                            if (compliance.mandatoryOverspend > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Перерасход сверх плана:", fontFamily = AppFontFamily, fontSize = 12.sp, color = PixelRedBerry, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("+${compliance.mandatoryOverspend}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PixelRedBerry)
                                        Spacer(Modifier.width(2.dp))
                                        CoinIcon(Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                        "OPTIONAL" -> {
                            val optLeft = maxOf(0, plan.plannedOptional - actual.actualOptional)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Запланировано:", fontFamily = AppFontFamily, fontSize = 13.sp, color = PixelTextDark)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${plan.plannedOptional}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PixelTextDark)
                                    Spacer(Modifier.width(2.dp))
                                    CoinIcon(Modifier.size(13.dp))
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Потрачено (факт):", fontFamily = AppFontFamily, fontSize = 13.sp, color = PixelTextDark)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${actual.actualOptional}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PixelTextDark)
                                    Spacer(Modifier.width(2.dp))
                                    CoinIcon(Modifier.size(13.dp))
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Осталось в банке:", fontFamily = AppFontFamily, fontSize = 13.sp, color = PixelTextDark)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("$optLeft", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (actual.actualOptional > plan.plannedOptional) PixelRedBerry else PixelGoldDark)
                                    Spacer(Modifier.width(2.dp))
                                    CoinIcon(Modifier.size(13.dp))
                                }
                            }
                            if (actual.borrowedFromOptional > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Взято на обязательное:", fontFamily = AppFontFamily, fontSize = 12.sp, color = JoyOrange, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${actual.borrowedFromOptional}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = JoyOrange)
                                        Spacer(Modifier.width(2.dp))
                                        CoinIcon(Modifier.size(12.dp))
                                    }
                                }
                            }
                            if (compliance.optionalOverspend > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Перерасход сверх плана:", fontFamily = AppFontFamily, fontSize = 12.sp, color = PixelRedBerry, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("+${compliance.optionalOverspend}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PixelRedBerry)
                                        Spacer(Modifier.width(2.dp))
                                        CoinIcon(Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                        else -> {
                            val activeGoal = goals.find { it.id == profile?.activeGoalId } ?: goals.firstOrNull()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("План накоплений:", fontFamily = AppFontFamily, fontSize = 13.sp, color = PixelTextDark)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${plan.plannedSavings}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PixelTextDark)
                                    Spacer(Modifier.width(2.dp))
                                    CoinIcon(Modifier.size(13.dp))
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Отложено в периоде:", fontFamily = AppFontFamily, fontSize = 13.sp, color = PixelTextDark)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${actual.actualSavings}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PixelGreenCrop)
                                    Spacer(Modifier.width(2.dp))
                                    CoinIcon(Modifier.size(13.dp))
                                }
                            }
                            if (activeGoal != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Цель «${activeGoal.title}»:", fontFamily = AppFontFamily, fontSize = 13.sp, color = PixelTextDark)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${activeGoal.savedAmount}/${activeGoal.targetCost}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PixelBlueWater)
                                        Spacer(Modifier.width(2.dp))
                                        CoinIcon(Modifier.size(13.dp))
                                    }
                                }
                            }
                            if (actual.withdrawnSavings > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Снято на срочные нужды:", fontFamily = AppFontFamily, fontSize = 12.sp, color = PixelRedBerry, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("-${actual.withdrawnSavings}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PixelRedBerry)
                                        Spacer(Modifier.width(2.dp))
                                        CoinIcon(Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Лог покупок/операций
            Text(
                text = if (category == "SAVINGS") "Движение накоплений:" else "Лог трат в этом периоде:",
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (14f * uiScale).coerceIn(11f, 15f).sp,
                color = PixelTextDark
            )

            if (category == "SAVINGS") {
                val activeGoal = goals.find { it.id == profile?.activeGoalId } ?: goals.firstOrNull()
                if (actual.actualSavings == 0 && actual.withdrawnSavings == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF5EFEB))
                            .padding((10.dp * uiScale).coerceIn(6.dp, 10.dp))
                    ) {
                        Text(
                            text = "В этом периоде монеты в копилку ещё не откладывались. Загляни в домик, чтобы пополнить цель на мечту! Если котику срочно понадобятся монеты на необходимое, их можно будет взять из копилки.",
                            fontFamily = AppFontFamily,
                            fontSize = 12.5.sp,
                            color = PixelTextMuted,
                            lineHeight = 16.sp
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (actual.actualSavings > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(6.dp))
                                    .padding((8.dp * uiScale).coerceIn(6.dp, 10.dp))
                            ) {
                                Column {
                                    Text(
                                        text = "Пополнение «${activeGoal?.title ?: "Мечта котика"}»",
                                        fontFamily = AppFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1B5E20)
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "+${actual.actualSavings}",
                                            fontFamily = AppFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = PixelGreenCrop
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        CoinIcon(Modifier.size(12.dp))
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "Влияние на кота: Мечта становится ближе! Учит котика терпению, ускоряет взросление и открывает новые улучшения.",
                                        fontFamily = AppFontFamily,
                                        fontSize = 11.5.sp,
                                        color = PixelTextDark,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                        if (actual.withdrawnSavings > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFFEBEE))
                                    .border(1.dp, Color(0xFFEF9A9A), RoundedCornerShape(6.dp))
                                    .padding((8.dp * uiScale).coerceIn(6.dp, 10.dp))
                            ) {
                                Column {
                                    Text(
                                        text = "Снято из прошлых накоплений",
                                        fontFamily = AppFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PixelRedBerry
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "-${actual.withdrawnSavings}",
                                            fontFamily = AppFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = PixelRedBerry
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        CoinIcon(Modifier.size(12.dp))
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "Влияние на кота: Мечта немного отдалилась, но накопленные монетки выручили котика в трудный момент!",
                                        fontFamily = AppFontFamily,
                                        fontSize = 11.5.sp,
                                        color = PixelTextDark,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                val categoryItems = periodPurchases.filter { it.category == category }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Информационные карточки перелива денег между банками
                    if (category == "MANDATORY" && actual.borrowedFromOptional > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFF3E0))
                                .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(6.dp))
                                .padding((8.dp * uiScale).coerceIn(6.dp, 10.dp))
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ToyIcon(modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Взято из банки «Желаемое»",
                                            fontFamily = AppFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFFE65100)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("+${actual.borrowedFromOptional}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JoyOrange)
                                        Spacer(Modifier.width(2.dp))
                                        CoinIcon(Modifier.size(12.dp))
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "В банке «Обязательное» не хватало монет на еду котику, поэтому ${actual.borrowedFromOptional} монет было взято из радостей питомца.",
                                    fontFamily = AppFontFamily,
                                    fontSize = 11.5.sp,
                                    color = PixelTextDark,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    if (category == "MANDATORY" && actual.withdrawnSavings > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE1F5FE))
                                .border(1.dp, Color(0xFF81D4FA), RoundedCornerShape(6.dp))
                                .padding((8.dp * uiScale).coerceIn(6.dp, 10.dp))
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        TargetIcon(modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Взято из «Копилки мечты»",
                                            fontFamily = AppFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF0277BD)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("+${actual.withdrawnSavings}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PixelBlueWater)
                                        Spacer(Modifier.width(2.dp))
                                        CoinIcon(Modifier.size(12.dp))
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "На покрытие обязательных нужд котика были сняты монеты из прошлых накоплений.",
                                    fontFamily = AppFontFamily,
                                    fontSize = 11.5.sp,
                                    color = PixelTextDark,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    if (category == "OPTIONAL" && actual.borrowedFromOptional > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFF3E0))
                                .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(6.dp))
                                .padding((8.dp * uiScale).coerceIn(6.dp, 10.dp))
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        MiskaIcon(modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Передано в «Обязательное»",
                                            fontFamily = AppFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFFE65100)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("-${actual.borrowedFromOptional}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JoyOrange)
                                        Spacer(Modifier.width(2.dp))
                                        CoinIcon(Modifier.size(12.dp))
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Часть монет из радостей котика пошла на покупку корма и спасение питомца от голода.",
                                    fontFamily = AppFontFamily,
                                    fontSize = 11.5.sp,
                                    color = PixelTextDark,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    if (categoryItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF5EFEB))
                                .padding((10.dp * uiScale).coerceIn(6.dp, 10.dp))
                        ) {
                            Text(
                                text = if (category == "MANDATORY") {
                                    "В этом периоде покупок из банки «Обязательное» ещё не было. Не забудь купить котику корм и воду в магазине!"
                                } else {
                                    "Покупок из банки «Желаемое» ещё не было. Монетки бережно лежат в сундучке!"
                                },
                                fontFamily = AppFontFamily,
                                fontSize = 12.5.sp,
                                color = PixelTextMuted,
                                lineHeight = 16.sp
                            )
                        }
                    } else {
                        categoryItems.forEach { purchase ->
                            val cat = catalogPurchases.find { it.id == purchase.itemId }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PixelParchmentLight)
                                    .border(1.dp, PixelParchmentBorder, RoundedCornerShape(6.dp))
                                    .padding((8.dp * uiScale).coerceIn(6.dp, 10.dp))
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = purchase.itemName,
                                            fontFamily = AppFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = PixelTextDark
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("-${purchase.price}", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PixelGoldDark)
                                            Spacer(Modifier.width(2.dp))
                                            CoinIcon(Modifier.size(12.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))

                                    val impactText = if (category == "MANDATORY") {
                                        val satietyText = if (purchase.newStat > 0) {
                                            "Сытость: было ${purchase.prevStat}% ➔ стало ${purchase.newStat}%"
                                        } else if ((cat?.satietyBonus ?: 0) > 0) {
                                            "Сытость: увеличена на +${cat!!.satietyBonus}%"
                                        } else {
                                            "Поддерживает жизнь котика и спасает от голода"
                                        }
                                        if ((cat?.healthBonus ?: 0) > 0) {
                                            "$satietyText • Здоровье +${cat!!.healthBonus}%"
                                        } else satietyText
                                    } else {
                                        if (purchase.newStat > 0) {
                                            "Настроение: было ${purchase.prevStat}% ➔ стало ${purchase.newStat}%"
                                        } else if ((cat?.moodBonus ?: 0) > 0) {
                                            "Настроение: увеличено на +${cat!!.moodBonus}%"
                                        } else {
                                            "Радует котика и прогоняет грусть"
                                        }
                                    }

                                    Text(
                                        text = "Влияние на кота: $impactText",
                                        fontFamily = AppFontFamily,
                                        fontSize = 11.5.sp,
                                        color = if (category == "MANDATORY") PixelGreenCrop else Color(0xFFB78103),
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Блок «Как эта банка влияет на кота в целом»
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (category) {
                            "MANDATORY" -> Color(0xFFE8F5E9)
                            "OPTIONAL" -> Color(0xFFFFF8E1)
                            else -> Color(0xFFE1F5FE)
                        }
                    )
                    .border(
                        1.dp,
                        when (category) {
                            "MANDATORY" -> Color(0xFFA5D6A7)
                            "OPTIONAL" -> Color(0xFFFFE082)
                            else -> Color(0xFF81D4FA)
                        },
                        RoundedCornerShape(8.dp)
                    )
                    .padding((10.dp * uiScale).coerceIn(8.dp, 12.dp))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        when (category) {
                            "MANDATORY" -> MiskaIcon(modifier = Modifier.size(16.dp))
                            "OPTIONAL" -> ToyIcon(modifier = Modifier.size(16.dp))
                            else -> TargetIcon(modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = when (category) {
                                "MANDATORY" -> "Влияние обязательных трат на питомца:"
                                "OPTIONAL" -> "Влияние трат на радости на питомца:"
                                else -> "Влияние накоплений на питомца:"
                            },
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = when (category) {
                                "MANDATORY" -> Color(0xFF1B5E20)
                                "OPTIONAL" -> Color(0xFFE65100)
                                else -> Color(0xFF01579B)
                            }
                        )
                    }
                    Text(
                        text = when (category) {
                            "MANDATORY" -> "Корм и вода жизненно необходимы котику каждый день! Если обязательные траты не запланированы или в банке не хватает монет, котик голодает (-25% сытости), слабеет (-10% здоровья) и может сбежать."
                            "OPTIONAL" -> "Игрушки, мячики и лакомства дарят котику море счастья и поднимают настроение! Но если потратить на них все монеты сверх плана, пострадает точность бюджета («План vs Факт»), а на еду может не хватить. Удовлетворяй желания умеренно!"
                            else -> "Копилка учит терпению и помогает котику расти! Достижение целей исполняет мечты и ускоряет взросление котика. Каждая монетка в банке приближает большое будущее питомца!"
                        },
                        fontFamily = AppFontFamily,
                        fontSize = 12.sp,
                        color = PixelTextDark,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Диалог с подробным объяснением механики «План vs Факт»
 */
@Composable
private fun BudgetHelpDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        KeepScreenImmersive()
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(PixelParchmentLight)
                .border(2.dp, PixelParchmentBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DiagramIcon(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Точность бюджета",
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PixelTextDark
                    )
                }

                Text(
                    text = "Показывает, насколько точно твои реальные траты совпали с планом в баночках.",
                    fontFamily = AppFontFamily,
                    fontSize = 13.sp,
                    color = PixelTextDark,
                    lineHeight = 18.sp
                )

                Text(
                    text = "Шкала оценок:\n• 75-100% — Отлично! Ты умело управляешь бюджетом.\n• 50-74% — Хорошо, но были небольшие перерасходы.\n• Меньше 50% — Внимание! Траты сильно превысили план.",
                    fontFamily = AppFontFamily,
                    fontSize = 12.sp,
                    color = PixelTextMuted,
                    lineHeight = 17.sp
                )

                Text(
                    text = "Совет: трать монеты осознанно и не забывай откладывать в копилку мечты!",
                    fontFamily = AppFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelGreenCrop
                )

                PixelButton(
                    text = "Понятно",
                    onClick = onDismiss,
                    containerColor = PixelGreenCrop,
                    textColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
