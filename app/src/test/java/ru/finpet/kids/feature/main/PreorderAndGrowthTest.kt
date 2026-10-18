package ru.finpet.kids.feature.main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.finpet.kids.core.data.repository.PurchaseItem
import ru.finpet.kids.core.domain.model.GrowthStage

class PreorderAndGrowthTest {

    @Test
    fun `preorder filters out parent groceries and calculates totals correctly`() {
        val allItems = listOf(
            PurchaseItem(
                id = "food_basic",
                name = "Сытный обед",
                category = "MANDATORY",
                price = 30,
                satietyBonus = 50,
                moodBonus = 0,
                healthBonus = 10,
                icon = "ic_food_bowl",
                description = "Полезная еда"
            ),
            PurchaseItem(
                id = "groceries_parents",
                name = "Продукты по поручению родителей",
                category = "MANDATORY",
                price = 0,
                satietyBonus = 0,
                moodBonus = 15,
                healthBonus = 0,
                icon = "ic_food_bowl",
                description = "Поручение родителей"
            ),
            PurchaseItem(
                id = "water_fresh",
                name = "Свежая вода",
                category = "MANDATORY",
                price = 10,
                satietyBonus = 15,
                moodBonus = 0,
                healthBonus = 5,
                icon = "ic_water_drop",
                description = "Чистая вода"
            ),
            PurchaseItem(
                id = "toy_ball",
                name = "Яркий мячик",
                category = "OPTIONAL",
                price = 30,
                satietyBonus = 0,
                moodBonus = 25,
                healthBonus = 0,
                icon = "ic_ball",
                description = "Мячик"
            )
        )

        // исключаем поручение родителей из предзакупок
        val eligible = allItems.filter { it.id != "groceries_parents" && it.category != "GROCERIES" }
        assertEquals(3, eligible.size)
        assertFalse(eligible.any { it.id == "groceries_parents" })

        // выбор товаров в предзаказ
        val selectedIds = setOf("food_basic", "toy_ball")
        val selectedItems = eligible.filter { selectedIds.contains(it.id) }
        assertEquals(2, selectedItems.size)

        val totalCost = selectedItems.sumOf { it.price }
        val totalSatiety = selectedItems.sumOf { it.satietyBonus }
        val totalMood = selectedItems.sumOf { it.moodBonus }
        val totalHealth = selectedItems.sumOf { it.healthBonus }

        assertEquals(60, totalCost)
        assertEquals(50, totalSatiety)
        assertEquals(25, totalMood)
        assertEquals(10, totalHealth)

        // расчет остатка баланса
        val initialBalance = 100
        val remaining = initialBalance - totalCost
        assertEquals(40, remaining)
        assertTrue(remaining >= 0)
    }

    @Test
    fun `growth stages progression advances from BABY to TEEN to ADULT`() {
        assertEquals(GrowthStage.BABY, GrowthStage.fromPoints(0))
        assertEquals(GrowthStage.BABY, GrowthStage.fromPoints(27))
        assertEquals(GrowthStage.TEEN, GrowthStage.fromPoints(28))
        assertEquals(GrowthStage.TEEN, GrowthStage.fromPoints(69))
        assertEquals(GrowthStage.ADULT, GrowthStage.fromPoints(70))
        assertEquals(GrowthStage.ADULT, GrowthStage.fromPoints(100))
    }

    @Test
    fun `budget plan comparison reflects selected preorder mandatory and optional sums`() {
        val selectedItems = listOf(
            PurchaseItem(
                id = "food_basic",
                name = "Сытный обед",
                category = "MANDATORY",
                price = 30,
                satietyBonus = 50,
                moodBonus = 0,
                healthBonus = 10,
                icon = "ic_food_bowl",
                description = "Полезная еда"
            ),
            PurchaseItem(
                id = "toy_ball",
                name = "Яркий мячик",
                category = "OPTIONAL",
                price = 25,
                satietyBonus = 0,
                moodBonus = 25,
                healthBonus = 0,
                icon = "ic_ball",
                description = "Мячик"
            )
        )

        val addMandatory = selectedItems.filter { it.category == "MANDATORY" }.sumOf { it.price }
        val addOptional = selectedItems.filter { it.category == "OPTIONAL" }.sumOf { it.price }

        assertEquals(30, addMandatory)
        assertEquals(25, addOptional)

        val actualMandatoryBefore = 10
        val planMandatory = 70
        val actualMandatoryAfter = actualMandatoryBefore + addMandatory
        assertEquals(40, actualMandatoryAfter)
        assertTrue(actualMandatoryAfter <= planMandatory)
    }

    @Test
    fun `formatStatGain displays 0 percent increase if stat is already at maximum or exceeds`() {
        assertEquals("100% → 100% (+0%)", ru.finpet.kids.feature.map.sheets.formatStatGain(100, 65))
        assertEquals("80% → 100% (+20%)", ru.finpet.kids.feature.map.sheets.formatStatGain(80, 50))
        assertEquals("50% → 75% (+25%)", ru.finpet.kids.feature.map.sheets.formatStatGain(50, 25))
    }

    @Test
    fun `plan button is visible in demo mode or when pet is adult for at least 7 days`() {
        fun isPlanVisible(demoMode: Boolean, stage: String, curDay: Int, adultStartDay: Int): Boolean {
            val isAdultPlus7 = stage == "ADULT" && adultStartDay > 0 && (curDay - adultStartDay >= 7)
            return demoMode || isAdultPlus7
        }

        // В демо-режиме кнопка видна всегда
        assertTrue(isPlanVisible(demoMode = true, stage = "BABY", curDay = 1, adultStartDay = 0))
        assertTrue(isPlanVisible(demoMode = true, stage = "TEEN", curDay = 3, adultStartDay = 0))

        // Без демо-режима:
        // 1. Не взрослый кот — кнопки нет
        assertFalse(isPlanVisible(demoMode = false, stage = "BABY", curDay = 10, adultStartDay = 0))
        assertFalse(isPlanVisible(demoMode = false, stage = "TEEN", curDay = 10, adultStartDay = 0))

        // 2. Взрослый кот, но прошло менее 7 дней — кнопки нет
        assertFalse(isPlanVisible(demoMode = false, stage = "ADULT", curDay = 15, adultStartDay = 10)) // 5 дней
        assertFalse(isPlanVisible(demoMode = false, stage = "ADULT", curDay = 16, adultStartDay = 10)) // 6 дней

        // 3. Взрослый кот, прошло ровно 7 дней или больше — кнопка появляется
        assertTrue(isPlanVisible(demoMode = false, stage = "ADULT", curDay = 17, adultStartDay = 10)) // 7 дней
        assertTrue(isPlanVisible(demoMode = false, stage = "ADULT", curDay = 25, adultStartDay = 10)) // 15 дней
    }
}

