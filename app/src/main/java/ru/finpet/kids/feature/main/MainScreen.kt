package ru.finpet.kids.feature.main

import androidx.compose.foundation.background
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
import ru.finpet.kids.core.designsystem.CoinBadge
import ru.finpet.kids.core.designsystem.SoftBackground
import ru.finpet.kids.feature.adult.AdultScreen
import ru.finpet.kids.feature.calendar.CalendarScreen
import ru.finpet.kids.feature.finik.FinikScreen
import ru.finpet.kids.feature.map.MapScreen
import ru.finpet.kids.feature.settings.SettingsScreen
import androidx.compose.foundation.layout.WindowInsets
import ru.finpet.kids.R
import ru.finpet.kids.feature.onboarding.SkinPickerScreen
import ru.finpet.kids.feature.onboarding.PetNameScreen
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import ru.finpet.kids.core.designsystem.LocalBottomBarHeight
import androidx.compose.runtime.mutableStateOf

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
        is OnboardingState.Ready -> {
        }
    }

    val ready = onboarding as? OnboardingState.Ready

    var selectedTab by rememberSaveable { mutableIntStateOf(2) } // По умолчанию вкладка 3: Финни в кресле
    var navBarHeight by remember { mutableStateOf(140.dp) }
    val density = LocalDensity.current

    // Все подписки — один раз наверху и lifecycle-aware. Раньше collectAsState()
    // создавались внутри веток when(tab): каждое переключение открывало новых
    // коллекторов и перезапускало чтение с диска. Теперь данные общие и тёплые.
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

    // Keep-alive вкладок: один раз показанная вкладка остаётся в композиции
    // (состояние скролла, диалоги, введённый текст), переключение — только
    // смена видимости, без новой композиции списков. Стартуем только с Финни,
    // остальные прогреваем по одной после первого кадра, чтобы не ронять TTID.
    val seenTabs = remember { mutableStateListOf(2) }
    if (!seenTabs.contains(selectedTab)) seenTabs.add(selectedTab)
    LaunchedEffect(Unit) {
        for (i in 0..4) {
            if (!seenTabs.contains(i)) {
                seenTabs.add(i)
                // Пауза между прогревами: каждая вкладка компонуется в своём кадре
                delay(48)
            }
        }
    }
    val stateHolder = rememberSaveableStateHolder()
    val onSelectTab: (Int) -> Unit = remember { { selectedTab = it } }

    // Отчёт компетенций пересчитывается только когда вкладка «Родителям»
    // действительно открыта, а не после каждого действия в других вкладках.
    LaunchedEffect(selectedTab) {
        if (selectedTab == 3) viewModel.refreshReport()
    }

    val balance = profile?.balance ?: 50
    CompositionLocalProvider(LocalBottomBarHeight provides navBarHeight) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = SoftBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                // Показываем топбар везде, кроме вкладки «Карта» (индекс 0)
                if (selectedTab != 0) {
                    TopBar(balance = balance)
                }
            }
            // Нижнего бара в Scaffold нет: стеклянный док парит ПОВЕРХ контента,
            // списки уже имеют нижний отступ 84dp и просвечивают сквозь стекло
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                KeepAliveTab(
                    visible = selectedTab == 0,
                    seen = seenTabs.contains(0),
                    key = 0,
                    stateHolder = stateHolder
                ) {
                    MapScreen(
                        profile = profile,
                        purchases = purchases,
                        quests = quests,
                        questProgress = questProgress,
                        goals = goals,
                        onBuyItem = { viewModel.buyItem(it) },
                        onAnswerQuest = { qId, optId -> viewModel.answerQuest(qId, optId) },
                        onDepositGoal = { gId, amt -> viewModel.depositGoal(gId, amt) },
                        onWithdrawGoal = { gId, amt -> viewModel.withdrawGoal(gId, amt) },
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
                        onAddRecurring = { title, cost, freq, icon ->
                            viewModel.addRecurringExpense(
                                title,
                                cost,
                                freq,
                                icon
                            )
                        },
                        onDeleteRecurring = { exp -> viewModel.deleteRecurringExpense(exp) }
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
                        selectedSkin = ready?.skinId,
                        petName = ready?.petName,
                        // Анимации персонажа работают только на видимой вкладке —
                        // скрытый Финни не тратит CPU на бесконечные перерисовки
                        animationsEnabled = selectedTab == 2,
                        onPetTapped = { /* Можно добавить звук мурлыканья */ },
                        onNavigateToGoals = { selectedTab = 0 }
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
                        onNextDemoPeriod = { viewModel.completePeriod() },
                        onResetProfile = { viewModel.resetDemoProfile() },
                        demoMode = demoMode,
                        onToggleDemoMode = { enabled -> viewModel.toggleDemo(enabled) }
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
                        onToggleSound = { viewModel.toggleSound(it) },
                        onToggleMusic = { viewModel.toggleMusic(it) },
                        onMusicVolumeChange = { viewModel.setMusicVolume(it) }
                    )
                }
                // Стеклянный док поверх контента: последний в Box = рисуется сверху
                NavBarWithHotspots(
                    navRes = R.drawable.menu,
                    imageAspectRatio = 1073f / 278f,
                    hotspots = DEFAULT_NAV_HOTSPOTS,
                    onSelectTab = onSelectTab,
                    debug = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .onGloballyPositioned { coords ->
                            navBarHeight = with(density) { coords.size.height.toDp() }
                        }
                )
            }
        }
    }
}

/**
 * Вкладка, сохраняющая композицию после первого показа.
 * Скрытая вкладка измеряется в 0x0: не рисуется, не перехватывает тачи,
 * но композиция, скролл и SaveableState остаются живы.
 * Переключение = только relayout, без рекомпозиции контента.
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

@Composable
private fun TopBar(
    balance: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🐾", fontSize = 24.sp)
            CoinBadge(coins = balance)
        }
    }
}
