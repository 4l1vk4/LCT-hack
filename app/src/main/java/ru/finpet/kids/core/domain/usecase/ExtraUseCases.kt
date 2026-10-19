package ru.finpet.kids.core.domain.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.QuestProgressEntity
import ru.finpet.kids.core.data.repository.ContentRepository
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

            // Учет в периоде:
            // 1. Если копили в текущем периоде — монетки забираются из банки текущего периода (actualSavings уменьшается).
            // 2. Если сумма превышает накопленное в текущем периоде (накоплено ранее в прошлых периодах) —
            //    это не уводит текущую банку в минус (не искажает визуал), но фиксируется в факте (withdrawnSavings),
            //    что мы забрали ранее накопленные средства.
            val period = repository.getPeriodSync(profile.currentPeriodIndex) ?: PeriodEntity(profile.currentPeriodIndex)
            val fromCurrentPeriod = minOf(period.actualSavings, amount)
            val fromPastSavings = amount - fromCurrentPeriod

            val updatedPeriod = period.copy(
                actualSavings = maxOf(0, period.actualSavings - fromCurrentPeriod),
                withdrawnSavings = period.withdrawnSavings + fromPastSavings
            )
            repository.savePeriod(updatedPeriod)

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
        val quests = contentRepository.getQuests()
        val quest = quests.find { it.id == questId } ?: return null
        val option = quest.options.find { it.id == optionId } ?: return null

        return repository.inTransaction<QuestExecutionResult?> {
            val profile = repository.getProfileSync() ?: return@inTransaction null

            val coinsToAward = option.rewardCoins
            val newBalance = profile.balance + coinsToAward
            repository.saveProfile(profile.copy(balance = newBalance))

            val progress = QuestProgressEntity(
                questId = questId,
                isCompleted = true,
                selectedOptionId = optionId,
                rewardClaimed = true,
                completedInPeriod = profile.currentPeriodIndex
            )
            repository.saveQuestProgress(progress)

            QuestExecutionResult(
                coinsAwarded = coinsToAward,
                feedback = option.feedback,
                isRecommended = option.isRecommended,
                newBalance = newBalance
            )
        }
    }

    suspend fun executeTextAnswer(questId: String, answer: String): QuestExecutionResult? {
        val quests = contentRepository.getQuests()
        val quest = quests.find { it.id == questId } ?: return null

        val cleanAnswer = answer.trim().lowercase()
        val isCorrect = quest.expectedAnswers.any { expected ->
            val cleanExpected = expected.trim().lowercase()
            cleanAnswer == cleanExpected || cleanAnswer.contains(cleanExpected) || cleanExpected.contains(cleanAnswer)
        }

        return repository.inTransaction<QuestExecutionResult?> {
            val profile = repository.getProfileSync() ?: return@inTransaction null

            val coinsToAward = if (isCorrect) quest.rewardCoins else 0
            val newBalance = profile.balance + coinsToAward
            repository.saveProfile(profile.copy(balance = newBalance))

            val progress = QuestProgressEntity(
                questId = questId,
                isCompleted = true,
                selectedOptionId = "text:$cleanAnswer",
                rewardClaimed = true,
                completedInPeriod = profile.currentPeriodIndex
            )
            repository.saveQuestProgress(progress)

            val feedback = if (isCorrect) {
                quest.feedbackSuccess.ifEmpty { "Правильно! Ты отлично разбираешься в финансовых терминах!" }
            } else {
                val correctExample = quest.expectedAnswers.firstOrNull() ?: ""
                quest.feedbackFailure.ifEmpty { "Хорошая попытка! Верный ответ: $correctExample." }
            }

            QuestExecutionResult(
                coinsAwarded = coinsToAward,
                feedback = feedback,
                isRecommended = isCorrect,
                newBalance = newBalance
            )
        }
    }

    suspend fun executeAllocation(questId: String, allocatedAmount: Int): QuestExecutionResult? {
        val quests = contentRepository.getQuests()
        val quest = quests.find { it.id == questId } ?: return null

        val isBalanced = allocatedAmount in quest.recommendedMin..quest.recommendedMax

        return repository.inTransaction<QuestExecutionResult?> {
            val profile = repository.getProfileSync() ?: return@inTransaction null

            val coinsToAward = if (isBalanced) quest.rewardCoins else 0
            val newBalance = profile.balance + coinsToAward
            repository.saveProfile(profile.copy(balance = newBalance))

            val progress = QuestProgressEntity(
                questId = questId,
                isCompleted = true,
                selectedOptionId = "alloc:$allocatedAmount",
                rewardClaimed = true,
                completedInPeriod = profile.currentPeriodIndex
            )
            repository.saveQuestProgress(progress)

            val feedback = if (isBalanced) {
                quest.allocationSuccessFeedback.ifEmpty { "Прекрасное распределение бюджета! Мудрый и взвешенный выбор." }
            } else {
                quest.allocationWarningFeedback.ifEmpty { "Ты сделал выбор! Стремись находить баланс между текущими радостями и будущей целью." }
            }

            QuestExecutionResult(
                coinsAwarded = coinsToAward,
                feedback = feedback,
                isRecommended = isBalanced,
                newBalance = newBalance
            )
        }
    }
}

// --- 3. Раздел для взрослого ---
data class AdultChallenge(
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
                question = "Какова базовая ставка налога на доходы физических лиц (НДФЛ) в РФ?",
                correctAnswer = "13%",
                options = listOf("13%", "20%", "7%").shuffled()
            ),
            AdultChallenge(
                question = "С какого возраста в РФ наступает полная гражданская дееспособность?",
                correctAnswer = "18 лет",
                options = listOf("18 лет", "14 лет", "21 год").shuffled()
            ),
            AdultChallenge(
                question = "Как расшифровывается аббревиатура ИНН гражданина?",
                correctAnswer = "Идентификационный номер налогоплательщика",
                options = listOf(
                    "Идентификационный номер налогоплательщика",
                    "Индивидуальный номер населения",
                    "Индекс налоговых накоплений"
                ).shuffled()
            ),
            AdultChallenge(
                question = "Решите уравнение: 4x - 18 = 46. Чему равен x?",
                correctAnswer = "16",
                options = listOf("16", "14", "18").shuffled()
            ),
            AdultChallenge(
                question = "Решите уравнение: 5x + 27 = 92. Чему равен x?",
                correctAnswer = "13",
                options = listOf("13", "11", "15").shuffled()
            ),
            AdultChallenge(
                question = "Сколько рублей составляют 15% скидки от суммы 600 рублей?",
                correctAnswer = "90 руб.",
                options = listOf("90 руб.", "60 руб.", "120 руб.").shuffled()
            ),
            AdultChallenge(
                question = "Доход по банковскому вкладу 12% годовых от 10 000 рублей за год составит:",
                correctAnswer = "1 200 руб.",
                options = listOf("1 200 руб.", "800 руб.", "1 500 руб.").shuffled()
            ),
            AdultChallenge(
                question = "Какой документ подтверждает регистрацию права собственности на квартиру?",
                correctAnswer = "Выписка из ЕГРН",
                options = listOf("Выписка из ЕГРН", "Паспорт БТИ", "Договор задатка").shuffled()
            ),
            AdultChallenge(
                question = "Какой документ удостоверяет регистрацию в системе пенсионного страхования РФ?",
                correctAnswer = "СНИЛС",
                options = listOf("СНИЛС", "ОМС", "ОГРН").shuffled()
            ),
            AdultChallenge(
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
            val newMood = if (profile.isPetRunaway) 0 else (profile.mood + 15).coerceAtMost(100)
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
        val allQuests = contentRepository.getQuests()
        val totalQuestsCount = allQuests.size.coerceAtLeast(1)

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
                scorePercent = ((completedQuests.toDouble() / totalQuestsCount) * 100).toInt().coerceIn(15, 100),
                statusDescription = "Решено $completedQuests из $totalQuestsCount ситуаций",
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
