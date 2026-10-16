package ru.finpet.kids.feature.calendar

import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.RecurringExpenseEntity
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

sealed interface CalendarDialog {
    data class DayDetails(val dayIndex: Int) : CalendarDialog
    data class AddNote(val dayIndex: Int) : CalendarDialog
    data object RecurringExpenses : CalendarDialog
    data object AddRecurringExpense : CalendarDialog
    data object BudgetPlanning : CalendarDialog
}

data class CalendarMonthInfo(
    val yearMonth: YearMonth,
    val monthName: String,
    val year: Int,
    val daysInMonth: Int,
    val firstDayOfWeek: Int // 1 = ПН .. 7 = ВС
) {
    companion object {
        fun current(): CalendarMonthInfo = of(YearMonth.now())

        fun of(ym: YearMonth): CalendarMonthInfo {
            val monthTitle = ym.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }
            return CalendarMonthInfo(
                yearMonth = ym,
                monthName = monthTitle,
                year = ym.year,
                daysInMonth = ym.lengthOfMonth(),
                firstDayOfWeek = ym.atDay(1).dayOfWeek.value
            )
        }
    }
}

data class CalendarUiState(
    val currentDayIndex: Int = 1,
    val balance: Int = 50,
    val currentPeriod: PeriodEntity? = null,
    val monthInfo: CalendarMonthInfo = CalendarMonthInfo.current(),
    val notesByDay: Map<Int, List<CalendarNoteEntity>> = emptyMap(),
    val recurringDayMap: Map<Int, List<RecurringExpenseEntity>> = emptyMap(),
    val activeDialog: CalendarDialog? = null
)
