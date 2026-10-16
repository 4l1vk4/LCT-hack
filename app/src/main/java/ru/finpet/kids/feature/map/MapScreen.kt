package ru.finpet.kids.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.R
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity
import ru.finpet.kids.core.data.repository.PurchaseItem
import ru.finpet.kids.core.data.repository.QuestItem
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.feature.map.sheets.HomeGoalsBottomSheet
import ru.finpet.kids.feature.map.sheets.SchoolBottomSheet
import ru.finpet.kids.feature.map.sheets.ShopBottomSheet

// флаг для отладки: если включить true, на карте подсвечиваются кликабельные прямоугольники
private const val DEBUG_HOTSPOTS = false

// главный экран карты города: кликаем по зданиям (лавка, школа, дом) и открываем шторки
@Composable
fun MapScreen(
    profile: ProfileEntity?,
    purchases: List<PurchaseItem>,
    quests: List<QuestItem>,
    questProgress: List<QuestProgressEntity>,
    goals: List<GoalEntity>,
    todayNotes: List<CalendarNoteEntity> = emptyList(),
    initialModal: String? = null,
    targetGoalId: String? = null,
    targetShopItemId: String? = null,
    targetQuestId: String? = null,
    isNight: Boolean = false,
    demoMode: Boolean = false,
    onModalDismissed: () -> Unit = {},
    onBuyItem: (PurchaseItem) -> Unit,
    onAnswerQuest: (questId: String, optionId: String) -> Unit,
    onDepositGoal: (goalId: String, amount: Int) -> Unit,
    onWithdrawGoal: (goalId: String, amount: Int) -> Unit,
    onSelectActiveGoal: (goalId: String) -> Unit = {},
    onCompleteHomeChore: (choreId: String, rewardCoins: Int) -> Unit = { _, _ -> },
    onNavigateToFinik: () -> Unit = {}
) {
    // какая локация сейчас открыта: "SHOP", "QUESTS" или "GOALS"
    var activeModal by remember(initialModal) { mutableStateOf<String?>(initialModal) }
    var showNightDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(initialModal, isNight) {
        if (initialModal != null) {
            if (isNight) {
                showNightDialog = initialModal
                activeModal = null
                onModalDismissed()
            } else {
                activeModal = initialModal
            }
        }
    }
    var notEnoughMoneyInfo by remember { mutableStateOf<Pair<Int, Int>?>(null) } // сколько не хватает, цена
    var questFeedback by remember { mutableStateOf<String?>(null) }
    var showNoPetDialog by remember { mutableStateOf(false) }

    val balance = profile?.balance ?: 50
    val petName = profile?.petName ?: "Финни"

    val mapRes = if (isNight) R.drawable.night else R.drawable.day
    val mapAspectRatio = 1073f / 3248f
    val mapBgColor = if (isNight) Color(0xFF0D0D1A) else Color(0xFF67BDEF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mapBgColor)
    ) {
        MapWithHotspots(
            mapRes = mapRes,
            imageAspectRatio = mapAspectRatio,
            hotspots = DEFAULT_MAP_HOTSPOTS,
            debug = DEBUG_HOTSPOTS,
            onClickHotspot = { spot ->
                when (spot.id) {
                    "shop" -> {
                        if (isNight) {
                            showNightDialog = "SHOP"
                        } else if (profile?.isPetRunaway == true && !demoMode) {
                            // проверка состояния сбежавшего кота в обычном режиме
                            showNoPetDialog = true
                        } else {
                            activeModal = "SHOP"
                        }
                    }
                    "quests" -> {
                        if (isNight) {
                            showNightDialog = "QUESTS"
                        } else {
                            activeModal = "QUESTS"
                        }
                    }
                    "home", "bank" -> {
                        if (isNight) {
                            showNightDialog = "BANK"
                        } else {
                            activeModal = "GOALS"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ночной индикатор режима (22:00 – 06:00)
        if (isNight) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(Color(0xDD121324), RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "🌙", fontSize = 14.sp)
                    Text(
                        text = "Ночь в городе (22:00 – 06:00)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // шторка школы финграмотности (квесты и словарик)
    if (activeModal == "QUESTS") {
        SchoolBottomSheet(
            quests = quests,
            progressList = questProgress,
            petName = petName,
            targetQuestId = targetQuestId,
            onDismiss = {
                activeModal = null
                onModalDismissed()
            },
            onSelectOption = { questId, optionId ->
                val quest = quests.find { it.id == questId }
                val opt = quest?.options?.find { it.id == optionId }
                onAnswerQuest(questId, optionId)
                questFeedback = opt?.feedback ?: "Молодец!"
            }
        )
    }

    // шторка магазина товаров (еда, вода, игрушки)
    if (activeModal == "SHOP") {
        ShopBottomSheet(
            balance = balance,
            items = purchases,
            todayNotes = todayNotes,
            targetItemId = targetShopItemId,
            onDismiss = {
                activeModal = null
                onModalDismissed()
            },
            onBuy = { item ->
                if (balance < item.price) {
                    notEnoughMoneyInfo = (item.price - balance) to item.price
                } else {
                    onBuyItem(item)
                }
            }
        )
    }

    // шторка дома и копилки (цели на мечту и домашние дела)
    if (activeModal == "GOALS") {
        HomeGoalsBottomSheet(
            balance = balance,
            goals = goals,
            activeGoalId = profile?.activeGoalId,
            currentPeriodIndex = profile?.currentPeriodIndex ?: 1,
            questProgress = questProgress,
            onSelectActiveGoal = onSelectActiveGoal,
            onCompleteHomeChore = onCompleteHomeChore,
            initialPiggyBankExpanded = (initialModal == "GOALS"),
            targetGoalId = targetGoalId,
            onDismiss = {
                activeModal = null
                onModalDismissed()
            },
            onDeposit = { goalId, amount ->
                if (balance >= amount) {
                    onDepositGoal(goalId, amount)
                }
            },
            onWithdraw = { goalId, amount ->
                onWithdrawGoal(goalId, amount)
            }
        )
    }

    // диалог нехватки монет: подсказываем ребенку заглянуть в школу
    notEnoughMoneyInfo?.let { (missing, price) ->
        AlertDialog(
            onDismissRequest = { notEnoughMoneyInfo = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoinIcon(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Ой, не хватает монет!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Text(
                    text = "Тебе не хватает $missing монет для покупки за $price.\n\nЗагляни в Школу финансовой грамотности и реши задание, чтобы заработать монет!",
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        notEnoughMoneyInfo = null
                        activeModal = "QUESTS"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JoyOrange)
                ) {
                    Text("Идти в Школу 🏫")
                }
            },
            dismissButton = {
                TextButton(onClick = { notEnoughMoneyInfo = null }) {
                    Text("Понятно")
                }
            }
        )
    }

    // предупреждение: питомец убежал
    if (showNoPetDialog) {
        AlertDialog(
            onDismissRequest = { showNoPetDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "😿", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Продукты никому не нужны",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Text(
                    text = "Кот убежал, и сейчас его нет дома. Продукты никому не нужны.\n\nПоставь миску корма у двери — и он скоро вернётся!",
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showNoPetDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = JoyOrange)
                ) {
                    Text("Понятно")
                }
            }
        )
    }

    // обратная связь после ответа на квест
    questFeedback?.let { feedback ->
        AlertDialog(
            onDismissRequest = { questFeedback = null },
            title = { Text(text = "💡 Совет от $petName", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Text(
                    text = feedback,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { questFeedback = null },
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                ) {
                    Text("Спасибо, понятно! 👍")
                }
            }
        )
    }

    // предупреждение: здания закрыты ночью
    showNightDialog?.let { target ->
        val isShop = target == "SHOP"
        val isQuests = target == "QUESTS"
        AlertDialog(
            onDismissRequest = { showNightDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🌙", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isShop -> "Лавка закрыта"
                            isQuests -> "Школа закрыта"
                            else -> "Банк закрыт"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Text(
                    text = when {
                        isShop -> "Ночью (с 22:00 до 06:00) лавка закрыта. Продавец и питомец спят 💤\n\nПриходи за покупками утром с 6:00!"
                        isQuests -> "Ночью (с 22:00 до 06:00) школа закрыта. Все уроки и финансовые квесты начнутся утром с 6:00 💤\n\nОтдыхай и набирайся сил перед новым днем!"
                        else -> "Ночью (с 22:00 до 06:00) банк и копилка закрыты. Все операции с накоплениями будут доступны утром с 6:00 💤\n\nГород спит, отдыхай до утра!"
                    },
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showNightDialog = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isShop -> JoyOrange
                            isQuests -> FreshGreen
                            else -> Color(0xFF1E88E5)
                        }
                    )
                ) {
                    Text(
                        "Понятно 👍",
                        fontSize = 18.sp
                    )
                }
            }
        )
    }
}
