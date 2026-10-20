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
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.core.data.local.entity.RecurringExpenseEntity
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FinPetTheme
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.PixelAlertDialog
import ru.finpet.kids.core.designsystem.pixelButtonBackground
import androidx.compose.material3.OutlinedTextFieldDefaults
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentDark
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.RenderBoardIcon
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
    val uiScale = LocalUiScale.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FinPetTheme {
            KeepScreenImmersive()
            Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            StardewBoard(
                headerTitle = "ПОСТОЯННЫЕ РАСХОДЫ",
                headerIcon = "🔄"
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy((8.dp * uiScale).coerceIn(5.dp, 8.dp)),
                    contentPadding = PaddingValues(bottom = 6.dp)
                ) {
                    item {
                        Text(
                            text = "Расходы, повторяющиеся регулярно в календаре питомца:",
                            fontSize = (14f * uiScale).coerceIn(11f, 15f).sp,
                            lineHeight = (19f * uiScale).coerceIn(15f, 20f).sp,
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
                                    .padding((10.dp * uiScale).coerceIn(6.dp, 12.dp))
                            ) {
                                Text(
                                    text = "Пока нет регулярных трат. Добавь их кнопкой ниже!",
                                    fontSize = (14f * uiScale).coerceIn(11f, 15f).sp,
                                    lineHeight = (19f * uiScale).coerceIn(15f, 20f).sp,
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
                                    .padding(
                                        horizontal = (8.dp * uiScale).coerceIn(5.dp, 8.dp),
                                        vertical = (6.dp * uiScale).coerceIn(4.dp, 6.dp)
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    RenderBoardIcon(expense.icon, modifier = Modifier.size((18.dp * uiScale).coerceIn(14.dp, 20.dp)))
                                    Spacer(modifier = Modifier.width((6.dp * uiScale).coerceIn(4.dp, 8.dp)))
                                    Column {
                                        Text(
                                            text = expense.title,
                                            fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
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
                                            fontSize = (13f * uiScale).coerceIn(9f, 14f).sp,
                                            color = PixelTextMuted
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CoinIcon(modifier = Modifier.size((14.dp * uiScale).coerceIn(10.dp, 16.dp)))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${expense.cost}",
                                        fontFamily = AppFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                                        color = PixelGoldDark
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { onDeleteRecurring(expense) },
                                        modifier = Modifier.size((32.dp * uiScale).coerceIn(24.dp, 36.dp))
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

                    item {
                        Spacer(modifier = Modifier.height((4.dp * uiScale).coerceIn(2.dp, 4.dp)))
                        PixelButton(
                            text = "Добавить цикличную трату",
                            onClick = onOpenAddRecurring,
                            containerColor = PixelGoldBright,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                            contentPadding = PaddingValues(vertical = (8.dp * uiScale).coerceIn(5.dp, 10.dp), horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height((4.dp * uiScale).coerceIn(2.dp, 4.dp)))
                        PixelButton(
                            text = "Закрыть",
                            onClick = onDismiss,
                            containerColor = PixelParchmentDark,
                            textColor = PixelTextDark,
                            borderColor = PixelParchmentBorder,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                            contentPadding = PaddingValues(vertical = (8.dp * uiScale).coerceIn(5.dp, 10.dp), horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
    }
}

/**
 * Аутентичный пиксельный диалог создания новой цикличной траты.
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
    val uiScale = LocalUiScale.current

    val icons = listOf("🍲", "💧", "💊", "⚽", "📚", "🧼")

    PixelAlertDialog(
        onDismissRequest = onDismiss,
        title = "ЦИКЛИЧНАЯ ТРАТА",
        icon = "🔄",
        confirmButton = {
            PixelButton(
                text = "Сохранить",
                onClick = {
                    val cost = recCost.toIntOrNull() ?: 0
                    if (recTitle.isNotBlank()) {
                        onAddRecurring(recTitle.trim(), cost, recFrequency, recIcon)
                    }
                },
                containerColor = PixelGoldBright,
                modifier = Modifier.fillMaxWidth(),
                fontSize = (15f * uiScale).coerceIn(11f, 16f).sp
            )
        },
        dismissButton = {
            PixelButton(
                text = "Отмена",
                onClick = onDismiss,
                containerColor = PixelParchmentMedium,
                textColor = PixelTextDark,
                modifier = Modifier.fillMaxWidth(),
                fontSize = (15f * uiScale).coerceIn(11f, 16f).sp
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = recTitle,
                onValueChange = { recTitle = it },
                label = { Text("Название регулярного расхода", fontFamily = AppFontFamily, fontSize = (14f * uiScale).coerceIn(10f, 15f).sp) },
                placeholder = { Text("Например: Школьный обед", fontFamily = AppFontFamily, fontSize = (13f * uiScale).coerceIn(10f, 14f).sp) },
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

            Spacer(modifier = Modifier.height((8.dp * uiScale).coerceIn(4.dp, 8.dp)))

            OutlinedTextField(
                value = recCost,
                onValueChange = { recCost = it.filter { ch -> ch.isDigit() } },
                label = { Text("Стоимость (монет)", fontFamily = AppFontFamily, fontSize = (14f * uiScale).coerceIn(10f, 15f).sp) },
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

            Spacer(modifier = Modifier.height((8.dp * uiScale).coerceIn(4.dp, 10.dp)))

            Text(
                text = "Иконка:",
                fontFamily = AppFontFamily,
                fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                fontWeight = FontWeight.Bold,
                color = PixelTextDark
            )

            Spacer(modifier = Modifier.height((5.dp * uiScale).coerceIn(3.dp, 6.dp)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((6.dp * uiScale).coerceIn(4.dp, 8.dp))
            ) {
                icons.forEach { icon ->
                    PixelButton(
                        icon = icon,
                        onClick = { recIcon = icon },
                        containerColor = if (recIcon == icon) PixelGoldBright else PixelParchmentLight,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = (5.dp * uiScale).coerceIn(3.dp, 6.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height((8.dp * uiScale).coerceIn(4.dp, 10.dp)))

            Text(
                text = "Периодичность:",
                fontFamily = AppFontFamily,
                fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                fontWeight = FontWeight.Bold,
                color = PixelTextDark
            )

            Spacer(modifier = Modifier.height((5.dp * uiScale).coerceIn(3.dp, 6.dp)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((6.dp * uiScale).coerceIn(4.dp, 8.dp))
            ) {
                PixelButton(
                    text = "Каждый день",
                    onClick = { recFrequency = 1 },
                    containerColor = if (recFrequency == 1) PixelGoldBright else PixelParchmentLight,
                    modifier = Modifier.weight(1f),
                    fontSize = (14f * uiScale).coerceIn(10f, 15f).sp,
                    contentPadding = PaddingValues(horizontal = (4.dp * uiScale).coerceIn(2.dp, 6.dp), vertical = (6.dp * uiScale).coerceIn(4.dp, 7.dp))
                )
                PixelButton(
                    text = "Раз в 3 дня",
                    onClick = { recFrequency = 3 },
                    containerColor = if (recFrequency == 3) PixelGoldBright else PixelParchmentLight,
                    modifier = Modifier.weight(1f),
                    fontSize = (14f * uiScale).coerceIn(10f, 15f).sp,
                    contentPadding = PaddingValues(horizontal = (4.dp * uiScale).coerceIn(2.dp, 6.dp), vertical = (6.dp * uiScale).coerceIn(4.dp, 7.dp))
                )
            }
        }
    }
}
