package ru.finpet.kids.feature.finik

import androidx.annotation.DrawableRes
import ru.finpet.kids.R

/**
 * Аксессуар для питомца.
 * Координаты в долях от размера кота (0..1):
 *   cx, cy — центр аксессуара (0.5 = центр по горизонтали/вертикали)
 *   widthRatio — ширина аксессуара относительно ширины кота
 */

data class AccessoryPlacement(
    val cx: Float,
    val cy: Float,
    val widthRatio: Float
)

data class Accessory(
    val id: String,
    val title: String,
    @DrawableRes val drawableRes: Int,
    val baby: AccessoryPlacement,
    val teen: AccessoryPlacement,
    val adult: AccessoryPlacement,
    val zOrder: Int = 0
) {
    fun placementFor(stage: String): AccessoryPlacement = when (stage) {
        "TEEN" -> teen
        "ADULT" -> adult
        else -> baby
    }
}

// Каталог аксессуаров. Позиции подобраны примерно, откалибруй после теста.
val ACCESSORIES: List<Accessory> = listOf(
    Accessory(
        id = "none",
        title = "Без аксессуара",
        drawableRes = 0,
        baby  = AccessoryPlacement(0f, 0f, 0f),
        teen  = AccessoryPlacement(0f, 0f, 0f),
        adult = AccessoryPlacement(0f, 0f, 0f),
        zOrder = -1
    ),
    Accessory(
        id = "glasses",
        title = "Очки",
        drawableRes = R.drawable.glasses,
        baby  = AccessoryPlacement(cx = 0.437f, cy = 0.446f, widthRatio = 0.9f),
        teen  = AccessoryPlacement(cx = 0.461f, cy = 0.440f, widthRatio = 0.8f),
        adult = AccessoryPlacement(cx = 0.461f, cy = 0.440f, widthRatio = 0.8f),
        zOrder = 3
    ),
    Accessory(
        id = "tie",
        title = "Галстук",
        drawableRes = R.drawable.tie,
        baby  = AccessoryPlacement(cx = 0.468f, cy = 0.635f, widthRatio = 0.17f),
        teen  = AccessoryPlacement(cx = 0.455f, cy = 0.635f, widthRatio = 0.17f),
        adult = AccessoryPlacement(cx = 0.455f, cy = 0.635f, widthRatio = 0.17f),
        zOrder = 2
    ),
    Accessory(
        id = "clown_nose",
        title = "Клоунский нос",
        drawableRes = R.drawable.clown_nose,
        baby  = AccessoryPlacement(cx = 0.466f, cy = 0.490f, widthRatio = 0.125f),
        teen  = AccessoryPlacement(cx = 0.465f, cy = 0.505f, widthRatio = 0.110f),
        adult = AccessoryPlacement(cx = 0.465f, cy = 0.505f, widthRatio = 0.110f),
        zOrder = 5
    ),
)

/**
 * Парсит строку "glasses,hat,tie" → список аксессуаров.
 * Игнорирует "none" и несуществующие id.
 */
fun parseAccessories(csv: String?): List<Accessory> {
    if (csv.isNullOrBlank() || csv == "none") return emptyList()
    return csv.split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() && it != "none" }
        .mapNotNull { findAccessory(it) }
        .sortedBy { it.zOrder }
}

/**
 * Собирает список аксессуаров в строку "glasses,hat".
 */
fun serializeAccessories(accessories: List<Accessory>): String {
    if (accessories.isEmpty()) return "none"
    return accessories.joinToString(",") { it.id }
}

fun findAccessory(id: String?): Accessory =
    ACCESSORIES.find { it.id == id } ?: ACCESSORIES.first()