package ru.finpet.kids.feature.budget.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.PixelAlertDialog
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted

/**
 * Диалог ручного ввода суммы для конкретной баночки с быстрыми кнопками (0, +10, Все).
 */
@Composable
fun CustomAmountDialog(
    jarTitle: String,
    currentAmount: Int,
    remainder: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val uiScale = LocalUiScale.current
    var customInput by remember(currentAmount) { mutableStateOf(currentAmount.toString()) }
    val focusManager = LocalFocusManager.current
    val maxPossible = (currentAmount + remainder).coerceAtLeast(0)

    PixelAlertDialog(
        onDismissRequest = onDismiss,
        title = "СВОЯ СУММА",
        icon = "empty_jar",
        confirmButton = {
            PixelButton(
                text = "Сохранить",
                onClick = {
                    val entered = customInput.toIntOrNull() ?: 0
                    onConfirm(entered.coerceIn(0, maxPossible))
                },
                containerColor = PixelGoldBright,
                modifier = Modifier.fillMaxWidth(),
                fontSize = (16f * uiScale).coerceIn(12f, 17f).sp
            )
        },
        dismissButton = {
            PixelButton(
                text = "Отмена",
                onClick = onDismiss,
                containerColor = PixelParchmentMedium,
                textColor = PixelTextDark,
                modifier = Modifier.fillMaxWidth(),
                fontSize = (16f * uiScale).coerceIn(12f, 17f).sp
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
                text = "Баночка: $jarTitle",
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (17f * uiScale).coerceIn(13f, 18f).sp,
                color = PixelTextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Свободно: $remainder",
                    fontFamily = AppFontFamily,
                    fontSize = (14f * uiScale).coerceIn(11f, 15f).sp,
                    color = PixelTextMuted
                )
                Spacer(modifier = Modifier.width(3.dp))
                CoinIcon(modifier = Modifier.size((15.dp * uiScale).coerceIn(12.dp, 16.dp)))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(макс.: $maxPossible",
                    fontFamily = AppFontFamily,
                    fontSize = (14f * uiScale).coerceIn(11f, 15f).sp,
                    color = PixelTextMuted
                )
                Spacer(modifier = Modifier.width(3.dp))
                CoinIcon(modifier = Modifier.size((15.dp * uiScale).coerceIn(12.dp, 16.dp)))
                Text(
                    text = ")",
                    fontFamily = AppFontFamily,
                    fontSize = (14f * uiScale).coerceIn(11f, 15f).sp,
                    color = PixelTextMuted
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = customInput,
                onValueChange = { customInput = it.filter { ch -> ch.isDigit() } },
                label = { Text("Монет в баночке", fontFamily = AppFontFamily, fontSize = (15f * uiScale).coerceIn(12f, 16f).sp) },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PixelButton(
                    text = "0",
                    trailingIcon = { CoinIcon(modifier = Modifier.size((13.dp * uiScale).coerceIn(10.dp, 14.dp))) },
                    onClick = { customInput = "0" },
                    modifier = Modifier.weight(1f),
                    fontSize = (13f * uiScale).coerceIn(10f, 14f).sp,
                    containerColor = PixelParchmentLight,
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                )
                PixelButton(
                    text = "+10",
                    trailingIcon = { CoinIcon(modifier = Modifier.size((13.dp * uiScale).coerceIn(10.dp, 14.dp))) },
                    repeating = true,
                    onClick = {
                        val curr = customInput.toIntOrNull() ?: 0
                        val next = (curr + 10).coerceAtMost(maxPossible)
                        customInput = next.toString()
                    },
                    modifier = Modifier.weight(1f),
                    fontSize = (13f * uiScale).coerceIn(10f, 14f).sp,
                    containerColor = PixelParchmentLight,
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                )
                PixelButton(
                    text = "Все ($maxPossible)",
                    onClick = { customInput = maxPossible.toString() },
                    modifier = Modifier.weight(1.5f),
                    fontSize = (13f * uiScale).coerceIn(10f, 14f).sp,
                    containerColor = PixelParchmentLight,
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                )
            }
        }
    }
}
