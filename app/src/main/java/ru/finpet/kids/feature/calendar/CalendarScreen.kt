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
import androidx.compose.foundation.layout.size
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
import ru.finpet.kids.core.designsystem.ExclamationIcon
import ru.finpet.kids.core.designsystem.LocalBottomBarHeight
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.core.designsystem.WarningIcon
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
    currentDayIndex: Int = profile?.currentPeriodIndex ?: 1,
    monthInfo: CalendarMonthInfo = remember(currentDayIndex) { CalendarMonthInfo.current() },
    onConfirmBudget: (BudgetPlan) -> Unit = {},
    onCompletePeriod: () -> Unit,
    onAdvancePeriod: () -> Unit = onCompletePeriod,
    onAdvanceToNextGrowthStage: () -> Unit = {},
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
    val isConfirmed = currentPeriod?.isBudgetConfirmed == true

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8DAC2)) // Тёплый фермерский фон земли Stardew
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = LocalBottomBarHeight.current + 36.dp)
    ) {


        // --- 2. БЛОК ПЛАНИРОВАНИЯ БЮДЖЕТА / ПЛАН VS ФАКТ (ТЗ 2.5.5) ---
        item(key = "budget_plan_section") {
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
                    actualSavings = currentPeriod?.actualSavings ?: 0,
                    withdrawnSavings = currentPeriod?.withdrawnSavings ?: 0,
                    borrowedFromOptional = currentPeriod?.borrowedFromOptional ?: 0,
                    hasPurchasedFood = (currentPeriod?.actualMandatory ?: 0) >= PetEconomyCalculator.MIN_FOOD_COST,
                    hasPurchasedWater = (currentPeriod?.actualMandatory ?: 0) >= PetEconomyCalculator.MIN_WATER_COST
                )
            }

            if (!isConfirmed) {
                StardewBoard(
                    headerTitle = "ПЛАН БЮДЖЕТА НА ПЕРИОД",
                    headerIcon = ""
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WarningIcon(modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Бюджет на период не составлен!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = PixelTextDark
                                )
                                Text(
                                    text = "Распредели монетки по 3 корзинам до начала покупок. План служит планкой трат: соблюдение плана и пополнение копилки обеспечат 100% точность бюджета!",
                                    fontSize = 16.sp,
                                    color = PixelTextMuted,
                                    lineHeight = 21.sp
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
                            fontSize = 16.sp,
                            leadingIcon = { ExclamationIcon(modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            } else {
                val compliance = remember(plan, actual, profile) {
                    PetEconomyCalculator.calculateCompliance(
                        plan = plan,
                        actual = actual,
                        isPetRunaway = profile?.isPetRunaway == true,
                        petMood = if (profile?.isPetRunaway == true) 0 else (profile?.mood ?: 70),
                        petSatiety = profile?.satiety ?: 100,
                        petHealth = profile?.health ?: 100
                    )
                }
                BudgetPlanVsFactCard(
                    currentDay = currentPeriod?.periodIndex ?: 1,
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

        // 4. кнопки скипа дня и периода (только в демо-моде)
        item(key = "next_day_action") {
            if (demoMode) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PixelButton(
                        text = "⏩ Пропустить день",
                        onClick = onCompletePeriod,
                        containerColor = PixelGreenCrop,
                        textColor = Color.White,
                        borderColor = Color(0xFF1B5E20),
                        modifier = Modifier.weight(1f),
                        fontSize = 15.sp
                    )

                    PixelButton(
                        text = "🗓️ Пропустить период (+200)",
                        onClick = onAdvancePeriod,
                        containerColor = JoyOrange,
                        textColor = Color.White,
                        borderColor = Color(0xFFBF360C),
                        modifier = Modifier.weight(1f),
                        fontSize = 15.sp
                    )
                }

                val currentStage = profile?.growthStage ?: "BABY"
                if (currentStage != "ADULT") {
                    val nextStageText = if (currentStage == "BABY") {
                        "🐾 Пропустить 7 периодов (до «Подростка»)"
                    } else {
                        "👑 Пропустить 11 периодов (до «Взрослого»)"
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    PixelButton(
                        text = nextStageText,
                        onClick = onAdvanceToNextGrowthStage,
                        containerColor = PixelGoldBright,
                        textColor = PixelTextDark,
                        borderColor = PixelGoldDark,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isNextMonday) "🎉 Завтра понедельник: выплата 200 монет карманных денег!"
                else "💡 Карманные деньги (+200 монет) выдаются по понедельникам",
                fontSize = 15.sp,
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
                onAdvancePeriod = {
                    activeDialog = null
                    onAdvancePeriod()
                },
                onAdvanceToNextGrowthStage = {
                    activeDialog = null
                    onAdvanceToNextGrowthStage()
                },
                growthStage = profile?.growthStage ?: "BABY",
                onNavigateToTask = onNavigateToTask,
                onDismiss = { activeDialog = null },
                isPetRunaway = profile?.isPetRunaway == true
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
