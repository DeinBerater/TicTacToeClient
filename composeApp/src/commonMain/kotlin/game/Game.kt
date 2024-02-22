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
        set(value) {
            if (!value) deactivateGame()
            field = value
        }

    /** The game code */
    var gameCode: String? = null

    private val fields = MutableList<TicTacToeSymbol?>(9) { _ -> null }

    // ToDo: Win Logic and scoreboard

    fun setGameActive(symbol: TicTacToeSymbol) {
        gameActive = true
        this.symbol = symbol
    }

    fun deactivateGame() {
        gameActive = false
    }

    /** Sets a symbol on the board (make a move)
     * @param coordinate the position to set a symbol on
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
    fun makeMove(coordinate: FieldCoordinate, opponent: Boolean) {
        if (!gameActive) throw GameNotActiveException()
        if (!opponent && !onTurn) throw NotOnTurnException()

        val position = coordinate.toIndex()
        if (fields[position] != null) throw FieldAlreadyOccupiedException()
        fields[position] = if (!opponent) symbol else symbol?.other()

        // Turns change. If the opponent just made a move, then this player is on turn.
        if (winner() == null) {
            onTurn = opponent
        } else gameActive = false
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
        return fields[FieldCoordinate(x, y).toIndex()]
    }


    /** Checks if a player has won the game.
     * @return If somebody has won, the row of the fields in which the win happened
     * */
    fun winner(): List<FieldCoordinate>? {

        // Check horizontally
        var fieldsToCheck = intArrayOf(0, 1, 2)
        for (i in 0..2) {
            val currentFieldsChecking = fieldsToCheck.map { it + i * 3 }.toIntArray() // Move by 3
            // If the symbols are equal, there have to be symbols on each field, hence !!
            if (fieldSymbolsEqual(*currentFieldsChecking)) return currentFieldsChecking.map {
                FieldCoordinate(
                    it
                )
            }
        }

        // Check vertically
        fieldsToCheck = intArrayOf(0, 3, 6)
        for (i in 0..2) {
            val currentFieldsChecking = fieldsToCheck.map { it + i }.toIntArray() // Move by 1
            // If the symbols are equal, there have to be symbols on each field, hence !!
            if (fieldSymbolsEqual(*currentFieldsChecking)) return currentFieldsChecking.map {
                FieldCoordinate(
                    it
                )
            }
        }

        // Check diagonally
        if (fieldSymbolsEqual(0, 4, 8)) return listOf(0, 4, 8).map { FieldCoordinate(it) }
        else if (fieldSymbolsEqual(2, 4, 6)) return listOf(2, 4, 6).map { FieldCoordinate(it) }

        // No win
        return null
    }

    /** Checks if multiple fields have the same symbol.
     * @param fieldIndexes the index of the fields (0-8) to check.
     * @return true if multiple fields have the same symbol (_not null!!_), false otherwise.
     * */
    private fun fieldSymbolsEqual(vararg fieldIndexes: Int): Boolean {
        val symbolToCheck = fields[fieldIndexes.first()] ?: return false
        return fieldIndexes.drop(1).all { fields[it] == symbolToCheck }
    }
}