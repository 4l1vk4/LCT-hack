package ru.finpet.kids.feature.map.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import kotlinx.coroutines.delay
import ru.finpet.kids.R
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.PixelAlertDialog
import ru.finpet.kids.core.designsystem.PixelBar
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.core.designsystem.WarningIcon
import ru.finpet.kids.core.designsystem.pixelButtonBackground
import ru.finpet.kids.feature.map.components.PixelLocationModalSheet
import ru.finpet.kids.feature.map.minigames.DishesCleaningGameDialog
import ru.finpet.kids.feature.map.minigames.FloorCleaningGameDialog
import ru.finpet.kids.feature.map.minigames.TrashCollectionGameDialog
import ru.finpet.kids.feature.map.sheets.components.GoalProgressCard
import ru.finpet.kids.feature.map.sheets.components.HomeChoreCard

/**
 * Описание домашнего дела с кулдауном.
 */
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

/**
 * Шторка уютного домика и копилки:
 * - Список целей накопления с прогресс-барами в пиксельном дизайне.
 * - Домашние дела с мини-играми (мытье полов, вынос мусора, мытье посуды).
 * - Управление средствами копилки (отложить / снять).
 */
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
    var activeMinigameChore by remember { mutableStateOf<HomeChoreItem?>(null) }
    var isPiggyBankExpanded by remember(initialPiggyBankExpanded) { mutableStateOf(initialPiggyBankExpanded) }
    val listState = rememberLazyListState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(initialPiggyBankExpanded, targetGoalId) {
        if (initialPiggyBankExpanded) {
            isPiggyBankExpanded = true
            delay(200)
            try {
                bringIntoViewRequester.bringIntoView()
            } catch (_: Exception) {
                listState.animateScrollToItem(1)
            }
        }
    }

    val totalSaved = goals.sumOf { it.savedAmount }

    // Проверка периода кулдауна домашних дел
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
            // Кнопка-аккордеон копилки целей
            FinCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isPiggyBankExpanded = !isPiggyBankExpanded }
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
                                .background(Color(0xFFFFE082)),
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
                                fontSize = 15.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Text(
                        text = if (isPiggyBankExpanded) "▲" else "▼",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = JoyOrange
                    )
                }
            }

            // Раскрывающийся список целей
            AnimatedVisibility(visible = isPiggyBankExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pixelButtonBackground(containerColor = Color(0xFFFFF9EE))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CoinIcon(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Доступно для накоплений: $balance монет",
                            fontFamily = AppFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    goals.forEach { goal ->
                        val isSelected = goal.id == (activeGoalId ?: goals.firstOrNull()?.id)
                        val isTarget = goal.id == (targetGoalId ?: activeGoalId ?: goals.firstOrNull()?.id)

                        GoalProgressCard(
                            goal = goal,
                            isSelected = isSelected,
                            isTarget = isTarget,
                            balance = balance,
                            bringIntoViewRequester = bringIntoViewRequester,
                            onSelectActiveGoal = onSelectActiveGoal,
                            onDeposit = onDeposit,
                            onWithdrawClick = { withdrawWarningGoal = it }
                        )
                    }
                }
            }

            // Блок домашних дел
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
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "$completedChoresCount / ${DEFAULT_HOME_CHORES.size} сделано",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (completedChoresCount == DEFAULT_HOME_CHORES.size) FreshGreen else PixelTextDark
                    )
                }

                Text(
                    text = "Помогай по дому и получай монеты в награду:",
                    fontSize = 16.sp,
                    color = TextSecondary
                )

                DEFAULT_HOME_CHORES.forEach { chore ->
                    HomeChoreCard(
                        chore = chore,
                        isDone = checkChoreDone(chore.id),
                        onPlayMinigame = { activeMinigameChore = it }
                    )
                }
            }
        }
    }

    // Диалог награды за выполнение дела
    completedChoreRewardInfo?.let { (title, reward) ->
        PixelAlertDialog(
            onDismissRequest = { completedChoreRewardInfo = null },
            title = "Дело сделано!",
            icon = "🎉",
            confirmButton = {
                PixelButton(
                    text = "Ура! +$reward",
                    onClick = { completedChoreRewardInfo = null },
                    containerColor = FreshGreen,
                    textColor = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { CoinIcon(modifier = Modifier.size(18.dp)) }
                )
            }
        ) {
            Text(
                text = "Ты выполнил дело «$title» и заработал $reward монет в копилку и кошелек!",
                fontFamily = AppFontFamily,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                color = PixelTextDark,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    // Предупреждение о снятии средств из копилки
    withdrawWarningGoal?.let { goal ->
        PixelAlertDialog(
            onDismissRequest = { withdrawWarningGoal = null },
            title = "Забрать монеты?",
            icon = "⚠️",
            confirmButton = {
                PixelButton(
                    text = "Да, забрать",
                    onClick = {
                        val amount = minOf(10, goal.savedAmount)
                        onWithdraw(goal.id, amount)
                        withdrawWarningGoal = null
                    },
                    containerColor = PixelRedBerry,
                    textColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                PixelButton(
                    text = "Оставить 👍",
                    onClick = { withdrawWarningGoal = null },
                    containerColor = PixelWoodMedium,
                    textColor = PixelParchmentLight,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ) {
            Text(
                text = "Если забрать 10 монет, покупка «${goal.title}» немного отложится.\n\nТочно хочешь забрать монеты обратно в кошелек?",
                fontFamily = AppFontFamily,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = PixelTextDark,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    // Запуск мини-игр
    activeMinigameChore?.let { chore ->
        when (chore.id) {
            "chore_floor" -> {
                FloorCleaningGameDialog(
                    rewardCoins = chore.rewardCoins,
                    onCompleteGame = {
                        val c = chore
                        activeMinigameChore = null
                        onCompleteHomeChore(c.id, c.rewardCoins)
                        completedChoreRewardInfo = c.title to c.rewardCoins
                    },
                    onDismiss = { activeMinigameChore = null }
                )
            }
            "chore_trash" -> {
                TrashCollectionGameDialog(
                    rewardCoins = chore.rewardCoins,
                    onCompleteGame = {
                        val c = chore
                        activeMinigameChore = null
                        onCompleteHomeChore(c.id, c.rewardCoins)
                        completedChoreRewardInfo = c.title to c.rewardCoins
                    },
                    onDismiss = { activeMinigameChore = null }
                )
            }
            "chore_dishes" -> {
                DishesCleaningGameDialog(
                    rewardCoins = chore.rewardCoins,
                    onCompleteGame = {
                        val c = chore
                        activeMinigameChore = null
                        onCompleteHomeChore(c.id, c.rewardCoins)
                        completedChoreRewardInfo = c.title to c.rewardCoins
                    },
                    onDismiss = { activeMinigameChore = null }
                )
            }
        }
    }
}
