package ru.finpet.kids.feature.finik.sheets

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.R
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.CloudIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FinPetTheme
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.PixelBar
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.RenderBoardIcon
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.feature.finik.components.PetGrowthStageCard

// карточка вопроса для котика
data class ScriptedAdvice(
    val title: String,
    val icon: String,
    val response: String
)

val DEFAULT_SCRIPTED_ADVICES = listOf(
    ScriptedAdvice(
        title = "Как работает личный бюджет?",
        icon = "💡",
        response = "Бюджет — это как карта сокровищ: ты заранее решаешь, сколько монет потратишь на важное, сколько на радости и сколько спрячешь в копилку!"
    ),
    ScriptedAdvice(
        title = "Что такое обязательные траты?",
        icon = "🥣",
        response = "Обязательные траты — это то, без чего нельзя прожить: еда, чистая вода и уход. Умные ребята сначала покупают необходимое, а уже потом развлечения!"
    ),
    ScriptedAdvice(
        title = "Как быстрее накопить на мечту?",
        icon = "🎯",
        response = "Секрет прост: откладывай в копилку каждый день хотя бы по 20–30 монет! Не спускай все деньги сразу, и мечта станет реальностью!"
    ),
    ScriptedAdvice(
        title = "Правило 3 корзин: 50 / 30 / 20",
        icon = "🫙",
        response = "Половину монет тратим на важное (еда и уход), часть — на радости и игрушки, а 20% сразу прячем в копилку на большую цель!"
    ),
    ScriptedAdvice(
        title = "Что делать, если не хватает монет?",
        icon = "🪙",
        response = "Не расстраивайся! Загляни на Карте в домик: поиграй в мини-игру и получи награду от 20 до 50 золотых монет!"
    )
)

// шторка диалога с питомцем: ребенок выбирает вопрос, питомец отвечает в облачке
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinikAdviceSheet(
    displayName: String,
    growthStage: String = "BABY",
    carePoints: Int = 0,
    advices: List<ScriptedAdvice> = DEFAULT_SCRIPTED_ADVICES,
    onSelectAdvice: (response: String) -> Unit,
    onDismiss: () -> Unit
) {
    val uiScale = LocalUiScale.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFE8DAC2),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        FinPetTheme {
            KeepScreenImmersive()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.78f)
                    .padding(horizontal = (16.dp * uiScale).coerceIn(12.dp, 18.dp))
                    .padding(top = (16.dp * uiScale).coerceIn(12.dp, 18.dp))
                    .padding(bottom = (24.dp * uiScale).coerceIn(16.dp, 28.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // пиксельный заголовок с крестиком на основе PixelBar
                PixelBar(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(
                        horizontal = (14.dp * uiScale).coerceIn(10.dp, 16.dp),
                        vertical = (8.dp * uiScale).coerceIn(6.dp, 10.dp)
                    )
                ) {
                    CloudIcon(modifier = Modifier.size((22.dp * uiScale).coerceIn(16.dp, 24.dp)))
                    Spacer(modifier = Modifier.width((8.dp * uiScale).coerceIn(4.dp, 10.dp)))
                    Text(
                        text = "Спроси у $displayName",
                        fontSize = (17f * uiScale).coerceIn(13f, 19f).sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelTextDark,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size((32.dp * uiScale).coerceIn(26.dp, 36.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.close_sign),
                            contentDescription = "Закрыть",
                            modifier = Modifier.size((28.dp * uiScale).coerceIn(22.dp, 32.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height((12.dp * uiScale).coerceIn(8.dp, 14.dp)))

                // скроллируемый список: стадия развития питомца перед списком вопросов
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy((8.dp * uiScale).coerceIn(6.dp, 10.dp)),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Стадия развития котика с орбами прогресса
                    item(key = "growth_stage_section") {
                        PetGrowthStageCard(
                            growthStage = growthStage,
                            carePoints = carePoints,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Подзаголовок перед списком вопросов
                    item(key = "questions_section_title") {
                        Text(
                            text = "Выбери вопрос для котика:",
                            fontFamily = AppFontFamily,
                            fontSize = (15.5f * uiScale).coerceIn(12f, 17f).sp,
                            fontWeight = FontWeight.Bold,
                            color = PixelTextDark,
                            modifier = Modifier.padding(top = (6.dp * uiScale).coerceIn(3.dp, 8.dp), bottom = 2.dp)
                        )
                    }

                    items(advices, key = { it.title }) { advice ->
                        FinCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onSelectAdvice(advice.response) },
                            backgroundColor = Color(0xFFFFF9EE),
                            borderColor = Color(0xFFDFC490)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = (14.dp * uiScale).coerceIn(10.dp, 16.dp),
                                        vertical = (14.dp * uiScale).coerceIn(10.dp, 16.dp)
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RenderBoardIcon(
                                    advice.icon,
                                    modifier = Modifier.size((22.dp * uiScale).coerceIn(16.dp, 24.dp))
                                )
                                Spacer(modifier = Modifier.width((12.dp * uiScale).coerceIn(8.dp, 14.dp)))
                                Text(
                                    text = advice.title,
                                    fontSize = (15f * uiScale).coerceIn(12f, 17f).sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
