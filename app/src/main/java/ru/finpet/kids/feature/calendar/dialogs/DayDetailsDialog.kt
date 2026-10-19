package ru.finpet.kids.feature.calendar.dialogs

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import ru.finpet.kids.core.designsystem.PixelCheckbox
import ru.finpet.kids.core.designsystem.StarIcon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FinPetTheme
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.RenderBoardIcon
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
    notes: List<CalendarNoteEntity>,
    demoMode: Boolean = false,
    onToggleNote: (CalendarNoteEntity) -> Unit,
    onOpenAddNote: () -> Unit,
    onDeleteNote: (CalendarNoteEntity) -> Unit,
    onCompletePeriod: () -> Unit,
    onAdvancePeriod: () -> Unit = onCompletePeriod,
    onAdvanceToNextGrowthStage: () -> Unit = {},
    growthStage: String = "BABY",
    onNavigateToTask: ((CalendarNoteEntity) -> Unit)? = null,
    onDismiss: () -> Unit,
    isPetRunaway: Boolean = false
) {
    val completedCount = notes.count { it.isCompleted }
    val totalCount = notes.size
    val uiScale = LocalUiScale.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FinPetTheme {
            KeepScreenImmersive()
            Box(
            modifier = Modifier
                .fillMaxWidth(0.96f)
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
                    verticalArrangement = Arrangement.spacedBy((8.dp * uiScale).coerceIn(5.dp, 10.dp)),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    // 1. Статус-строка дня и баланс
                    item {
                        PixelPanel(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(
                                horizontal = (10.dp * uiScale).coerceIn(6.dp, 12.dp),
                                vertical = (6.dp * uiScale).coerceIn(4.dp, 8.dp)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isToday) {
                                            StarIcon(modifier = Modifier.size((18.dp * uiScale).coerceIn(12.dp, 20.dp)))
                                            Spacer(modifier = Modifier.width((5.dp * uiScale).coerceIn(2.dp, 6.dp)))
                                        }
                                        Text(
                                            text = if (isToday) "Сегодняшние задачи" else "Задачи на День $dayNumber",
                                            fontSize = (16f * uiScale).coerceIn(12f, 18f).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isToday) PixelGoldDark else PixelTextDark
                                        )
                                    }
                                    Text(
                                        text = "Выполнено: $completedCount из $totalCount",
                                        fontSize = (13f * uiScale).coerceIn(10f, 15f).sp,
                                        color = PixelTextMuted
                                    )
                                }
                            }
                        }
                    }

                    // 2. Список задач чек-листа
                    if (notes.isEmpty()) {
                        item {
                            PixelPanel(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues((10.dp * uiScale).coerceIn(6.dp, 12.dp))
                            ) {
                                Text(
                                    text = "На этот день нет задач. Нажмите кнопку ниже, чтобы добавить свою заметку!",
                                    fontSize = (14f * uiScale).coerceIn(10f, 16f).sp,
                                    lineHeight = (18f * uiScale).coerceIn(13f, 21f).sp,
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
                                contentPadding = PaddingValues(
                                    start = (6.dp * uiScale).coerceIn(4.dp, 8.dp),
                                    end = (4.dp * uiScale).coerceIn(2.dp, 4.dp),
                                    top = (5.dp * uiScale).coerceIn(3.dp, 6.dp),
                                    bottom = (5.dp * uiScale).coerceIn(3.dp, 6.dp)
                                )
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
                                    PixelCheckbox(
                                        checked = note.isCompleted,
                                        onCheckedChange = if (isSystemTask) null else { { onToggleNote(note) } },
                                        enabled = !isSystemTask,
                                        modifier = Modifier.size((24.dp * uiScale).coerceIn(18.dp, 28.dp))
                                    )

                                    Spacer(modifier = Modifier.width((5.dp * uiScale).coerceIn(3.dp, 6.dp)))
                                    RenderBoardIcon(taskIcon, modifier = Modifier.size((16.dp * uiScale).coerceIn(12.dp, 18.dp)))
                                    Spacer(modifier = Modifier.width((6.dp * uiScale).coerceIn(4.dp, 8.dp)))

                                    val displayTitle = if (note.category == "PET_FOOD" && isPetRunaway) {
                                        "Купить корм для возвращения"
                                    } else {
                                        note.title
                                    }
                                    Text(
                                        text = displayTitle,
                                        fontSize = (14f * uiScale).coerceIn(10f, 16f).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (note.isCompleted) PixelTextMuted else PixelTextDark,
                                        textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }

                                Spacer(modifier = Modifier.width((4.dp * uiScale).coerceIn(2.dp, 6.dp)))

                                // Статус-бейдж в пиксельном стиле (со смещением влево на 2px)
                                if (note.isCompleted) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = (-2).dp)
                                            .pixelButtonBackground(containerColor = Color(0xFFDCEDC8))
                                            .padding(
                                                horizontal = (6.dp * uiScale).coerceIn(4.dp, 8.dp),
                                                vertical = (3.dp * uiScale).coerceIn(2.dp, 4.dp)
                                            )
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (note.category == "GROCERIES" && note.cost > 0) {
                                                Text(
                                                    text = "Сдача +${note.cost}",
                                                    fontFamily = AppFontFamily,
                                                    fontSize = (12f * uiScale).coerceIn(8f, 14f).sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PixelGreenCrop
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                CoinIcon(modifier = Modifier.size((12.dp * uiScale).coerceIn(9.dp, 14.dp)))
                                            } else {
                                                Text(
                                                    text = "Готово",
                                                    fontFamily = AppFontFamily,
                                                    fontSize = (12f * uiScale).coerceIn(8f, 14f).sp,
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
                                            .padding(
                                                horizontal = (6.dp * uiScale).coerceIn(4.dp, 8.dp),
                                                vertical = (3.dp * uiScale).coerceIn(2.dp, 4.dp)
                                            )
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (note.cost > 0 && !isSystemTask) {
                                                Text(
                                                    text = "${note.cost}",
                                                    fontFamily = AppFontFamily,
                                                    fontSize = (12f * uiScale).coerceIn(8f, 14f).sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PixelGoldBright
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                CoinIcon(modifier = Modifier.size((12.dp * uiScale).coerceIn(9.dp, 14.dp)))
                                            } else if (note.category == "PET_FOOD") {
                                                val badgeText = if (isPetRunaway) "Домой • 60" else "Лавка • 30"
                                                Text(
                                                    text = badgeText,
                                                    fontFamily = AppFontFamily,
                                                    fontSize = (12f * uiScale).coerceIn(8f, 14f).sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PixelGoldBright
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                CoinIcon(modifier = Modifier.size((12.dp * uiScale).coerceIn(9.dp, 14.dp)))
                                            } else {
                                                val badgeLabel = when {
                                                    note.category == "GROCERIES" -> "Лавка • Сдача"
                                                    note.category == "BUDGET_PLAN" -> "Баночки"
                                                    note.category == "NOTE" || note.category == "CHORE" -> "Дело"
                                                    else -> category.defaultBadge
                                                }
                                                Text(
                                                    text = badgeLabel,
                                                    fontFamily = AppFontFamily,
                                                    fontSize = (12f * uiScale).coerceIn(8f, 14f).sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PixelGoldBright
                                                )
                                            }
                                        }
                                    }
                                }

                                // Кнопка удаления только для пользовательских заметок (с доступным touch target 48x48)
                                if (!isSystemTask) {
                                    IconButton(
                                        onClick = { onDeleteNote(note) },
                                        modifier = Modifier.size((32.dp * uiScale).coerceIn(24.dp, 40.dp))
                                    ) {
                                        Text(
                                            text = "✕",
                                            fontWeight = FontWeight.Bold,
                                            color = PixelRedBerry,
                                            fontSize = (13f * uiScale).coerceIn(10f, 14f).sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                    // 4. Кнопка добавления своей заметки
                    item {
                        Spacer(modifier = Modifier.height((4.dp * uiScale).coerceIn(2.dp, 4.dp)))
                        PixelButton(
                            text = "Добавить свою заметку",
                            onClick = onOpenAddNote,
                            containerColor = PixelGoldBright,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                            contentPadding = PaddingValues(vertical = (8.dp * uiScale).coerceIn(5.dp, 10.dp), horizontal = 8.dp)
                        )
                    }

                    // 5. Завершение дня или периода (только если день — СЕГОДНЯ и включен демо-режим)
                    if (isToday && demoMode) {
                        item {
                            Spacer(modifier = Modifier.height((4.dp * uiScale).coerceIn(2.dp, 4.dp)))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy((6.dp * uiScale).coerceIn(4.dp, 8.dp))
                            ) {
                                PixelButton(
                                    text = "⏩ Пропустить день",
                                    onClick = onCompletePeriod,
                                    containerColor = PixelGreenCrop,
                                    textColor = Color.White,
                                    borderColor = Color(0xFF1B5E20),
                                    modifier = Modifier.weight(1f),
                                    fontSize = (13f * uiScale).coerceIn(9f, 15f).sp,
                                    contentPadding = PaddingValues(vertical = (6.dp * uiScale).coerceIn(4.dp, 8.dp), horizontal = (6.dp * uiScale).coerceIn(3.dp, 8.dp))
                                )

                                PixelButton(
                                    text = "🗓️ Пропустить период (+200)",
                                    onClick = onAdvancePeriod,
                                    containerColor = JoyOrange,
                                    textColor = Color.White,
                                    borderColor = Color(0xFFBF360C),
                                    modifier = Modifier.weight(1f),
                                    fontSize = (13f * uiScale).coerceIn(9f, 15f).sp,
                                    contentPadding = PaddingValues(vertical = (6.dp * uiScale).coerceIn(4.dp, 8.dp), horizontal = (6.dp * uiScale).coerceIn(3.dp, 8.dp))
                                )
                            }

                            if (growthStage != "ADULT") {
                                val nextStageText = if (growthStage == "BABY") {
                                    "Пропустить 7 периодов (до «Подростка»)"
                                } else {
                                    "Пропустить 11 периодов (до «Взрослого»)"
                                }
                                Spacer(modifier = Modifier.height((6.dp * uiScale).coerceIn(3.dp, 6.dp)))
                                PixelButton(
                                    text = nextStageText,
                                    onClick = onAdvanceToNextGrowthStage,
                                    containerColor = PixelGoldBright,
                                    textColor = PixelTextDark,
                                    borderColor = PixelGoldDark,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontSize = (13f * uiScale).coerceIn(9f, 15f).sp,
                                    contentPadding = PaddingValues(vertical = (6.dp * uiScale).coerceIn(4.dp, 8.dp), horizontal = (6.dp * uiScale).coerceIn(3.dp, 8.dp))
                                )
                            }
                        }
                    }

                    // 6. Кнопка закрытия
                    item {
                        PixelButton(
                            text = "Закрыть",
                            onClick = onDismiss,
                            containerColor = PixelWoodMedium,
                            textColor = PixelParchmentLight,
                            borderColor = PixelWoodDark,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                            contentPadding = PaddingValues(vertical = (8.dp * uiScale).coerceIn(5.dp, 10.dp))
                        )
                    }
                }
            }
        }
    }
    }
}
