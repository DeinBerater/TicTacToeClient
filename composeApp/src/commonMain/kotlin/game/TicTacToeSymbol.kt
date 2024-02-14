package game

enum class TicTacToeSymbol {
    X, O;

    /** Get the other symbol.
     * */
    fun other(): TicTacToeSymbol {
        return when (this) {
            X -> O
            O -> X
        }
    }
}