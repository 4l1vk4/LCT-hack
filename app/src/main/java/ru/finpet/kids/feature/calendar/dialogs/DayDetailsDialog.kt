package ru.finpet.kids.feature.calendar.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.RecurringExpenseEntity
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGoldGlow
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.feature.calendar.model.CalendarTaskCategory

/**
 * Модальная доска чек-листа выбранного дня (Daily Checklist) в стиле Stardew Valley.
 */
@Composable
fun DayDetailsDialog(
    dayNumber: Int,
    isToday: Boolean,
    balance: Int,
    notes: List<CalendarNoteEntity>,
    recurringExpenses: List<RecurringExpenseEntity> = emptyList(),
    isNextMonday: Boolean = false,
    onToggleNote: (CalendarNoteEntity) -> Unit,
    onCompleteChecklistTask: (note: CalendarNoteEntity, rewardCoins: Int) -> Unit,
    onOpenAddNote: () -> Unit,
    onDeleteNote: (CalendarNoteEntity) -> Unit,
    onCompletePeriod: () -> Unit,
    onNavigateToTask: ((CalendarNoteEntity) -> Unit)? = null,
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
                    // 1. Статус-строка дня и баланс
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
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToday) PixelGoldDark else PixelTextDark
                                )
                                Text(
                                    text = "Выполнено: $completedCount из $totalCount",
                                    fontSize = 15.sp,
                                    color = PixelTextMuted
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CoinIcon(modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$balance",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = PixelGoldDark,
                                    fontFamily = AppFontFamily
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

                    // 2. Регулярные платежи дня (если есть)
                    if (recurringExpenses.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PixelParchmentMedium)
                                    .border(1.dp, PixelParchmentBorder, RoundedCornerShape(4.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "🔄 Регулярные платежи этого дня:",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PixelTextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                recurringExpenses.forEach { exp ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${exp.icon} ${exp.title}",
                                            fontSize = 15.sp,
                                            color = PixelTextDark
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CoinIcon(modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${exp.cost}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = PixelGoldDark,
                                                fontFamily = AppFontFamily
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Список задач чек-листа
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
                                    fontSize = 14.sp,
                                    color = PixelTextMuted
                                )
                            }
                        }
                    } else {
                        items(notes, key = { it.id }) { note ->
                            val category = CalendarTaskCategory.fromId(note.category)
                            val isSystemTask = category.isSystem
                            val taskIcon = category.icon

                            val isNavigable = !note.isCompleted && (isSystemTask || note.category == "SAVINGS")
                            val handleNavigate = {
                                onDismiss()
                                onNavigateToTask?.invoke(note)
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
                                    .then(if (isNavigable && onNavigateToTask != null) Modifier.clickable { handleNavigate() } else Modifier)
                                    .padding(start = 8.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Чекбокс: системные задачи выполняются через реальные действия в игре
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
                                        modifier = Modifier.size(32.dp)
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = taskIcon, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = note.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (note.isCompleted) PixelTextMuted else PixelTextDark,
                                        textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Статус-бейдж
                                if (note.isCompleted) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFDCEDC8))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (note.category == "GROCERIES" && note.cost > 0) {
                                                Text(
                                                    text = "✅ Сдача +${note.cost}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PixelGreenCrop
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                CoinIcon(modifier = Modifier.size(13.dp))
                                            } else {
                                                Text(
                                                    text = "✅ Готово",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PixelGreenCrop
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PixelWoodDark)
                                            .then(if (isNavigable && onNavigateToTask != null) Modifier.clickable { handleNavigate() } else Modifier)
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (note.cost > 0 && !isSystemTask) {
                                                Text(
                                                    text = "${note.cost}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PixelGoldBright
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                CoinIcon(modifier = Modifier.size(13.dp))
                                            } else if (note.category == "PET_FOOD") {
                                                Text(
                                                    text = "Лавка • 30",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PixelGoldBright
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                CoinIcon(modifier = Modifier.size(13.dp))
                                            } else {
                                                val badgeLabel = when {
                                                    note.category == "GROCERIES" -> "Лавка • Сдача"
                                                    else -> category.defaultBadge
                                                }
                                                Text(
                                                    text = badgeLabel,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PixelGoldBright
                                                )
                                            }
                                            if (isNavigable && onNavigateToTask != null) {
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(text = "👉", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }

                                // Кнопка удаления только для пользовательских заметок (с доступным touch target 48x48)
                                if (!isSystemTask) {
                                    IconButton(
                                        onClick = { onDeleteNote(note) },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Text(
                                            text = "✕",
                                            fontWeight = FontWeight.Bold,
                                            color = PixelRedBerry,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Кнопка добавления своей заметки
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        PixelButton(
                            text = "➕ Добавить свою заметку",
                            onClick = onOpenAddNote,
                            containerColor = PixelGoldBright,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 15.sp
                        )
                    }

                    // 5. Завершение дня (только если день — СЕГОДНЯ)
                    if (isToday) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
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
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 15.sp
                            )
                        }
                    }

                    // 6. Кнопка закрытия
                    item {
                        PixelButton(
                            text = "✕ Закрыть",
                            onClick = onDismiss,
                            containerColor = PixelWoodMedium,
                            textColor = PixelParchmentLight,
                            borderColor = PixelWoodDark,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
