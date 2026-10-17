package ru.finpet.kids.feature.calendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.RecurringExpenseEntity
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.feature.calendar.CalendarMonthInfo

/**
 * Сетка календаря текущего месяца (динамическое число дней 28..31 с учетом первого дня недели).
 */
@Composable
fun CalendarMonthGrid(
    monthInfo: CalendarMonthInfo,
    currentDayIndex: Int,
    inspectedDay: Int?,
    notesByDay: Map<Int, List<CalendarNoteEntity>>,
    recurringDayMap: Map<Int, List<RecurringExpenseEntity>>,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val weekDays = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
    val leadingEmpty = monthInfo.firstDayOfWeek - 1 // Дней до 1-го числа (1=ПН -> 0)
    val totalDays = monthInfo.daysInMonth
    val trailingEmpty = (7 - ((leadingEmpty + totalDays) % 7)) % 7
    val totalCells = leadingEmpty + totalDays + trailingEmpty
    val totalWeeks = totalCells / 7

    StardewBoard(
        headerTitle = "${monthInfo.monthName.uppercase()} ${monthInfo.year}",
        headerIcon = "📅",
        modifier = modifier
    ) {
        // 1. Шапка дней недели
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach { dayName ->
                Text(
                    text = dayName,
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (dayName in listOf("СБ", "ВС")) PixelRedBerry else PixelTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        // 2. Сетка недель месяца
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (week in 0 until totalWeeks) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = week * 7 + col
                        val dayNum = cellIndex - leadingEmpty + 1

                        if (dayNum in 1..totalDays) {
                            val isToday = dayNum == currentDayIndex
                            val isSelected = dayNum == inspectedDay
                            val isPast = dayNum < currentDayIndex
                            val hasNotes = notesByDay[dayNum]?.isNotEmpty() == true
                            val hasRecurring = recurringDayMap[dayNum]?.isNotEmpty() == true

                            CalendarDayCell(
                                dayNumber = dayNum,
                                isToday = isToday,
                                isSelected = isSelected,
                                isPast = isPast,
                                hasRecurring = hasRecurring,
                                hasNotes = hasNotes,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.85f),
                                onClick = { onDayClick(dayNum) }
                            )
                        } else {
                            // Пустая ячейка-заполнитель до начала месяца или после конца
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.85f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Легенда значков календаря
        CalendarLegend()
    }
}
