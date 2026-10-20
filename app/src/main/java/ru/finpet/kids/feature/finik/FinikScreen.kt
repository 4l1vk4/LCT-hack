package ru.finpet.kids.feature.finik

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import ru.finpet.kids.core.designsystem.CatCryIcon
import ru.finpet.kids.core.designsystem.CloudIcon
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import ru.finpet.kids.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import androidx.compose.ui.text.style.TextAlign
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.domain.calculator.PetEconomyCalculator
import ru.finpet.kids.feature.finik.components.FinikDailyTaskCard
import ru.finpet.kids.feature.finik.components.FinikGoalCard
import ru.finpet.kids.feature.finik.components.FinikRunawaySection
import ru.finpet.kids.feature.finik.components.FinikStatsCard
import ru.finpet.kids.feature.finik.sheets.DEFAULT_SCRIPTED_ADVICES
import ru.finpet.kids.feature.finik.sheets.FinikAdviceSheet
import ru.finpet.kids.feature.finik.sheets.FinikTasksSheet

/**
 * Главный экран питомца Финни:
 * - Комната с анимированным котиком в кресле или секция возвращения сбежавшего питомца.
 * - Карточка характеристик (сытость, энергия, здоровье) без лишних эффектов.
 * - Карточка активной цели накопления с улучшенным контрастным прогресс-баром.
 * - Карточка задачи дня в строгом пиксельном стиле без оранжевых рамок и без эффектов ховера.
 */
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
    onNavigateToGoals: ((String?) -> Unit)? = null,
    onNavigateToMap: (() -> Unit)? = null,
    onNavigateToTask: ((CalendarNoteEntity) -> Unit)? = null,
    openAdvice: Boolean = false,
    onAdviceDismissed: () -> Unit = {},
    onAskQuestion: () -> Unit = {},
    onPlaceFoodBowl: () -> Unit = {},
    onToggleNote: (CalendarNoteEntity) -> Unit = {},
    isNight: Boolean = false
) {
    var panelOpen by remember { mutableStateOf(false) }

    LaunchedEffect(openAdvice) {
        if (openAdvice) {
            panelOpen = true
        }
    }

    var showWardrobe by remember { mutableStateOf(false) }
    var showTasksMenu by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var hiddenRoomItems by remember {
        mutableStateOf(RoomDecorationPreferences.getHiddenItems(context))
    }

    val satiety = profile?.satiety ?: 80
    val health = profile?.health ?: 90
    val isPetRunaway = profile?.isPetRunaway == true
    val mood = if (isPetRunaway) 0 else (profile?.mood ?: 70)
    val displayName = petName ?: profile?.petName ?: "Финни"
    val uiScale = LocalUiScale.current

    // Взвешенное общее самочувствие котика
    val effectiveMood = remember(mood, satiety, health) {
        PetEconomyCalculator.calculateEffectiveMood(mood, satiety, health)
    }

    // Реплика при запуске экрана
    val initialSpeech = remember(isPetRunaway, effectiveMood, displayName) {
        when {
            isPetRunaway -> "Мяу... Мне было так грустно, что я пошел прогуляться. Поставь миску корма у двери!"
            effectiveMood >= 80 -> "Мурр! Я полон сил и готов копить на мечту вместе с тобой! Нажми на меня, если хочешь о чём-то спросить."
            effectiveMood >= 50 -> "Привет! Не забудь покормить меня и заглянуть в Школу. Можешь нажать на меня и спросить что-нибудь!"
            else -> "Мяу... Мне немного грустно. Давай купим вкусняшку в Лавке? А ещё можешь нажать на меня и спросить что-нибудь!"
        }
    }
    var currentSpeech by remember(initialSpeech) { mutableStateOf(initialSpeech) }

    val activeGoal = remember(goals, profile?.activeGoalId) {
        goals.find { it.id == profile?.activeGoalId } ?: goals.firstOrNull()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8DAC2)) // Тёплый фермерский фон Stardew
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 104.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Облачко мыслей котика в аутентичном 9-slice пиксельном стиле
            item(key = "speech_bubble") {
                PixelPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isPetRunaway) {
                                panelOpen = true
                            }
                        },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        if (isPetRunaway) {
                            CatCryIcon(modifier = Modifier.size((22.dp * uiScale).coerceIn(16.dp, 24.dp)))
                        } else {
                            CloudIcon(modifier = Modifier.size((22.dp * uiScale).coerceIn(16.dp, 24.dp)))
                        }
                        Spacer(modifier = Modifier.width((8.dp * uiScale).coerceIn(4.dp, 8.dp)))
                        Text(
                            text = currentSpeech,
                            fontSize = (16f * uiScale).coerceIn(12f, 17f).sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            lineHeight = (22f * uiScale).coerceIn(16f, 23f).sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Котик в комнате либо пустая комната с сообщением о возврате питомца под ней
            item(key = "finik_character") {
                val bgRes = if (isNight) R.drawable.cats_background_night else R.drawable.cats_background

                // Контейнер комнаты с соотношением сторон оригинального фона 1073:406.
                // На экранах меньшей ширины картинка выравнивается по центру (обрезаясь симметрично слева и справа).
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1073f / 406f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val containerWidth = maxWidth
                    val containerHeight = maxHeight

                    // 1. Фоновое изображение комнаты (день / ночь)
                    Image(
                        painter = painterResource(id = bgRes),
                        contentDescription = "Комната питомца",
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    )

                    // 2. Имя питомца на подвесной деревянной табличке на стене
                    // Координаты таблички в исходном холсте 1073x406:
                    // X: 395..666 (36.81%..62.07%), Y: 64..127 (15.76%..31.28%)
                    val boardW = containerWidth * (272f / 1073f)
                    val boardH = containerHeight * (65f / 405f)
                    val boardLeft = containerWidth * (398f / 1073f)
                    val boardTop = containerHeight * (58f / 406f)

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = boardLeft, y = boardTop)
                            .size(width = boardW, height = boardH),
                        contentAlignment = Alignment.Center
                    ) {
                        val baseFontSize = when {
                            displayName.length > 12 -> 11f
                            displayName.length > 8  -> 13f
                            else                    -> 15f
                        }
                        val nameFontSize = (baseFontSize * uiScale).coerceIn(8f, 16f).sp
                        val letterSpacing = (1f * uiScale).coerceIn(0.2f, 1.2f).sp
                        Text(
                            text = displayName.uppercase(),
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = nameFontSize,
                            color = PixelTextDark,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            letterSpacing = letterSpacing
                        )
                    }

                    // 3. Интерактивный хитбокс шкафа (открывает гардероб)
                    // В исходном холсте 1073x406 шкаф расположен в координатах:
                    // X: 830..1050 (77.35%..97.85%), Y: 64..354 (15.76%..87.19%)
                    val wardrobeW = containerWidth * (220f / 1073f)
                    val wardrobeH = containerHeight * (290f / 406f)
                    val wardrobeLeft = containerWidth * (830f / 1073f)
                    val wardrobeTop = containerHeight * (64f / 406f)

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = wardrobeLeft, y = wardrobeTop)
                            .size(width = wardrobeW, height = wardrobeH)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                showWardrobe = true
                            }
                    )

                    // Анимированная парящая пиксельная вывеска «ГАРДЕРОБ» над шкафом
                    val wardrobePlateW = containerWidth * (237f / 1073f)
                    val wardrobePlateH = containerHeight * (63f / 406f)
                    val wardrobePlateLeft = containerWidth * (819f / 1073f)
                    val wardrobePlateTop = containerHeight * (18f / 406f)

                    val wardrobePlateAnim = rememberInfiniteTransition(label = "wardrobe_plate_anim")
                    val wardrobeBobY by wardrobePlateAnim.animateFloat(
                        initialValue = -3f,
                        targetValue = 3f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "wardrobe_bob"
                    )

                    Image(
                        painter = painterResource(id = if (isNight) R.drawable.label_wardrobe_night else R.drawable.label_wardrobe),
                        contentDescription = "Гардероб",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = wardrobePlateLeft, y = wardrobePlateTop)
                            .size(width = wardrobePlateW, height = wardrobePlateH)
                            .graphicsLayer {
                                translationY = wardrobeBobY.dp.toPx()
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                showWardrobe = true
                            }
                    )

                    // Предметы накоплений, купленные пользователем при достижении цели (с возможностью спрятать/показать через гардероб)
                    val isDemo = profile?.isDemoMode == true
                    val isBallPurchased = (isDemo || goals.any { it.id == "goal_ball" && (it.isReached || it.savedAmount >= it.targetCost) }) && ("goal_ball" !in hiddenRoomItems)
                    val isHeadphonesPurchased = (isDemo || goals.any { it.id == "goal_headphones" && (it.isReached || it.savedAmount >= it.targetCost) }) && ("goal_headphones" !in hiddenRoomItems)
                    val isScooterPurchased = (isDemo || goals.any { it.id == "goal_scooter" && (it.isReached || it.savedAmount >= it.targetCost) }) && ("goal_scooter" !in hiddenRoomItems)

                    // Самокат рядом с окном
                    if (isScooterPurchased) {
                        val scooterH = containerHeight * (200f / 406f)
                        val scooterW = scooterH * (34f / 32f)
                        val scooterLeft = containerWidth * (200f / 1073f)
                        val scooterTop = containerHeight * (120f / 406f)
                        Image(
                            painter = painterResource(id = R.drawable.samokat),
                            contentDescription = "Городской самокат",
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = scooterLeft, y = scooterTop)
                                .size(width = scooterW, height = scooterH)
                        )
                    }

                    // 4. Котик на подушке/лежанке (если кот сбежал — комната пустая!)
                    if (!isPetRunaway) {
                        val currentStage = profile?.growthStage ?: "BABY"
                        val (catHMultiplier, catTopOffsetMultiplier) = when (currentStage) {
                            "TEEN" -> Pair(330f / 406f, 61f / 406f)
                            "ADULT" -> Pair(365f / 406f, 10f / 406f)
                            else -> Pair(290f / 406f, 103f / 406f) // BABY
                        }

                        val catH = containerHeight * catHMultiplier
                        val catW = catH * (224f / 448f)
                        val catLeft = (containerWidth * 0.5f) - (catW / 2)
                        val catTop = containerHeight * catTopOffsetMultiplier

                        FinikInArmchair(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = catLeft, y = catTop)
                                .size(width = catW, height = catH),
                            skinId = selectedSkin ?: "cat_black",
                            accessoryId = accessoryId,
                            mood = when {
                                effectiveMood < 40 -> "SAD"
                                effectiveMood >= 70 -> "HAPPY"
                                else -> "NEUTRAL"
                            },
                            stage = currentStage,
                            enabled = animationsEnabled,
                            onClick = {
                                panelOpen = true
                            }
                        )
                    }

                    // Наушники на подушке кота
                    if (isHeadphonesPurchased) {
                        val hpSize = containerHeight * (90f / 406f)
                        val hpLeft = containerWidth * (545f / 1073f)
                        val hpTop = containerHeight * (260f / 406f)
                        Image(
                            painter = painterResource(id = R.drawable.headphones),
                            contentDescription = "Беспроводные наушники",
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = hpLeft, y = hpTop)
                                .size(hpSize)
                        )
                    }

                    // Футбольный мяч на полу слева от котика
                    if (isBallPurchased) {
                        val ballSize = containerHeight * (80f / 406f)
                        val ballLeft = containerWidth * (375f / 1073f)
                        val ballTop = containerHeight * (310f / 406f)
                        Image(
                            painter = painterResource(id = R.drawable.ball),
                            contentDescription = "Футбольный мяч",
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = ballLeft, y = ballTop)
                                .size(ballSize)
                        )
                    }

                    // Миска с едой на полу комнаты, если питомец сбежал и миска сегодня поставлена
                    if (isPetRunaway && profile?.bowlPlacedToday == true) {
                        val miskaSize = 44.dp
                        val miskaLeft = 50.dp
                        val miskaTop = containerHeight * (270f / 406f)
                        Image(
                            painter = painterResource(id = R.drawable.miska),
                            contentDescription = "Миска с кормом",
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = miskaLeft, y = miskaTop)
                                .size(miskaSize)
                        )
                    }
                }


                // Если котик убежал — выводим карточку с сообщением и кнопкой вернуть миску прямо под комнатой
                if (isPetRunaway) {
                    FinikRunawaySection(
                        petName = displayName,
                        bowlPlacedToday = profile?.bowlPlacedToday == true,
                        balance = profile?.balance ?: 0,
                        onPlaceFoodBowl = onPlaceFoodBowl,
                        onNavigateToHomeChores = { onNavigateToGoals?.invoke(null) }
                    )
                }
            }

            // Карточка характеристик питомца (стадия развития вынесена в диалог вопросов)
            item(key = "stats_panel") {
                FinikStatsCard(
                    displayName = displayName,
                    effectiveMood = effectiveMood,
                    satiety = satiety,
                    mood = mood,
                    health = health,
                    isPetRunaway = isPetRunaway
                )
            }

            // Карточка активной цели накопления
            item(key = "goal_bar") {
                if (activeGoal != null) {
                    FinikGoalCard(
                        activeGoal = activeGoal,
                        onNavigateToGoals = onNavigateToGoals
                    )
                }
            }

            // Карточка задачи дня
            item(key = "daily_task_card") {
                FinikDailyTaskCard(
                    todayNotes = todayNotes,
                    onShowTasksMenu = { showTasksMenu = true },
                    onNavigateToTask = onNavigateToTask,
                    onNavigateToMap = onNavigateToMap,
                    onOpenQuestions = { panelOpen = true },
                    isPetRunaway = isPetRunaway
                )
            }
        }

        // Шторка диалогов и вопросов (со стадией развития перед вопросами)
        if (panelOpen) {
            FinikAdviceSheet(
                displayName = displayName,
                growthStage = profile?.growthStage ?: "BABY",
                carePoints = profile?.carePoints ?: 0,
                advices = DEFAULT_SCRIPTED_ADVICES,
                onSelectAdvice = { response ->
                    onAskQuestion()
                    currentSpeech = response
                    panelOpen = false
                    onAdviceDismissed()
                },
                onDismiss = {
                    panelOpen = false
                    onAdviceDismissed()
                }
            )
        }

        // Шторка задач на день
        if (showTasksMenu) {
            FinikTasksSheet(
                todayNotes = todayNotes,
                onToggleNote = onToggleNote,
                onNavigateToMap = onNavigateToMap,
                onNavigateToTask = onNavigateToTask,
                onOpenQuestions = { panelOpen = true },
                onDismiss = { showTasksMenu = false },
                isPetRunaway = isPetRunaway
            )
        }

        // Диалог гардероба питомца и расстановки предметов комнаты
        if (showWardrobe) {
            WardrobeDialog(
                currentAccessoryIds = parseAccessories(accessoryId).map { it.id }.toSet(),
                petName = displayName,
                goals = goals,
                hiddenRoomItems = hiddenRoomItems,
                isDemoMode = profile?.isDemoMode == true,
                onToggleAccessory = { accId -> onToggleAccessory(accId) },
                onToggleRoomItem = { itemId ->
                    hiddenRoomItems = RoomDecorationPreferences.toggleItemVisibility(context, itemId)
                },
                onDismiss = { showWardrobe = false }
            )
        }
    }
}
