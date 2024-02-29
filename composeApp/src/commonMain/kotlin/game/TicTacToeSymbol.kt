package game

enum class TicTacToeSymbol {
    O, X;

    /** Get the other symbol.
     * */
    fun other(): TicTacToeSymbol {
        return when (this) {
            X -> O
            O -> X
        }
    }

    fun asEmoji(): String {
        return when (this) {
            X -> "❌"
            O -> "⭕"
        }
    }
}