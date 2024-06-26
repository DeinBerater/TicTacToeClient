package game

import game.exceptions.GameNotActiveException
import game.exceptions.NotOnTurnException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue


class GameTest {

    @Test
    fun getOnTurn() {
        val sut = Game()

        assertTrue(sut.onTurn)
    }

    @Test
    fun setOnTurn() {
        val sut = Game()

        sut.onTurn = false

        assertFalse(sut.onTurn)
    }

    @Test
    fun hasOpponent() {
        val sut = Game()

        assertFalse(sut.hasOpponent)
    }

    @Test
    fun setOpponent() {
        val sut = Game()
        sut.hasOpponent = true

        assertTrue(sut.hasOpponent)
    }

    @Test
    fun gameCode() {
        val sut = Game()

        assertNull(sut.gameCode)
    }

    @Test
    fun setGameCode() {
        val sut = Game()
        val gameCode = "ABCDE"
        sut.gameCode = gameCode

        assertEquals(gameCode, sut.gameCode)
    }

    @Test
    fun getSymbolBeginning() {
        val sut = Game()

        assertNull(sut.symbol)
    }

    @Test
    fun setSymbol() {
        val sut = Game()
        val symbol = TicTacToeSymbol.X

        sut.symbol = symbol

        assertEquals(symbol, sut.symbol)
    }

    @Test
    fun getGameActive() {
        val sut = Game()

        assertFalse(sut.gameActive)
    }

    @Test
    fun setGameActive() {
        val sut = Game()
        val symbol = TicTacToeSymbol.X

        sut.setGameActive(symbol)

        assertTrue(sut.gameActive)
        assertEquals(symbol, sut.symbol)
    }

    @Test
    fun makeMoveNotActive() {
        val sut = Game()

        val position = 0
        val opponent = false

        assertFailsWith(GameNotActiveException::class) {
            sut.makeMove(FieldCoordinate(position), opponent)
        }
    }

    @Test
    fun getSymbolByCoordsEmpty() {
        val sut = Game()
        val have = sut.getSymbolByCoords(0, 0)

        assertNull(have)
    }

    @Test
    fun makeMove() {
        val sut = Game()
        val symbol = TicTacToeSymbol.O

        sut.setGameActive(symbol)

        val position = 0
        val opponent = false

        sut.makeMove(FieldCoordinate(position), opponent)

        val have = sut.getSymbolByCoords(0, 0)

        assertEquals(symbol, have)
    }

    @Test
    fun makeMoveTurnChange() {
        val sut = Game()
        val symbol = TicTacToeSymbol.O

        sut.setGameActive(symbol)

        val position = 0
        val opponent = false

        sut.makeMove(FieldCoordinate(position), opponent)

        assertFalse(sut.onTurn)
    }

    @Test
    fun makeMoveOpponent() {
        val sut = Game()
        val symbol = TicTacToeSymbol.O
        sut.setGameActive(symbol)

        val position = 0
        val opponent = true

        sut.makeMove(FieldCoordinate(position), opponent)
        val have = sut.getSymbolByCoords(0, 0)
        val want = TicTacToeSymbol.X

        assertEquals(want, have)
    }

    @Test
    fun makeMoveNotOnTurn() {
        val sut = Game()
        val symbol = TicTacToeSymbol.O
        sut.setGameActive(symbol)
        sut.onTurn = false

        val position = 0
        val opponent = false

        assertFailsWith(NotOnTurnException::class) {
            sut.makeMove(FieldCoordinate(position), opponent)
        }
    }

    @Test
    fun deactivateGame() {
        val sut = Game()
        assertFalse(sut.gameActive)

        sut.setGameActive(TicTacToeSymbol.X)
        assertTrue(sut.gameActive)

        sut.deactivateGame()
        assertFalse(sut.gameActive)
    }

    @Test
    fun updateBoard() {
        val newSymbols =
            listOf(null, null, TicTacToeSymbol.X, null, null, null, TicTacToeSymbol.O, null, null)
        val sut = Game()
        sut.updateBoard(newSymbols)

        assertNull(sut.getSymbolByCoords(2, 1))
        assertEquals(sut.getSymbolByCoords(2, 0), TicTacToeSymbol.X)
        assertEquals(sut.getSymbolByCoords(0, 2), TicTacToeSymbol.O)
    }

    @Test
    fun getSymbolByCoordsTooBig() {
        val sut = Game()

        assertFailsWith(IllegalArgumentException::class) {
            sut.getSymbolByCoords(3, 1)
        }
    }

    @Test
    fun getSymbolByCoordsNegative() {
        val sut = Game()

        assertFailsWith(IllegalArgumentException::class) {
            sut.getSymbolByCoords(1, -1)
        }
    }

    @Test
    fun updateBoardAndGetWinners() {
        val newSymbols =
            listOf(null, null, TicTacToeSymbol.X, null, null, null, TicTacToeSymbol.O, null, null)
        val sut = Game()
        sut.updateBoard(newSymbols)

        assertNull(sut.getSymbolByCoords(2, 1))
        assertEquals(sut.getSymbolByCoords(2, 0), TicTacToeSymbol.X)
        assertEquals(sut.getSymbolByCoords(0, 2), TicTacToeSymbol.O)

        assertNull(sut.winner())
    }

    @Test
    fun updateBoardAndGetWinners2() {
        val newSymbols =
            listOf(
                null,
                null,
                TicTacToeSymbol.X,
                null,
                TicTacToeSymbol.X,
                TicTacToeSymbol.O,
                TicTacToeSymbol.X,
                null,
                TicTacToeSymbol.O
            )
        val sut = Game()

        sut.updateBoard(newSymbols)

        val want = listOf(FieldCoordinate(2, 0), FieldCoordinate(1, 1), FieldCoordinate(0, 2))
        val have = sut.winner()
        assertEquals(want, have)
    }

    @Test
    fun updateBoardAndGetWinners3() {
        val newSymbols =
            listOf(
                TicTacToeSymbol.O,
                TicTacToeSymbol.O,
                TicTacToeSymbol.O,
                null,
                TicTacToeSymbol.X,
                TicTacToeSymbol.X,
                TicTacToeSymbol.X,
                null,
                null
            )
        val sut = Game()

        sut.updateBoard(newSymbols)

        val want = listOf(FieldCoordinate(0, 0), FieldCoordinate(1, 0), FieldCoordinate(2, 0))
        val have = sut.winner()
        assertEquals(want, have)
    }

    @Test
    fun updateBoardAndGetWinners4() {
        val newSymbols =
            listOf(
                TicTacToeSymbol.O,
                TicTacToeSymbol.X,
                TicTacToeSymbol.X,
                TicTacToeSymbol.O,
                null,
                TicTacToeSymbol.X,
                TicTacToeSymbol.O,
                null,
                null
            )
        val sut = Game()

        sut.updateBoard(newSymbols)

        val want = listOf(FieldCoordinate(0, 0), FieldCoordinate(0, 1), FieldCoordinate(0, 2))
        val have = sut.winner()
        assertEquals(want, have)
    }

    @Test
    fun updateBoardAndGetWinners5() {
        val newSymbols =
            listOf(
                TicTacToeSymbol.X,
                TicTacToeSymbol.O,
                TicTacToeSymbol.O,
                TicTacToeSymbol.O,
                TicTacToeSymbol.X,
                null,
                TicTacToeSymbol.X,
                null,
                TicTacToeSymbol.X
            )
        val sut = Game()

        sut.updateBoard(newSymbols)

        val want = listOf(FieldCoordinate(0, 0), FieldCoordinate(1, 1), FieldCoordinate(2, 2))
        val have = sut.winner()
        assertEquals(want, have)
    }
}