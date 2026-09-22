package ru.finpet.kids.feature.finik

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.SkyBlue
import ru.finpet.kids.core.designsystem.StatBar
import ru.finpet.kids.core.designsystem.TextPrimary

data class ScriptedAdvice(
    val title: String,
    val icon: String,
    val response: String
)

@Composable
fun FinikScreen(
    profile: ProfileEntity?,
    animationsEnabled: Boolean = true,
    onPetTapped: () -> Unit = {}
) {
    val satiety = profile?.satiety ?: 100
    val mood = profile?.mood ?: 80
    val health = profile?.health ?: 100
    val petName = profile?.petName ?: "Финни"

    val defaultGreeting = "Привет! Я твой финансовый помощник $petName. Спрашивай меня обо всем — я помогу тебе стать мастером монет!"
    var currentSpeech by remember { mutableStateOf(defaultGreeting) }

    val adviceList = remember(satiety, mood, health) {
        listOf(
            ScriptedAdvice(
                title = "Как ты себя чувствуешь?",
                icon = "🐱",
                response = when {
                    satiety < 50 -> "Мой животик урчит! Кажется, мы забыли купить сытный обед. Покорми меня, пожалуйста!"
                    health < 60 -> "Я немного приболел... Витаминки или свежая водичка мне очень помогут!"
                    mood < 50 -> "Мне немного грустно... Давай заглянем в лавку и выберем мячик или печенье!"
                    else -> "Я счастлив, полон сил и мурчу от радости! Ты отлично заботишься обо мне!"
                }
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
                icon = "🧺",
                response = "Половину монет тратим на важное (еда и уход), часть — на радости и игрушки, а 20% сразу прячем в копилку на большую цель!"
            ),
            ScriptedAdvice(
                title = "Что делать, если не хватает монет?",
                icon = "🪙",
                response = "Не расстраивайся! Загляни на Карте в шатер Квестов: реши интересную задачку и получи награду от 20 до 50 золотых монет!"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 84.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(key = "speech_bubble") {
            // Облачко диалога Финни
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFFFF9E6))
                    .border(2.dp, Color(0xFFFFE082), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "💬 $currentSpeech",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    lineHeight = 22.sp
                )
            }
        }

        item(key = "finik_character") {
            // Финни в кресле
            FinikInArmchair(
                modifier = Modifier.padding(vertical = 4.dp),
                mood = when {
                    satiety < 50 || mood < 40 -> "SAD"
                    satiety >= 70 && mood >= 70 -> "HAPPY"
                    else -> "NEUTRAL"
                },
                enabled = animationsEnabled,
                onClick = {
                    onPetTapped()
                    currentSpeech = "Муррр! Я люблю, когда мы вместе учимся беречь монетки!"
                }
            )
        }

        item(key = "status_panel") {
            // Статус-панель питомца
            FinCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                backgroundColor = Color.White
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Состояние $petName",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    StatBar(
                        label = "Сытость",
                        icon = "🍎",
                        value = satiety,
                        barColor = JoyOrange,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatBar(
                        label = "Настроение",
                        icon = "⚡",
                        value = mood,
                        barColor = SkyBlue,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatBar(
                        label = "Здоровье",
                        icon = "❤️",
                        value = health,
                        barColor = FreshGreen,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item(key = "advice_header") {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Спроси у $petName:",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        items(adviceList.size, key = { index -> "advice_$index" }) { index ->
            val advice = adviceList[index]
            FinCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        currentSpeech = advice.response
                    },
                backgroundColor = Color(0xFFFBFBFB)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = advice.icon, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = advice.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "👉", fontSize = 16.sp)
                }
            }
        }
    }
}
