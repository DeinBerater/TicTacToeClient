import communication.ByteDeconstructor
import communication.createCommunicator
import game.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class Player(
    private val game: Game,
    private val scope: CoroutineScope
) {
    val updateChannel = Channel<String?>()
    private val communicator = createCommunicator()

    init {
        scope.launch {
            try {
                communicator.connectWithWebsocket()
                println("Now connected to WebSocket!")
                communicator.initializeEventListeners(scope)
            } catch (e: Exception) {
                updateChannel.send("Cannot connect to WebSocket: ${e::class.simpleName}")
                e.printStackTrace()
                return@launch
            }

            listenToIncomingBytes()
        }
    }

    private suspend fun listenToIncomingBytes() {
        for (incoming in communicator.bytesIncoming) {
            if (incoming == null) {
                updateChannel.send("Due to an error, the connection has been closed.")
                return
            }

            println("Received data from server: ${incoming.toList()}")
            // ToDo: Do action when bytes arrive
            val type = ByteDeconstructor(incoming).readInt(3)
        }
    }

    private fun updateUi() {
        scope.launch {
            updateChannel.send(null)
        }
    }


    fun makeMove() {

    }

    fun submitGameCode(gameCode: String) = communicator.sendSubmitGameCode(gameCode)
}