package ru.finpet.kids.feature.map.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import ru.finpet.kids.core.designsystem.FinPetTheme
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.LocalUiScale

// цвета пергамента и дерева для модалок локаций
val PixelParchmentLight = Color(0xFFFFF8E7)
val PixelDarkBrown = Color(0xFF351A0C)
val PixelWoodMedium = Color(0xFF6B3C1A)

// общая подложка для всех зданий на карте (школа, лавка, дом)
// открывается снизу почти на весь экран (90%), с шапкой-картинкой и скроллом
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PixelLocationModalSheet(
    title: String,
    subtitle: String,
    headerDrawableRes: Int,
    badgeText: String = "",
    badgeContent: (@Composable () -> Unit)? = null,
    onDismiss: () -> Unit,
    listState: LazyListState = rememberLazyListState(),
    topStartExtra: (@Composable () -> Unit)? = null,
    headerOverlay: (@Composable androidx.compose.foundation.layout.BoxWithConstraintsScope.() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val uiScale = LocalUiScale.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PixelParchmentLight,
        scrimColor = Color.Black.copy(alpha = 0.40f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        FinPetTheme {
            // сохранение полноэкранного режима без вылезающих системных панелей
            KeepScreenImmersive()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
        ) {
            // шапка с пиксельной картинкой здания
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1073f / 493f)
            ) {
                Image(
                    painter = painterResource(id = headerDrawableRes),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                headerOverlay?.invoke(this)

                val boxLeft = maxWidth * (36f / 1073f)
                val boxTop = maxHeight * (36f / 493f)
                val boxWidth = maxWidth * (275f / 1073f)
                val boxHeight = maxHeight * (77f / 493f)

                // бейдж статуса/монет прямо внутри аутентичного белого квадрата на картинке
                if (badgeContent != null || badgeText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .offset(x = boxLeft, y = boxTop)
                            .size(width = boxWidth, height = boxHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        if (badgeContent != null) {
                            badgeContent()
                        } else {
                            Text(
                                text = badgeText,
                                fontSize = (13f * uiScale).coerceIn(9f, 14f).sp,
                                fontWeight = FontWeight.Bold,
                                color = PixelDarkBrown,
                                maxLines = 1
                            )
                        }
                    }
                }

                // доп. кнопка слева (например, "Список покупок") правее белого квадрата на той же высоте
                if (topStartExtra != null) {
                    Box(
                        modifier = Modifier
                            .offset(x = boxLeft + boxWidth + 8.dp, y = boxTop)
                            .height(boxHeight),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        topStartExtra()
                    }
                }

                // крестик закрытия
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            end = (10.dp * uiScale).coerceIn(6.dp, 12.dp),
                            top = (10.dp * uiScale).coerceIn(6.dp, 12.dp)
                        )
                        .size((38.dp * uiScale).coerceIn(28.dp, 44.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = ru.finpet.kids.R.drawable.close_sign),
                        contentDescription = "Закрыть",
                        modifier = Modifier.size((28.dp * uiScale).coerceIn(20.dp, 34.dp))
                    )
                }
            }

            // скроллируемый список задач / товаров / целей
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = (14.dp * uiScale).coerceIn(8.dp, 16.dp)),
                contentPadding = PaddingValues(top = (12.dp * uiScale).coerceIn(8.dp, 14.dp), bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy((10.dp * uiScale).coerceIn(6.dp, 12.dp))
            ) {
                item(key = "location_header_texts") {
                    Column {
                        Text(
                            text = title,
                            fontSize = (20f * uiScale).coerceIn(14f, 22f).sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PixelDarkBrown
                        )
                        Spacer(modifier = Modifier.height((3.dp * uiScale).coerceIn(2.dp, 4.dp)))
                        Text(
                            text = subtitle,
                            fontSize = (14f * uiScale).coerceIn(10f, 16f).sp,
                            color = PixelWoodMedium,
                            lineHeight = (18f * uiScale).coerceIn(13f, 21f).sp
                        )
                        Spacer(modifier = Modifier.height((6.dp * uiScale).coerceIn(4.dp, 8.dp)))
                    }
                }

                item(key = "location_main_content") {
                    content()
                }
            }
        }
        }
    }
}
