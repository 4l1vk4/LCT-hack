package ru.finpet.kids.feature.map.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import ru.finpet.kids.core.designsystem.KeepScreenImmersive

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
    badgeText: String,
    onDismiss: () -> Unit,
    listState: LazyListState = rememberLazyListState(),
    content: @Composable () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PixelParchmentLight,
        scrimColor = Color.Black.copy(alpha = 0.40f), // аккуратное затемнение карты сзади
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        // сохраняем полноэкранный режим без вылезающих системных панелей
        KeepScreenImmersive()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f) // занимаем 90% экрана, чтобы влезало много контента
        ) {
            // шапка с пиксельной картинкой здания
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                Image(
                    painter = painterResource(id = headerDrawableRes),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // полоска drag-handle
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .size(width = 44.dp, height = 5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(PixelWoodMedium.copy(alpha = 0.7f))
                )

                // бейдж статуса слева (например "3/8 решено" или сумма в копилке)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 16.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    border = BorderStroke(1.5.dp, PixelDarkBrown),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelDarkBrown,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // крестик закрытия справа — тач-зона 48x48 dp, чтобы ребенку было легко попасть
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 12.dp, top = 12.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.95f))
                            .border(1.5.dp, PixelDarkBrown, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = PixelDarkBrown
                        )
                    }
                }
            }

            // скроллируемый список задач / товаров / целей
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "location_header_texts") {
                    Column {
                        Text(
                            text = title,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PixelDarkBrown
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            fontSize = 18.sp,
                            color = PixelWoodMedium,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item(key = "location_main_content") {
                    content()
                }
            }
        }
    }
}
