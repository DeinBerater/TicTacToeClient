package communication

import kotlinx.coroutines.channels.Channel

abstract class BaseCommunicator {
    // Available after connection with websocket
    lateinit var bytesIncoming: Channel<ByteDeconstructor>

    abstract suspend fun connectWithWebsocket()

    fun sendSubmitGameCode(gameCode: String) {

    }

    fun sendRequestCurrentStatus() {

    }

    fun sendMakeMove() {

    }

    fun sendResetBoard() {

    }

    fun sendToggleSymbol() {

    }

    protected abstract suspend fun sendBytes(bytes: ByteArray)


}

expect fun createCommunicator(): BaseCommunicator