import communication.ByteDeconstructor
import communication.createCommunicator
import game.Game
import game.TicTacToeSymbol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class Player(
    private val scope: CoroutineScope
) {
    val updateChannel = Channel<String?>()
    private val game = Game()

    private val communicator = createCommunicator()

    private val gameCodeCharRange: CharRange = ('A'..'Z')

    init {
        scope.launch {
            try {
                println("Trying to connect to WebSocket...")
                // This action is blocking.
                communicator.connectWithWebsocket()
            } catch (e: Exception) {
                updateChannel.send("Error in WebSocket connection: ${e::class.simpleName}")
                e.printStackTrace()
                return@launch
            }
        }
        scope.launch {
            listenToIncomingBytes()
        }
    }

    /** Get the game */
    fun game() = game

    private fun updateUi() {
        scope.launch {
            updateChannel.send(null)
        }
    }

    private suspend fun listenToIncomingBytes() {
        println("Listening to incoming bytes...")
        for (incoming in communicator.bytesIncoming) {
            if (incoming == null) {
                updateChannel.send("Due to an error, the connection has been closed.")
                return
            }

            println("Received data from server: ${incoming.toList()}")

            val byteDeconstructor = ByteDeconstructor(incoming)

            try {
                when (byteDeconstructor.readInt(3)) {
                    0 -> onWelcome(byteDeconstructor) // Welcome
                    1 -> updateChannel.send("Sorry, there was a problem with sending data :/") // PacketInvalid
                    2 -> onOpponentMakeMove(
                        byteDeconstructor.readInt(4),
                        byteDeconstructor.readBoolean()
                    ) // OpponentMakeMove
                    3 -> game.hasOpponent = false // OpponentLeave
                    4 -> updateGame(byteDeconstructor) // GameInfo
                    5 -> updateChannel.send("You cannot do this right now.") // ActionInvalid
                    6 -> updateChannel.send("This game code is invalid.") // GameCodeInvalid
                    7 -> updateChannel.send("The game is full, please wait or join another game.") // GameFull
                }
            } catch (e: Exception) {
                updateChannel.send("Something went wrong...")
                e.printStackTrace()
            }
        }
    }

    private fun onWelcome(byteDeconstructor: ByteDeconstructor) {
        var gameCode = ""
        repeat(5) {
            val char = gameCodeCharRange.first + byteDeconstructor.readInt(5)
            gameCode += char
        }
        game.gameCode = gameCode
        updateUi()
    }

    private fun onOpponentMakeMove(position: Int, justWon: Boolean) {
        game.makeMove(position, true)

        // ToDo: Handle just won

        updateUi()
    }

    private fun updateGame(byteDeconstructor: ByteDeconstructor) {
        val symbol = getSymbolByBoolean(byteDeconstructor.readBoolean())
        game.onTurn = byteDeconstructor.readBoolean()
        game.hasOpponent = byteDeconstructor.readBoolean()
        if (byteDeconstructor.readBoolean()) game.setGameActive(symbol) else game.symbol = symbol

        val boardFields = mutableListOf<TicTacToeSymbol?>()
        // Build board
        repeat(9) {
            if (!byteDeconstructor.readBoolean()) {
                boardFields.add(null)
                return@repeat
            }
            boardFields.add(getSymbolByBoolean(byteDeconstructor.readBoolean()))
        }
        byteDeconstructor.finish()
        game.updateBoard(boardFields)

        updateUi()
    }

    private fun getSymbolByBoolean(boolean: Boolean): TicTacToeSymbol {
        return if (boolean) TicTacToeSymbol.X else TicTacToeSymbol.O
    }


    /** Makes a move on the board
     * **Might throw an exception, which has to be caught!**
     * */
    fun makeMove(x: Int, y: Int) {
        val pos = 3 * y + x
        game.makeMove(pos, false)
        updateUi()
        communicator.sendMakeMove(pos)
    }

    fun submitGameCode(gameCode: String) {
        if (!gameCode.all { gameCodeCharRange.contains(it) }) throw IllegalArgumentException("The game code should just contain uppercase letters (A - Z).")
        if (gameCode.length != 5) throw IllegalArgumentException("The game code should have a length of 5.")

        communicator.sendSubmitGameCode(gameCode)
    }

    fun resetBoard() = communicator.sendResetBoard()
    fun toggleSymbol() = communicator.sendToggleSymbol()
}