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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.PixelBlueWater
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGoldGlow
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.pixelButtonBackground
import ru.finpet.kids.core.designsystem.pixelPanelBackground
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.PixelWoodLight
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.core.designsystem.WarningIcon
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
    isConfirmed: Boolean = false,
    onConfirm: (BudgetPlan) -> Unit,
    onDismiss: () -> Unit
) {
    // локальное состояние баночек до нажатия кнопки утверждения
    var mandatory by remember(initialPlan) { mutableIntStateOf(initialPlan.plannedMandatory) }
    var optional by remember(initialPlan) { mutableIntStateOf(initialPlan.plannedOptional) }
    var savings by remember(initialPlan) { mutableIntStateOf(initialPlan.plannedSavings) }
    var selectedPreset by remember { mutableStateOf<String?>("50/30/20") }

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
        KeepScreenImmersive()
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
                    // реакция питомца на наполнение баночек ("Животик урчит...")
                    item {
                        PixelPanel(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFF9EE))
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
                                        fontSize = 18.sp,
                                        color = PixelTextDark
                                    )
                                    Text(
                                        text = finnieSpeech,
                                        fontSize = 16.sp,
                                        color = PixelTextDark,
                                        lineHeight = 21.sp
                                    )
                                }
                            }
                        }
                    }

                    if (isConfirmed) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .border(1.dp, PixelGreenCrop, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🔒", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Бюджет на сегодня утверждён!",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1B5E20)
                                        )
                                        Text(
                                            text = "Распределение монет зафиксировано. Новый план можно составить в следующий день.",
                                            fontSize = 15.sp,
                                            color = Color(0xFF2E7D32),
                                            lineHeight = 19.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // остаток в кошельке и статус перерасхода
                    item {
                        PixelPanel(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
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
                                        .pixelButtonBackground(containerColor = if (isOverBudget) Color(0xFFFFCDD2) else Color(0xFFDCEDC8))
                                        .padding(horizontal = 18.dp, vertical = 8.dp)
                                ) {
                                    if (isOverBudget) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            WarningIcon(modifier = Modifier.size(18.dp))
                                            Text(
                                                text = "Превышение бюджета на ${-remainder} монет! Уменьши суммы в баночках.",
                                                fontFamily = AppFontFamily,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PixelRedBerry
                                            )
                                        }
                                    } else {
                                        Text(
                                             text = "👛 Свободно в кошельке: $remainder монет останется на руках.",
                                             fontFamily = AppFontFamily,
                                             fontSize = 16.sp,
                                             fontWeight = FontWeight.Bold,
                                             color = PixelGreenCrop
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (!isConfirmed) {
                        // готовые детские пресеты распределения в виде удобной и четкой сетки 2х2
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "✨ БЫСТРЫЕ ПРЕСЕТЫ:",
                                    fontFamily = AppFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = PixelTextMuted
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PresetCard(
                                        icon = "🌟",
                                        title = "Умный хозяин",
                                        proportions = "50% • 30% • 20%",
                                        isSelected = selectedPreset == "50/30/20",
                                        containerColor = PixelParchmentLight,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            selectedPreset = "50/30/20"
                                            // 50% обязательное, 30% накопления, 20% радости
                                            mandatory = ((balance * 0.50f) / 10).toInt() * 10
                                            savings = ((balance * 0.30f) / 10).toInt() * 10
                                            optional = ((balance * 0.20f) / 10).toInt() * 10
                                        }
                                    )
                                    PresetCard(
                                        icon = "🎯",
                                        title = "Турбо-копилка",
                                        proportions = "35% • 55% • 10%",
                                        isSelected = selectedPreset == "turbo",
                                        containerColor = PixelParchmentLight,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            selectedPreset = "turbo"
                                            // акцент на быстрое накопление цели
                                            mandatory = ((balance * 0.35f) / 10).toInt() * 10
                                            savings = ((balance * 0.55f) / 10).toInt() * 10
                                            optional = ((balance * 0.10f) / 10).toInt() * 10
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PresetCard(
                                        icon = "⚖️",
                                        title = "Поровну",
                                        proportions = "по 33% в баночки",
                                        isSelected = selectedPreset == "equal",
                                        containerColor = PixelParchmentLight,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            selectedPreset = "equal"
                                            val share = (balance / 30) * 10
                                            mandatory = share
                                            optional = share
                                            savings = share
                                        }
                                    )
                                    PresetCard(
                                        icon = "↺",
                                        title = "Сброс",
                                        proportions = "по 0 монет",
                                        isSelected = selectedPreset == "reset",
                                        containerColor = PixelParchmentLight,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            selectedPreset = "reset"
                                            mandatory = 0
                                            optional = 0
                                            savings = 0
                                        }
                                    )
                                }
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
                            isReadOnly = isConfirmed,
                            onMinus = {
                                selectedPreset = null
                                if (mandatory >= 10) mandatory -= 10
                            },
                            onPlus10 = {
                                selectedPreset = null
                                mandatory += 10
                            },
                            onPlus20 = {
                                selectedPreset = null
                                mandatory += 20
                            }
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
                            isReadOnly = isConfirmed,
                            onMinus = {
                                selectedPreset = null
                                if (optional >= 10) optional -= 10
                            },
                            onPlus10 = {
                                selectedPreset = null
                                optional += 10
                            },
                            onPlus20 = {
                                selectedPreset = null
                                optional += 20
                            }
                        )
                    }

                    // баночка 3: накопления в копилку
                    item {
                        MagicJarCard(
                            icon = "🏦",
                            title = "«Копилка мечты»",
                            categoryLabel = "В копилку",
                            subtitle = "Отчисления на заветную цель и домик",
                            amount = savings,
                            walletBalance = balance,
                            color = PixelBlueWater,
                            isReadOnly = isConfirmed,
                            onMinus = {
                                selectedPreset = null
                                if (savings >= 10) savings -= 10
                            },
                            onPlus10 = {
                                selectedPreset = null
                                savings += 10
                            },
                            onPlus20 = {
                                selectedPreset = null
                                savings += 20
                            }
                        )
                    }

                    // кнопки закрытия и подтверждения плана
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isConfirmed) {
                            PixelButton(
                                text = "✓ Понятно",
                                onClick = onDismiss,
                                containerColor = PixelGreenCrop,
                                textColor = Color.White,
                                borderColor = Color(0xFF1B5E20),
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 17.sp
                            )
                        } else {
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
                                    text = if (isOverBudget) "Перерасход" else "📜 Утвердить план",
                                    leadingIcon = if (isOverBudget) {
                                        { WarningIcon(modifier = Modifier.size(16.dp)) }
                                    } else null,
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
}

/**
 * компактная плашка пресета
 */
@Composable
private fun PresetCard(
    icon: String,
    title: String,
    proportions: String,
    isSelected: Boolean = false,
    containerColor: Color = PixelParchmentLight,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (isSelected) PixelGoldBright else containerColor
    Box(
        modifier = modifier
            .pixelButtonBackground(containerColor = bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = icon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = PixelTextDark,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = proportions,
                fontSize = 14.sp,
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) PixelTextDark else PixelTextMuted,
                textAlign = TextAlign.Center
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
    isReadOnly: Boolean = false,
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
            .pixelPanelBackground()
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .pixelButtonBackground(containerColor = color.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PixelTextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 16.sp,
                    color = PixelTextMuted,
                    lineHeight = 21.sp
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            // текущая сумма в баночке в пиксельном бейдже
            Box(
                modifier = Modifier
                    .pixelButtonBackground(containerColor = PixelParchmentLight)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
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

        // шкала наполнения баночки относительно всех денег в кошельке (без зеленых точек)
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Наполнение баночки:",
                    fontSize = 15.sp,
                    color = PixelTextMuted
                )
                Text(
                    text = "$fillPercent% от кошелька",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontFamily = AppFontFamily
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
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
                        .fillMaxWidth(fillRatio.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }
        }

        if (!isReadOnly) {
            Spacer(modifier = Modifier.height(8.dp))

            // бейдж категории и кнопки быстрого добавления (-10, +10, +20)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .pixelButtonBackground(containerColor = color.copy(alpha = 0.35f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = categoryLabel,
                        fontSize = 14.sp,
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextDark
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PixelButton(
                        text = "-10",
                        onClick = onMinus,
                        enabled = amount >= 10,
                        containerColor = PixelParchmentMedium,
                        textColor = PixelTextDark,
                        fontSize = 14.sp,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
                    )
                    PixelButton(
                        text = "+10",
                        onClick = onPlus10,
                        containerColor = color,
                        textColor = Color.White,
                        fontSize = 14.sp,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
                    )
                    PixelButton(
                        text = "+20",
                        onClick = onPlus20,
                        containerColor = color,
                        textColor = Color.White,
                        fontSize = 14.sp,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
