package ru.finpet.kids.core.domain.model

enum class PetEmotion {
    HAPPY,
    NEUTRAL,
    SAD
}

enum class GrowthStage(val title: String, val minPoints: Int) {
    BABY("Малыш", 0),
    TEEN("Подросток", 5),
    ADULT("Взрослый", 10);

    companion object {
        fun fromPoints(points: Int): GrowthStage = when {
            points >= ADULT.minPoints -> ADULT
            points >= TEEN.minPoints -> TEEN
            else -> BABY
        }
    }
}

data class PetStats(
    val satiety: Int,       // 0..100
    val health: Int,        // 0..100
    val mood: Int,          // 0..100
    val carePoints: Int,    // 0..∞
    val stage: GrowthStage,
    val emotion: PetEmotion,
    val feedback: String
)

data class BudgetPlan(
    val plannedMandatory: Int = 0,
    val plannedOptional: Int = 0,
    val plannedSavings: Int = 0
) {
    val totalPlanned: Int get() = plannedMandatory + plannedOptional + plannedSavings

    fun isValidForBalance(balance: Int): Boolean =
        plannedMandatory >= 0 && plannedOptional >= 0 && plannedSavings >= 0 && totalPlanned <= balance
}

data class ActualExpenses(
    val actualMandatory: Int = 0,
    val actualOptional: Int = 0,
    val actualSavings: Int = 0,
    val hasPurchasedFood: Boolean = false,
    val hasPurchasedWater: Boolean = false,
    val hasPurchasedMedicine: Boolean = false
) {
    val totalActual: Int get() = actualMandatory + actualOptional + actualSavings
}

data class BudgetCompliance(
    val mandatoryScore: Float,  // 0.0 .. 1.0
    val optionalScore: Float,   // 0.0 .. 1.0
    val savingsScore: Float,    // 0.0 .. 1.0
    val totalPercent: Int,      // 0 .. 100
    val isDisciplined: Boolean, // true if totalPercent >= 75
    val feedback: String
)

data class PeriodResolution(
    val newStats: PetStats,
    val compliance: BudgetCompliance,
    val carePointsEarned: Int,
    val periodIncome: Int = 100,
    val summaryFeedback: String
)
