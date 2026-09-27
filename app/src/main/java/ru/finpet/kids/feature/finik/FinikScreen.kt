package ru.finpet.kids.feature.finik

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.SkyBlue
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary

data class ScriptedAdvice(
    val title: String,
    val icon: String,
    val response: String
)

@Composable
fun FinikScreen(
    profile: ProfileEntity?,
    goals: List<GoalEntity> = emptyList(),
    selectedSkin: String? = null,
    petName: String? = null,
    animationsEnabled: Boolean = true,
    onPetTapped: () -> Unit = {},
    onNavigateToGoals: (() -> Unit)? = null
) {
    val satiety = profile?.satiety ?: 100
    val mood = profile?.mood ?: 80
    val health = profile?.health ?: 100
    val displayName = petName ?: profile?.petName ?: "Финни"

    // Сытость и здоровье скрыто влияют на единый показатель настроения
    val effectiveMood = remember(mood, satiety, health) {
        var calculated = (mood * 0.60f + satiety * 0.20f + health * 0.20f)
        if (satiety < 50) {
            calculated -= (50 - satiety) * 0.5f
        }
        if (health < 60) {
            calculated -= (60 - health) * 0.6f
        }
        calculated.roundToInt().coerceIn(0, 100)
    }

    val defaultGreeting = "Привет! Я твой финансовый помощник $displayName. Нажми на меня, чтобы задать вопрос!"
    var currentSpeech by remember { mutableStateOf(defaultGreeting) }
    var panelOpen by remember { mutableStateOf(false) }

    val activeGoal = remember(goals, profile?.activeGoalId) {
        goals.find { it.id == profile?.activeGoalId } ?: goals.firstOrNull()
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
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
                FinikInArmchair(
                    modifier = Modifier.padding(vertical = 4.dp),
                    skinId = selectedSkin ?: "cat_black",
                    mood = when {
                        effectiveMood < 40 -> "SAD"
                        effectiveMood >= 70 -> "HAPPY"
                        else -> "NEUTRAL"
                    },
                    stage = profile?.growthStage ?: "BABY",
                    enabled = animationsEnabled,
                    onClick = {
                        onPetTapped()
                        panelOpen = true
                    }
                )
            }

            item(key = "mood_bar") {
                // Единый показатель настроения питомца (в 2 раза толще обычного бара — 24dp, сытость и здоровье влияют скрыто)
                FinCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    backgroundColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val animatedProgress by animateFloatAsState(
                            targetValue = (effectiveMood.coerceIn(0, 100) / 100f),
                            animationSpec = tween(durationMillis = 500),
                            label = "mood_anim"
                        )
                        val moodColor = remember(effectiveMood) {
                            when {
                                effectiveMood >= 70 -> FreshGreen
                                effectiveMood >= 40 -> JoyOrange
                                else -> Color(0xFFFF5252)
                            }
                        }
                        val moodBrush = remember(moodColor) {
                            Brush.horizontalGradient(
                                listOf(moodColor.copy(alpha = 0.8f), moodColor)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when {
                                        effectiveMood >= 70 -> "⚡"
                                        effectiveMood >= 40 -> "🙂"
                                        else -> "🥺"
                                    },
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Настроение $displayName",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "$effectiveMood%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = moodColor
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEEEEE))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedProgress)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(moodBrush)
                            )
                        }
                    }
                }
            }

            item(key = "goal_bar") {
                // Планка прогресса по цели в копилке
                if (activeGoal != null) {
                    val progress = (activeGoal.savedAmount.toFloat() / activeGoal.targetCost.toFloat()).coerceIn(0f, 1f)
                    val percent = (progress * 100).toInt()
                    val remaining = (activeGoal.targetCost - activeGoal.savedAmount).coerceAtLeast(0)
                    val goalEmoji = when (activeGoal.id) {
                        "goal_ball" -> "⚽"
                        "goal_headphones" -> "🎧"
                        "goal_scooter" -> "🛴"
                        else -> "🎯"
                    }
                    val animatedGoalProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(durationMillis = 500),
                        label = "goal_anim"
                    )
                    val goalBrush = remember {
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFB74D), JoyOrange)
                        )
                    }

                    FinCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .then(if (onNavigateToGoals != null) Modifier.clickable { onNavigateToGoals() } else Modifier),
                        backgroundColor = Color.White
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = goalEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Цель: ${activeGoal.title}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${activeGoal.savedAmount} / ${activeGoal.targetCost} ",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = JoyOrange
                                    )
                                    CoinIcon(modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEEEEEE))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(animatedGoalProgress)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(goalBrush)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (activeGoal.isReached) "🎉 Мечта достигнута!" else "Осталось накопить: $remaining монет",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (activeGoal.isReached) FreshGreen else TextSecondary
                                )
                                Text(
                                    text = "$percent%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JoyOrange
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = panelOpen,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { panelOpen = false }
            )
        }

        // ─── Нижняя панель с вопросами ───
        AnimatedVisibility(
            visible = panelOpen,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(280)
            ) + fadeIn(tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(220)
            ) + fadeOut(tween(160)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color(0xFFFFFDF7).copy(alpha = 0.94f))
                    .border(
                        width = 2.dp,
                        color = Color(0xFFFFE082),
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // Ручка-«полоска» сверху (визуально намекает, что можно тащить вниз)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(48.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFD0D0D0))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Заголовок
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "💬", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Спроси у $displayName",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "✕",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { panelOpen = false }
                                .padding(6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Список вопросов — листается
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(adviceList, key = { it.title }) { advice ->
                            FinCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentSpeech = advice.response
                                        panelOpen = false
                                    },
                                backgroundColor = Color(0xFFFBFBFB)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
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
            }
        }
    }
}
