package ru.finpet.kids.feature.finik

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.domain.calculator.PetEconomyCalculator
import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetPlan
import ru.finpet.kids.feature.budget.BudgetPlanVsFactDialog
import ru.finpet.kids.feature.budget.BudgetPlanningDialog
import ru.finpet.kids.feature.finik.components.FinikDailyTaskCard
import ru.finpet.kids.feature.finik.components.FinikGoalCard
import ru.finpet.kids.feature.finik.components.FinikRunawaySection
import ru.finpet.kids.feature.finik.components.FinikStatsCard
import ru.finpet.kids.feature.finik.sheets.DEFAULT_SCRIPTED_ADVICES
import ru.finpet.kids.feature.finik.sheets.FinikAdviceSheet
import ru.finpet.kids.feature.finik.sheets.FinikTasksSheet

/**
 * Главный экран питомца Финни:
 * - Комната с анимированным котиком в кресле или секция возвращения сбежавшего питомца.
 * - Карточка характеристик (сытость, энергия, здоровье) без лишних эффектов.
 * - Карточка активной цели накопления с улучшенным контрастным прогресс-баром.
 * - Карточка задачи дня в строгом пиксельном стиле без оранжевых рамок и без эффектов ховера.
 */
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

    // Взвешенное общее самочувствие котика
    val effectiveMood = remember(mood, satiety, health) {
        PetEconomyCalculator.calculateEffectiveMood(mood, satiety, health)
    }

    // Реплика при запуске экрана
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8DAC2)) // Тёплый фермерский фон Stardew
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 104.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Облачко мыслей котика в аутентичном 9-slice пиксельном стиле
            item(key = "speech_bubble") {
                PixelPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
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

            // Котик в комнате либо секция возврата сбежавшего котика
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

                        // Кнопка гардероба в правом нижнем углу
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

            // Карточка характеристик питомца
            item(key = "stats_panel") {
                FinikStatsCard(
                    displayName = displayName,
                    effectiveMood = effectiveMood,
                    satiety = satiety,
                    mood = mood,
                    health = health,
                    isPetRunaway = isPetRunaway
                )
            }

            // Карточка активной цели накопления
            item(key = "goal_bar") {
                if (activeGoal != null) {
                    FinikGoalCard(
                        activeGoal = activeGoal,
                        onNavigateToGoals = onNavigateToGoals
                    )
                }
            }

            // Карточка задачи дня
            item(key = "daily_task_card") {
                FinikDailyTaskCard(
                    todayNotes = todayNotes,
                    onShowTasksMenu = { showTasksMenu = true },
                    onNavigateToTask = onNavigateToTask,
                    onNavigateToMap = onNavigateToMap,
                    onOpenQuestions = { panelOpen = true }
                )
            }
        }

        // Шторка диалогов и вопросов
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

        // Шторка задач на день
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

        // Диалог планирования бюджета
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

        // Диалог «План vs Факт»
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

        // Диалог гардероба питомца
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
