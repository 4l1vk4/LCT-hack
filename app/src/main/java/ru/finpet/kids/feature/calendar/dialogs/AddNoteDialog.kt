package ru.finpet.kids.feature.calendar.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.PixelAlertDialog
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted

/**
 * Аутентичный пиксельный диалог создания заметки / задачи на выбранный день.
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

    PixelAlertDialog(
        onDismissRequest = onDismiss,
        title = "ЗАМЕТКА • ДЕНЬ $dayIndex",
        icon = "📝",
        confirmButton = {
            PixelButton(
                text = "Добавить",
                onClick = {
                    val cost = noteCost.toIntOrNull() ?: 0
                    if (noteTitle.isNotBlank()) {
                        onAddNote(noteTitle.trim(), cost, selectedCategory)
                    }
                },
                containerColor = PixelGoldBright,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 16.sp
            )
        },
        dismissButton = {
            PixelButton(
                text = "Отмена",
                onClick = onDismiss,
                containerColor = PixelParchmentMedium,
                textColor = PixelTextDark,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 16.sp
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = noteTitle,
                onValueChange = { noteTitle = it },
                label = { Text("Что планируем купить?", fontFamily = AppFontFamily, fontSize = 15.sp) },
                placeholder = { Text("Например: Корм или Игрушка", fontFamily = AppFontFamily, fontSize = 15.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PixelParchmentBorder,
                    unfocusedBorderColor = PixelParchmentBorder,
                    focusedContainerColor = PixelParchmentLight,
                    unfocusedContainerColor = PixelParchmentLight,
                    focusedTextColor = PixelTextDark,
                    unfocusedTextColor = PixelTextDark
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = noteCost,
                onValueChange = { noteCost = it.filter { ch -> ch.isDigit() } },
                label = { Text("Ожидаемая сумма (монет)", fontFamily = AppFontFamily, fontSize = 15.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PixelParchmentBorder,
                    unfocusedBorderColor = PixelParchmentBorder,
                    focusedContainerColor = PixelParchmentLight,
                    unfocusedContainerColor = PixelParchmentLight,
                    focusedTextColor = PixelTextDark,
                    unfocusedTextColor = PixelTextDark
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Категория расходов:",
                fontFamily = AppFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PixelTextDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PixelButton(
                    text = "🥣 Обязательное",
                    onClick = { selectedCategory = "MANDATORY" },
                    containerColor = if (selectedCategory == "MANDATORY") PixelGoldBright else PixelParchmentLight,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 7.dp)
                )
                PixelButton(
                    text = "🎈 Желаемое",
                    onClick = { selectedCategory = "OPTIONAL" },
                    containerColor = if (selectedCategory == "OPTIONAL") PixelGoldBright else PixelParchmentLight,
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 7.dp)
                )
            }
        }
    }
}
