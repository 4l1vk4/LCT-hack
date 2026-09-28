package ru.finpet.kids.feature.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.PixelBlueWater
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGoldGlow
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
import ru.finpet.kids.core.domain.calculator.PetEconomyCalculator
import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetCompliance
import ru.finpet.kids.core.domain.model.BudgetPlan

/**
 * Модель «3 Звезды Бюджета» (ТЗ 2.5.5, Шаг 5 Приложения А):
 * ⭐ Звезда Заботы — Обязательное выполнено (еда, вода, здоровье питомца)
 * ⭐ Звезда Мечты — В копилку отложено по плану (накопления на цель ребенка)
 * ⭐ Звезда Мудрости — Желаемое не превышено (лимит трат на радости соблюден)
 */
data class BudgetStars(
    val careStar: Boolean,
    val dreamStar: Boolean,
    val wisdomStar: Boolean
) {
    val count: Int get() = (if (careStar) 1 else 0) + (if (dreamStar) 1 else 0) + (if (wisdomStar) 1 else 0)
    val isAllEarned: Boolean get() = count == 3
}

/**
 * Расчет полученных 3 Звезд Бюджета
 */
fun calculateBudgetStars(plan: BudgetPlan, actual: ActualExpenses): BudgetStars {
    val careStar = if (plan.plannedMandatory > 0) {
        actual.actualMandatory >= plan.plannedMandatory
    } else {
        actual.actualMandatory >= 30 || actual.hasPurchasedFood
    }

    val dreamStar = if (plan.plannedSavings > 0) {
        actual.actualSavings >= plan.plannedSavings
    } else {
        actual.actualSavings >= 20
    }

    val wisdomStar = actual.actualOptional <= plan.plannedOptional

    return BudgetStars(
        careStar = careStar,
        dreamStar = dreamStar,
        wisdomStar = wisdomStar
    )
}

/**
 * Диалог планирования личного бюджета «Интерактивные волшебные баночки заботы»
 * (Концепт 1 для ТЗ 2.5.5, Шаг 5 Приложения А).
 *
 * Предназначен для детей 7–11 лет:
 * - Шапка с живой реакцией Финни (голод, мечта, перерасход, похвала)
 * - 3 детских пресета («Умный хозяин», «Турбо-копилка», «Поровну»)
 * - 3 волшебные баночки: «Миска жизни», «Сундучок радости», «Копилка мечты»
 *   с кнопками быстрого добавления (+10, +20, -10) и визуальным прогресс-баром
 * - Наглядный остаток: «Свободно в кошельке: X монет» с защитой от перерасхода
 */
@Composable
fun BudgetPlanningDialog(
    balance: Int,
    currentDay: Int,
    initialPlan: BudgetPlan,
    onConfirm: (BudgetPlan) -> Unit,
    onDismiss: () -> Unit
) {
    var mandatory by remember(initialPlan) { mutableIntStateOf(initialPlan.plannedMandatory) }
    var optional by remember(initialPlan) { mutableIntStateOf(initialPlan.plannedOptional) }
    var savings by remember(initialPlan) { mutableIntStateOf(initialPlan.plannedSavings) }

    val totalPlanned = mandatory + optional + savings
    val remainder = balance - totalPlanned
    val isOverBudget = remainder < 0
    val isValid = !isOverBudget && (totalPlanned > 0 || balance == 0)

    // Живая реакция Финни в шапке
    val (finnieEmoji, finnieMoodTitle, finnieSpeech) = when {
        isOverBudget -> Triple(
            "🙀",
            "Ой-ой, перерасход!",
            "В кошельке не хватает ${-remainder} монет! Давай пересыпем немного назад из баночек."
        )
        mandatory < 30 && balance >= 30 -> Triple(
            "🥣",
            "Животик урчит...",
            "Положи хотя бы 30 монет в «Миску жизни» на вкусный обед, иначе я останусь голодным!"
        )
        savings > 0 && remainder >= 0 -> Triple(
            "😻",
            "Ура, мечта всё ближе!",
            "В «Копилку мечты» добавлено $savings монет! Каждый шаг приближает заветную цель!"
        )
        totalPlanned > 0 && remainder >= 0 -> Triple(
            "😺",
            "Отличный план заботы!",
            "Все баночки наполнены правильно! Финни будет сыт, здоров и счастлив."
        )
        else -> Triple(
            "🐾",
            "Привет, хозяин!",
            "Давай распределим монетки по 3 волшебным баночкам до начала покупок!"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            StardewBoard(
                headerTitle = "ВОЛШЕБНЫЕ БАНОЧКИ • ДЕНЬ $currentDay",
                headerIcon = "🍯"
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    // 1. Шапка с живой реакцией Финни
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isOverBudget -> Color(0xFFFFEBEE)
                                        mandatory < 30 && balance >= 30 -> Color(0xFFFFF3E0)
                                        savings > 0 -> Color(0xFFE8F5E9)
                                        totalPlanned > 0 -> Color(0xFFF1F8E9)
                                        else -> PixelParchmentMedium
                                    }
                                )
                                .border(
                                    1.5.dp,
                                    when {
                                        isOverBudget -> PixelRedBerry
                                        mandatory < 30 && balance >= 30 -> Color(0xFFFF9800)
                                        savings > 0 || totalPlanned > 0 -> PixelGreenCrop
                                        else -> PixelParchmentBorder
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.5.dp, PixelWoodLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = finnieEmoji, fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = finnieMoodTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PixelTextDark
                                    )
                                    Text(
                                        text = finnieSpeech,
                                        fontSize = 11.sp,
                                        color = PixelTextDark,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // 2. Сводка кошелька и остатка с защитой от перерасхода
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isOverBudget) Color(0xFFFFEBEE) else PixelParchmentMedium)
                                .border(
                                    1.dp,
                                    if (isOverBudget) PixelRedBerry else PixelParchmentBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CoinIcon(modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "В кошельке: $balance",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PixelTextDark,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = "В баночках: $totalPlanned 🪙",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isOverBudget) PixelRedBerry else PixelTextDark,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Наглядный остаток (ТЗ 2.5.5)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isOverBudget) PixelRedBerry.copy(alpha = 0.15f) else Color(0xFFE8F5E9))
                                    .border(
                                        1.dp,
                                        if (isOverBudget) PixelRedBerry else PixelGreenCrop,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (isOverBudget) {
                                        "⚠️ Превышение бюджета на ${-remainder} монет! Уменьши суммы в баночках."
                                    } else {
                                        "👛 Свободно в кошельке: $remainder монет останется на руках."
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverBudget) PixelRedBerry else PixelGreenCrop
                                )
                            }
                        }
                    }

                    // 3. Детские пресеты
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "✨ БЫСТРЫЕ ПРЕСЕТЫ:",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = PixelTextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                PresetCard(
                                    icon = "🌟",
                                    title = "Умный хозяин",
                                    proportions = "50 / 30 / 20",
                                    containerColor = PixelGoldGlow,
                                    borderColor = PixelGoldDark,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        mandatory = ((balance * 0.50f) / 10).toInt() * 10
                                        savings = ((balance * 0.30f) / 10).toInt() * 10
                                        optional = ((balance * 0.20f) / 10).toInt() * 10
                                    }
                                )
                                PresetCard(
                                    icon = "🎯",
                                    title = "Турбо-копилка",
                                    proportions = "35 / 55 / 10",
                                    containerColor = PixelParchmentLight,
                                    borderColor = PixelBlueWater,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        mandatory = ((balance * 0.35f) / 10).toInt() * 10
                                        savings = ((balance * 0.55f) / 10).toInt() * 10
                                        optional = ((balance * 0.10f) / 10).toInt() * 10
                                    }
                                )
                                PresetCard(
                                    icon = "⚖️",
                                    title = "Поровну",
                                    proportions = "по 33%",
                                    containerColor = PixelParchmentMedium,
                                    borderColor = PixelParchmentBorder,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        val share = (balance / 30) * 10
                                        mandatory = share
                                        optional = share
                                        savings = share
                                    }
                                )
                                PresetCard(
                                    icon = "↺",
                                    title = "Сброс",
                                    proportions = "по 0",
                                    containerColor = PixelParchmentMedium,
                                    borderColor = PixelParchmentBorder,
                                    modifier = Modifier.weight(0.75f),
                                    onClick = {
                                        mandatory = 0
                                        optional = 0
                                        savings = 0
                                    }
                                )
                            }
                        }
                    }

                    // 4. Баночка 1: 🍲 «Миска жизни» (Обязательное: еда, вода, здоровье)
                    item {
                        MagicJarCard(
                            icon = "🍲",
                            title = "«Миска жизни»",
                            categoryLabel = "Обязательное",
                            subtitle = "Еда, вода, гигиена и здоровье питомца",
                            amount = mandatory,
                            walletBalance = balance,
                            color = PixelGreenCrop,
                            onMinus = { if (mandatory >= 10) mandatory -= 10 },
                            onPlus10 = { mandatory += 10 },
                            onPlus20 = { mandatory += 20 }
                        )
                    }

                    // 5. Баночка 2: 🎈 «Сундучок радости» (Желаемое: лакомства, мячики, шарфик)
                    item {
                        MagicJarCard(
                            icon = "🎈",
                            title = "«Сундучок радости»",
                            categoryLabel = "Желаемое",
                            subtitle = "Лакомства, мячики, шарфик и веселье",
                            amount = optional,
                            walletBalance = balance,
                            color = PixelGoldDark,
                            onMinus = { if (optional >= 10) optional -= 10 },
                            onPlus10 = { optional += 10 },
                            onPlus20 = { optional += 20 }
                        )
                    }

                    // 6. Баночка 3: 🏦 «Копилка мечты» (Накопления: цель ребенка)
                    item {
                        MagicJarCard(
                            icon = "🏦",
                            title = "«Копилка мечты»",
                            categoryLabel = "Накопления",
                            subtitle = "Отчисления на заветную цель и домик",
                            amount = savings,
                            walletBalance = balance,
                            color = PixelBlueWater,
                            onMinus = { if (savings >= 10) savings -= 10 },
                            onPlus10 = { savings += 10 },
                            onPlus20 = { savings += 20 }
                        )
                    }

                    // 7. Кнопки действий
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PixelButton(
                                text = "✕ Закрыть",
                                onClick = onDismiss,
                                containerColor = PixelWoodMedium,
                                textColor = PixelParchmentLight,
                                borderColor = PixelWoodDark,
                                modifier = Modifier.weight(1f)
                            )
                            PixelButton(
                                text = if (isOverBudget) "⚠️ Перерасход" else "📜 Утвердить план",
                                onClick = {
                                    if (isValid) {
                                        onConfirm(
                                            BudgetPlan(
                                                plannedMandatory = mandatory,
                                                plannedOptional = optional,
                                                plannedSavings = savings
                                            )
                                        )
                                    }
                                },
                                enabled = isValid,
                                containerColor = if (isValid) PixelGreenCrop else Color(0xFFB0BEC5),
                                textColor = Color.White,
                                borderColor = if (isValid) Color(0xFF1B5E20) else Color(0xFF78909C),
                                modifier = Modifier.weight(1.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Компактная карточка детского пресета
 */
@Composable
private fun PresetCard(
    icon: String,
    title: String,
    proportions: String,
    containerColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(borderColor)
            .clickable(onClick = onClick)
            .padding(bottom = 2.dp)
            .background(containerColor)
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 14.sp)
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = PixelTextDark,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = proportions,
                fontSize = 8.sp,
                color = PixelTextMuted,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * Интерактивная волшебная баночка бюджета:
 * Название, иконка, визуальный прогресс-бар наполнения,
 * кнопки быстрого добавления (+10, +20, -10).
 */
@Composable
private fun MagicJarCard(
    icon: String,
    title: String,
    categoryLabel: String,
    subtitle: String,
    amount: Int,
    walletBalance: Int,
    color: Color,
    onMinus: () -> Unit,
    onPlus10: () -> Unit,
    onPlus20: () -> Unit
) {
    val fillRatio = if (walletBalance > 0) {
        (amount.toFloat() / walletBalance.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val fillPercent = (fillRatio * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(PixelParchmentLight)
            .border(1.5.dp, PixelParchmentBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        // Шапка баночки
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.15f))
                    .border(1.dp, color, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PixelTextDark
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(color.copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = categoryLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = PixelTextMuted,
                    lineHeight = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            // Сумма в баночке
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.15f))
                    .border(1.dp, color, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$amount 🪙",
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Визуальный прогресс-бар наполнения баночки
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Наполнение баночки:",
                    fontSize = 10.sp,
                    color = PixelTextMuted
                )
                Text(
                    text = "$fillPercent% от кошелька",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            LinearProgressIndicator(
                progress = { fillRatio },
                color = color,
                trackColor = PixelParchmentBorder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопки быстрого добавления (+10, +20, -10)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PixelButton(
                text = "-10",
                onClick = onMinus,
                enabled = amount >= 10,
                containerColor = PixelParchmentMedium,
                textColor = PixelTextDark,
                borderColor = PixelParchmentBorder
            )
            Spacer(modifier = Modifier.width(6.dp))
            PixelButton(
                text = "+10",
                onClick = onPlus10,
                containerColor = color,
                textColor = Color.White,
                borderColor = PixelWoodDark
            )
            Spacer(modifier = Modifier.width(6.dp))
            PixelButton(
                text = "+20",
                onClick = onPlus20,
                containerColor = color,
                textColor = Color.White,
                borderColor = PixelWoodDark
            )
        }
    }
}

/**
 * Виджет «ПЛАН vs ФАКТ» (ТЗ 2.5.5, Шаг 5 Приложения А).
 * Включает систему «3 Звезды Бюджета» (⭐⭐⭐):
 * 1. ⭐ Звезда Заботы (Обязательное выполнено)
 * 2. ⭐ Звезда Мечты (В копилку отложено по плану)
 * 3. ⭐ Звезда Мудрости (Желаемое не превышено)
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
            // Верхняя сводная плашка
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
                        text = "План: ${plan.totalPlanned} 🪙 • Факт: ${actual.totalActual} 🪙",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PixelTextDark,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (compliance.isDisciplined) "🌟 Отличная дисциплина трат!" else "💡 Следи за покупками в Лавке",
                        fontSize = 11.sp,
                        color = PixelTextMuted
                    )
                }

                // Бейдж процента соблюдения
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
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Блок «3 ЗВЕЗДЫ БЮДЖЕТА» (ТЗ 2.5.5)
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
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = PixelGoldBright
                            )
                        }
                        Text(
                            text = "${stars.count}/3 ЗВЁЗД",
                            fontFamily = FontFamily.Monospace,
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

            // Корзина 1: Обязательное
            CategoryComparisonRow(
                icon = "🍲",
                title = "«Миска жизни» (Обязательное)",
                starStatus = if (stars.careStar) "⭐" else "⚪",
                planned = plan.plannedMandatory,
                actual = actual.actualMandatory,
                color = PixelGreenCrop,
                isCompleted = actual.actualMandatory >= plan.plannedMandatory && plan.plannedMandatory > 0
            )

            // Корзина 2: Необязательное
            CategoryComparisonRow(
                icon = "🎈",
                title = "«Сундучок радости» (Желаемое)",
                starStatus = if (stars.wisdomStar) "⭐" else "⚪",
                planned = plan.plannedOptional,
                actual = actual.actualOptional,
                color = PixelGoldDark,
                isOverspent = actual.actualOptional > plan.plannedOptional
            )

            // Корзина 3: Копилка
            CategoryComparisonRow(
                icon = "🏦",
                title = "«Копилка мечты» (В копилку)",
                starStatus = if (stars.dreamStar) "⭐" else "⚪",
                planned = plan.plannedSavings,
                actual = actual.actualSavings,
                color = PixelBlueWater,
                isCompleted = actual.actualSavings >= plan.plannedSavings && plan.plannedSavings > 0
            )

            // Совет от Финни
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

            // Кнопки управления
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
 * Чип отдельной звезды бюджета
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
 * Строка сравнения плана и факта по одной категории с прогресс-баром и статусом звезды
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
                    fontSize = 11.sp,
                    color = PixelTextDark
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = starStatus, fontSize = 12.sp)
            }
            Text(
                text = "$actual / $planned 🪙",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = if (isOverspent) PixelRedBerry else color,
                fontFamily = FontFamily.Monospace
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

/**
 * Компактный виджет «Пульс Бюджета» (ТЗ 2.5.3, 2.5.5).
 * Отображает 3 мини-корзины `🍲 40/40 • 🎈 0/20 • 🏦 30/30`,
 * число заработанных звёзд и бейдж соответствия плану.
 * По клику открывает карточку «План vs Факт».
 */
@Composable
fun BudgetPulseWidget(
    plan: BudgetPlan,
    actual: ActualExpenses,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            // Верхняя плашка: Заголовок + Звезды + Бейдж соответствия
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
                        fontFamily = FontFamily.Monospace,
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

                // Бейдж процента
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
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // 3 мини-корзины: 🍲 40/40 • 🎈 0/20 • 🏦 30/30
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🍲 ${actual.actualMandatory}/${plan.plannedMandatory}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = PixelGreenCrop,
                    fontFamily = FontFamily.Monospace
                )
                Text(text = "•", color = PixelParchmentBorder, fontWeight = FontWeight.Bold)
                Text(
                    text = "🎈 ${actual.actualOptional}/${plan.plannedOptional}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (actual.actualOptional > plan.plannedOptional) PixelRedBerry else PixelGoldDark,
                    fontFamily = FontFamily.Monospace
                )
                Text(text = "•", color = PixelParchmentBorder, fontWeight = FontWeight.Bold)
                Text(
                    text = "🏦 ${actual.actualSavings}/${plan.plannedSavings}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = PixelBlueWater,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Подсказка клика
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

/**
 * Диалог «План vs Факт» для открытия по клику на BudgetPulseWidget
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
 * Блок итогов дня для диалога смены дня (NextDayConfirmDialog).
 * Отображает траты и полученные 3 Звезды Бюджета.
 */
@Composable
fun DaySummaryBudgetSection(
    plan: BudgetPlan,
    actual: ActualExpenses,
    compliance: BudgetCompliance,
    carePoints: Int
) {
    val stars = remember(plan, actual) { calculateBudgetStars(plan, actual) }

    Column(
        modifier = Modifier
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
                fontFamily = FontFamily.Monospace,
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

        // Отображение 3 Звёзд Бюджета (ТЗ 2.5.5)
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
            Text(
                text = "Звёзды бюджета: ${stars.count}/3",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = PixelTextDark
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "${if (stars.careStar) "⭐" else "⚪"} Забота",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (stars.careStar) PixelGreenCrop else PixelTextMuted
                )
                Text(
                    text = "${if (stars.dreamStar) "⭐" else "⚪"} Мечта",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (stars.dreamStar) PixelBlueWater else PixelTextMuted
                )
                Text(
                    text = "${if (stars.wisdomStar) "⭐" else "⚪"} Мудрость",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (stars.wisdomStar) PixelGoldDark else PixelRedBerry
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "🍲 Обязательное:\n${actual.actualMandatory} / ${plan.plannedMandatory} 🪙",
                fontSize = 10.sp,
                lineHeight = 13.sp,
                color = PixelTextDark
            )
            Text(
                text = "🎈 Желаемое:\n${actual.actualOptional} / ${plan.plannedOptional} 🪙",
                fontSize = 10.sp,
                lineHeight = 13.sp,
                color = PixelTextDark
            )
            Text(
                text = "🏦 В копилку:\n${actual.actualSavings} / ${plan.plannedSavings} 🪙",
                fontSize = 10.sp,
                lineHeight = 13.sp,
                color = PixelTextDark
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

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
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (compliance.isDisciplined) PixelGreenCrop else PixelTextDark,
                lineHeight = 15.sp
            )
        }
    }
}
