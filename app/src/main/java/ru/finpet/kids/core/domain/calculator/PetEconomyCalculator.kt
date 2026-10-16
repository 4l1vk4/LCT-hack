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

/**
 * калькулятор игровой экономики: баланс, статы питомца, план vs факт и переход между днями
 */
object PetEconomyCalculator {

    const val STARTING_BALANCE = 200
    const val WEEKLY_ALLOWANCE = 200
    const val DAILY_INCOME = 0
    const val MIN_FOOD_COST = 30
    const val MIN_WATER_COST = 10

    // проверяем, выпадает ли день на понедельник реального месяца (для выдачи карманных денег)
    fun isMonday(dayIndex: Int, yearMonth: java.time.YearMonth? = null): Boolean {
        return try {
            if (yearMonth != null && dayIndex in 1..yearMonth.lengthOfMonth()) {
                yearMonth.atDay(dayIndex).dayOfWeek == java.time.DayOfWeek.MONDAY
            } else {
                (dayIndex - 1) % 7 == 0
            }
        } catch (_: Exception) {
            (dayIndex - 1) % 7 == 0
        }
    }

    // карманные деньги начисляются по понедельникам: +200 крокетсов на неделю вперед
    fun calculateDayIncome(nextDayIndex: Int, yearMonth: java.time.YearMonth? = null): Int {
        return if (isMonday(nextDayIndex, yearMonth)) WEEKLY_ALLOWANCE else DAILY_INCOME
    }

    // считаем взвешенное настроение (0..100): если сытость или здоровье на дне, настроение тоже резко проседает
    fun calculateEffectiveMood(mood: Int, satiety: Int, health: Int): Int {
        var calculated = (mood * 0.60f + satiety * 0.20f + health * 0.20f)
        if (satiety < 50) {
            calculated -= (50 - satiety) * 0.5f
        }
        if (health < 60) {
            calculated -= (60 - health) * 0.6f
        }
        return calculated.roundToInt().coerceIn(0, 100)
    }

    // считаем сытость: если купили обед — 100%, если только воду — поддерживаем на 60%, иначе падает до 30% и ниже
    fun calculateSatiety(actualExpenses: ActualExpenses, previousSatiety: Int = 100): Int {
        return when {
            actualExpenses.hasPurchasedFood || actualExpenses.actualMandatory >= MIN_FOOD_COST -> 100
            actualExpenses.hasPurchasedWater || actualExpenses.actualMandatory >= MIN_WATER_COST -> {
                if (previousSatiety > 60) 60 else minOf(60, previousSatiety + 15)
            }
            else -> {
                if (previousSatiety > 30) {
                    30
                } else {
                    maxOf(0, previousSatiety - 15)
                }
            }
        }
    }

    // здоровье питомца: если обязательные расходы закрыты и питомец сыт — 100%, иначе постепенно ухудшается
    fun calculateHealth(
        satiety: Int,
        plan: BudgetPlan,
        actualExpenses: ActualExpenses,
        previousHealth: Int = 100
    ): Int {
        val mandatoryCovered = actualExpenses.actualMandatory >= plan.plannedMandatory && satiety >= 60
        return when {
            mandatoryCovered -> 100
            actualExpenses.hasPurchasedMedicine -> 90
            else -> {
                if (previousHealth > 60) {
                    60
                } else if (satiety < 30) {
                    maxOf(0, previousHealth - 20)
                } else {
                    maxOf(20, previousHealth - 10)
                }
            }
        }
    }

    // базовое настроение: растут бонусы за игрушки и копилку, но за перерасход и голод штрафуем
    fun calculateMood(
        satiety: Int,
        plan: BudgetPlan,
        actualExpenses: ActualExpenses,
        currentMood: Int = 60
    ): Int {
        var mood = if (satiety < 50) {
            minOf(currentMood, 60)
        } else {
            maxOf(currentMood, 60)
        }

        // бонус за радости и лакомства
        val optionalBonus = min(35, (actualExpenses.actualOptional / 10) * 10)
        mood += optionalBonus

        // похвала за дисциплину в копилке
        if (actualExpenses.actualSavings >= plan.plannedSavings && plan.plannedSavings > 0) {
            mood += 10
        }

        // штраф за превышение лимита на спонтанные траты
        if (actualExpenses.actualOptional > plan.plannedOptional && plan.plannedOptional > 0) {
            mood -= 20
        }

        // штраф, если забыли покормить
        if (satiety < 50) {
            mood -= 30
        }

        // критический голод роняет настроение еще сильнее
        if (satiety < 30) {
            mood -= (30 - satiety)
        }

        return mood.coerceIn(0, 100)
    }

    // эмоция для отрисовки мордочки питомца в интерфейсе
    fun resolveEmotion(satiety: Int, health: Int, mood: Int): PetEmotion {
        val effective = calculateEffectiveMood(mood, satiety, health)
        return when {
            effective >= 70 -> PetEmotion.HAPPY
            effective >= 40 -> PetEmotion.NEUTRAL
            else -> PetEmotion.SAD
        }
    }

    // считаем точность соблюдения бюджета по 3 корзинам (экономия и перевыполнение накоплений не штрафуются)
    fun calculateCompliance(plan: BudgetPlan, actual: ActualExpenses): BudgetCompliance {
        // обязательные траты: штрафуем за перерасход или если не куплен минимум для питомца
        val mandScore = when {
            plan.plannedMandatory == 0 && actual.actualMandatory == 0 -> 1.0f
            actual.actualMandatory <= plan.plannedMandatory -> {
                // если потратили в пределах плана — проверяем, покормлен ли питомец
                if (actual.hasPurchasedFood || actual.actualMandatory >= MIN_FOOD_COST || plan.plannedMandatory == 0) {
                    1.0f
                } else {
                    // потратили меньше, но еду не купили
                    (actual.actualMandatory.toFloat() / max(plan.plannedMandatory, 1).toFloat()).coerceIn(0.5f, 1.0f)
                }
            }
            else -> {
                // перерасход обязательных трат
                val diff = actual.actualMandatory - plan.plannedMandatory
                max(0.0f, 1.0f - diff.toFloat() / max(plan.plannedMandatory, 30).toFloat())
            }
        }

        // спонтанные траты (хотелки): если сэкономили и потратили меньше плана — отлично (100%), штраф только за перерасход
        val optScore = when {
            plan.plannedOptional == 0 && actual.actualOptional == 0 -> 1.0f
            actual.actualOptional <= plan.plannedOptional -> 1.0f
            else -> {
                val diff = actual.actualOptional - plan.plannedOptional
                max(0.0f, 1.0f - diff.toFloat() / max(plan.plannedOptional, 20).toFloat())
            }
        }

        // накопления: если отложили столько же или БОЛЬШЕ плана — отлично (100%), штраф только если отложили меньше
        val savScore = when {
            plan.plannedSavings == 0 && actual.actualSavings == 0 -> 1.0f
            actual.actualSavings >= plan.plannedSavings -> 1.0f
            else -> {
                if (plan.plannedSavings > 0) {
                    (actual.actualSavings.toFloat() / plan.plannedSavings.toFloat()).coerceIn(0.0f, 1.0f)
                } else 1.0f
            }
        }

        val totalPercent = (((mandScore + optScore + savScore) / 3.0f) * 100.0f).roundToInt().coerceIn(0, 100)
        val isDisciplined = totalPercent >= 75

        val feedback = when {
            totalPercent >= 90 -> "Превосходно! Твой бюджет соблюдён почти идеально, а экономия пошла на пользу!"
            totalPercent >= 75 -> "Отличная работа! Ты уверенно управляешь своими монетками и бережёшь бюджет."
            totalPercent >= 50 -> "Неплохо, но траты немного разошлись с планом. Попробуем ещё раз!"
            else -> "План не совсем сошёлся с покупками. В следующем периоде сначала купи главное и не перерасходуй хотелки!"
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

    // очки заботы за день (0..4 очка), двигают эволюцию питомца
    fun calculateCarePoints(
        satiety: Int,
        health: Int,
        plan: BudgetPlan,
        actual: ActualExpenses,
        compliance: BudgetCompliance
    ): Int {
        var points = 0

        // +2 очка: базовые потребности закрыты (сыт и здоров)
        if (satiety >= 60 && health >= 80) {
            points += 2
        }

        // +1 очко: копилка пополнена по плану или самостоятельный взнос от 20 монет
        val savingsKept = (plan.plannedSavings > 0 && actual.actualSavings >= plan.plannedSavings) ||
                (plan.plannedSavings == 0 && actual.actualSavings >= 20)
        if (savingsKept) {
            points += 1
        }

        // +1 очко: хорошая финансовая дисциплина (от 75%)
        if (compliance.isDisciplined) {
            points += 1
        }

        return points
    }

    // полный расчет итогов дня: пересчитываем статы, проверяем стадию взросления и начисляем доход
    fun resolvePeriod(
        currentStats: PetStats,
        plan: BudgetPlan,
        actual: ActualExpenses,
        nextDayIndex: Int = 1,
        yearMonth: java.time.YearMonth? = null
    ): PeriodResolution {
        val satiety = calculateSatiety(actual, currentStats.satiety)
        val health = calculateHealth(satiety, plan, actual, currentStats.health)
        val rawMood = calculateMood(satiety, plan, actual, currentStats.mood)
        val effectiveMood = calculateEffectiveMood(rawMood, satiety, health)
        val mood = if (effectiveMood <= 0) 0 else rawMood
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

        val periodIncome = calculateDayIncome(nextDayIndex, yearMonth)

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
            summaryFeedback = summaryFeedback,
            plan = plan,
            actual = actual
        )
    }

    // считаем, сколько дней еще копить до цели при текущем темпе
    fun calculatePeriodsLeft(targetCost: Int, savedAmount: Int, avgDeposit: Int): Int {
        val remaining = max(0, targetCost - savedAmount)
        if (remaining == 0) return 0
        val step = max(avgDeposit, 10)
        return ceil(remaining.toDouble() / step.toDouble()).toInt()
    }
}
