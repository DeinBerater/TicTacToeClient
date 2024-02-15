package game

import game.exceptions.FieldAlreadyOccupiedException
import game.exceptions.GameNotActiveException
import game.exceptions.NotOnTurnException

class Game {
    init {
        println("Game created.")
    }

    /** If the player playing this game is on turn */
    var onTurn: Boolean = true

    /** The symbol of the player playing this game */
    var symbol: TicTacToeSymbol? = null

    /** If this game is currently active, meaning if symbols can be placed */
    var gameActive: Boolean = false
        private set

    var hasOpponent: Boolean = false

    private val fields = MutableList<TicTacToeSymbol?>(9) { _ -> null }

    fun setGameActive(symbol: TicTacToeSymbol) {
        gameActive = true
        this.symbol = symbol
    }

    fun deactivateGame() {
        gameActive = false
    }

    /** Sets a symbol on the board (make a move)
     * @param position the position to set a symbol on
     * @param opponent if the player or the opponent makes the move
     * @throws GameNotActiveException if the game is not active, meaning nothing can be placed
     * @throws NotOnTurnException if the player playing this game is making a move and is not on turn
     * @throws FieldAlreadyOccupiedException if the field is already occupied
     * */
    @Throws(
        GameNotActiveException::class,
        NotOnTurnException::class,
        FieldAlreadyOccupiedException::class
    )
    fun makeMove(position: Int, opponent: Boolean) {
        if (!gameActive) throw GameNotActiveException()
        if (!opponent && !onTurn) throw NotOnTurnException()
        if (fields[position] != null) throw FieldAlreadyOccupiedException()
        fields[position] = if (!opponent) symbol else symbol?.other()
    }

    /** Updates the board with full data
     * @param symbols the list of symbols the new board should have
     * */
    fun updateBoard(symbols: List<TicTacToeSymbol?>) {
        symbols.forEachIndexed { index, symbol ->
            fields[index] = symbol
        }
    }

    /** Gets the symbol by coordinates (starting at top-left corner with 0, 0)
     * */
    fun getSymbolByCoords(x: Int, y: Int): TicTacToeSymbol? {
        if (x !in 0..2 || y !in 0..2) throw IllegalArgumentException()
        val index = 3 * y + x
        return fields[index]
    }
}