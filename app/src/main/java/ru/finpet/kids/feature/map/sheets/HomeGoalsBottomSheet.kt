package ru.finpet.kids.feature.map.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.R
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.feature.map.components.PixelLocationModalSheet

// список домашних дел с кулдаунами (посуда раз в день, мусор раз в 2 дня, полы раз в 3 дня)
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

@OptIn(ExperimentalFoundationApi::class)
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
    onWithdraw: (goalId: String, amount: Int) -> Unit,
    initialPiggyBankExpanded: Boolean = false,
    targetGoalId: String? = null
) {
    var withdrawWarningGoal by remember { mutableStateOf<GoalEntity?>(null) }
    var completedChoreRewardInfo by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var isPiggyBankExpanded by remember(initialPiggyBankExpanded) { mutableStateOf(initialPiggyBankExpanded) }
    val listState = rememberLazyListState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(initialPiggyBankExpanded, targetGoalId) {
        if (initialPiggyBankExpanded) {
            isPiggyBankExpanded = true
            // небольшая пауза на разворот аккордеона и скролл прямо к цели
            delay(200)
            try {
                bringIntoViewRequester.bringIntoView()
            } catch (_: Exception) {
                listState.animateScrollToItem(1)
            }
        }
    }

    val totalSaved = goals.sumOf { it.savedAmount }

    // проверяем кулдаун дела: не даем спамить кнопку чаще положенного срока
    fun checkChoreDone(choreId: String): Boolean {
        val chore = questProgress.find { it.questId == choreId } ?: return false
        if (!chore.isCompleted) return false
        return when (choreId) {
            "chore_dishes" -> chore.completedInPeriod >= currentPeriodIndex
            "chore_trash" -> (currentPeriodIndex - chore.completedInPeriod) < 2
            "chore_floor" -> (currentPeriodIndex - chore.completedInPeriod) < 3
            else -> chore.completedInPeriod >= currentPeriodIndex
        }
    }

    val completedChoresCount = DEFAULT_HOME_CHORES.count { chore -> checkChoreDone(chore.id) }

    PixelLocationModalSheet(
        title = "🏡 Уютный домик и Копилка",
        subtitle = "Выполняй домашние дела, зарабатывай монеты и копи на мечту",
        headerDrawableRes = R.drawable.location_home_header,
        badgeText = "💰 $totalSaved накоплено",
        onDismiss = onDismiss,
        listState = listState
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // кнопка-аккордеон копилки (раскрывает цели на мечту)
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
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isPiggyBankExpanded) "Нажми, чтобы скрыть цели ▲" else "Накоплено $totalSaved монет • Нажми, чтобы открыть ▼",
                                fontSize = 16.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Text(
                        text = if (isPiggyBankExpanded) "▲" else "▼",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = JoyOrange
                    )
                }
            }

            // раскрытый список финансовых целей
            AnimatedVisibility(visible = isPiggyBankExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                            CoinIcon(modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Доступно для накоплений: $balance монет",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = FreshGreen
                            )
                        }
                    }

                    goals.forEach { goal ->
                        val isSelected = goal.id == (activeGoalId ?: goals.firstOrNull()?.id)
                        val isTarget = goal.id == (targetGoalId ?: activeGoalId ?: goals.firstOrNull()?.id)
                        val progress = (goal.savedAmount.toFloat() / goal.targetCost.toFloat()).coerceIn(0f, 1f)
                        val percent = (progress * 100).toInt()
                        val remaining = (goal.targetCost - goal.savedAmount).coerceAtLeast(0)

                        FinCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (isTarget) Modifier.bringIntoViewRequester(bringIntoViewRequester) else Modifier),
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
                                        // чекбокс выбора активной цели
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
                                                    fontSize = 22.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = goalEmoji, fontSize = 26.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = goal.title,
                                                fontSize = 21.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            if (isSelected) {
                                                Text(
                                                    text = "⭐ Главная цель",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = JoyOrange
                                                )
                                            }
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${goal.savedAmount} / ${goal.targetCost} ",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JoyOrange
                                        )
                                        CoinIcon(modifier = Modifier.size(18.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFEEEEEE))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(FreshGreen)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (goal.isReached) "🎉 Мечта достигнута!" else "Осталось накопить: $remaining монет ($percent%)",
                                    fontSize = 17.sp,
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
                                            Text("+10 ", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                            CoinIcon(modifier = Modifier.size(16.dp))
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
                                            Text("+50 ", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                            CoinIcon(modifier = Modifier.size(16.dp))
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
                                            Text("Забрать", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // блок домашних дел для честного заработка монеток
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
                            fontSize = 22.sp,
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (completedChoresCount == DEFAULT_HOME_CHORES.size) FreshGreen else JoyOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Помогай по дому и получай монеты в награду:",
                    fontSize = 17.sp,
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
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = chore.description,
                                        fontSize = 16.sp,
                                        color = TextSecondary,
                                        lineHeight = 20.sp
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
                                        Text(text = "✅ +${chore.rewardCoins}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = FreshGreen)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        CoinIcon(modifier = Modifier.size(16.dp))
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
                                        Text(text = "+${chore.rewardCoins}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
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

    // поздравление за выполненное дело
    completedChoreRewardInfo?.let { (title, reward) ->
        AlertDialog(
            onDismissRequest = { completedChoreRewardInfo = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎉", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Дело сделано!", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
            },
            text = {
                Text(
                    text = "Ты выполнил дело «$title» и заработал $reward монет в копилку и кошелек!",
                    fontSize = 19.sp,
                    lineHeight = 26.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { completedChoreRewardInfo = null },
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Ура! ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        CoinIcon(modifier = Modifier.size(18.dp))
                    }
                }
            }
        )
    }

    // по ТЗ 2.5.7: если ребенок снимает из копилки, предупреждаем что мечта отдалится
    withdrawWarningGoal?.let { goal ->
        AlertDialog(
            onDismissRequest = { withdrawWarningGoal = null },
            title = { Text(text = "⚠️ Забрать монеты из копилки?", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
            text = {
                Text(
                    text = "Если забрать 50 монет, покупка «${goal.title}» отложится на 1 день!\n\nТочно хочешь забрать монеты обратно в кошелек?",
                    fontSize = 19.sp,
                    lineHeight = 26.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = minOf(50, goal.savedAmount)
                        onWithdraw(goal.id, amount)
                        withdrawWarningGoal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) {
                    Text("Да, забрать", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { withdrawWarningGoal = null }) {
                    Text("Оставить в копилке 👍", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}
