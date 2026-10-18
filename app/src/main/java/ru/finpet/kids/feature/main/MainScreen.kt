package ru.finpet.kids.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import ru.finpet.kids.core.designsystem.SoftBackground
import ru.finpet.kids.feature.adult.AdultScreen
import ru.finpet.kids.feature.calendar.CalendarScreen
import ru.finpet.kids.feature.finik.FinikScreen
import ru.finpet.kids.feature.map.MapScreen
import ru.finpet.kids.feature.calendar.CalendarMonthInfo
import ru.finpet.kids.feature.settings.SettingsScreen
import ru.finpet.kids.core.domain.calculator.PetEconomyCalculator
import ru.finpet.kids.core.domain.model.BudgetPlan
import ru.finpet.kids.feature.budget.BudgetPlanningDialog
import ru.finpet.kids.feature.budget.PeriodAnalysisDialog
import ru.finpet.kids.feature.finik.dialogs.PetGrowthDialog
import java.time.YearMonth
import androidx.compose.foundation.layout.WindowInsets
import ru.finpet.kids.R
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.feature.onboarding.SkinPickerScreen
import ru.finpet.kids.feature.onboarding.PetNameScreen
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import ru.finpet.kids.core.designsystem.LocalBottomBarHeight
import androidx.compose.runtime.mutableStateOf
import ru.finpet.kids.core.designsystem.MoneyNotification
import kotlin.math.roundToInt
import ru.finpet.kids.feature.onboarding.TutorialScreen

@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val onboarding by viewModel.onboardingState.collectAsStateWithLifecycle()

    when (val state = onboarding) {
        is OnboardingState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFA726))
            )
            return
        }
        is OnboardingState.NeedSkin -> {
            SkinPickerScreen(
                onSkinSelected = { skinId -> viewModel.setSkin(skinId) }
            )
            return
        }
        is OnboardingState.NeedName -> {
            PetNameScreen(
                onNameConfirmed = { name -> viewModel.setPetName(name) }
            )
            return
        }
        is OnboardingState.NeedTutorial -> {
            TutorialScreen(
                petName = state.petName,
                skinId = state.skinId,
                onFinish = { viewModel.completeTutorial() }
            )
            return
        }
        is OnboardingState.Ready -> {
        }
    }

    val ready = onboarding as? OnboardingState.Ready

    var selectedTab by rememberSaveable { mutableIntStateOf(2) } // по умолчанию вкладка финни
    var previousTabBeforeSettings by rememberSaveable { mutableIntStateOf(2) }
    val showWardrobe = remember { mutableStateOf(false) }
    var navBarHeight by remember { mutableStateOf(140.dp) }
    val density = LocalDensity.current

    // единые подписки на данные профиля и состояния
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val quests by viewModel.quests.collectAsStateWithLifecycle()
    val questProgress by viewModel.questProgress.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val currentPeriod by viewModel.currentPeriod.collectAsStateWithLifecycle()
    val calendarNotes by viewModel.calendarNotes.collectAsStateWithLifecycle()
    val recurringExpenses by viewModel.recurringExpenses.collectAsStateWithLifecycle()
    val report by viewModel.competencyReport.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val musicEnabled by viewModel.musicEnabled.collectAsStateWithLifecycle()
    val musicVolume by viewModel.musicVolume.collectAsStateWithLifecycle()
    val demoMode by viewModel.demoMode.collectAsStateWithLifecycle()
    val isNightMode by viewModel.isNightMode.collectAsStateWithLifecycle()
    val virtualDate by viewModel.virtualDate.collectAsStateWithLifecycle()
    val calendarMonthInfo = remember(virtualDate) { CalendarMonthInfo.of(YearMonth.from(virtualDate)) }
    val moneyEvent by viewModel.moneyEvent.collectAsStateWithLifecycle()
    val periodAnalysis by viewModel.periodAnalysisEvent.collectAsStateWithLifecycle()
    val petGrowthPetName by viewModel.petGrowthEvent.collectAsStateWithLifecycle()
    val preorderItemIds by viewModel.preorderItemIds.collectAsStateWithLifecycle()
    val adultStageStartDay by viewModel.adultStageStartDay.collectAsStateWithLifecycle()
    var showBudgetPlanningDialog by remember { mutableStateOf(false) }
    var helpPanelOpen by remember { mutableStateOf(false) }
    var showTutorialFromMenu by remember { mutableStateOf(false) }
    var mapInitialModal by remember { mutableStateOf<String?>(null) }
    var mapTargetGoalId by remember { mutableStateOf<String?>(null) }
    var mapTargetShopItemId by remember { mutableStateOf<String?>(null) }
    var mapTargetQuestId by remember { mutableStateOf<String?>(null) }
    var finikOpenAdvice by remember { mutableStateOf(false) }

    // переход в настройки с сохранением предыдущей вкладки
    val openSettings: () -> Unit = {
        if (selectedTab != 4) {
            previousTabBeforeSettings = selectedTab
        }
        selectedTab = 4
    }

    // закрытие настроек и возврат к предыдущей вкладке
    val closeSettings: () -> Unit = {
        selectedTab = if (previousTabBeforeSettings != 4) previousTabBeforeSettings else 2
    }

    // переход из задач дня в целевое здание с фокусом на действие
    val onNavigateToTask: (CalendarNoteEntity) -> Unit = { note ->
        when (note.category) {
            "BUDGET_PLAN" -> {
                showBudgetPlanningDialog = true
            }
            "PET_FOOD" -> {
                if (profile?.isPetRunaway == true) {
                    selectedTab = 2
                } else {
                    mapInitialModal = "SHOP"
                    mapTargetShopItemId = "food_basic"
                    selectedTab = 0
                }
            }
            "GROCERIES" -> {
                mapInitialModal = "SHOP"
                mapTargetShopItemId = "groceries_parents"
                selectedTab = 0
            }
            "SCHOOL" -> {
                val nextQuest = quests.firstOrNull { q ->
                    questProgress.none { it.questId == q.id && it.isCompleted }
                } ?: quests.firstOrNull()
                mapInitialModal = "QUESTS"
                mapTargetQuestId = nextQuest?.id
                selectedTab = 0
            }
            "SAVINGS" -> {
                mapInitialModal = "GOALS"
                mapTargetGoalId = profile?.activeGoalId ?: goals.firstOrNull()?.id
                selectedTab = 0
            }
            "PET_QUESTION" -> {
                finikOpenAdvice = true
                selectedTab = 2
            }
            else -> {
                selectedTab = 0
            }
        }
    }

    // сохранение состояния вкладок после первого показа
    val seenTabs = remember { mutableStateListOf(2) }
    if (!seenTabs.contains(selectedTab)) seenTabs.add(selectedTab)
    LaunchedEffect(Unit) {
        for (i in 0..4) {
            if (!seenTabs.contains(i)) {
                seenTabs.add(i)
                // постепенный прогрев вкладок по кадрам
                delay(48)
            }
        }
    }
    val stateHolder = rememberSaveableStateHolder()
    val onSelectTab: (Int) -> Unit = remember { { selectedTab = it } }

    // обновление отчета компетенций при переходе во вкладку родителей
    LaunchedEffect(selectedTab) {
        if (selectedTab == 3) viewModel.refreshReport()
    }

    val balance = profile?.balance ?: 50
    val currentTabBgColor = when (selectedTab) {
        0 -> if (isNightMode) Color(0xFF0D0D1A) else SoftBackground
        1 -> Color(0xFFE8DAC2) // теплый фон календаря
        else -> SoftBackground
    }
    CompositionLocalProvider(LocalBottomBarHeight provides navBarHeight) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = currentTabBgColor,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    if (selectedTab != 0) {
                        TopBarWithHotspots(
                            topBarRes = R.drawable.top,
                            imageAspectRatio = 1073f / 145f,
                            balance = balance,
                            moodPercent = profile?.let { p ->
                                PetEconomyCalculator.calculateEffectiveMood(p.mood, p.satiety, p.health)
                            } ?: 70,
                            onOpenInstructions = { showTutorialFromMenu = true },
                            onOpenSettings = openSettings,
                            menuExpanded = helpPanelOpen,
                            onToggleMenu = { helpPanelOpen = !helpPanelOpen },
                            debug = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (helpPanelOpen) {
                                        // затемнение фона за верхней панелью под цвет оверлея
                                        Color.Black.copy(alpha = 0.25f)
                                    } else {
                                        currentTabBgColor
                                    }
                                )
                                .padding(top = 0.dp)
                        )
                    }
                }
                // нижняя навигация рисуется поверх контента
            ) { innerPadding ->
                val contentTopPadding = if (selectedTab == 0) 0.dp else innerPadding.calculateTopPadding()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = contentTopPadding)
                ) {
                    val currentDayIndex = profile?.currentPeriodIndex ?: 1
                    val todayNotes = remember(calendarNotes, currentDayIndex) {
                        calendarNotes.filter { it.dayIndex == currentDayIndex }
                    }
                    KeepAliveTab(
                        visible = selectedTab == 0,
                        seen = seenTabs.contains(0),
                        key = 0,
                        stateHolder = stateHolder
                    ) {
                        MapScreen(
                            profile = profile,
                            currentPeriod = currentPeriod,
                            purchases = purchases,
                            quests = quests,
                            questProgress = questProgress,
                            goals = goals,
                            todayNotes = todayNotes,
                            initialModal = mapInitialModal,
                            targetGoalId = mapTargetGoalId,
                            targetShopItemId = mapTargetShopItemId,
                            targetQuestId = mapTargetQuestId,
                            isNight = isNightMode,
                            demoMode = demoMode,
                            adultStageStartDay = adultStageStartDay,
                            preorderItemIds = preorderItemIds,
                            onTogglePreorderItem = { viewModel.togglePreorderItem(it) },
                            onQuickBuyItems = { viewModel.quickBuyItems(it) },
                            onModalDismissed = {
                                mapInitialModal = null
                                mapTargetGoalId = null
                                mapTargetShopItemId = null
                                mapTargetQuestId = null
                            },
                            onBuyItem = { viewModel.buyItem(it) },
                            onAnswerQuest = { qId, optId -> viewModel.answerQuest(qId, optId) },
                            onDepositGoal = { gId, amt -> viewModel.depositGoal(gId, amt) },
                            onWithdrawGoal = { gId, amt -> viewModel.withdrawGoal(gId, amt) },
                            onSelectActiveGoal = { gId -> viewModel.selectActiveGoal(gId) },
                            onCompleteHomeChore = { choreId, coins ->
                                viewModel.completeHomeChore(
                                    choreId,
                                    coins
                                )
                            },
                            onNavigateToFinik = { selectedTab = 2 }
                        )
                    }
                    KeepAliveTab(
                        visible = selectedTab == 1,
                        seen = seenTabs.contains(1),
                        key = 1,
                        stateHolder = stateHolder
                    ) {
                        CalendarScreen(
                            profile = profile,
                            currentPeriod = currentPeriod,
                            calendarNotes = calendarNotes,
                            recurringExpenses = recurringExpenses,
                            monthInfo = calendarMonthInfo,
                            onConfirmBudget = { plan -> viewModel.confirmBudget(plan) },
                            onCompletePeriod = { viewModel.completePeriod() },
                            onAddNote = { day, title, cost, cat ->
                                viewModel.addCalendarNote(
                                    day,
                                    title,
                                    cost,
                                    cat
                                )
                            },
                            onDeleteNote = { note -> viewModel.deleteCalendarNote(note) },
                            onToggleNote = { note -> viewModel.toggleCalendarNote(note) },
                            onNavigateToTask = onNavigateToTask,
                            onCompleteChecklistTask = { note, reward ->
                                viewModel.completeChecklistTask(
                                    note,
                                    reward
                                )
                            },
                            onAddRecurring = { title, cost, freq, icon ->
                                viewModel.addRecurringExpense(
                                    title,
                                    cost,
                                    freq,
                                    icon
                                )
                            },
                            onDeleteRecurring = { exp -> viewModel.deleteRecurringExpense(exp) },
                            demoMode = demoMode
                        )
                    }
                    KeepAliveTab(
                        visible = selectedTab == 2,
                        seen = seenTabs.contains(2),
                        key = 2,
                        stateHolder = stateHolder
                    ) {
                        FinikScreen(
                            profile = profile,
                            goals = goals,
                            todayNotes = todayNotes,
                            selectedSkin = ready?.skinId,
                            petName = ready?.petName,
                            accessoryId = profile?.accessoryId ?: "none",
                            onToggleAccessory = { accId -> viewModel.toggleAccessory(accId) },
                            // анимации персонажа включены только на активной вкладке
                            animationsEnabled = selectedTab == 2,
                            isBudgetConfirmed = currentPeriod?.isBudgetConfirmed == true,
                            currentPeriod = currentPeriod,
                            openAdvice = finikOpenAdvice,
                            onAdviceDismissed = { finikOpenAdvice = false },
                            onConfirmBudget = { plan -> viewModel.confirmBudget(plan) },
                            onNavigateToPlans = { selectedTab = 1 },
                            onNavigateToGoals = { goalId ->
                                mapInitialModal = "GOALS"
                                mapTargetGoalId = goalId
                                selectedTab = 0
                            },
                            onNavigateToMap = { selectedTab = 0 },
                            onNavigateToTask = onNavigateToTask,
                            onAskQuestion = { viewModel.onPetAskedQuestion() },
                            onPlaceFoodBowl = { viewModel.placeFoodBowl() },
                            onToggleNote = { note -> viewModel.toggleCalendarNote(note) },
                            isNight = isNightMode
                        )
                    }
                    KeepAliveTab(
                        visible = selectedTab == 3,
                        seen = seenTabs.contains(3),
                        key = 3,
                        stateHolder = stateHolder
                    ) {
                        AdultScreen(
                            report = report,
                            adultSectionUseCase = viewModel.adultSectionUseCase,
                            onGrantBonus = { coins, reason ->
                                viewModel.grantParentBonus(
                                    coins,
                                    reason
                                )
                            },
                            onResetProfile = { viewModel.resetDemoProfile() }
                        )
                    }
                    KeepAliveTab(
                        visible = selectedTab == 4,
                        seen = seenTabs.contains(4),
                        key = 4,
                        stateHolder = stateHolder
                    ) {
                        SettingsScreen(
                            soundEnabled = soundEnabled,
                            musicEnabled = musicEnabled,
                            musicVolume = musicVolume,
                            demoMode = demoMode,
                            onToggleSound = { viewModel.toggleSound(it) },
                            onToggleMusic = { viewModel.toggleMusic(it) },
                            onMusicVolumeChange = { viewModel.setMusicVolume(it) },
                            onToggleDemoMode = { viewModel.toggleDemo(it) },
                            isNightMode = isNightMode,
                            onToggleNightMode = { viewModel.toggleNightMode(it) },
                            onClose = closeSettings
                        )
                    }
                    // нижняя панель поверх контента
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* блокировка прокликивания сквозь нижнюю панель */ }
                    ) {
                        val isPetRunaway = profile?.isPetRunaway == true
                        val calendarHasWarning = currentPeriod?.isBudgetConfirmed != true
                        val mapHasWarning = !isPetRunaway && ((profile?.satiety ?: 100) <= 30 || (profile?.mood ?: 100) <= 30 || (profile?.health ?: 100) <= 30)
                        NavBarWithHotspots(
                            navRes = R.drawable.menu,
                            imageAspectRatio = 1073f / 174f,
                            hotspots = DEFAULT_NAV_HOTSPOTS,
                            onSelectTab = onSelectTab,
                            selectedTab = selectedTab,
                            showImage = selectedTab != 0,
                            calendarHasWarning = calendarHasWarning,
                            mapHasWarning = mapHasWarning,
                            debug = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coords ->
                                    navBarHeight = with(density) { coords.size.height.toDp() }
                                }
                        )
                    }

                    moneyEvent?.let { event ->
                        MoneyNotification(
                            amount = event.amount,
                            source = event.source,
                            icon = event.icon,
                            onDismiss = { viewModel.dismissMoneyEvent() },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 12.dp)
                        )
                    }

                    // оверлей справки поверх контента
                    if (helpPanelOpen) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.25f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { helpPanelOpen = false },
                            contentAlignment = Alignment.TopStart
                        ) {
                            HelpPanelWithHotspots(
                                panelRes = R.drawable.top_fall,
                                imageAspectRatio = 1073f / 104f,
                                onInstructions = {
                                    helpPanelOpen = false
                                    showTutorialFromMenu = true
                                },
                                onSettings = {
                                    helpPanelOpen = false
                                    openSettings()
                                },
                                onClose = { helpPanelOpen = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 0.dp),
                                debug = false
                            )
                        }
                    }
                }
            }
            if (showTutorialFromMenu) {
                val currentSkinId = ready?.skinId ?: "cat_black"
                TutorialScreen(
                    petName = ready?.petName ?: profile?.petName ?: "Финни",
                    skinId = currentSkinId,
                    onFinish = { showTutorialFromMenu = false }
                )
            }

            // всплывающий диалог анализа периода
            periodAnalysis?.let { resolution ->
                PeriodAnalysisDialog(
                    resolution = resolution,
                    onConfirm = {
                        viewModel.dismissPeriodAnalysis()
                        showBudgetPlanningDialog = true
                    }
                )
            }

            // диалог взросления питомца
            if (periodAnalysis == null && petGrowthPetName != null) {
                PetGrowthDialog(
                    petName = petGrowthPetName ?: "Питомец",
                    onDismiss = { viewModel.dismissPetGrowthEvent() }
                )
            }

            // диалог планирования бюджета на день
            if (showBudgetPlanningDialog) {
                val currentPeriodVal = currentPeriod
                val initialPlan = remember(currentPeriodVal) {
                    BudgetPlan(
                        plannedMandatory = currentPeriodVal?.plannedMandatory ?: 0,
                        plannedOptional = currentPeriodVal?.plannedOptional ?: 0,
                        plannedSavings = currentPeriodVal?.plannedSavings ?: 0
                    )
                }
                val currentDay = profile?.currentPeriodIndex ?: 1
                val isConfirmed = currentPeriod?.isBudgetConfirmed == true
                BudgetPlanningDialog(
                    balance = balance,
                    currentDay = currentDay,
                    initialPlan = initialPlan,
                    isConfirmed = isConfirmed,
                    onConfirm = { plan ->
                        viewModel.confirmBudget(plan)
                        showBudgetPlanningDialog = false
                    },
                    onDismiss = { showBudgetPlanningDialog = false }
                )
            }
        }
    }
}

/**
 * сохранение композиции вкладки после первого показа.
 */
@Composable
private fun KeepAliveTab(
    visible: Boolean,
    seen: Boolean,
    key: Int,
    stateHolder: SaveableStateHolder,
    content: @Composable () -> Unit
) {
    if (!seen) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (visible) {
                    Modifier
                } else {
                    Modifier
                        .alpha(0f)
                        .layout { measurable, _ ->
                            val placeable = measurable.measure(Constraints.fixed(0, 0))
                            layout(0, 0) { placeable.place(0, 0) }
                        }
                }
            )
    ) {
        stateHolder.SaveableStateProvider(key) {
            content()
        }
    }
}
