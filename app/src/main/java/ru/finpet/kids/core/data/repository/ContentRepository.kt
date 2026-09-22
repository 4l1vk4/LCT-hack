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
    val options: List<QuestOption>
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

    private var cachedPurchases: List<PurchaseItem>? = null
    private var cachedGoals: List<GoalItem>? = null
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

    suspend fun ensureDataInitialized() = withContext(Dispatchers.IO) {
        // Мьютекс: FinPetApp.preload() и MainViewModel.init могут вызвать
        // конкурентно на холодном старте — повторная вставка идемпотентна (REPLACE),
        // но лишний проход по БД нам не нужен.
        initMutex.withLock {
            val profile = finPetRepository.getProfileSync()
            if (profile == null) {
                // Инициализация стартового профиля на 50 крокетсов
                val defaultProfile = ProfileEntity(
                    id = "default_player",
                    petName = "Финни",
                    petType = "CAT",
                    bodyColor = 0xFFFFA726.toInt(),
                    eyesType = 1,
                    accessoryId = "none",
                    balance = 50,
                    currentPeriodIndex = 1,
                    carePoints = 0,
                    growthStage = "BABY",
                    satiety = 100,
                    health = 100,
                    mood = 80,
                    activeGoalId = "goal_house",
                    isDemoMode = false
                )
                finPetRepository.saveProfile(defaultProfile)

                // Инициализация периода 1
                val firstPeriod = PeriodEntity(
                    periodIndex = 1,
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

            // Инициализация дефолтных целей из JSON
            val goals = getGoals().map {
                GoalEntity(
                    id = it.id,
                    title = it.title,
                    targetCost = it.targetCost,
                    savedAmount = 0,
                    isReached = false,
                    iconName = it.iconName
                )
            }
            finPetRepository.initDefaultGoals(goals)
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
