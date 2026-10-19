package ru.finpet.kids.feature.map.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import ru.finpet.kids.feature.map.sheets.components.GoalStickerIcon
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import kotlinx.coroutines.delay
import ru.finpet.kids.R
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
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
import ru.finpet.kids.feature.map.components.PixelDarkBrown
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
        title = "Уборка листьев",
        description = "Собрать осенние листья во дворе (раз в 3 суток)",
        icon = "🍂",
        rewardCoins = 10
    ),
    HomeChoreItem(
        id = "chore_trash",
        title = "Вынос мусора",
        description = "Собрать мусорный пакет и вынести в уличный контейнер (раз в 2 суток)",
        icon = "🗑️",
        rewardCoins = 8
    ),
    HomeChoreItem(
        id = "chore_dishes",
        title = "Мытье посуды",
        description = "Помыть за собой тарелку, ложку и кружку после еды (каждый день)",
        icon = "🍽️",
        rewardCoins = 5
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
    growthStage: String = "BABY",
    skinId: String = "cat_black",
    petMood: String = "HAPPY",
    onSelectActiveGoal: (goalId: String) -> Unit,
    onCompleteHomeChore: (choreId: String, rewardCoins: Int) -> Unit,
    onDismiss: () -> Unit,
    onDeposit: (goalId: String, amount: Int) -> Unit,
    onWithdraw: (goalId: String, amount: Int) -> Unit,
    initialPiggyBankExpanded: Boolean = false,
    focusOnChores: Boolean = false,
    targetGoalId: String? = null,
    profile: ProfileEntity? = null
) {
    var withdrawWarningGoal by remember { mutableStateOf<GoalEntity?>(null) }
    var completedChoreRewardInfo by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var activeMinigameChore by remember { mutableStateOf<HomeChoreItem?>(null) }
    var completedGoalCelebration by remember { mutableStateOf<GoalEntity?>(null) }
    var isPiggyBankExpanded by remember(initialPiggyBankExpanded) { mutableStateOf(initialPiggyBankExpanded) }
    val listState = rememberLazyListState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val choresRequester = remember { BringIntoViewRequester() }

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

    LaunchedEffect(focusOnChores) {
        if (focusOnChores) {
            isPiggyBankExpanded = false
            delay(150)
            try {
                choresRequester.bringIntoView()
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

    val animTotalSaved by animateIntAsState(
        targetValue = totalSaved,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "home_total_saved"
    )

    PixelLocationModalSheet(
        title = "Уютный домик и Копилка",
        subtitle = "Выполняй домашние дела, зарабатывай монеты и копи на мечту",
        headerDrawableRes = R.drawable.house_background_empty,
        badgeContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$animTotalSaved",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PixelDarkBrown,
                    fontFamily = AppFontFamily
                )
                Spacer(modifier = Modifier.width(4.dp))
                CoinIcon(modifier = Modifier.size(18.dp))
            }
        },
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
                        Image(
                            painter = painterResource(id = R.drawable.saving_jar),
                            contentDescription = "Копилка на мечту",
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Копилка на мечту",
                                fontSize = 18.sp,
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
                            onDeposit = { goalId, amount ->
                                val target = goals.find { it.id == goalId }
                                if (target != null && (target.savedAmount + amount) >= target.targetCost) {
                                    completedGoalCelebration = target.copy(savedAmount = target.targetCost, isReached = true)
                                }
                                onDeposit(goalId, amount)
                            },
                            onWithdrawClick = { withdrawWarningGoal = it },
                            onCustomWithdraw = { goalId, amount -> onWithdraw(goalId, amount) }
                        )
                    }
                }
            }


            // Блок домашних дел (заданий)
            Column(
                modifier = Modifier.bringIntoViewRequester(choresRequester),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Задания и дела по дому",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                }

                Text(
                    text = "Выполняй задания по дому и получай монеты в награду:",
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

    // Поздравление с достижением цели накоплений
    completedGoalCelebration?.let { goal ->
        PixelAlertDialog(
            onDismissRequest = { completedGoalCelebration = null },
            title = "Мечта сбылась!",
            icon = "🎉",
            confirmButton = {
                PixelButton(
                    text = "В домик! 🏠",
                    onClick = {
                        completedGoalCelebration = null
                        onDismiss()
                    },
                    containerColor = FreshGreen,
                    textColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                PixelButton(
                    text = "Здорово!",
                    onClick = { completedGoalCelebration = null },
                    containerColor = PixelWoodMedium,
                    textColor = PixelParchmentLight,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GoalStickerIcon(goalId = goal.id, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Поздравляем!\nТы накопил на «${goal.title}»!",
                    fontFamily = AppFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PixelTextDark,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Твоя цель полностью достигнута, и теперь этот предмет украшает комнату твоего котика!",
                    fontFamily = AppFontFamily,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    color = PixelTextDark,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
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
