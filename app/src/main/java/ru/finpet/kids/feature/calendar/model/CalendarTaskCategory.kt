package ru.finpet.kids.feature.calendar.model

/**
 * Категории задач в календаре питомца.
 * Системные задачи выполняются через реальные игровые действия (Школа, Лавка, общение).
 * Пользовательские заметки отмечаются чекбоксом вручную.
 */
enum class CalendarTaskCategory(
    val id: String,
    val icon: String,
    val defaultBadge: String,
    val isSystem: Boolean
) {
    PET_FOOD("PET_FOOD", "🍲", "Лавка • 30", isSystem = true),
    SCHOOL("SCHOOL", "📚", "Школа", isSystem = true),
    GROCERIES("GROCERIES", "🛒", "Лавка • Сдача", isSystem = true),
    PET_QUESTION("PET_QUESTION", "🐱", "У питомца", isSystem = true),
    BUDGET_PLAN("BUDGET_PLAN", "📊", "План", isSystem = true),
    MANDATORY("MANDATORY", "📝", "Обязательное", isSystem = false),
    OPTIONAL("OPTIONAL", "✨", "Желаемое", isSystem = false),
    SAVINGS("SAVINGS", "🎯", "В копилку", isSystem = false);

    companion object {
        fun fromId(id: String): CalendarTaskCategory =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: MANDATORY
    }
}
