package ru.finpet.kids.feature.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity
import ru.finpet.kids.core.data.repository.PurchaseItem
import ru.finpet.kids.core.data.repository.QuestItem
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.SkyBlue
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.R

private const val DEBUG_HOTSPOTS = false

// Палитра Pixel Parchment
private val PixelParchmentLight = Color(0xFFFFF8E7)
private val PixelDarkBrown = Color(0xFF351A0C)
private val PixelWoodMedium = Color(0xFF6B3C1A)

@Composable
fun MapScreen(
    profile: ProfileEntity?,
    purchases: List<PurchaseItem>,
    quests: List<QuestItem>,
    questProgress: List<QuestProgressEntity>,
    goals: List<GoalEntity>,
    onBuyItem: (PurchaseItem) -> Unit,
    onAnswerQuest: (questId: String, optionId: String) -> Unit,
    onDepositGoal: (goalId: String, amount: Int) -> Unit,
    onWithdrawGoal: (goalId: String, amount: Int) -> Unit,
    onSelectActiveGoal: (goalId: String) -> Unit = {},
    onCompleteHomeChore: (choreId: String, rewardCoins: Int) -> Unit = { _, _ -> },
    onNavigateToFinik: () -> Unit = {}
) {
    var activeModal by remember { mutableStateOf<String?>(null) } // "SHOP", "QUESTS", "GOALS"
    var notEnoughMoneyInfo by remember { mutableStateOf<Pair<Int, Int>?>(null) } // missing, price
    var questFeedback by remember { mutableStateOf<String?>(null) }

    val balance = profile?.balance ?: 50

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp),
        contentPadding = PaddingValues(top = 0.dp, bottom = 84.dp)
    ) {
        item(key = "map_image") {
            MapWithHotspots(
                mapRes = R.drawable.day,
                imageAspectRatio = 185f / 360f,
                hotspots = DEFAULT_MAP_HOTSPOTS,
                debug = DEBUG_HOTSPOTS,     // ← рамки видны прямо здесь
                onClickHotspot = { spot ->
                    when (spot.id) {
                        "shop" -> activeModal = "SHOP"
                        "quests" -> activeModal = "QUESTS"
                        "home", "bank" -> activeModal = "GOALS"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // --- БОЛЬШАЯ ВСПЛЫВАШКА: ШКОЛА ФИННИ (КВЕСТЫ) ---
    if (activeModal == "QUESTS") {
        SchoolBottomSheet(
            quests = quests,
            progressList = questProgress,
            onDismiss = { activeModal = null },
            onSelectOption = { questId, optionId ->
                val quest = quests.find { it.id == questId }
                val opt = quest?.options?.find { it.id == optionId }
                onAnswerQuest(questId, optionId)
                questFeedback = opt?.feedback ?: "Молодец!"
            }
        )
    }

    // --- БОЛЬШАЯ ВСПЛЫВАШКА: ЛАВКА ТОВАРОВ (МАГАЗИН) ---
    if (activeModal == "SHOP") {
        ShopBottomSheet(
            balance = balance,
            items = purchases,
            onDismiss = { activeModal = null },
            onBuy = { item ->
                if (balance < item.price) {
                    notEnoughMoneyInfo = (item.price - balance) to item.price
                } else {
                    onBuyItem(item)
                }
            }
        )
    }

    // --- БОЛЬШАЯ ВСПЛЫВАШКА: ДОМ ФИННИ И КОПИЛКА ---
    if (activeModal == "GOALS") {
        HomeGoalsBottomSheet(
            balance = balance,
            goals = goals,
            activeGoalId = profile?.activeGoalId,
            currentPeriodIndex = profile?.currentPeriodIndex ?: 1,
            questProgress = questProgress,
            onSelectActiveGoal = onSelectActiveGoal,
            onCompleteHomeChore = onCompleteHomeChore,
            onDismiss = { activeModal = null },
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

    // --- ДИАЛОГ НЕХВАТКИ СРЕДСТВ ---
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
                    text = "Тебе не хватает $missing монет для покупки за $price.\n\nЗагляни в Школу Финни и реши задание, чтобы заработать монет!",
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

    // --- ОБРАТНАЯ СВЯЗЬ ПО КВЕСТУ ---
    questFeedback?.let { feedback ->
        AlertDialog(
            onDismissRequest = { questFeedback = null },
            title = { Text(text = "💡 Совет от Финни", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
}

// ====================================================================
// УНИВЕРСАЛЬНАЯ ПИКСЕЛЬНАЯ ПОДЛОЖКА ОКНА (MODAL BOTTOM SHEET ~90% ЭКРАНА)
// ====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PixelLocationModalSheet(
    title: String,
    subtitle: String,
    headerDrawableRes: Int,
    badgeText: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PixelParchmentLight,
        scrimColor = Color.Black.copy(alpha = 0.40f), // затемнение фона 40%
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f) // Большое окно: 90% экрана
        ) {
            // ФИКСИРОВАННЫЙ ХЕДЕР (~1/4 экрана: 210 dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                // Пиксель-арт иллюстрация с отключенным блюром
                Image(
                    painter = painterResource(id = headerDrawableRes),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Drag-handle по центру сверху
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .size(width = 44.dp, height = 5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(PixelWoodMedium.copy(alpha = 0.7f))
                )

                // Сейф-зона слева: Бейдж
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 16.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    border = BorderStroke(1.5.dp, PixelDarkBrown),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelDarkBrown,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // Сейф-зона справа: Крестик закрытия (48x48 dp тач-зона)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 12.dp, top = 12.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.95f))
                            .border(1.5.dp, PixelDarkBrown, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = PixelDarkBrown
                        )
                    }
                }
            }

            // СКРОЛЛИРУЕМАЯ КОНТЕНТНАЯ ЗОНА
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Заголовок и подзаголовок локации
                item(key = "location_header_texts") {
                    Column {
                        Text(
                            text = title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PixelDarkBrown
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            fontSize = 14.sp,
                            color = PixelWoodMedium,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Специфический контент окна
                item(key = "location_main_content") {
                    content()
                }
            }
        }
    }
}

// ====================================================================
// 1. ВСПЛЫВАШКА ШКОЛЫ
// ====================================================================

@Composable
fun SchoolBottomSheet(
    quests: List<QuestItem>,
    progressList: List<QuestProgressEntity>,
    onDismiss: () -> Unit,
    onSelectOption: (questId: String, optionId: String) -> Unit
) {
    val completedCount = progressList.count { it.isCompleted }

    PixelLocationModalSheet(
        title = "🏫 Школа Финни",
        subtitle = "Уроки финансовой грамотности • Полезные советы для жизни",
        headerDrawableRes = R.drawable.location_school_header,
        badgeText = "$completedCount / ${quests.size} решено",
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            quests.forEach { quest ->
                val isDone = progressList.any { it.questId == quest.id && it.isCompleted }

                FinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (isDone) Color(0xFFE8F5E9) else Color(0xFFFFF9E6),
                    borderColor = if (isDone) FreshGreen else Color(0xFFFFD54F)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = quest.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (isDone) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.9f),
                                    border = BorderStroke(1.dp, FreshGreen)
                                ) {
                                    Text(
                                        text = "✅ Пройдено",
                                        fontSize = 12.sp,
                                        color = FreshGreen,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = quest.situation,
                            fontSize = 15.sp,
                            color = TextPrimary,
                            lineHeight = 21.sp
                        )

                        if (!isDone) {
                            Spacer(modifier = Modifier.height(12.dp))
                            quest.options.forEach { opt ->
                                Button(
                                    onClick = { onSelectOption(quest.id, opt.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .border(1.5.dp, JoyOrange, RoundedCornerShape(14.dp)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = opt.text,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.fillMaxWidth()
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

// ====================================================================
// 2. ВСПЛЫВАШКА ЛАВКИ (МАГАЗИН)
// ====================================================================

@Composable
fun ShopBottomSheet(
    balance: Int,
    items: List<PurchaseItem>,
    onDismiss: () -> Unit,
    onBuy: (PurchaseItem) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    var itemToConfirm by remember { mutableStateOf<PurchaseItem?>(null) }

    val filteredItems = remember(selectedCategory, items) {
        if (selectedCategory == "ALL") items
        else items.filter { it.category == selectedCategory }
    }

    PixelLocationModalSheet(
        title = "🏪 Лавка товаров",
        subtitle = "Вкусная еда, чистая вода и радости для питомца",
        headerDrawableRes = R.drawable.location_shop_header,
        badgeText = "🪙 $balance монет",
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Индикатор кошелька
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFFF3E0),
                border = BorderStroke(1.5.dp, Color(0xFFFFB74D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoinIcon(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "В кошельке: $balance монет",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                }
            }

            // Фильтры категорий
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryChip("Все", selectedCategory == "ALL") { selectedCategory = "ALL" }
                CategoryChip("Обязательное 🥣", selectedCategory == "MANDATORY") { selectedCategory = "MANDATORY" }
                CategoryChip("Желаемое 🎈", selectedCategory == "OPTIONAL") { selectedCategory = "OPTIONAL" }
            }

            // Список товаров
            filteredItems.forEach { item ->
                FinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (item.category == "MANDATORY") Color(0xFFF1F8E9) else Color(0xFFF3E5F5),
                    borderColor = if (item.category == "MANDATORY") Color(0xFFA5D6A7) else Color(0xFFCE93D8)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.description,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 17.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (item.satietyBonus > 0) Text("🍎 +${item.satietyBonus}", fontSize = 12.sp, color = JoyOrange, fontWeight = FontWeight.Bold)
                                if (item.moodBonus > 0) Text("⚡ +${item.moodBonus}", fontSize = 12.sp, color = SkyBlue, fontWeight = FontWeight.Bold)
                                if (item.healthBonus > 0) Text("❤️ +${item.healthBonus}", fontSize = 12.sp, color = FreshGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { itemToConfirm = item },
                            colors = ButtonDefaults.buttonColors(containerColor = JoyOrange),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${item.price} ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                CoinIcon(modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }
            }
        }
    }
    // --- ДИАЛОГ ПОДТВЕРЖДЕНИЯ ПОКУПКИ ---
    itemToConfirm?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToConfirm = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Купить «${item.name}»?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = item.description,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Цена
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Цена: ",
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${item.price} ",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = JoyOrange
                        )
                        CoinIcon(modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    // Остаток после покупки
                    val remaining = balance - item.price
                    Text(
                        text = "Останется: $remaining монет",
                        fontSize = 13.sp,
                        color = if (remaining >= 0) FreshGreen else Color(0xFFD32F2F),
                        fontWeight = FontWeight.SemiBold
                    )

                    // Что даёт питомцу
                    val effects = mutableListOf<String>()
                    if (item.satietyBonus > 0) effects.add("🍎 Сытость +${item.satietyBonus}")
                    if (item.moodBonus > 0) effects.add("⚡ Настроение +${item.moodBonus}")
                    if (item.healthBonus > 0) effects.add("❤️ Здоровье +${item.healthBonus}")

                    if (effects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Эффект: ${effects.joinToString(", ")}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBuy(item)
                        itemToConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                ) {
                    Text("Купить 🛒", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToConfirm = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}

// ====================================================================
// 3. ВСПЛЫВАШКА ДОМА (КОПИЛКА И ЦЕЛИ)
// ====================================================================

data class HomeChoreItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val rewardCoins: Int
)

val DEFAULT_HOME_CHORES = listOf(
    HomeChoreItem(
        id = "chore_floor",
        title = "Мытье полов",
        description = "Помыть полы в комнатах для чистоты (раз в 3 суток)",
        icon = "🧹",
        rewardCoins = 50
    ),
    HomeChoreItem(
        id = "chore_trash",
        title = "Вынос мусора",
        description = "Собрать мусорный пакет и вынести в уличный контейнер (раз в 2 суток)",
        icon = "🗑️",
        rewardCoins = 25
    ),
    HomeChoreItem(
        id = "chore_dishes",
        title = "Мытье посуды",
        description = "Помыть за собой тарелку, ложку и кружку после еды (каждый день)",
        icon = "🍽️",
        rewardCoins = 20
    )
)

@Composable
fun HomeGoalsBottomSheet(
    balance: Int,
    goals: List<GoalEntity>,
    activeGoalId: String?,
    currentPeriodIndex: Int = 1,
    questProgress: List<QuestProgressEntity>,
    onSelectActiveGoal: (goalId: String) -> Unit,
    onCompleteHomeChore: (choreId: String, rewardCoins: Int) -> Unit,
    onDismiss: () -> Unit,
    onDeposit: (goalId: String, amount: Int) -> Unit,
    onWithdraw: (goalId: String, amount: Int) -> Unit
) {
    var withdrawWarningGoal by remember { mutableStateOf<GoalEntity?>(null) }
    var completedChoreRewardInfo by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var isPiggyBankExpanded by remember { mutableStateOf(false) }
    val totalSaved = goals.sumOf { it.savedAmount }

    fun checkChoreDone(choreId: String): Boolean {
        val choreProgress = questProgress.find { it.questId == choreId } ?: return false
        if (!choreProgress.isCompleted) return false
        return when (choreId) {
            "chore_dishes" -> choreProgress.completedInPeriod >= currentPeriodIndex
            "chore_trash" -> (currentPeriodIndex - choreProgress.completedInPeriod) < 2
            "chore_floor" -> (currentPeriodIndex - choreProgress.completedInPeriod) < 3
            else -> choreProgress.completedInPeriod >= currentPeriodIndex
        }
    }

    val completedChoresCount = DEFAULT_HOME_CHORES.count { chore -> checkChoreDone(chore.id) }

    PixelLocationModalSheet(
        title = "🏡 Уютный домик и Копилка",
        subtitle = "Выполняй домашние дела, зарабатывай монеты и копи на мечту",
        headerDrawableRes = R.drawable.location_home_header,
        badgeText = "💰 $totalSaved накоплено",
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Кнопка перехода в копилку (сверху над задачами)
            FinCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isPiggyBankExpanded = !isPiggyBankExpanded },
                backgroundColor = if (isPiggyBankExpanded) Color(0xFFFFF3E0) else Color.White,
                borderColor = if (isPiggyBankExpanded) JoyOrange else Color(0xFFFFD54F)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isPiggyBankExpanded) JoyOrange else Color(0xFFFFE082)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🐷", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Копилка на мечту",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isPiggyBankExpanded) "Нажми, чтобы скрыть цели ▲" else "Накоплено $totalSaved монет • Нажми, чтобы открыть ▼",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Text(
                        text = if (isPiggyBankExpanded) "▲" else "▼",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = JoyOrange
                    )
                }
            }

            // Раскрывающийся вниз список целей в копилке
            AnimatedVisibility(visible = isPiggyBankExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Индикатор доступных монет
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFE8F5E9),
                        border = BorderStroke(1.5.dp, Color(0xFFA5D6A7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CoinIcon(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Доступно для накоплений: $balance монет",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = FreshGreen
                            )
                        }
                    }

                    // Список целей (без надписи про квадратик)
                    goals.forEach { goal ->
                        val isSelected = goal.id == (activeGoalId ?: goals.firstOrNull()?.id)
                        val progress = (goal.savedAmount.toFloat() / goal.targetCost.toFloat()).coerceIn(0f, 1f)
                        val percent = (progress * 100).toInt()
                        val remaining = (goal.targetCost - goal.savedAmount).coerceAtLeast(0)

                        FinCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color.White,
                            borderColor = if (isSelected) JoyOrange else Color(0xFFFFD54F)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val goalEmoji = when (goal.id) {
                                        "goal_ball" -> "⚽"
                                        "goal_headphones" -> "🎧"
                                        "goal_scooter" -> "🛴"
                                        else -> "🎯"
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onSelectActiveGoal(goal.id) }
                                            .padding(vertical = 2.dp)
                                    ) {
                                        // Квадрат с галочкой выбора главной цели
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) JoyOrange else Color(0xFFF5F5F5))
                                                .border(
                                                    width = 2.dp,
                                                    color = if (isSelected) JoyOrange else Color(0xFFBDBDBD),
                                                    shape = RoundedCornerShape(8.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Text(
                                                    text = "✓",
                                                    color = Color.White,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = goalEmoji, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = goal.title,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            if (isSelected) {
                                                Text(
                                                    text = "⭐ Главная цель",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = JoyOrange
                                                )
                                            }
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${goal.savedAmount} / ${goal.targetCost} ",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JoyOrange
                                        )
                                        CoinIcon(modifier = Modifier.size(15.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    color = FreshGreen,
                                    trackColor = Color(0xFFEEEEEE)
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (goal.isReached) "🎉 Мечта достигнута!" else "Осталось накопить: $remaining монет ($percent%)",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { onDeposit(goal.id, 10) },
                                        enabled = balance >= 10,
                                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("+10 ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            CoinIcon(modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Button(
                                        onClick = { onDeposit(goal.id, 50) },
                                        enabled = balance >= 50,
                                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("+50 ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            CoinIcon(modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    if (goal.savedAmount >= 20) {
                                        Button(
                                            onClick = { withdrawWarningGoal = goal },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                        ) {
                                            Text("Забрать", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Домашние дела (изначально видны в домике)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🧹", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Домашние дела",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (completedChoresCount == DEFAULT_HOME_CHORES.size) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    ) {
                        Text(
                            text = "$completedChoresCount / ${DEFAULT_HOME_CHORES.size} сделано",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (completedChoresCount == DEFAULT_HOME_CHORES.size) FreshGreen else JoyOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Помогай по дому и получай монеты в награду:",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                DEFAULT_HOME_CHORES.forEach { chore ->
                    val isDone = checkChoreDone(chore.id)

                    FinCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = if (isDone) Color(0xFFF9FBE7) else Color.White,
                        borderColor = if (isDone) Color(0xFFAED581) else Color(0xFFE0E0E0)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isDone) Color(0xFFDCEDC8) else Color(0xFFF5F5F5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = chore.icon, fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = chore.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = chore.description,
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            if (isDone) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Text(text = "✅ +${chore.rewardCoins}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FreshGreen)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        CoinIcon(modifier = Modifier.size(13.dp))
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        onCompleteHomeChore(chore.id, chore.rewardCoins)
                                        completedChoreRewardInfo = chore.title to chore.rewardCoins
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "+${chore.rewardCoins}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        CoinIcon(modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Сообщение об успешном выполнении домашнего дела
    completedChoreRewardInfo?.let { (title, reward) ->
        AlertDialog(
            onDismissRequest = { completedChoreRewardInfo = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎉", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Дело сделано!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Ты выполнил дело «$title» и заработал $reward монет в копилку и кошелек!",
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { completedChoreRewardInfo = null },
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                ) {
                    Text("Ура! 🪙")
                }
            }
        )
    }

    // Предупреждение при снятии монет из копилки
    withdrawWarningGoal?.let { goal ->
        AlertDialog(
            onDismissRequest = { withdrawWarningGoal = null },
            title = { Text(text = "⚠️ Забрать монеты из копилки?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Если забрать 50 монет, покупка «${goal.title}» отложится на 1 день!\n\nТочно хочешь забрать монеты обратно в кошелек?",
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toWithdraw = 50.coerceAtMost(goal.savedAmount)
                        onWithdraw(goal.id, toWithdraw)
                        withdrawWarningGoal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Да, забрать")
                }
            },
            dismissButton = {
                TextButton(onClick = { withdrawWarningGoal = null }) {
                    Text("Оставить в копилке")
                }
            }
        )
    }
}

// ====================================================================
// ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ КАРТЫ
// ====================================================================

@Composable
fun CategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) JoyOrange else Color(0xFFEEEEEE),
        modifier = Modifier.height(34.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (isSelected) Color.White else TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

