package ru.finpet.kids.feature.calendar.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.PixelBlueWater
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted

/**
 * Диалог создания заметки / задачи на выбранный день.
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
                fontFamily = AppFontFamily,
                fontSize = 17.sp
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
                Text(text = "Категория расходов:", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedCategory == "MANDATORY",
                        onClick = { selectedCategory = "MANDATORY" },
                        colors = RadioButtonDefaults.colors(selectedColor = PixelGreenCrop)
                    )
                    Text("Обязательное", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    RadioButton(
                        selected = selectedCategory == "OPTIONAL",
                        onClick = { selectedCategory = "OPTIONAL" },
                        colors = RadioButtonDefaults.colors(selectedColor = PixelBlueWater)
                    )
                    Text("Желаемое", fontSize = 14.sp)
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
                Text("Добавить", color = PixelTextDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = PixelTextMuted, fontSize = 15.sp)
            }
        }
    )
}
