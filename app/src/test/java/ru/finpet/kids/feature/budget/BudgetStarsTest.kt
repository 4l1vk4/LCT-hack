package ru.finpet.kids.feature.budget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetPlan

class BudgetStarsTest {

    @Test
    fun `careStar is earned when mandatory expenses reach or exceed planned amount`() {
        val plan = BudgetPlan(plannedMandatory = 40, plannedOptional = 20, plannedSavings = 40)
        val actual = ActualExpenses(actualMandatory = 40)
        val stars = calculateBudgetStars(plan, actual)
        assertTrue(stars.careStar)
    }

    @Test
    fun `careStar is not earned when mandatory expenses are below planned amount`() {
        val plan = BudgetPlan(plannedMandatory = 50, plannedOptional = 20, plannedSavings = 30)
        val actual = ActualExpenses(actualMandatory = 30)
        val stars = calculateBudgetStars(plan, actual)
        assertFalse(stars.careStar)
    }

    @Test
    fun `careStar is earned if planned was 0 but food was purchased`() {
        val plan = BudgetPlan(plannedMandatory = 0)
        val actual = ActualExpenses(actualMandatory = 30, hasPurchasedFood = true)
        val stars = calculateBudgetStars(plan, actual)
        assertTrue(stars.careStar)
    }

    @Test
    fun `dreamStar is earned when planned savings target is met`() {
        val plan = BudgetPlan(plannedSavings = 50)
        val actual = ActualExpenses(actualSavings = 50)
        val stars = calculateBudgetStars(plan, actual)
        assertTrue(stars.dreamStar)
    }

    @Test
    fun `dreamStar is not earned when planned savings target is missed`() {
        val plan = BudgetPlan(plannedSavings = 50)
        val actual = ActualExpenses(actualSavings = 20)
        val stars = calculateBudgetStars(plan, actual)
        assertFalse(stars.dreamStar)
    }

    @Test
    fun `dreamStar is earned when planned was 0 but child voluntarily saved at least 20 coins`() {
        val plan = BudgetPlan(plannedSavings = 0)
        val actual = ActualExpenses(actualSavings = 20)
        val stars = calculateBudgetStars(plan, actual)
        assertTrue(stars.dreamStar)
    }

    @Test
    fun `wisdomStar is earned when optional expenses stay within limit`() {
        val plan = BudgetPlan(plannedOptional = 30)
        val actual = ActualExpenses(actualOptional = 30)
        val stars = calculateBudgetStars(plan, actual)
        assertTrue(stars.wisdomStar)

        val actualUnder = ActualExpenses(actualOptional = 10)
        assertTrue(calculateBudgetStars(plan, actualUnder).wisdomStar)
    }

    @Test
    fun `wisdomStar is lost when optional expenses exceed planned limit`() {
        val plan = BudgetPlan(plannedOptional = 20)
        val actual = ActualExpenses(actualOptional = 30)
        val stars = calculateBudgetStars(plan, actual)
        assertFalse(stars.wisdomStar)
    }

    @Test
    fun `all 3 stars earned when all conditions are fulfilled`() {
        val plan = BudgetPlan(plannedMandatory = 40, plannedOptional = 20, plannedSavings = 40)
        val actual = ActualExpenses(actualMandatory = 40, actualOptional = 10, actualSavings = 40)
        val stars = calculateBudgetStars(plan, actual)

        assertEquals(3, stars.count)
        assertTrue(stars.isAllEarned)
        assertTrue(stars.careStar)
        assertTrue(stars.dreamStar)
        assertTrue(stars.wisdomStar)
    }

    @Test
    fun `zero stars earned when none of the conditions are met`() {
        val plan = BudgetPlan(plannedMandatory = 50, plannedOptional = 10, plannedSavings = 40)
        // Mandatory not met (0 < 50), optional overspent (50 > 10), savings not met (0 < 40)
        val actual = ActualExpenses(actualMandatory = 0, actualOptional = 50, actualSavings = 0)
        val stars = calculateBudgetStars(plan, actual)

        assertEquals(0, stars.count)
        assertFalse(stars.isAllEarned)
        assertFalse(stars.careStar)
        assertFalse(stars.dreamStar)
        assertFalse(stars.wisdomStar)
    }

    @Test
    fun `budget presets correctly calculate proportions for balance 200`() {
        val balance = 200

        // 🌟 «Умный хозяин» (50% еда, 30% мечта, 20% радости)
        val smartMandatory = ((balance * 0.50f) / 10).toInt() * 10
        val smartSavings = ((balance * 0.30f) / 10).toInt() * 10
        val smartOptional = ((balance * 0.20f) / 10).toInt() * 10

        assertEquals(100, smartMandatory)
        assertEquals(60, smartSavings)
        assertEquals(40, smartOptional)
        assertEquals(balance, smartMandatory + smartSavings + smartOptional)

        // 🎯 «Турбо-копилка» (35% еда, 55% мечта, 10% радости)
        val turboMandatory = ((balance * 0.35f) / 10).toInt() * 10
        val turboSavings = ((balance * 0.55f) / 10).toInt() * 10
        val turboOptional = ((balance * 0.10f) / 10).toInt() * 10

        assertEquals(70, turboMandatory)
        assertEquals(110, turboSavings)
        assertEquals(20, turboOptional)
        assertEquals(balance, turboMandatory + turboSavings + turboOptional)

        // ⚖️ «Поровну» (по 33% в каждую баночку)
        val equalShare = (balance / 30) * 10
        assertEquals(60, equalShare)
        assertTrue(equalShare * 3 <= balance)
    }
}
