package ru.finpet.kids.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity
import ru.finpet.kids.core.data.repository.PurchaseItem
import ru.finpet.kids.core.data.repository.QuestItem
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.SkyBlue
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.R

private const val DEBUG_HOTSPOTS = false // отражение кликов для дебага хитбоксов

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
    onWithdrawGoal: (goalId: String, amount: Int) -> Unit
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
                        "bank" -> activeModal = "GOALS"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }

    // --- МОДАЛКА МАГАЗИНА ---
    if (activeModal == "SHOP") {
        ShopDialog(
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

    // --- ДИАЛОГ НЕХВАТКИ СРЕДСТВ (ШАГ 7 ТЗ) ---
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
                    text = "Тебе не хватает $missing монет для покупки за $price.\n\nНе расстраивайся: загляни в Шатер историй и заработай монетки, или купи эту вещь в следующем дне!",
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
                    Text("Идти в Квесты 🎪")
                }
            },
            dismissButton = {
                TextButton(onClick = { notEnoughMoneyInfo = null }) {
                    Text("Понятно")
                }
            }
        )
    }

    // --- МОДАЛКА КВЕСТОВ ---
    if (activeModal == "QUESTS") {
        QuestsDialog(
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

    // --- МОДАЛКА КОПИЛКИ И ЦЕЛЕЙ ---
    if (activeModal == "GOALS") {
        GoalsDialog(
            balance = balance,
            goals = goals,
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
}

@Composable
fun MapLocationCard(
    title: String,
    subtitle: String,
    badge: String,
    gradientColors: List<Color>,
    iconEmoji: String,
    onClick: () -> Unit
) {
    // Кисть и производные списки запоминаем: градиентный шейдер дорогой,
    // пересоздавать его на каждую рекомпозицию не нужно
    val backgroundBrush = remember(gradientColors) {
        Brush.horizontalGradient(gradientColors.map { it.copy(alpha = 0.35f) })
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .background(backgroundBrush)
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, gradientColors.last(), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = iconEmoji, fontSize = 30.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
                Text(text = "👉", fontSize = 20.sp)
            }
        }
    }
}

// --- ДИАЛОГ МАГАЗИНА ---
@Composable
fun ShopDialog(
    balance: Int,
    items: List<PurchaseItem>,
    onDismiss: () -> Unit,
    onBuy: (PurchaseItem) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }

    val filteredItems = remember(selectedCategory, items) {
        if (selectedCategory == "ALL") items
        else items.filter { it.category == selectedCategory }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "🏪 Лавка товаров", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Твой кошелек: ", fontSize = 14.sp, color = Color(0xFFE65100))
                    CoinIcon(modifier = Modifier.size(15.dp))
                    Text(text = " $balance монет", fontSize = 14.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CategoryChip("Все", selectedCategory == "ALL") { selectedCategory = "ALL" }
                    CategoryChip("Обязательное", selectedCategory == "MANDATORY") { selectedCategory = "MANDATORY" }
                    CategoryChip("Желаемое", selectedCategory == "OPTIONAL") { selectedCategory = "OPTIONAL" }
                }
            }
        },
        text = {
            LazyColumn(modifier = Modifier.height(380.dp)) {
                items(filteredItems, key = { it.id }) { item ->
                    FinCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        backgroundColor = if (item.category == "MANDATORY") Color(0xFFF1F8E9) else Color(0xFFF3E5F5)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = item.description,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    if (item.satietyBonus > 0) Text("🍎 +${item.satietyBonus} ", fontSize = 11.sp, color = JoyOrange)
                                    if (item.moodBonus > 0) Text("⚡ +${item.moodBonus} ", fontSize = 11.sp, color = SkyBlue)
                                    if (item.healthBonus > 0) Text("❤️ +${item.healthBonus} ", fontSize = 11.sp, color = FreshGreen)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onBuy(item) },
                                colors = ButtonDefaults.buttonColors(containerColor = JoyOrange),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${item.price} ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    CoinIcon(modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
fun CategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) JoyOrange else Color(0xFFEEEEEE),
        modifier = Modifier.height(30.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (isSelected) Color.White else TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// --- ДИАЛОГ КВЕСТОВ ---
@Composable
fun QuestsDialog(
    quests: List<QuestItem>,
    progressList: List<QuestProgressEntity>,
    onDismiss: () -> Unit,
    onSelectOption: (questId: String, optionId: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "📜 Шатер мудрых историй", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        },
        text = {
            LazyColumn(modifier = Modifier.height(420.dp)) {
                items(quests, key = { it.id }) { quest ->
                    val isDone = progressList.any { it.questId == quest.id && it.isCompleted }

                    FinCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        backgroundColor = if (isDone) Color(0xFFE8F5E9) else Color(0xFFFFF9E6),
                        borderColor = if (isDone) FreshGreen else Color(0xFFFFD54F)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = quest.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (isDone) {
                                    Text(text = "✅ Пройдено", fontSize = 12.sp, color = FreshGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = quest.situation,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                            if (!isDone) {
                                Spacer(modifier = Modifier.height(10.dp))
                                quest.options.forEach { opt ->
                                    Button(
                                        onClick = { onSelectOption(quest.id, opt.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .border(1.5.dp, JoyOrange, RoundedCornerShape(12.dp))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                                        ) {
                                            Text(
                                                text = "${opt.text} (+${opt.rewardCoins} ",
                                                color = TextPrimary,
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Center
                                            )
                                            CoinIcon(modifier = Modifier.size(13.dp))
                                            Text(text = ")", color = TextPrimary, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

// --- ДИАЛОГ КОПИЛКИ И ЦЕЛЕЙ ---
@Composable
fun GoalsDialog(
    balance: Int,
    goals: List<GoalEntity>,
    onDismiss: () -> Unit,
    onDeposit: (goalId: String, amount: Int) -> Unit,
    onWithdraw: (goalId: String, amount: Int) -> Unit
) {
    var withdrawWarningGoal by remember { mutableStateOf<GoalEntity?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "🏦 Копилка целей", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Доступно монет: ", fontSize = 14.sp, color = FreshGreen)
                    CoinIcon(modifier = Modifier.size(15.dp))
                    Text(text = " $balance", fontSize = 14.sp, color = FreshGreen, fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            LazyColumn(modifier = Modifier.height(380.dp)) {
                items(goals, key = { it.id }) { goal ->
                    val progress = (goal.savedAmount.toFloat() / goal.targetCost.toFloat()).coerceIn(0f, 1f)
                    val percent = (progress * 100).toInt()
                    val remaining = (goal.targetCost - goal.savedAmount).coerceAtLeast(0)

                    FinCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        backgroundColor = Color(0xFFFBFBFB)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = goal.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${goal.savedAmount} / ${goal.targetCost} ",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JoyOrange
                                    )
                                    CoinIcon(modifier = Modifier.size(13.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = FreshGreen,
                                trackColor = Color(0xFFEEEEEE)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (goal.isReached) "🎉 Цель достигнута!" else "Осталось накопить: $remaining монет ($percent%)",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onDeposit(goal.id, 30) },
                                    enabled = balance >= 30,
                                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("+30 ", fontSize = 12.sp)
                                        CoinIcon(modifier = Modifier.size(13.dp))
                                    }
                                }
                                Button(
                                    onClick = { onDeposit(goal.id, 50) },
                                    enabled = balance >= 50,
                                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("+50 ", fontSize = 12.sp)
                                        CoinIcon(modifier = Modifier.size(13.dp))
                                    }
                                }
                                if (goal.savedAmount >= 20) {
                                    Button(
                                        onClick = { withdrawWarningGoal = goal },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Забрать", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )

    // Предупреждение при снятии монет из копилки
    withdrawWarningGoal?.let { goal ->
        AlertDialog(
            onDismissRequest = { withdrawWarningGoal = null },
            title = { Text(text = "⚠️ Забрать монеты из копилки?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Если забрать 50 монет, покупка «${goal.title}» отложится на 1 день!\n\nТочно хочешь забрать монеты обратно в кошелек?",
                    fontSize = 14.sp
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
