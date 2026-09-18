package ru.finpet.kids.core.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetPlan
import ru.finpet.kids.core.domain.model.GrowthStage
import ru.finpet.kids.core.domain.model.PetEmotion
import ru.finpet.kids.core.domain.model.PetStats

class PetEconomyCalculatorTest {

    @Test
    fun `calculateSatiety returns 100 when food is purchased`() {
        val expenses = ActualExpenses(actualMandatory = 30, hasPurchasedFood = true)
        val satiety = PetEconomyCalculator.calculateSatiety(expenses)
        assertEquals(100, satiety)
    }

    @Test
    fun `calculateSatiety returns 60 when only water is purchased`() {
        val expenses = ActualExpenses(actualMandatory = 10, hasPurchasedWater = true)
        val satiety = PetEconomyCalculator.calculateSatiety(expenses)
        assertEquals(60, satiety)
    }

    @Test
    fun `calculateSatiety returns 30 when nothing is purchased (pet never dies)`() {
        val expenses = ActualExpenses()
        val satiety = PetEconomyCalculator.calculateSatiety(expenses)
        assertEquals(30, satiety)
    }

    @Test
    fun `calculateHealth returns 100 when mandatory needs are covered`() {
        val plan = BudgetPlan(plannedMandatory = 30)
        val actual = ActualExpenses(actualMandatory = 30, hasPurchasedFood = true)
        val health = PetEconomyCalculator.calculateHealth(satiety = 100, plan = plan, actualExpenses = actual)
        assertEquals(100, health)
    }

    @Test
    fun `calculateHealth returns 90 when medicine is purchased after failure`() {
        val plan = BudgetPlan(plannedMandatory = 40)
        val actual = ActualExpenses(actualMandatory = 20, hasPurchasedMedicine = true)
        val health = PetEconomyCalculator.calculateHealth(satiety = 50, plan = plan, actualExpenses = actual)
        assertEquals(90, health)
    }

    @Test
    fun `calculateMood applies bonus for toys and penalty for overspending`() {
        val plan = BudgetPlan(plannedOptional = 30)
        // Overspent optional (50 > 30) -> penalty -20, but +bonus 35
        val actual = ActualExpenses(actualOptional = 50)
        val mood = PetEconomyCalculator.calculateMood(satiety = 100, plan = plan, actualExpenses = actual)
        // 60 + 35 - 20 = 75
        assertEquals(75, mood)
    }

    @Test
    fun `calculateMood clamps value between 10 and 100`() {
        val plan = BudgetPlan(plannedOptional = 10)
        // Overspent by a lot + starving
        val actual = ActualExpenses(actualOptional = 60)
        val mood = PetEconomyCalculator.calculateMood(satiety = 30, plan = plan, actualExpenses = actual)
        // 60 + 35 - 20 - 30 = 45 (still >= 10)
        assertTrue(mood in 10..100)
    }

    @Test
    fun `resolveEmotion correctly classifies HAPPY, NEUTRAL, and SAD`() {
        assertEquals(PetEmotion.HAPPY, PetEconomyCalculator.resolveEmotion(satiety = 80, health = 90, mood = 75))
        assertEquals(PetEmotion.NEUTRAL, PetEconomyCalculator.resolveEmotion(satiety = 55, health = 70, mood = 50))
        assertEquals(PetEmotion.SAD, PetEconomyCalculator.resolveEmotion(satiety = 30, health = 60, mood = 30))
    }

    @Test
    fun `calculateCompliance handles exact match and avoids division by zero`() {
        val plan = BudgetPlan(plannedMandatory = 30, plannedOptional = 20, plannedSavings = 50)
        val actual = ActualExpenses(actualMandatory = 30, actualOptional = 20, actualSavings = 50)

        val compliance = PetEconomyCalculator.calculateCompliance(plan, actual)
        assertEquals(100, compliance.totalPercent)
        assertTrue(compliance.isDisciplined)
    }

    @Test
    fun `calculateCompliance never produces negative percent on severe overspending`() {
        val plan = BudgetPlan(plannedMandatory = 10, plannedOptional = 10, plannedSavings = 10)
        val actual = ActualExpenses(actualMandatory = 100, actualOptional = 100, actualSavings = 0)

        val compliance = PetEconomyCalculator.calculateCompliance(plan, actual)
        assertTrue(compliance.totalPercent in 0..100)
    }

    @Test
    fun `calculateCarePoints awards up to 4 points for ideal behavior`() {
        val plan = BudgetPlan(plannedMandatory = 30, plannedOptional = 20, plannedSavings = 50)
        val actual = ActualExpenses(
            actualMandatory = 30,
            actualOptional = 20,
            actualSavings = 50,
            hasPurchasedFood = true
        )
        val compliance = PetEconomyCalculator.calculateCompliance(plan, actual)
        val points = PetEconomyCalculator.calculateCarePoints(
            satiety = 100,
            health = 100,
            plan = plan,
            actual = actual,
            compliance = compliance
        )
        assertEquals(4, points) // +2 needs, +1 savings, +1 discipline
    }

    @Test
    fun `resolvePeriod progresses pet through evolution stages`() {
        val initialStats = PetStats(
            satiety = 50,
            health = 60,
            mood = 50,
            carePoints = 4, // 1 point away from TEEN
            stage = GrowthStage.BABY,
            emotion = PetEmotion.NEUTRAL,
            feedback = "Начало"
        )
        val plan = BudgetPlan(plannedMandatory = 30, plannedOptional = 20, plannedSavings = 30)
        val actual = ActualExpenses(
            actualMandatory = 30,
            actualOptional = 20,
            actualSavings = 30,
            hasPurchasedFood = true
        )

        val resolution = PetEconomyCalculator.resolvePeriod(initialStats, plan, actual)
        assertEquals(GrowthStage.TEEN, resolution.newStats.stage)
        assertEquals(8, resolution.newStats.carePoints) // 4 + 4 = 8
        assertEquals(PetEmotion.HAPPY, resolution.newStats.emotion)
    }

    @Test
    fun `calculatePeriodsLeft correctly computes remaining periods`() {
        val periods = PetEconomyCalculator.calculatePeriodsLeft(targetCost = 150, savedAmount = 50, avgDeposit = 50)
        assertEquals(2, periods) // (150 - 50) / 50 = 2

        val finished = PetEconomyCalculator.calculatePeriodsLeft(targetCost = 150, savedAmount = 150, avgDeposit = 50)
        assertEquals(0, finished)
    }
}
