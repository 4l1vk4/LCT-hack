package ru.finpet.kids.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.local.entity.RecurringExpenseEntity
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.PixelBlueWater
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGoldGlow
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentDark
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.SkyBlue
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.core.domain.calculator.PetEconomyCalculator
import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetPlan
import ru.finpet.kids.feature.budget.BudgetPlanVsFactCard
import ru.finpet.kids.feature.budget.BudgetPlanningDialog
import ru.finpet.kids.feature.budget.DaySummaryBudgetSection

@Composable
fun CalendarScreen(
    profile: ProfileEntity?,
    currentPeriod: PeriodEntity?,
    calendarNotes: List<CalendarNoteEntity>,
    recurringExpenses: List<RecurringExpenseEntity>,
    onConfirmBudget: (BudgetPlan) -> Unit = {},
    onCompletePeriod: () -> Unit,
    onAddNote: (dayIndex: Int, title: String, cost: Int, category: String) -> Unit,
    onDeleteNote: (CalendarNoteEntity) -> Unit,
    onToggleNote: (CalendarNoteEntity) -> Unit,
    onCompleteChecklistTask: (note: CalendarNoteEntity, rewardCoins: Int) -> Unit = { _, _ -> },
    onAddRecurring: (title: String, cost: Int, frequencyDays: Int, icon: String) -> Unit = { _, _, _, _ -> },
    onDeleteRecurring: (RecurringExpenseEntity) -> Unit = {}
) {
    val balance = profile?.balance ?: 50
    val currentDayIndex = profile?.currentPeriodIndex ?: 1

    // Экран календаря чистый: детальная информация открывается ТОЛЬКО по нажатию на день
    var inspectedDay by remember { mutableStateOf<Int?>(null) }
    var showRecurringDialog by remember { mutableStateOf(false) }
    var showAddNoteDialogForDay by remember { mutableStateOf<Int?>(null) }
    var showAddRecurringDialog by remember { mutableStateOf(false) }
    var showNextDayConfirmDialog by remember { mutableStateOf(false) }
    var showBudgetPlanningDialog by remember { mutableStateOf(false) }

    // Оптимизация: кешируем распределение постоянных трат и заметок по дням
    val recurringDayMap = remember(recurringExpenses) {
        (1..28).associateWith { dayNum ->
            recurringExpenses.filter { expense ->
                if (expense.frequencyDays <= 1) true
                else (dayNum % expense.frequencyDays) == 0
            }
        }
    }

    val notesByDay = remember(calendarNotes) {
        calendarNotes.groupBy { it.dayIndex }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8DAC2)) // Тёплый фермерский фон земли Stardew
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 86.dp)
    ) {
        // --- 1. ШАПКА КАЛЕНДАРЯ ---
        item(key = "calendar_header") {
            StardewBoard(
                headerTitle = "КАЛЕНДАРЬ РАСХОДОВ",
                headerIcon = "📅"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ДЕНЬ $currentDayIndex ИЗ 28",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = PixelTextDark
                        )
                        Text(
                            text = "Внутриигровой день Финни",
                            fontSize = 12.sp,
                            color = PixelTextMuted
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PixelWoodDark)
                            .padding(1.dp)
                            .border(1.dp, PixelGoldBright, RoundedCornerShape(3.dp))
                            .background(PixelGoldGlow)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CoinIcon(modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "$balance МОНЕТ",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = PixelTextDark,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // --- 1.1. БЛОК ПЛАНИРОВАНИЯ БЮДЖЕТА / ПЛАН VS ФАКТ (ТЗ 2.5.5) ---
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
                                    fontSize = 13.sp,
                                    color = PixelTextDark
                                )
                                Text(
                                    text = "Распредели доступные монетки по 3 корзинам: на жизнь, радости и мечту до начала покупок.",
                                    fontSize = 11.sp,
                                    color = PixelTextMuted,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        PixelButton(
                            text = "Спланировать бюджет ($balance монет)",
                            onClick = { showBudgetPlanningDialog = true },
                            containerColor = PixelGoldBright,
                            textColor = PixelTextDark,
                            borderColor = PixelGoldDark,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                val compliance = remember(plan, actual) {
                    PetEconomyCalculator.calculateCompliance(plan, actual)
                }
                BudgetPlanVsFactCard(
                    currentDay = currentDayIndex,
                    plan = plan,
                    actual = actual,
                    compliance = compliance,
                    onEditPlan = { showBudgetPlanningDialog = true }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // --- 2. СЕТКА КАЛЕНДАРЯ 4 НЕДЕЛИ X 7 ДНЕЙ (28 ДНЕЙ) ---
        item(key = "calendar_grid") {
            StardewBoard(
                headerTitle = "РАСПИСАНИЕ МЕСЯЦА (ПН-ВС)",
                headerIcon = "🗓️"
            ) {
                // Шапка дней недели
                val weekDays = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekDays.forEach { dayName ->
                        Text(
                            text = dayName,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (dayName in listOf("СБ", "ВС")) PixelRedBerry else PixelTextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Сетка 4 недели (28 дней)
                for (week in 0 until 4) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (dayOfWeek in 1..7) {
                            val dayNum = week * 7 + dayOfWeek
                            val isToday = dayNum == currentDayIndex
                            val isSelected = dayNum == inspectedDay
                            val isPast = dayNum < currentDayIndex

                            val hasNotes = notesByDay.containsKey(dayNum)

                            StardewDayCell(
                                dayNumber = dayNum,
                                isToday = isToday,
                                isSelected = isSelected,
                                isPast = isPast,
                                hasRecurring = false,
                                hasNotes = hasNotes,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.85f),
                                onClick = { inspectedDay = dayNum }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Легенда значков календаря
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(PixelParchmentMedium)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🐾", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Сегодня", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = PixelTextDark)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📋", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Чек-лист дня", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = PixelTextDark)
                    }
                }
            }
        }

        // --- 3. КНОПКА «СЛЕДУЮЩИЙ ДЕНЬ» ПОД КАЛЕНДАРЕМ ---
        item(key = "next_day_action") {
            Spacer(modifier = Modifier.height(14.dp))
            val nextDayNumber = currentDayIndex + 1
            val isNextMonday = (nextDayNumber - 1) % 7 == 0
            val completeButtonText = if (isNextMonday) {
                "🌅 Следующий день (День $nextDayNumber • +200 🪙!)"
            } else {
                "🌅 Следующий день (День $nextDayNumber)"
            }

            PixelButton(
                text = completeButtonText,
                onClick = { showNextDayConfirmDialog = true },
                containerColor = PixelGreenCrop,
                textColor = Color.White,
                borderColor = Color(0xFF1B5E20),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isNextMonday) "🎉 Завтра понедельник: выплата 200 монет карманных денег!"
                else "💡 Карманные деньги (+200 монет) выдаются по понедельникам",
                fontSize = 11.sp,
                color = PixelTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // --- МОДАЛЬНАЯ ДОСКА ВЫБРАННОГО ДНЯ (ОТКРЫВАЕТСЯ ТОЛЬКО ПО ТАПУ НА ДЕНЬ) ---
    inspectedDay?.let { dayNum ->
        val selectedDayNotes = notesByDay[dayNum].orEmpty()
        val selectedDayRecurring = recurringDayMap[dayNum].orEmpty()
        val isSelectedToday = dayNum == currentDayIndex

        DayDetailsDialog(
            dayNumber = dayNum,
            isToday = isSelectedToday,
            balance = balance,
            notes = selectedDayNotes,
            onToggleNote = onToggleNote,
            onCompleteChecklistTask = onCompleteChecklistTask,
            onOpenAddNote = { showAddNoteDialogForDay = dayNum },
            onDeleteNote = onDeleteNote,
            onCompletePeriod = {
                inspectedDay = null
                showNextDayConfirmDialog = true
            },
            onDismiss = { inspectedDay = null }
        )
    }

    // --- ДИАЛОГ УПРАВЛЕНИЯ ПОСТОЯННЫМИ РАСХОДАМИ ---
    if (showRecurringDialog) {
        RecurringExpensesDialog(
            recurringExpenses = recurringExpenses,
            onDismiss = { showRecurringDialog = false },
            onOpenAddRecurring = { showAddRecurringDialog = true },
            onDeleteRecurring = onDeleteRecurring
        )
    }

    // --- ДИАЛОГ ДОБАВЛЕНИЯ ЗАМЕТКИ НА ВЫБРАННЫЙ ДЕНЬ ---
    showAddNoteDialogForDay?.let { targetDay ->
        AddNoteDialog(
            dayIndex = targetDay,
            onDismiss = { showAddNoteDialogForDay = null },
            onAddNote = { title, cost, category ->
                onAddNote(targetDay, title, cost, category)
                showAddNoteDialogForDay = null
            }
        )
    }

    // --- ДИАЛОГ ДОБАВЛЕНИЯ ЦИКЛИЧНОЙ ТРАТЫ ---
    if (showAddRecurringDialog) {
        AddRecurringExpenseDialog(
            onDismiss = { showAddRecurringDialog = false },
            onAddRecurring = { title, cost, freq, icon ->
                onAddRecurring(title, cost, freq, icon)
                showAddRecurringDialog = false
            }
        )
    }

    // --- ДИАЛОГ ПЛАНИРОВАНИЯ БЮДЖЕТА (ТЗ 2.5.5) ---
    if (showBudgetPlanningDialog) {
        val initialPlan = remember(currentPeriod) {
            BudgetPlan(
                plannedMandatory = currentPeriod?.plannedMandatory ?: 0,
                plannedOptional = currentPeriod?.plannedOptional ?: 0,
                plannedSavings = currentPeriod?.plannedSavings ?: 0
            )
        }
        BudgetPlanningDialog(
            balance = balance,
            currentDay = currentDayIndex,
            initialPlan = initialPlan,
            onConfirm = { plan ->
                onConfirmBudget(plan)
                showBudgetPlanningDialog = false
            },
            onDismiss = { showBudgetPlanningDialog = false }
        )
    }

    // --- ДИАЛОГ ПОДТВЕРЖДЕНИЯ ПЕРЕХОДА НА СЛЕДУЮЩИЙ ДЕНЬ ---
    if (showNextDayConfirmDialog) {
        val nextDayNumber = currentDayIndex + 1
        NextDayConfirmDialog(
            currentDay = currentDayIndex,
            nextDay = nextDayNumber,
            currentPeriod = currentPeriod,
            onConfirm = {
                showNextDayConfirmDialog = false
                onCompletePeriod()
            },
            onDismiss = {
                showNextDayConfirmDialog = false
            }
        )
    }
}

/**
 * Модальная доска деталей выбранного дня (Stardew Valley Day Inspector)
 */
/**
 * Модальная доска чек-листа выбранного дня (Daily Checklist)
 */
@Composable
fun DayDetailsDialog(
    dayNumber: Int,
    isToday: Boolean,
    balance: Int,
    notes: List<CalendarNoteEntity>,
    onToggleNote: (CalendarNoteEntity) -> Unit,
    onCompleteChecklistTask: (note: CalendarNoteEntity, rewardCoins: Int) -> Unit,
    onOpenAddNote: () -> Unit,
    onDeleteNote: (CalendarNoteEntity) -> Unit,
    onCompletePeriod: () -> Unit,
    onDismiss: () -> Unit
) {
    val completedCount = notes.count { it.isCompleted }
    val totalCount = notes.size

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            StardewBoard(
                headerTitle = "ЧЕК-ЛИСТ ДНЯ $dayNumber" + if (isToday) " • СЕГОДНЯ" else "",
                headerIcon = if (isToday) "🐾" else "📋"
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    // 1. Статус-строка дня
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isToday) PixelGoldGlow else PixelParchmentMedium)
                                .border(1.dp, if (isToday) PixelGoldBright else PixelParchmentBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isToday) "🌟 Сегодняшние задачи" else "🗓️ Задачи на День $dayNumber",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToday) PixelGoldDark else PixelTextDark
                                )
                                Text(
                                    text = "Выполнено: $completedCount из $totalCount",
                                    fontSize = 11.sp,
                                    color = PixelTextMuted
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CoinIcon(modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$balance",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = PixelTextDark,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        // Шкала прогресса выполнения чек-листа дня
                        LinearProgressIndicator(
                            progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f },
                            color = PixelGreenCrop,
                            trackColor = PixelParchmentBorder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }

                    // 2. Список задач чек-листа
                    if (notes.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PixelParchmentMedium)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "На этот день нет задач. Нажмите кнопку ниже, чтобы добавить свою заметку!",
                                    fontSize = 12.sp,
                                    color = PixelTextMuted
                                )
                            }
                        }
                    } else {
                        items(notes, key = { it.id }) { note ->
                            val isSystemTask = note.category in listOf("PET_FOOD", "SCHOOL", "GROCERIES", "PET_QUESTION")
                            val taskIcon = when (note.category) {
                                "PET_FOOD" -> "🍲"
                                "SCHOOL" -> "📚"
                                "GROCERIES" -> "🛒"
                                "PET_QUESTION" -> "🐱"
                                else -> "📝"
                            }

                            val taskBadge = when (note.category) {
                                "PET_FOOD" -> "Лавка • 30 🪙"
                                "SCHOOL" -> "Школа"
                                "GROCERIES" -> if (note.isCompleted && note.cost > 0) "+${note.cost} 🪙" else "Лавка • Сдача"
                                "PET_QUESTION" -> "У питомца"
                                else -> if (note.cost > 0) "${note.cost} 🪙" else "Заметка"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (note.isCompleted) Color(0xFFF1F8E9) else PixelParchmentMedium)
                                    .border(
                                        1.dp,
                                        if (note.isCompleted) Color(0xFFAED581) else PixelParchmentBorder,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Чекбокс: системные задачи выполняются только через реальные действия в игре (Лавка, Школа, общение)
                                    Checkbox(
                                        checked = note.isCompleted,
                                        onCheckedChange = if (isSystemTask) null else { { onToggleNote(note) } },
                                        enabled = !isSystemTask,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = PixelGreenCrop,
                                            uncheckedColor = PixelTextMuted,
                                            disabledCheckedColor = PixelGreenCrop,
                                            disabledUncheckedColor = PixelParchmentBorder
                                        ),
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = taskIcon, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Только название задания, без описания (по запросу)
                                    Text(
                                        text = note.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (note.isCompleted) PixelTextMuted else PixelTextDark,
                                        textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Статус-бейдж
                                if (note.isCompleted) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFDCEDC8))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (note.category == "GROCERIES" && note.cost > 0) "✅ Сдача +${note.cost} 🪙" else "✅ Готово",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PixelGreenCrop
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PixelWoodDark)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = taskBadge,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PixelGoldBright
                                        )
                                    }
                                }

                                // Кнопка удаления только для пользовательских заметок
                                if (!isSystemTask) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "✕",
                                        fontWeight = FontWeight.Bold,
                                        color = PixelRedBerry,
                                        fontSize = 14.sp,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable { onDeleteNote(note) }
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Кнопка добавления своей заметки
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        PixelButton(
                            text = "➕ Добавить свою заметку",
                            onClick = onOpenAddNote,
                            containerColor = PixelGoldBright,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 4. Завершение дня (только если день — СЕГОДНЯ)
                    if (isToday) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            val nextDayNumber = dayNumber + 1
                            val isNextMonday = (nextDayNumber - 1) % 7 == 0
                            val completeButtonText = if (isNextMonday) {
                                "🌅 Завершить день (+200 монет на неделю!)"
                            } else {
                                "🌅 Завершить день"
                            }

                            PixelButton(
                                text = completeButtonText,
                                onClick = onCompletePeriod,
                                containerColor = PixelGreenCrop,
                                textColor = Color.White,
                                borderColor = Color(0xFF1B5E20),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // 5. Кнопка закрытия
                    item {
                        PixelButton(
                            text = "✕ Закрыть",
                            onClick = onDismiss,
                            containerColor = PixelWoodMedium,
                            textColor = PixelParchmentLight,
                            borderColor = PixelWoodDark,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Диалог управления постоянными (цикличными) расходами питомца
 */
@Composable
fun RecurringExpensesDialog(
    recurringExpenses: List<RecurringExpenseEntity>,
    onDismiss: () -> Unit,
    onOpenAddRecurring: () -> Unit,
    onDeleteRecurring: (RecurringExpenseEntity) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.78f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            StardewBoard(
                headerTitle = "ПОСТОЯННЫЕ РАСХОДЫ",
                headerIcon = "🔄"
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 6.dp)
                ) {
                    item {
                        Text(
                            text = "Расходы, повторяющиеся регулярно в календаре питомца:",
                            fontSize = 12.sp,
                            color = PixelTextMuted
                        )
                    }

                    items(recurringExpenses, key = { it.id }) { expense ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(PixelParchmentMedium)
                                .border(1.dp, PixelParchmentBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = expense.icon, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = expense.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PixelTextDark
                                    )
                                    Text(
                                        text = when (expense.frequencyDays) {
                                            1 -> "Каждый день"
                                            3 -> "Раз в 3 дня"
                                            7 -> "Раз в неделю"
                                            else -> "Каждые ${expense.frequencyDays} дн."
                                        },
                                        fontSize = 10.sp,
                                        color = PixelTextMuted
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CoinIcon(modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${expense.cost}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PixelGoldDark
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "✕",
                                    fontWeight = FontWeight.Bold,
                                    color = PixelRedBerry,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { onDeleteRecurring(expense) }
                                        .padding(4.dp)
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        PixelButton(
                            text = "➕ Добавить цикличную трату",
                            onClick = onOpenAddRecurring,
                            containerColor = PixelGoldBright,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        PixelButton(
                            text = "Закрыть",
                            onClick = onDismiss,
                            containerColor = PixelParchmentDark,
                            textColor = PixelTextDark,
                            borderColor = PixelParchmentBorder,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Диалог создания заметки на выбранный день
 */
@Composable
fun AddNoteDialog(
    dayIndex: Int,
    onDismiss: () -> Unit,
    onAddNote: (title: String, cost: Int, category: String) -> Unit
) {
    var noteTitle by remember { mutableStateOf("") }
    var noteCost by remember { mutableStateOf("30") }
    var selectedCategory by remember { mutableStateOf("MANDATORY") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📝 Заметка на День $dayIndex",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    label = { Text("Что планируем купить?") },
                    placeholder = { Text("Например: Корм или Игрушка") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = noteCost,
                    onValueChange = { noteCost = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Ожидаемая сумма (монет)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Категория расходов:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedCategory == "MANDATORY",
                        onClick = { selectedCategory = "MANDATORY" },
                        colors = RadioButtonDefaults.colors(selectedColor = PixelGreenCrop)
                    )
                    Text("Обязательное", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    RadioButton(
                        selected = selectedCategory == "OPTIONAL",
                        onClick = { selectedCategory = "OPTIONAL" },
                        colors = RadioButtonDefaults.colors(selectedColor = PixelBlueWater)
                    )
                    Text("Желаемое", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = noteCost.toIntOrNull() ?: 0
                    if (noteTitle.isNotBlank()) {
                        onAddNote(noteTitle.trim(), cost, selectedCategory)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PixelGoldBright)
            ) {
                Text("Добавить", color = PixelTextDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = PixelTextMuted)
            }
        }
    )
}

/**
 * Диалог создания новой цикличной траты
 */
@Composable
fun AddRecurringExpenseDialog(
    onDismiss: () -> Unit,
    onAddRecurring: (title: String, cost: Int, frequency: Int, icon: String) -> Unit
) {
    var recTitle by remember { mutableStateOf("") }
    var recCost by remember { mutableStateOf("20") }
    var recFrequency by remember { mutableIntStateOf(1) }
    var recIcon by remember { mutableStateOf("🍲") }

    val icons = listOf("🍲", "💧", "💊", "⚽", "📚", "🧼")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🔄 Новая цикличная трата",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = recTitle,
                    onValueChange = { recTitle = it },
                    label = { Text("Название регулярного расхода") },
                    placeholder = { Text("Например: Школьный обед") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = recCost,
                    onValueChange = { recCost = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Стоимость (монет)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Иконка:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    icons.forEach { icon ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (recIcon == icon) PixelGoldBright else Color(0xFFEEEEEE))
                                .clickable { recIcon = icon }
                                .padding(6.dp)
                        ) {
                            Text(text = icon, fontSize = 20.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Периодичность:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = recFrequency == 1,
                        onClick = { recFrequency = 1 },
                        colors = RadioButtonDefaults.colors(selectedColor = PixelGreenCrop)
                    )
                    Text("Каждый день", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    RadioButton(
                        selected = recFrequency == 3,
                        onClick = { recFrequency = 3 },
                        colors = RadioButtonDefaults.colors(selectedColor = PixelGoldDark)
                    )
                    Text("Раз в 3 дн.", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = recCost.toIntOrNull() ?: 0
                    if (recTitle.isNotBlank()) {
                        onAddRecurring(recTitle.trim(), cost, recFrequency, recIcon)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PixelGoldBright)
            ) {
                Text("Сохранить", color = PixelTextDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = PixelTextMuted)
            }
        }
    )
}

/**
 * Ячейка отдельного дня в сетке Stardew Valley
 */
@Composable
fun StardewDayCell(
    dayNumber: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isPast: Boolean,
    hasRecurring: Boolean,
    hasNotes: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cellBg = when {
        isToday -> Color(0xFFFFF1C6)
        isSelected -> Color(0xFFFFECC7)
        isPast -> Color(0xFFEBE0C7)
        else -> PixelParchmentLight
    }

    val cellBorder = when {
        isToday -> PixelGoldBright
        isSelected -> PixelWoodDark
        else -> PixelParchmentBorder
    }

    val borderWidth = when {
        isToday -> 2.dp
        isSelected -> 2.dp
        else -> 1.dp
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(cellBg)
            .border(borderWidth, cellBorder, RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Номер дня и маркер лапки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$dayNumber",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isToday) FontWeight.Black else FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isToday) PixelGoldDark else if (isPast) PixelTextMuted else PixelTextDark
                )
                if (isToday) {
                    Text(text = "🐾", fontSize = 9.sp)
                } else if (isPast) {
                    Text(text = "✓", fontSize = 9.sp, color = PixelGreenCrop, fontWeight = FontWeight.Bold)
                }
            }

            // Иконки событий/трат в этот день
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (hasRecurring) {
                    CoinIcon(modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(1.dp))
                }
                if (hasNotes) {
                    Text(text = "📝", fontSize = 9.sp)
                }
            }

            // Нижняя золотая полоска статуса сегодняшнего дня
            if (isToday) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(PixelGoldBright)
                )
            } else {
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }
}

@Composable
fun BudgetCounterRow(
    title: String,
    amount: Int,
    color: Color,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    enabled: Boolean
) {
    Column {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = PixelTextDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(color.copy(alpha = 0.2f))
                        .border(1.dp, color, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$amount",
                        fontWeight = FontWeight.ExtraBold,
                        color = color,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "монет", fontSize = 12.sp, color = PixelTextMuted)
            }

            if (enabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PixelButton(
                        text = "-10",
                        onClick = onMinus,
                        containerColor = PixelParchmentMedium,
                        textColor = PixelTextDark,
                        borderColor = PixelParchmentBorder
                    )
                    PixelButton(
                        text = "+10",
                        onClick = onPlus,
                        containerColor = color,
                        textColor = Color.White,
                        borderColor = PixelWoodDark
                    )
                }
            }
        }
    }
}

/**
 * Всплывающее окно подтверждения перехода на следующий день с итогами бюджета (ТЗ 2.5.5, 2.5.11)
 */
@Composable
fun NextDayConfirmDialog(
    currentDay: Int,
    nextDay: Int,
    currentPeriod: PeriodEntity?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isNextMonday = (nextDay - 1) % 7 == 0
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
    val compliance = remember(plan, actual) {
        PetEconomyCalculator.calculateCompliance(plan, actual)
    }
    val carePointsEarned = remember(plan, actual, compliance) {
        PetEconomyCalculator.calculateCarePoints(
            satiety = 100,
            health = 100,
            plan = plan,
            actual = actual,
            compliance = compliance
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(8.dp))
        ) {
            StardewBoard(
                headerTitle = "НОВЫЙ ДЕНЬ",
                headerIcon = "🌅"
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Перейти на День $nextDay?",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = PixelTextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Если бюджет был утвержден — показываем карточку «Итоги бюджета дня»
                    if (isConfirmed) {
                        DaySummaryBudgetSection(
                            plan = plan,
                            actual = actual,
                            compliance = compliance,
                            carePoints = carePointsEarned
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(PixelParchmentMedium)
                            .border(1.dp, PixelParchmentBorder, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = if (isNextMonday) "🎉" else "🐾", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isNextMonday) "Понедельник — выплата карманных!" else "Завершение Дня $currentDay",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PixelTextDark
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isNextMonday) {
                                    "Наступает новая неделя! При переходе на День $nextDay тебе будет начислено 200 монет 🪙 карманных денег."
                                } else {
                                    "Все заметки и траты за сегодня сохранятся в календаре, а питомец перейдет на следующий день."
                                },
                                fontSize = 11.sp,
                                color = PixelTextDark,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PixelButton(
                            text = "✕ Отмена",
                            onClick = onDismiss,
                            containerColor = PixelWoodMedium,
                            textColor = PixelParchmentLight,
                            borderColor = PixelWoodDark,
                            modifier = Modifier.weight(1f)
                        )
                        PixelButton(
                            text = "🌅 Перейти",
                            onClick = onConfirm,
                            containerColor = PixelGreenCrop,
                            textColor = Color.White,
                            borderColor = Color(0xFF1B5E20),
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }
            }
        }
    }
}
