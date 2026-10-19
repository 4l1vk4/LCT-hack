package ru.finpet.kids.feature.map.sheets.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import ru.finpet.kids.R
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.feature.map.sheets.HomeChoreItem

/**
 * Карточка домашнего дела (мытье полов, вынос мусора, мытье посуды):
 * - Интерактивный запуск мини-игры для выполнения дела.
 * - Статус кулдауна и заработанных монет.
 * - Строгий пиксельный стиль без округлых пастельных плашек.
 */
@Composable
fun HomeChoreCard(
    chore: HomeChoreItem,
    isDone: Boolean,
    onPlayMinigame: (HomeChoreItem) -> Unit
) {
    FinCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (chore.id) {
                        "chore_floor" -> Image(
                            painter = painterResource(id = R.drawable.autumn_sheet),
                            contentDescription = chore.title,
                            modifier = Modifier.size(34.dp)
                        )
                        "chore_dishes" -> Image(
                            painter = painterResource(id = R.drawable.tarelka_clear),
                            contentDescription = chore.title,
                            modifier = Modifier.size(34.dp)
                        )
                        "chore_trash" -> Image(
                            painter = painterResource(id = R.drawable.bin),
                            contentDescription = chore.title,
                            modifier = Modifier.size(34.dp)
                        )
                        else -> Text(text = chore.icon, fontSize = 26.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = chore.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = chore.description,
                        fontSize = 16.sp,
                        color = TextSecondary,
                        lineHeight = 21.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isDone) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "✅ +${chore.rewardCoins} ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FreshGreen
                    )
                    CoinIcon(modifier = Modifier.size(16.dp))
                }
            } else {
                PixelButton(
                    text = "Играть! +${chore.rewardCoins}",
                    onClick = { onPlayMinigame(chore) },
                    containerColor = PixelGoldBright,
                    textColor = PixelTextDark,
                    fontSize = 16.sp,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    trailingIcon = { CoinIcon(modifier = Modifier.size(16.dp)) }
                )
            }
        }
    }
}
