package ru.finpet.kids.feature.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import androidx.compose.ui.text.font.FontWeight
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.PawsIcon
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelTextDark

/**
 * Легенда обозначений ячеек в календаре.
 */
@Composable
fun CalendarLegend(
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        PawsIcon(modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text("Сегодня", fontSize = 14.sp, fontFamily = AppFontFamily, color = PixelTextDark)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PixelGreenCrop)
        Spacer(modifier = Modifier.width(3.dp))
        Text("Прошедшие", fontSize = 14.sp, fontFamily = AppFontFamily, color = PixelTextDark)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Black, color = JoyOrange)
        Spacer(modifier = Modifier.width(3.dp))
        Text("Все задачи", fontSize = 14.sp, fontFamily = AppFontFamily, color = PixelTextDark)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        CoinIcon(modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text("Выплата карманных", fontSize = 14.sp, fontFamily = AppFontFamily, color = PixelTextDark)
    }
}
