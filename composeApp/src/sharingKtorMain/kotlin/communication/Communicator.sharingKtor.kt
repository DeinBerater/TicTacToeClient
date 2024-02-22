package communication

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.FrameType
import io.ktor.websocket.close
import kotlinx.coroutines.isActive

class Communicator : BaseCommunicator() {
    private lateinit var websocketSession: DefaultClientWebSocketSession

    /** Connects this communicator with a websocket.
     * This should directly be done after the communicator is initialized, else it is useless.
     * Throws an exception if something didn't work with that.
     * */
    override suspend fun connectWithWebsocket() {
        val client = HttpClient(getCommunicatorEngine()) {
            install(WebSockets)
        }
        client.webSocket(getWebSocketUrl()) {
            println("Connection established.")
            websocketSession = this

            for (frame in websocketSession.incoming) {
                if (frame !is Frame.Binary) {
                    if (frame is Frame.Close) println("Received closing frame for a connection.")
                    else println("Error: Expected binary frame but received ${frame.frameType}!!!")
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
        if (!this@Communicator::websocketSession.isInitialized || !websocketSession.isActive) throw WebSocketNotConnectedException()
        doAsynchronously {
            try {
                websocketSession.send(
                    Frame.byType(
                        true, FrameType.BINARY, bytes,
                        rsv1 = false,
                        rsv2 = false,
                        rsv3 = false
                    )
                )
            } catch (e: Exception) {
                println("Error: Bytes could not be transmitted via the WebSocket:")
                e.printStackTrace()

                websocketSession.close()

                // Websocket closed, sending null.
                bytesIncoming.send(null)
            }
        }
    }

    override suspend fun closeWebSocket() {
        if (!this@Communicator::websocketSession.isInitialized) return
        websocketSession.close()
    }

}

actual fun createCommunicator(): BaseCommunicator = Communicator()
expect fun getCommunicatorEngine(): HttpClientEngineFactory<HttpClientEngineConfig>

expect fun doAsynchronously(block: suspend () -> Unit)