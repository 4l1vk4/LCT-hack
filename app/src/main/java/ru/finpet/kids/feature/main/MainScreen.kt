package ru.finpet.kids.feature.main

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import ru.finpet.kids.core.designsystem.CoinBadge
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.SoftBackground
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.feature.adult.AdultScreen
import ru.finpet.kids.feature.calendar.CalendarScreen
import ru.finpet.kids.feature.finik.FinikScreen
import ru.finpet.kids.feature.map.MapScreen
import ru.finpet.kids.feature.settings.SettingsScreen

data class NavItem(
    val title: String,
    val icon: String
)

private val NAV_ITEMS = listOf(
    NavItem("Карта", "🗺️"),
    NavItem("Календарь", "📅"),
    NavItem("Финни", "🐱"),
    NavItem("Родителям", "👨‍👩‍👦"),
    NavItem("Настройки", "⚙️")
)

@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(2) } // По умолчанию вкладка 3: Финни в кресле

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

    val balance = profile?.balance ?: 50

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SoftBackground,
        topBar = {
            TopBar(balance = balance)
        }
        // Нижнего бара в Scaffold нет: стеклянный док парит ПОВЕРХ контента,
        // списки уже имеют нижний отступ 84dp и просвечивают сквозь стекло
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            KeepAliveTab(visible = selectedTab == 0, seen = seenTabs.contains(0), key = 0, stateHolder = stateHolder) {
                MapScreen(
                    profile = profile,
                    purchases = purchases,
                    quests = quests,
                    questProgress = questProgress,
                    goals = goals,
                    onBuyItem = { viewModel.buyItem(it) },
                    onAnswerQuest = { qId, optId -> viewModel.answerQuest(qId, optId) },
                    onDepositGoal = { gId, amt -> viewModel.depositGoal(gId, amt) },
                    onWithdrawGoal = { gId, amt -> viewModel.withdrawGoal(gId, amt) }
                )
            }
            KeepAliveTab(visible = selectedTab == 1, seen = seenTabs.contains(1), key = 1, stateHolder = stateHolder) {
                CalendarScreen(
                    profile = profile,
                    currentPeriod = currentPeriod,
                    calendarNotes = calendarNotes,
                    recurringExpenses = recurringExpenses,
                    onConfirmBudget = { plan -> viewModel.confirmBudget(plan) },
                    onCompletePeriod = { viewModel.completePeriod() },
                    onAddNote = { day, title, cost, cat -> viewModel.addCalendarNote(day, title, cost, cat) },
                    onDeleteNote = { note -> viewModel.deleteCalendarNote(note) },
                    onToggleNote = { note -> viewModel.toggleCalendarNote(note) },
                    onAddRecurring = { title, cost, freq, icon -> viewModel.addRecurringExpense(title, cost, freq, icon) },
                    onDeleteRecurring = { exp -> viewModel.deleteRecurringExpense(exp) }
                )
            }
            KeepAliveTab(visible = selectedTab == 2, seen = seenTabs.contains(2), key = 2, stateHolder = stateHolder) {
                FinikScreen(
                    profile = profile,
                    // Анимации персонажа работают только на видимой вкладке —
                    // скрытый Финни не тратит CPU на бесконечные перерисовки
                    animationsEnabled = selectedTab == 2,
                    onPetTapped = { /* Можно добавить звук мурлыканья */ }
                )
            }
            KeepAliveTab(visible = selectedTab == 3, seen = seenTabs.contains(3), key = 3, stateHolder = stateHolder) {
                AdultScreen(
                    report = report,
                    adultSectionUseCase = viewModel.adultSectionUseCase,
                    onGrantBonus = { coins, reason -> viewModel.grantParentBonus(coins, reason) },
                    onNextDemoPeriod = { viewModel.completePeriod() },
                    onResetProfile = { viewModel.resetDemoProfile() }
                )
            }
            KeepAliveTab(visible = selectedTab == 4, seen = seenTabs.contains(4), key = 4, stateHolder = stateHolder) {
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
            BottomBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                selectedTab = selectedTab,
                onSelect = onSelectTab
            )
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

/**
 * Селектор вкладок в стиле Liquid Glass: парящая пилюля из полупрозрачного
 * стекла поверх контента. Слои: мягкая тень -> полупрозрачная основа ->
 * зеркальный блик сверху -> светящаяся кромка. Выбранная вкладка подсвечена
 * «жидкой» капсулой, всплывающей с пружинной анимацией.
 */
@Composable
private fun BottomBar(
    modifier: Modifier = Modifier,
    selectedTab: Int,
    onSelect: (Int) -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    10.dp,
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    ambientColor = Color(0x33000000),
                    spotColor = Color(0x22000000)
                )
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White.copy(alpha = 0.96f))
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color.White.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.55f)
                        )
                    ),
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
        ) {
            // Зеркальный блик: строго по фактическому размеру бара через matchParentSize()
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.White.copy(alpha = 0.4f),
                            0.45f to Color.Transparent
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NAV_ITEMS.forEachIndexed { index, item ->
                    DockItem(
                        item = item,
                        selected = selectedTab == index,
                        onClick = { onSelect(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.DockItem(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val pop by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dock_pop"
    )
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        // «Жидкая» капсула-индикатор выбранной вкладки
        Box(
            modifier = Modifier
                .height(46.dp)
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .graphicsLayer {
                    scaleX = 0.7f + 0.3f * pop
                    scaleY = 0.7f + 0.3f * pop
                    alpha = pop
                }
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFF3E0), Color(0xFFFFCC80))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Text(
                text = item.icon,
                fontSize = 21.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = 1f + 0.15f * pop
                    scaleY = 1f + 0.15f * pop
                }
            )
            Text(
                text = item.title,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) JoyOrange else TextSecondary
            )
        }
    }
}
