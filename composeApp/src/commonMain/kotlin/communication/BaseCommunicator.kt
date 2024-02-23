package communication

import OutgoingPacketType
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

    protected fun getWebSocketUrl(): String {
        return "ws://192.168.8.86:80"
    }

    fun sendSubmitGameCode(gameCode: String) {
        val byteBuilder = ByteBuilder().addInt(OutgoingPacketType.CodeSubmit.ordinal, 3)
        gameCode.forEach {
            byteBuilder.addInt(it - 'A', 5)
        }
        sendBytes(byteBuilder.getBytes())
    }

    fun sendRequestCurrentStatus() = sendEmptyPacket(OutgoingPacketType.RequestCurrentStatus)

    fun sendMakeMove(position: Int) {
        sendBytes(
            ByteBuilder().addInt(OutgoingPacketType.PlayerMakeMove.ordinal, 3).addInt(position, 4)
                .getBytes()
        )
    }

    fun sendResetBoard() = sendEmptyPacket(OutgoingPacketType.BoardReset)

    fun sendToggleSymbol() = sendEmptyPacket(OutgoingPacketType.ToggleSymbol)

    private fun sendEmptyPacket(type: OutgoingPacketType) {
        sendBytes(ByteBuilder().addInt(type.ordinal, 3).getBytes())
    }

    /** Sends bytes to the websocket in a new coroutine.
     * If for any reason the bytes cannot be sent, the websocket is closed and a message is printed into the console.
     * @throws WebSocketNotConnectedException if the websocket is not connected.
     * */
    protected abstract fun sendBytes(bytes: ByteArray)

    /** Disconnects the WebSocket, if initialized/connected
     * */
    abstract suspend fun closeWebSocket()


}


expect fun createCommunicator(): BaseCommunicator