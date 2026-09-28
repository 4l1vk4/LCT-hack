package ru.finpet.kids.feature.map

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.core.designsystem.PixelButton
import ru.finpet.kids.core.designsystem.PixelGoldBright
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.PixelParchmentBorder
import ru.finpet.kids.core.designsystem.PixelParchmentLight
import ru.finpet.kids.core.designsystem.PixelParchmentMedium
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.PixelTextMuted
import ru.finpet.kids.core.designsystem.PixelWoodMedium
import ru.finpet.kids.core.designsystem.StardewBoard

data class GlossaryTerm(
    val term: String,
    val icon: String,
    val definition: String,
    val example: String = ""
)

fun  financialGlossary(petName: String) = listOf(
    GlossaryTerm(
        term = "Бюджет",
        icon = "📋",
        definition = "Это план, куда потратить деньги. Ты решаешь заранее, сколько на что уйдёт.",
        example = "Например: 30 монет на еду, 20 на игрушки, 50 в копилку."
    ),
    GlossaryTerm(
        term = "Доход",
        icon = "💰",
        definition = "Это деньги, которые ты получаешь. Например, карманные или награда за дела.",
        example = "По понедельникам тебе выдают 200 монет карманных."
    ),
    GlossaryTerm(
        term = "Расход",
        icon = "🛒",
        definition = "Это деньги, которые ты тратишь на покупки.",
        example = "Купил корм для питомца — это расход."
    ),
    GlossaryTerm(
        term = "Обязательные траты",
        icon = "🥣",
        definition = "Это то, без чего нельзя обойтись: еда, вода, уход за питомцем.",
        example = "Корм для $petName и лекарства — обязательные траты."
    ),
    GlossaryTerm(
        term = "Желаемые траты",
        icon = "🎈",
        definition = "Это то, что хочется, но можно отложить: игрушки, лакомства, украшения.",
        example = "Мячик или печенье — желаемые, но необязательные."
    ),
    GlossaryTerm(
        term = "Накопления",
        icon = "🐷",
        definition = "Это деньги, которые ты откладываешь и не тратишь. Они копятся на мечту.",
        example = "50 монет каждый день — через неделю уже 350!"
    ),
    GlossaryTerm(
        term = "Копилка",
        icon = "🏦",
        definition = "Место, где хранятся накопления. Их нельзя взять просто так — только с подтверждением.",
        example = "В копилке уже 100 монет на домик для $petName."
    ),
    GlossaryTerm(
        term = "Финансовая цель",
        icon = "🎯",
        definition = "Это то, на что ты копишь. У цели есть цена и понятный результат.",
        example = "Домик за 150 монет — это цель."
    ),
    GlossaryTerm(
        term = "Сдача",
        icon = "🪙",
        definition = "Это монеты, которые остаются после покупки. Их лучше отложить, а не потерять.",
        example = "Купил корм за 25, дал 30 — сдача 5 монет."
    ),
    GlossaryTerm(
        term = "Скидка и акция",
        icon = "🏷️",
        definition = "Когда товар продают дешевле. Но скидка выгодна, только если вещь нужна.",
        example = "3 расчески по акции — это НЕ выгодно, если нужна одна."
    ),
    GlossaryTerm(
        term = "Карманные деньги",
        icon = "👛",
        definition = "Небольшая сумма, которую выдают родители на твои решения.",
        example = "Раз в неделю ты получаешь 200 монет."
    ),
    GlossaryTerm(
        term = "Импульсивная покупка",
        icon = "⚡",
        definition = "Когда покупаешь сразу, не подумав. Часто потом жалеешь.",
        example = "Увидел яркую наклейку — и сразу потратил всё из копилки."
    )
)

@Composable
fun GlossaryDialog(
    petName: String,
    onDismiss: () -> Unit
) {
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    val glossary = remember(petName) { financialGlossary(petName) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            StardewBoard(
                headerTitle = "СЛОВАРИК ${petName.uppercase()}",
                headerIcon = "📖"
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // Подсказка сверху
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(PixelParchmentMedium)
                            .border(1.dp, PixelParchmentBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💡", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Нажми на слово, чтобы узнать его значение!",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = PixelTextMuted,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Список терминов
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(glossary.size, key = { index -> glossary[index].term }) { index ->
                            val term = glossary[index]
                            val isExpanded = expandedIndex == index

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isExpanded) Color(0xFFFFF8E7) else PixelParchmentLight)
                                    .border(
                                        width = if (isExpanded) 2.dp else 1.dp,
                                        color = if (isExpanded) PixelGoldBright else PixelParchmentBorder,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        expandedIndex = if (isExpanded) null else index
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isExpanded) PixelGoldBright else PixelParchmentMedium)
                                            .border(
                                                1.dp,
                                                if (isExpanded) PixelGoldDark else PixelParchmentBorder,
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = term.icon, fontSize = 20.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = term.term,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = PixelTextDark,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Text(
                                        text = if (isExpanded) "▲" else "▼",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExpanded) PixelGoldDark else PixelTextMuted
                                    )
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = term.definition,
                                        fontSize = 13.sp,
                                        color = PixelTextDark,
                                        lineHeight = 18.sp
                                    )

                                    if (term.example.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFF1F8E9))
                                                .border(1.dp, Color(0xFFAED581), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.Top) {
                                                Text(text = "🌟", fontSize = 12.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = term.example,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = PixelGreenCrop,
                                                    lineHeight = 15.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Кнопка закрытия
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            PixelButton(
                                text = "✕ Закрыть словарик",
                                onClick = onDismiss,
                                containerColor = PixelParchmentMedium,
                                textColor = PixelTextDark,
                                borderColor = PixelParchmentBorder,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}