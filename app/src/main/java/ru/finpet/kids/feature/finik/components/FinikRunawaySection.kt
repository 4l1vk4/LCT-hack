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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.TextSecondary

// блок, когда котик убежал из дома (настроение упало до 0%)
// ребенку нужно поставить миску у двери (цена 60 монет), чтобы котик вернулся
@Composable
fun FinikRunawaySection(
    bowlPlacedToday: Boolean,
    balance: Int,
    onPlaceFoodBowl: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // изображение сбежавшего котика (облачко пыли после побега)
        Image(
            painter = painterResource(id = R.drawable.cat_runaway),
            contentDescription = "Кот убежал",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(190.dp)
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Кот убежал!",
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
            // кнопка выставить миску корма
            val canAffordBowl = balance >= 60
            Button(
                onClick = onPlaceFoodBowl,
                enabled = canAffordBowl,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JoyOrange,
                    disabledContainerColor = Color(0xFFCCCCCC)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🥣 Поставить миску корма (60 ",
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
}
