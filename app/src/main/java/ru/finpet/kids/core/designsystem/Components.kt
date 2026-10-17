package ru.finpet.kids.core.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import ru.finpet.kids.R
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.text.font.FontFamily

@Composable
fun FinButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = JoyOrange,
    contentColor: Color = PixelTextDark,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 9.dp),
    icon: String? = null
) {
    val actualBg = if (enabled) containerColor else Color(0xFFCCCCCC)
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
            if (icon != null) {
                Text(text = icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) contentColor else Color(0xFF7A5839).copy(alpha = 0.5f),
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CoinIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.ic_coin),
        contentDescription = "Монета",
        modifier = modifier
    )
}

@Composable
fun CalendarIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.calender_emodji),
        contentDescription = "Календарь",
        modifier = modifier
    )
}

@Composable
fun HeartIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.heart_emodji),
        contentDescription = "Здоровье",
        modifier = modifier
    )
}

@Composable
fun WarningIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.warning_sign_emodji),
        contentDescription = "Внимание",
        modifier = modifier
    )
}

@Composable
fun LightningIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.lightning_emodji),
        contentDescription = "Энергия",
        modifier = modifier
    )
}

@Composable
fun DiagramIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.diagramm_emodji),
        contentDescription = "Диаграмма",
        modifier = modifier
    )
}

@Composable
fun ExclamationIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.exclimation_sign_emodji),
        contentDescription = "Важно",
        modifier = modifier
    )
}

@Composable
fun ShieldIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.shield_emodji),
        contentDescription = "Родительский режим",
        modifier = modifier
    )
}

@Composable
fun AppleIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.apple_emodji),
        contentDescription = "Сытость",
        modifier = modifier
    )
}

@Composable
fun CoinBadge(
    coins: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFF3E0))
            .border(2.dp, Color(0xFFFFB74D), RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        CoinIcon(modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$coins",
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFE65100)
        )
    }
}

@Composable
fun StatBar(
    label: String,
    icon: String,
    value: Int, // 0..100
    barColor: Color,
    modifier: Modifier = Modifier
) {
    // tween вместо spring по умолчанию: фиксированное число кадров,
    // без осцилляций и лишних перерисовок
    val animatedProgress by animateFloatAsState(
        targetValue = (value.coerceIn(0, 100) / 100f),
        animationSpec = tween(durationMillis = 500),
        label = "stat_anim"
    )
    // Шейдер градиента создаём один раз на цвет, а не на каждый кадр анимации
    val barBrush = remember(barColor) {
        Brush.horizontalGradient(
            listOf(barColor.copy(alpha = 0.8f), barColor)
        )
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Text(
                text = "$value%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFEEEEEE))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(6.dp))
                    .background(barBrush)
            )
        }
    }
}

/**
 * Панель карточки в пиксельном стиле на основе top_part, middle_part, bottom_part
 */
@Composable
fun FinCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = CardSurface,
    borderColor: Color = Color(0xFFEEEEEE),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .pixelPanelBackground()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        content()
    }
}

/**
 * Текст с монеткой в конце: "150 🪙" → [150] [coin-icon]
 */
@Composable
fun CoinText(
    fontSize: TextUnit = 14.sp,
    color: Color = TextPrimary,
    fontWeight: FontWeight = FontWeight.Bold,
    fontFamily: FontFamily? = null,
    prefix: String = "",
    suffix: String = "",
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = "$prefix$suffix",
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            color = color
        )
        Spacer(modifier = Modifier.width(3.dp))
        CoinIcon(modifier = Modifier.size((fontSize.value * 1.15f).dp.coerceIn(12.dp, 24.dp)))
    }
}