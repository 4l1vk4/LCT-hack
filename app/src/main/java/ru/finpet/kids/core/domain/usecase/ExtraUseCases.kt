package ru.finpet.kids.core.domain.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity
import ru.finpet.kids.core.data.repository.ContentRepository
import ru.finpet.kids.core.domain.calculator.PetEconomyCalculator
import ru.finpet.kids.core.domain.model.GrowthStage
import ru.finpet.kids.core.domain.repository.FinPetRepository
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.random.Random

// --- 1. Снятие средств из копилки ---
sealed interface GoalWithdrawResult {
    data class Success(val newBalance: Int, val newSaved: Int, val delayPeriods: Int) : GoalWithdrawResult
    data class NotEnoughSaved(val availableSaved: Int) : GoalWithdrawResult
    data object GoalNotFound : GoalWithdrawResult
}

class WithdrawFromGoalUseCase @Inject constructor(
    private val repository: FinPetRepository
) {
    suspend operator fun invoke(goalId: String, amount: Int): GoalWithdrawResult =
        repository.inTransaction<GoalWithdrawResult> {
            val profile = repository.getProfileSync() ?: return@inTransaction GoalWithdrawResult.GoalNotFound
            val goal = repository.getGoalByIdSync(goalId) ?: return@inTransaction GoalWithdrawResult.GoalNotFound

            if (goal.savedAmount < amount) {
                return@inTransaction GoalWithdrawResult.NotEnoughSaved(goal.savedAmount)
            }

            val newSaved = goal.savedAmount - amount
            val newBalance = profile.balance + amount

            repository.updateGoal(goal.copy(savedAmount = newSaved, isReached = false))
            repository.updateBalance(newBalance)

            val delayPeriods = ceil(amount.toDouble() / 50.0).toInt().coerceAtLeast(1)

            GoalWithdrawResult.Success(newBalance, newSaved, delayPeriods)
        }
}

// --- 2. Движок квестов ---
data class QuestExecutionResult(
    val coinsAwarded: Int,
    val feedback: String,
    val isRecommended: Boolean,
    val newBalance: Int
)

class QuestEngineUseCase @Inject constructor(
    private val repository: FinPetRepository,
    private val contentRepository: ContentRepository
) {
    suspend fun executeOption(questId: String, optionId: String): QuestExecutionResult? {
        // JSON-кэш читаем до транзакции: внутри транзакции нельзя уходить
        // на другой диспетчер (это разорвало бы привязку к транзакционному потоку).
        val quests = contentRepository.getQuests()
        val quest = quests.find { it.id == questId } ?: return null
        val option = quest.options.find { it.id == optionId } ?: return null

        return repository.inTransaction<QuestExecutionResult?> {
            val profile = repository.getProfileSync() ?: return@inTransaction null

            val progress = QuestProgressEntity(
                questId = questId,
                isCompleted = true,
                selectedOptionId = optionId,
                rewardClaimed = true,
                completedInPeriod = profile.currentPeriodIndex
            )
            repository.saveQuestProgress(progress)

            QuestExecutionResult(
                coinsAwarded = 0,
                feedback = option.feedback,
                isRecommended = option.isRecommended,
                newBalance = profile.balance
            )
        }
    }
}

// --- 3. Раздел для взрослого ---
data class AdultChallenge(
    val title: String,
    val question: String,
    val correctAnswer: String,
    val options: List<String>
)

data class MathProblem(
    val expression: String,
    val correctAnswer: Int,
    val options: List<Int>
)

class AdultSectionUseCase @Inject constructor(
    private val repository: FinPetRepository
) {
    fun generateAdultChallenge(): AdultChallenge {
        val challenges = listOf(
            AdultChallenge(
                title = "Налоговая грамотность",
                question = "Какова базовая ставка налога на доходы физических лиц (НДФЛ) в РФ?",
                correctAnswer = "13%",
                options = listOf("13%", "20%", "7%").shuffled()
            ),
            AdultChallenge(
                title = "Гражданское право",
                question = "С какого возраста в РФ наступает полная гражданская дееспособность?",
                correctAnswer = "18 лет",
                options = listOf("18 лет", "14 лет", "21 год").shuffled()
            ),
            AdultChallenge(
                title = "Государственные документы",
                question = "Как расшифровывается аббревиатура ИНН гражданина?",
                correctAnswer = "Идентификационный номер налогоплательщика",
                options = listOf(
                    "Идентификационный номер налогоплательщика",
                    "Индивидуальный номер населения",
                    "Индекс налоговых накоплений"
                ).shuffled()
            ),
            AdultChallenge(
                title = "Алгебра (7 класс)",
                question = "Решите уравнение: 4x - 18 = 46. Чему равен x?",
                correctAnswer = "16",
                options = listOf("16", "14", "18").shuffled()
            ),
            AdultChallenge(
                title = "Алгебра (7 класс)",
                question = "Решите уравнение: 5x + 27 = 92. Чему равен x?",
                correctAnswer = "13",
                options = listOf("13", "11", "15").shuffled()
            ),
            AdultChallenge(
                title = "Финансовая математика",
                question = "Сколько рублей составляют 15% скидки от суммы 600 рублей?",
                correctAnswer = "90 руб.",
                options = listOf("90 руб.", "60 руб.", "120 руб.").shuffled()
            ),
            AdultChallenge(
                title = "Банковские вклады",
                question = "Доход по банковскому вкладу 12% годовых от 10 000 рублей за год составит:",
                correctAnswer = "1 200 руб.",
                options = listOf("1 200 руб.", "800 руб.", "1 500 руб.").shuffled()
            ),
            AdultChallenge(
                title = "Документооборот",
                question = "Какой документ подтверждает регистрацию права собственности на квартиру?",
                correctAnswer = "Выписка из ЕГРН",
                options = listOf("Выписка из ЕГРН", "Паспорт БТИ", "Договор задатка").shuffled()
            ),
            AdultChallenge(
                title = "Социальные стандарты",
                question = "Какой документ удостоверяет регистрацию в системе пенсионного страхования РФ?",
                correctAnswer = "СНИЛС",
                options = listOf("СНИЛС", "ОМС", "ОГРН").shuffled()
            ),
            AdultChallenge(
                title = "Система уравнений (7 класс)",
                question = "Решите систему: x + y = 20, x - y = 6. Чему равен x?",
                correctAnswer = "13",
                options = listOf("13", "14", "11").shuffled()
            )
        )
        return challenges.random()
    }

    fun generateMathProblem(): MathProblem {
        val a = Random.nextInt(15, 45)
        val b = Random.nextInt(15, 45)
        return MathProblem("$a + $b", a + b, listOf(a + b, a + b - 3, a + b + 4).shuffled())
    }

    suspend fun grantParentBonus(coins: Int, reason: String): Int =
        repository.inTransaction {
            val profile = repository.getProfileSync() ?: return@inTransaction 0
            val newBalance = profile.balance + coins
            val newMood = (profile.mood + 15).coerceAtMost(100)
            repository.saveProfile(profile.copy(balance = newBalance, mood = newMood))
            newBalance
        }
}

// --- 4. Аналитика компетенций (Минфин РФ) ---
data class CompetencyItem(
    val title: String,
    val scorePercent: Int,
    val statusDescription: String,
    val advice: String
)

data class CompetencyReport(
    val budgetDisciplinePercent: Int,
    val essentialSpendingRatio: Int,
    val totalSavedCoins: Int,
    val completedQuestsCount: Int,
    val competencies: List<CompetencyItem>
)

class CompetencyTracker @Inject constructor(
    private val repository: FinPetRepository,
    private val contentRepository: ContentRepository
) {
    /**
     * Отчёт считается агрегатами SQL (SUM/COUNT/AVG) параллельно:
     * 5 лёгких запросов вместо 4 последовательных выборок всей таблицы в память.
     */
    suspend fun generateReport(): CompetencyReport = coroutineScope {
        val avgComplianceDeferred = async { repository.getAvgClosedCompliance() }
        val mandatoryDeferred = async { repository.getCategorySpending("MANDATORY") }
        val optionalDeferred = async { repository.getCategorySpending("OPTIONAL") }
        val savedDeferred = async { repository.getTotalSavedAmount() }
        val questsDeferred = async { repository.getCompletedQuestCount() }

        val avgCompliance = avgComplianceDeferred.await()?.toInt() ?: 80 // Базовый прогноз
        val mandatorySum = mandatoryDeferred.await()
        val optionalSum = optionalDeferred.await()
        val totalPurchases = mandatorySum + optionalSum
        val essentialRatio = if (totalPurchases > 0) {
            ((mandatorySum.toDouble() / totalPurchases) * 100).toInt()
        } else {
            65 // Рекомендованный баланс по умолчанию
        }

        val totalSaved = savedDeferred.await()
        val completedQuests = questsDeferred.await()

        val competencies = listOf(
            CompetencyItem(
                title = "Планирование личного бюджета",
                scorePercent = avgCompliance.coerceIn(40, 100),
                statusDescription = if (avgCompliance >= 75) "Отличная дисциплина" else "Формируется привычка",
                advice = "Ребенок старается не тратить больше, чем спланировал на день."
            ),
            CompetencyItem(
                title = "Различение необходимого и желаемого",
                scorePercent = essentialRatio.coerceIn(30, 100),
                statusDescription = if (essentialRatio >= 50) "Осознанное потребление" else "Преобладают сиюминутные желания",
                advice = "Базовые потребности питомца (еда и уход) обеспечиваются в первую очередь."
            ),
            CompetencyItem(
                title = "Навык сбережений и целей",
                scorePercent = (totalSaved * 100 / 150).coerceIn(20, 100),
                statusDescription = if (totalSaved >= 50) "Уверенно копит" else "Первые накопления",
                advice = "Регулярно откладывает часть карманных денег в копилку."
            ),
            CompetencyItem(
                title = "Осознанные финансовые решения",
                scorePercent = ((completedQuests.toDouble() / 6.0) * 100).toInt().coerceIn(15, 100),
                statusDescription = "Решено $completedQuests из 6 ситуаций",
                advice = "Понимает, как противостоять уловкам и действовать при поломках."
            )
        )

        CompetencyReport(
            budgetDisciplinePercent = avgCompliance,
            essentialSpendingRatio = essentialRatio,
            totalSavedCoins = totalSaved,
            completedQuestsCount = completedQuests,
            competencies = competencies
        )
    }
}

// --- 5. Сброс профиля в 1 клик для экспертов ---
class ResetDemoProfileUseCase @Inject constructor(
    private val repository: FinPetRepository,
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke() {
        repository.resetAllData()
        contentRepository.ensureDataInitialized()
    }
}
