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
            val remainingMandatory = maxOf(0, period.plannedMandatory - period.actualMandatory)
            if (period.isBudgetConfirmed && period.plannedMandatory > 0 && price > remainingMandatory) {
                // Если обязательного не хватает, недостающая часть берется из банки «Желаемое»!
                val fromMandatory = remainingMandatory
                val fromOptional = price - remainingMandatory
                period.copy(
                    actualMandatory = period.actualMandatory + fromMandatory,
                    actualOptional = period.actualOptional + fromOptional,
                    borrowedFromOptional = period.borrowedFromOptional + fromOptional
                )
            } else {
                period.copy(actualMandatory = period.actualMandatory + price)
            }
        } else {
            period.copy(actualOptional = period.actualOptional + price)
        }
        repository.savePeriod(updatedPeriod)

        // Обновление статов питомца. Баланс пишется один раз здесь —
        // отдельный updateBalance ниже был бы второй перезаписью той же строки.
        // Если питомец сбежал, статы не восстанавливаются ни при каких обстоятельствах.
        val newSatiety = if (profile.isPetRunaway) profile.satiety else (profile.satiety + satietyBonus).coerceIn(0, 100)
        val newMood = if (profile.isPetRunaway) 0 else (profile.mood + moodBonus).coerceIn(0, 100)
        val newHealth = if (profile.isPetRunaway) profile.health else (profile.health + healthBonus).coerceIn(0, 100)

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
            repository.savePeriod(period.copy(actualSavings = period.actualSavings + actualAmount))

            GoalDepositResult.Success(newBalance, newSaved, isReached, actualAmount)
        }
}

class CompletePeriodUseCase @Inject constructor(
    private val repository: FinPetRepository
) {
    suspend operator fun invoke(
        nextDayIndex: Int? = null,
        yearMonth: java.time.YearMonth? = null,
        isPeriodSkip: Boolean = false
    ): PeriodResolution? = repository.inTransaction<PeriodResolution?> {
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
            withdrawnSavings = period.withdrawnSavings,
            borrowedFromOptional = period.borrowedFromOptional,
            hasPurchasedFood = period.actualMandatory >= PetEconomyCalculator.MIN_FOOD_COST,
            hasPurchasedWater = period.actualMandatory >= PetEconomyCalculator.MIN_WATER_COST
        )

        val currentStats = PetStats(
            satiety = profile.satiety,
            health = profile.health,
            mood = if (profile.isPetRunaway) 0 else profile.mood,
            carePoints = profile.carePoints,
            stage = GrowthStage.valueOf(profile.growthStage),
            emotion = PetEconomyCalculator.resolveEmotion(profile.satiety, profile.health, if (profile.isPetRunaway) 0 else profile.mood),
            feedback = ""
        )

        val nextPeriodIndex = profile.currentPeriodIndex + 1
        val resolution = PetEconomyCalculator.resolvePeriod(
            currentStats = currentStats,
            plan = plan,
            actual = actual,
            nextDayIndex = nextDayIndex ?: nextPeriodIndex,
            yearMonth = yearMonth,
            isPetRunaway = profile.isPetRunaway,
            hasRecoveredPet = profile.isPetRunaway && profile.bowlPlacedToday,
            isPeriodSkip = isPeriodSkip
        ).copy(closedDayIndex = profile.currentPeriodIndex)

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
        var newConsecutivePeriodSkips = profile.consecutivePeriodSkips
        var newLastSkipMood = profile.lastSkipMood

        val foodPurchased = actual.hasPurchasedFood || actual.actualMandatory >= PetEconomyCalculator.MIN_FOOD_COST

        var finalMood = if (!foodPurchased) {
            minOf(profile.mood, resolution.newStats.mood)
        } else {
            profile.mood
        }
        var finalSatiety = if (!foodPurchased) {
            minOf(profile.satiety, resolution.newStats.satiety)
        } else {
            profile.satiety
        }
        var finalHealth = if (!foodPurchased) {
            minOf(profile.health, resolution.newStats.health)
        } else {
            profile.health
        }
        var finalCarePoints = resolution.newStats.carePoints
        var finalGrowthStage = resolution.newStats.stage.name

        if (profile.isPetRunaway) {
            // пока питомец в побеге, очки заботы, рост и все статы (сытость, настроение, здоровье) заморожены
            finalCarePoints = profile.carePoints
            finalGrowthStage = profile.growthStage
            finalMood = 0
            finalSatiety = profile.satiety
            finalHealth = profile.health

            if (profile.bowlPlacedToday) {
                val daysRemaining = profile.runawayDaysLeft - 1
                if (daysRemaining <= 0) {
                    // возвращение питомца домой
                    newIsPetRunaway = false
                    newRunawayDaysLeft = 0
                    finalMood = 60
                    finalSatiety = 80
                    finalHealth = maxOf(profile.health, 70)
                    newConsecutivePeriodSkips = 0
                    newLastSkipMood = 60
                } else {
                    newRunawayDaysLeft = daysRemaining
                    newIsPetRunaway = true
                    finalMood = 0
                }
            } else {
                // без миски корма питомец не возвращается
                newIsPetRunaway = true
                newRunawayDaysLeft = profile.runawayDaysLeft
                finalMood = 0
            }
        } else {
            // питомец сейчас дома
            val effectiveMood = PetEconomyCalculator.calculateEffectiveMood(
                mood = finalMood,
                satiety = finalSatiety,
                health = finalHealth
            )
            val shouldRunawayNormally = (finalMood <= 0 || effectiveMood <= 0)

            if (isPeriodSkip) {
                if (profile.consecutivePeriodSkips == 0) {
                    // ПЕРВЫЙ ПРОПУСК ПЕРИОДА: кот НЕ сбегает с 1-го раза
                    newIsPetRunaway = false
                    newRunawayDaysLeft = 0
                    newConsecutivePeriodSkips = 1
                    newLastSkipMood = finalMood
                } else {
                    // ПОВТОРНЫЙ ПРОПУСК: проверяем, подняли ли настроение хотя бы на 1%
                    val moodWasRaised = profile.mood >= profile.lastSkipMood + 1
                    if (moodWasRaised) {
                        newIsPetRunaway = false
                        newRunawayDaysLeft = 0
                        newConsecutivePeriodSkips = 1
                        newLastSkipMood = finalMood
                    } else {
                        // Настроение не поднимали: кот убегает!
                        newIsPetRunaway = true
                        newRunawayDaysLeft = (1..3).random()
                        finalMood = 0
                        newConsecutivePeriodSkips = 0
                        newLastSkipMood = 0
                    }
                }
            } else {
                // Обычный переход дня
                if (shouldRunawayNormally) {
                    newIsPetRunaway = true
                    newRunawayDaysLeft = (1..3).random()
                    finalMood = 0
                    newConsecutivePeriodSkips = 0
                    newLastSkipMood = 0
                } else {
                    if (finalMood > profile.lastSkipMood) {
                        newConsecutivePeriodSkips = 0
                    }
                    newLastSkipMood = finalMood
                }
            }
        }

        // Обновляем профиль на следующий период
        repository.saveProfile(
            profile.copy(
                balance = profile.balance + resolution.periodIncome,
                currentPeriodIndex = nextPeriodIndex,
                carePoints = finalCarePoints,
                growthStage = finalGrowthStage,
                satiety = finalSatiety,
                health = finalHealth,
                mood = finalMood,
                isPetRunaway = newIsPetRunaway,
                runawayDaysLeft = newRunawayDaysLeft,
                bowlPlacedToday = newBowlPlacedToday,
                consecutivePeriodSkips = newConsecutivePeriodSkips,
                lastSkipMood = newLastSkipMood
            )
        )

        // Инициализируем новый период
        repository.savePeriod(PeriodEntity(periodIndex = nextPeriodIndex))

        resolution.copy(
            newStats = resolution.newStats.copy(
                mood = finalMood,
                satiety = finalSatiety,
                health = finalHealth,
                carePoints = finalCarePoints,
                stage = GrowthStage.valueOf(finalGrowthStage),
                emotion = PetEconomyCalculator.resolveEmotion(finalSatiety, finalHealth, finalMood)
            )
        )
    }
}
