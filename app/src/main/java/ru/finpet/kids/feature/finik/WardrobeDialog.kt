package ru.finpet.kids.feature.finik

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.R
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelRedBerry
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.core.designsystem.pixelButtonBackground

data class RoomItemDef(
    val id: String,
    val title: String,
    @DrawableRes val drawableRes: Int
)

val ROOM_DECORATIONS: List<RoomItemDef> = listOf(
    RoomItemDef(
        id = "goal_scooter",
        title = "Самокат",
        drawableRes = R.drawable.samokat
    ),
    RoomItemDef(
        id = "goal_ball",
        title = "Мяч",
        drawableRes = R.drawable.ball
    ),
    RoomItemDef(
        id = "goal_headphones",
        title = "Наушники",
        drawableRes = R.drawable.headphones
    )
)

@Composable
fun WardrobeDialog(
    currentAccessoryIds: Set<String>,
    petName: String,
    goals: List<GoalEntity> = emptyList(),
    hiddenRoomItems: Set<String> = emptySet(),
    isDemoMode: Boolean = false,
    onToggleAccessory: (String) -> Unit,
    onToggleRoomItem: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        KeepScreenImmersive()
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
        ) {
            StardewBoard(
                headerTitle = "ГАРДЕРОБ",
                headerIcon = ""
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Надень одежду на $petName или расставь вещи в комнате",
                        fontFamily = AppFontFamily,
                        fontSize = 15.sp,
                        color = PixelTextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Единая сетка аксессуаров и предметов комнаты
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Заголовок секции аксессуаров
                        item(span = { GridItemSpan(3) }, key = "header_accessories") {
                            Text(
                                text = "🐱 Одежда для $petName",
                                fontFamily = AppFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PixelTextDark,
                                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                            )
                        }

                        // Аксессуары котика
                        items(ACCESSORIES.filter { it.id != "none" }, key = { "acc_${it.id}" }) { acc ->
                            val isOn = acc.id in currentAccessoryIds

                            Box(
                                modifier = Modifier
                                    .pixelButtonBackground(containerColor = if (isOn) PixelGoldBright else PixelParchmentLight)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onToggleAccessory(acc.id) }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier.size(50.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(acc.drawableRes),
                                            contentDescription = acc.title,
                                            modifier = Modifier.size(42.dp)
                                        )

                                        // Галочка "надето"
                                        if (isOn) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF2E7D32)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "✓",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = acc.title,
                                        fontFamily = AppFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PixelTextDark,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (isOn) "надето" else "снято",
                                        fontFamily = AppFontFamily,
                                        fontSize = 10.sp,
                                        color = if (isOn) Color(0xFF2E7D32) else PixelTextMuted,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Заголовок секции предметов комнаты
                        item(span = { GridItemSpan(3) }, key = "header_room") {
                            Text(
                                text = "🏠 Предметы в комнате",
                                fontFamily = AppFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PixelTextDark,
                                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                            )
                        }

                        // Предметы комнаты (самокат, мяч, наушники)
                        items(ROOM_DECORATIONS, key = { "room_${it.id}" }) { roomItem ->
                            val isPurchased = isDemoMode || goals.any {
                                it.id == roomItem.id && (it.isReached || it.savedAmount >= it.targetCost)
                            }
                            val isEquipped = isPurchased && (roomItem.id !in hiddenRoomItems)

                            Box(
                                modifier = Modifier
                                    .pixelButtonBackground(
                                        containerColor = if (isEquipped) PixelGoldBright else PixelParchmentLight
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        enabled = isPurchased,
                                        onClick = { onToggleRoomItem(roomItem.id) }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier.size(50.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(roomItem.drawableRes),
                                            contentDescription = roomItem.title,
                                            modifier = Modifier
                                                .size(42.dp)
                                                .graphicsLayer {
                                                    alpha = if (isPurchased) 1f else 0.45f
                                                }
                                        )

                                        if (isEquipped) {
                                            // Галочка "в комнате"
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF2E7D32)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "✓",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White
                                                )
                                            }
                                        } else if (!isPurchased) {
                                            // Замочек "не накоплено"
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF757575)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "🔒",
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = roomItem.title,
                                        fontFamily = AppFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPurchased) PixelTextDark else PixelTextMuted,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = when {
                                            !isPurchased -> "в копилке"
                                            isEquipped -> "в комнате"
                                            else -> "убрано"
                                        },
                                        fontFamily = AppFontFamily,
                                        fontSize = 10.sp,
                                        color = if (isEquipped) Color(0xFF2E7D32) else PixelTextMuted,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentAccessoryIds.isNotEmpty()) {
                            PixelButton(
                                text = "🚫 Снять одежду",
                                onClick = { onToggleAccessory("none") },
                                containerColor = Color(0xFFFFCDD2),
                                textColor = PixelRedBerry,
                                modifier = Modifier.weight(1f),
                                fontSize = 14.sp,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            )
                        }
                        PixelButton(
                            text = "Готово",
                            onClick = onDismiss,
                            containerColor = PixelGoldBright,
                            modifier = Modifier.weight(1f),
                            fontSize = 15.sp,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}