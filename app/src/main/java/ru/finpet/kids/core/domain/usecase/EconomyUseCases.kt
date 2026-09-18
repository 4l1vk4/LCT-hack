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
    suspend operator fun invoke(periodIndex: Int, plan: BudgetPlan): BudgetValidationResult {
        val profile = repository.getProfileSync() ?: return BudgetValidationResult.NegativeValues
        if (plan.plannedMandatory < 0 || plan.plannedOptional < 0 || plan.plannedSavings < 0) {
            return BudgetValidationResult.NegativeValues
        }
        if (plan.totalPlanned > profile.balance) {
            return BudgetValidationResult.ExceedsBalance(plan.totalPlanned, profile.balance)
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
        return BudgetValidationResult.Success
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
    ): PurchaseResult {
        val profile = repository.getProfileSync() ?: return PurchaseResult.ProfileNotFound
        if (profile.balance < price) {
            return PurchaseResult.NotEnoughMoney(
                missingCoins = price - profile.balance,
                available = profile.balance,
                price = price
            )
        }

        val newBalance = profile.balance - price
        repository.updateBalance(newBalance)

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

        // Обновление статов питомца
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

        return PurchaseResult.Success(newBalance, itemName)
    }
}

sealed interface GoalDepositResult {
    data class Success(val newBalance: Int, val newSaved: Int, val isGoalReached: Boolean) : GoalDepositResult
    data class NotEnoughMoney(val missingCoins: Int) : GoalDepositResult
    data object GoalNotFound : GoalDepositResult
}

class DepositToGoalUseCase @Inject constructor(
    private val repository: FinPetRepository
) {
    suspend operator fun invoke(goalId: String, amount: Int): GoalDepositResult {
        val profile = repository.getProfileSync() ?: return GoalDepositResult.GoalNotFound
        if (profile.balance < amount) {
            return GoalDepositResult.NotEnoughMoney(amount - profile.balance)
        }

        val goal = repository.getGoalById(goalId)
        // Для синхронного чтения можно использовать прямой метод или список
        val allGoals = repository.getAllGoals()
        // Найдем цель
        val currentGoal = repository.getProfileSync()?.let {
            // обновим баланс
            val newBalance = it.balance - amount
            repository.updateBalance(newBalance)

            // обновим период
            val period = repository.getPeriodSync(it.currentPeriodIndex) ?: PeriodEntity(it.currentPeriodIndex)
            repository.savePeriod(period.copy(actualSavings = period.actualSavings + amount))

            newBalance
        } ?: return GoalDepositResult.GoalNotFound

        return GoalDepositResult.Success(currentGoal, amount, false)
    }
}

class CompletePeriodUseCase @Inject constructor(
    private val repository: FinPetRepository
) {
    suspend operator fun invoke(): PeriodResolution? {
        val profile = repository.getProfileSync() ?: return null
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

        val resolution = PetEconomyCalculator.resolvePeriod(currentStats, plan, actual)

        // Закрываем текущий период
        repository.savePeriod(
            period.copy(
                isPeriodClosed = true,
                compliancePercent = resolution.compliance.totalPercent,
                carePointsEarned = resolution.carePointsEarned
            )
        )

        // Обновляем профиль на следующий период
        val nextPeriodIndex = profile.currentPeriodIndex + 1
        repository.saveProfile(
            profile.copy(
                balance = profile.balance + resolution.periodIncome,
                currentPeriodIndex = nextPeriodIndex,
                carePoints = resolution.newStats.carePoints,
                growthStage = resolution.newStats.stage.name,
                satiety = resolution.newStats.satiety,
                health = resolution.newStats.health,
                mood = resolution.newStats.mood
            )
        )

        // Инициализируем новый период
        repository.savePeriod(PeriodEntity(periodIndex = nextPeriodIndex))

        return resolution
    }
}
