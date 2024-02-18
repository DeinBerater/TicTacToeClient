package game

/** A simple coordinate of a TicTacToeField
 * */
data class FieldCoordinate(val x: Int, val y: Int) {
    constructor(index: Int) : this(index % 3, index / 3)

    init {
        if (x !in 0..2 || y !in 0..2) throw IllegalArgumentException()
    }

    fun toIndex() = 3 * y + x
}
