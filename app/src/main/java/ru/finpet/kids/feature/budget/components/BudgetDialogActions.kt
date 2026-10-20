package ru.finpet.kids.feature.budget.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.WarningIcon

/**
 * Нижний блок кнопок действий в диалоге планирования бюджета (закрытие, подтверждение или режим просмотра).
 */
@Composable
fun BudgetDialogActions(
    isConfirmed: Boolean,
    isOverBudget: Boolean,
    isValid: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiScale = LocalUiScale.current

    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(4.dp))
        if (isConfirmed) {
            PixelButton(
                text = "✓ Понятно",
                onClick = onDismiss,
                containerColor = PixelGreenCrop,
                textColor = Color.White,
                borderColor = Color(0xFF1B5E20),
                modifier = Modifier.fillMaxWidth(),
                fontSize = (16f * uiScale).coerceIn(12f, 17f).sp
            )
        } else {
            val confirmText = when {
                isOverBudget -> "Перерасход"
                uiScale < 0.85f -> "Утвердить"
                else -> "📜 Утвердить план"
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((6.dp * uiScale).coerceIn(4.dp, 8.dp))
            ) {
                PixelButton(
                    text = "Закрыть",
                    onClick = onDismiss,
                    containerColor = PixelWoodMedium,
                    textColor = PixelParchmentLight,
                    borderColor = PixelWoodDark,
                    modifier = Modifier.weight(1f),
                    fontSize = (15f * uiScale).coerceIn(11f, 17f).sp
                )
                PixelButton(
                    text = confirmText,
                    leadingIcon = if (isOverBudget) {
                        { WarningIcon(modifier = Modifier.size((16.dp * uiScale).coerceIn(12.dp, 16.dp))) }
                    } else null,
                    onClick = {
                        if (isValid) {
                            onConfirm()
                        }
                    },
                    enabled = isValid,
                    containerColor = if (isValid) PixelGreenCrop else Color(0xFFB0BEC5),
                    textColor = Color.White,
                    borderColor = if (isValid) Color(0xFF1B5E20) else Color(0xFF78909C),
                    modifier = Modifier.weight(1.6f),
                    fontSize = (15f * uiScale).coerceIn(11f, 17f).sp
                )
            }
        }
    }
}
