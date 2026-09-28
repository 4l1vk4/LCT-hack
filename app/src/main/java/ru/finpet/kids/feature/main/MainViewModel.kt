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
import ru.finpet.kids.core.domain.model.BudgetPlan
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import coil.util.CoilUtils.result

sealed interface OnboardingState {
    data object Loading : OnboardingState
    data object NeedSkin : OnboardingState
    data object NeedName : OnboardingState
    data class Ready(val skinId: String, val petName: String) : OnboardingState
}

data class MoneyEvent(
    val amount: Int,
    val source: String,
    val icon: String = "🪙"
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

    // Eagerly: подписка на Room/DataStore стартует сразу при создании VM в фоне,
    // а не в момент первого открытия вкладки — переключение вкладок не ждёт диск.
    // Сами запросы Room выполняет на своём пуле, главный поток не блокируется.
    val profile: StateFlow<ProfileEntity?> = repository.getProfile()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _currentPeriod = MutableStateFlow<PeriodEntity?>(null)
    val currentPeriod: StateFlow<PeriodEntity?> = _currentPeriod.asStateFlow()

    private val _moneyEvent = MutableStateFlow<MoneyEvent?>(null)
    val moneyEvent: StateFlow<MoneyEvent?> = _moneyEvent.asStateFlow()

    private fun showMoneyEvent(amount: Int, source: String, icon: String = "🪙") {
        if (amount <= 0) return
        _moneyEvent.value = MoneyEvent(amount, source, icon)
    }

    fun dismissMoneyEvent() {
        _moneyEvent.value = null
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

    val demoMode: StateFlow<Boolean> = settingsRepository.isDemoMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val onboardingState: StateFlow<OnboardingState> = combine(
        settingsRepository.selectedSkin,
        settingsRepository.petName
    ) { skin, name ->
        val s = skin?.takeIf { it.isNotBlank() }
        val n = name?.takeIf { it.isNotBlank() }
        when {
            s == null -> OnboardingState.NeedSkin
            n == null -> OnboardingState.NeedName
            else      -> OnboardingState.Ready(s, n)
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

    init {
        // Вся инициализация — на IO-пуле и максимально параллельно:
        // парсинг JSON не ждёт БД, БД-верх не ждёт парсинг.
        // После FinPetApp.preload() кэши уже тёплые и это отрабатывает за миллисекунды.
        viewModelScope.launch(Dispatchers.IO) {
            coroutineScope {
                val dbInit = async { contentRepository.ensureDataInitialized() }
                // JSON-кэш греется параллельно с БД (независимые операции)
                val purchasesDeferred = async { contentRepository.getPurchases() }
                val questsDeferred = async { contentRepository.getQuests() }
                dbInit.await()
                // Календарь зависит только от готовой БД
                ensureCalendarDataInitialized()
                _purchases.value = purchasesDeferred.await()
                _quests.value = questsDeferred.await()
                loadCurrentPeriod()
            }
            refreshReportSync()
        }
    }

    private suspend fun ensureCalendarDataInitialized() {
        calendarDao.clearAllRecurringExpenses()
        val petName = repository.getProfileSync()?.petName ?: "Финни"
        if (calendarDao.getPetQuestionNotesCount() == 0) {
            calendarDao.clearAllNotes()
            val allNotes = mutableListOf<CalendarNoteEntity>()
            for (day in 1..28) {
                // 1. Ежедневный сытный корм для питомца
                allNotes.add(
                    CalendarNoteEntity(
                        dayIndex = day,
                        title = "Купить корм для питомца в Лавке",
                        cost = 30,
                        category = "PET_FOOD",
                        isCompleted = false
                    )
                )

                // 2. В будни — 2 урока в Школе; в выходные (сб-вс: 6, 7, 13, 14, 20, 21, 27, 28) — спросить вопросы у питомца
                val isWeekend = (day % 7) in listOf(6, 0)
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

                // 3. Продукты по поручению родителей (спавнится ровно 3 раза в неделю: дни 2, 4, 6 в каждой семидневке)
                if ((day % 7) in listOf(2, 4, 6)) {
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
    }

    private suspend fun loadCurrentPeriod() {
        val prof = repository.getProfileSync() ?: return
        val period = repository.getPeriodSync(prof.currentPeriodIndex)
        _currentPeriod.value = period
    }

    fun confirmBudget(plan: BudgetPlan) {
        viewModelScope.launch {
            val prof = repository.getProfileSync() ?: return@launch
            confirmBudgetUseCase(prof.currentPeriodIndex, plan)
            loadCurrentPeriod()
        }
    }

    fun buyItem(item: PurchaseItem) {
        viewModelScope.launch {
            val prof = repository.getProfileSync() ?: return@launch
            val currentDay = prof.currentPeriodIndex

            if (item.id == "groceries_parents") {
                // По поручению родителей: деньги дают родители (цена 0),
                // а сдача (5, 10 или 15 монет) с рандомным шансом остаётся ребёнку!
                val change = listOf(5, 10, 15).random()
                repository.saveProfile(prof.copy(balance = prof.balance + change))
                calendarDao.updateChecklistNoteByCategory(currentDay, "GROCERIES", isCompleted = true, cost = change)
                showMoneyEvent(
                    amount = change,
                    source = "Сдача от покупки продуктов",
                    icon = "🛒"
                )
                loadCurrentPeriod()
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

            // Если куплен корм для питомца — автоматически отмечаем сегодняшнее задание
            if (item.id == "food_basic" || (item.category == "MANDATORY" && item.id.contains("food"))) {
                calendarDao.completeChecklistTaskByCategory(currentDay, "PET_FOOD")
            }

            loadCurrentPeriod()
        }
    }

    fun answerQuest(questId: String, optionId: String) {
        viewModelScope.launch {
            questEngineUseCase.executeOption(questId, optionId)
            val prof = repository.getProfileSync() ?: return@launch
            val currentDay = prof.currentPeriodIndex
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
            val prof = repository.getProfileSync() ?: return@launch
            calendarDao.completeChecklistTaskByCategory(prof.currentPeriodIndex, "PET_QUESTION")
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
                    runawayDaysLeft = daysLeft
                )
            )
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
            val currentDay = prof.currentPeriodIndex
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
                icon = "🧹"
            )
            loadCurrentPeriod()
        }
    }

    fun completePeriod() {
        viewModelScope.launch {
            val result = completePeriodUseCase()
            if (result != null && result.periodIncome > 0) {
                showMoneyEvent(
                    amount = result.periodIncome,
                    source = "Карманные деньги на неделю (понедельник)"
                )
            }
            loadCurrentPeriod()
        }
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
            settingsRepository.setSelectedSkin("")
            settingsRepository.setPetName("")
            settingsRepository.setOnboardingCompleted(false)
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

    /**
     * Пересчёт отчёта для вкладки «Родителям».
     * Вызывается при открытии вкладки, а не после каждого действия:
     * раньше каждый тап (покупка/квест/взнос) платил 4 полных чтения таблиц.
     */
    fun refreshReport() {
        viewModelScope.launch(Dispatchers.IO) {
            refreshReportSync()
        }
    }

    // generateReport — 5 параллельных SQL-агрегатов, только вне главного потока
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
        viewModelScope.launch { settingsRepository.setDemoMode(enabled) }
    }

    private fun choreTitle(choreId: String): String = when (choreId) {
        "chore_floor" -> "Мытьё полов"
        "chore_trash" -> "Вынос мусора"
        "chore_dishes" -> "Мытьё посуды"
        else -> "Помощь по дому"
    }

    fun setAccessory(accessoryId: String) {
        viewModelScope.launch {
            val prof = repository.getProfileSync() ?: return@launch
            repository.saveProfile(prof.copy(accessoryId = accessoryId))
        }
    }
}

