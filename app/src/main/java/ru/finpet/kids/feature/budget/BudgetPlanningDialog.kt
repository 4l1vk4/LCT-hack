package ru.finpet.kids.feature.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finpet.kids.R
import ru.finpet.kids.core.designsystem.FinPetTheme
import ru.finpet.kids.core.designsystem.KeepScreenImmersive
import ru.finpet.kids.core.designsystem.LocalUiScale
import ru.finpet.kids.core.designsystem.PixelBlueWater
import ru.finpet.kids.core.designsystem.PixelGoldDark
import ru.finpet.kids.core.designsystem.PixelGreenCrop
import ru.finpet.kids.core.designsystem.StardewBoard
import ru.finpet.kids.core.domain.model.BudgetPlan
import ru.finpet.kids.feature.budget.components.BudgetConfirmedNotice
import ru.finpet.kids.feature.budget.components.BudgetDialogActions
import ru.finpet.kids.feature.budget.components.BudgetPresetsSection
import ru.finpet.kids.feature.budget.components.BudgetWalletStatusCard
import ru.finpet.kids.feature.budget.components.MagicJarCard

/**
 * Диалог планирования личного бюджета «волшебные баночки заботы» (ТЗ 2.5.5).
 * Ребенок раскладывает карманные монеты по 3 категориям:
 * 1. Обязательное (еда и уход за питомцем)
 * 2. Желаемое (игрушки и лакомства)
 * 3. Накопления (в копилку на большую цель)
 */
@Composable
fun BudgetPlanningDialog(
    balance: Int,
    currentDay: Int,
    initialPlan: BudgetPlan,
    isConfirmed: Boolean = false,
    onConfirm: (BudgetPlan) -> Unit,
    onDismiss: () -> Unit
) {
    // локальное состояние баночек до нажатия кнопки утверждения
    var mandatory by remember(initialPlan) { mutableIntStateOf(initialPlan.plannedMandatory) }
    var optional by remember(initialPlan) { mutableIntStateOf(initialPlan.plannedOptional) }
    var savings by remember(initialPlan) { mutableIntStateOf(initialPlan.plannedSavings) }
    var selectedPreset by remember { mutableStateOf<String?>("50/30/20") }

    val totalPlanned = mandatory + optional + savings
    val remainder = balance - totalPlanned
    val isOverBudget = remainder < 0
    val isValid = !isOverBudget && (totalPlanned > 0 || balance == 0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FinPetTheme {
            val uiScale = LocalUiScale.current
            KeepScreenImmersive()
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.94f)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                StardewBoard(
                    headerTitle = "ВОЛШЕБНЫЕ БАНОЧКИ",
                    headerIcon = "empty_jar"
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy((10.dp * uiScale).coerceIn(6.dp, 10.dp)),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        if (isConfirmed) {
                            item {
                                BudgetConfirmedNotice()
                            }
                        }

                        // остаток в кошельке и статус перерасхода
                        item {
                            BudgetWalletStatusCard(
                                balance = balance,
                                totalPlanned = totalPlanned,
                                remainder = remainder,
                                isOverBudget = isOverBudget
                            )
                        }

                        if (!isConfirmed) {
                            // готовые детские пресеты распределения в виде удобной и четкой сетки 2х2
                            item {
                                BudgetPresetsSection(
                                    balance = balance,
                                    selectedPreset = selectedPreset,
                                    onSelectPreset = { presetKey, m, o, s ->
                                        selectedPreset = presetKey
                                        mandatory = m
                                        optional = o
                                        savings = s
                                    }
                                )
                            }
                        }

                        // баночка 1: обязательные расходы (еда, здоровье)
                        item {
                            MagicJarCard(
                                jarRes = R.drawable.life_jar,
                                icon = "🍲",
                                title = "«Миска жизни»",
                                categoryLabel = "Обязательное",
                                subtitle = "Еда, вода, гигиена и здоровье питомца",
                                amount = mandatory,
                                walletBalance = balance,
                                color = PixelGreenCrop,
                                remainder = remainder,
                                isReadOnly = isConfirmed,
                                onMinus = {
                                    selectedPreset = null
                                    if (mandatory >= 10) mandatory -= 10
                                },
                                onPlus10 = {
                                    selectedPreset = null
                                    mandatory += 10
                                },
                                onPlus20 = {
                                    selectedPreset = null
                                    mandatory += 20
                                },
                                onSetAmount = { newAmt ->
                                    selectedPreset = null
                                    mandatory = newAmt
                                }
                            )
                        }

                        // баночка 2: желаемые расходы (игрушки, вкусняшки)
                        item {
                            MagicJarCard(
                                jarRes = R.drawable.joy_jar,
                                icon = "🎈",
                                title = "«Сундучок радости»",
                                categoryLabel = "Желаемое",
                                subtitle = "Лакомства, мячики, шарфик и веселье",
                                amount = optional,
                                walletBalance = balance,
                                color = PixelGoldDark,
                                remainder = remainder,
                                isReadOnly = isConfirmed,
                                onMinus = {
                                    selectedPreset = null
                                    if (optional >= 10) optional -= 10
                                },
                                onPlus10 = {
                                    selectedPreset = null
                                    optional += 10
                                },
                                onPlus20 = {
                                    selectedPreset = null
                                    optional += 20
                                },
                                onSetAmount = { newAmt ->
                                    selectedPreset = null
                                    optional = newAmt
                                }
                            )
                        }

                        // баночка 3: накопления в копилку
                        item {
                            MagicJarCard(
                                jarRes = R.drawable.saving_jar,
                                icon = "🏦",
                                title = "«Копилка мечты»",
                                categoryLabel = "В копилку",
                                subtitle = "Отчисления на заветную цель и домик",
                                amount = savings,
                                walletBalance = balance,
                                color = PixelBlueWater,
                                remainder = remainder,
                                isReadOnly = isConfirmed,
                                onMinus = {
                                    selectedPreset = null
                                    if (savings >= 10) savings -= 10
                                },
                                onPlus10 = {
                                    selectedPreset = null
                                    savings += 10
                                },
                                onPlus20 = {
                                    selectedPreset = null
                                    savings += 20
                                },
                                onSetAmount = { newAmt ->
                                    selectedPreset = null
                                    savings = newAmt
                                }
                            )
                        }

                        // кнопки закрытия и подтверждения плана
                        item {
                            BudgetDialogActions(
                                isConfirmed = isConfirmed,
                                isOverBudget = isOverBudget,
                                isValid = isValid,
                                onDismiss = onDismiss,
                                onConfirm = {
                                    onConfirm(
                                        BudgetPlan(
                                            plannedMandatory = mandatory,
                                            plannedOptional = optional,
                                            plannedSavings = savings
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
