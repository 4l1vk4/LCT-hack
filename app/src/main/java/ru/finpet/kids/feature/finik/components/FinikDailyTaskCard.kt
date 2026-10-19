package ru.finpet.kids.feature.finik.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.DocsIcon
import ru.finpet.kids.core.designsystem.StarIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.feature.calendar.model.CalendarTaskCategory

/**
 * Карточка «Задача дня» на главном экране питомца:
 * - Полностью удалены современные округлые пастельные плашки и оранжевые обводки.
 * - Отключен эффект ряби/ховера при нажатиях (indication = null).
 * - Чистый пиксельный стиль отображения прогресса и действия.
 */
@Composable
fun FinikDailyTaskCard(
    todayNotes: List<CalendarNoteEntity>,
    onShowTasksMenu: () -> Unit,
    onNavigateToTask: ((CalendarNoteEntity) -> Unit)?,
    onNavigateToMap: (() -> Unit)?,
    onOpenQuestions: () -> Unit,
    modifier: Modifier = Modifier,
    isPetRunaway: Boolean = false
) {
    if (todayNotes.isEmpty()) return

    val pendingTask = todayNotes.firstOrNull { !it.isCompleted }
    val completedCount = todayNotes.count { it.isCompleted }
    val totalCount = todayNotes.size

    val handleTaskClick: () -> Unit = {
        if (pendingTask != null) {
            if (pendingTask.category == "PET_QUESTION") {
                onOpenQuestions()
            } else if (onNavigateToTask != null) {
                onNavigateToTask(pendingTask)
            } else {
                onNavigateToMap?.invoke()
            }
        } else {
            onShowTasksMenu()
        }
    }

    FinCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onShowTasksMenu() }
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            // Верхняя строка: заголовок и счетчик выполненных задач
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (pendingTask == null) {
                        StarIcon(modifier = Modifier.size(18.dp))
                    } else {
                        DocsIcon(modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (pendingTask == null) "Все задачи выполнены!" else "Задача дня",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pendingTask == null) FreshGreen else TextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onShowTasksMenu() }
                ) {
                    Text(
                        text = "$completedCount/$totalCount",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pendingTask == null) FreshGreen else JoyOrange
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Содержимое текущей активной задачи
            if (pendingTask != null) {
                val category = CalendarTaskCategory.fromId(pendingTask.category)
                val taskIcon = category.icon
                val taskBadge = when {
                    pendingTask.category == "GROCERIES" -> "Лавка • Сдача"
                    pendingTask.category == "SCHOOL" -> "Школа • 2 урока"
                    pendingTask.category == "PET_FOOD" && isPetRunaway -> "Домой • 60"
                    pendingTask.category == "PET_FOOD" -> "Лавка • 30"
                    else -> category.defaultBadge
                }

                val displayTitle = if (pendingTask.category == "PET_FOOD" && isPetRunaway) {
                    "Купить корм для возвращения"
                } else {
                    pendingTask.title
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { handleTaskClick() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = taskIcon, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = displayTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Метка действия / стоимости без округлой разноцветной плашки
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { handleTaskClick() }
                    ) {
                        if (pendingTask.category == "PET_FOOD") {
                            val badgeText = if (isPetRunaway) "Домой • 60" else "Лавка • 30"
                            Text(
                                text = badgeText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = JoyOrange
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            CoinIcon(modifier = Modifier.size(14.dp))
                        } else if (pendingTask.cost > 0 && !category.isSystem) {
                            Text(
                                text = "${pendingTask.cost}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = JoyOrange
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            CoinIcon(modifier = Modifier.size(14.dp))
                        } else {
                            Text(
                                text = taskBadge,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = JoyOrange
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Все дела и уроки сделаны, Финни счастлив! 🎉",
                    fontSize = 15.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
