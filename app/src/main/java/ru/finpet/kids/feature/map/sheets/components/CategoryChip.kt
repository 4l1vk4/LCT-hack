package ru.finpet.kids.feature.map.sheets.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.pixelButtonBackground

/**
 * Пиксельная кнопка-вкладка для переключения фильтров категорий в магазине
 * на основе button_* 9-slice.
 */
@Composable
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val uiScale = LocalUiScale.current
    Box(
        modifier = modifier
            .pixelButtonBackground(containerColor = if (isSelected) PixelGoldBright else PixelParchmentLight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = (8.dp * uiScale).coerceIn(4.dp, 12.dp),
                vertical = (5.dp * uiScale).coerceIn(3.dp, 7.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = AppFontFamily,
            fontSize = (15f * uiScale).coerceIn(10f, 16f).sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isSelected) PixelTextDark else PixelWoodDark,
            textAlign = TextAlign.Center
        )
    }
}
