package ru.finpet.kids.core.domain.calculator

import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetCompliance
import ru.finpet.kids.core.domain.model.BudgetPlan
import ru.finpet.kids.core.domain.model.GrowthStage
import ru.finpet.kids.core.domain.model.PeriodResolution
import ru.finpet.kids.core.domain.model.PetEmotion
import ru.finpet.kids.core.domain.model.PetStats
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

    // карманные деньги начисляются по понедельникам: +200 монет на неделю вперед
    fun calculateDayIncome(nextDayIndex: Int, yearMonth: java.time.YearMonth? = null, isPeriodSkip: Boolean = false): Int {
        return if (isPeriodSkip || isMonday(nextDayIndex, yearMonth)) WEEKLY_ALLOWANCE else DAILY_INCOME
    }

    // считаем взвешенное настроение (0..100): если сытость или здоровье на дне, настроение тоже резко проседает
    fun calculateEffectiveMood(mood: Int, satiety: Int, health: Int): Int {
        if (mood <= 0) return 0
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

    // базовое настроение: растет за игрушки и копилку, но если экономим на жизни (обязательное/еда), настроение падает
    fun calculateMood(
        satiety: Int,
        plan: BudgetPlan,
        actualExpenses: ActualExpenses,
        currentMood: Int = 60
    ): Int {
        if (currentMood <= 0) return 0
        var mood = if (satiety < 50) {
            minOf(currentMood, 60)
        } else {
            currentMood
        }

        // бонус за радости и лакомства (желаемое)
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

    // считаем точность соблюдения бюджета по 3 корзинам:
    // Главный приоритет — сытость, здоровье и состояние питомца.
    // Накопления поощряются только при сытом питомце, а перерасход и чрезмерные траты снижают точность.
    fun calculateCompliance(
        plan: BudgetPlan,
        actual: ActualExpenses,
        isPetRunaway: Boolean = false,
        hasRecoveredPet: Boolean = false,
        petMood: Int = 70,
        petSatiety: Int = 100,
        petHealth: Int = 100
    ): BudgetCompliance {
        // 1. Проверка качества жизни: сыт и ухожен ли питомец
        val isPetHungry = !hasRecoveredPet && (
            petSatiety < 50 ||
            ((!actual.hasPurchasedFood && actual.actualMandatory < MIN_FOOD_COST) && (actual.totalActual > 0 || petSatiety < 70))
        )
        val isPetCriticallyStarving = !hasRecoveredPet && (
            petSatiety < 30 ||
            (!actual.hasPurchasedFood && actual.actualMandatory == 0 && actual.totalActual > 0)
        )
        val hasAdequateCare = !isPetHungry && (actual.hasPurchasedFood || actual.actualMandatory >= MIN_FOOD_COST || hasRecoveredPet)
        val isDepressedLife = (petMood < 40 || isPetHungry || petHealth < 60) && !isPetRunaway

        // 2. Обязательные траты (Миска жизни):
        val mandScore = when {
            // если питомец вернулся и куплен корм для возвращения (60 монет)
            hasRecoveredPet || (isPetRunaway && actual.actualMandatory >= 60) -> 1.0f
            isPetCriticallyStarving -> 0.0f
            isPetHungry -> {
                val ratio = (actual.actualMandatory.toFloat() / max(plan.plannedMandatory, MIN_FOOD_COST).toFloat()).coerceIn(0f, 1f)
                ratio * 0.15f
            }
            // если план 0 и факт 0: если питомец сыт — 1.0, иначе 0.0 (планировать 0 на еду голодному питомцу нельзя!)
            plan.plannedMandatory == 0 && actual.actualMandatory == 0 -> {
                if (hasAdequateCare) 1.0f else 0.0f
            }
            actual.actualMandatory <= plan.plannedMandatory -> {
                if (isDepressedLife) 0.70f else 1.0f
            }
            else -> {
                // перерасход обязательных трат
                val diff = actual.actualMandatory - plan.plannedMandatory
                max(0.0f, 1.0f - diff.toFloat() / max(plan.plannedMandatory, 30).toFloat())
            }
        }

        // 3. Спонтанные траты (хотелки / Сундучок радости):
        // если сэкономили и потратили меньше плана — отлично (100%), штраф только за перерасход.
        // При голодном питомце траты на хотелки строго наказываются, а экономия не дает 100%.
        val optScore = when {
            isPetCriticallyStarving -> 0.0f
            isPetHungry -> {
                if (actual.actualOptional > 0) 0.0f else 0.20f
            }
            plan.plannedOptional == 0 && actual.actualOptional == 0 -> 1.0f
            actual.actualOptional <= plan.plannedOptional -> 1.0f
            else -> {
                val diff = actual.actualOptional - plan.plannedOptional
                max(0.0f, 1.0f - diff.toFloat() / max(plan.plannedOptional, 20).toFloat())
            }
        }

        // 4. Накопления (Копилка мечты):
        val isExceededSavings = actual.actualSavings > plan.plannedSavings && plan.plannedSavings > 0
        val rawSavScore = when {
            plan.plannedSavings == 0 && actual.actualSavings == 0 -> 1.0f
            actual.actualSavings >= plan.plannedSavings -> 1.0f
            else -> {
                if (plan.plannedSavings > 0) {
                    (actual.actualSavings.toFloat() / plan.plannedSavings.toFloat()).coerceIn(0.0f, 1.0f)
                } else 1.0f
            }
        }

        // ПРИОРИТЕТ СОСТОЯНИЯ ПИТОМЦА НАД НАКОПЛЕНИЯМИ:
        // Если питомец голоден (экономия на жизни), накопления не могут считаться успехом!
        val savScore = when {
            isPetCriticallyStarving && !hasRecoveredPet -> 0.0f
            isPetHungry && !hasRecoveredPet -> rawSavScore * 0.15f
            else -> rawSavScore
        }

        val mandOverspend = if (hasRecoveredPet || isPetRunaway) 0 else max(0, actual.actualMandatory - plan.plannedMandatory)
        val optOverspend = max(0, actual.actualOptional - plan.plannedOptional)
        val totalOverspend = mandOverspend + optOverspend

        val basePercent = (((mandScore + optScore + savScore) / 3.0f) * 100.0f).roundToInt()

        // Штраф за перерасход: перерасход напрямую уменьшает итоговую статистику плана vs факта
        val overspendPenalty = if (totalOverspend > 0) {
            val totalPlanBasis = max(plan.totalPlanned, 30).toFloat()
            ((totalOverspend.toFloat() / totalPlanBasis) * 40.0f).roundToInt().coerceAtLeast(minOf(totalOverspend, 5))
        } else 0

        // Штраф за экономию на жизни (питомец голоден / угнетен):
        val lifeEconomyPenalty = when {
            isPetCriticallyStarving && !hasRecoveredPet -> 15
            isPetHungry && !hasRecoveredPet -> 10
            isDepressedLife && !hasRecoveredPet -> 5
            else -> 0
        }

        // Штраф за снятие монет из прошлых накоплений:
        val pastSavingsPenalty = if (actual.withdrawnSavings > 0) {
            minOf(20, (actual.withdrawnSavings * 0.4f).roundToInt() + 5)
        } else 0

        // Штраф за взятие монет из «Желаемого» на обязательные расходы:
        val borrowedPenalty = if (actual.borrowedFromOptional > 0) {
            minOf(15, (actual.borrowedFromOptional * 0.3f).roundToInt() + 3)
        } else 0

        // Штраф за использование нераспределенных денег из свободного кошелька на покупки:
        val freeMoneyPenalty = if (actual.usedFromFreeMoney > 0) {
            minOf(25, (actual.usedFromFreeMoney * 0.4f).roundToInt() + 5)
        } else 0

        val totalPercent = (basePercent - overspendPenalty - lifeEconomyPenalty - pastSavingsPenalty - borrowedPenalty - freeMoneyPenalty).coerceIn(0, 100)
        val isDisciplined = totalPercent >= 75 && !isPetHungry

        val overspendCause = when {
            mandOverspend > 0 && optOverspend > 0 ->
                "в «Обязательном» (+${mandOverspend} 🪙) и «Желаемом» (+${optOverspend} 🪙)"
            mandOverspend > 0 ->
                "в «Обязательном» («Миска жизни») на ${mandOverspend} 🪙"
            optOverspend > 0 ->
                "в «Желаемом» («Сундучок радости») на ${optOverspend} 🪙"
            else -> ""
        }

        val feedback = when {
            isPetCriticallyStarving && !hasRecoveredPet && actual.actualSavings > 0 ->
                "Питомец сильно голодает! Забота о жизни важнее накоплений: не экономь на питании котика ради копилки, срочно накорми питомца!"
            isPetCriticallyStarving && !hasRecoveredPet ->
                "Питомец сильно голодает! Срочно накорми котика: забота о жизни и здоровье питомца — это главный приоритет бюджета!"
            isPetHungry && !hasRecoveredPet && actual.actualSavings > 0 ->
                "Питомец голоден! Забота о жизни важнее накоплений: не экономь на питании питомца ради копилки, сначала накорми котика!"
            isPetHungry && !hasRecoveredPet ->
                "Питомец остался голодным! Забота о жизни и здоровье питомца — это главный приоритет бюджета."
            isDepressedLife && !hasRecoveredPet ->
                "Настроение питомца ухудшилось из-за жесткой экономии на его жизни! Не стоит урезать обязательные траты во вред здоровью."
            actual.withdrawnSavings > 0 ->
                "Снятие монет из прошлых накоплений (-${actual.withdrawnSavings} 🪙) снизило точность бюджета. Старайся не вскрывать копилку без необходимости!"
            actual.borrowedFromOptional > 0 ->
                "В банке «Обязательное» не хватило монет, и ${actual.borrowedFromOptional} 🪙 было взято из «Желаемого». Это снизило точность бюджета."
            actual.usedFromFreeMoney > 0 ->
                "На покупки потрачено ${actual.usedFromFreeMoney} 🪙 из свободного кошелька вне баночек! Нераспределенные деньги трудно контролировать — планируй траты в баночках заранее."
            isExceededSavings && hasAdequateCare && totalOverspend == 0 ->
                "Великолепно! Ты скопил даже больше, чем планировал, не допустил перерасхода и сохранил питомца сытым и довольным! Отличный баланс!"
            hasRecoveredPet || (isPetRunaway && actual.actualMandatory >= 60) ->
                "Молодец, что правильно выделил монеты и вернул питомца домой!"
            totalOverspend > 0 && actual.actualSavings == 0 ->
                "Перерасход $overspendCause снизил точность бюджета! Ты потратил слишком много денег на хотелки и ничего не отложил в копилку."
            totalOverspend > 0 ->
                "Перерасход $overspendCause снизил точность бюджета! Чтобы поднять точность: держи покупки в рамках баночек или пополняй копилку."
            actual.actualSavings == 0 && plan.plannedSavings > 0 ->
                "Питомец сыт и траты под контролем, но ты не пополнил копилку мечты. Для высокой точности не забывай откладывать на цель!"
            totalPercent >= 90 ->
                "Превосходно! Твой бюджет соблюдён почти идеально: питомец сыт и счастлив, перерасхода нет, а копилка растет!"
            totalPercent >= 75 ->
                "Отличная работа! Ты уверенно держишь баланс: питомец сыт, траты под контролем, а накопления пополняются."
            totalPercent >= 50 ->
                "Неплохо, но траты разошлись с планом. Помни о балансе: сытый питомец, умеренные радости и регулярная копилка."
            else ->
                "Низкая точность бюджета. Сбалансируй траты: сначала еда для питомца, затем умеренные радости и копилка без перерасхода!"
        }

        return BudgetCompliance(
            mandatoryScore = mandScore,
            optionalScore = optScore,
            savingsScore = savScore,
            totalPercent = totalPercent,
            isDisciplined = isDisciplined,
            feedback = feedback,
            overspendAmount = totalOverspend,
            mandatoryOverspend = mandOverspend,
            optionalOverspend = optOverspend,
            isPetHungry = isPetHungry && !hasRecoveredPet,
            withdrawnSavings = actual.withdrawnSavings,
            borrowedFromOptional = actual.borrowedFromOptional
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
        yearMonth: java.time.YearMonth? = null,
        isPetRunaway: Boolean = false,
        hasRecoveredPet: Boolean = false,
        isPeriodSkip: Boolean = false
    ): PeriodResolution {
        val satiety = calculateSatiety(actual, currentStats.satiety)
        val health = calculateHealth(satiety, plan, actual, currentStats.health)
        val rawMood = calculateMood(satiety, plan, actual, currentStats.mood)
        val effectiveMood = calculateEffectiveMood(rawMood, satiety, health)
        val mood = if (effectiveMood <= 0 || (isPetRunaway && !hasRecoveredPet)) 0 else rawMood
        val emotion = resolveEmotion(satiety, health, mood)
        val compliance = calculateCompliance(
            plan = plan,
            actual = actual,
            isPetRunaway = isPetRunaway,
            hasRecoveredPet = hasRecoveredPet,
            petMood = mood,
            petSatiety = satiety,
            petHealth = health
        )

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

        val periodIncome = calculateDayIncome(nextDayIndex, yearMonth, isPeriodSkip)

        val summaryFeedback = buildString {
            append("Итоги дня: получено +$carePointsEarned очков заботы. ")
            if (newStage != currentStats.stage) {
                append("Ура! Питомец вырос до стадии «${newStage.title}»! ")
            }
            if (periodIncome > 0) {
                append("Новый понедельник! Выданы карманные деньги на неделю: +$periodIncome монет.")
            } else {
                append("Наступил новый день. Карманные деньги выдаются раз в неделю по понедельникам.")
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
