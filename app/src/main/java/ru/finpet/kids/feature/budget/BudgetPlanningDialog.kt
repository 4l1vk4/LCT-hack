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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.R
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FinPetTheme
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.KoshelekIcon
import ru.finpet.kids.core.designsystem.LockIcon
import ru.finpet.kids.core.designsystem.RenderBoardIcon
import ru.finpet.kids.core.designsystem.PixelBlueWater
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelJar
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.pixelButtonBackground
import ru.finpet.kids.core.designsystem.pixelPanelBackground
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import ru.finpet.kids.core.designsystem.PixelAlertDialog
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.core.designsystem.WarningIcon
import ru.finpet.kids.core.domain.model.BudgetPlan
import kotlin.math.roundToInt

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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FinPetTheme {
            val uiScale = LocalUiScale.current
            KeepScreenImmersive()
            Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            StardewBoard(
                headerTitle = "ВОЛШЕБНЫЕ БАНОЧКИ",
                headerIcon = "empty_jar"
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy((10.dp * uiScale).coerceIn(6.dp, 10.dp)),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    if (isConfirmed) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .border(1.dp, PixelGreenCrop, RoundedCornerShape(6.dp))
                                    .padding(
                                        horizontal = (10.dp * uiScale).coerceIn(6.dp, 10.dp),
                                        vertical = (8.dp * uiScale).coerceIn(5.dp, 8.dp)
                                    )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LockIcon(modifier = Modifier.size((18.dp * uiScale).coerceIn(14.dp, 18.dp)))
                                    Spacer(modifier = Modifier.width((8.dp * uiScale).coerceIn(4.dp, 8.dp)))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Бюджет на период утверждён!",
                                            fontSize = (15f * uiScale).coerceIn(12f, 16f).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1B5E20)
                                        )
                                        Text(
                                            text = "Распределение монет зафиксировано. Новый план можно составить в начале следующей недели.",
                                            fontSize = (14f * uiScale).coerceIn(11f, 15f).sp,
                                            color = Color(0xFF2E7D32),
                                            lineHeight = (18f * uiScale).coerceIn(13f, 19f).sp
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
                            contentPadding = PaddingValues(
                                horizontal = (12.dp * uiScale).coerceIn(6.dp, 14.dp),
                                vertical = (8.dp * uiScale).coerceIn(5.dp, 10.dp)
                            )
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "В кошельке: $balance",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = (16f * uiScale).coerceIn(11f, 17f).sp,
                                            color = PixelTextDark,
                                            fontFamily = AppFontFamily
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        CoinIcon(modifier = Modifier.size((16.dp * uiScale).coerceIn(12.dp, 19.dp)))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "В баночках: $totalPlanned",
                                            fontSize = (16f * uiScale).coerceIn(11f, 17f).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOverBudget) PixelRedBerry else PixelTextDark,
                                            fontFamily = AppFontFamily
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        CoinIcon(modifier = Modifier.size((16.dp * uiScale).coerceIn(12.dp, 19.dp)))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .pixelButtonBackground(containerColor = if (isOverBudget) Color(0xFFFFCDD2) else Color(0xFFDCEDC8))
                                        .padding(
                                            horizontal = (12.dp * uiScale).coerceIn(6.dp, 18.dp),
                                            vertical = (6.dp * uiScale).coerceIn(4.dp, 8.dp)
                                        )
                                ) {
                                    if (isOverBudget) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            WarningIcon(modifier = Modifier.size((18.dp * uiScale).coerceIn(14.dp, 18.dp)))
                                            Text(
                                                text = "Превышение бюджета на ${-remainder} монет! Уменьши суммы в баночках.",
                                                fontFamily = AppFontFamily,
                                                fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PixelRedBerry,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    } else {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                KoshelekIcon(modifier = Modifier.size((18.dp * uiScale).coerceIn(14.dp, 20.dp)))
                                                Text(
                                                    text = "Свободно в кошельке: $remainder монет останется на руках.",
                                                    fontFamily = AppFontFamily,
                                                    fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PixelGreenCrop,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            if (remainder > 0) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Нераспределённые деньги расходуются только если в банках закончатся монеты. Траты из свободного кошелька снижают точность бюджета, так как их трудно контролировать!",
                                                    fontSize = (14f * uiScale).coerceIn(10f, 15f).sp,
                                                    color = PixelTextMuted,
                                                    lineHeight = (16f * uiScale).coerceIn(12f, 17f).sp,
                                                    fontFamily = AppFontFamily
                                                )
                                            }
                                        }
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
                                    text = "БЫСТРЫЕ ПРЕСЕТЫ:",
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
                            jarRes = R.drawable.life_jar,
                            icon = "🍲",
                            title = "«Миска жизни»",
                            categoryLabel = "Обязательное",
                            subtitle = "Еда, вода, гигиена и здоровье питомца",
                            amount = mandatory,
                            walletBalance = balance,
                            color = PixelGreenCrop,
                            remainder = remainder,
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
                            },
                            onSetAmount = { newAmt ->
                                selectedPreset = null
                                mandatory = newAmt
                            }
                        )
                    }

                    // баночка 2: желаемые расходы (игрушки, вкусняшки)
                    item {
                        MagicJarCard(
                            jarRes = R.drawable.joy_jar,
                            icon = "🎈",
                            title = "«Сундучок радости»",
                            categoryLabel = "Желаемое",
                            subtitle = "Лакомства, мячики, шарфик и веселье",
                            amount = optional,
                            walletBalance = balance,
                            color = PixelGoldDark,
                            remainder = remainder,
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
                            },
                            onSetAmount = { newAmt ->
                                selectedPreset = null
                                optional = newAmt
                            }
                        )
                    }

                    // баночка 3: накопления в копилку
                    item {
                        MagicJarCard(
                            jarRes = R.drawable.saving_jar,
                            icon = "🏦",
                            title = "«Копилка мечты»",
                            categoryLabel = "В копилку",
                            subtitle = "Отчисления на заветную цель и домик",
                            amount = savings,
                            walletBalance = balance,
                            color = PixelBlueWater,
                            remainder = remainder,
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
                            },
                            onSetAmount = { newAmt ->
                                selectedPreset = null
                                savings = newAmt
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
                                fontSize = (16f * uiScale).coerceIn(12f, 17f).sp
                            )
                        } else {
                            val confirmText = when {
                                isOverBudget -> "Перерасход"
                                uiScale < 0.85f -> "Утвердить"
                                else -> "📜 Утвердить план"
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy((6.dp * uiScale).coerceIn(4.dp, 8.dp))
                            ) {
                                PixelButton(
                                    text = "Закрыть",
                                    onClick = onDismiss,
                                    containerColor = PixelWoodMedium,
                                    textColor = PixelParchmentLight,
                                    borderColor = PixelWoodDark,
                                    modifier = Modifier.weight(1f),
                                    fontSize = (15f * uiScale).coerceIn(11f, 17f).sp
                                )
                                PixelButton(
                                    text = confirmText,
                                    leadingIcon = if (isOverBudget) {
                                        { WarningIcon(modifier = Modifier.size((16.dp * uiScale).coerceIn(12.dp, 16.dp))) }
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
                                    fontSize = (15f * uiScale).coerceIn(11f, 17f).sp
                                )
                            }
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
    val uiScale = LocalUiScale.current
    val bg = if (isSelected) PixelGoldBright else containerColor
    Box(
        modifier = modifier
            .pixelButtonBackground(containerColor = bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = (6.dp * uiScale).coerceIn(3.dp, 8.dp),
                vertical = (5.dp * uiScale).coerceIn(3.dp, 7.dp)
            ),
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
                if (icon == "↺") {
                    RenderBoardIcon(icon = icon, modifier = Modifier.size((14.dp * uiScale).coerceIn(10.dp, 14.dp)))
                } else {
                    RenderBoardIcon(icon = icon, modifier = Modifier.size((18.dp * uiScale).coerceIn(12.dp, 20.dp)))
                }
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = title,
                    fontSize = (13f * uiScale).coerceIn(10f, 15f).sp,
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = PixelTextDark,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = proportions,
                fontSize = (12f * uiScale).coerceIn(9f, 14f).sp,
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
    jarRes: Int = 0,
    icon: String,
    title: String,
    categoryLabel: String,
    subtitle: String,
    amount: Int,
    walletBalance: Int,
    color: Color,
    remainder: Int,
    isReadOnly: Boolean = false,
    onMinus: () -> Unit,
    onPlus10: () -> Unit,
    onPlus20: () -> Unit,
    onSetAmount: (Int) -> Unit
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    var customInput by remember(amount) { mutableStateOf(amount.toString()) }

    if (showCustomDialog) {
        PixelAlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = "СВОЯ СУММА",
            icon = "empty_jar",
            confirmButton = {
                PixelButton(
                    text = "Сохранить",
                    onClick = {
                        val entered = customInput.toIntOrNull() ?: 0
                        val maxPossible = amount + remainder
                        onSetAmount(entered.coerceIn(0, maxPossible))
                        showCustomDialog = false
                    },
                    containerColor = PixelGoldBright,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 16.sp
                )
            },
            dismissButton = {
                PixelButton(
                    text = "Отмена",
                    onClick = { showCustomDialog = false },
                    containerColor = PixelParchmentMedium,
                    textColor = PixelTextDark,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 16.sp
                )
            }
        ) {
            val focusManager = LocalFocusManager.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
            ) {
                Text(
                    text = "Баночка: $title",
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PixelTextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Свободно: $remainder",
                        fontFamily = AppFontFamily,
                        fontSize = 14.sp,
                        color = PixelTextMuted
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    CoinIcon(modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(макс.: ${amount + remainder}",
                        fontFamily = AppFontFamily,
                        fontSize = 14.sp,
                        color = PixelTextMuted
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    CoinIcon(modifier = Modifier.size(15.dp))
                    Text(
                        text = ")",
                        fontFamily = AppFontFamily,
                        fontSize = 14.sp,
                        color = PixelTextMuted
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = customInput,
                    onValueChange = { customInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Монет в баночке", fontFamily = AppFontFamily, fontSize = 15.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PixelParchmentBorder,
                        unfocusedBorderColor = PixelParchmentBorder,
                        focusedContainerColor = PixelParchmentLight,
                        unfocusedContainerColor = PixelParchmentLight,
                        focusedTextColor = PixelTextDark,
                        unfocusedTextColor = PixelTextDark
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PixelButton(
                        text = "0",
                        trailingIcon = { CoinIcon(modifier = Modifier.size(13.dp)) },
                        onClick = { customInput = "0" },
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        containerColor = PixelParchmentLight,
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                    )
                    PixelButton(
                        text = "+10",
                        trailingIcon = { CoinIcon(modifier = Modifier.size(13.dp)) },
                        repeating = true,
                        onClick = {
                            val curr = customInput.toIntOrNull() ?: 0
                            val next = (curr + 10).coerceAtMost(amount + remainder)
                            customInput = next.toString()
                        },
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        containerColor = PixelParchmentLight,
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                    )
                    PixelButton(
                        text = "Все (${amount + remainder})",
                        onClick = { customInput = (amount + remainder).toString() },
                        modifier = Modifier.weight(1.5f),
                        fontSize = 13.sp,
                        containerColor = PixelParchmentLight,
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                    )
                }
            }
        }
    }

    val uiScale = LocalUiScale.current
    val fillRatio = if (walletBalance > 0) {
        (amount.toFloat() / walletBalance.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val fillPercent = (fillRatio * 100).toInt()
    val fillDivisions = (fillRatio * 10f).roundToInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pixelPanelBackground()
            .padding((10.dp * uiScale).coerceIn(6.dp, 10.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (jarRes != 0) {
                PixelJar(
                    jarRes = jarRes,
                    fillDivisions = fillDivisions,
                    modifier = Modifier.size((44.dp * uiScale).coerceIn(32.dp, 46.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size((36.dp * uiScale).coerceIn(28.dp, 38.dp))
                        .pixelButtonBackground(containerColor = color.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = (18f * uiScale).coerceIn(14f, 20f).sp)
                }
            }
            Spacer(modifier = Modifier.width((6.dp * uiScale).coerceIn(4.dp, 8.dp)))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = (17f * uiScale).coerceIn(13f, 18f).sp,
                    color = PixelTextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = (14f * uiScale).coerceIn(10f, 16f).sp,
                    color = PixelTextMuted,
                    lineHeight = (18f * uiScale).coerceIn(13f, 21f).sp
                )
            }
            Spacer(modifier = Modifier.width((6.dp * uiScale).coerceIn(4.dp, 6.dp)))
            // текущая сумма в баночке в пиксельном бейдже (клик открывает ввод своей суммы)
            Box(
                modifier = Modifier
                    .pixelButtonBackground(containerColor = PixelParchmentLight)
                    .clickable(enabled = !isReadOnly) {
                        customInput = amount.toString()
                        showCustomDialog = true
                    }
                    .padding(
                        horizontal = (8.dp * uiScale).coerceIn(4.dp, 10.dp),
                        vertical = (4.dp * uiScale).coerceIn(3.dp, 5.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$amount",
                        fontWeight = FontWeight.ExtraBold,
                        color = color,
                        fontSize = (16f * uiScale).coerceIn(12f, 17f).sp,
                        fontFamily = AppFontFamily
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    CoinIcon(modifier = Modifier.size((16.dp * uiScale).coerceIn(12.dp, 18.dp)))
                }
            }
        }

        Spacer(modifier = Modifier.height((6.dp * uiScale).coerceIn(4.dp, 8.dp)))

        // шкала наполнения баночки относительно всех денег в кошельке и бейдж категории
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .pixelButtonBackground(containerColor = color.copy(alpha = 0.35f))
                        .padding(
                            horizontal = (6.dp * uiScale).coerceIn(4.dp, 8.dp),
                            vertical = (2.dp * uiScale).coerceIn(2.dp, 4.dp)
                        )
                ) {
                    Text(
                        text = categoryLabel,
                        fontSize = (12f * uiScale).coerceIn(9f, 13f).sp,
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextDark
                    )
                }
                Text(
                    text = "$fillPercent% от кошелька",
                    fontSize = (13f * uiScale).coerceIn(10f, 15f).sp,
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
            Spacer(modifier = Modifier.height((6.dp * uiScale).coerceIn(4.dp, 8.dp)))

            // кнопки быстрого добавления (-10, +10, +20, Своя) на всю ширину
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((4.dp * uiScale).coerceIn(2.dp, 4.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PixelButton(
                    text = "-10",
                    onClick = onMinus,
                    enabled = amount >= 10,
                    repeating = true,
                    containerColor = PixelParchmentMedium,
                    textColor = PixelTextDark,
                    fontSize = (13f * uiScale).coerceIn(10f, 13f).sp,
                    contentPadding = PaddingValues(
                        horizontal = (4.dp * uiScale).coerceIn(2.dp, 6.dp),
                        vertical = (4.dp * uiScale).coerceIn(2.dp, 4.dp)
                    ),
                    modifier = Modifier.weight(1f)
                )
                PixelButton(
                    text = "+10",
                    onClick = onPlus10,
                    enabled = remainder >= 10,
                    repeating = true,
                    containerColor = color,
                    textColor = Color.White,
                    fontSize = (13f * uiScale).coerceIn(10f, 13f).sp,
                    contentPadding = PaddingValues(
                        horizontal = (4.dp * uiScale).coerceIn(2.dp, 6.dp),
                        vertical = (4.dp * uiScale).coerceIn(2.dp, 4.dp)
                    ),
                    modifier = Modifier.weight(1f)
                )
                PixelButton(
                    text = "+20",
                    onClick = onPlus20,
                    enabled = remainder >= 20,
                    repeating = true,
                    containerColor = color,
                    textColor = Color.White,
                    fontSize = (13f * uiScale).coerceIn(10f, 13f).sp,
                    contentPadding = PaddingValues(
                        horizontal = (4.dp * uiScale).coerceIn(2.dp, 6.dp),
                        vertical = (4.dp * uiScale).coerceIn(2.dp, 4.dp)
                    ),
                    modifier = Modifier.weight(1f)
                )
                PixelButton(
                    text = "Своя",
                    onClick = {
                        customInput = amount.toString()
                        showCustomDialog = true
                    },
                    containerColor = PixelParchmentLight,
                    textColor = PixelTextDark,
                    fontSize = (12f * uiScale).coerceIn(9f, 12f).sp,
                    contentPadding = PaddingValues(
                        horizontal = (4.dp * uiScale).coerceIn(2.dp, 6.dp),
                        vertical = (4.dp * uiScale).coerceIn(2.dp, 4.dp)
                    ),
                    modifier = Modifier.weight(1.1f)
                )
            }
        }
    }
}
