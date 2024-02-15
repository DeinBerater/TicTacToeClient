package communication

import kotlinx.coroutines.channels.Channel

abstract class BaseCommunicator {
    /** The channel any incoming bytes from the websocket are sent to.
     * Only receives bytes if the websocket is connected, obviously.
     * Sends null if the websocket gets disconnected for any reason.
     * */
    val bytesIncoming = Channel<ByteArray?>()

    /** Connects the communicator with a websocket.
     * This should directly be done after the communicator is initialized, else it is useless.
     * Throws an exception if something didn't work with that.
     * */
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

    /** Sends bytes to the websocket in a new coroutine.
     * If for any reason the bytes cannot be sent, the websocket is closed and a message is printed into the console.
     * @throws WebSocketNotConnectedException if the websocket is not connected.
     * */
    protected abstract fun sendBytes(bytes: ByteArray)


}

expect fun createCommunicator(): BaseCommunicator