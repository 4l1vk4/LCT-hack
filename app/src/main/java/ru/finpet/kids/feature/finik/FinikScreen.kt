package ru.finpet.kids.feature.finik

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.OrbStatBar
import ru.finpet.kids.core.designsystem.SkyBlue
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.core.domain.calculator.PetEconomyCalculator
import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetPlan
import ru.finpet.kids.feature.budget.BudgetPlanVsFactDialog
import ru.finpet.kids.feature.budget.BudgetPlanningDialog
import ru.finpet.kids.feature.calendar.model.CalendarTaskCategory
import ru.finpet.kids.feature.finik.components.FinikRunawaySection
import ru.finpet.kids.feature.finik.sheets.DEFAULT_SCRIPTED_ADVICES
import ru.finpet.kids.feature.finik.sheets.FinikAdviceSheet
import ru.finpet.kids.feature.finik.sheets.FinikTasksSheet

// главный экран питомца (комната, состояние, диалоги, быстрый чек-лист дня)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinikScreen(
    profile: ProfileEntity?,
    goals: List<GoalEntity> = emptyList(),
    todayNotes: List<CalendarNoteEntity> = emptyList(),
    selectedSkin: String? = null,
    petName: String? = null,
    accessoryId: String = "none",
    onToggleAccessory: (String) -> Unit = {},
    animationsEnabled: Boolean = true,
    onPetTapped: () -> Unit = {},
    onNavigateToGoals: ((String?) -> Unit)? = null,
    onNavigateToMap: (() -> Unit)? = null,
    onNavigateToPlans: (() -> Unit)? = null,
    onNavigateToTask: ((CalendarNoteEntity) -> Unit)? = null,
    openAdvice: Boolean = false,
    onAdviceDismissed: () -> Unit = {},
    onAskQuestion: () -> Unit = {},
    onPlaceFoodBowl: () -> Unit = {},
    onToggleNote: (CalendarNoteEntity) -> Unit = {},
    isBudgetConfirmed: Boolean = true,
    currentPeriod: PeriodEntity? = null,
    onConfirmBudget: (BudgetPlan) -> Unit = {}
) {
    var panelOpen by remember { mutableStateOf(false) }

    LaunchedEffect(openAdvice) {
        if (openAdvice) {
            panelOpen = true
        }
    }

    var showWardrobe by remember { mutableStateOf(false) }
    var showTasksMenu by remember { mutableStateOf(false) }
    var showBudgetPlanningDialog by remember { mutableStateOf(false) }
    var showPlanVsFactDialog by remember { mutableStateOf(false) }

    val satiety = profile?.satiety ?: 80
    val health = profile?.health ?: 90
    val mood = profile?.mood ?: 70
    val isPetRunaway = profile?.isPetRunaway == true
    val displayName = petName ?: profile?.petName ?: "Финни"

    // считаем взвешенное настроение (60% эмоции + по 20% здоровье и сытость)
    val effectiveMood = remember(mood, satiety, health) {
        PetEconomyCalculator.calculateEffectiveMood(mood, satiety, health)
    }

    // подбираем первую реплику котика под его самочувствие
    val initialSpeech = remember(isPetRunaway, effectiveMood, displayName) {
        when {
            isPetRunaway -> "Мяу... Мне было так грустно, что я убежал. Поставь миску корма у двери!"
            effectiveMood >= 80 -> "Мурр! Я полон сил и готов копить на мечту вместе с тобой!"
            effectiveMood >= 50 -> "Привет! Не забудь покормить меня и проверить уроки в Школе!"
            else -> "Мяу... Мне немного грустно. Давай купим вкусняшку в Лавке?"
        }
    }
    var currentSpeech by remember(initialSpeech) { mutableStateOf(initialSpeech) }

    val activeGoal = remember(goals, profile?.activeGoalId) {
        goals.find { it.id == profile?.activeGoalId } ?: goals.firstOrNull()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 104.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // облачко речи котика сверху
            item(key = "speech_bubble") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isPetRunaway) Color(0xFFFFEBEE) else Color(0xFFFFF9E6))
                        .border(2.dp, if (isPetRunaway) Color(0xFFFFCDD2) else Color(0xFFFFE082), RoundedCornerShape(22.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (isPetRunaway) "😿 $currentSpeech" else "💬 $currentSpeech",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )
                }
            }

            // сам котик в кресле либо плашка сбежавшего котика
            item(key = "finik_character") {
                if (isPetRunaway) {
                    FinikRunawaySection(
                        petName = displayName,
                        bowlPlacedToday = profile?.bowlPlacedToday == true,
                        balance = profile?.balance ?: 0,
                        onPlaceFoodBowl = onPlaceFoodBowl
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        FinikInArmchair(
                            modifier = Modifier.padding(vertical = 4.dp),
                            skinId = selectedSkin ?: "cat_black",
                            accessoryId = accessoryId,
                            mood = when {
                                effectiveMood < 40 -> "SAD"
                                effectiveMood >= 70 -> "HAPPY"
                                else -> "NEUTRAL"
                            },
                            stage = profile?.growthStage ?: "BABY",
                            enabled = animationsEnabled,
                            onClick = {
                                onPetTapped()
                                panelOpen = true
                            }
                        )

                        // круглая кнопка гардероба в правом нижнем углу котика
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 8.dp, bottom = 8.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(JoyOrange)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { showWardrobe = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👔", fontSize = 22.sp)
                        }
                    }
                }
            }

            // карточка с тремя индикаторами состояния (сытость, энергия, здоровье)
            item(key = "stats_panel") {
                FinCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 6.dp),
                    backgroundColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Состояние $displayName",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "$effectiveMood%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = remember(effectiveMood) {
                                    when {
                                        effectiveMood >= 70 -> FreshGreen
                                        effectiveMood >= 40 -> JoyOrange
                                        else -> Color(0xFFFF5252)
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        OrbStatBar(
                            label = "Сытость",
                            icon = "🍎",
                            value = satiety,
                            tintColor = JoyOrange,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OrbStatBar(
                            label = "Энергия",
                            icon = "⚡",
                            value = mood,
                            tintColor = SkyBlue,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OrbStatBar(
                            label = "Здоровье",
                            icon = "❤️",
                            value = health,
                            tintColor = FreshGreen,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Следи за всеми тремя — от них зависит общее настроение!",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // карточка цели накопления (копилка)
            item(key = "goal_bar") {
                if (activeGoal != null) {
                    val progress = (activeGoal.savedAmount.toFloat() / activeGoal.targetCost.toFloat()).coerceIn(0f, 1f)
                    val percent = (progress * 100).toInt()
                    val goalEmoji = when (activeGoal.id) {
                        "goal_ball" -> "⚽"
                        "goal_headphones" -> "🎧"
                        "goal_scooter" -> "🛴"
                        else -> "🎯"
                    }
                    val animatedGoalProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(durationMillis = 500),
                        label = "goal_anim"
                    )
                    val goalBrush = remember {
                        Brush.horizontalGradient(listOf(Color(0xFFFFB74D), JoyOrange))
                    }

                    FinCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .then(if (onNavigateToGoals != null) Modifier.clickable { onNavigateToGoals(activeGoal.id) } else Modifier),
                        backgroundColor = Color.White
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Text(text = goalEmoji, fontSize = 17.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Цель: ${activeGoal.title}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${activeGoal.savedAmount} / ${activeGoal.targetCost} ",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = JoyOrange
                                    )
                                    CoinIcon(modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "($percent%)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeGoal.isReached) FreshGreen else TextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(7.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(13.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(Color(0xFFEEEEEE))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(animatedGoalProgress)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(goalBrush)
                                )
                            }
                        }
                    }
                }
            }

            // карточка задачи дня под копилкой
            item(key = "daily_task_card") {
                val pendingTask = todayNotes.firstOrNull { !it.isCompleted }
                val completedCount = todayNotes.count { it.isCompleted }
                val totalCount = todayNotes.size

                if (todayNotes.isNotEmpty()) {
                    FinCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { showTasksMenu = true },
                        backgroundColor = if (pendingTask == null) Color(0xFFF1F8E9) else Color.White,
                        borderColor = if (pendingTask == null) Color(0xFFAED581) else Color(0xFFFFD54F)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = if (pendingTask == null) "🌟" else "📋", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (pendingTask == null) "Все задачи выполнены!" else "Задача дня",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pendingTask == null) FreshGreen else TextPrimary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (pendingTask == null) Color(0xFFDCEDC8) else Color(0xFFFFF3E0),
                                    modifier = Modifier.clickable { showTasksMenu = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$completedCount/$totalCount",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (pendingTask == null) FreshGreen else JoyOrange
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(text = "👉", fontSize = 10.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (pendingTask != null) {
                                val category = CalendarTaskCategory.fromId(pendingTask.category)
                                val taskIcon = category.icon
                                val taskBadge = when {
                                    pendingTask.category == "GROCERIES" -> "Лавка • Сдача"
                                    pendingTask.category == "SCHOOL" -> "Школа • 2 урока"
                                    pendingTask.category == "PET_FOOD" -> "Лавка • 30"
                                    else -> category.defaultBadge
                                }

                                val handleTaskClick = {
                                    if (pendingTask.category == "PET_QUESTION") {
                                        panelOpen = true
                                    } else if (onNavigateToTask != null) {
                                        onNavigateToTask(pendingTask)
                                    } else {
                                        onNavigateToMap?.invoke()
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFFDE7))
                                        .border(1.dp, Color(0xFFFFF59D), RoundedCornerShape(8.dp))
                                        .clickable { handleTaskClick() }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = taskIcon, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = pendingTask.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = JoyOrange,
                                        modifier = Modifier.clickable { handleTaskClick() }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            if (pendingTask.category == "PET_FOOD") {
                                                Text(
                                                    text = "Лавка • 30",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                CoinIcon(modifier = Modifier.size(10.dp))
                                            } else if (pendingTask.cost > 0 && !category.isSystem) {
                                                Text(
                                                    text = "${pendingTask.cost}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                CoinIcon(modifier = Modifier.size(10.dp))
                                            } else {
                                                Text(
                                                    text = taskBadge,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(text = "👉", fontSize = 9.sp)
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "Все дела и уроки сделаны, Финни счастлив! 🎉",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // шторка диалогов и вопросов питомцу
        if (panelOpen) {
            FinikAdviceSheet(
                displayName = displayName,
                advices = DEFAULT_SCRIPTED_ADVICES,
                onSelectAdvice = { response ->
                    onAskQuestion()
                    currentSpeech = response
                    panelOpen = false
                    onAdviceDismissed()
                },
                onDismiss = {
                    panelOpen = false
                    onAdviceDismissed()
                }
            )
        }

        // шторка списка задач на сегодня
        if (showTasksMenu) {
            FinikTasksSheet(
                todayNotes = todayNotes,
                onToggleNote = onToggleNote,
                onNavigateToMap = onNavigateToMap,
                onNavigateToTask = onNavigateToTask,
                onOpenQuestions = { panelOpen = true },
                onDismiss = { showTasksMenu = false }
            )
        }

        // диалог планирования бюджета (3 корзины, ТЗ 2.5.5)
        if (showBudgetPlanningDialog) {
            val initialPlan = remember(currentPeriod) {
                BudgetPlan(
                    plannedMandatory = currentPeriod?.plannedMandatory ?: 0,
                    plannedOptional = currentPeriod?.plannedOptional ?: 0,
                    plannedSavings = currentPeriod?.plannedSavings ?: 0
                )
            }
            BudgetPlanningDialog(
                balance = profile?.balance ?: 0,
                currentDay = profile?.currentPeriodIndex ?: 1,
                initialPlan = initialPlan,
                isConfirmed = isBudgetConfirmed,
                onConfirm = { newPlan ->
                    onConfirmBudget(newPlan)
                    showBudgetPlanningDialog = false
                },
                onDismiss = { showBudgetPlanningDialog = false }
            )
        }

        // диалог «План vs Факт» со звездами бюджета
        if (showPlanVsFactDialog && currentPeriod != null) {
            val plan = remember(currentPeriod) {
                BudgetPlan(
                    plannedMandatory = currentPeriod.plannedMandatory,
                    plannedOptional = currentPeriod.plannedOptional,
                    plannedSavings = currentPeriod.plannedSavings
                )
            }
            val actual = remember(currentPeriod) {
                ActualExpenses(
                    actualMandatory = currentPeriod.actualMandatory,
                    actualOptional = currentPeriod.actualOptional,
                    actualSavings = currentPeriod.actualSavings
                )
            }
            val compliance = remember(plan, actual) {
                PetEconomyCalculator.calculateCompliance(plan, actual)
            }
            BudgetPlanVsFactDialog(
                currentDay = profile?.currentPeriodIndex ?: 1,
                plan = plan,
                actual = actual,
                compliance = compliance,
                onEditPlan = if (isBudgetConfirmed) null else {
                    {
                        showPlanVsFactDialog = false
                        showBudgetPlanningDialog = true
                    }
                },
                onDismiss = { showPlanVsFactDialog = false }
            )
        }

        // диалог гардероба (шарфик, бантик, кепка)
        if (showWardrobe) {
            WardrobeDialog(
                currentAccessoryIds = parseAccessories(accessoryId).map { it.id }.toSet(),
                petName = displayName,
                onToggle = { accId -> onToggleAccessory(accId) },
                onDismiss = { showWardrobe = false }
            )
        }
    }
}
