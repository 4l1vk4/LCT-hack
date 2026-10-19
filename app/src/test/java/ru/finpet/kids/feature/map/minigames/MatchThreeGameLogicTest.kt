package ru.finpet.kids.feature.map.minigames

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchThreeGameLogicTest {

    @Test
    fun `generateInitialBoard produces valid 10x6 board without initial matches and with valid moves`() {
        val board = MatchThreeGameLogic.generateInitialBoard()
        assertEquals(10, board.size)
        assertEquals(6, board[0].size)

        val matches = MatchThreeGameLogic.findMatches(board)
        assertTrue("Initial board should not contain 3-in-a-row matches", matches.isEmpty())
        assertTrue("Initial board must have at least one valid move", MatchThreeGameLogic.hasValidMoves(board))
    }

    @Test
    fun `findMatches finds horizontal matches`() {
        val board = MatchThreeGameLogic.createEmptyBoard()
        board[2][1] = BallColor.RED
        board[2][2] = BallColor.RED
        board[2][3] = BallColor.RED

        val matches = MatchThreeGameLogic.findMatches(board)
        assertEquals(3, matches.size)
        assertTrue(matches.contains(2 to 1))
        assertTrue(matches.contains(2 to 2))
        assertTrue(matches.contains(2 to 3))
    }

    @Test
    fun `findMatches finds vertical matches`() {
        val board = MatchThreeGameLogic.createEmptyBoard()
        board[4][3] = BallColor.BLUE
        board[5][3] = BallColor.BLUE
        board[6][3] = BallColor.BLUE
        board[7][3] = BallColor.BLUE

        val matches = MatchThreeGameLogic.findMatches(board)
        assertEquals(4, matches.size)
        assertTrue(matches.contains(4 to 3))
        assertTrue(matches.contains(5 to 3))
        assertTrue(matches.contains(6 to 3))
        assertTrue(matches.contains(7 to 3))
    }

    @Test
    fun `booster area calculation correctly targets 3x3`() {
        val center = MatchThreeGameLogic.getBoosterAffectedCells(BoosterType.AREA, 5, 3)
        assertEquals(9, center.size)

        val corner = MatchThreeGameLogic.getBoosterAffectedCells(BoosterType.AREA, 0, 0)
        assertEquals(4, corner.size)
        assertTrue(corner.contains(0 to 0))
        assertTrue(corner.contains(0 to 1))
        assertTrue(corner.contains(1 to 0))
        assertTrue(corner.contains(1 to 1))
    }

    @Test
    fun `booster column and row calculations cover whole lines`() {
        val colCells = MatchThreeGameLogic.getBoosterAffectedCells(BoosterType.COLUMN, 0, 2)
        assertEquals(10, colCells.size)
        assertTrue(colCells.all { it.second == 2 })

        val rowCells = MatchThreeGameLogic.getBoosterAffectedCells(BoosterType.ROW, 4, 0)
        assertEquals(6, rowCells.size)
        assertTrue(rowCells.all { it.first == 4 })
    }

    @Test
    fun `gravity drops balls down and fills nulls`() {
        val board = MatchThreeGameLogic.createEmptyBoard()
        board[0][0] = BallColor.YELLOW
        // bottom is null
        MatchThreeGameLogic.applyGravityAndRefill(board)
        assertEquals(BallColor.YELLOW, board[9][0])
        // and all cells above are refilled (non-null)
        for (r in 0 until 10) {
            assertTrue(board[r][0] != null)
        }
    }
}
