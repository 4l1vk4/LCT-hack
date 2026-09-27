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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import ru.finpet.kids.core.designsystem.SkyBlue
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.core.domain.model.BudgetPlan

@Composable
fun CalendarScreen(
    profile: ProfileEntity?,
    currentPeriod: PeriodEntity?,
    calendarNotes: List<CalendarNoteEntity>,
    recurringExpenses: List<RecurringExpenseEntity>,
    onConfirmBudget: (BudgetPlan) -> Unit,
    onCompletePeriod: () -> Unit,
    onAddNote: (dayIndex: Int, title: String, cost: Int, category: String) -> Unit,
    onDeleteNote: (CalendarNoteEntity) -> Unit,
    onToggleNote: (CalendarNoteEntity) -> Unit,
    onAddRecurring: (title: String, cost: Int, frequencyDays: Int, icon: String) -> Unit,
    onDeleteRecurring: (RecurringExpenseEntity) -> Unit
) {
    val balance = profile?.balance ?: 50
    val currentDayIndex = profile?.currentPeriodIndex ?: 1

    // Экран календаря чистый: детальная информация открывается ТОЛЬКО по нажатию на день
    var inspectedDay by remember { mutableStateOf<Int?>(null) }
    var showRecurringDialog by remember { mutableStateOf(false) }
    var showAddNoteDialogForDay by remember { mutableStateOf<Int?>(null) }
    var showAddRecurringDialog by remember { mutableStateOf(false) }

    var plannedMandatory by remember(currentPeriod) {
        mutableIntStateOf(currentPeriod?.plannedMandatory ?: 60)
    }
    var plannedOptional by remember(currentPeriod) {
        mutableIntStateOf(currentPeriod?.plannedOptional ?: 40)
    }
    var plannedSavings by remember(currentPeriod) {
        mutableIntStateOf(currentPeriod?.plannedSavings ?: 50)
    }

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

                            val hasRecurring = recurringDayMap[dayNum]?.isNotEmpty() == true
                            val hasNotes = notesByDay.containsKey(dayNum)

                            StardewDayCell(
                                dayNumber = dayNum,
                                isToday = isToday,
                                isSelected = isSelected,
                                isPast = isPast,
                                hasRecurring = hasRecurring,
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
                        CoinIcon(modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Постоянная трата", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = PixelTextDark)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📝", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Заметка", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = PixelTextDark)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // --- 3. ПОДСКАЗКА И КНОПКА ПОСТОЯННЫХ РАСХОДОВ ---
        item(key = "calendar_hint") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(PixelParchmentMedium)
                    .border(1.dp, PixelParchmentBorder, RoundedCornerShape(6.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📌", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Нажмите на любой день календаря, чтобы открыть заметки, список покупок и распределить бюджет!",
                        fontSize = 12.sp,
                        color = PixelTextDark,
                        lineHeight = 17.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        item(key = "recurring_button") {
            PixelButton(
                text = "🔄 Постоянные расходы питомца (${recurringExpenses.size})",
                onClick = { showRecurringDialog = true },
                containerColor = PixelWoodDark,
                textColor = PixelParchmentLight,
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
            dayRecurring = selectedDayRecurring,
            currentPeriod = currentPeriod,
            plannedMandatory = plannedMandatory,
            plannedOptional = plannedOptional,
            plannedSavings = plannedSavings,
            onMandatoryChange = { plannedMandatory = it },
            onOptionalChange = { plannedOptional = it },
            onSavingsChange = { plannedSavings = it },
            onConfirmBudget = onConfirmBudget,
            onCompletePeriod = {
                onCompletePeriod()
                inspectedDay = null
            },
            onDismiss = { inspectedDay = null },
            onOpenAddNote = { showAddNoteDialogForDay = dayNum },
            onDeleteNote = onDeleteNote,
            onToggleNote = onToggleNote,
            onManageRecurring = { showRecurringDialog = true }
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
}

/**
 * Модальная доска деталей выбранного дня (Stardew Valley Day Inspector)
 */
@Composable
fun DayDetailsDialog(
    dayNumber: Int,
    isToday: Boolean,
    balance: Int,
    notes: List<CalendarNoteEntity>,
    dayRecurring: List<RecurringExpenseEntity>,
    currentPeriod: PeriodEntity?,
    plannedMandatory: Int,
    plannedOptional: Int,
    plannedSavings: Int,
    onMandatoryChange: (Int) -> Unit,
    onOptionalChange: (Int) -> Unit,
    onSavingsChange: (Int) -> Unit,
    onConfirmBudget: (BudgetPlan) -> Unit,
    onCompletePeriod: () -> Unit,
    onDismiss: () -> Unit,
    onOpenAddNote: () -> Unit,
    onDeleteNote: (CalendarNoteEntity) -> Unit,
    onToggleNote: (CalendarNoteEntity) -> Unit,
    onManageRecurring: () -> Unit
) {
    val totalPlanned = plannedMandatory + plannedOptional + plannedSavings
    val remainder = balance - totalPlanned
    val isConfirmed = currentPeriod?.isBudgetConfirmed == true

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
                headerTitle = "ДЕНЬ $dayNumber" + if (isToday) " • СЕГОДНЯ" else " • ПЛАНЫ ДНЯ",
                headerIcon = if (isToday) "🐾" else "📜"
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
                                    text = if (isToday) "🌟 Текущий игровой день" else "Календарный день месяца",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToday) PixelGoldDark else PixelTextDark
                                )
                                Text(
                                    text = if (isToday) "День Финни в игре" else "День $dayNumber из 28",
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
                    }

                    // 2. Заметки и запланированные покупки
                    item {
                        Text(
                            text = "📝 ЗАМЕТКИ И ПОКУПКИ НА ДЕНЬ $dayNumber",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PixelTextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (notes.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PixelParchmentMedium)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "На этот день еще нет покупок или заметок. Нажмите кнопку ниже, чтобы спланировать трату!",
                                    fontSize = 12.sp,
                                    color = PixelTextMuted,
                                    lineHeight = 16.sp
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                notes.forEach { note ->
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Checkbox(
                                                checked = note.isCompleted,
                                                onCheckedChange = { onToggleNote(note) },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = PixelGreenCrop,
                                                    uncheckedColor = PixelTextMuted
                                                ),
                                                modifier = Modifier.size(26.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = note.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (note.isCompleted) PixelTextMuted else PixelTextDark,
                                                    textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                                )
                                                Text(
                                                    text = when (note.category) {
                                                        "MANDATORY" -> "Обязательное"
                                                        "SAVINGS" -> "В копилку"
                                                        else -> "Желаемое"
                                                    },
                                                    fontSize = 10.sp,
                                                    color = when (note.category) {
                                                        "MANDATORY" -> PixelGreenCrop
                                                        "SAVINGS" -> PixelGoldDark
                                                        else -> PixelBlueWater
                                                    }
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (note.cost > 0) {
                                                CoinIcon(modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "${note.cost}",
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    color = PixelGoldDark
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
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
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        PixelButton(
                            text = "➕ Добавить заметку / покупку",
                            onClick = onOpenAddNote,
                            containerColor = PixelGoldBright,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 3. Цикличные расходы, приходящиеся на этот день
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔄 ПОСТОЯННЫЕ ТРАТЫ В ЭТОТ ДЕНЬ",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = PixelTextDark
                            )
                            Text(
                                text = "Все (${dayRecurring.size})",
                                fontSize = 11.sp,
                                color = PixelBlueWater,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable(onClick = onManageRecurring)
                                    .padding(2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        if (dayRecurring.isEmpty()) {
                            Text(
                                text = "В этот день нет постоянных расходов.",
                                fontSize = 11.sp,
                                color = PixelTextMuted
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                dayRecurring.forEach { expense ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PixelParchmentMedium)
                                            .border(1.dp, PixelParchmentBorder, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = expense.icon, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = expense.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PixelTextDark)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CoinIcon(modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${expense.cost}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PixelGoldDark,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Блок бюджета и завершения дня (только если день — СЕГОДНЯ)
                    if (isToday) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🧺 РАСПРЕДЕЛЕНИЕ БЮДЖЕТА НА СЕГОДНЯ",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = PixelTextDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            BudgetCounterRow(
                                title = "1. Обязательное (еда, вода)",
                                amount = plannedMandatory,
                                color = FreshGreen,
                                onMinus = { if (!isConfirmed && plannedMandatory >= 10) onMandatoryChange(plannedMandatory - 10) },
                                onPlus = { if (!isConfirmed) onMandatoryChange(plannedMandatory + 10) },
                                enabled = !isConfirmed
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            BudgetCounterRow(
                                title = "2. Желаемое (игрушки, сладости)",
                                amount = plannedOptional,
                                color = SkyBlue,
                                onMinus = { if (!isConfirmed && plannedOptional >= 10) onOptionalChange(plannedOptional - 10) },
                                onPlus = { if (!isConfirmed) onOptionalChange(plannedOptional + 10) },
                                enabled = !isConfirmed
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            BudgetCounterRow(
                                title = "3. В копилку на мечту",
                                amount = plannedSavings,
                                color = JoyOrange,
                                onMinus = { if (!isConfirmed && plannedSavings >= 10) onSavingsChange(plannedSavings - 10) },
                                onPlus = { if (!isConfirmed) onSavingsChange(plannedSavings + 10) },
                                enabled = !isConfirmed
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Сводка плана
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PixelParchmentMedium)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "План: $totalPlanned монет", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PixelTextDark)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Остаток: $remainder монет",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (remainder >= 0) PixelGreenCrop else PixelRedBerry
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            if (!isConfirmed) {
                                PixelButton(
                                    text = if (remainder >= 0) "Утвердить план на день" else "Не хватает монет!",
                                    enabled = remainder >= 0,
                                    onClick = {
                                        onConfirmBudget(
                                            BudgetPlan(
                                                plannedMandatory = plannedMandatory,
                                                plannedOptional = plannedOptional,
                                                plannedSavings = plannedSavings
                                            )
                                        )
                                    },
                                    containerColor = PixelGoldBright,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✅ План на день утвержден!",
                                        fontWeight = FontWeight.Bold,
                                        color = PixelGreenCrop,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            val nextDayNumber = dayNumber + 1
                            val isNextMonday = (nextDayNumber - 1) % 7 == 0
                            val completeButtonText = if (isNextMonday) {
                                "🌅 Завершить день (+200 монет на неделю!)"
                            } else {
                                "🌅 Завершить день"
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            PixelButton(
                                text = completeButtonText,
                                onClick = onCompletePeriod,
                                containerColor = PixelGreenCrop,
                                textColor = Color.White,
                                borderColor = Color(0xFF1B5E20),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = if (isNextMonday) "🎉 Завтра понедельник: выплата 200 монет карманных денег!"
                                else "💡 Карманные деньги (+200 монет) выдаются по понедельникам",
                                fontSize = 11.sp,
                                color = PixelTextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        }
                    }

                    // 5. Кнопка закрытия
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        PixelButton(
                            text = "✕ Закрыть",
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
