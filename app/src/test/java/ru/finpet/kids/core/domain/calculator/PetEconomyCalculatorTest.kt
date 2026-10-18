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
    fun `calculateCompliance rewards economizing and exceeding savings without penalty`() {
        // Запланировали 30 на еду, 30 на хотелки, 40 в копилку.
        // Фактически: еду купили за 30, на хотелки сэкономили и потратили только 10, а в копилку отложили 60 (больше плана!)
        val plan = BudgetPlan(plannedMandatory = 30, plannedOptional = 30, plannedSavings = 40)
        val actual = ActualExpenses(
            actualMandatory = 30,
            actualOptional = 10,
            actualSavings = 60,
            hasPurchasedFood = true
        )

        val compliance = PetEconomyCalculator.calculateCompliance(plan, actual)
        assertEquals(100, compliance.totalPercent)
        assertEquals(1.0f, compliance.mandatoryScore)
        assertEquals(1.0f, compliance.optionalScore)
        assertEquals(1.0f, compliance.savingsScore)
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
            carePoints = 24, // 4 points away from TEEN (28)
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
        assertEquals(28, resolution.newStats.carePoints) // 24 + 4 = 28
        assertEquals(PetEmotion.HAPPY, resolution.newStats.emotion)
    }

    @Test
    fun `calculatePeriodsLeft correctly computes remaining periods`() {
        val periods = PetEconomyCalculator.calculatePeriodsLeft(targetCost = 150, savedAmount = 50, avgDeposit = 50)
        assertEquals(2, periods) // (150 - 50) / 50 = 2

        val finished = PetEconomyCalculator.calculatePeriodsLeft(targetCost = 150, savedAmount = 150, avgDeposit = 50)
        assertEquals(0, finished)
    }

    @Test
    fun `calculateEffectiveMood correctly combines stats and drops smoothly below 26 percent`() {
        assertEquals(100, PetEconomyCalculator.calculateEffectiveMood(mood = 100, satiety = 100, health = 100))
        assertEquals(82, PetEconomyCalculator.calculateEffectiveMood(mood = 70, satiety = 100, health = 100))
        assertEquals(26, PetEconomyCalculator.calculateEffectiveMood(mood = 30, satiety = 30, health = 60))
        
        // Значения ниже 26% при прогрессирующем ухудшении состояния
        val moodLevelLow = PetEconomyCalculator.calculateEffectiveMood(mood = 20, satiety = 20, health = 50)
        assertTrue("Настроение должно опускаться ниже 26%, получили: $moodLevelLow", moodLevelLow < 26)
        assertEquals(5, moodLevelLow)

        val moodLevelCritical = PetEconomyCalculator.calculateEffectiveMood(mood = 10, satiety = 15, health = 45)
        assertEquals(0, moodLevelCritical)

        assertEquals(0, PetEconomyCalculator.calculateEffectiveMood(mood = 0, satiety = 0, health = 0))
    }

    @Test
    fun `calculateSatiety progressively drops below 30 on consecutive missed feedings`() {
        val emptyExpenses = ActualExpenses()
        val day1Satiety = PetEconomyCalculator.calculateSatiety(emptyExpenses, previousSatiety = 100)
        assertEquals(30, day1Satiety)

        val day2Satiety = PetEconomyCalculator.calculateSatiety(emptyExpenses, previousSatiety = day1Satiety)
        assertEquals(15, day2Satiety)

        val day3Satiety = PetEconomyCalculator.calculateSatiety(emptyExpenses, previousSatiety = day2Satiety)
        assertEquals(0, day3Satiety)
    }

    @Test
    fun `consecutive unfed periods drop effective mood below 26 percent down to zero`() {
        val initialStats = PetStats(
            satiety = 100,
            health = 100,
            mood = 70,
            carePoints = 2,
            stage = GrowthStage.BABY,
            emotion = PetEmotion.HAPPY,
            feedback = ""
        )
        val plan = BudgetPlan(plannedMandatory = 30, plannedOptional = 10, plannedSavings = 20)
        val noExpenses = ActualExpenses()

        // День 1 без еды: настроение опускается до 26%
        val day1 = PetEconomyCalculator.resolvePeriod(initialStats, plan, noExpenses)
        val day1Effective = PetEconomyCalculator.calculateEffectiveMood(day1.newStats.mood, day1.newStats.satiety, day1.newStats.health)
        assertEquals(26, day1Effective)
        assertEquals(PetEmotion.SAD, day1.newStats.emotion)

        // День 2 без еды: состояние ухудшается дальше, настроение падает ниже 26% до 0%!
        val day2 = PetEconomyCalculator.resolvePeriod(day1.newStats, plan, noExpenses)
        val day2Effective = PetEconomyCalculator.calculateEffectiveMood(day2.newStats.mood, day2.newStats.satiety, day2.newStats.health)
        assertTrue("На 2-й день без еды настроение должно опуститься ниже 26%", day2Effective < 26)
        assertEquals(0, day2Effective)
        assertEquals(0, day2.newStats.mood)
        assertEquals(PetEmotion.SAD, day2.newStats.emotion)
    }

    @Test
    fun `calculateCompliance penalizes overspending and reduces totalPercent below base average`() {
        val plan = BudgetPlan(plannedMandatory = 30, plannedOptional = 20, plannedSavings = 50)
        // Overspend optional by 40 coins (60 vs 20)
        val actual = ActualExpenses(
            actualMandatory = 30,
            actualOptional = 60,
            actualSavings = 50,
            hasPurchasedFood = true
        )

        val compliance = PetEconomyCalculator.calculateCompliance(plan, actual)
        assertEquals(40, compliance.overspendAmount)
        // mandScore = 1.0, optScore = 0.0, savScore = 1.0 -> base = 67%
        // overspend penalty: (40 / 100) * 40 = 16 penalty points -> totalPercent = 67 - 16 = 51%
        assertTrue("Итоговый процент должен быть меньше базового 67% из-за штрафа за перерасход", compliance.totalPercent < 67)
        assertEquals(51, compliance.totalPercent)
        assertEquals(40, compliance.optionalOverspend)
        assertEquals(0, compliance.mandatoryOverspend)
        assertTrue(compliance.feedback.contains("Перерасход в «Желаемом»"))
        assertTrue(compliance.feedback.contains("40 🪙"))
    }

    @Test
    fun `calculateCompliance tracks mandatoryOverspend when taking from optional`() {
        val plan = BudgetPlan(plannedMandatory = 30, plannedOptional = 50, plannedSavings = 20)
        // Mandatory exceeded by 20 coins (50 vs 30)
        val actual = ActualExpenses(
            actualMandatory = 50,
            actualOptional = 20,
            actualSavings = 20,
            hasPurchasedFood = true
        )

        val compliance = PetEconomyCalculator.calculateCompliance(plan, actual)
        assertEquals(20, compliance.mandatoryOverspend)
        assertEquals(0, compliance.optionalOverspend)
        assertEquals(20, compliance.overspendAmount)
        assertTrue(compliance.feedback.contains("Перерасход в «Обязательном»"))
        assertTrue(compliance.feedback.contains("20 🪙"))
    }

    @Test
    fun `calculateCompliance does not penalize mandatory spending when recovering pet`() {
        val plan = BudgetPlan(plannedMandatory = 30, plannedOptional = 20, plannedSavings = 50)
        // 60 coins spent on recovery bowl
        val actual = ActualExpenses(
            actualMandatory = 60,
            actualOptional = 20,
            actualSavings = 50,
            hasPurchasedFood = true
        )

        val compliance = PetEconomyCalculator.calculateCompliance(
            plan = plan,
            actual = actual,
            isPetRunaway = true,
            hasRecoveredPet = true
        )
        assertEquals(0, compliance.overspendAmount)
        assertEquals(100, compliance.totalPercent)
        assertTrue(compliance.isDisciplined)
    }

    @Test
    fun `calculateCompliance drops accuracy when pet is hungry even if all money was saved`() {
        val plan = BudgetPlan(plannedMandatory = 30, plannedOptional = 20, plannedSavings = 50)
        // Pet is hungry: 0 spent on food, but all 50 coins were deposited to savings!
        val actual = ActualExpenses(
            actualMandatory = 0,
            actualOptional = 0,
            actualSavings = 50,
            hasPurchasedFood = false
        )

        val compliance = PetEconomyCalculator.calculateCompliance(plan, actual)
        // Ранее давало 80%! Теперь должно быть строго ниже 35% и дисциплина false
        assertTrue("Точность при голодном питомце должна быть низкой (получили ${compliance.totalPercent}%)", compliance.totalPercent <= 35)
        assertFalse("При голодном питомце бюджет не может быть дисциплинированным", compliance.isDisciplined)
        assertTrue(compliance.isPetHungry)
        assertTrue(compliance.feedback.contains("Питомец голоден") || compliance.feedback.contains("важнее"))
    }

    @Test
    fun `calculateCompliance drops accuracy when overspending excessively and not saving`() {
        val plan = BudgetPlan(plannedMandatory = 30, plannedOptional = 20, plannedSavings = 50)
        // Mandatory covered (30), but optional wasted by 70 coins (90 vs 20), and 0 saved!
        val actual = ActualExpenses(
            actualMandatory = 30,
            actualOptional = 90,
            actualSavings = 0,
            hasPurchasedFood = true
        )

        val compliance = PetEconomyCalculator.calculateCompliance(plan, actual)
        assertTrue("Чрезмерные траты и отсутствие накоплений должны ронять точность (получили ${compliance.totalPercent}%)", compliance.totalPercent <= 25)
        assertFalse(compliance.isDisciplined)
        assertTrue(compliance.feedback.contains("Перерасход"))
    }

    @Test
    fun `calculateCompliance penalizes withdrawing past savings`() {
        val plan = BudgetPlan(plannedMandatory = 30, plannedOptional = 20, plannedSavings = 50)
        // Питомец накормлен, но снято 25 монет из прошлых накоплений
        val actual = ActualExpenses(
            actualMandatory = 30,
            actualOptional = 20,
            actualSavings = 50,
            withdrawnSavings = 25,
            hasPurchasedFood = true
        )

        val compliance = PetEconomyCalculator.calculateCompliance(plan, actual)
        assertTrue("Снятие прошлых накоплений должно снижать точность", compliance.totalPercent < 100)
        assertEquals(25, compliance.withdrawnSavings)
        assertTrue(compliance.feedback.contains("прошлых накоплений"))
    }

    @Test
    fun `calculateCompliance records borrowing from optional jar to cover mandatory expenses`() {
        val plan = BudgetPlan(plannedMandatory = 30, plannedOptional = 50, plannedSavings = 20)
        // В банке обязательного не хватило 20 монет, взято из желаемого
        val actual = ActualExpenses(
            actualMandatory = 30,
            actualOptional = 20,
            actualSavings = 20,
            borrowedFromOptional = 20,
            hasPurchasedFood = true
        )

        val compliance = PetEconomyCalculator.calculateCompliance(plan, actual)
        assertEquals(20, compliance.borrowedFromOptional)
        assertTrue("Перераспределение из желаемого должно слегка снижать точность", compliance.totalPercent < 100)
        assertTrue(compliance.feedback.contains("«Желаемого»"))
    }

    @Test
    fun `calculateCompliance detects hunger when petSatiety is low even if mandatory expenses were incurred`() {
        val plan = BudgetPlan(plannedMandatory = 30, plannedOptional = 20, plannedSavings = 50)
        // 30 потрачено на обязательное, но текущая сытость кота упала до 25 (кот голодает)
        val actual = ActualExpenses(
            actualMandatory = 30,
            actualOptional = 10,
            actualSavings = 30,
            hasPurchasedFood = true
        )

        val compliance = PetEconomyCalculator.calculateCompliance(
            plan = plan,
            actual = actual,
            petSatiety = 25
        )
        assertTrue("Кот должен распознаваться как голодный", compliance.isPetHungry)
        assertFalse("При голодном коте дисциплина не может быть true", compliance.isDisciplined)
        assertTrue("Точность бюджета должна быть строго ниже 30%", compliance.totalPercent <= 30)
        assertTrue(compliance.feedback.contains("голод"))
    }
}

