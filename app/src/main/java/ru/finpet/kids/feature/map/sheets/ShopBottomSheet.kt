package ru.finpet.kids.feature.map.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.finpet.kids.R
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.repository.PurchaseItem
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.ExclamationIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.HeartIcon
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.LightningIcon
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.SkyBlue
import ru.finpet.kids.core.designsystem.WarningIcon
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.feature.map.components.PixelLocationModalSheet

// шторка магазина товаров: еда, вода, игрушки и поручение купить продукты
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShopBottomSheet(
    balance: Int,
    items: List<PurchaseItem>,
    todayNotes: List<CalendarNoteEntity> = emptyList(),
    targetItemId: String? = null,
    preorderItemIds: Set<String> = emptySet(),
    currentPeriod: PeriodEntity? = null,
    profile: ProfileEntity? = null,
    demoMode: Boolean = false,
    adultStageStartDay: Int = 0,
    onTogglePreorderItem: (String) -> Unit = {},
    onQuickBuy: (List<PurchaseItem>) -> Unit = {},
    onBuy: (PurchaseItem) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    var itemToConfirm by remember { mutableStateOf<PurchaseItem?>(null) }
    var showPreorderDialog by remember { mutableStateOf(false) }
    var showQuickBuyConfirm by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    val isRunaway = profile?.isPetRunaway == true

    // кнопка плана доступна только если включен демо режим либо питомец взрослый и прошло 7 дней или в плане уже что-то есть
    val isAdultPlus7 = run {
        val isAdult = profile?.growthStage == "ADULT"
        val curDay = profile?.currentPeriodIndex ?: 1
        isAdult && adultStageStartDay > 0 && (curDay - adultStageStartDay >= 7)
    }
    val showPlanButton = demoMode || isAdultPlus7 || preorderItemIds.isNotEmpty()

    // скроллим к целевому товару при переходе из задачи дня
    LaunchedEffect(targetItemId) {
        if (targetItemId != null) {
            selectedCategory = "ALL"
            delay(200)
            try {
                bringIntoViewRequester.bringIntoView()
            } catch (_: Exception) {
                listState.animateScrollToItem(1)
            }
        }
    }

    val filteredItems = remember(items, selectedCategory) {
        when (selectedCategory) {
            "MANDATORY" -> items.filter { it.category == "MANDATORY" }
            "OPTIONAL" -> items.filter { it.category == "OPTIONAL" }
            else -> items
        }
    }

    PixelLocationModalSheet(
        title = "🏪 Лавка товаров",
        subtitle = "Полезный корм, игрушки и поручения родителей",
        headerDrawableRes = R.drawable.location_shop_header,
        badgeText = "$balance монет",
        onDismiss = onDismiss,
        listState = listState,
        topStartExtra = if (showPlanButton) {
            {
                Surface(
                    onClick = { showPreorderDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF6D4C41),
                    border = BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "📋 Список покупок",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        } else null
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // быстрая покупка прямо под заголовком "Лавка товаров", появляется только если план составлен
            if (preorderItemIds.isNotEmpty()) {
                val selectedItems = remember(items, preorderItemIds) {
                    items.filter { preorderItemIds.contains(it.id) && it.id != "groceries_parents" && it.category != "GROCERIES" }
                }
                val totalCost = selectedItems.sumOf { it.price }

                Button(
                    onClick = { showQuickBuyConfirm = true },
                    enabled = !isRunaway,
                    colors = ButtonDefaults.buttonColors(containerColor = PixelGreenCrop),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 11.dp, horizontal = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Быстрая покупка (${selectedItems.size}) • ",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$totalCost ",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        CoinIcon(modifier = Modifier.size(19.dp))
                    }
                }
            }

            // предупреждение если питомец сбежал
            if (isRunaway) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFEBEE),
                    border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 Лавка закрыта: ${profile?.petName ?: "Кот"} убежал! Поставь миску корма у двери, чтобы вернуть его.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // текущий баланс монет
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFF3E0),
                border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "В кошельке: $balance ",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    CoinIcon(modifier = Modifier.size(21.dp))
                }
            }

            // фильтрация: все / обязательные / желаемые
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryChip("Все", selectedCategory == "ALL") { selectedCategory = "ALL" }
                CategoryChip("Обязательное 🥣", selectedCategory == "MANDATORY") { selectedCategory = "MANDATORY" }
                CategoryChip("Желаемое 🎈", selectedCategory == "OPTIONAL") { selectedCategory = "OPTIONAL" }
            }

            // список товаров лавки
            filteredItems.forEach { item ->
                val isGroceriesItem = item.id == "groceries_parents"
                val groceryTask = if (isGroceriesItem) todayNotes.find { it.category == "GROCERIES" } else null
                val isGroceriesCompleted = groceryTask?.isCompleted == true
                val isGroceriesUnavailable = isGroceriesItem && groceryTask == null
                val isTarget = targetItemId != null && item.id == targetItemId

                FinCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isTarget) Modifier.bringIntoViewRequester(bringIntoViewRequester) else Modifier),
                    backgroundColor = if (item.category == "MANDATORY") Color(0xFFF1F8E9) else Color(0xFFF3E5F5),
                    borderColor = if (isTarget) JoyOrange else if (item.category == "MANDATORY") Color(0xFFA5D6A7) else Color(0xFFCE93D8)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val curSatiety = profile?.satiety ?: 100
                            val curMood = profile?.mood ?: 100
                            val curHealth = profile?.health ?: 100

                            val boostsLowSatiety = curSatiety <= 30 && item.satietyBonus > 0
                            val boostsLowMood = curMood <= 30 && item.moodBonus > 0
                            val boostsLowHealth = curHealth <= 30 && item.healthBonus > 0
                            val canBoostAnyLowStat = boostsLowSatiety || boostsLowMood || boostsLowHealth

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (canBoostAnyLowStat) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    ExclamationIcon(modifier = Modifier.size(18.dp))
                                }
                                if (isGroceriesCompleted) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFDCEDC8)
                                    ) {
                                        Text(
                                            text = "Выполнено",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FreshGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = when {
                                    isGroceriesCompleted -> "Поручение родителей выполнено! Сдача +${groceryTask?.cost ?: 0} уже в кошельке."
                                    isGroceriesUnavailable -> "Родители сегодня не давали поручение на покупки. Загляни во Вт, Чт или Сб!"
                                    else -> item.description
                                },
                                fontSize = 17.sp,
                                color = TextSecondary,
                                lineHeight = 21.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (item.satietyBonus > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (boostsLowSatiety) {
                                            ExclamationIcon(modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                        }
                                        Text("🍎 +${item.satietyBonus}", fontSize = 16.sp, color = JoyOrange, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (item.moodBonus > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (boostsLowMood) {
                                            ExclamationIcon(modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                        }
                                        LightningIcon(modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("+${item.moodBonus}", fontSize = 16.sp, color = SkyBlue, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (item.healthBonus > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (boostsLowHealth) {
                                            ExclamationIcon(modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                        }
                                        HeartIcon(modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("+${item.healthBonus}", fontSize = 16.sp, color = FreshGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (isGroceriesItem && !isGroceriesCompleted && !isGroceriesUnavailable) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CoinIcon(modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Сдача: 5, 10 или 15", fontSize = 16.sp, color = FreshGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        when {
                            isRunaway -> {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF5F5F5),
                                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                ) {
                                    Text(
                                        text = "🔒 Закрыто",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    )
                                }
                            }
                            isGroceriesCompleted -> {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFE8F5E9),
                                    border = BorderStroke(1.dp, Color(0xFFAED581))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        if ((groceryTask?.cost ?: 0) > 0) {
                                            Text(
                                                text = "✅ +${groceryTask?.cost} ",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = FreshGreen
                                            )
                                            CoinIcon(modifier = Modifier.size(16.dp))
                                        } else {
                                            Text(
                                                text = "✅ Готово",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = FreshGreen
                                            )
                                        }
                                    }
                                }
                            }
                            isGroceriesUnavailable -> {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF5F5F5),
                                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                                ) {
                                    Text(
                                        text = "🔒 Нет задания",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    )
                                }
                            }
                            else -> {
                                Button(
                                    onClick = { itemToConfirm = item },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isGroceriesItem) FreshGreen else JoyOrange
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    if (isGroceriesItem) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("0 ", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                            CoinIcon(modifier = Modifier.size(16.dp))
                                            Text(" • Сдача!", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                        }
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("${item.price} ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                            CoinIcon(modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // диалог подтверждения перед обычной покупкой со всеми увеличенными шрифтами на 3
    itemToConfirm?.let { item ->
        val curSatiety = profile?.satiety ?: 50
        val curMood = profile?.mood ?: 50
        val curHealth = profile?.health ?: 50

        AlertDialog(
            onDismissRequest = { itemToConfirm = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (item.id == "groceries_parents") "Выполнить поручение родителей?" else "Купить «${item.name}»?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    )
                }
            },
            text = {
                Column {
                    if (item.id == "groceries_parents") {
                        Text(
                            text = "Родители дают деньги на покупки для дома. Тебе платить ничего не нужно, а вся сдача (5, 10 или 15) с чека попадёт прямо в твой кошелёк!",
                            fontSize = 21.sp,
                            color = TextSecondary,
                            lineHeight = 27.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "💰 Награда: сдача 5, 10 или 15 ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                color = FreshGreen
                            )
                            CoinIcon(modifier = Modifier.size(23.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WarningIcon(modifier = Modifier.size(19.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Поручение можно выполнить только 1 раз за день!",
                                fontWeight = FontWeight.Medium,
                                fontSize = 19.sp,
                                color = JoyOrange
                            )
                        }
                    } else {
                        Text(
                            text = item.description,
                            fontSize = 21.sp,
                            color = TextSecondary,
                            lineHeight = 27.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // цена с иконкой монеты
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Цена: ", fontSize = 22.sp, color = TextPrimary)
                            Text(
                                text = "${item.price} ",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = JoyOrange
                            )
                            CoinIcon(modifier = Modifier.size(26.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        // сколько останется после покупки
                        val remaining = balance - item.price
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Останется: $remaining ",
                                fontSize = 20.sp,
                                color = if (remaining >= 0) FreshGreen else Color(0xFFD32F2F),
                                fontWeight = FontWeight.SemiBold
                            )
                            CoinIcon(modifier = Modifier.size(22.dp))
                        }

                        // информирование по тому, что дается коту по статам (0% если превышает)
                        if (item.satietyBonus > 0 || item.moodBonus > 0 || item.healthBonus > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Показатели питомца после покупки:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (item.satietyBonus > 0) {
                                Text(
                                    text = "🍎 Сытость: ${formatStatGain(curSatiety, item.satietyBonus)}",
                                    fontSize = 18.sp,
                                    color = TextSecondary
                                )
                            }
                            if (item.moodBonus > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LightningIcon(modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Настроение: ${formatStatGain(curMood, item.moodBonus)}",
                                        fontSize = 18.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            if (item.healthBonus > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    HeartIcon(modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Здоровье: ${formatStatGain(curHealth, item.healthBonus)}",
                                        fontSize = 18.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBuy(item)
                        itemToConfirm = null
                    },
                    enabled = !isRunaway,
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                ) {
                    Text(
                        text = if (item.id == "groceries_parents") "Выполнить поручение 🛒" else "Купить 🛒",
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToConfirm = null }) {
                    Text("Отмена", fontSize = 21.sp)
                }
            }
        )
    }

    // диалог плана предзакупок
    if (showPreorderDialog) {
        PreorderPlanDialog(
            balance = balance,
            items = items,
            selectedItemIds = preorderItemIds,
            currentPeriod = currentPeriod,
            profile = profile,
            onToggleItem = onTogglePreorderItem,
            onQuickBuy = onQuickBuy,
            onDismiss = { showPreorderDialog = false }
        )
    }

    // диалог подтверждения быстрой покупки прямо из лавки
    if (showQuickBuyConfirm) {
        val selectedItems = remember(items, preorderItemIds) {
            items.filter { preorderItemIds.contains(it.id) && it.id != "groceries_parents" && it.category != "GROCERIES" }
        }
        QuickBuyConfirmDialog(
            balance = balance,
            selectedItems = selectedItems,
            currentPeriod = currentPeriod,
            profile = profile,
            onDismiss = { showQuickBuyConfirm = false },
            onConfirm = {
                onQuickBuy(selectedItems)
                showQuickBuyConfirm = false
            }
        )
    }
}

// чип фильтрации категорий товаров
@Composable
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) JoyOrange else Color.White,
        border = BorderStroke(1.5.dp, if (isSelected) JoyOrange else Color(0xFFE0E0E0))
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
