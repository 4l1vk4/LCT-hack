package ru.finpet.kids.feature.budget

import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetPlan

/**
 * модель трех звезд бюджета по ТЗ 2.5.5:
 * ⭐ звезда заботы — обязательные расходы покрыты (еда, вода, здоровье)
 * ⭐ звезда мечты — отложено в копилку на цель не меньше плана
 * ⭐ звезда мудрости — не превышен лимит на спонтанные радости
 */
data class BudgetStars(
    val careStar: Boolean,
    val dreamStar: Boolean,
    val wisdomStar: Boolean
) {
    val count: Int get() = (if (careStar) 1 else 0) + (if (dreamStar) 1 else 0) + (if (wisdomStar) 1 else 0)
    val isAllEarned: Boolean get() = count == 3
}

/**
 * считаем, какие звезды ребенок заслужил по итогам трат дня
 */
fun calculateBudgetStars(plan: BudgetPlan, actual: ActualExpenses): BudgetStars {
    // звезду заботы даем, если куплена еда или потрачено на обязательное по плану
    val careStar = if (plan.plannedMandatory > 0) {
        actual.actualMandatory >= plan.plannedMandatory
    } else {
        actual.actualMandatory >= 30 || actual.hasPurchasedFood
    }

    // звезду мечты даем за дисциплинированное пополнение копилки
    val dreamStar = if (plan.plannedSavings > 0) {
        actual.actualSavings >= plan.plannedSavings
    } else {
        actual.actualSavings >= 20
    }

    // звезду мудрости даем, если удержались в рамках на сладости и игрушки
    val wisdomStar = actual.actualOptional <= plan.plannedOptional

    return BudgetStars(
        careStar = careStar,
        dreamStar = dreamStar,
        wisdomStar = wisdomStar
    )
}
