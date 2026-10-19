package ru.finpet.kids.feature.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.DocsIcon
import ru.finpet.kids.core.designsystem.PawsIcon
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.PixelWoodDark

/**
 * Ячейка отдельного дня в сетке календаря Stardew Valley.
 */
@Composable
fun CalendarDayCell(
    dayNumber: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isPast: Boolean,
    hasRecurring: Boolean,
    hasNotes: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cellBg = when {
        isToday -> Color(0xFFFFF1C6)
        isSelected -> Color(0xFFFFECC7)
        isPast -> Color(0xFFEBE0C7)
        else -> PixelParchmentLight
    }

    val cellBorder = when {
        isToday -> PixelGoldBright
        isSelected -> PixelWoodDark
        else -> PixelParchmentBorder
    }

    val borderWidth = when {
        isToday -> 2.dp
        isSelected -> 2.dp
        else -> 1.dp
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(cellBg)
            .border(borderWidth, cellBorder, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(3.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Номер дня и статусная отметка
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$dayNumber",
                    fontFamily = AppFontFamily,
                    fontWeight = if (isToday) FontWeight.Black else FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isToday) PixelGoldDark else if (isPast) PixelTextMuted else PixelTextDark
                )
                if (isToday) {
                    PawsIcon(modifier = Modifier.size(13.dp))
                } else if (isPast) {
                    Text(text = "✓", fontSize = 13.sp, color = PixelGreenCrop, fontWeight = FontWeight.Bold)
                }
            }

            // Иконки событий/трат в этот день
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (hasRecurring) {
                    CoinIcon(modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                }
                if (hasNotes) {
                    DocsIcon(modifier = Modifier.size(13.dp))
                }
            }

            // Нижняя золотая полоска статуса сегодняшнего дня
            if (isToday) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(PixelGoldBright)
                )
            } else {
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }
}
