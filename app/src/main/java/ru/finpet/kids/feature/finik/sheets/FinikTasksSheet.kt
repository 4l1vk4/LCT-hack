package ru.finpet.kids.feature.finik.sheets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import ru.finpet.kids.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import ru.finpet.kids.core.designsystem.PixelCheckbox
import ru.finpet.kids.core.designsystem.DocsIcon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.DiagramIcon
import ru.finpet.kids.core.designsystem.RenderBoardIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.core.designsystem.pixelButtonBackground
import ru.finpet.kids.feature.calendar.model.CalendarTaskCategory
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.remember
import ru.finpet.kids.core.designsystem.PixelBar
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted

// модальный боттом-шит быстрого чек-листа задач прямо с экрана питомца
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinikTasksSheet(
    todayNotes: List<CalendarNoteEntity>,
    onToggleNote: (CalendarNoteEntity) -> Unit,
    onNavigateToMap: (() -> Unit)? = null,
    onNavigateToTask: ((CalendarNoteEntity) -> Unit)? = null,
    onOpenQuestions: () -> Unit,
    onDismiss: () -> Unit,
    isPetRunaway: Boolean = false
) {
    val completedCount = todayNotes.count { it.isCompleted }
    val totalCount = todayNotes.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFE8DAC2),
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        // сохраняем полноэкранный режим
        KeepScreenImmersive()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Пиксельная шапка со счетчиком и крестиком
            PixelBar(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                DocsIcon(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Задачи на сегодня",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextDark
                    )
                    Text(
                        text = "Выполнено $completedCount из $totalCount",
                        fontSize = 15.sp,
                        color = PixelTextMuted
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.close_sign),
                        contentDescription = "Закрыть",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // список задач на сегодня
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 10.dp)
            ) {
                items(todayNotes, key = { it.id }) { note ->
                    val category = CalendarTaskCategory.fromId(note.category)
                    val isSystemTask = category.isSystem
                    val taskIcon = category.icon

                    val taskBadge = when {
                        note.category == "GROCERIES" -> "Лавка • Сдача"
                        note.category == "PET_FOOD" && isPetRunaway -> "Домой • 60"
                        note.category == "PET_FOOD" -> "Лавка • 30"
                        note.category == "BUDGET_PLAN" -> "Баночки"
                        else -> category.defaultBadge
                    }

                    val isNavigable = !note.isCompleted && (isSystemTask || note.category == "SAVINGS")
                    val handleNavigate = {
                        onDismiss()
                        if (note.category == "PET_QUESTION") {
                            onOpenQuestions()
                        } else if (note.category == "PET_FOOD" && isPetRunaway) {
                            onNavigateToTask?.invoke(note)
                        } else if (onNavigateToTask != null) {
                            onNavigateToTask(note)
                        } else {
                            onNavigateToMap?.invoke()
                        }
                    }

                    FinCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (isNavigable) Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { handleNavigate() } else Modifier),
                        backgroundColor = if (note.isCompleted) Color(0xFFF9FBE7) else Color(0xFFFFF9EE),
                        borderColor = if (note.isCompleted) Color(0xFFAED581) else Color(0xFFDFC490)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                PixelCheckbox(
                                    checked = note.isCompleted,
                                    onCheckedChange = if (isSystemTask) null else { { onToggleNote(note) } },
                                    enabled = !isSystemTask,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                RenderBoardIcon(taskIcon, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                val displayTitle = if (note.category == "PET_FOOD" && isPetRunaway) {
                                    "Купить корм для возвращения"
                                } else {
                                    note.title
                                }
                                Text(
                                    text = displayTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (note.isCompleted) TextSecondary else TextPrimary,
                                    textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            if (note.isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .pixelButtonBackground(containerColor = Color(0xFFE8F5E9))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (note.category == "GROCERIES" && note.cost > 0) {
                                             Text(
                                                text = "✅ +${note.cost}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FreshGreen
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            CoinIcon(modifier = Modifier.size(13.dp))
                                        } else {
                                            Text(
                                                text = "✅ Готово",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FreshGreen
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .pixelButtonBackground(containerColor = Color(0xFFFFF3E0))
                                        .then(
                                            if (isNavigable) {
                                                Modifier.clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) { handleNavigate() }
                                            } else Modifier
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (note.category == "PET_FOOD") {
                                            val badgeText = if (isPetRunaway) "Домой • 60" else "Лавка • 30"
                                            Text(
                                                text = badgeText,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            CoinIcon(modifier = Modifier.size(13.dp))
                                        } else if (note.cost > 0 && !isSystemTask) {
                                            Text(
                                                text = "${note.cost}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            CoinIcon(modifier = Modifier.size(13.dp))
                                        } else {
                                            Text(
                                                text = taskBadge,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // пиксельная кнопка закрытия
            PixelButton(
                text = "Понятно",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
