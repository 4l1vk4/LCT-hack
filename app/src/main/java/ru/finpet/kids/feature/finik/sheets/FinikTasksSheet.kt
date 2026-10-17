package ru.finpet.kids.feature.finik.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.feature.calendar.model.CalendarTaskCategory
import androidx.compose.foundation.layout.PaddingValues

// модальный боттом-шит быстрого чек-листа задач прямо с экрана питомца
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinikTasksSheet(
    todayNotes: List<CalendarNoteEntity>,
    onToggleNote: (CalendarNoteEntity) -> Unit,
    onNavigateToMap: (() -> Unit)? = null,
    onNavigateToTask: ((CalendarNoteEntity) -> Unit)? = null,
    onOpenQuestions: () -> Unit,
    onDismiss: () -> Unit
) {
    val completedCount = todayNotes.count { it.isCompleted }
    val totalCount = todayNotes.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        // сохраняем полноэкранный режим
        KeepScreenImmersive()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // шапка со счетчиком и крестиком
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📋", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Задачи на сегодня",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Выполнено $completedCount из $totalCount",
                            fontSize = 15.sp,
                            color = TextSecondary
                        )
                    }
                }
                Text(
                    text = "✕",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onDismiss() }
                        .padding(6.dp)
                )
            }

            // прогресс-бар выполнения без сторонних точек индикатора
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFEEEEEE))
            ) {
                val progressFraction = if (totalCount > 0) (completedCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f
                if (progressFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressFraction)
                            .clip(RoundedCornerShape(4.dp))
                            .background(FreshGreen)
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
                        note.category == "PET_FOOD" -> "Лавка • 30"
                        note.category == "BUDGET_PLAN" -> "Баночки"
                        else -> category.defaultBadge
                    }

                    val isNavigable = !note.isCompleted && (isSystemTask || note.category == "SAVINGS")
                    val handleNavigate = {
                        onDismiss()
                        if (note.category == "PET_QUESTION") {
                            onOpenQuestions()
                        } else if (onNavigateToTask != null) {
                            onNavigateToTask(note)
                        } else {
                            onNavigateToMap?.invoke()
                        }
                    }

                    FinCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (isNavigable) Modifier.clickable { handleNavigate() } else Modifier),
                        backgroundColor = if (note.isCompleted) Color(0xFFF9FBE7) else Color(0xFFFAFAFA),
                        borderColor = if (note.isCompleted) Color(0xFFAED581) else Color(0xFFE0E0E0)
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
                                Checkbox(
                                    checked = note.isCompleted,
                                    onCheckedChange = if (isSystemTask) null else { { onToggleNote(note) } },
                                    enabled = !isSystemTask,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = FreshGreen,
                                        uncheckedColor = TextSecondary,
                                        disabledCheckedColor = FreshGreen,
                                        disabledUncheckedColor = Color(0xFFBDBDBD)
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (taskIcon == "📊") {
                                    DiagramIcon(modifier = Modifier.size(20.dp))
                                } else {
                                    Text(text = taskIcon, fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = note.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (note.isCompleted) TextSecondary else TextPrimary,
                                    textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            if (note.isCompleted) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        if (note.category == "GROCERIES" && note.cost > 0) {
                                            Text(
                                                text = "✅ +${note.cost}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FreshGreen
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            CoinIcon(modifier = Modifier.size(11.dp))
                                        } else {
                                            Text(
                                                text = "✅ Готово",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FreshGreen
                                            )
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFFF3E0),
                                    modifier = if (isNavigable) {
                                        Modifier.clickable { handleNavigate() }
                                    } else Modifier
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        if (note.category == "PET_FOOD") {
                                            Text(
                                                text = "Лавка • 30",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = JoyOrange
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            CoinIcon(modifier = Modifier.size(13.dp))
                                        } else if (note.cost > 0 && !isSystemTask) {
                                            Text(
                                                text = "${note.cost}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = JoyOrange
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            CoinIcon(modifier = Modifier.size(13.dp))
                                        } else {
                                            Text(
                                                text = taskBadge,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = JoyOrange
                                            )
                                        }
                                        if (isNavigable) {
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(text = "👉", fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // кнопка закрытия
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = JoyOrange),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Понятно", fontWeight = FontWeight.Bold)
            }
        }
    }
}
