import communication.createCommunicator
import game.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class Player(
    private val game: Game,
    private val scope: CoroutineScope
) {
    val updateChannel = Channel<Unit>()
    private val communicator = createCommunicator()

    init {
        scope.launch {
            communicator.connectWithWebsocket()
            listenToIncomingBytes()
        }
    }

    private suspend fun listenToIncomingBytes() {
        for (incoming in communicator.bytesIncoming) {
            // Do action when bytes arrive
            val type = incoming.readInt(3)
        }
    }

    private fun updateUi() {
        scope.launch {
            updateChannel.send(Unit)
        }
    }


    fun makeMove() {

    }

    fun submitGameCode(gameCode: String) = communicator.sendSubmitGameCode(gameCode)
}