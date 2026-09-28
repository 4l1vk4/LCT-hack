package ru.finpet.kids.core.domain.usecase

import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.PurchaseEntity
import ru.finpet.kids.core.domain.calculator.PetEconomyCalculator
import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetPlan
import ru.finpet.kids.core.domain.model.GrowthStage
import ru.finpet.kids.core.domain.model.PeriodResolution
import ru.finpet.kids.core.domain.model.PetStats
import ru.finpet.kids.core.domain.repository.FinPetRepository
import javax.inject.Inject

sealed interface BudgetValidationResult {
    data object Success : BudgetValidationResult
    data class ExceedsBalance(val planned: Int, val available: Int) : BudgetValidationResult
    data object NegativeValues : BudgetValidationResult
}

class ConfirmBudgetUseCase @Inject constructor(
    private val repository: FinPetRepository
) {
    suspend operator fun invoke(periodIndex: Int, plan: BudgetPlan): BudgetValidationResult =
        repository.inTransaction<BudgetValidationResult> {
            val profile = repository.getProfileSync()
                ?: return@inTransaction BudgetValidationResult.NegativeValues
            if (plan.plannedMandatory < 0 || plan.plannedOptional < 0 || plan.plannedSavings < 0) {
                return@inTransaction BudgetValidationResult.NegativeValues
            }
            if (plan.totalPlanned > profile.balance) {
                return@inTransaction BudgetValidationResult.ExceedsBalance(plan.totalPlanned, profile.balance)
            }

            val existingPeriod = repository.getPeriodSync(periodIndex)
            val updatedPeriod = existingPeriod?.copy(
                plannedMandatory = plan.plannedMandatory,
                plannedOptional = plan.plannedOptional,
                plannedSavings = plan.plannedSavings,
                isBudgetConfirmed = true
            ) ?: PeriodEntity(
                periodIndex = periodIndex,
                plannedMandatory = plan.plannedMandatory,
                plannedOptional = plan.plannedOptional,
                plannedSavings = plan.plannedSavings,
                isBudgetConfirmed = true
            )

            repository.savePeriod(updatedPeriod)
            BudgetValidationResult.Success
        }
}

sealed interface PurchaseResult {
    data class Success(val newBalance: Int, val itemTitle: String) : PurchaseResult
    data class NotEnoughMoney(val missingCoins: Int, val available: Int, val price: Int) : PurchaseResult
    data object ProfileNotFound : PurchaseResult
}

class MakePurchaseUseCase @Inject constructor(
    private val repository: FinPetRepository
) {
    suspend operator fun invoke(
        itemId: String,
        itemName: String,
        category: String, // MANDATORY, OPTIONAL
        price: Int,
        satietyBonus: Int = 0,
        moodBonus: Int = 0,
        healthBonus: Int = 0
    ): PurchaseResult = repository.inTransaction<PurchaseResult> {
        val profile = repository.getProfileSync() ?: return@inTransaction PurchaseResult.ProfileNotFound
        if (profile.balance < price) {
            return@inTransaction PurchaseResult.NotEnoughMoney(
                missingCoins = price - profile.balance,
                available = profile.balance,
                price = price
            )
        }

        val newBalance = profile.balance - price

        val purchase = PurchaseEntity(
            periodIndex = profile.currentPeriodIndex,
            itemId = itemId,
            itemName = itemName,
            category = category,
            price = price
        )
        repository.recordPurchase(purchase)

        // Обновление статов периода
        val period = repository.getPeriodSync(profile.currentPeriodIndex) ?: PeriodEntity(profile.currentPeriodIndex)
        val updatedPeriod = if (category == "MANDATORY") {
            period.copy(actualMandatory = period.actualMandatory + price)
        } else {
            period.copy(actualOptional = period.actualOptional + price)
        }
        repository.savePeriod(updatedPeriod)

        // Обновление статов питомца. Баланс пишется один раз здесь —
        // отдельный updateBalance ниже был бы второй перезаписью той же строки.
        val newSatiety = (profile.satiety + satietyBonus).coerceIn(0, 100)
        val newMood = (profile.mood + moodBonus).coerceIn(0, 100)
        val newHealth = (profile.health + healthBonus).coerceIn(0, 100)

        repository.saveProfile(
            profile.copy(
                balance = newBalance,
                satiety = newSatiety,
                mood = newMood,
                health = newHealth
            )
        )

        PurchaseResult.Success(newBalance, itemName)
    }
}

sealed interface GoalDepositResult {
    data class Success(val newBalance: Int, val newSaved: Int, val isGoalReached: Boolean, val depositedAmount: Int) : GoalDepositResult
    data class NotEnoughMoney(val missingCoins: Int) : GoalDepositResult
    data object GoalNotFound : GoalDepositResult
}

class DepositToGoalUseCase @Inject constructor(
    private val repository: FinPetRepository
) {
    suspend operator fun invoke(goalId: String, amount: Int): GoalDepositResult =
        repository.inTransaction<GoalDepositResult> {
            val profile = repository.getProfileSync() ?: return@inTransaction GoalDepositResult.GoalNotFound
            if (profile.balance < amount) {
                return@inTransaction GoalDepositResult.NotEnoughMoney(amount - profile.balance)
            }

            val goal = repository.getGoalByIdSync(goalId) ?: return@inTransaction GoalDepositResult.GoalNotFound

            val remaining = (goal.targetCost - goal.savedAmount).coerceAtLeast(0)

            val actualAmount = minOf(amount, remaining, profile.balance)
            if (actualAmount <= 0) {
                return@inTransaction GoalDepositResult.NotEnoughMoney(amount - profile.balance)
            }

            // Списание с баланса
            val newBalance = profile.balance - actualAmount
            repository.updateBalance(newBalance)

            // Пополнение цели
            val newSaved = goal.savedAmount + actualAmount
            val isReached = newSaved >= goal.targetCost
            repository.updateGoal(goal.copy(savedAmount = newSaved, isReached = isReached))

            // Учет в периоде
            val period = repository.getPeriodSync(profile.currentPeriodIndex) ?: PeriodEntity(profile.currentPeriodIndex)
            repository.savePeriod(period.copy(actualSavings = period.actualSavings + amount))

            GoalDepositResult.Success(newBalance, newSaved, isReached, actualAmount)
        }
}

class CompletePeriodUseCase @Inject constructor(
    private val repository: FinPetRepository
) {
    suspend operator fun invoke(): PeriodResolution? = repository.inTransaction<PeriodResolution?> {
        val profile = repository.getProfileSync() ?: return@inTransaction null
        val period = repository.getPeriodSync(profile.currentPeriodIndex) ?: PeriodEntity(profile.currentPeriodIndex)

        val plan = BudgetPlan(
            plannedMandatory = period.plannedMandatory,
            plannedOptional = period.plannedOptional,
            plannedSavings = period.plannedSavings
        )
        val actual = ActualExpenses(
            actualMandatory = period.actualMandatory,
            actualOptional = period.actualOptional,
            actualSavings = period.actualSavings,
            hasPurchasedFood = period.actualMandatory >= PetEconomyCalculator.MIN_FOOD_COST,
            hasPurchasedWater = period.actualMandatory >= PetEconomyCalculator.MIN_WATER_COST
        )

        val currentStats = PetStats(
            satiety = profile.satiety,
            health = profile.health,
            mood = profile.mood,
            carePoints = profile.carePoints,
            stage = GrowthStage.valueOf(profile.growthStage),
            emotion = PetEconomyCalculator.resolveEmotion(profile.satiety, profile.health, profile.mood),
            feedback = ""
        )

        val nextPeriodIndex = profile.currentPeriodIndex + 1
        val resolution = PetEconomyCalculator.resolvePeriod(
            currentStats = currentStats,
            plan = plan,
            actual = actual,
            nextDayIndex = nextPeriodIndex
        )

        // Закрываем текущий период
        repository.savePeriod(
            period.copy(
                isPeriodClosed = true,
                compliancePercent = resolution.compliance.totalPercent,
                carePointsEarned = resolution.carePointsEarned
            )
        )

        var newIsPetRunaway = profile.isPetRunaway
        var newRunawayDaysLeft = profile.runawayDaysLeft
        var newBowlPlacedToday = false // Сбрасываем на новый день

        var finalMood = resolution.newStats.mood
        var finalSatiety = resolution.newStats.satiety
        var finalHealth = resolution.newStats.health

        if (profile.isPetRunaway) {
            if (profile.bowlPlacedToday) {
                val daysRemaining = profile.runawayDaysLeft - 1
                if (daysRemaining <= 0) {
                    // Питомец возвращается домой!
                    newIsPetRunaway = false
                    newRunawayDaysLeft = 0
                    finalMood = 60
                    finalSatiety = 80
                    finalHealth = maxOf(finalHealth, 70)
                } else {
                    newRunawayDaysLeft = daysRemaining
                    newIsPetRunaway = true
                }
            } else {
                // Миска не была поставлена — питомец не вернется, пока не поставят корм
                newIsPetRunaway = true
                newRunawayDaysLeft = profile.runawayDaysLeft
            }
        } else {
            // Если питомец был дома, но настроение упало до 0% — он убегает
            if (resolution.newStats.mood <= 0) {
                newIsPetRunaway = true
                newRunawayDaysLeft = (1..3).random()
                finalMood = 0
            }
        }

        // Обновляем профиль на следующий период
        repository.saveProfile(
            profile.copy(
                balance = profile.balance + resolution.periodIncome,
                currentPeriodIndex = nextPeriodIndex,
                carePoints = resolution.newStats.carePoints,
                growthStage = resolution.newStats.stage.name,
                satiety = finalSatiety,
                health = finalHealth,
                mood = finalMood,
                isPetRunaway = newIsPetRunaway,
                runawayDaysLeft = newRunawayDaysLeft,
                bowlPlacedToday = newBowlPlacedToday
            )
        )

        // Инициализируем новый период
        repository.savePeriod(PeriodEntity(periodIndex = nextPeriodIndex))

        resolution
    }
}
