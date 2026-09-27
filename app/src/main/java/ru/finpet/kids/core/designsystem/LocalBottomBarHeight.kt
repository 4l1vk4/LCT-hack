package ru.finpet.kids.core.designsystem

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Высота нижней панели навигации (NavBarWithHotspots).
 * MainScreen записывает сюда реальную высоту, экраны — читают
 * и добавляют её в contentPadding, чтобы контент не уезжал под панель.
 */
val LocalBottomBarHeight = compositionLocalOf<Dp> { 140.dp }