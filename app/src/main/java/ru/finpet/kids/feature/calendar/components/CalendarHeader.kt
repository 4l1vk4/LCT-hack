package ru.finpet.kids.feature.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldGlow
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.PixelWoodDark
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.feature.calendar.CalendarMonthInfo

/**
 * Верхняя информационная карточка календаря.
 */
@Composable
fun CalendarHeader(
    currentDayIndex: Int,
    balance: Int,
    monthInfo: CalendarMonthInfo,
    onOpenRecurring: () -> Unit,
    modifier: Modifier = Modifier
) {
    StardewBoard(
        headerTitle = "КАЛЕНДАРЬ РАСХОДОВ",
        headerIcon = "📅",
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ДЕНЬ $currentDayIndex ИЗ ${monthInfo.daysInMonth}",
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = PixelTextDark
                )
                Text(
                    text = "${monthInfo.monthName} ${monthInfo.year}",
                    fontSize = 15.sp,
                    color = PixelTextMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Кнопка регулярных трат
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PixelParchmentMedium)
                        .border(1.dp, PixelParchmentBorder, RoundedCornerShape(4.dp))
                        .clickable(onClick = onOpenRecurring)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🔄 Траты",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextDark,
                        fontFamily = AppFontFamily
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Бейдж баланса монет
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PixelWoodDark)
                        .padding(1.dp)
                        .border(1.dp, PixelGoldBright, RoundedCornerShape(3.dp))
                        .background(PixelGoldGlow)
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CoinIcon(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$balance",
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = PixelTextDark,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
