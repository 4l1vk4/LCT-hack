package ru.finpet.kids.feature.finik

import androidx.annotation.DrawableRes
import ru.finpet.kids.R

/**
 * Аксессуар для питомца.
 * Координаты в долях от размера кота (0..1):
 *   cx, cy — центр аксессуара (0.5 = центр по горизонтали/вертикали)
 *   widthRatio — ширина аксессуара относительно ширины кота
 */
data class Accessory(
    val id: String,
    val title: String,
    @DrawableRes val drawableRes: Int,
    val cx: Float,
    val cy: Float,
    val widthRatio: Float
)

// Каталог аксессуаров. Позиции подобраны примерно, откалибруй после теста.
val ACCESSORIES: List<Accessory> = listOf(
    Accessory(
        id = "none",
        title = "Без аксессуара",
        drawableRes = 0,
        cx = 0f, cy = 0f, widthRatio = 0f
    ),
    Accessory(
        id = "glasses",
        title = "Очки",
        drawableRes = R.drawable.glasses,
        cx = 0.50f,
        cy = 0.33f,
        widthRatio = 0.45f
    ),
//    Accessory(
//        id = "tie",
//        title = "Галстук",
//        drawableRes = R.drawable.acc_tie,
//        cx = 0.50f,
//        cy = 0.62f,      // на груди
//        widthRatio = 0.20f
//    ),
)

fun findAccessory(id: String?): Accessory =
    ACCESSORIES.find { it.id == id } ?: ACCESSORIES.first()