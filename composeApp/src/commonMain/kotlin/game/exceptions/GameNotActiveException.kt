package game.exceptions

/** Thrown when an action is invalid due to the game not being active, such as making a move while there is no opponent.
 * */
class GameNotActiveException : Exception()