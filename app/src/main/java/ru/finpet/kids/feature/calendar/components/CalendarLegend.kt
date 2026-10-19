package ru.finpet.kids.feature.calendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.PawsIcon
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelTextDark

/**
 * Легенда обозначений ячеек в календаре.
 */
@Composable
fun CalendarLegend(
    modifier: Modifier = Modifier
) {
    val uiScale = LocalUiScale.current
    val iconSize = (12.dp * uiScale).coerceIn(9.dp, 14.dp)
    val textFontSize = (11f * uiScale).coerceIn(8f, 13f).sp
    val spacerWidth = (2.dp * uiScale).coerceIn(1.dp, 3.dp)

    Row(verticalAlignment = Alignment.CenterVertically) {
        PawsIcon(modifier = Modifier.size(iconSize))
        Spacer(modifier = Modifier.width(spacerWidth))
        Text("Сегодня", fontSize = textFontSize, fontFamily = AppFontFamily, color = PixelTextDark, maxLines = 1)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("✓", fontSize = textFontSize, fontWeight = FontWeight.Bold, color = PixelGreenCrop)
        Spacer(modifier = Modifier.width(spacerWidth))
        Text("Прошедшие", fontSize = textFontSize, fontFamily = AppFontFamily, color = PixelTextDark, maxLines = 1)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("✓", fontSize = textFontSize, fontWeight = FontWeight.Black, color = JoyOrange)
        Spacer(modifier = Modifier.width(spacerWidth))
        Text("Все задачи", fontSize = textFontSize, fontFamily = AppFontFamily, color = PixelTextDark, maxLines = 1)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        CoinIcon(modifier = Modifier.size(iconSize))
        Spacer(modifier = Modifier.width(spacerWidth))
        Text("Карманные", fontSize = textFontSize, fontFamily = AppFontFamily, color = PixelTextDark, maxLines = 1)
    }
}
