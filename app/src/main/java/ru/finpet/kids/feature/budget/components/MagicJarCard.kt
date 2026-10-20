package ru.finpet.kids.feature.budget.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelJar
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.pixelButtonBackground
import ru.finpet.kids.core.designsystem.pixelPanelBackground
import kotlin.math.roundToInt

/**
 * Интерактивная карточка баночки бюджета с индикатором заполнения и кнопками быстрых сумм (-10, +10, +20, Своя).
 */
@Composable
fun MagicJarCard(
    jarRes: Int = 0,
    icon: String,
    title: String,
    categoryLabel: String,
    subtitle: String,
    amount: Int,
    walletBalance: Int,
    color: Color,
    remainder: Int,
    isReadOnly: Boolean = false,
    onMinus: () -> Unit,
    onPlus10: () -> Unit,
    onPlus20: () -> Unit,
    onSetAmount: (Int) -> Unit
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    if (showCustomDialog) {
        CustomAmountDialog(
            jarTitle = title,
            currentAmount = amount,
            remainder = remainder,
            onConfirm = { newAmt ->
                onSetAmount(newAmt)
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false }
        )
    }

    val uiScale = LocalUiScale.current
    val fillRatio = if (walletBalance > 0) {
        (amount.toFloat() / walletBalance.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val fillPercent = (fillRatio * 100).toInt()
    val fillDivisions = (fillRatio * 10f).roundToInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pixelPanelBackground()
            .padding((10.dp * uiScale).coerceIn(6.dp, 10.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (jarRes != 0) {
                PixelJar(
                    jarRes = jarRes,
                    fillDivisions = fillDivisions,
                    modifier = Modifier.size((44.dp * uiScale).coerceIn(32.dp, 46.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size((36.dp * uiScale).coerceIn(28.dp, 38.dp))
                        .pixelButtonBackground(containerColor = color.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = (18f * uiScale).coerceIn(14f, 20f).sp)
                }
            }
            Spacer(modifier = Modifier.width((6.dp * uiScale).coerceIn(4.dp, 8.dp)))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = (17f * uiScale).coerceIn(13f, 18f).sp,
                    color = PixelTextDark,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = (14f * uiScale).coerceIn(10f, 16f).sp,
                    color = PixelTextMuted,
                    lineHeight = (18f * uiScale).coerceIn(13f, 21f).sp,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.width((6.dp * uiScale).coerceIn(4.dp, 6.dp)))
            // текущая сумма в баночке: фиксированный размер квадратика (не раздувается), шрифт масштабируется от длины числа
            val digitsCount = "$amount".length
            val amountFontSize = when {
                digitsCount <= 2 -> (16f * uiScale).coerceIn(12f, 17f).sp
                digitsCount == 3 -> (13.5f * uiScale).coerceIn(10.5f, 14.5f).sp
                digitsCount == 4 -> (11.5f * uiScale).coerceIn(9f, 12.5f).sp
                else -> (9.5f * uiScale).coerceIn(8f, 10.5f).sp
            }
            val badgeCoinSize = when {
                digitsCount <= 2 -> (15.dp * uiScale).coerceIn(12.dp, 17.dp)
                digitsCount == 3 -> (14.dp * uiScale).coerceIn(11.dp, 15.dp)
                digitsCount == 4 -> (13.dp * uiScale).coerceIn(10.dp, 14.dp)
                else -> (12.dp * uiScale).coerceIn(9.dp, 13.dp)
            }
            val badgeWidth = (56.dp * uiScale).coerceIn(48.dp, 64.dp)
            val badgeHeight = (34.dp * uiScale).coerceIn(28.dp, 38.dp)

            Box(
                modifier = Modifier
                    .size(width = badgeWidth, height = badgeHeight)
                    .pixelButtonBackground(containerColor = PixelParchmentLight)
                    .clickable(enabled = !isReadOnly) {
                        showCustomDialog = true
                    }
                    .padding(
                        horizontal = (3.dp * uiScale).coerceIn(2.dp, 5.dp),
                        vertical = (3.dp * uiScale).coerceIn(2.dp, 5.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$amount",
                        fontWeight = FontWeight.ExtraBold,
                        color = color,
                        fontSize = amountFontSize,
                        fontFamily = AppFontFamily,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width((2.dp * uiScale).coerceIn(1.dp, 3.dp)))
                    CoinIcon(modifier = Modifier.size(badgeCoinSize))
                }
            }
        }

        Spacer(modifier = Modifier.height((6.dp * uiScale).coerceIn(4.dp, 8.dp)))

        // шкала наполнения баночки относительно всех денег в кошельке и бейдж категории
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .pixelButtonBackground(containerColor = color.copy(alpha = 0.35f))
                        .padding(
                            horizontal = (6.dp * uiScale).coerceIn(4.dp, 8.dp),
                            vertical = (2.dp * uiScale).coerceIn(2.dp, 4.dp)
                        )
                ) {
                    Text(
                        text = categoryLabel,
                        fontSize = (12f * uiScale).coerceIn(9f, 13f).sp,
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextDark
                    )
                }
                Text(
                    text = "$fillPercent% от кошелька",
                    fontSize = (13f * uiScale).coerceIn(10f, 15f).sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontFamily = AppFontFamily
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(PixelParchmentBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fillRatio.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }
        }

        if (!isReadOnly) {
            Spacer(modifier = Modifier.height((6.dp * uiScale).coerceIn(4.dp, 8.dp)))

            // кнопки быстрого добавления (-10, +10, +20, Своя) на всю ширину
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((4.dp * uiScale).coerceIn(2.dp, 4.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PixelButton(
                    text = "-10",
                    onClick = onMinus,
                    enabled = amount >= 10,
                    repeating = true,
                    containerColor = PixelParchmentMedium,
                    textColor = PixelTextDark,
                    fontSize = (13f * uiScale).coerceIn(10f, 13f).sp,
                    contentPadding = PaddingValues(
                        horizontal = (4.dp * uiScale).coerceIn(2.dp, 6.dp),
                        vertical = (4.dp * uiScale).coerceIn(2.dp, 4.dp)
                    ),
                    modifier = Modifier.weight(1f)
                )
                PixelButton(
                    text = "+10",
                    onClick = onPlus10,
                    enabled = remainder >= 10,
                    repeating = true,
                    containerColor = color,
                    textColor = Color.White,
                    fontSize = (13f * uiScale).coerceIn(10f, 13f).sp,
                    contentPadding = PaddingValues(
                        horizontal = (4.dp * uiScale).coerceIn(2.dp, 6.dp),
                        vertical = (4.dp * uiScale).coerceIn(2.dp, 4.dp)
                    ),
                    modifier = Modifier.weight(1f)
                )
                PixelButton(
                    text = "+20",
                    onClick = onPlus20,
                    enabled = remainder >= 20,
                    repeating = true,
                    containerColor = color,
                    textColor = Color.White,
                    fontSize = (13f * uiScale).coerceIn(10f, 13f).sp,
                    contentPadding = PaddingValues(
                        horizontal = (4.dp * uiScale).coerceIn(2.dp, 6.dp),
                        vertical = (4.dp * uiScale).coerceIn(2.dp, 4.dp)
                    ),
                    modifier = Modifier.weight(1f)
                )
                PixelButton(
                    text = "Своя",
                    onClick = {
                        showCustomDialog = true
                    },
                    containerColor = PixelParchmentLight,
                    textColor = PixelTextDark,
                    fontSize = (12f * uiScale).coerceIn(9f, 12f).sp,
                    contentPadding = PaddingValues(
                        horizontal = (4.dp * uiScale).coerceIn(2.dp, 6.dp),
                        vertical = (4.dp * uiScale).coerceIn(2.dp, 4.dp)
                    ),
                    modifier = Modifier.weight(1.1f)
                )
            }
        }
    }
}
