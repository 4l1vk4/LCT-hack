package ru.finpet.kids.feature.calendar.dialogs

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
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

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

/**
 * Аутентичный пиксельный диалог создания заметки / дела на выбранный день.
 * Позволяет ребенку запланировать свое личное дело без трат и без категорий.
 */
@Composable
fun AddNoteDialog(
    dayIndex: Int,
    onDismiss: () -> Unit,
    onAddNote: (title: String, cost: Int, category: String) -> Unit
) {
    var noteTitle by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    PixelAlertDialog(
        onDismissRequest = onDismiss,
        title = "ДЕЛО • ДЕНЬ $dayIndex",
        icon = "📝",
        confirmButton = {
            PixelButton(
                text = "Добавить дело",
                onClick = {
                    if (noteTitle.isNotBlank()) {
                        onAddNote(noteTitle.trim(), 0, "NOTE")
                    }
                },
                enabled = noteTitle.isNotBlank(),
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            Text(
                text = "Какое дело запланируем?",
                fontFamily = AppFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PixelTextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = noteTitle,
                onValueChange = { noteTitle = it },
                label = { Text("Название дела", fontFamily = AppFontFamily, fontSize = 15.sp) },
                placeholder = { Text("Например: Погулять с котиком или почитать книгу", fontFamily = AppFontFamily, fontSize = 14.sp) },
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
        }
    }
}
