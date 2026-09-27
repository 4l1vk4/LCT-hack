package ru.finpet.kids.core.domain.calculator

import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetCompliance
import ru.finpet.kids.core.domain.model.BudgetPlan
import ru.finpet.kids.core.domain.model.GrowthStage
import ru.finpet.kids.core.domain.model.PeriodResolution
import ru.finpet.kids.core.domain.model.PetEmotion
import ru.finpet.kids.core.domain.model.PetStats
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object PetEconomyCalculator {

    const val STARTING_BALANCE = 200
    const val WEEKLY_ALLOWANCE = 200
    const val DAILY_INCOME = 0
    const val MIN_FOOD_COST = 30
    const val MIN_WATER_COST = 10

    /**
     * Понедельники в 28-дневном календаре (4 недели по 7 дней) — это дни 1, 8, 15, 22.
     */
    fun isMonday(dayIndex: Int): Boolean {
        return (dayIndex - 1) % 7 == 0
    }

    /**
     * Расчет начисления карманных денег при переходе на следующий день.
     * Понедельник: +200 монет (на неделю), остальные дни: 0 монет.
     */
    fun calculateDayIncome(nextDayIndex: Int): Int {
        return if (isMonday(nextDayIndex)) WEEKLY_ALLOWANCE else DAILY_INCOME
    }

    /**
     * Рассчитывает сытость питомца (0..100) на основе обязательных расходов на питание.
     * Питомец не может умереть (минимальная сытость 30, если еду не купили).
     */
    fun calculateSatiety(actualExpenses: ActualExpenses): Int {
        return when {
            actualExpenses.hasPurchasedFood || actualExpenses.actualMandatory >= MIN_FOOD_COST -> 100
            actualExpenses.hasPurchasedWater || actualExpenses.actualMandatory >= MIN_WATER_COST -> 60
            else -> 30
        }
    }

    /**
     * Рассчитывает здоровье питомца (0..100).
     */
    fun calculateHealth(
        satiety: Int,
        plan: BudgetPlan,
        actualExpenses: ActualExpenses
    ): Int {
        val mandatoryCovered = actualExpenses.actualMandatory >= plan.plannedMandatory && satiety >= 60
        return when {
            mandatoryCovered -> 100
            actualExpenses.hasPurchasedMedicine -> 90
            else -> 60
        }
    }

    /**
     * Рассчитывает настроение питомца (10..100) с учетом радостей, копилки и перерасхода.
     */
    fun calculateMood(
        satiety: Int,
        plan: BudgetPlan,
        actualExpenses: ActualExpenses
    ): Int {
        var mood = 60

        // Бонус за полезные/приятные покупки
        val optionalBonus = min(35, (actualExpenses.actualOptional / 10) * 10)
        mood += optionalBonus

        // Бонус за дисциплину в накоплениях
        if (actualExpenses.actualSavings >= plan.plannedSavings && plan.plannedSavings > 0) {
            mood += 10
        }

        // Штраф за перерасход необязательного бюджета
        if (actualExpenses.actualOptional > plan.plannedOptional && plan.plannedOptional > 0) {
            mood -= 20
        }

        // Штраф, если питомец голоден
        if (satiety < 50) {
            mood -= 30
        }

        return mood.coerceIn(10, 100)
    }

    /**
     * Определение эмоционального состояния питомца для UI.
     */
    fun resolveEmotion(satiety: Int, health: Int, mood: Int): PetEmotion {
        return when {
            satiety >= 70 && health >= 80 && mood >= 70 -> PetEmotion.HAPPY
            satiety >= 50 && health >= 60 && mood >= 40 -> PetEmotion.NEUTRAL
            else -> PetEmotion.SAD
        }
    }

    /**
     * Расчет соответствия плану бюджета (Plan vs Fact).
     * Защищен от деления на 0 и возврата отрицательных значений.
     */
    fun calculateCompliance(plan: BudgetPlan, actual: ActualExpenses): BudgetCompliance {
        fun categoryScore(p: Int, a: Int): Float {
            return when {
                p == 0 && a == 0 -> 1.0f
                p == 0 -> max(0.0f, 1.0f - (a / 50.0f))
                else -> max(0.0f, 1.0f - (abs(a - p).toFloat() / p.toFloat()))
            }
        }

        val mandScore = categoryScore(plan.plannedMandatory, actual.actualMandatory)
        val optScore = categoryScore(plan.plannedOptional, actual.actualOptional)
        val savScore = categoryScore(plan.plannedSavings, actual.actualSavings)

        val totalPercent = (((mandScore + optScore + savScore) / 3.0f) * 100.0f).roundToInt().coerceIn(0, 100)
        val isDisciplined = totalPercent >= 75

        val feedback = when {
            totalPercent >= 90 -> "Превосходно! Твой бюджет соблюдён почти идеально!"
            totalPercent >= 75 -> "Отличная работа! Ты уверенно управляешь своими монетками."
            totalPercent >= 50 -> "Неплохо, но траты немного разошлись с планом. Попробуем ещё раз!"
            else -> "План не совсем сошёлся с покупками. В следующем периоде сначала купи главное!"
        }

        return BudgetCompliance(
            mandatoryScore = mandScore,
            optionalScore = optScore,
            savingsScore = savScore,
            totalPercent = totalPercent,
            isDisciplined = isDisciplined,
            feedback = feedback
        )
    }

    /**
     * Расчет очков заботы за период (0..4 Care Points).
     */
    fun calculateCarePoints(
        satiety: Int,
        health: Int,
        plan: BudgetPlan,
        actual: ActualExpenses,
        compliance: BudgetCompliance
    ): Int {
        var points = 0

        // +2 очка: обязательные нужды закрыты (сыт и здоров)
        if (satiety >= 60 && health >= 80) {
            points += 2
        }

        // +1 очко: отложено в копилку по плану или самостоятельный взнос >= 20
        val savingsKept = (plan.plannedSavings > 0 && actual.actualSavings >= plan.plannedSavings) ||
                (plan.plannedSavings == 0 && actual.actualSavings >= 20)
        if (savingsKept) {
            points += 1
        }

        // +1 очко: высокая дисциплина бюджета (>= 75%)
        if (compliance.isDisciplined) {
            points += 1
        }

        return points
    }

    /**
     * Полное завершение игрового периода: вычисление всех статов, наград и эволюции.
     */
    fun resolvePeriod(
        currentStats: PetStats,
        plan: BudgetPlan,
        actual: ActualExpenses,
        nextDayIndex: Int = 1
    ): PeriodResolution {
        val satiety = calculateSatiety(actual)
        val health = calculateHealth(satiety, plan, actual)
        val mood = calculateMood(satiety, plan, actual)
        val emotion = resolveEmotion(satiety, health, mood)
        val compliance = calculateCompliance(plan, actual)

        val carePointsEarned = calculateCarePoints(satiety, health, plan, actual, compliance)
        val newTotalCarePoints = currentStats.carePoints + carePointsEarned
        val newStage = GrowthStage.fromPoints(newTotalCarePoints)

        val feedback = when (emotion) {
            PetEmotion.HAPPY -> "Питомец счастлив! Он сыт, весел и гордится твоей заботой."
            PetEmotion.NEUTRAL -> "Всё в порядке, питомец чувствует себя хорошо."
            PetEmotion.SAD -> "Питомцу немного грустно. Давай в следующий раз сначала купим обед и не будем забывать о важном!"
        }

        val newStats = PetStats(
            satiety = satiety,
            health = health,
            mood = mood,
            carePoints = newTotalCarePoints,
            stage = newStage,
            emotion = emotion,
            feedback = feedback
        )

        val periodIncome = calculateDayIncome(nextDayIndex)

        val summaryFeedback = buildString {
            append("Итоги дня: получено +$carePointsEarned очков заботы. ")
            if (newStage != currentStats.stage) {
                append("Ура! Питомец вырос до стадии «${newStage.title}»! ")
            }
            if (periodIncome > 0) {
                append("🌅 Новый понедельник! Выданы карманные деньги на неделю: +$periodIncome монет.")
            } else {
                append("🌅 Наступил новый день. Карманные деньги выдаются раз в неделю по понедельникам.")
            }
        }

        return PeriodResolution(
            newStats = newStats,
            compliance = compliance,
            carePointsEarned = carePointsEarned,
            periodIncome = periodIncome,
            summaryFeedback = summaryFeedback
        )
    }

    /**
     * Расчет оставшихся периодов до достижения цели.
     */
    fun calculatePeriodsLeft(targetCost: Int, savedAmount: Int, avgDeposit: Int): Int {
        val remaining = max(0, targetCost - savedAmount)
        if (remaining == 0) return 0
        val step = max(avgDeposit, 10)
        return ceil(remaining.toDouble() / step.toDouble()).toInt()
    }
}
