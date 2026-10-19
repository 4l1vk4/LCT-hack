package ru.finpet.kids.feature.map.minigames

import ru.finpet.kids.R
import kotlin.math.abs
import kotlin.random.Random

enum class BallColor(val drawableRes: Int, val id: Int) {
    BLUE(R.drawable.blue_ball, 0),
    GREEN(R.drawable.green_ball, 1),
    RED(R.drawable.red_ball, 2),
    YELLOW(R.drawable.yellow_ball, 3);

    companion object {
        fun random(rng: Random = Random.Default): BallColor =
            entries[rng.nextInt(entries.size)]
    }
}

object MatchThreeGameLogic {
    const val ROWS = 10
    const val COLS = 6

    fun createEmptyBoard(): Array<Array<BallColor?>> {
        return Array(ROWS) { arrayOfNulls<BallColor>(COLS) }
    }

    /**
     * Создает начальное игровое поле 10x6:
     * - Без готовых совпадений 3-в-ряд на старте
     * - С гарантией наличия хотя бы одного возможного хода
     */
    fun generateInitialBoard(rng: Random = Random.Default): Array<Array<BallColor?>> {
        var attempts = 0
        while (attempts < 200) {
            attempts++
            val board = Array(ROWS) { arrayOfNulls<BallColor>(COLS) }

            // Заполняем с проверкой по ходу генерации
            for (r in 0 until ROWS) {
                for (c in 0 until COLS) {
                    val forbidden = mutableSetOf<BallColor>()
                    val c1 = board[r].getOrNull(c - 1)
                    val c2 = board[r].getOrNull(c - 2)
                    if (c >= 2 && c1 != null && c1 == c2) {
                        forbidden.add(c1)
                    }
                    val r1 = board.getOrNull(r - 1)?.get(c)
                    val r2 = board.getOrNull(r - 2)?.get(c)
                    if (r >= 2 && r1 != null && r1 == r2) {
                        forbidden.add(r1)
                    }
                    val allowed = BallColor.entries.filter { it !in forbidden }
                    board[r][c] = allowed[rng.nextInt(allowed.size)]
                }
            }

            if (findMatches(board).isEmpty() && hasValidMoves(board)) {
                return board
            }
        }

        // Fallback: детерминированная шахматная раскладка с гарантированным ходом
        val fallback = Array(ROWS) { r ->
            Array(COLS) { c ->
                val colorIdx = (r + c) % 4
                BallColor.entries[colorIdx] as BallColor?
            }
        }
        fallback[0][0] = BallColor.BLUE
        fallback[0][1] = BallColor.GREEN
        fallback[0][2] = BallColor.BLUE
        fallback[1][1] = BallColor.BLUE
        return fallback
    }

    /**
     * Поиск всех совпадающих шариков (3 или более подряд по горизонтали или вертикали)
     */
    fun findMatches(board: Array<Array<BallColor?>>): Set<Pair<Int, Int>> {
        val matched = mutableSetOf<Pair<Int, Int>>()

        // Проверка по горизонтали
        for (r in 0 until ROWS) {
            var c = 0
            while (c < COLS) {
                val color = board[r][c]
                if (color == null) {
                    c++
                    continue
                }
                var runLength = 1
                while (c + runLength < COLS && board[r][c + runLength] == color) {
                    runLength++
                }
                if (runLength >= 3) {
                    for (i in 0 until runLength) {
                        matched.add(r to (c + i))
                    }
                }
                c += runLength
            }
        }

        // Проверка по вертикали
        for (c in 0 until COLS) {
            var r = 0
            while (r < ROWS) {
                val color = board[r][c]
                if (color == null) {
                    r++
                    continue
                }
                var runLength = 1
                while (r + runLength < ROWS && board[r + runLength][c] == color) {
                    runLength++
                }
                if (runLength >= 3) {
                    for (i in 0 until runLength) {
                        matched.add((r + i) to c)
                    }
                }
                r += runLength
            }
        }

        return matched
    }

    /**
     * Проверка, образует ли обмен двух соседних клеток совпадение
     */
    fun isValidSwap(board: Array<Array<BallColor?>>, r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        if (abs(r1 - r2) + abs(c1 - c2) != 1) return false
        if (r1 !in 0 until ROWS || r2 !in 0 until ROWS || c1 !in 0 until COLS || c2 !in 0 until COLS) return false
        val b1 = board[r1][c1] ?: return false
        val b2 = board[r2][c2] ?: return false
        if (b1 == b2) return false

        // Проверяем обмен
        board[r1][c1] = b2
        board[r2][c2] = b1

        val matches = findMatches(board)

        // Откатываем обмен
        board[r1][c1] = b1
        board[r2][c2] = b2

        return matches.isNotEmpty()
    }

    /**
     * Проверка наличия хотя бы одного допустимого хода на поле
     */
    fun hasValidMoves(board: Array<Array<BallColor?>>): Boolean {
        for (r in 0 until ROWS) {
            for (c in 0 until COLS) {
                if (c + 1 < COLS && isValidSwap(board, r, c, r, c + 1)) return true
                if (r + 1 < ROWS && isValidSwap(board, r, c, r + 1, c)) return true
            }
        }
        return false
    }

    /**
     * Перемешивание поля при отсутствии ходов
     */
    fun shuffleBoard(board: Array<Array<BallColor?>>, rng: Random = Random.Default) {
        val allBalls = mutableListOf<BallColor>()
        for (r in 0 until ROWS) {
            for (c in 0 until COLS) {
                board[r][c]?.let { allBalls.add(it) }
            }
        }

        var attempts = 0
        while (attempts < 100) {
            attempts++
            allBalls.shuffle(rng)
            var idx = 0
            for (r in 0 until ROWS) {
                for (c in 0 until COLS) {
                    if (idx < allBalls.size) {
                        board[r][c] = allBalls[idx++]
                    }
                }
            }
            if (findMatches(board).isEmpty() && hasValidMoves(board)) {
                return
            }
        }
    }

    /**
     * Применение гравитации и заполнение освободившихся верхних позиций новыми шариками
     */
    fun applyGravityAndRefill(
        board: Array<Array<BallColor?>>,
        rng: Random = Random.Default
    ) {
        for (c in 0 until COLS) {
            var writeRow = ROWS - 1
            for (r in (ROWS - 1) downTo 0) {
                val ball = board[r][c]
                if (ball != null) {
                    if (writeRow != r) {
                        board[writeRow][c] = ball
                        board[r][c] = null
                    }
                    writeRow--
                }
            }
            // Заполняем верхние пустые ячейки новыми шариками
            for (r in writeRow downTo 0) {
                board[r][c] = BallColor.random(rng)
            }
        }
    }

    /**
     * Очистка шариков по правилу бустера
     */
    fun getBoosterAffectedCells(type: BoosterType, targetR: Int, targetC: Int): Set<Pair<Int, Int>> {
        return when (type) {
            BoosterType.SINGLE -> {
                if (targetR in 0 until ROWS && targetC in 0 until COLS) setOf(targetR to targetC)
                else emptySet()
            }
            BoosterType.COLUMN -> {
                if (targetC in 0 until COLS) {
                    (0 until ROWS).map { r -> r to targetC }.toSet()
                } else emptySet()
            }
            BoosterType.ROW -> {
                if (targetR in 0 until ROWS) {
                    (0 until COLS).map { c -> targetR to c }.toSet()
                } else emptySet()
            }
            BoosterType.AREA -> {
                val cells = mutableSetOf<Pair<Int, Int>>()
                for (r in (targetR - 1)..(targetR + 1)) {
                    for (c in (targetC - 1)..(targetC + 1)) {
                        if (r in 0 until ROWS && c in 0 until COLS) {
                            cells.add(r to c)
                        }
                    }
                }
                cells
            }
        }
    }
}
