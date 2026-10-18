package ru.finpet.kids.feature.map.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.repository.PurchaseItem
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.ExclamationIcon
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.HeartIcon
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.LightningIcon
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.SkyBlue
import ru.finpet.kids.core.designsystem.StardewBoard

// отображение суммы с иконкой монеты на 2px больше размера текста
@Composable
fun CoinPriceTag(
    amount: Any,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = Color.Unspecified,
    prefix: String = "",
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (prefix.isNotEmpty()) {
            Text(
                text = prefix,
                fontSize = fontSize,
                fontWeight = fontWeight,
                color = color
            )
        }
        Text(
            text = "$amount ",
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color
        )
        CoinIcon(modifier = Modifier.size((fontSize.value + 2).dp))
    }
}

// вспомогательная функция форматирования прироста стата: 0% если уже максимум
fun formatStatGain(cur: Int, bonus: Int): String {
    val target = (cur + bonus).coerceIn(0, 100)
    val gain = if (cur >= 100) 0 else (target - cur)
    return "$cur% → $target% (+${gain}%)"
}

// диалог плана предзакупок
@Composable
fun PreorderPlanDialog(
    balance: Int,
    items: List<PurchaseItem>,
    selectedItemIds: Set<String>,
    currentPeriod: PeriodEntity?,
    profile: ProfileEntity?,
    onToggleItem: (String) -> Unit,
    onQuickBuy: (List<PurchaseItem>) -> Unit,
    onDismiss: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    val curSatiety = profile?.satiety ?: 100
    val curMood = profile?.mood ?: 100
    val curHealth = profile?.health ?: 100

    // исключаем поручение родителей из предзакупок
    val eligibleItems = remember(items) {
        items.filter { it.id != "groceries_parents" && it.category != "GROCERIES" }
    }

    val selectedItems = remember(eligibleItems, selectedItemIds) {
        eligibleItems.filter { selectedItemIds.contains(it.id) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        KeepScreenImmersive()
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(8.dp)
        ) {
            StardewBoard(
                headerTitle = "ПЛАН БЫСТРЫХ ПОКУПОК",
                headerIcon = "📋"
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // верхняя панель с кошельком (шрифт увеличен на 3) и кнопкой закрытия
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CoinPriceTag(
                            prefix = "Кошелёк: ",
                            amount = balance,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PixelTextDark
                        )
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(PixelWoodDark)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✕",
                                color = PixelGoldBright,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // текст под кошельком (шрифт увеличен на 3: 13 -> 16)
                    Text(
                        text = "Выбери товары, которые хочешь покупать одним нажатием.",
                        fontSize = 16.sp,
                        color = PixelTextMuted,
                        fontFamily = AppFontFamily
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // список доступных товаров
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(eligibleItems, key = { it.id }) { item ->
                            val isSelected = selectedItemIds.contains(item.id)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0xFFE5E7EB) else Color.White)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSelected) Color(0xFF9CA3AF) else PixelParchmentBorder,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onToggleItem(item.id) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        val boostsLowSatiety = curSatiety <= 30 && item.satietyBonus > 0
                                        val boostsLowMood = curMood <= 30 && item.moodBonus > 0
                                        val boostsLowHealth = curHealth <= 30 && item.healthBonus > 0
                                        val canBoostAnyLowStat = boostsLowSatiety || boostsLowMood || boostsLowHealth

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = item.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = if (isSelected) Color(0xFF374151) else PixelTextDark
                                            )
                                            if (canBoostAnyLowStat) {
                                                ExclamationIcon(modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CoinPriceTag(
                                                amount = item.price,
                                                fontSize = 14.sp,
                                                color = if (isSelected) Color(0xFF4B5563) else JoyOrange
                                            )
                                            if (item.satietyBonus > 0) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    if (boostsLowSatiety) {
                                                        ExclamationIcon(modifier = Modifier.size(12.dp))
                                                    }
                                                    Text("🍎 +${item.satietyBonus}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = JoyOrange)
                                                }
                                            }
                                            if (item.moodBonus > 0) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    if (boostsLowMood) {
                                                        ExclamationIcon(modifier = Modifier.size(12.dp))
                                                    }
                                                    LightningIcon(modifier = Modifier.size(13.dp))
                                                    Text("+${item.moodBonus}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SkyBlue)
                                                }
                                            }
                                            if (item.healthBonus > 0) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    if (boostsLowHealth) {
                                                        ExclamationIcon(modifier = Modifier.size(12.dp))
                                                    }
                                                    HeartIcon(modifier = Modifier.size(13.dp))
                                                    Text("+${item.healthBonus}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FreshGreen)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // флажок выбора: галочка в квадратике (шрифт увеличен на 3: 16 -> 19) или пустой черный квадратик
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) Color(0xFFF3F4F6) else Color.Transparent)
                                            .border(2.dp, Color(0xFF1F2937), RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Text(
                                                text = "✓",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 19.sp,
                                                color = Color(0xFF1F2937)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // кнопка быстрой покупки
                    val hasItems = selectedItems.isNotEmpty()

                    PixelButton(
                        text = if (hasItems) "Быстрая покупка (${selectedItems.size})" else "Выбери товары для покупки",
                        onClick = { if (hasItems) showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = hasItems,
                        containerColor = if (hasItems) PixelGreenCrop else Color(0xFFCCCCCC),
                        borderColor = if (hasItems) Color(0xFF2E7D32) else Color(0xFF999999),
                        textColor = if (hasItems) Color.White else Color(0xFF666666),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // диалог подтверждения быстрой покупки
    if (showConfirmDialog) {
        QuickBuyConfirmDialog(
            balance = balance,
            selectedItems = selectedItems,
            currentPeriod = currentPeriod,
            profile = profile,
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                onQuickBuy(selectedItems)
                showConfirmDialog = false
                onDismiss()
            }
        )
    }
}

// окно подтверждения быстрой покупки со всеми шрифтами увеличенными на 3
@Composable
fun QuickBuyConfirmDialog(
    balance: Int,
    selectedItems: List<PurchaseItem>,
    currentPeriod: PeriodEntity?,
    profile: ProfileEntity?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val totalCost = remember(selectedItems) { selectedItems.sumOf { it.price } }
    val totalSatiety = remember(selectedItems) { selectedItems.sumOf { it.satietyBonus } }
    val totalMood = remember(selectedItems) { selectedItems.sumOf { it.moodBonus } }
    val totalHealth = remember(selectedItems) { selectedItems.sumOf { it.healthBonus } }

    val addMandatory = remember(selectedItems) {
        selectedItems.filter { it.category == "MANDATORY" }.sumOf { it.price }
    }
    val addOptional = remember(selectedItems) {
        selectedItems.filter { it.category == "OPTIONAL" }.sumOf { it.price }
    }

    val actualMandatory = currentPeriod?.actualMandatory ?: 0
    val plannedMandatory = currentPeriod?.plannedMandatory ?: 0
    val actualOptional = currentPeriod?.actualOptional ?: 0
    val plannedOptional = currentPeriod?.plannedOptional ?: 0

    val curSatiety = profile?.satiety ?: 50
    val curMood = profile?.mood ?: 50
    val curHealth = profile?.health ?: 50

    val remainingBalance = balance - totalCost
    val isEnoughMoney = remainingBalance >= 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        KeepScreenImmersive()
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.94f)
                .padding(8.dp)
        ) {
            StardewBoard(
                headerTitle = "ПОДТВЕРЖДЕНИЕ ПОКУПКИ",
                headerIcon = "🛒"
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Будет куплено (${selectedItems.size}):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = PixelTextDark,
                                fontFamily = AppFontFamily
                            )
                        }

                        // список выбранных товаров с отдельными эффектами
                        items(selectedItems, key = { it.id }) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PixelParchmentMedium)
                                    .border(1.dp, PixelParchmentBorder, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = PixelTextDark
                                        )
                                        CoinPriceTag(
                                            amount = item.price,
                                            fontSize = 17.sp,
                                            color = JoyOrange
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (item.satietyBonus > 0) {
                                            Text("🍎 Сытость +${item.satietyBonus}", fontSize = 15.sp, color = JoyOrange, fontWeight = FontWeight.Bold)
                                        }
                                        if (item.moodBonus > 0) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                LightningIcon(modifier = Modifier.size(16.dp))
                                                Text("Настроение +${item.moodBonus}", fontSize = 15.sp, color = SkyBlue, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        if (item.healthBonus > 0) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                HeartIcon(modifier = Modifier.size(16.dp))
                                                Text("Здоровье +${item.healthBonus}", fontSize = 15.sp, color = FreshGreen, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // блок влияния на бюджет
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFFF8E1))
                                    .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Влияние на план бюджета:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = PixelTextDark,
                                        fontFamily = AppFontFamily
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (addMandatory > 0 || plannedMandatory > 0) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Обязательное: $actualMandatory/$plannedMandatory → ${actualMandatory + addMandatory}/$plannedMandatory ",
                                                fontSize = 16.sp,
                                                color = PixelTextDark,
                                                fontFamily = AppFontFamily
                                            )
                                            CoinIcon(modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    if (addOptional > 0 || plannedOptional > 0) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Желаемое: $actualOptional/$plannedOptional → ${actualOptional + addOptional}/$plannedOptional ",
                                                fontSize = 16.sp,
                                                color = PixelTextDark,
                                                fontFamily = AppFontFamily
                                            )
                                            CoinIcon(modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // блок улучшения статов питомца
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEDE7F6))
                                    .border(1.dp, Color(0xFFD1C4E9), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Показатели питомца после покупки:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = PixelTextDark,
                                        fontFamily = AppFontFamily
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🍎 Сытость: ${formatStatGain(curSatiety, totalSatiety)}",
                                        fontSize = 16.sp,
                                        color = JoyOrange,
                                        fontFamily = AppFontFamily
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        LightningIcon(modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "Настроение: ${formatStatGain(curMood, totalMood)}",
                                            fontSize = 16.sp,
                                            color = SkyBlue,
                                            fontFamily = AppFontFamily
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        HeartIcon(modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "Здоровье: ${formatStatGain(curHealth, totalHealth)}",
                                            fontSize = 16.sp,
                                            color = FreshGreen,
                                            fontFamily = AppFontFamily
                                        )
                                    }
                                }
                            }
                        }

                        // блок стоимости и остатка баланса (шрифты увеличены на 3)
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PixelParchmentLight)
                                    .border(1.dp, PixelParchmentBorder, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Полная цена:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PixelTextDark)
                                        CoinPriceTag(amount = totalCost, fontSize = 19.sp, color = JoyOrange)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Баланс сейчас:", fontSize = 17.sp, color = PixelTextDark)
                                        CoinPriceTag(amount = balance, fontSize = 18.sp, color = PixelTextDark)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Останется:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PixelTextDark)
                                        CoinPriceTag(
                                            amount = remainingBalance,
                                            fontSize = 19.sp,
                                            color = if (isEnoughMoney) FreshGreen else PixelRedBerry
                                        )
                                    }
                                    if (!isEnoughMoney) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Не хватает:", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PixelRedBerry)
                                            CoinPriceTag(amount = totalCost - balance, fontSize = 18.sp, color = PixelRedBerry)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // кнопки отмены и подтверждения покупки (шрифт увеличен на 3: 14 -> 17)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PixelButton(
                            text = "Отмена",
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            containerColor = PixelParchmentMedium,
                            borderColor = PixelParchmentBorder,
                            textColor = PixelTextDark,
                            fontSize = 17.sp
                        )

                        PixelButton(
                            text = if (isEnoughMoney) "Купить всё 🛒" else "Мало монет",
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            enabled = isEnoughMoney,
                            containerColor = if (isEnoughMoney) PixelGreenCrop else Color(0xFFCCCCCC),
                            borderColor = if (isEnoughMoney) Color(0xFF2E7D32) else Color(0xFF999999),
                            textColor = if (isEnoughMoney) Color.White else Color(0xFF666666),
                            fontSize = 17.sp
                        )
                    }
                }
            }
        }
    }
}
