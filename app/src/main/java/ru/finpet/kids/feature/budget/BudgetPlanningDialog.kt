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
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.CoinText
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
import ru.finpet.kids.core.designsystem.PixelWoodLight
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.core.domain.model.BudgetPlan

/**
 * диалог планирования личного бюджета «волшебные баночки заботы» (ТЗ 2.5.5).
 * ребенок раскладывает карманные монеты по 3 категориям:
 * 1. обязательное (еда и уход за питомцем)
 * 2. желаемое (игрушки и лакомства)
 * 3. накопления (в копилку на большую цель)
 */
@Composable
fun BudgetPlanningDialog(
    balance: Int,
    currentDay: Int,
    initialPlan: BudgetPlan,
    onConfirm: (BudgetPlan) -> Unit,
    onDismiss: () -> Unit
) {
    // локальное состояние баночек до нажатия кнопки утверждения
    var mandatory by remember(initialPlan) { mutableIntStateOf(initialPlan.plannedMandatory) }
    var optional by remember(initialPlan) { mutableIntStateOf(initialPlan.plannedOptional) }
    var savings by remember(initialPlan) { mutableIntStateOf(initialPlan.plannedSavings) }

    val totalPlanned = mandatory + optional + savings
    val remainder = balance - totalPlanned
    val isOverBudget = remainder < 0
    val isValid = !isOverBudget && (totalPlanned > 0 || balance == 0)

    // живая реакция питомца на текущее распределение монет
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
                    // реакция питомца на наполнение баночек
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
                                        fontSize = 17.sp,
                                        color = PixelTextDark
                                    )
                                    Text(
                                        text = finnieSpeech,
                                        fontSize = 15.sp,
                                        color = PixelTextDark,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }

                    // остаток в кошельке и статус перерасхода
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
                                        fontSize = 17.sp,
                                        color = PixelTextDark,
                                        fontFamily = AppFontFamily
                                    )
                                }
                                CoinText(
                                    prefix = "В баночках: $totalPlanned",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverBudget) PixelRedBerry else PixelTextDark
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

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
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverBudget) PixelRedBerry else PixelGreenCrop
                                )
                            }
                        }
                    }

                    // готовые детские пресеты распределения
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "✨ БЫСТРЫЕ ПРЕСЕТЫ:",
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
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
                                        // 50% обязательное, 30% накопления, 20% радости
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
                                        // акцент на быстрое накопление цели
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

                    // баночка 1: обязательные расходы (еда, здоровье)
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

                    // баночка 2: желаемые расходы (игрушки, вкусняшки)
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

                    // баночка 3: накопления в копилку
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

                    // кнопки закрытия и подтверждения плана
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
                                modifier = Modifier.weight(1f),
                                fontSize = 17.sp
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
                                modifier = Modifier.weight(1.6f),
                                fontSize = 17.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * компактная плашка пресета
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
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PixelTextDark,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = proportions,
                fontSize = 12.sp,
                color = PixelTextMuted,
                fontFamily = AppFontFamily,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * интерактивная баночка бюджета с кнопками быстрого добавления (+10, +20, -10)
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
                        fontSize = 17.sp,
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
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 15.sp,
                    color = PixelTextMuted,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            // текущая сумма в баночке
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.15f))
                    .border(1.dp, color, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                CoinText(
                    prefix = "$amount",
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    fontSize = 17.sp,
                    fontFamily = AppFontFamily
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // шкала наполнения баночки относительно всех денег в кошельке
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Наполнение баночки:",
                    fontSize = 14.sp,
                    color = PixelTextMuted
                )
                Text(
                    text = "$fillPercent% от кошелька",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontFamily = AppFontFamily
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

        // кнопки быстрого добавления (+10, +20, -10)
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
                borderColor = PixelParchmentBorder,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            PixelButton(
                text = "+10",
                onClick = onPlus10,
                containerColor = color,
                textColor = Color.White,
                borderColor = PixelWoodDark,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            PixelButton(
                text = "+20",
                onClick = onPlus20,
                containerColor = color,
                textColor = Color.White,
                borderColor = PixelWoodDark,
                fontSize = 16.sp
            )
        }
    }
}
