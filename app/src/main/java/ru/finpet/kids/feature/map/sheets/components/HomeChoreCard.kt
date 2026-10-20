package ru.finpet.kids.feature.map.sheets.components

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import ru.finpet.kids.R
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelCheckbox
import ru.finpet.kids.core.designsystem.PixelGoldBright
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
    val uiScale = LocalUiScale.current
    FinCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = (12.dp * uiScale).coerceIn(8.dp, 14.dp),
                    vertical = (10.dp * uiScale).coerceIn(6.dp, 12.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size((36.dp * uiScale).coerceIn(24.dp, 38.dp)),
                contentAlignment = Alignment.Center
            ) {
                val iconSize = (32.dp * uiScale).coerceIn(20.dp, 34.dp)
                when (chore.id) {
                    "chore_floor" -> Image(
                        painter = painterResource(id = R.drawable.autumn_sheet),
                        contentDescription = chore.title,
                        modifier = Modifier.size(iconSize)
                    )
                    "chore_dishes" -> Image(
                        painter = painterResource(id = R.drawable.tarelka_clear),
                        contentDescription = chore.title,
                        modifier = Modifier.size(iconSize)
                    )
                    "chore_trash" -> Image(
                        painter = painterResource(id = R.drawable.bin),
                        contentDescription = chore.title,
                        modifier = Modifier.size(iconSize)
                    )
                    "chore_match3" -> Image(
                        painter = painterResource(id = R.drawable.blue_ball),
                        contentDescription = chore.title,
                        modifier = Modifier.size(iconSize)
                    )
                    else -> Text(text = chore.icon, fontSize = (22f * uiScale).coerceIn(16f, 26f).sp)
                }
            }

            Spacer(modifier = Modifier.width((8.dp * uiScale).coerceIn(4.dp, 12.dp)))

            // Текст задачи: занимает всё свободное пространство и не ломается по буквам
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chore.title,
                    fontSize = (17f * uiScale).coerceIn(12f, 18f).sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = chore.description,
                    fontSize = (14f * uiScale).coerceIn(10f, 16f).sp,
                    color = TextSecondary,
                    lineHeight = (18f * uiScale).coerceIn(13f, 21f).sp
                )
            }

            Spacer(modifier = Modifier.width((6.dp * uiScale).coerceIn(4.dp, 10.dp)))

            if (isDone) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+${chore.rewardCoins} ",
                        fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                        fontWeight = FontWeight.Bold,
                        color = FreshGreen
                    )
                    CoinIcon(modifier = Modifier.size((15.dp * uiScale).coerceIn(11.dp, 16.dp)))
                }
            } else {
                PixelButton(
                    text = if (chore.rewardCoins > 0 && uiScale >= 0.85f) "Играть! +${chore.rewardCoins}" else "Играть!",
                    onClick = { onPlayMinigame(chore) },
                    containerColor = PixelGoldBright,
                    textColor = PixelTextDark,
                    fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                    contentPadding = PaddingValues(
                        horizontal = (8.dp * uiScale).coerceIn(5.dp, 10.dp),
                        vertical = (5.dp * uiScale).coerceIn(3.dp, 6.dp)
                    ),
                    trailingIcon = if (chore.rewardCoins > 0 && uiScale >= 0.85f) {
                        { CoinIcon(modifier = Modifier.size((14.dp * uiScale).coerceIn(10.dp, 16.dp))) }
                    } else null
                )
            }
        }
    }
}
