package ru.finpet.kids.feature.finik.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
            .padding(vertical = 12.dp)
    ) {
        // изображение сбежавшего котика (облачко пыли после побега)
        Image(
            painter = painterResource(id = R.drawable.cat_runaway),
            contentDescription = "$petName убежал",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(190.dp)
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "$petName убежал!",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFC62828)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Настроение упало до 0%. Поставь миску корма у двери (цена x2 от обычного корма: 60 монет). Питомец вернётся через 1–3 дня, если ставить миску каждый день!",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (bowlPlacedToday) {
            // миска уже поставлена сегодня
            FinCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                backgroundColor = Color(0xFFE8F5E9),
                borderColor = Color(0xFFAED581)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🥣", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Миска с кормом стоит у двери!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = FreshGreen
                        )
                        Text(
                            text = "Кот чувствует заботу. Проверь завтра, вернулся ли он!",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            // кнопка купить корм для питомца с восклицательным знаком над ней
            val canAffordBowl = balance >= 60
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Button(
                    onClick = { showConfirmDialog = true },
                    enabled = canAffordBowl,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JoyOrange,
                        disabledContainerColor = Color(0xFFCCCCCC)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🥣 Купить корм для питомца (60 ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        CoinIcon(modifier = Modifier.size(16.dp))
                        Text(
                            text = ")",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }

                // красный восклицательный знак над кнопкой справа сверху
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-10).dp)
                ) {
                    ExclamationIcon(modifier = Modifier.size(24.dp))
                }
            }

            if (!canAffordBowl) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Не хватает монет (нужно 60 монет). Загляни в Школу или выполни дела по дому!",
                    fontSize = 12.sp,
                    color = Color(0xFFD32F2F),
                    textAlign = TextAlign.Center
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
                    fontSize = 15.sp,
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
