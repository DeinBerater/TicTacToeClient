import communication.ByteDeconstructor
import communication.createCommunicator
import game.FieldCoordinate
import game.Game
import game.TicTacToeSymbol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class Player(
    private val scope: CoroutineScope
) {
    val updateChannel = Channel<String?>()
    private var game = Game()
    private var communicator = createCommunicator()

    private var lastGameCodeEntered: String? = null

    private var communicatorClosedPurposely = false

    init {
        connectWithWebSocket()
    }

    private fun connectWithWebSocket() {
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
        println("Updating ui...")
        scope.launch {
            updateChannel.send(null)
        }
    }

    suspend fun closeConnection() {
        communicator.closeWebSocket()
        communicatorClosedPurposely = true
    }

    fun restartConnection() {
        scope.launch {
            closeConnection()
            communicator = createCommunicator()
            connectWithWebSocket()
            game = game()
        }
    }

    private suspend fun listenToIncomingBytes() {
        println("Listening to incoming bytes...")
        for (incoming in communicator.bytesIncoming) {
            if (incoming == null) {
                if (!communicatorClosedPurposely) updateChannel.send("Error in WebSocket connection: The connection has been closed.")
                return
            }

            println("Received data from server: ${incoming.toList()}")

            val byteDeconstructor = ByteDeconstructor(incoming)

            try {
                when (byteDeconstructor.readInt(3)) {
                    0 -> onWelcome(byteDeconstructor) // Welcome
                    1 -> updateChannel.send("Sorry, there was a problem with sending data :/") // PacketInvalid
                    2 -> onOpponentMakeMove(
                        byteDeconstructor.readInt(4)
                    ) // OpponentMakeMove
                    3 -> {
                        game.hasOpponent = false; updateUi()
                    } // OpponentLeave
                    4 -> updateGame(byteDeconstructor) // GameInfo
                    5 -> updateChannel.send("You cannot do this right now.") // ActionInvalid
                    6 -> onGameCodeInvalid() // GameCodeInvalid
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
        lastGameCodeEntered = gameCode

        updateUi()
    }

    private fun onOpponentMakeMove(position: Int) {
        game.makeMove(FieldCoordinate(position), true)

        updateUi()
    }

    private fun updateGame(byteDeconstructor: ByteDeconstructor) {
        val symbol = getSymbolByBoolean(byteDeconstructor.readBoolean())
        game.onTurn = byteDeconstructor.readBoolean()
        game.hasOpponent = byteDeconstructor.readBoolean()
        if (byteDeconstructor.readBoolean()) game.setGameActive(symbol) else game.symbol = symbol
        game.gameCode = lastGameCodeEntered

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

    private suspend fun onGameCodeInvalid() {
        updateChannel.send("This game code is invalid.") // GameCodeInvalid
        lastGameCodeEntered = game.gameCode // Set the last code back for it not to update.
    }

    private fun getSymbolByBoolean(boolean: Boolean): TicTacToeSymbol {
        return if (boolean) TicTacToeSymbol.X else TicTacToeSymbol.O
    }


    /** Makes a move on the board
     * **Might throw an exception, which has to be caught!**
     * */
    fun makeMove(x: Int, y: Int) {
        val pos = FieldCoordinate(x, y)
        game.makeMove(pos, false)
        updateUi()
        communicator.sendMakeMove(pos.toIndex())
    }

    fun submitGameCode(gameCode: String) {
        validateGameCode(gameCode)

        val gameCodeUpperCase = gameCode.uppercase()
        lastGameCodeEntered = gameCodeUpperCase

        communicator.sendSubmitGameCode(gameCodeUpperCase)
    }

    companion object {
        private val gameCodeCharRange: CharRange = ('A'..'Z')

        @Throws(IllegalArgumentException::class)
        fun validateGameCode(gameCode: String) {
            if (!gameCode.uppercase()
                    .all { gameCodeCharRange.contains(it) }
            ) throw IllegalArgumentException(
                "The game code should just contain letters (A - Z)."
            )
            if (gameCode.length != 5) throw IllegalArgumentException("The game code should have a length of 5.")
        }
    }

    fun resetBoard() = communicator.sendResetBoard()
    fun toggleSymbol() = communicator.sendToggleSymbol()
}