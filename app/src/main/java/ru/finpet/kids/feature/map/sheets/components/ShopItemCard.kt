package ru.finpet.kids.feature.map.sheets.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.data.repository.PurchaseItem
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.ExclamationIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.HeartIcon
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.LightningIcon
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.SkyBlue
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.core.designsystem.pixelButtonBackground
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.core.designsystem.AppleIcon
import ru.finpet.kids.core.designsystem.LockIcon

/**
 * Карточка товара в лавке (пища, игрушки, поручение родителей).
 *
 * Особенности:
 * - Чёткий пиксельный стиль без размытых пастельных бэйджей.
 * - Индикатор важности (восклицательный знак), если стат питомца <= 30% и этот товар восстанавливает его.
 * - Пиксельная кнопка покупки с фаской и тактильным эффектом без ховер-подсветки.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShopItemCard(
    item: PurchaseItem,
    profile: ProfileEntity?,
    isRunaway: Boolean,
    todayNotes: List<CalendarNoteEntity>,
    isTarget: Boolean,
    bringIntoViewRequester: BringIntoViewRequester,
    onBuyClick: (PurchaseItem) -> Unit
) {
    val isGroceriesItem = item.id == "groceries_parents"
    val groceryTask = if (isGroceriesItem) todayNotes.find { it.category == "GROCERIES" } else null
    val isGroceriesCompleted = groceryTask?.isCompleted == true
    val isGroceriesUnavailable = isGroceriesItem && groceryTask == null

    val curSatiety = profile?.satiety ?: 100
    val curMood = profile?.mood ?: 100
    val curHealth = profile?.health ?: 100

    val boostsLowSatiety = curSatiety <= 30 && item.satietyBonus > 0
    val boostsLowMood = curMood <= 30 && item.moodBonus > 0
    val boostsLowHealth = curHealth <= 30 && item.healthBonus > 0
    val canBoostAnyLowStat = boostsLowSatiety || boostsLowMood || boostsLowHealth
    val uiScale = LocalUiScale.current

    FinCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isTarget) Modifier.bringIntoViewRequester(bringIntoViewRequester) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding((12.dp * uiScale).coerceIn(8.dp, 14.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Название товара + отметки важности и выполнения
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontSize = (17f * uiScale).coerceIn(12f, 18f).sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (canBoostAnyLowStat && !isRunaway) {
                        Spacer(modifier = Modifier.width((5.dp * uiScale).coerceIn(3.dp, 6.dp)))
                        ExclamationIcon(modifier = Modifier.size((16.dp * uiScale).coerceIn(12.dp, 18.dp)))
                    }
                    if (isGroceriesCompleted) {
                        Spacer(modifier = Modifier.width((6.dp * uiScale).coerceIn(4.dp, 8.dp)))
                        Text(
                            text = "Выполнено",
                            fontSize = (14f * uiScale).coerceIn(10f, 15f).sp,
                            fontWeight = FontWeight.Bold,
                            color = FreshGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = when {
                        isGroceriesCompleted -> "Поручение родителей выполнено! Сдача +${groceryTask?.cost ?: 0} уже в кошельке."
                        isGroceriesUnavailable -> "Родители сегодня не давали поручение на покупки. Загляни во Вт, Чт или Сб!"
                        else -> item.description
                    },
                    fontSize = (14f * uiScale).coerceIn(10f, 16f).sp,
                    color = TextSecondary,
                    lineHeight = (18f * uiScale).coerceIn(13f, 21f).sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                // Бонусы к характеристикам питомца
                Row(
                    horizontalArrangement = Arrangement.spacedBy((6.dp * uiScale).coerceIn(4.dp, 8.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.satietyBonus > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppleIcon(modifier = Modifier.size((15.dp * uiScale).coerceIn(11.dp, 16.dp)))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("+${item.satietyBonus}", fontSize = (14f * uiScale).coerceIn(11f, 16f).sp, color = JoyOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (item.moodBonus > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LightningIcon(modifier = Modifier.size((15.dp * uiScale).coerceIn(11.dp, 16.dp)))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("+${item.moodBonus}", fontSize = (14f * uiScale).coerceIn(11f, 16f).sp, color = SkyBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (item.healthBonus > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HeartIcon(modifier = Modifier.size((15.dp * uiScale).coerceIn(11.dp, 16.dp)))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("+${item.healthBonus}", fontSize = (14f * uiScale).coerceIn(11f, 16f).sp, color = FreshGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (isGroceriesItem && !isGroceriesCompleted && !isGroceriesUnavailable) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CoinIcon(modifier = Modifier.size((15.dp * uiScale).coerceIn(11.dp, 16.dp)))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Сдача: 5, 10 или 15", fontSize = (14f * uiScale).coerceIn(11f, 16f).sp, color = FreshGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width((8.dp * uiScale).coerceIn(4.dp, 10.dp)))

            // Кнопка действия (покупка / сдача / статус блокировки) в аутентичном пиксельном дизайне
            when {
                isRunaway -> {
                    Box(
                        modifier = Modifier
                            .pixelButtonBackground(containerColor = Color(0xFFDED0B6))
                            .padding(
                                horizontal = (8.dp * uiScale).coerceIn(4.dp, 10.dp),
                                vertical = (5.dp * uiScale).coerceIn(3.dp, 6.dp)
                            )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LockIcon(modifier = Modifier.size((14.dp * uiScale).coerceIn(10.dp, 15.dp)))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Закрыто",
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = (13f * uiScale).coerceIn(10f, 14f).sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
                isGroceriesCompleted -> {
                    Box(
                        modifier = Modifier
                            .pixelButtonBackground(containerColor = Color(0xFFDCEDC8))
                            .padding(
                                horizontal = (8.dp * uiScale).coerceIn(4.dp, 10.dp),
                                vertical = (5.dp * uiScale).coerceIn(3.dp, 6.dp)
                            )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if ((groceryTask?.cost ?: 0) > 0) {
                                Text(
                                    text = "+${groceryTask?.cost} ",
                                    fontFamily = AppFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (13f * uiScale).coerceIn(10f, 14f).sp,
                                    color = PixelGreenCrop
                                )
                                CoinIcon(modifier = Modifier.size((14.dp * uiScale).coerceIn(10.dp, 15.dp)))
                            } else {
                                Text(
                                    text = "Готово",
                                    fontFamily = AppFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (13f * uiScale).coerceIn(10f, 14f).sp,
                                    color = PixelGreenCrop
                                )
                            }
                        }
                    }
                }
                isGroceriesUnavailable -> {
                    Box(
                        modifier = Modifier
                            .pixelButtonBackground(containerColor = Color(0xFFDED0B6))
                            .padding(
                                horizontal = (8.dp * uiScale).coerceIn(4.dp, 10.dp),
                                vertical = (5.dp * uiScale).coerceIn(3.dp, 6.dp)
                            )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LockIcon(modifier = Modifier.size((14.dp * uiScale).coerceIn(10.dp, 15.dp)))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Нет задания",
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = (13f * uiScale).coerceIn(10f, 14f).sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .pixelButtonBackground(containerColor = if (isGroceriesItem) PixelGreenCrop else PixelGoldBright)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onBuyClick(item) }
                            .padding(
                                horizontal = (10.dp * uiScale).coerceIn(5.dp, 12.dp),
                                vertical = (6.dp * uiScale).coerceIn(4.dp, 7.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGroceriesItem) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiScale < 0.85f) {
                                    CoinIcon(modifier = Modifier.size((14.dp * uiScale).coerceIn(10.dp, 15.dp)))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Сдача!", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = (14f * uiScale).coerceIn(10f, 15f).sp, color = Color.White)
                                } else {
                                    Text("0 ", fontFamily = AppFontFamily, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                                    CoinIcon(modifier = Modifier.size(16.dp))
                                    Text(" • Сдача!", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${item.price} ", fontFamily = AppFontFamily, fontWeight = FontWeight.Black, fontSize = (16f * uiScale).coerceIn(11f, 17f).sp, color = PixelTextDark)
                                CoinIcon(modifier = Modifier.size((15.dp * uiScale).coerceIn(11.dp, 17.dp)))
                            }
                        }
                    }
                }
            }
        }
    }
}
