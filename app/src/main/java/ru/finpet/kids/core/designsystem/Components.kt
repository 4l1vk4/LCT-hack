package ru.finpet.kids.core.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.R


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
    fontSize: TextUnit = 16.sp,
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