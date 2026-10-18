package ru.finpet.kids.feature.finik.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.R
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.ExclamationIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.PixelAlertDialog
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.TextSecondary

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
    modifier: Modifier = Modifier
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

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
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ExclamationIcon(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$petName убежал!",
                        fontFamily = AppFontFamily,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = PixelRedBerry
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ExclamationIcon(modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Настроение упало до 0%. Поставь миску корма у двери (цена x2 от обычного корма: 60 монет). Питомец вернётся через 1–3 дня, если ставить миску каждый день!",
                    fontFamily = AppFontFamily,
                    fontSize = 14.sp,
                    color = PixelTextDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

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
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🥣", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Миска с кормом стоит у двери!",
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = FreshGreen
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Кот чувствует заботу. Проверь завтра, вернулся ли он!",
                            fontFamily = AppFontFamily,
                            fontSize = 14.sp,
                            lineHeight = 19.sp,
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
                    text = "Купить корм для питомца",
                    onClick = { showConfirmDialog = true },
                    enabled = canAffordBowl,
                    containerColor = JoyOrange,
                    textColor = PixelTextDark,
                    fontSize = 15.sp,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Text("🥣", fontSize = 18.sp) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "(60 ",
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = if (canAffordBowl) PixelTextDark else Color(0xFF7A6855)
                            )
                            CoinIcon(modifier = Modifier.size(15.dp))
                            Text(
                                text = ")",
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = if (canAffordBowl) PixelTextDark else Color(0xFF7A6855)
                            )
                        }
                    }
                )

                // красный восклицательный знак над кнопкой справа сверху
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-8).dp)
                ) {
                    ExclamationIcon(modifier = Modifier.size(24.dp))
                }
            }

            if (!canAffordBowl) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Не хватает монет (нужно 60 монет). Загляни в Школу или выполни дела по дому!",
                    fontFamily = AppFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = PixelRedBerry,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    // диалог подтверждения покупки корма для сбежавшего питомца
    if (showConfirmDialog) {
        PixelAlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = "Корм для $petName",
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
                    trailingIcon = { CoinIcon(modifier = Modifier.size(16.dp)) }
                )
            },
            dismissButton = {
                PixelButton(
                    text = "Отмена",
                    onClick = { showConfirmDialog = false },
                    containerColor = PixelWoodMedium,
                    textColor = PixelParchmentLight,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "$petName убежал и сейчас прячется. Если купить и поставить миску корма у двери, он почувствует заботу и вернется домой через 1–3 дня!",
                    fontFamily = AppFontFamily,
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    color = PixelTextDark
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Цена покупки: 60 ",
                        fontFamily = AppFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PixelTextDark
                    )
                    CoinIcon(modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
