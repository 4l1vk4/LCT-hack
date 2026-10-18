package ru.finpet.kids.feature.finik

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import ru.finpet.kids.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.kids.core.data.local.entity.CalendarNoteEntity
import ru.finpet.kids.core.data.local.entity.GoalEntity
import ru.finpet.kids.core.data.local.entity.PeriodEntity
import ru.finpet.kids.core.data.local.entity.ProfileEntity
import androidx.compose.ui.text.style.TextAlign
import ru.finpet.kids.core.designsystem.AppFontFamily
import ru.finpet.kids.core.designsystem.JoyOrange
import ru.finpet.kids.core.designsystem.PixelPanel
import ru.finpet.kids.core.designsystem.PixelTextDark
import ru.finpet.kids.core.designsystem.TextPrimary
import ru.finpet.kids.core.domain.calculator.PetEconomyCalculator
import ru.finpet.kids.core.domain.model.ActualExpenses
import ru.finpet.kids.core.domain.model.BudgetPlan
import ru.finpet.kids.feature.budget.BudgetPlanVsFactDialog
import ru.finpet.kids.feature.budget.BudgetPlanningDialog
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
    onNavigateToPlans: (() -> Unit)? = null,
    onNavigateToTask: ((CalendarNoteEntity) -> Unit)? = null,
    openAdvice: Boolean = false,
    onAdviceDismissed: () -> Unit = {},
    onAskQuestion: () -> Unit = {},
    onPlaceFoodBowl: () -> Unit = {},
    onToggleNote: (CalendarNoteEntity) -> Unit = {},
    isBudgetConfirmed: Boolean = true,
    currentPeriod: PeriodEntity? = null,
    onConfirmBudget: (BudgetPlan) -> Unit = {},
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
    var showBudgetPlanningDialog by remember { mutableStateOf(false) }
    var showPlanVsFactDialog by remember { mutableStateOf(false) }

    val satiety = profile?.satiety ?: 80
    val health = profile?.health ?: 90
    val isPetRunaway = profile?.isPetRunaway == true
    val mood = if (isPetRunaway) 0 else (profile?.mood ?: 70)
    val displayName = petName ?: profile?.petName ?: "Финни"

    // Взвешенное общее самочувствие котика
    val effectiveMood = remember(mood, satiety, health) {
        PetEconomyCalculator.calculateEffectiveMood(mood, satiety, health)
    }

    // Реплика при запуске экрана
    val initialSpeech = remember(isPetRunaway, effectiveMood, displayName) {
        when {
            isPetRunaway -> "Мяу... Мне было так грустно, что я убежал. Поставь миску корма у двери!"
            effectiveMood >= 80 -> "Мурр! Я полон сил и готов копить на мечту вместе с тобой!"
            effectiveMood >= 50 -> "Привет! Не забудь покормить меня и проверить уроки в Школе!"
            else -> "Мяу... Мне немного грустно. Давай купим вкусняшку в Лавке?"
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
                        .padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
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
                        val nameFontSize = when {
                            displayName.length > 12 -> 11.sp
                            displayName.length > 8  -> 13.sp
                            else                    -> 15.sp
                        }
                        Text(
                            text = displayName.uppercase(),
                            fontFamily = AppFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = nameFontSize,
                            color = PixelTextDark,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            letterSpacing = 1.sp
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

            // Карточка характеристик питомца
            item(key = "stats_panel") {
                FinikStatsCard(
                    displayName = displayName,
                    effectiveMood = effectiveMood,
                    satiety = satiety,
                    mood = mood,
                    health = health,
                    isPetRunaway = isPetRunaway,
                    carePoints = profile?.carePoints ?: 0,
                    growthStage = profile?.growthStage ?: "BABY"
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

        // Шторка диалогов и вопросов
        if (panelOpen) {
            FinikAdviceSheet(
                displayName = displayName,
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

        // Диалог планирования бюджета
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
                isConfirmed = isBudgetConfirmed,
                onConfirm = { newPlan ->
                    onConfirmBudget(newPlan)
                    showBudgetPlanningDialog = false
                },
                onDismiss = { showBudgetPlanningDialog = false }
            )
        }

        // Диалог «План vs Факт»
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
                    actualSavings = currentPeriod.actualSavings,
                    withdrawnSavings = currentPeriod.withdrawnSavings,
                    borrowedFromOptional = currentPeriod.borrowedFromOptional,
                    hasPurchasedFood = currentPeriod.actualMandatory >= PetEconomyCalculator.MIN_FOOD_COST,
                    hasPurchasedWater = currentPeriod.actualMandatory >= PetEconomyCalculator.MIN_WATER_COST
                )
            }
            val compliance = remember(plan, actual, profile) {
                PetEconomyCalculator.calculateCompliance(
                    plan = plan,
                    actual = actual,
                    isPetRunaway = profile?.isPetRunaway == true,
                    petMood = if (profile?.isPetRunaway == true) 0 else (profile?.mood ?: 70),
                    petSatiety = profile?.satiety ?: 100,
                    petHealth = profile?.health ?: 100
                )
            }
            BudgetPlanVsFactDialog(
                currentDay = profile?.currentPeriodIndex ?: 1,
                plan = plan,
                actual = actual,
                compliance = compliance,
                onEditPlan = if (isBudgetConfirmed) null else {
                    {
                        showPlanVsFactDialog = false
                        showBudgetPlanningDialog = true
                    }
                },
                onDismiss = { showPlanVsFactDialog = false }
            )
        }

        // Диалог гардероба питомца
        if (showWardrobe) {
            WardrobeDialog(
                currentAccessoryIds = parseAccessories(accessoryId).map { it.id }.toSet(),
                petName = displayName,
                onToggle = { accId -> onToggleAccessory(accId) },
                onDismiss = { showWardrobe = false }
            )
        }
    }
}
