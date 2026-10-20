package ru.finpet.kids.feature.finik.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.ExclamationIcon
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.MiskaIcon
import ru.finpet.kids.core.designsystem.PixelAlertDialog
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium

import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.PixelRedBerry

// блок, когда котик убежал из дома (настроение упало до 0%)
// ребенку нужно поставить миску у двери (цена 60 монет), чтобы котик вернулся
@Composable
fun FinikRunawaySection(
    petName: String,
    bowlPlacedToday: Boolean,
    balance: Int,
    onPlaceFoodBowl: () -> Unit,
    onNavigateToHomeChores: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    val uiScale = LocalUiScale.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Плашка с информацией о побеге в стиле 9-slice PixelPanel
        PixelPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = (14.dp * uiScale).coerceIn(8.dp, 16.dp),
                        vertical = (10.dp * uiScale).coerceIn(6.dp, 12.dp)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$petName загрустил и ушел погулять...",
                    fontFamily = AppFontFamily,
                    fontSize = (17f * uiScale).coerceIn(13f, 20f).sp,
                    fontWeight = FontWeight.Black,
                    color = PixelRedBerry,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height((5.dp * uiScale).coerceIn(3.dp, 7.dp)))

                Text(
                    text = "Настроение упало до 0%. Поставь миску его любимых вкусняшек у двери (цена x2 от обычного корма: 60 монет). Питомец вернётся через 1–3 дня, если ставить миску каждый день!",
                    fontFamily = AppFontFamily,
                    fontSize = (13f * uiScale).coerceIn(10f, 15f).sp,
                    color = PixelTextDark,
                    textAlign = TextAlign.Center,
                    lineHeight = (18f * uiScale).coerceIn(14f, 20f).sp
                )
            }
        }

        Spacer(modifier = Modifier.height((5.dp * uiScale).coerceIn(3.dp, 7.dp)))

        if (bowlPlacedToday) {
            // миска уже поставлена сегодня — 9-slice PixelPanel
            PixelPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = (14.dp * uiScale).coerceIn(8.dp, 16.dp),
                            vertical = (10.dp * uiScale).coerceIn(6.dp, 12.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Миска с кормом стоит у двери!",
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (15f * uiScale).coerceIn(11f, 17f).sp,
                            color = FreshGreen
                        )
                        Spacer(modifier = Modifier.height((2.dp * uiScale).coerceIn(1.dp, 3.dp)))
                        Text(
                            text = "Кот чувствует заботу. Проверь завтра, вернулся ли он!",
                            fontFamily = AppFontFamily,
                            fontSize = (13f * uiScale).coerceIn(10f, 15f).sp,
                            lineHeight = (18f * uiScale).coerceIn(14f, 20f).sp,
                            color = PixelTextDark
                        )
                    }
                }
            }
        } else {
            // кнопка купить корм для питомца в стиле 9-slice PixelButton
            val canAffordBowl = balance >= 60
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                PixelButton(
                    text = "Купить вкусняшек для питомца",
                    onClick = { showConfirmDialog = true },
                    enabled = canAffordBowl,
                    containerColor = JoyOrange,
                    textColor = PixelTextDark,
                    fontSize = (14f * uiScale).coerceIn(11f, 16f).sp,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = (12.dp * uiScale).coerceIn(8.dp, 14.dp),
                        vertical = (8.dp * uiScale).coerceIn(5.dp, 10.dp)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    icon = "🥣"
                )

                // красный восклицательный знак над кнопкой справа сверху
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-8).dp)
                ) {
                    ExclamationIcon(modifier = Modifier.size((22.dp * uiScale).coerceIn(16.dp, 26.dp)))
                }
            }

            if (!canAffordBowl) {
                Spacer(modifier = Modifier.height((4.dp * uiScale).coerceIn(2.dp, 5.dp)))
                Text(
                    text = "Не хватает монет (нужно 60 монет). Загляни в домик и выполни задания по дому!",
                    fontFamily = AppFontFamily,
                    fontSize = (13f * uiScale).coerceIn(10f, 15f).sp,
                    lineHeight = (18f * uiScale).coerceIn(14f, 20f).sp,
                    color = PixelRedBerry,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = (16.dp * uiScale).coerceIn(10.dp, 18.dp))
                )
                if (onNavigateToHomeChores != null) {
                    Spacer(modifier = Modifier.height((5.dp * uiScale).coerceIn(3.dp, 7.dp)))
                    PixelButton(
                        text = "В Домик к заданиям",
                        icon = "🏡",
                        onClick = onNavigateToHomeChores,
                        containerColor = JoyOrange,
                        textColor = PixelTextDark,
                        fontSize = (14f * uiScale).coerceIn(11f, 16f).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = (8.dp * uiScale).coerceIn(4.dp, 10.dp))
                    )
                }
            }
        }
    }

    // диалог подтверждения покупки корма для сбежавшего питомца
    if (showConfirmDialog) {
        val uiScale2 = LocalUiScale.current
        PixelAlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = "Корм для питомца",
            icon = "🥣",
            confirmButton = {
                PixelButton(
                    text = "Купить корм",
                    onClick = {
                        showConfirmDialog = false
                        onPlaceFoodBowl()
                    },
                    containerColor = JoyOrange,
                    textColor = PixelTextDark,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = (15f * uiScale2).coerceIn(11f, 16f).sp,
                    trailingIcon = { CoinIcon(modifier = Modifier.size((15.dp * uiScale2).coerceIn(11.dp, 18.dp))) }
                )
            },
            dismissButton = {
                PixelButton(
                    text = "Отмена",
                    onClick = { showConfirmDialog = false },
                    containerColor = PixelWoodMedium,
                    textColor = PixelParchmentLight,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = (15f * uiScale2).coerceIn(11f, 16f).sp
                )
            }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "$petName сейчас гуляет. Если купить и поставить миску корма у двери, он почувствует будет довольным и вернется домой через 1–3 дня!",
                    fontFamily = AppFontFamily,
                    fontSize = (15f * uiScale2).coerceIn(11f, 16f).sp,
                    lineHeight = (20f * uiScale2).coerceIn(15f, 22f).sp,
                    color = PixelTextDark
                )
                Spacer(modifier = Modifier.height((8.dp * uiScale2).coerceIn(5.dp, 12.dp)))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Цена покупки: 60 ",
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (15f * uiScale2).coerceIn(11f, 16f).sp,
                        color = PixelTextDark
                    )
                    CoinIcon(modifier = Modifier.size((16.dp * uiScale2).coerceIn(12.dp, 20.dp)))
                }
            }
        }
    }
}
