package ru.finpet.kids.feature.budget.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.KoshelekIcon
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.LockIcon
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.WarningIcon
import ru.finpet.kids.core.designsystem.pixelButtonBackground

/**
 * Уведомление об утверждённом бюджете на текущий период.
 */
@Composable
fun BudgetConfirmedNotice(modifier: Modifier = Modifier) {
    val uiScale = LocalUiScale.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE8F5E9))
            .border(1.dp, PixelGreenCrop, RoundedCornerShape(6.dp))
            .padding(
                horizontal = (10.dp * uiScale).coerceIn(6.dp, 10.dp),
                vertical = (8.dp * uiScale).coerceIn(5.dp, 8.dp)
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LockIcon(modifier = Modifier.size((18.dp * uiScale).coerceIn(14.dp, 18.dp)))
            Spacer(modifier = Modifier.width((8.dp * uiScale).coerceIn(4.dp, 8.dp)))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Бюджет на период утверждён!",
                    fontSize = (15f * uiScale).coerceIn(12f, 16f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = "Распределение монет зафиксировано. Новый план можно составить в начале следующей недели.",
                    fontSize = (14f * uiScale).coerceIn(11f, 15f).sp,
                    color = Color(0xFF2E7D32),
                    lineHeight = (18f * uiScale).coerceIn(13f, 19f).sp
                )
            }
        }
    }
}

/**
 * Панель статуса кошелька и распределения монет в баночках (с предупреждением о перерасходе или остатке).
 */
@Composable
fun BudgetWalletStatusCard(
    balance: Int,
    totalPlanned: Int,
    remainder: Int,
    isOverBudget: Boolean,
    modifier: Modifier = Modifier
) {
    val uiScale = LocalUiScale.current
    PixelPanel(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = (12.dp * uiScale).coerceIn(6.dp, 14.dp),
            vertical = (8.dp * uiScale).coerceIn(5.dp, 10.dp)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val textSize = (16f * uiScale).sp
            val coinSize = (18.dp * uiScale)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy((4.dp * uiScale).coerceIn(3.dp, 6.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "В кошельке: $balance",
                        fontWeight = FontWeight.Bold,
                        fontSize = textSize,
                        color = PixelTextDark,
                        fontFamily = AppFontFamily
                    )
                    Spacer(modifier = Modifier.width((4.dp * uiScale).coerceIn(3.dp, 6.dp)))
                    CoinIcon(modifier = Modifier.size(coinSize))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "В баночках: $totalPlanned",
                        fontSize = textSize,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverBudget) PixelRedBerry else PixelTextDark,
                        fontFamily = AppFontFamily
                    )
                    Spacer(modifier = Modifier.width((4.dp * uiScale).coerceIn(3.dp, 6.dp)))
                    CoinIcon(modifier = Modifier.size(coinSize))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pixelButtonBackground(containerColor = if (isOverBudget) Color(0xFFFFCDD2) else Color(0xFFDCEDC8))
                    .padding(
                        horizontal = (12.dp * uiScale).coerceIn(6.dp, 18.dp),
                        vertical = (6.dp * uiScale).coerceIn(4.dp, 8.dp)
                    )
            ) {
                if (isOverBudget) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WarningIcon(modifier = Modifier.size((18.dp * uiScale).coerceIn(14.dp, 18.dp)))
                        Text(
                            text = "Превышение бюджета на ${-remainder} монет! Уменьши суммы в баночках.",
                            fontFamily = AppFontFamily,
                            fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                            fontWeight = FontWeight.Bold,
                            color = PixelRedBerry,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            KoshelekIcon(modifier = Modifier.size((18.dp * uiScale).coerceIn(14.dp, 20.dp)))
                            Text(
                                text = "Свободно в кошельке: $remainder монет останется на руках.",
                                fontFamily = AppFontFamily,
                                fontSize = (15f * uiScale).coerceIn(11f, 16f).sp,
                                fontWeight = FontWeight.Bold,
                                color = PixelGreenCrop,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (remainder > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Нераспределённые деньги расходуются только если в банках закончатся монеты. Траты из свободного кошелька снижают точность бюджета, так как их трудно контролировать!",
                                fontSize = (14f * uiScale).coerceIn(10f, 15f).sp,
                                color = PixelTextMuted,
                                lineHeight = (16f * uiScale).coerceIn(12f, 17f).sp,
                                fontFamily = AppFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}
