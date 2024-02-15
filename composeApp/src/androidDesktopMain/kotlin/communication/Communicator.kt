package communication

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.FrameType
import io.ktor.websocket.close
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Communicator : BaseCommunicator() {
    private lateinit var websocketSession: DefaultClientWebSocketSession

    /** Connects this communicator with a websocket.
     * This should directly be done after the communicator is initialized, else it is useless.
     * Throws an exception if something didn't work with that.
     * */
    override suspend fun connectWithWebsocket() {
        val client = HttpClient(CIO) {
            install(WebSockets)
        }
        client.webSocket("ws://192.168.178.121:80") {
            websocketSession = this

            for (frame in incoming) {
                if (frame !is Frame.Binary) {
                    println("Error: Received non-binary frame but expected binary!!!")
                    continue
                }
                bytesIncoming.send(frame.data)
            }

            // Websocket closed, sending null.
            bytesIncoming.send(null)
        }
    }

    /** Sends bytes to the websocket in a new coroutine.
     * If for any reason the bytes cannot be sent, the websocket is closed and a message is printed into the console.
     * @throws WebSocketNotConnectedException if the websocket is not connected.
     * */
    @Throws(WebSocketNotConnectedException::class)
    override fun sendBytes(bytes: ByteArray) {
        runBlocking {
            if (!this@Communicator::websocketSession.isInitialized || !websocketSession.isActive) throw WebSocketNotConnectedException()
            launch {
                try {
                    websocketSession.send(
                        Frame.byType(
                            false, FrameType.BINARY, bytes,
                            rsv1 = false,
                            rsv2 = false,
                            rsv3 = false
                        )
                    )
                } catch (e: Exception) {
                    println("Error: Bytes could not be transmitted via the websocket:")
                    e.printStackTrace()

                    websocketSession.close()

                    // Websocket closed, sending null.
                    bytesIncoming.send(null)
                }
            }
        }
    }

}

actual fun createCommunicator(): BaseCommunicator = Communicator()