package ru.finpet.kids.core.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.domain.repository.FinPetRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class PurchaseItem(
    val id: String,
    val name: String,
    val category: String, // MANDATORY, OPTIONAL
    val price: Int,
    val satietyBonus: Int = 0,
    val moodBonus: Int = 0,
    val healthBonus: Int = 0,
    val icon: String = "ic_food",
    val description: String = ""
)

@Serializable
data class GoalItem(
    val id: String,
    val title: String,
    val targetCost: Int,
    val iconName: String = "ic_house",
    val description: String = ""
)

@Serializable
data class QuestOption(
    val id: String,
    val text: String,
    val isRecommended: Boolean,
    val feedback: String,
    val rewardCoins: Int
)

@Serializable
data class QuestItem(
    val id: String,
    val topic: String,
    val topicTitle: String,
    val title: String,
    val situation: String,
    val stage: String = "BABY", // BABY, TEEN, ADULT
    val type: String = "CHOICE", // CHOICE, TEXT_INPUT, MONEY_ALLOCATION, CALCULATION
    val options: List<QuestOption> = emptyList(),
    val expectedAnswers: List<String> = emptyList(),
    val hint: String = "",
    val inputPlaceholder: String = "",
    val feedbackSuccess: String = "",
    val feedbackFailure: String = "",
    val rewardCoins: Int = 10,
    val totalBudget: Int = 100,
    val sliderLabel: String = "",
    val allocationTarget: String = "",
    val recommendedMin: Int = 30,
    val recommendedMax: Int = 70,
    val allocationSuccessFeedback: String = "",
    val allocationWarningFeedback: String = "",
    val dynamicVariants: List<String> = emptyList(),
    val calculationBasePrice: Int = 30,
    val calculationDiscount: Int = 10,
    val calculationGivenMoney: Int = 50
)

@Singleton
class ContentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val finPetRepository: FinPetRepository
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // @Volatile: пишутся с Dispatchers.IO, читаются из разных корутин —
    // иначе нет happens-before и парсинг файла может выполниться дважды.
    @Volatile
    private var cachedPurchases: List<PurchaseItem>? = null
    @Volatile
    private var cachedGoals: List<GoalItem>? = null
    @Volatile
    private var cachedQuests: List<QuestItem>? = null
    private val initMutex = Mutex()

    suspend fun getPurchases(): List<PurchaseItem> = withContext(Dispatchers.IO) {
        cachedPurchases ?: loadFromAsset<List<PurchaseItem>>("content/purchases.json").also {
            cachedPurchases = it
        }
    }

    suspend fun getGoals(): List<GoalItem> = withContext(Dispatchers.IO) {
        cachedGoals ?: loadFromAsset<List<GoalItem>>("content/goals.json").also {
            cachedGoals = it
        }
    }

    suspend fun getQuests(): List<QuestItem> = withContext(Dispatchers.IO) {
        cachedQuests ?: loadFromAsset<List<QuestItem>>("content/quests.json").also {
            cachedQuests = it
        }
    }

    suspend fun ensureDataInitialized() {
        // JSON-парсинг — строго ДО транзакции: внутри транзакции нельзя
        // уходить на другой диспетчер, иначе разрывается привязка к её потоку.
        val defaultGoals = getGoals().map {
            GoalEntity(
                id = it.id,
                title = it.title,
                targetCost = it.targetCost,
                savedAmount = 0,
                isReached = false,
                iconName = it.iconName
            )
        }

        withContext(Dispatchers.IO) {
            // Мьютекс: FinPetApp.preload() и MainViewModel.init могут вызвать
            // конкурентно на холодом старте — второй вызов упрётся в лок и
            // станет дешёвым read-only проходом вместо повторной вставки.
            initMutex.withLock {
                finPetRepository.inTransaction {
                    val profile = finPetRepository.getProfileSync()
                    if (profile == null) {
                        val currentDay = LocalDate.now().dayOfMonth
                        // Инициализация стартового профиля на 50 крокетсов
                        val defaultProfile = ProfileEntity(
                            id = "default_player",
                            petName = "Финни",
                            petType = "CAT",
                            bodyColor = 0xFFFFA726.toInt(),
                            eyesType = 1,
                            accessoryId = "none",
                            balance = 200,
                            currentPeriodIndex = currentDay,
                            carePoints = 0,
                            growthStage = "BABY",
                            satiety = 100,
                            health = 100,
                            mood = 80,
                            activeGoalId = "goal_ball",
                            isDemoMode = false
                        )
                        finPetRepository.saveProfile(defaultProfile)

                        // Инициализация периода текущего дня
                        val firstPeriod = PeriodEntity(
                            periodIndex = currentDay,
                            plannedMandatory = 20,
                            plannedOptional = 15,
                            plannedSavings = 15,
                            isBudgetConfirmed = false
                        )
                        finPetRepository.savePeriod(firstPeriod)
                    } else {
                        var updated = profile
                        if (updated.petName.equals("Финик", ignoreCase = true)) {
                            updated = updated.copy(petName = "Финни")
                        }
                        if (updated.balance == 300) {
                            updated = updated.copy(balance = 50)
                        }
                        if (updated.isDemoMode) {
                            updated = updated.copy(isDemoMode = false)
                        }
                        if (updated != profile) {
                            finPetRepository.saveProfile(updated)
                        }
                    }

                    // Дефолтные цели: вставка с IGNORE + условное обновление
                    // метаданных. Накопленные savedAmount/isReached не обнуляются,
                    // а повторный проход не пишет в БД ничего.
                    finPetRepository.initDefaultGoals(defaultGoals)
                }
            }
        }
    }

    /**
     * Вызывается из FinPetApp в фоне до создания Activity: греет JSON-кэш
     * (парсинг идёт параллельно) и создаёт стартовые данные, чтобы
     * MainViewModel.init на первом кадре нашёл всё готовым.
     */
    suspend fun preload() = coroutineScope {
        val purchases = async(Dispatchers.IO) { getPurchases() }
        val quests = async(Dispatchers.IO) { getQuests() }
        val goals = async(Dispatchers.IO) { getGoals() }
        // БД-инициализация не зависит от парсинга — запускаем сразу
        val dbInit = async(Dispatchers.IO) { ensureDataInitialized() }
        purchases.await()
        quests.await()
        goals.await()
        dbInit.await()
    }

    /** Сброс in-memory кэша при нехватке памяти; данные перечитаются из assets. */
    fun trimMemoryCache() {
        cachedPurchases = null
        cachedGoals = null
        cachedQuests = null
    }

    private inline fun <reified T> loadFromAsset(path: String): T {
        val content = context.assets.open(path).bufferedReader().use { it.readText() }
        return json.decodeFromString(content)
    }
}
