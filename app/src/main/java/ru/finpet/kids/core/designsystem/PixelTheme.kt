package ru.finpet.kids.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
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

val PixelTextDark = Color(0xFF26150B)
val PixelTextMuted = Color(0xFF4A3423)
val PixelGreenCrop = Color(0xFF2E7D32)
val PixelRedBerry = Color(0xFFC62828)
val PixelBlueWater = Color(0xFF1565C0)

/**
 * Резная пиксельная доска календаря и панелей на основе top_part, middle_part, bottom_part
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
            .pixelPanelBackground()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            if (headerTitle != null) {
                // Пиксельная плашка заголовка на основе left_part, middle_little_part, right_part
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pixelBarBackground()
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        RenderBoardIcon(headerIcon)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = headerTitle,
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = PixelTextDark,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        RenderBoardIcon(headerIcon)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            content()
        }
    }
}

@Composable
fun RenderBoardIcon(icon: String, modifier: Modifier = Modifier.size(26.dp)) {
    when (icon) {
        "📅" -> CalendarIcon(modifier = modifier)
        "📊" -> DiagramIcon(modifier = modifier)
        "⚠️" -> WarningIcon(modifier = modifier)
        "⚡" -> LightningIcon(modifier = modifier)
        "❤️" -> HeartIcon(modifier = modifier)
        "🛡️" -> ShieldIcon(modifier = modifier)
        "🍎" -> AppleIcon(modifier = modifier)
        "❗", "!" -> ExclamationIcon(modifier = modifier)
        else -> Text(text = icon, fontSize = 26.sp)
    }
}


// Пиксельная кнопка на основе button_left_part, button_middle_little_part, button_right_part.
@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = PixelGoldBright,
    textColor: Color = PixelTextDark,
    borderColor: Color = PixelGoldDark,
    enabled: Boolean = true,
    fontSize: TextUnit = 16.sp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val actualBg = if (enabled) containerColor else Color(0xFFC0B098)
    val actualText = if (enabled) textColor else Color(0xFF7A6855)

    Box(
        modifier = modifier
            .pixelButtonBackground(containerColor = actualBg)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontFamily = AppFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                color = actualText,
                textAlign = TextAlign.Center
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(6.dp))
                trailingIcon()
            }
        }
    }
}

/**
 * Аутентичный пиксельный диалог в стиле Stardew Valley.
 */
@Composable
fun PixelAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    icon: String = "⚠️",
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            StardewBoard(
                headerTitle = title,
                headerIcon = icon,
                modifier = modifier
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    content()
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (dismissButton != null) {
                            Box(modifier = Modifier.weight(1f)) {
                                dismissButton()
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            confirmButton()
                        }
                    }
                }
            }
        }
    }
}
