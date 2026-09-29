package ru.finpet.kids.feature.finik

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
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
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import ru.finpet.kids.core.domain.calculator.PetEconomyCalculator
import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetPlan
import ru.finpet.kids.core.designsystem.CoinIcon
import ru.finpet.kids.core.designsystem.FinCard
import ru.finpet.kids.core.designsystem.FreshGreen
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.SkyBlue
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.designsystem.TextSecondary
import ru.finpet.kids.feature.budget.BudgetPlanVsFactDialog
import ru.finpet.kids.feature.budget.BudgetPlanningDialog
import ru.finpet.kids.core.designsystem.OrbStatBar

data class ScriptedAdvice(
    val title: String,
    val icon: String,
    val response: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinikScreen(
    profile: ProfileEntity?,
    goals: List<GoalEntity> = emptyList(),
    todayNotes: List<CalendarNoteEntity> = emptyList(),
    selectedSkin: String? = null,
    petName: String? = null,
    accessoryId: String = "none",
    onToggleAccessory: (String) -> Unit = {},
    animationsEnabled: Boolean = true,
    onPetTapped: () -> Unit = {},
    onNavigateToGoals: (() -> Unit)? = null,
    onNavigateToMap: (() -> Unit)? = null,
    onAskQuestion: () -> Unit = {},
    onPlaceFoodBowl: () -> Unit = {},
    onToggleNote: (CalendarNoteEntity) -> Unit = {},
    isBudgetConfirmed: Boolean = true,
    onNavigateToPlans: (() -> Unit)? = null,
    currentPeriod: PeriodEntity? = null,
    onConfirmBudget: (BudgetPlan) -> Unit = {}
) {
    val satiety = profile?.satiety ?: 100
    val mood = profile?.mood ?: 80
    val health = profile?.health ?: 100
    val displayName = petName ?: profile?.petName ?: "Финни"

    // Сытость и здоровье скрыто влияют на единый показатель настроения
    val effectiveMood = remember(mood, satiety, health) {
        PetEconomyCalculator.calculateEffectiveMood(mood, satiety, health)
    }

    val isPetRunaway = (profile?.isPetRunaway == true) || (effectiveMood <= 0)

    val defaultGreeting = if (isPetRunaway) {
        if (profile?.bowlPlacedToday == true) {
            "Миска с кормом стоит у двери! Котик чувствует заботу и скоро вернётся (через 1–3 дня)."
        } else {
            "Кот убежал! Настроение упало до 0%. Скорее поставь миску с кормом у двери, чтобы вернуть питомца!"
        }
    } else if (!isBudgetConfirmed) {
        "Давай распределим монетки по баночкам! 📜"
    } else {
        "Привет! Я твой финансовый помощник $displayName. Нажми на меня, чтобы задать вопрос!"
    }
    var currentSpeech by remember { mutableStateOf(defaultGreeting) }
    var panelOpen by remember { mutableStateOf(false) }
    var showTasksMenu by remember { mutableStateOf(false) }
    var showWardrobe by remember { mutableStateOf(false) }
    var showBudgetPlanningDialog by remember { mutableStateOf(false) }
    var showPlanVsFactDialog by remember { mutableStateOf(false) }

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
            contentPadding = PaddingValues(top = 10.dp, bottom = 104.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(key = "speech_bubble") {
                // Облачко диалога Финни
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isPetRunaway) Color(0xFFFFEBEE) else Color(0xFFFFF9E6))
                        .border(2.dp, if (isPetRunaway) Color(0xFFFFCDD2) else Color(0xFFFFE082), RoundedCornerShape(22.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (isPetRunaway) "😿 $currentSpeech" else "💬 $currentSpeech",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )
                }
            }

            item(key = "finik_character") {
                if (isPetRunaway) {
                    // Состояние «Кот убежал» при достижении 0% настроения
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFEBEE))
                                .border(3.dp, Color(0xFFFFCDD2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "😿", fontSize = 68.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Кот убежал!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFC62828)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Настроение упало до 0%. Поставь миску корма у двери (цена x2 от обычного корма: 60 монет). Питомец вернётся через 1–3 дня, если ставить миску каждый день!",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (profile?.bowlPlacedToday == true) {
                            FinCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                backgroundColor = Color(0xFFE8F5E9),
                                borderColor = Color(0xFFAED581)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🥣", fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Миска с кормом стоит у двери!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = FreshGreen
                                        )
                                        Text(
                                            text = "Кот чувствует заботу. Проверь завтра, вернулся ли он!",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        } else {
                            val canAffordBowl = (profile?.balance ?: 0) >= 60
                            Button(
                                onClick = onPlaceFoodBowl,
                                enabled = canAffordBowl,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JoyOrange,
                                    disabledContainerColor = Color(0xFFCCCCCC)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "🥣 Поставить миску корма (60 🪙)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }

                            if (!canAffordBowl) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Не хватает монет (нужно 60 🪙). Загляни в Школу или выполни дела по дому!",
                                    fontSize = 12.sp,
                                    color = Color(0xFFD32F2F),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        FinikInArmchair(
                            modifier = Modifier.padding(vertical = 4.dp),
                            skinId = selectedSkin ?: "cat_black",
                            accessoryId = accessoryId,
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

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 8.dp, bottom = 8.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(JoyOrange)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { showWardrobe = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👔", fontSize = 22.sp)
                        }
                    }
                }
            }

            item(key = "stats_panel") {
                FinCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 6.dp),
                    backgroundColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Состояние $displayName",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            // Общий процент настроения — сразу видно, к чему стремиться
                            Text(
                                text = "$effectiveMood%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = remember(effectiveMood) {
                                    when {
                                        effectiveMood >= 70 -> FreshGreen
                                        effectiveMood >= 40 -> JoyOrange
                                        else -> Color(0xFFFF5252)
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // 1. Сытость
                        OrbStatBar(
                            label = "Сытость",
                            icon = "🍎",
                            value = satiety,
                            tintColor  = JoyOrange,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Энергия (настроение)
                        OrbStatBar(
                            label = "Энергия",
                            icon = "⚡",
                            value = mood,
                            tintColor  = SkyBlue,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // 3. Здоровье
                        OrbStatBar(
                            label = "Здоровье",
                            icon = "❤️",
                            value = health,
                            tintColor  = FreshGreen,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Следи за всеми тремя — от них зависит общее настроение!",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            item(key = "goal_bar") {
                // Компактная планка прогресса по цели в копилке
                if (activeGoal != null) {
                    val progress = (activeGoal.savedAmount.toFloat() / activeGoal.targetCost.toFloat()).coerceIn(0f, 1f)
                    val percent = (progress * 100).toInt()
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
                            .padding(bottom = 6.dp)
                            .then(if (onNavigateToGoals != null) Modifier.clickable { onNavigateToGoals() } else Modifier),
                        backgroundColor = Color.White
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Text(text = goalEmoji, fontSize = 17.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Цель: ${activeGoal.title}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${activeGoal.savedAmount} / ${activeGoal.targetCost} ",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = JoyOrange
                                    )
                                    CoinIcon(modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "($percent%)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeGoal.isReached) FreshGreen else TextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(7.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(13.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(Color(0xFFEEEEEE))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(animatedGoalProgress)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(goalBrush)
                                )
                            }
                        }
                    }
                }
            }

            item(key = "daily_task_card") {
                // Карточка задачи дня под целью накопления
                val pendingTask = todayNotes.firstOrNull { !it.isCompleted }
                val completedCount = todayNotes.count { it.isCompleted }
                val totalCount = todayNotes.size

                if (todayNotes.isNotEmpty()) {
                    FinCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { showTasksMenu = true },
                        backgroundColor = if (pendingTask == null) Color(0xFFF1F8E9) else Color.White,
                        borderColor = if (pendingTask == null) Color(0xFFAED581) else Color(0xFFFFD54F)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (pendingTask == null) "🌟" else "📋",
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (pendingTask == null) "Все задачи выполнены!" else "Задача дня",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pendingTask == null) FreshGreen else TextPrimary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (pendingTask == null) Color(0xFFDCEDC8) else Color(0xFFFFF3E0),
                                    modifier = Modifier.clickable { showTasksMenu = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$completedCount/$totalCount",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (pendingTask == null) FreshGreen else JoyOrange
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(text = "👉", fontSize = 10.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (pendingTask != null) {
                                val taskIcon = when (pendingTask.category) {
                                    "PET_FOOD" -> "🍲"
                                    "SCHOOL" -> "📚"
                                    "GROCERIES" -> "🛒"
                                    "PET_QUESTION" -> "🐱"
                                    else -> "📝"
                                }

                                val taskBadge = when (pendingTask.category) {
                                    "PET_FOOD" -> "Лавка • 30 🪙"
                                    "SCHOOL" -> "Школа • 2 урока"
                                    "GROCERIES" -> "Лавка • Сдача"
                                    "PET_QUESTION" -> "У питомца"
                                    else -> if (pendingTask.cost > 0) "${pendingTask.cost} 🪙" else "Заметка"
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFFDE7))
                                        .border(1.dp, Color(0xFFFFF59D), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = taskIcon, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = pendingTask.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = JoyOrange,
                                        modifier = if (pendingTask.category in listOf("PET_FOOD", "GROCERIES", "SCHOOL") && onNavigateToMap != null) {
                                            Modifier.clickable { onNavigateToMap() }
                                        } else if (pendingTask.category == "PET_QUESTION") {
                                            Modifier.clickable { panelOpen = true }
                                        } else Modifier
                                    ) {
                                        Text(
                                            text = taskBadge,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Все дела и уроки сделаны, Финни счастлив! 🎉",
                                    fontSize = 12.sp,
                                    color = TextSecondary
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
                                        onAskQuestion()
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

        if (showTasksMenu) {
            val completedCount = todayNotes.count { it.isCompleted }
            val totalCount = todayNotes.size

            ModalBottomSheet(
                onDismissRequest = { showTasksMenu = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📋", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Задачи на сегодня",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Выполнено $completedCount из $totalCount",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Text(
                            text = "✕",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { showTasksMenu = false }
                                .padding(6.dp)
                        )
                    }

                    LinearProgressIndicator(
                        progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f },
                        color = FreshGreen,
                        trackColor = Color(0xFFEEEEEE),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(todayNotes, key = { it.id }) { note ->
                            val isSystemTask = note.category in listOf("PET_FOOD", "SCHOOL", "GROCERIES", "PET_QUESTION")
                            val taskIcon = when (note.category) {
                                "PET_FOOD" -> "🍲"
                                "SCHOOL" -> "📚"
                                "GROCERIES" -> "🛒"
                                "PET_QUESTION" -> "🐱"
                                else -> "📝"
                            }
                            val taskBadge = when (note.category) {
                                "PET_FOOD" -> "Лавка • 30 🪙"
                                "SCHOOL" -> "Школа"
                                "GROCERIES" -> if (note.isCompleted && note.cost > 0) "+${note.cost} 🪙" else "Лавка • Сдача"
                                "PET_QUESTION" -> "У питомца"
                                else -> if (note.cost > 0) "${note.cost} 🪙" else "Заметка"
                            }

                            FinCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = if (note.isCompleted) Color(0xFFF9FBE7) else Color(0xFFFAFAFA),
                                borderColor = if (note.isCompleted) Color(0xFFAED581) else Color(0xFFE0E0E0)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Checkbox(
                                            checked = note.isCompleted,
                                            onCheckedChange = if (isSystemTask) null else { { onToggleNote(note) } },
                                            enabled = !isSystemTask,
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = FreshGreen,
                                                uncheckedColor = TextSecondary,
                                                disabledCheckedColor = FreshGreen,
                                                disabledUncheckedColor = Color(0xFFBDBDBD)
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = taskIcon, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = note.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (note.isCompleted) TextSecondary else TextPrimary,
                                            textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    if (note.isCompleted) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFE8F5E9)
                                        ) {
                                            Text(
                                                text = if (note.category == "GROCERIES" && note.cost > 0) "✅ +${note.cost} 🪙" else "✅ Готово",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FreshGreen,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFFF3E0),
                                            modifier = if (note.category in listOf("PET_FOOD", "GROCERIES", "SCHOOL") && onNavigateToMap != null) {
                                                Modifier.clickable {
                                                    showTasksMenu = false
                                                    onNavigateToMap()
                                                }
                                            } else if (note.category == "PET_QUESTION") {
                                                Modifier.clickable {
                                                    showTasksMenu = false
                                                    panelOpen = true
                                                }
                                            } else Modifier
                                        ) {
                                            Text(
                                                text = taskBadge,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = JoyOrange,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showTasksMenu = false },
                        colors = ButtonDefaults.buttonColors(containerColor = JoyOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text("Понятно 👍", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Диалог планирования бюджета (Концепт 1 «Волшебные баночки заботы», ТЗ 2.5.5)
        if (showBudgetPlanningDialog) {
            val initialPlan = remember(currentPeriod) {
                BudgetPlan(
                    plannedMandatory = currentPeriod?.plannedMandatory ?: 0,
                    plannedOptional = currentPeriod?.plannedOptional ?: 0,
                    plannedSavings = currentPeriod?.plannedSavings ?: 0
                )
            }
            BudgetPlanningDialog(
                balance = profile?.balance ?: 0,
                currentDay = profile?.currentPeriodIndex ?: 1,
                initialPlan = initialPlan,
                onConfirm = { newPlan ->
                    onConfirmBudget(newPlan)
                    showBudgetPlanningDialog = false
                },
                onDismiss = { showBudgetPlanningDialog = false }
            )
        }

        // Диалог «План vs Факт» со Звёздами Бюджета (ТЗ 2.5.5)
        if (showPlanVsFactDialog && currentPeriod != null) {
            val plan = remember(currentPeriod) {
                BudgetPlan(
                    plannedMandatory = currentPeriod.plannedMandatory,
                    plannedOptional = currentPeriod.plannedOptional,
                    plannedSavings = currentPeriod.plannedSavings
                )
            }
            val actual = remember(currentPeriod) {
                ActualExpenses(
                    actualMandatory = currentPeriod.actualMandatory,
                    actualOptional = currentPeriod.actualOptional,
                    actualSavings = currentPeriod.actualSavings
                )
            }
            val compliance = remember(plan, actual) {
                PetEconomyCalculator.calculateCompliance(plan, actual)
            }
            BudgetPlanVsFactDialog(
                currentDay = profile?.currentPeriodIndex ?: 1,
                plan = plan,
                actual = actual,
                compliance = compliance,
                onEditPlan = {
                    showPlanVsFactDialog = false
                    showBudgetPlanningDialog = true
                },
                onDismiss = { showPlanVsFactDialog = false }
            )
        }

        if (showWardrobe) {
            WardrobeDialog(
                currentAccessoryIds = parseAccessories(accessoryId).map { it.id }.toSet(),
                petName = displayName,
                onToggle = { accId ->
                    onToggleAccessory(accId)
                },
                onDismiss = { showWardrobe = false }
            )
        }
    }
}
