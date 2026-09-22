package ru.finpet.kids.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Цвета Stardew Valley фермерского календаря
val PixelWoodDark = Color(0xFF351A0C)
val PixelWoodMedium = Color(0xFF6B3C1A)
val PixelWoodLight = Color(0xFF9E5C2D)
val PixelWoodBevel = Color(0xFFBD7B48)

val PixelParchmentLight = Color(0xFFFFF8E7)
val PixelParchmentMedium = Color(0xFFF5E4BE)
val PixelParchmentDark = Color(0xFFDFC490)
val PixelParchmentBorder = Color(0xFFC7A86E)

val PixelGoldBright = Color(0xFFFFC107)
val PixelGoldGlow = Color(0xFFFFD54F)
val PixelGoldDark = Color(0xFFC67D00)

val PixelTextDark = Color(0xFF381F0E)
val PixelTextMuted = Color(0xFF7A5839)
val PixelGreenCrop = Color(0xFF2E7D32)
val PixelRedBerry = Color(0xFFC62828)
val PixelBlueWater = Color(0xFF1565C0)

/**
 * Деревянная резная доска календаря в стиле Stardew Valley
 */
@Composable
fun StardewBoard(
    modifier: Modifier = Modifier,
    headerTitle: String? = null,
    headerIcon: String = "📅",
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(PixelWoodDark)
            .padding(4.dp)
            .border(2.dp, PixelWoodBevel, RoundedCornerShape(6.dp))
            .background(PixelWoodMedium)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(PixelParchmentLight)
                .border(2.dp, PixelParchmentBorder, RoundedCornerShape(4.dp))
                .padding(10.dp)
        ) {
            if (headerTitle != null) {
                // Деревянная плашка заголовка
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(PixelWoodDark)
                        .padding(2.dp)
                        .border(1.dp, PixelGoldBright, RoundedCornerShape(3.dp))
                        .background(PixelWoodMedium)
                        .padding(vertical = 6.dp, horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = headerIcon, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = headerTitle,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = PixelGoldBright,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = headerIcon, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            content()
        }
    }
}

/**
 * Пиксельная кнопка в стиле 16-битных RPG / Stardew Valley
 */
@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = PixelGoldBright,
    textColor: Color = PixelTextDark,
    borderColor: Color = PixelGoldDark,
    enabled: Boolean = true
) {
    val actualBg = if (enabled) containerColor else Color(0xFFCCCCCC)
    val actualBorder = if (enabled) borderColor else Color(0xFF999999)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(actualBorder)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(bottom = 3.dp) // Эффект 3D-фаски
            .background(actualBg)
            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = if (enabled) textColor else Color(0xFF666666),
            textAlign = TextAlign.Center
        )
    }
}
