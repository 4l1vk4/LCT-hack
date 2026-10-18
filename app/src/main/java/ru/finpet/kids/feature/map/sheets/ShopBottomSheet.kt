package ru.finpet.kids.feature.map.sheets

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
import ru.finpet.kids.R
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.repository.PurchaseItem
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.AppleIcon
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.HeartIcon
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.LightningIcon
import ru.finpet.kids.core.designsystem.LockIcon
import ru.finpet.kids.core.designsystem.PixelAlertDialog
import ru.finpet.kids.core.designsystem.PixelBar
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.core.designsystem.WarningIcon
import ru.finpet.kids.core.designsystem.pixelBarBackground
import ru.finpet.kids.core.designsystem.pixelButtonBackground
import ru.finpet.kids.feature.map.components.PixelLocationModalSheet
import ru.finpet.kids.feature.map.sheets.components.CategoryChip
import androidx.compose.foundation.ExperimentalFoundationApi
import ru.finpet.kids.feature.map.sheets.components.ShopItemCard

/**
 * Шторка магазина товаров:
 * - Покупка корма, воды, игрушек для питомца.
 * - Поручение родителей «Купить продукты по списку» со сдачей.
 * - Быстрая покупка товаров из составленного плана.
 * - Оформление в пиксельном фермерском стиле Stardew Valley.
 */
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
    onDismiss: () -> Unit,
    onNavigateToHouse: () -> Unit = {},
    onNavigateToSchool: () -> Unit = {}
) {

    var selectedCategory by remember { mutableStateOf("ALL") }
    var itemToConfirm by remember { mutableStateOf<PurchaseItem?>(null) }
    var showPreorderDialog by remember { mutableStateOf(false) }
    var showQuickBuyConfirm by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    val isRunaway = profile?.isPetRunaway == true

    // Кнопка плана предзакупок доступна в демо-режиме, на взрослой стадии (спустя 7 дней) или если план уже сформирован
    val isAdultPlus7 = run {
        val isAdult = profile?.growthStage == "ADULT"
        val curDay = profile?.currentPeriodIndex ?: 1
        isAdult && adultStageStartDay > 0 && (curDay - adultStageStartDay >= 7)
    }
    val showPlanButton = demoMode || isAdultPlus7 || preorderItemIds.isNotEmpty()

    // Скролл к целевому товару при переходе из задачи дня
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
                Box(
                    modifier = Modifier
                        .pixelBarBackground()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showPreorderDialog = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "📋 Список покупок",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextDark
                    )
                }
            }
        } else null
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Быстрая покупка товаров из плана
            if (preorderItemIds.isNotEmpty()) {
                val selectedItems = remember(items, preorderItemIds) {
                    items.filter { preorderItemIds.contains(it.id) && it.id != "groceries_parents" && it.category != "GROCERIES" }
                }
                val totalCost = selectedItems.sumOf { it.price }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pixelBarBackground()
                        .then(
                            if (!isRunaway) Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showQuickBuyConfirm = true } else Modifier
                        )
                        .padding(vertical = 10.dp, horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Быстрая покупка (${selectedItems.size}) • $totalCost ",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PixelTextDark
                        )
                        CoinIcon(modifier = Modifier.size(19.dp))
                    }
                }
            }

            // Предупреждение о побеге питомца
            if (isRunaway) {
                PixelPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LockIcon(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Лавка закрыта: ${profile?.petName ?: "Кот"} убежал! Поставь миску корма у двери в комнате котика, чтобы вернуть его.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828),
                            textAlign = TextAlign.Center,
                            lineHeight = 21.sp
                        )
                    }
                }
            }

            // Панель баланса кошелька в пиксельном стиле кнопочного 9-slice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pixelButtonBackground(containerColor = Color(0xFFFFF9EE))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "В кошельке: $balance ",
                    fontFamily = AppFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF4A2810)
                )
                CoinIcon(modifier = Modifier.size(20.dp))
            }

            // Фильтры категорий
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryChip("Все", selectedCategory == "ALL") { selectedCategory = "ALL" }
                CategoryChip("Обязательное", selectedCategory == "MANDATORY") { selectedCategory = "MANDATORY" }
                CategoryChip("Желаемое", selectedCategory == "OPTIONAL") { selectedCategory = "OPTIONAL" }
            }

            // Список товаров лавки
            filteredItems.forEach { item ->
                val isTarget = targetItemId != null && item.id == targetItemId
                ShopItemCard(
                    item = item,
                    profile = profile,
                    isRunaway = isRunaway,
                    todayNotes = todayNotes,
                    isTarget = isTarget,
                    bringIntoViewRequester = bringIntoViewRequester,
                    onBuyClick = { itemToConfirm = it }
                )
            }
        }
    }

    // Диалог подтверждения покупки
    itemToConfirm?.let { item ->
        val curSatiety = profile?.satiety ?: 50
        val curMood = profile?.mood ?: 50
        val curHealth = profile?.health ?: 50

        val remainingMandatory = if (currentPeriod != null && currentPeriod.isBudgetConfirmed) {
            maxOf(0, currentPeriod.plannedMandatory - currentPeriod.actualMandatory)
        } else 0
        val remainingOptional = if (currentPeriod != null && currentPeriod.isBudgetConfirmed) {
            maxOf(0, currentPeriod.plannedOptional - currentPeriod.actualOptional)
        } else 0
        val totalJarsRemaining = remainingMandatory + remainingOptional
        val freeCoins = maxOf(0, balance - totalJarsRemaining)

        val isEnoughCoins = balance >= item.price
        val missingCoins = if (isEnoughCoins) 0 else (item.price - balance)

        // Для обязательных товаров (MANDATORY):
        // Сначала списываем из банки «Обязательное», если не хватает — в первую очередь берем из «Желаемое»,
        // и только если и в «Желаемое» монет не хватило (или там 0) — остаток берем из свободных денег!
        val isMandatoryShortage = item.category == "MANDATORY" && currentPeriod != null &&
                currentPeriod.isBudgetConfirmed && (item.price > remainingMandatory)
        val missingMandatory = if (isMandatoryShortage) item.price - remainingMandatory else 0
        val takeFromOptional = if (isMandatoryShortage) minOf(missingMandatory, remainingOptional) else 0
        val takeFromFreeMandatory = if (isMandatoryShortage) missingMandatory - takeFromOptional else 0

        // Для желаемых товаров (OPTIONAL):
        // Списываем из банки «Желаемое», а недостающее — из свободных денег!
        val isOptionalShortage = item.category == "OPTIONAL" && currentPeriod != null &&
                currentPeriod.isBudgetConfirmed && (item.price > remainingOptional)
        val missingOptional = if (isOptionalShortage) item.price - remainingOptional else 0
        val takeFromFreeOptional = missingOptional

        val confirmButtonText = when {
            item.id == "groceries_parents" -> "Выполнить 🛒"
            isMandatoryShortage && takeFromOptional > 0 -> "Взять из желаемого и купить 🛒"
            else -> "Купить 🛒"
        }

        PixelAlertDialog(
            onDismissRequest = { itemToConfirm = null },
            title = when {
                item.id == "groceries_parents" -> "Поручение родителей"
                !isEnoughCoins -> "Не хватает монет"
                else -> "Покупка"
            },
            icon = if (!isEnoughCoins) "!" else "🛒",
            confirmButton = {
                if (isEnoughCoins) {
                    PixelButton(
                        text = confirmButtonText,
                        onClick = {
                            onBuy(item)
                            itemToConfirm = null
                        },
                        enabled = !isRunaway,
                        containerColor = if (isMandatoryShortage && takeFromOptional > 0) JoyOrange else FreshGreen,
                        textColor = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    PixelButton(
                        text = "Закрыть",
                        onClick = { itemToConfirm = null },
                        containerColor = PixelWoodMedium,
                        textColor = PixelParchmentLight,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            dismissButton = if (isEnoughCoins) {
                {
                    PixelButton(
                        text = "Отмена",
                        onClick = { itemToConfirm = null },
                        containerColor = PixelWoodMedium,
                        textColor = PixelParchmentLight,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else null
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (item.id == "groceries_parents") {
                    Text(
                        text = "Родители дают деньги на покупки для дома. Тебе платить ничего не нужно, а вся сдача (5, 10 или 15) с чека попадёт прямо в твой кошелёк!",
                        fontFamily = AppFontFamily,
                        fontSize = 16.sp,
                        color = TextSecondary,
                        lineHeight = 21.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "💰 Награда: сдача 5, 10 или 15 ",
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = FreshGreen
                        )
                        CoinIcon(modifier = Modifier.size(20.dp))
                    }
                } else {
                    Text(
                        text = item.description,
                        fontFamily = AppFontFamily,
                        fontSize = 16.sp,
                        color = TextSecondary,
                        lineHeight = 21.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Цена: ", fontFamily = AppFontFamily, fontSize = 17.sp, color = TextPrimary)
                        Text(
                            text = "${item.price} ",
                            fontFamily = AppFontFamily,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = JoyOrange
                        )
                        CoinIcon(modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    val remaining = balance - item.price
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "В кошельке: $balance монет",
                            fontFamily = AppFontFamily,
                            fontSize = 16.sp,
                            color = TextSecondary
                        )
                    }

                    if (!isEnoughCoins) {
                        // Не хватает монет: выводим предупреждение и кнопки перехода в Домик и Школу
                        Spacer(modifier = Modifier.height(10.dp))
                        PixelPanel(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                WarningIcon(modifier = Modifier.size(34.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Не хватает $missingCoins монет!",
                                        fontFamily = AppFontFamily,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PixelRedBerry
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Тебе не хватает $missingCoins монет для этой покупки. Заработай их за полезные дела в домике или за решение задачек в школе!",
                                        fontFamily = AppFontFamily,
                                        fontSize = 15.sp,
                                        color = PixelRedBerry,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PixelButton(
                                text = "🏠 В Домик",
                                onClick = {
                                    itemToConfirm = null
                                    onDismiss()
                                    onNavigateToHouse()
                                },
                                containerColor = JoyOrange,
                                textColor = PixelTextDark,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                            PixelButton(
                                text = "🎓 В Школу",
                                onClick = {
                                    itemToConfirm = null
                                    onDismiss()
                                    onNavigateToSchool()
                                },
                                containerColor = FreshGreen,
                                textColor = Color.White,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        // Монет хватает (хватает в банках либо используются свободные деньги)
                        if (isMandatoryShortage) {
                            Spacer(modifier = Modifier.height(10.dp))
                            if (takeFromOptional > 0 && takeFromFreeMandatory == 0) {
                                // Все недостающие монеты берутся из банки «Желаемое»
                                PixelPanel(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        WarningIcon(modifier = Modifier.size(34.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Не хватает в банке «Обязательное»!",
                                                fontFamily = AppFontFamily,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFD84315)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val jarInfo = if (remainingMandatory > 0) {
                                                "В банке «Обязательное» осталось $remainingMandatory монет."
                                            } else {
                                                "В банке «Обязательное» нет монет."
                                            }
                                            Text(
                                                text = "$jarInfo Недостающие $takeFromOptional монет будут взяты из банки «Желаемое», что создаст перерасход («План vs Факт»).",
                                                fontFamily = AppFontFamily,
                                                fontSize = 14.sp,
                                                color = Color(0xFFE65100),
                                                lineHeight = 19.sp
                                            )
                                        }
                                    }
                                }
                            } else if (takeFromOptional > 0 && takeFromFreeMandatory > 0) {
                                // Часть берется из «Желаемое» (опустошая ее), а остаток — из свободных денег
                                PixelPanel(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        WarningIcon(modifier = Modifier.size(34.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Взятие из желаемого и свободных денег",
                                                fontFamily = AppFontFamily,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFD84315)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val jarInfo = if (remainingMandatory > 0) "В обязательной банке осталось $remainingMandatory монет. " else "В обязательной банке нет монет. "
                                            Text(
                                                text = "${jarInfo}Из банки «Желаемое» будет взято $takeFromOptional монет (в ней больше нет монет), а остальные $takeFromFreeMandatory монет оплатятся свободными деньгами!",
                                                fontFamily = AppFontFamily,
                                                fontSize = 14.sp,
                                                color = Color(0xFFE65100),
                                                lineHeight = 19.sp
                                            )
                                        }
                                    }
                                }
                            } else {
                                // takeFromOptional == 0: в банке «Желаемое» 0 монет, вся нехватка из свободных денег
                                PixelPanel(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        CoinIcon(modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Оплата свободными деньгами",
                                                fontFamily = AppFontFamily,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFB26A00)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val mandInfo = if (remainingMandatory > 0) {
                                                "В банке «Обязательное» осталось $remainingMandatory монет. "
                                            } else {
                                                "В банке «Обязательное» нет монет. "
                                            }
                                            Text(
                                                text = "${mandInfo}В банке «Желаемое» монет тоже нет, поэтому недостающие $takeFromFreeMandatory монет полностью оплачиваются свободными деньгами (за дела и игры)!",
                                                fontFamily = AppFontFamily,
                                                fontSize = 14.sp,
                                                color = PixelTextDark,
                                                lineHeight = 19.sp
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (isOptionalShortage) {
                            Spacer(modifier = Modifier.height(10.dp))
                            PixelPanel(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    CoinIcon(modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Оплата свободными деньгами",
                                            fontFamily = AppFontFamily,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFB26A00)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val jarText = if (remainingOptional > 0) {
                                            "В банке «Желаемое» осталось $remainingOptional монет. Недостающие $takeFromFreeOptional монет будут оплачены свободными деньгами, заработанными за полезные дела и игры (без штрафа за перерасход)!"
                                        } else {
                                            "В банке «Желаемое» нет монет, но покупка полностью оплачивается свободными деньгами, заработанными за полезные дела и игры (без штрафа за перерасход)!"
                                        }
                                        Text(
                                            text = jarText,
                                            fontFamily = AppFontFamily,
                                            fontSize = 14.sp,
                                            color = PixelTextDark,
                                            lineHeight = 19.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (item.satietyBonus > 0 || item.moodBonus > 0 || item.healthBonus > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Показатели питомца после покупки:",
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (item.satietyBonus > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppleIcon(modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Сытость: ${formatStatGain(curSatiety, item.satietyBonus)}",
                                    fontFamily = AppFontFamily,
                                    fontSize = 16.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        if (item.moodBonus > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LightningIcon(modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Настроение: ${formatStatGain(curMood, item.moodBonus)}",
                                    fontFamily = AppFontFamily,
                                    fontSize = 16.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        if (item.healthBonus > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                HeartIcon(modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Здоровье: ${formatStatGain(curHealth, item.healthBonus)}",
                                    fontFamily = AppFontFamily,
                                    fontSize = 16.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог плана предзакупок
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

    // Диалог подтверждения быстрой покупки прямо из лавки
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
