package ru.finpet.kids.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.local.entity.RecurringExpenseEntity
import ru.finpet.kids.core.designsystem.LocalBottomBarHeight
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.core.domain.calculator.PetEconomyCalculator
import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetPlan
import ru.finpet.kids.feature.budget.BudgetPlanVsFactCard
import ru.finpet.kids.feature.budget.BudgetPlanningDialog
import ru.finpet.kids.feature.calendar.components.CalendarMonthGrid
import ru.finpet.kids.feature.calendar.dialogs.AddNoteDialog
import ru.finpet.kids.feature.calendar.dialogs.AddRecurringExpenseDialog
import ru.finpet.kids.feature.calendar.dialogs.DayDetailsDialog
import ru.finpet.kids.feature.calendar.dialogs.RecurringExpensesDialog

/**
 * Главный экран календаря расходов питомца.
 * Отображает текущий реальный календарный месяц (с реальным количеством дней и смещением дней недели),
 * сетку заданий, планирование бюджета и переход по дням.
 */
@Composable
fun CalendarScreen(
    profile: ProfileEntity?,
    currentPeriod: PeriodEntity?,
    calendarNotes: List<CalendarNoteEntity>,
    recurringExpenses: List<RecurringExpenseEntity>,
    monthInfo: CalendarMonthInfo = remember(profile?.currentPeriodIndex) { CalendarMonthInfo.current() },
    onConfirmBudget: (BudgetPlan) -> Unit = {},
    onCompletePeriod: () -> Unit,
    onAddNote: (dayIndex: Int, title: String, cost: Int, category: String) -> Unit,
    onDeleteNote: (CalendarNoteEntity) -> Unit,
    onToggleNote: (CalendarNoteEntity) -> Unit,
    onNavigateToTask: ((CalendarNoteEntity) -> Unit)? = null,
    onCompleteChecklistTask: (note: CalendarNoteEntity, rewardCoins: Int) -> Unit = { _, _ -> },
    onAddRecurring: (title: String, cost: Int, frequencyDays: Int, icon: String) -> Unit = { _, _, _, _ -> },
    onDeleteRecurring: (RecurringExpenseEntity) -> Unit = {},
    demoMode: Boolean = false
) {
    val balance = profile?.balance ?: 50
    val currentDayIndex = profile?.currentPeriodIndex ?: 1

    // Единый стейт модальных окон
    var activeDialog by remember { mutableStateOf<CalendarDialog?>(null) }

    // Кеширование распределения повторяющихся расходов по дням
    val recurringDayMap = remember(recurringExpenses, monthInfo.daysInMonth) {
        (1..monthInfo.daysInMonth).associateWith { dayNum ->
            recurringExpenses.filter { expense ->
                if (expense.frequencyDays <= 1) true
                else (dayNum % expense.frequencyDays) == 0
            }
        }
    }

    val notesByDay = remember(calendarNotes) {
        calendarNotes.groupBy { it.dayIndex }
    }

    val nextDayNumber = currentDayIndex + 1
    val isNextMonday = PetEconomyCalculator.isMonday(nextDayNumber, monthInfo.yearMonth)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8DAC2)) // Тёплый фермерский фон земли Stardew
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = LocalBottomBarHeight.current + 36.dp)
    ) {


        // --- 2. БЛОК ПЛАНИРОВАНИЯ БЮДЖЕТА / ПЛАН VS ФАКТ (ТЗ 2.5.5) ---
        item(key = "budget_plan_section") {
            val isConfirmed = currentPeriod?.isBudgetConfirmed == true
            val plan = remember(currentPeriod) {
                BudgetPlan(
                    plannedMandatory = currentPeriod?.plannedMandatory ?: 0,
                    plannedOptional = currentPeriod?.plannedOptional ?: 0,
                    plannedSavings = currentPeriod?.plannedSavings ?: 0
                )
            }
            val actual = remember(currentPeriod) {
                ActualExpenses(
                    actualMandatory = currentPeriod?.actualMandatory ?: 0,
                    actualOptional = currentPeriod?.actualOptional ?: 0,
                    actualSavings = currentPeriod?.actualSavings ?: 0
                )
            }

            if (!isConfirmed) {
                StardewBoard(
                    headerTitle = "ПЛАН БЮДЖЕТА НА ДЕНЬ $currentDayIndex",
                    headerIcon = "📜"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⚠️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Бюджет на сегодня не составлен!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = PixelTextDark
                                )
                                Text(
                                    text = "Распредели доступные монетки по 3 корзинам: на жизнь, радости и мечту до начала покупок.",
                                    fontSize = 15.sp,
                                    color = PixelTextMuted,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        PixelButton(
                            text = "Спланировать бюджет ($balance монет)",
                            onClick = { activeDialog = CalendarDialog.BudgetPlanning },
                            containerColor = PixelGoldBright,
                            textColor = PixelTextDark,
                            borderColor = PixelGoldDark,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                val compliance = remember(plan, actual) {
                    PetEconomyCalculator.calculateCompliance(plan, actual)
                }
                val isConfirmed = currentPeriod?.isBudgetConfirmed == true
                BudgetPlanVsFactCard(
                    currentDay = currentDayIndex,
                    plan = plan,
                    actual = actual,
                    compliance = compliance,
                    onEditPlan = if (isConfirmed) null else { { activeDialog = CalendarDialog.BudgetPlanning } }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // --- 3. СЕТКА КАЛЕНДАРЯ МЕСЯЦА (СМЕЩЕНИЕ ПН-ВС И 28..31 ДЕНЬ) ---
        item(key = "calendar_grid") {
            val inspectedDay = (activeDialog as? CalendarDialog.DayDetails)?.dayIndex
            CalendarMonthGrid(
                monthInfo = monthInfo,
                currentDayIndex = currentDayIndex,
                inspectedDay = inspectedDay,
                notesByDay = notesByDay,
                recurringDayMap = recurringDayMap,
                onDayClick = { day -> activeDialog = CalendarDialog.DayDetails(day) }
            )
        }

        // 4. кнопка скипа периода (только в демо-моде)
        item(key = "next_day_action") {
            if (demoMode) {
                Spacer(modifier = Modifier.height(14.dp))
                val completeButtonText = when {
                    nextDayNumber > monthInfo.daysInMonth -> {
                        "🏆 Завершить месяц (${monthInfo.monthName} • День $currentDayIndex)"
                    }
                    isNextMonday -> {
                        "🌅 Следующий день (День $nextDayNumber • +200 монет!)"
                    }
                    else -> {
                        "🌅 Следующий день (День $nextDayNumber)"
                    }
                }

                PixelButton(
                    text = completeButtonText,
                    onClick = onCompletePeriod,
                    containerColor = PixelGreenCrop,
                    textColor = Color.White,
                    borderColor = Color(0xFF1B5E20),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isNextMonday) "🎉 Завтра понедельник: выплата 200 монет карманных денег!"
                else "💡 Карманные деньги (+200 монет) выдаются по понедельникам",
                fontSize = 14.sp,
                color = PixelTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // --- МОДАЛЬНЫЕ ОКНА И ДИАЛОГИ (УПРАВЛЯЮТСЯ ЧЕРЕЗ ACTIVE DIALOG) ---
    when (val dialog = activeDialog) {
        is CalendarDialog.DayDetails -> {
            val dayNum = dialog.dayIndex
            val selectedDayNotes = notesByDay[dayNum].orEmpty()
            val selectedDayRecurring = recurringDayMap[dayNum].orEmpty()
            val isSelectedToday = dayNum == currentDayIndex

            DayDetailsDialog(
                dayNumber = dayNum,
                isToday = isSelectedToday,
                balance = balance,
                notes = selectedDayNotes,
                recurringExpenses = selectedDayRecurring,
                isNextMonday = isNextMonday,
                demoMode = demoMode,
                onToggleNote = onToggleNote,
                onCompleteChecklistTask = onCompleteChecklistTask,
                onOpenAddNote = { activeDialog = CalendarDialog.AddNote(dayNum) },
                onDeleteNote = onDeleteNote,
                onCompletePeriod = {
                    activeDialog = null
                    onCompletePeriod()
                },
                onNavigateToTask = onNavigateToTask,
                onDismiss = { activeDialog = null }
            )
        }

        is CalendarDialog.AddNote -> {
            val targetDay = dialog.dayIndex
            AddNoteDialog(
                dayIndex = targetDay,
                onDismiss = { activeDialog = CalendarDialog.DayDetails(targetDay) },
                onAddNote = { title, cost, category ->
                    onAddNote(targetDay, title, cost, category)
                    activeDialog = CalendarDialog.DayDetails(targetDay)
                }
            )
        }

        is CalendarDialog.RecurringExpenses -> {
            RecurringExpensesDialog(
                recurringExpenses = recurringExpenses,
                onDismiss = { activeDialog = null },
                onOpenAddRecurring = { activeDialog = CalendarDialog.AddRecurringExpense },
                onDeleteRecurring = onDeleteRecurring
            )
        }

        is CalendarDialog.AddRecurringExpense -> {
            AddRecurringExpenseDialog(
                onDismiss = { activeDialog = CalendarDialog.RecurringExpenses },
                onAddRecurring = { title, cost, freq, icon ->
                    onAddRecurring(title, cost, freq, icon)
                    activeDialog = CalendarDialog.RecurringExpenses
                }
            )
        }

        is CalendarDialog.BudgetPlanning -> {
            val initialPlan = remember(currentPeriod) {
                BudgetPlan(
                    plannedMandatory = currentPeriod?.plannedMandatory ?: 0,
                    plannedOptional = currentPeriod?.plannedOptional ?: 0,
                    plannedSavings = currentPeriod?.plannedSavings ?: 0
                )
            }
            val isConfirmed = currentPeriod?.isBudgetConfirmed == true
            BudgetPlanningDialog(
                balance = balance,
                currentDay = currentDayIndex,
                initialPlan = initialPlan,
                isConfirmed = isConfirmed,
                onConfirm = { plan ->
                    onConfirmBudget(plan)
                    activeDialog = null
                },
                onDismiss = { activeDialog = null }
            )
        }

        null -> {}
    }
}
