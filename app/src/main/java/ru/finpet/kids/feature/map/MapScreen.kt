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
import androidx.compose.material3.Text
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
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.PixelAlertDialog
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.feature.map.sheets.HomeGoalsBottomSheet
import ru.finpet.kids.feature.map.sheets.SchoolBottomSheet
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.feature.map.sheets.ShopBottomSheet

// флаг для отладки: если включить true, на карте подсвечиваются кликабельные прямоугольники
private const val DEBUG_HOTSPOTS = false

// главный экран карты города: кликаем по зданиям (лавка, школа, дом) и открываем шторки
@Composable
fun MapScreen(
    profile: ProfileEntity?,
    currentPeriod: PeriodEntity? = null,
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
    adultStageStartDay: Int = 0,
    preorderItemIds: Set<String> = emptySet(),
    onTogglePreorderItem: (String) -> Unit = {},
    onQuickBuyItems: (List<PurchaseItem>) -> Unit = {},
    onModalDismissed: () -> Unit = {},
    onBuyItem: (PurchaseItem) -> Unit,
    onAnswerQuest: (questId: String, optionId: String) -> Unit,
    onAnswerQuestText: (questId: String, answer: String) -> Unit = { _, _ -> },
    onAnswerQuestAllocation: (questId: String, amount: Int) -> Unit = { _, _ -> },
    onDepositGoal: (goalId: String, amount: Int) -> Unit,
    onWithdrawGoal: (goalId: String, amount: Int) -> Unit,
    onSelectActiveGoal: (goalId: String) -> Unit = {},
    onCompleteHomeChore: (choreId: String, rewardCoins: Int) -> Unit = { _, _ -> },
    onNavigateToFinik: () -> Unit = {}
) {
    // какая локация сейчас открыта: "SHOP", "QUESTS" или "GOALS"
    var activeModal by remember(initialModal) { mutableStateOf<String?>(initialModal) }
    var focusOnHomeChores by remember { mutableStateOf(false) }
    var notEnoughMoneyInfo by remember { mutableStateOf<Pair<Int, Int>?>(null) } // сколько не хватает, цена
    var questFeedback by remember { mutableStateOf<String?>(null) }
    var showNoPetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialModal) {
        if (initialModal != null) {
            if (initialModal == "SHOP" && profile?.isPetRunaway == true) {
                showNoPetDialog = true
                onModalDismissed()
            } else {
                activeModal = initialModal
            }
        }
    }

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
        val isRunaway = profile?.isPetRunaway == true
        val anyStatLow = (profile?.satiety ?: 100) <= 30 || (profile?.mood ?: 100) <= 30 || (profile?.health ?: 100) <= 30
        MapWithHotspots(
            mapRes = mapRes,
            imageAspectRatio = mapAspectRatio,
            hotspots = DEFAULT_MAP_HOTSPOTS,
            hasShopWarning = anyStatLow && !isRunaway,
            debug = DEBUG_HOTSPOTS,
            isNight = isNight,
            onClickHotspot = { spot ->
                when (spot.id) {
                    "shop" -> {
                        if (profile?.isPetRunaway == true) {
                            // если питомец в побеге, магазин закрыт
                            showNoPetDialog = true
                        } else {
                            activeModal = "SHOP"
                        }
                    }
                    "quests" -> {
                        activeModal = "QUESTS"
                    }
                    "home", "bank" -> {
                        activeModal = "GOALS"
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    // шторка школы финграмотности (квесты и словарик)
    if (activeModal == "QUESTS") {
        SchoolBottomSheet(
            quests = quests,
            progressList = questProgress,
            petName = petName,
            growthStage = profile?.growthStage ?: "BABY",
            demoMode = demoMode,
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
            },
            onAnswerText = { questId, answerText ->
                val quest = quests.find { it.id == questId }
                onAnswerQuestText(questId, answerText)
                val clean = answerText.trim().lowercase()
                val isCorrect = quest?.expectedAnswers?.any {
                    val exp = it.trim().lowercase()
                    clean == exp || clean.contains(exp) || exp.contains(clean)
                } ?: true
                questFeedback = if (isCorrect) {
                    quest?.feedbackSuccess?.ifEmpty { "Правильно! Ты отлично разбираешься в финансовых терминах!" }
                } else {
                    val exp = quest?.expectedAnswers?.firstOrNull() ?: ""
                    quest?.feedbackFailure?.ifEmpty { "Хорошая попытка! Верный ответ: $exp." }
                } ?: "Отличный ответ!"
            },
            onAnswerAllocation = { questId, amount ->
                val quest = quests.find { it.id == questId }
                onAnswerQuestAllocation(questId, amount)
                val isBalanced = quest?.let { amount in it.recommendedMin..it.recommendedMax } ?: true
                questFeedback = if (isBalanced) {
                    quest?.allocationSuccessFeedback?.ifEmpty { "Прекрасное распределение бюджета! Мудрый и взвешенный выбор." }
                } else {
                    quest?.allocationWarningFeedback?.ifEmpty { "Ты сделал выбор! Стремись к балансу между текущими радостями и будущей целью." }
                } ?: "Бюджет распределен!"
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
            preorderItemIds = preorderItemIds,
            currentPeriod = currentPeriod,
            profile = profile,
            demoMode = demoMode,
            adultStageStartDay = adultStageStartDay,
            onTogglePreorderItem = onTogglePreorderItem,
            onQuickBuy = onQuickBuyItems,
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
            currentPeriodIndex = todayNotes.firstOrNull()?.dayIndex ?: (profile?.currentPeriodIndex ?: 1),
            questProgress = questProgress,
            onSelectActiveGoal = onSelectActiveGoal,
            onCompleteHomeChore = onCompleteHomeChore,
            initialPiggyBankExpanded = if (focusOnHomeChores) false else (initialModal == "GOALS"),
            focusOnChores = focusOnHomeChores,
            targetGoalId = targetGoalId,
            onDismiss = {
                activeModal = null
                focusOnHomeChores = false
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

    // диалог нехватки монет: подсказываем ребенку заглянуть в домик к заданиям
    notEnoughMoneyInfo?.let { (missing, price) ->
        PixelAlertDialog(
            onDismissRequest = { notEnoughMoneyInfo = null },
            title = "Не хватает монет!",
            icon = "!",
            confirmButton = {
                PixelButton(
                    text = "В Домик к заданиям",
                    onClick = {
                        notEnoughMoneyInfo = null
                        focusOnHomeChores = true
                        activeModal = "GOALS"
                    },
                    containerColor = JoyOrange,
                    textColor = PixelTextDark,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                PixelButton(
                    text = "Понятно",
                    onClick = { notEnoughMoneyInfo = null },
                    containerColor = PixelWoodMedium,
                    textColor = PixelParchmentLight,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ) {
            Text(
                text = "Тебе не хватает $missing монет для покупки за $price.\n\nЗагляни в домик и выполни задания по дому, чтобы заработать монет!",
                fontFamily = AppFontFamily,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = PixelTextDark,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    // предупреждение: питомец убежал
    if (showNoPetDialog) {
        PixelAlertDialog(
            onDismissRequest = { showNoPetDialog = false },
            title = "Финик сбежал",
            icon = "!",
            confirmButton = {
                PixelButton(
                    text = "Понятно",
                    onClick = { showNoPetDialog = false },
                    containerColor = JoyOrange,
                    textColor = PixelTextDark,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ) {
            Text(
                text = "$petName убежал, и сейчас его нет дома. Продукты некому кушать.\n\nПоставь миску корма у двери в разделе питомца — и он скоро вернётся!",
                fontFamily = AppFontFamily,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = PixelTextDark,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    // обратная связь после ответа на квест
    questFeedback?.let { feedback ->
        PixelAlertDialog(
            onDismissRequest = { questFeedback = null },
            title = "Совет от $petName",
            icon = "!",
            confirmButton = {
                PixelButton(
                    text = "Спасибо, понятно!",
                    onClick = { questFeedback = null },
                    containerColor = FreshGreen,
                    textColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ) {
            Text(
                text = feedback,
                fontFamily = AppFontFamily,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                color = PixelTextDark,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

}
