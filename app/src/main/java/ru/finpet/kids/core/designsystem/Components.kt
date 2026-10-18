package ru.finpet.kids.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.R

@Composable
fun LockIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.lock_emodji),
        contentDescription = "Замок",
        modifier = modifier
    )
}

@Composable
fun TargetIcon(
    modifier: Modifier = Modifier.size(18.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.target_emodji),
        contentDescription = "Цель",
        modifier = modifier
    )
}

/**
 * Интерактивная банка с пиксельным наполнением.
 * Высота заполнения: 40px при 64x64 (10 делений по 4px).
 * fullfill_the_jar.png: 42x4px, цвет 0xFFD95763.
 *
 * @param jarRes ресурс банки (R.drawable.life_jar, R.drawable.joy_jar, R.drawable.saving_jar)
 * @param fillDivisions уровень наполнения от 0 до 10 делений
 */
@Composable
fun PixelJar(
    jarRes: Int,
    fillDivisions: Int,
    modifier: Modifier = Modifier.size(64.dp)
) {
    val clamped = fillDivisions.coerceIn(0, 10)
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / 64f
            val scaleY = size.height / 64f
            if (clamped > 0) {
                val fillLeft = 11f * scaleX
                val fillWidth = 42f * scaleX
                val fillHeight = (clamped * 4f) * scaleY
                val fillTop = (57f - clamped * 4f) * scaleY
                drawRect(
                    color = Color(0xFFD95763),
                    topLeft = Offset(fillLeft, fillTop),
                    size = Size(fillWidth, fillHeight)
                )
            }
        }
        Image(
            painter = painterResource(id = jarRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
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

/**
 * Текст, в котором эмодзи 🪙 автоматически заменяются на нарисованную пиксельную монетку CoinIcon
 */
@Composable
fun RichFeedbackText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    color: Color = TextPrimary,
    fontWeight: FontWeight = FontWeight.Normal,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign = TextAlign.Center,
    lineHeight: TextUnit = 21.sp
) {
    val coinPlaceholderId = "DRAWN_COIN"
    val annotatedString = buildAnnotatedString {
        val parts = text.split("🪙")
        for (i in parts.indices) {
            append(parts[i])
            if (i < parts.size - 1) {
                appendInlineContent(coinPlaceholderId, "[coin]")
            }
        }
    }

    val iconSize = (fontSize.value * 1.15f).sp
    val inlineContent = mapOf(
        coinPlaceholderId to InlineTextContent(
            Placeholder(
                width = iconSize,
                height = iconSize,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            CoinIcon(modifier = Modifier.fillMaxSize())
        }
    )

    Text(
        text = annotatedString,
        inlineContent = inlineContent,
        fontSize = fontSize,
        color = color,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        textAlign = textAlign,
        lineHeight = lineHeight,
        modifier = modifier
    )
}