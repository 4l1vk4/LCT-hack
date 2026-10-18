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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.IconButton
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
import ru.finpet.kids.core.designsystem.DiagramIcon
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGoldGlow
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.core.designsystem.pixelButtonBackground
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
    demoMode: Boolean = false,
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
        KeepScreenImmersive()
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            StardewBoard(
                modifier = Modifier.fillMaxSize(),
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
                        PixelPanel(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isToday) "🌟 Сегодняшние задачи" else "🗓️ Задачи на День $dayNumber",
                                        fontSize = 18.sp,
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
                                        fontSize = 16.sp,
                                        color = PixelGoldDark,
                                        fontFamily = AppFontFamily
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        // Шкала прогресса выполнения чек-листа дня (без точек на конце)
                        val progressFraction = if (totalCount > 0) (completedCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(PixelParchmentBorder)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressFraction)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PixelGreenCrop)
                            )
                        }
                    }

                    // 2. Список задач чек-листа
                    if (notes.isEmpty()) {
                        item {
                            PixelPanel(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Text(
                                    text = "На этот день нет задач. Нажмите кнопку ниже, чтобы добавить свою заметку!",
                                    fontSize = 16.sp,
                                    lineHeight = 21.sp,
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

                            PixelPanel(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (isNavigable && onNavigateToTask != null) Modifier.clickable { handleNavigate() } else Modifier),
                                contentPadding = PaddingValues(start = 8.dp, end = 4.dp, top = 6.dp, bottom = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
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
                                    if (taskIcon == "📊") {
                                        DiagramIcon(modifier = Modifier.size(18.dp))
                                    } else {
                                        Text(text = taskIcon, fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = note.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (note.isCompleted) PixelTextMuted else PixelTextDark,
                                        textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Статус-бейдж в пиксельном стиле (со смещением влево на 2px)
                                if (note.isCompleted) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = (-2).dp)
                                            .pixelButtonBackground(containerColor = Color(0xFFDCEDC8))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (note.category == "GROCERIES" && note.cost > 0) {
                                                Text(
                                                    text = "✅ Сдача +${note.cost}",
                                                    fontFamily = AppFontFamily,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PixelGreenCrop
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                CoinIcon(modifier = Modifier.size(14.dp))
                                            } else {
                                                Text(
                                                    text = "✅ Готово",
                                                    fontFamily = AppFontFamily,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PixelGreenCrop
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = (-2).dp)
                                            .pixelButtonBackground(containerColor = PixelWoodMedium)
                                            .then(if (isNavigable && onNavigateToTask != null) Modifier.clickable { handleNavigate() } else Modifier)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (note.cost > 0 && !isSystemTask) {
                                                Text(
                                                    text = "${note.cost}",
                                                    fontFamily = AppFontFamily,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PixelGoldBright
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                CoinIcon(modifier = Modifier.size(14.dp))
                                            } else if (note.category == "PET_FOOD") {
                                                Text(
                                                    text = "Лавка • 30",
                                                    fontFamily = AppFontFamily,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PixelGoldBright
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                CoinIcon(modifier = Modifier.size(14.dp))
                                            } else {
                                                val badgeLabel = when {
                                                    note.category == "GROCERIES" -> "Лавка • Сдача"
                                                    note.category == "BUDGET_PLAN" -> "Баночки"
                                                    else -> category.defaultBadge
                                                }
                                                Text(
                                                    text = badgeLabel,
                                                    fontFamily = AppFontFamily,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PixelGoldBright
                                                )
                                            }
                                            if (isNavigable && onNavigateToTask != null) {
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(text = "👉", fontSize = 13.sp)
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
                }

                    // 4. Кнопка добавления своей заметки
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        PixelButton(
                            text = "➕ Добавить свою заметку",
                            onClick = onOpenAddNote,
                            containerColor = PixelGoldBright,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 16.sp
                        )
                    }

                    // 5. Завершение дня (только если день — СЕГОДНЯ и включен демо-режим)
                    if (isToday && demoMode) {
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
                                fontSize = 16.sp
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
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
