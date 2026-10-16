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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.core.data.local.entity.RecurringExpenseEntity
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentDark
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.StardewBoard

/**
 * Диалог управления постоянными (цикличными) расходами питомца.
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

                    if (recurringExpenses.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PixelParchmentMedium)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Пока нет регулярных трат. Добавь их кнопкой ниже!",
                                    fontSize = 12.sp,
                                    color = PixelTextMuted
                                )
                            }
                        }
                    } else {
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
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text(text = expense.icon, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = expense.title,
                                            fontSize = 15.sp,
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
                                            fontSize = 13.sp,
                                            color = PixelTextMuted
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CoinIcon(modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${expense.cost}",
                                        fontFamily = AppFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = PixelGoldDark
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { onDeleteRecurring(expense) },
                                        modifier = Modifier.size(36.dp)
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

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        PixelButton(
                            text = "➕ Добавить цикличную трату",
                            onClick = onOpenAddRecurring,
                            containerColor = PixelGoldBright,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        PixelButton(
                            text = "✕ Закрыть",
                            onClick = onDismiss,
                            containerColor = PixelParchmentDark,
                            textColor = PixelTextDark,
                            borderColor = PixelParchmentBorder,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Диалог создания новой цикличной траты.
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
                fontFamily = AppFontFamily,
                fontSize = 17.sp
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
                Text(text = "Иконка:", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
                Text(text = "Периодичность:", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = recFrequency == 1,
                        onClick = { recFrequency = 1 },
                        colors = RadioButtonDefaults.colors(selectedColor = PixelGreenCrop)
                    )
                    Text("Каждый день", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    RadioButton(
                        selected = recFrequency == 3,
                        onClick = { recFrequency = 3 },
                        colors = RadioButtonDefaults.colors(selectedColor = PixelGoldDark)
                    )
                    Text("Раз в 3 дн.", fontSize = 14.sp)
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
                Text("Сохранить", color = PixelTextDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = PixelTextMuted, fontSize = 15.sp)
            }
        }
    )
}
