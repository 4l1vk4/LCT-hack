package ru.finpet.kids.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.finpet.kids.core.data.local.dao.CalendarDao
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity
import ru.finpet.kids.core.data.local.entity.RecurringExpenseEntity
import ru.finpet.kids.core.data.repository.ContentRepository
import ru.finpet.kids.core.data.repository.PurchaseItem
import ru.finpet.kids.core.data.repository.QuestItem
import ru.finpet.kids.core.data.repository.SettingsRepository
import ru.finpet.kids.core.domain.calculator.PetEconomyCalculator
import ru.finpet.kids.core.domain.model.BudgetPlan
import ru.finpet.kids.core.domain.model.GrowthStage
import ru.finpet.kids.core.domain.model.PeriodResolution
import ru.finpet.kids.core.domain.repository.FinPetRepository
import ru.finpet.kids.core.domain.usecase.AdultSectionUseCase
import ru.finpet.kids.core.domain.usecase.CompetencyReport
import ru.finpet.kids.core.domain.usecase.CompetencyTracker
import ru.finpet.kids.core.domain.usecase.CompletePeriodUseCase
import ru.finpet.kids.core.domain.usecase.ConfirmBudgetUseCase
import ru.finpet.kids.core.domain.usecase.DepositToGoalUseCase
import ru.finpet.kids.core.domain.usecase.MakePurchaseUseCase
import ru.finpet.kids.core.domain.usecase.QuestEngineUseCase
import ru.finpet.kids.core.domain.usecase.ResetDemoProfileUseCase
import ru.finpet.kids.core.domain.usecase.WithdrawFromGoalUseCase
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import ru.finpet.kids.feature.calendar.CalendarMonthInfo
import ru.finpet.kids.feature.finik.parseAccessories

sealed interface OnboardingState {
    data object Loading : OnboardingState
    data object NeedSkin : OnboardingState
    data object NeedName : OnboardingState
    data class NeedTutorial(val skinId: String, val petName: String) : OnboardingState
    data class Ready(val skinId: String, val petName: String) : OnboardingState
}

data class MoneyEvent(
    val amount: Int,
    val source: String,
    val icon: String = "🪙"
)

data class PetGrowthInfo(
    val petName: String,
    val stage: GrowthStage
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FinPetRepository,
    private val contentRepository: ContentRepository,
    private val settingsRepository: SettingsRepository,
    private val calendarDao: CalendarDao,
    private val confirmBudgetUseCase: ConfirmBudgetUseCase,
    private val makePurchaseUseCase: MakePurchaseUseCase,
    private val depositToGoalUseCase: DepositToGoalUseCase,
    private val completePeriodUseCase: CompletePeriodUseCase,
    private val withdrawFromGoalUseCase: WithdrawFromGoalUseCase,
    private val questEngineUseCase: QuestEngineUseCase,
    val adultSectionUseCase: AdultSectionUseCase,
    private val competencyTracker: CompetencyTracker,
    private val resetDemoProfileUseCase: ResetDemoProfileUseCase
) : ViewModel() {

    private var isProcessingGroceries = false

    // подписка на данные профиля
    val profile: StateFlow<ProfileEntity?> = repository.getProfile()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentPeriod: StateFlow<PeriodEntity?> = repository.getProfile()
        .flatMapLatest { prof ->
            if (prof == null) flowOf(null)
            else repository.getPeriod(prof.currentPeriodIndex)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _virtualDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val virtualDate: StateFlow<LocalDate> = _virtualDate.asStateFlow()

    // ночной режим: с 22:00 до 06:00 по времени устройства + возможность ручного переключения в демо
    private val _isNightOverride = MutableStateFlow<Boolean?>(null)
    val isNightOverride: StateFlow<Boolean?> = _isNightOverride.asStateFlow()

    private fun isCurrentTimeNight(time: LocalTime = LocalTime.now()): Boolean {
        val hour = time.hour
        return hour >= 22 || hour < 6
    }

    val demoMode: StateFlow<Boolean> = settingsRepository.isDemoMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isNightMode: StateFlow<Boolean> = combine(
        flow {
            while (true) {
                emit(LocalTime.now())
                delay(30_000L)
            }
        },
        _isNightOverride,
        demoMode
    ) { time, override, isDemo ->
        if (isDemo && override != null) {
            override
        } else if (isDemo && override == null) {
            false // при включении демо-режима по умолчанию даем день для тестирования
        } else {
            override ?: isCurrentTimeNight(time)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        isCurrentTimeNight()
    )

    fun toggleNightMode(enabled: Boolean? = null) {
        val current = _isNightOverride.value ?: isCurrentTimeNight()
        _isNightOverride.value = enabled ?: !current
    }

    private val _moneyEvent = MutableStateFlow<MoneyEvent?>(null)
    val moneyEvent: StateFlow<MoneyEvent?> = _moneyEvent.asStateFlow()

    private fun showMoneyEvent(amount: Int, source: String, icon: String = "🪙") {
        if (amount <= 0) return
        _moneyEvent.value = MoneyEvent(amount, source, icon)
    }

    fun dismissMoneyEvent() {
        _moneyEvent.value = null
    }

    private val _periodAnalysisEvent = MutableStateFlow<PeriodResolution?>(null)
    val periodAnalysisEvent: StateFlow<PeriodResolution?> = _periodAnalysisEvent.asStateFlow()

    fun dismissPeriodAnalysis() {
        _periodAnalysisEvent.value = null
    }

    private val _petGrowthEvent = MutableStateFlow<PetGrowthInfo?>(null)
    val petGrowthEvent: StateFlow<PetGrowthInfo?> = _petGrowthEvent.asStateFlow()

    fun dismissPetGrowthEvent() {
        _petGrowthEvent.value = null
    }

    fun triggerPetGrowthEventForTest(petName: String, stage: GrowthStage = GrowthStage.ADULT) {
        _petGrowthEvent.value = PetGrowthInfo(petName, stage)
    }

    val preorderItemIds: StateFlow<Set<String>> = settingsRepository.preorderItemIds
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val adultStageStartDay: StateFlow<Int> = settingsRepository.adultStageStartDay
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun togglePreorderItem(itemId: String) {
        viewModelScope.launch {
            settingsRepository.togglePreorderItem(itemId)
        }
    }

    fun quickBuyItems(items: List<PurchaseItem>) {
        viewModelScope.launch {
            val prof = repository.getProfileSync() ?: return@launch
            if (prof.isPetRunaway) return@launch
            val totalCost = items.sumOf { it.price }
            if (prof.balance < totalCost) return@launch

            for (item in items) {
                makePurchaseUseCase(
                    itemId = item.id,
                    itemName = item.name,
                    category = item.category,
                    price = item.price,
                    satietyBonus = item.satietyBonus,
                    moodBonus = item.moodBonus,
                    healthBonus = item.healthBonus
                )
                val currentDay = _virtualDate.value.dayOfMonth
                if (item.id == "food_basic" || (item.category == "MANDATORY" && item.id.contains("food"))) {
                    calendarDao.completeChecklistTaskByCategory(currentDay, "PET_FOOD")
                }
            }
            loadCurrentPeriod()
        }
    }

    val goals: StateFlow<List<GoalEntity>> = repository.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val questProgress: StateFlow<List<QuestProgressEntity>> = repository.getQuestProgress()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val calendarNotes: StateFlow<List<CalendarNoteEntity>> = calendarDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val recurringExpenses: StateFlow<List<RecurringExpenseEntity>> = calendarDao.getAllRecurringExpenses()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _purchases = MutableStateFlow<List<PurchaseItem>>(emptyList())
    val purchases: StateFlow<List<PurchaseItem>> = _purchases.asStateFlow()

    private val _quests = MutableStateFlow<List<QuestItem>>(emptyList())
    val quests: StateFlow<List<QuestItem>> = _quests.asStateFlow()

    private val _competencyReport = MutableStateFlow<CompetencyReport?>(null)
    val competencyReport: StateFlow<CompetencyReport?> = _competencyReport.asStateFlow()

    val soundEnabled: StateFlow<Boolean> = settingsRepository.isSoundEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val musicEnabled: StateFlow<Boolean> = settingsRepository.isMusicEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val musicVolume: StateFlow<Float> = settingsRepository.musicVolume
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.7f)

    val onboardingState: StateFlow<OnboardingState> = combine(
        settingsRepository.selectedSkin,
        settingsRepository.petName,
        settingsRepository.isTutorialCompleted
    ) { skin, name, tutorialDone ->
        val s = skin?.takeIf { it.isNotBlank() }
        val n = name?.takeIf { it.isNotBlank() }
        when {
            s == null          -> OnboardingState.NeedSkin
            n == null          -> OnboardingState.NeedName
            !tutorialDone      -> OnboardingState.NeedTutorial(s, n)
            else               -> OnboardingState.Ready(s, n)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, OnboardingState.Loading)

    fun setSkin(skinId: String) {
        viewModelScope.launch { settingsRepository.setSelectedSkin(skinId) }
    }

    fun setPetName(name: String) {
        viewModelScope.launch {
            settingsRepository.setPetName(name)
            // Также обновляем Room, чтобы FinikScreen показывал актуальное имя
            val profile = repository.getProfileSync()
            if (profile != null) {
                repository.saveProfile(profile.copy(petName = name))
            }
        }
    }

    fun completeTutorial() {
        viewModelScope.launch { settingsRepository.setTutorialCompleted(true) }
    }


    init {
        // фоновая инициализация кэша и базы данных
        viewModelScope.launch(Dispatchers.IO) {
            coroutineScope {
                val dbInit = async { contentRepository.ensureDataInitialized() }
                val purchasesDeferred = async { contentRepository.getPurchases() }
                val questsDeferred = async { contentRepository.getQuests() }
                dbInit.await()
                // синхронизация времени и состояния режима
                initVirtualTime()
                settingsRepository.setDemoMode(false)
                ensureCalendarDataInitialized()
                _purchases.value = purchasesDeferred.await()
                _quests.value = questsDeferred.await()
                loadCurrentPeriod()
            }
            refreshReportSync()
        }
    }

    private suspend fun initVirtualTime() {
        val todayReal = LocalDate.now()
        val lastReal = settingsRepository.getLastRealLocalDateSync()
        val savedVirtual = settingsRepository.getVirtualLocalDateSync()

        val resolvedVirtual = if (lastReal == null) {
            val vDate = savedVirtual ?: todayReal
            settingsRepository.setLastRealDate(todayReal)
            settingsRepository.setVirtualDate(vDate)
            vDate
        } else {
            val daysPassed = ChronoUnit.DAYS.between(lastReal, todayReal)
            if (daysPassed > 0) {
                val baseDate = savedVirtual ?: lastReal
                val advanced = baseDate.plusDays(daysPassed)
                settingsRepository.setLastRealDate(todayReal)
                settingsRepository.setVirtualDate(advanced)
                advanced
            } else {
                savedVirtual ?: todayReal
            }
        }
        _virtualDate.value = resolvedVirtual

        // Если при наступлении нового дня сменился месяц — обновляем задачи на новый месяц
        val oldMonth = savedVirtual?.month
        if (oldMonth != null && oldMonth != resolvedVirtual.month) {
            seedCalendarNotesForMonth(YearMonth.from(resolvedVirtual))
        }
    }

    private suspend fun ensureCalendarDataInitialized() {
        if (calendarDao.getRecurringExpensesSync().isEmpty()) {
            calendarDao.insertRecurringExpenses(
                listOf(
                    RecurringExpenseEntity(
                        title = "Школьный обед",
                        cost = 20,
                        frequencyDays = 1,
                        icon = "🍲",
                        category = "MANDATORY"
                    ),
                    RecurringExpenseEntity(
                        title = "Витаминки для питомца",
                        cost = 15,
                        frequencyDays = 3,
                        icon = "💊",
                        category = "MANDATORY"
                    ),
                    RecurringExpenseEntity(
                        title = "Уход и шампунь",
                        cost = 25,
                        frequencyDays = 7,
                        icon = "🧼",
                        category = "OPTIONAL"
                    )
                )
            )
        }
        val currentYM = YearMonth.from(_virtualDate.value)
        val notesCount = calendarDao.getNotesCount()
        val petQuestionsCount = calendarDao.getPetQuestionNotesCount()
        val hasBudgetTasks = calendarDao.getNoteByDayAndCategory(1, "BUDGET_PLAN") != null
        if (notesCount == 0 || petQuestionsCount == 0 || !hasBudgetTasks) {
            seedCalendarNotesForMonth(currentYM)
        }
        val prof = repository.getProfileSync()
        if (prof != null) {
            val period = repository.getPeriodSync(prof.currentPeriodIndex)
            if (period?.isBudgetConfirmed == true) {
                val currentDay = _virtualDate.value.dayOfMonth
                calendarDao.completeBudgetPlanTasksUpToDay(currentDay)
            }
        }
    }

    private suspend fun seedCalendarNotesForMonth(yearMonth: YearMonth) {
        val daysInMonth = yearMonth.lengthOfMonth()
        val petName = repository.getProfileSync()?.petName ?: "Финни"
        calendarDao.clearAllNotes()
        val allNotes = mutableListOf<CalendarNoteEntity>()
        for (day in 1..daysInMonth) {
            val date = yearMonth.atDay(day)
            val isWeekend = date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

            val isPeriodStart = (day == 1 || date.dayOfWeek == DayOfWeek.MONDAY)

            // 0. распределение бюджета на период (только в начале периода)
            if (isPeriodStart) {
                allNotes.add(
                    CalendarNoteEntity(
                        dayIndex = day,
                        title = "Распределить бюджет на период",
                        cost = 0,
                        category = "BUDGET_PLAN",
                        isCompleted = false
                    )
                )
            }

            // 1. ежедневный сытный корм для питомца
            allNotes.add(
                CalendarNoteEntity(
                    dayIndex = day,
                    title = "Купить корм для питомца в Лавке",
                    cost = 30,
                    category = "PET_FOOD",
                    isCompleted = false
                )
            )

            // 2. в будни уроки в школе, в выходные вопросы питомцу
            if (isWeekend) {
                allNotes.add(
                    CalendarNoteEntity(
                        dayIndex = day,
                        title = "Спросить вопросы у $petName",
                        cost = 0,
                        category = "PET_QUESTION",
                        isCompleted = false
                    )
                )
            } else {
                allNotes.add(
                    CalendarNoteEntity(
                        dayIndex = day,
                        title = "Сделать 2 урока в Школе",
                        cost = 0,
                        category = "SCHOOL",
                        isCompleted = false
                    )
                )
            }

            // 3. покупка продуктов по поручению родителей
            if (date.dayOfWeek in listOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY)) {
                allNotes.add(
                    CalendarNoteEntity(
                        dayIndex = day,
                        title = "Купить продукты по поручению родителей",
                        cost = 0,
                        category = "GROCERIES",
                        isCompleted = false
                    )
                )
            }
        }
        calendarDao.insertNotes(allNotes)
    }

    private suspend fun loadCurrentPeriod() {
        // автообновление через flow
    }

    fun confirmBudget(plan: BudgetPlan) {
        viewModelScope.launch {
            val prof = repository.getProfileSync() ?: return@launch
            confirmBudgetUseCase(prof.currentPeriodIndex, plan)
            val currentDay = _virtualDate.value.dayOfMonth
            calendarDao.completeBudgetPlanTasksUpToDay(currentDay)
            loadCurrentPeriod()
        }
    }

    fun buyItem(item: PurchaseItem) {
        viewModelScope.launch {
            val prof = repository.getProfileSync() ?: return@launch
            if (prof.isPetRunaway) return@launch
            val currentDay = _virtualDate.value.dayOfMonth

            if (item.id == "groceries_parents") {
                // проверка повторного выполнения задания родителей
                if (isProcessingGroceries) return@launch
                isProcessingGroceries = true
                try {
                    val groceryTask = calendarDao.getNoteByDayAndCategory(currentDay, "GROCERIES")
                    if (groceryTask == null || groceryTask.isCompleted) {
                        return@launch
                    }

                    // начисление сдачи ребенку
                    val change = listOf(5, 10, 15).random()
                    repository.saveProfile(prof.copy(balance = prof.balance + change))
                    calendarDao.updateChecklistNoteByCategory(currentDay, "GROCERIES", isCompleted = true, cost = change)
                    showMoneyEvent(
                        amount = change,
                        source = "Сдача от покупки продуктов",
                        icon = "🛒"
                    )
                    loadCurrentPeriod()
                } finally {
                    isProcessingGroceries = false
                }
                return@launch
            }

            makePurchaseUseCase(
                itemId = item.id,
                itemName = item.name,
                category = item.category,
                price = item.price,
                satietyBonus = item.satietyBonus,
                moodBonus = item.moodBonus,
                healthBonus = item.healthBonus
            )

            // автоотметка задания при покупке корма
            if (item.id == "food_basic" || (item.category == "MANDATORY" && item.id.contains("food"))) {
                calendarDao.completeChecklistTaskByCategory(currentDay, "PET_FOOD")
            }

            loadCurrentPeriod()
        }
    }

    fun answerQuest(questId: String, optionId: String) {
        viewModelScope.launch {
            questEngineUseCase.executeOption(questId, optionId)
            val currentDay = _virtualDate.value.dayOfMonth
            val completedToday = repository.getQuestProgress().first().count {
                it.completedInPeriod == currentDay && it.isCompleted
            }
            if (completedToday >= 2) {
                calendarDao.completeChecklistTaskByCategory(currentDay, "SCHOOL")
            }
        }
    }

    fun onPetAskedQuestion() {
        viewModelScope.launch {
            val currentDay = _virtualDate.value.dayOfMonth
            calendarDao.completeChecklistTaskByCategory(currentDay, "PET_QUESTION")
        }
    }

    fun placeFoodBowl() {
        viewModelScope.launch {
            val prof = repository.getProfileSync() ?: return@launch
            if (prof.bowlPlacedToday) return@launch
            val bowlPrice = 60 // x2 от стоимости обычного корма (30 * 2)
            if (prof.balance < bowlPrice) return@launch

            val daysLeft = if (prof.runawayDaysLeft <= 0) (1..3).random() else prof.runawayDaysLeft
            repository.saveProfile(
                prof.copy(
                    balance = prof.balance - bowlPrice,
                    isPetRunaway = true,
                    bowlPlacedToday = true,
                    runawayDaysLeft = daysLeft,
                    mood = 0
                )
            )

            // учет миски в обязательных расходах периода
            val curPeriod = repository.getPeriodSync(prof.currentPeriodIndex) ?: PeriodEntity(prof.currentPeriodIndex)
            repository.savePeriod(
                curPeriod.copy(
                    actualMandatory = curPeriod.actualMandatory + bowlPrice
                )
            )

            // отметка задачи дня о корме как выполненной с обновлением цены (60) и названия
            val currentDay = _virtualDate.value.dayOfMonth
            val existingPetFoodNote = calendarDao.getNoteByDayAndCategory(currentDay, "PET_FOOD")
            if (existingPetFoodNote != null) {
                calendarDao.completeChecklistTaskWithDetails(
                    dayIndex = currentDay,
                    category = "PET_FOOD",
                    cost = bowlPrice,
                    title = "Купить корм для возвращения"
                )
            } else {
                calendarDao.insertNote(
                    CalendarNoteEntity(
                        dayIndex = currentDay,
                        title = "Купить корм для возвращения",
                        cost = bowlPrice,
                        category = "PET_FOOD",
                        isCompleted = true
                    )
                )
            }

            loadCurrentPeriod()
        }
    }

    fun depositGoal(goalId: String, amount: Int) {
        viewModelScope.launch {
            depositToGoalUseCase(goalId, amount)
            loadCurrentPeriod()
        }
    }

    fun withdrawGoal(goalId: String, amount: Int) {
        viewModelScope.launch {
            withdrawFromGoalUseCase(goalId, amount)
            loadCurrentPeriod()
        }
    }

    fun selectActiveGoal(goalId: String) {
        viewModelScope.launch {
            val prof = repository.getProfileSync() ?: return@launch
            repository.saveProfile(prof.copy(activeGoalId = goalId))
        }
    }

    fun completeHomeChore(choreId: String, rewardCoins: Int) {
        viewModelScope.launch {
            val prof = repository.getProfileSync() ?: return@launch
            val currentDay = _virtualDate.value.dayOfMonth
            val existing = questProgress.value.find { it.questId == choreId }
            val isAlreadyDone = if (existing != null && existing.isCompleted) {
                when (choreId) {
                    "chore_dishes" -> existing.completedInPeriod >= currentDay
                    "chore_trash" -> (currentDay - existing.completedInPeriod) < 2
                    "chore_floor" -> (currentDay - existing.completedInPeriod) < 3
                    else -> existing.completedInPeriod >= currentDay
                }
            } else false
            if (isAlreadyDone) return@launch

            repository.inTransaction {
                repository.saveProfile(prof.copy(balance = prof.balance + rewardCoins))
                repository.saveQuestProgress(
                    QuestProgressEntity(
                        questId = choreId,
                        isCompleted = true,
                        rewardClaimed = true,
                        completedInPeriod = currentDay
                    )
                )
            }
            showMoneyEvent(
                amount = rewardCoins,
                source = "Домашнее дело: ${choreTitle(choreId)}",
                icon = ""
            )
            loadCurrentPeriod()
        }
    }

    fun advanceOneDay() {
        viewModelScope.launch {
            val curDate = _virtualDate.value
            val nextDate = curDate.plusDays(1)
            _virtualDate.value = nextDate
            settingsRepository.setVirtualDate(nextDate)

            val monthChanged = nextDate.month != curDate.month
            val nextDayIndex = nextDate.dayOfMonth
            val nextYM = YearMonth.from(nextDate)
            val curYM = YearMonth.from(curDate)

            if (monthChanged) {
                seedCalendarNotesForMonth(nextYM)
            }

            val isPeriodTransition = nextDate.dayOfWeek == DayOfWeek.MONDAY

            val profBefore = repository.getProfileSync()
            val stageBefore = profBefore?.growthStage ?: "BABY"
            val petName = profBefore?.petName ?: "Котик"

            // 1. Применяем результаты прошедшего дня (curDate) к питомцу (включая переход в воскресенье)
            if (profBefore != null) {
                val fedYesterday = calendarDao.getNoteByDayAndCategory(curDate.dayOfMonth, "PET_FOOD")?.isCompleted == true
                var newSatiety = profBefore.satiety
                var newHealth = profBefore.health
                var newMood = profBefore.mood
                var newRunaway = profBefore.isPetRunaway
                var newRunawayDays = profBefore.runawayDaysLeft

                if (profBefore.isPetRunaway) {
                    if (profBefore.bowlPlacedToday) {
                        val daysRemaining = profBefore.runawayDaysLeft - 1
                        if (daysRemaining <= 0) {
                            newRunaway = false
                            newRunawayDays = 0
                            newMood = 60
                            newSatiety = 80
                            newHealth = maxOf(profBefore.health, 70)
                        } else {
                            newRunawayDays = daysRemaining
                            newMood = 0
                        }
                    } else {
                        newMood = 0
                    }
                } else {
                    if (!fedYesterday) {
                        newSatiety = maxOf(0, profBefore.satiety - 25)
                        if (newSatiety < 20) {
                            newHealth = maxOf(0, profBefore.health - 10)
                        }
                        newMood = maxOf(0, profBefore.mood - 15)
                    }
                    val effectiveMood = PetEconomyCalculator.calculateEffectiveMood(newMood, newSatiety, newHealth)
                    if (newMood <= 0 || effectiveMood <= 0) {
                        newRunaway = true
                        newRunawayDays = if (newRunawayDays <= 0) (1..3).random() else newRunawayDays
                        newMood = 0
                    }
                }

                repository.saveProfile(
                    profBefore.copy(
                        satiety = newSatiety,
                        health = newHealth,
                        mood = if (newRunaway) 0 else newMood,
                        isPetRunaway = newRunaway,
                        runawayDaysLeft = newRunawayDays,
                        bowlPlacedToday = false
                    )
                )
            }

            // 2. Если наступил понедельник — закрываем завершившийся период (неделю)
            if (isPeriodTransition) {
                val result = completePeriodUseCase(nextDayIndex = nextDayIndex, yearMonth = nextYM, isPeriodSkip = false)
                if (result != null) {
                    val stageAfter = result.newStats.stage.name
                    if (stageAfter != stageBefore) {
                        _petGrowthEvent.value = PetGrowthInfo(petName, result.newStats.stage)
                    }
                    if (result.newStats.stage == GrowthStage.ADULT) {
                        val currentStart = settingsRepository.adultStageStartDay.first()
                        if (currentStart == 0) {
                            settingsRepository.setAdultStageStartDay(nextDayIndex)
                        }
                    }
                    val isIncome = result.periodIncome > 0
                    val enrichedResult = result.copy(
                        isMonthTransition = monthChanged,
                        closedMonthName = CalendarMonthInfo.of(curYM).monthName,
                        newMonthName = CalendarMonthInfo.of(nextYM).monthName,
                        isIncomeDay = isIncome
                    )
                    // диалог анализа периода
                    _periodAnalysisEvent.value = enrichedResult
                    if (isIncome) {
                        showMoneyEvent(
                            amount = result.periodIncome,
                            source = "Карманные деньги на неделю (понедельник)"
                        )
                    }
                }
            }
            loadCurrentPeriod()
        }
    }

    fun advanceOnePeriod() {
        viewModelScope.launch {
            val curDate = _virtualDate.value
            val nextDate = curDate.plusDays(7)
            _virtualDate.value = nextDate
            settingsRepository.setVirtualDate(nextDate)

            val monthChanged = nextDate.month != curDate.month
            val nextDayIndex = nextDate.dayOfMonth
            val nextYM = YearMonth.from(nextDate)
            val curYM = YearMonth.from(curDate)

            if (monthChanged) {
                seedCalendarNotesForMonth(nextYM)
            }

            val profBefore = repository.getProfileSync()
            val stageBefore = profBefore?.growthStage ?: "BABY"
            val petName = profBefore?.petName ?: "Котик"

            val result = completePeriodUseCase(
                nextDayIndex = nextDayIndex,
                yearMonth = nextYM,
                isPeriodSkip = true
            )
            if (result != null) {
                val stageAfter = result.newStats.stage.name
                if (stageAfter != stageBefore) {
                    _petGrowthEvent.value = PetGrowthInfo(petName, result.newStats.stage)
                }
                if (result.newStats.stage == GrowthStage.ADULT) {
                    val currentStart = settingsRepository.adultStageStartDay.first()
                    if (currentStart == 0) {
                        settingsRepository.setAdultStageStartDay(nextDayIndex)
                    }
                }
                val isIncome = result.periodIncome > 0
                val enrichedResult = result.copy(
                    isMonthTransition = monthChanged,
                    closedMonthName = CalendarMonthInfo.of(curYM).monthName,
                    newMonthName = CalendarMonthInfo.of(nextYM).monthName,
                    isIncomeDay = isIncome
                )
                // диалог анализа периода
                _periodAnalysisEvent.value = enrichedResult
                if (isIncome) {
                    showMoneyEvent(
                        amount = result.periodIncome,
                        source = "Карманные деньги на неделю (+200)"
                    )
                }
            }
            loadCurrentPeriod()
        }
    }

    fun advanceToNextGrowthStage() {
        viewModelScope.launch {
            val profBefore = repository.getProfileSync() ?: return@launch
            val stageBefore = profBefore.growthStage
            val petName = profBefore.petName

            val periodsToSkip = when (stageBefore) {
                "BABY" -> 7
                "TEEN" -> 11
                else -> return@launch
            }

            var curDate = _virtualDate.value
            var lastResult: PeriodResolution? = null
            var totalIncome = 0

            for (i in 1..periodsToSkip) {
                val nextDate = curDate.plusDays(7)
                curDate = nextDate

                val monthChanged = nextDate.month != curDate.minusDays(7).month
                val nextDayIndex = nextDate.dayOfMonth
                val nextYM = YearMonth.from(nextDate)

                if (monthChanged) {
                    seedCalendarNotesForMonth(nextYM)
                }

                val result = completePeriodUseCase(
                    nextDayIndex = nextDayIndex,
                    yearMonth = nextYM,
                    isPeriodSkip = true,
                    isDemoPeriodSkip = true
                )
                if (result != null) {
                    lastResult = result
                    totalIncome += result.periodIncome
                    if (result.newStats.stage == GrowthStage.ADULT) {
                        val currentStart = settingsRepository.adultStageStartDay.first()
                        if (currentStart == 0) {
                            settingsRepository.setAdultStageStartDay(nextDayIndex)
                        }
                    }
                }
            }

            _virtualDate.value = curDate
            settingsRepository.setVirtualDate(curDate)

            if (lastResult != null) {
                val stageAfter = lastResult.newStats.stage.name
                if (stageAfter != stageBefore) {
                    _petGrowthEvent.value = PetGrowthInfo(petName, lastResult.newStats.stage)
                }

                val curYM = YearMonth.from(curDate)
                val enrichedResult = lastResult.copy(
                    isMonthTransition = false,
                    closedMonthName = CalendarMonthInfo.of(curYM).monthName,
                    newMonthName = CalendarMonthInfo.of(curYM).monthName,
                    isIncomeDay = totalIncome > 0,
                    periodIncome = totalIncome
                )
                _periodAnalysisEvent.value = enrichedResult

                if (totalIncome > 0) {
                    showMoneyEvent(
                        amount = totalIncome,
                        source = "Карманные деньги за $periodsToSkip нед. (+$totalIncome)"
                    )
                }
            }

            loadCurrentPeriod()
        }
    }

    fun completePeriod() {
        advanceOneDay()
    }

    fun grantParentBonus(coins: Int, reason: String) {
        viewModelScope.launch {
            adultSectionUseCase.grantParentBonus(coins, reason)
            showMoneyEvent(
                amount = coins,
                source = "Награда от родителей: $reason",
            )
        }
    }

    fun resetDemoProfile() {
        viewModelScope.launch {
            resetDemoProfileUseCase()
            val initialDate = LocalDate.now()
            _virtualDate.value = initialDate
            settingsRepository.setVirtualDate(initialDate)
            settingsRepository.setLastRealDate(LocalDate.now())
            settingsRepository.setSelectedSkin("")
            settingsRepository.setPetName("")
            settingsRepository.setTutorialCompleted(false)
            settingsRepository.setOnboardingCompleted(false)
            settingsRepository.setAdultStageStartDay(0)
            settingsRepository.setPreorderItemIds(emptySet())
            calendarDao.clearAllNotes()
            calendarDao.clearAllRecurringExpenses()
            ensureCalendarDataInitialized()
            loadCurrentPeriod()
            refreshReport()
        }
    }

    fun addCalendarNote(dayIndex: Int, title: String, cost: Int, category: String) {
        viewModelScope.launch {
            calendarDao.insertNote(
                CalendarNoteEntity(
                    dayIndex = dayIndex,
                    title = title,
                    cost = cost,
                    category = category
                )
            )
        }
    }

    fun deleteCalendarNote(note: CalendarNoteEntity) {
        viewModelScope.launch {
            calendarDao.deleteNote(note)
        }
    }

    fun toggleCalendarNote(note: CalendarNoteEntity) {
        viewModelScope.launch {
            calendarDao.updateNoteCompleted(note.id, !note.isCompleted)
        }
    }

    fun completeChecklistTask(note: CalendarNoteEntity, rewardCoins: Int) {
        viewModelScope.launch {
            if (note.isCompleted) return@launch
            calendarDao.updateNoteStatus(note.id, true, if (rewardCoins > 0) rewardCoins else note.cost)
            if (rewardCoins > 0) {
                val prof = repository.getProfileSync() ?: return@launch
                repository.saveProfile(prof.copy(balance = prof.balance + rewardCoins))
                showMoneyEvent(
                    amount = rewardCoins,
                    source = "Задание: ${note.title}"
                )
            }
        }
    }

    fun addRecurringExpense(title: String, cost: Int, frequencyDays: Int, icon: String) {
        viewModelScope.launch {
            calendarDao.insertRecurringExpense(
                RecurringExpenseEntity(
                    title = title,
                    cost = cost,
                    frequencyDays = frequencyDays,
                    icon = icon,
                    category = "MANDATORY"
                )
            )
        }
    }

    fun deleteRecurringExpense(expense: RecurringExpenseEntity) {
        viewModelScope.launch {
            calendarDao.deleteRecurringExpense(expense)
        }
    }

    // пересчет отчета компетенций
    fun refreshReport() {
        viewModelScope.launch(Dispatchers.IO) {
            refreshReportSync()
        }
    }

    // расчет отчета вне главного потока
    private suspend fun refreshReportSync() {
        _competencyReport.value = competencyTracker.generateReport()
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSoundEnabled(enabled) }
    }

    fun toggleMusic(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setMusicEnabled(enabled) }
    }

    fun setMusicVolume(volume: Float) {
        viewModelScope.launch { settingsRepository.setMusicVolume(volume) }
    }

    fun toggleDemo(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDemoMode(enabled)
            if (!enabled) {
                _isNightOverride.value = null
            }
        }
    }

    private fun choreTitle(choreId: String): String = when (choreId) {
        "chore_floor" -> "Уборка листьев"
        "chore_trash" -> "Вынос мусора"
        "chore_dishes" -> "Мытьё посуды"
        else -> "Помощь по дому"
    }

    fun toggleAccessory(accessoryId: String) {
        viewModelScope.launch {
            val prof = repository.getProfileSync() ?: return@launch

            // снятие всех аксессуаров
            val newValue = if (accessoryId == "none") {
                "none"
            } else {
                val current = parseAccessories(prof.accessoryId).map { it.id }.toMutableSet()
                if (accessoryId in current) {
                    current.remove(accessoryId)
                } else {
                    current.add(accessoryId)
                }
                if (current.isEmpty()) "none"
                else current.joinToString(",")
            }

            repository.saveProfile(prof.copy(accessoryId = newValue))
        }
    }
}

